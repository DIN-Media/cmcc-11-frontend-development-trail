<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="js" type="com.coremedia.blueprint.common.contentbeans.CMJavaScript" -->
<#-- @ftlvariable name="previewFacade" type="com.coremedia.objectserver.view.freemarker.PreviewFacade" -->
<#include "../includes/slider.ftl" />

<#assign context=self.context>
<#assign previewMetadata = lc.previewMetaData()>
<#assign sliderMetadata = previewFacade.metadata(sliderMetadata)>
<!--CM { "objectType":"page","renderType":"metadata","title":"${context.title}","description":"",
"keywords":"${context.keywords}","pageName":"${context.title}","pbe":"${previewMetadata?html}", "slider":"${sliderMetadata?html}" } CM-->

<#-- include all css (incl. with ieExpressions) -->
<#list self.css![] as css>
  <@cm.include self=css view="asCSSLink" />
</#list>

<#-- include all javascript without ieExpressions -->
<#list self.javaScript![] as js>
  <#if !js.ieExpression?has_content>
    <@cm.include self=js view="asJSLink" />
  </#if>
</#list>

<#-- include javascript with ieExpressions -->
<#list self.javaScript![] as js>
  <#if js.ieExpression?has_content>
    <@cm.include self=js view="asHeaderJSLink" />
  </#if>
</#list>

<@preview.previewScripts />

<#-- make the crawler index the coremedia content id-->
<#if self.content.contentId?has_content>
<meta name="coremedia_content_id" content="${self.content.contentId}"/>
</#if>
