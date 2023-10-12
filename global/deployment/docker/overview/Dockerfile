ARG NGINX_BASE_IMAGE=coremedia/nginx-base:1.2-cm-1.25-alpine
FROM --platform=$BUILDPLATFORM ${NGINX_BASE_IMAGE}
ARG BUILDARCH
ENV CMS_VERSION=unknown \
    COMMERCE_MOCK_ENABLED=false \
    COMMERCE_COMMERCETOOLS_ENABLED=false \
    COMMERCE_COMMERCETOOLS_HOST="" \
    COMMERCE_SFCC_ENABLED=false \
    COMMERCE_SFCC_HOST="" \
    COMMERCE_HYBRIS_ENABLED=false \
    COMMERCE_HYBRIS_HOST="" \
    COMMERCE_WCS_ENABLED=false \
    COMMERCE_WCS_HOST="" \
    COMMERCE_WCS_VERSION="8.0" \
    USER_CHANGES_ENABLED=true \
    HEADLESS_CLIENT_ENABLED=false \
    CAMPAIGN_ENABLED=false \
    CAMPAIGN_PREVIEW_AUTHORIZATION_ID="" \
    CAMPAIGN_DELIVERY_ENDPOINT="" \
    CAMPAIGN_DELIVERY_AUTHORIZATION_ID="" \
    STUDIO_CAMPAIGN_APP_TENANTID="" \
    STUDIO_CAMPAIGN_APP_GRAPHQLENDPOINT=""
COPY entrypoint.sh /entrypoint.sh
RUN apk add --update \
      curl \
      bash && \
    rm -rf /var/cache/apk/* && \
    chmod 775 /entrypoint.sh && \
    mkdir -p /etc/confd/templates \
             /etc/confd/conf.d
RUN if [ "${BUILDARCH}" = "arm64" ]; \
          then CONFD_256="05d5dcb1eb473ab4625cfc0a7bea0d7b810453a96151d3f770ea91be99e93ebc";\
          else CONFD_256="974819bc92de0620b459bb856a36537caed6e5c4a1ffe47e0d549712b12024de"; \
    fi && \
    curl -sL "https://github.com/abtreece/confd/releases/download/v0.19.2/confd-v0.19.2-linux-${BUILDARCH}.tar.gz" -o /tmp/conf.d.tar.gz && \
    echo "${CONFD_256}  /tmp/conf.d.tar.gz" | sha256sum -c - && \
    tar -xz -C /usr/bin/ -f /tmp/conf.d.tar.gz && rm /tmp/conf.d.tar.gz

COPY confd /etc/confd
COPY index.html /usr/share/nginx/html/
COPY assets /usr/share/nginx/html/assets
COPY conf.d /etc/nginx/conf.d

ENTRYPOINT ["/entrypoint.sh"]
