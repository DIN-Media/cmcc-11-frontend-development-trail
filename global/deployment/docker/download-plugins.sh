#!/usr/bin/env bash

#
# Downloads plugins from given descriptor urls.
#
# Requires: bash, curl, jq, unzip
#
# Usage: ./download-plugins.sh DESCRIPTOR_URL...
#
# The descriptors and plugins are downloaded into the following file structure:
# .
# ├── plugin-descriptors (Containing the downloaded descriptor json files)
# └── plugins
#     ├── studio-client (Containing the extracted studio-client packages providing additional studio-apps)
#     ├── studio-client.main (Containing the extracted studio-client packages for the main app)
#     ├── studio-client.workflow (Containing the extracted studio-client packages for the workflow app)
#     ├── studio-server (Containing the studio-server plugin zip files)
#     ├── other-java-app (Same as for studio-server)
#     └── ...
#
# After execution the plugins are ready to be mounted into the respective docker containers (see compose/plugins.yml).
# BE AWARE that this script runs in the current working directory and cleans the directories listed above!
#
# GitHub Authentication:
# When an environment variable 'GITHUB_TOKEN' is set, it is used as Authorization token for github.com urls.
#
# GitHub Actions Artifacts:
# It is also possible to provide urls to GitHub Actions Runs. The first artifact attached to the given run will be
# downloaded and extracted. It is expected to contain a descriptor json file and the plugin zip files. The descriptor is
# expected to reference the plugin zips not with the 'url' but with the 'file' key using the filename as the value.
# For this integration a GITHUB_TOKEN with the necessary permissions is mandatory.
#

set -euo pipefail

descriptor_urls="$@"

if [ -z "${descriptor_urls}" ]; then
  echo "Missing arguments!"
  echo "Usage: ./download-plugins.sh DESCRIPTOR_URL..."
  exit 1
fi

rm -rf 'plugin-descriptors' 'plugins'
mkdir 'plugin-descriptors' 'plugins'
# make sure to create all folders that will be mounted as docker volumes to prevent permission issues
mkdir -p 'plugins/studio-server' 'plugins/studio-client' 'plugins/studio-client.main' 'plugins/studio-client.workflow' 'plugins/headless-server' 'plugins/content-feeder' 'plugins/content-server' 'plugins/cae'

function main() {
  download_descriptors
  descriptors=$(find plugin-descriptors -name *.json)

  echo "Descriptors:"
  echo "${descriptors}"

  for descriptor in ${descriptors} ; do
    download_plugins ${descriptor}
  done

  echo "Plugins:"
  find plugins -mindepth 2 -maxdepth 2 -type f -printf '%P\n'

  extract_plugins
}

function download_descriptors() {
  (
    cd plugin-descriptors

    for descriptor_url in ${descriptor_urls}; do
      download_descriptor ${descriptor_url}
    done

    extract_bundles
  )
}

function download_descriptor() {
  url="${1}"

  if [[ "${url}" =~ https://github.com/.*/actions/runs/.* ]]; then
    download_github_action_artifact ${url}
  elif [[ "${url}" =~ https://github.com/.* ]]; then
    download_github_release_artifact ${url}
  else
    echo "Downloading ${url}"
    curl -s -L -O -J "${url}"
  fi
}

function download_plugins() {
  descriptor="${1}"

  apps=$(cat ${descriptor} | jq -r '.plugins | keys[]')
  for app in ${apps}; do
    file=$(cat ${descriptor} | jq -r --arg app "${app}" '.plugins[$app].file')
    if [ ${file} != null ]; then
      mkdir -p plugins/${app}
      cp plugin-descriptors/${file} plugins/${app}/
    fi

    url=$(cat ${descriptor} | jq -r --arg app "${app}" '.plugins[$app].url')
    if [ ${url} != null ]; then
      mkdir -p plugins/${app}
      (
        cd plugins/${app}
        if [[ "${url}" =~ https://github.com/.* ]]; then
          download_github_release_artifact ${url}
        else
          echo "Downloading ${url}"
          curl -s -L -O -J ${url}
        fi
      )
    fi
  done
}

function download_github_release_artifact() {
  url="${1}"

  if [ -z ${GITHUB_TOKEN:-} ]; then
    echo "Downloading ${url}"
    curl -s -L -O -J ${url}
  else
    gh_curl_header="Authorization: token ${GITHUB_TOKEN}"

    tag="$(echo ${url} | sed -E 's#.*/download/([^/]+)/.*#\1#g')"
    release_url="$(echo ${url} | sed -E "s#(.*)github.com/(.*)/releases/.*#\1api.github.com/repos/\2/releases/tags/${tag}#g")"
    artifact_name="$(echo ${url} | sed -E "s#.*/##g")"
    artifact_url="$(curl -H "${gh_curl_header}" -f -s -L "${release_url}" | jq -r --arg artifact_name "${artifact_name}" '.assets[] | select(.name == $artifact_name).url')"

    echo "Downloading ${artifact_url}"
    curl -H "${gh_curl_header}" -H "Accept:application/octet-stream" -f -s -L -o "${artifact_name}" "${artifact_url}"
  fi
}

function download_github_action_artifact() {
  url="${1}"

  if [ -z ${GITHUB_TOKEN:-} ]; then
    echo "GITHUB_TOKEN is required to download from GitHub Action Runs"
    exit 1
  else
    gh_curl_header="Authorization: token ${GITHUB_TOKEN}"
  fi

  artifacts_url="$(echo ${url} | sed 's#/github.com/#/api.github.com/repos/#g')/artifacts"
  artifacts_response="$(curl -H "${gh_curl_header}" -f -s -L "${artifacts_url}")"
  artifact_url="$(echo ${artifacts_response} | jq -r '.artifacts[0].archive_download_url')"
  artifact_name="$(echo ${artifacts_response} | jq -r '.artifacts[0].name')"

  echo "Downloading ${artifact_url}"
  curl -H "${gh_curl_header}" -s -L -o "${artifact_name}.zip" "${artifact_url}"
}

function extract_bundles() {
  bundle_zips=$(find -name '*.zip')
  if [ -n "${bundle_zips}" ]; then
    mkdir -p bundles
    for bundle_zip in ${bundle_zips}; do
      unzip -d bundles ${bundle_zip}
      rm ${bundle_zip}
    done
    mv bundles/* . || true
    rmdir bundles || true
  fi
}

function extract_plugins() {
  (
    cd plugins
    apps="$(ls)"
    for app in ${apps}; do
      (
        cd ${app}
        app_plugin="$(ls)"
        if [ -n "${app_plugin}" ]; then
          for PLUGIN in ${app_plugin}; do
            EXTRACT_DIR="${PLUGIN%.zip}"
            unzip -qq ${PLUGIN} -d ${EXTRACT_DIR}
            rm ${PLUGIN}
          done
        fi
      )
    done
  )
}

main "${@}"
