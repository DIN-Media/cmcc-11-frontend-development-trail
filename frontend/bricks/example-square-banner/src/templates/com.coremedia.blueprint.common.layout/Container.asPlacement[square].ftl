<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#--
  Template Description:

  This template redirects to the view "asSquareBanner".

  @since 1907
-->

<#assign additionalClass=cm.localParameters().additionalClass!"" />

<@cm.include self=self view="asSquareBanner" params={"additionalClass": additionalClass} />