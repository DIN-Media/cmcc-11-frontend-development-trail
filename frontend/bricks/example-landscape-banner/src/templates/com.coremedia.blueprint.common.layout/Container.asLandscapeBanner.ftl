<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#--
  Template Description:

  This template renders all items as flattened list with the view "asLandscapeBanner".

  @since 1907
-->

<#assign items=self.flattenedItems![] />
<#assign additionalClass=cm.localParameters().additionalClass!"" />

<div class="cm-landscape-banner-container ${additionalClass}" <@preview.metadata data=[bp.getContainerMetadata(self),bp.getPlacementHighlightingMetaData(self)!""] />>
  <#if self.teaserTitle?has_content>
    <h2 class="cm-landscape-banner-container__headline" <@preview.metadata data=[self.content,"properties.teaserTitle"]/>>${self.teaserTitle}</h2>
  </#if>
  <#if (items?size > 0)>
    <div class="cm-landscape-banner-container__items cm-landscape-banner-grid"<#if self.content?has_content><@preview.metadata data=[self.content,"properties.items"]/></#if>>
      <#list items as item>
        <div class="cm-landscape-banner-grid__item">
          <@cm.include self=item view="asLandscapeBanner" />
        </div>
      </#list>
    </div>
  </#if>
</div>