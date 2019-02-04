<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->
<#-- @ftlvariable name="index" type="java.lang.Integer" -->

<#assign index=cm.localParameters().index!0 />

<#assign additionalVariantCssClass="" />
<#if index % 2 == 0>
  <#assign additionalVariantCssClass=" cm-carousel--corporate-identity" />
</#if>

<#assign items=self.items![] />
<#if items?has_content>
  <div class="cm-container cm-container--carousel"<@preview.metadata data=[bp.getContainerMetadata(self),bp.getPlacementHighlightingMetaData(self)!""] />>
    <@cm.include self=self view="asCarousel" params={"modifier": "default", "controlIcon": "triangle", "additionalClass": additionalVariantCssClass, "viewItems": "asTeaser", "displayPagination": true, "index": index} />
  </div>
</#if>
