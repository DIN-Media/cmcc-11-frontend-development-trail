<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->
<#assign uniqueId=cm.localParameters().parentLevelId+"_"+bp.generateId("dm")/>
<#assign idPrefix=cm.localParameters().idPrefix!""/>
<#assign link=cm.getLink(self!cm.UNDEFINED)/>

<a id="${idPrefix}categoryLink_${uniqueId}" href="${link}" aria-label="${self.teaserTitle}" class="menuLink" role="menuitem" tabindex="-1"<@cm.metadata self.content/>>${self.teaserTitle}</a>
<ul class="subcategoryList">
  <li>
    <div class="cm-dropdown-image">
      <@bp.optionalLink href=cm.getLink(self!cm.UNDEFINED)>
         <@cm.include self=self view="asPicture" params={"additionalClass": "cm-teaser--megamenu"}/>
      </@bp.optionalLink>
    </div>
  </li>
</ul>
