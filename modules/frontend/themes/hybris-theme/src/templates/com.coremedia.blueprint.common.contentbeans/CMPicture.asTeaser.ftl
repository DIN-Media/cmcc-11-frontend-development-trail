<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPicture" -->

<#assign isLast=cm.localParameter("islast", false)/>
<@cm.include self=self view="teaser" params={
  "isLast": isLast,
  "renderTeaserText": false,
  "renderDimmer": false,
  "renderLink": false
} + cm.localParameters() />
