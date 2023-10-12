package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.base.taxonomies.TaxonomyLocalizationStrategy;
import com.coremedia.blueprint.taxonomies.TaxonomyNode;
import com.coremedia.blueprint.taxonomies.TaxonomyNodeList;
import com.coremedia.blueprint.taxonomies.TaxonomyUtil;
import com.coremedia.blueprint.taxonomies.cycleprevention.TaxonomyCycleValidator;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.common.descriptors.LinkPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.content.authorization.AccessControl;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.rest.cap.content.search.SearchService;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * A strategy which represents the folder/document structure of the content repository
 * as a taxonomy...
 * <p/>
 * this class is maybe not very useful but it demonstrates how to implement taxonomy strategies.
 */
public class DefaultTaxonomy extends TaxonomyBase { // NOSONAR  cyclomatic complexity

  private static final String ROOT_TYPE = "root";

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTaxonomy.class);

  private static final int LIMIT = 10;
  public static final String VALUE = "value";
  private static final String CHILDREN = "children";

  private static final String NEW_KEYWORD = "new keyword";
  private static final String ROOT_SETTINGS_DOCUMENT = "_root";
  private static final String TYPE_SETTINGS = "CMSettings";
  private static final String SETTINGS_STRUCT = "settings";
  private static final String ROOTS_LIST = "roots";

  private final ContentRepository contentRepository;
  private final ContentType taxonomyContentType;
  private final TaxonomyCycleValidator taxonomyCycleValidator;
  private final TaxonomyLocalizationStrategy taxonomyLocalizationStrategy;
  private final TaxonomyNode root;
  private final Content rootFolder;

  private final SearchService searchService;
  private int maxDocumentsPerFolder = 0;

  public DefaultTaxonomy(Content rootFolder,
                         String siteId,
                         ContentType type,
                         ContentRepository contentRepository,
                         SearchService searchService,
                         TaxonomyCycleValidator taxonomyCycleValidator,
                         TaxonomyLocalizationStrategy taxonomyLocalizationStrategy,
                         int documentsPerFolder) {
    super(rootFolder.getName(), siteId);

    this.rootFolder = rootFolder;
    this.contentRepository = contentRepository;
    this.searchService = searchService;
    this.taxonomyContentType = type;
    this.taxonomyCycleValidator = taxonomyCycleValidator;
    this.taxonomyLocalizationStrategy = taxonomyLocalizationStrategy;

    // Constructor Calls Overridable Method
    root = createEmptyNode();
    root.setName(getTaxonomyId());
    root.setSiteId(siteId);
    root.setSelectable(false);
    root.setRoot(true);
    root.setRef(TaxonomyUtil.getRestIdFromCapId(rootFolder.getId()));
    root.setType(ROOT_TYPE);
    root.setLevel(0);

    setMaxDocumentsPerFolder(documentsPerFolder);
  }

  @Override
  public boolean isWriteable() {
    Content content = asContent(getRoot());
    AccessControl accessControl = contentRepository.getAccessControl();
    return accessControl.mayCreate(content, taxonomyContentType);
  }

  @Override
  public boolean isValid() {
    return rootFolder.isReadable() && rootFolder.isInProduction() && !rootFolder.getChildDocuments().isEmpty();
  }

  @Override
  @Nullable
  public TaxonomyNode getNodeByRef(@NonNull String ref) {
    if (root.getRef().equals(ref)) {
      return root;
    }

    Content content = getContent(ref);
    if (content == null) {
      return null;
    }
    return asNode(content);
  }

  @Override
  @NonNull
  public TaxonomyNode getRoot() {
    return root;
  }

  @Override
  @Nullable
  public TaxonomyNode getParent(@NonNull String ref) {
    Content nodeContent = getContent(ref);
    if (nodeContent == null) {
      return null;
    }

    if (taxonomyCycleValidator.isCyclic(nodeContent, taxonomyContentType)) {
      return null;
    }

    Content parent = getParent(nodeContent);
    if (parent == null) {
      return null;
    }
    return asNode(parent);
  }

  @Override
  @NonNull
  public TaxonomyNodeList getChildren(@NonNull TaxonomyNode node, int offset, int count) {
    List<Content> children = getRoot().equals(node) ? getTopLevelNodes() : getChildren(asContent(node));
    long skip = Math.max(offset, 0);
    long limit = count == -1 ? children.size() : count;

    List<TaxonomyNode> nodes = children.stream()
            .skip(skip)
            .limit(limit)
            .map(c -> asNode(c, true))
            .collect(Collectors.toList());

    return new TaxonomyNodeList(nodes);
  }

  @Override
  @NonNull
  public TaxonomyNode getPath(@NonNull TaxonomyNode node) {
    List<Content> path = getPath(asContent(node));
    TaxonomyNodeList list = asNodeList(path, -1, -1, true);
    node.setPath(list);
    return node;
  }

  @Override
  @NonNull
  public List<Content> getLinks(@NonNull TaxonomyNode node, boolean recursive) {
    List<Content> result = new ArrayList<>();
    Content content = asContent(node);

    //search recursively
    List<Content> allChildren = collectAllTaxonomiesBelow(content, recursive);

    for (Content child : allChildren) {
      for (Content ref : child.getReferrers()) {
        if (!result.contains(ref)
                && ref.isInProduction()
                && ref.getName().equals(ROOT_SETTINGS_DOCUMENT)
                && !ref.getType().isSubtypeOf(taxonomyContentType)
                && !ref.getType().isSubtypeOf("EditorPreferences")) {
          result.add(ref);
        }
      }
    }

    return result;
  }


  @Override
  @NonNull
  public List<Content> getStrongLinks(@NonNull TaxonomyNode node, boolean recursive) {
    Content content = asContent(node);
    List<Content> result = new ArrayList<>();

    List<Content> allChildren = collectAllTaxonomiesBelow(content, recursive);

    for (Content child : allChildren) {
      for (Content ref : child.getReferrers()) {
        if (ref.getName().equals(ROOT_SETTINGS_DOCUMENT)) {
          continue;
        }
        if (!result.contains(ref) && ref.isInProduction() && !ref.getType().isSubtypeOf(taxonomyContentType) && !isWeakLinked(child, ref)) {
          result.add(ref);
        }
      }
    }

    return result;
  }

  @Override
  @NonNull
  public TaxonomyNodeList find(@NonNull String text) {
    TaxonomyNodeList list = new TaxonomyNodeList();
    List<TaxonomyNode> hits = new ArrayList<>();
    if (StringUtils.isBlank(text)) {
      return list;
    }

    String query = TaxonomyUtil.formatQuery(text);//NOSONAR
    List<Content> matches = TaxonomyUtil.search(searchService, rootFolder, taxonomyContentType, query, LIMIT);
    for (Content match : matches) {
      if (match.isDeleted() || taxonomyCycleValidator.isCyclic(match, taxonomyContentType)) {
        continue;
      }
      if (shouldMatchBeDisplayed(match, text)) {
        TaxonomyNode hit = asNode(match);
        hit.setPath(getPath(hit).getPath());
        hits.add(hit);
      }
    }
    list.setNodes(hits);
    return list;
  }

  /**
   * This method filters the search results for valid hits
   *
   * @param match the possible match
   * @param text  the entered search term
   * @return true if the match should be displayed
   */
  protected boolean shouldMatchBeDisplayed(@NonNull Content match, @NonNull String text) {
    String nodeName = getNodeName(match);

    if (StringUtils.containsIgnoreCase(match.getName(), text) ||
            StringUtils.containsIgnoreCase(match.getString(VALUE), text) ||
            StringUtils.containsIgnoreCase(nodeName, text)) {
      return true;
    }

    if (this.taxonomyLocalizationStrategy != null && this.taxonomyLocalizationStrategy.isSearchAllLocalesEnabled()) {
      List<Locale> supportedLocales = this.taxonomyLocalizationStrategy.getSupportedLocales();
      for (Locale supportedLocale : supportedLocales) {
        String localizedValue = this.taxonomyLocalizationStrategy.getDisplayName(match, supportedLocale);
        if (StringUtils.equalsAnyIgnoreCase(localizedValue, text)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  @Nullable
  public TaxonomyNode moveNode(@NonNull TaxonomyNode node, @NonNull TaxonomyNode target) {  // NOSONAR  cyclomatic complexity
    //retrieve the contents we need for this operation
    Content nodeContent = asContent(node);
    Content parent = getParent(nodeContent);
    Content targetContent = asContent(target);

    if (parent != null && targetContent.getId().equals(parent.getId())) {
      LOG.warn("Can not move '{}' to '{}', it's already there.", node.getName(), targetContent.getName());
      return asNode(nodeContent);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Moving '{}' to '{}'", node.getName(), targetContent.getName());
    }

    //checkout the content objects first.
    if (!nodeContent.isCheckedOut()) {
      nodeContent.checkOut();
    }
    if (!targetContent.isFolder() && !targetContent.isCheckedOut()) {
      targetContent.checkOut();
    }
    if (parent != null && !parent.isCheckedOut()) {
      parent.checkOut();
    }

    //remove child relation in the parent
    if (parent != null) {
      List<Content> children = Collections.synchronizedList(new ArrayList<>());
      children.addAll(getChildren(parent));
      children.remove(nodeContent);
      parent.set(CHILDREN, children);
    } else {
      //no parent? Then remove it from the settings
      List<Content> rootNodes = getTopLevelNodes();
      rootNodes.remove(nodeContent);
      updateRootNodeSettings(rootNodes);
    }

    //now move the content to the new target by appending as child (we do not set the parent relation anymore!)
    if (targetContent.isDocument()) { // NOSONAR //target can be the root folder too!!!
      List<Content> targetChildren = Collections.synchronizedList(new ArrayList<>());
      targetChildren.addAll(getChildren(targetContent));
      targetChildren.add(nodeContent);
      targetContent.set(CHILDREN, targetChildren);
    } else {
      List<Content> rootNodes = getTopLevelNodes();
      if (!rootNodes.isEmpty()) {
        rootNodes.add(nodeContent);
        updateRootNodeSettings(rootNodes);
      }
    }


    //finally update the lifecycle status
    if (nodeContent.isCheckedOut()) {
      nodeContent.checkIn();
    }
    approveAndPublish(nodeContent);
    if (parent != null) {
      if (parent.isCheckedOut()) {
        parent.checkIn();
      }
      approveAndPublish(parent);
    }

    if (!targetContent.isFolder() && targetContent.isCheckedOut()) {
      targetContent.checkIn();
    }
    approveAndPublish(targetContent);

    //return updated ref
    return getNodeByRef(node.getRef());
  }

  @Override
  @Nullable
  public TaxonomyNode delete(@NonNull TaxonomyNode toDelete) {
    Content deleteMe = asContent(toDelete);
    Content parent = getParent(deleteMe);

    LOG.info("Deleting taxonomy node {}", toDelete);
    if (deleteMe.isCheckedOut()) {
      deleteMe.checkIn();
    }

    //collect all sub nodes we have to delete
    List<Content> allChildren = collectAllTaxonomiesBelow(deleteMe, true);
    deleteChildren(allChildren);

    unlinkFromParent(deleteMe, parent);
    return (parent == null) ? root : asNode(parent);
  }

  @NonNull
  private List<Content> collectAllTaxonomiesBelow(@NonNull Content node, boolean recursively) {
    if (!recursively) {
      return List.of(node);
    }

    List<Content> taxonomies = Collections.synchronizedList(new ArrayList<>());
    collectChildren(node, taxonomies);
    return taxonomies;
  }

  /**
   * Collects recursively all nodes of the given node
   */
  private void collectChildren(Content node, List<Content> allChildren) {
    allChildren.add(node);
    List<Content> children = getChildren(node);
    allChildren.addAll(children);
    for (Content child : children) {
      collectChildren(child, allChildren);
    }
  }

  /**
   * Deletes the given taxonomy content respecting the current lifecycle.
   *
   * @param children the taxonomy contents to delete
   */
  private void deleteChildren(List<Content> children) {
    PublicationService publicationService = contentRepository.getPublicationService();

    for (Content child : children) {
      if (publicationService.isPublished(child)) {
        publicationService.toBeWithdrawn(child);
        publicationService.approvePlace(child);
        publicationService.publish(child);
      }
      child.delete();
    }
  }

  /**
   * Removes the given node from the parent
   *
   * @param child  the child to delete
   * @param parent the parent to remove the child from
   */
  private void unlinkFromParent(Content child, Content parent) {
    if (parent != null) {
      if (!parent.isCheckedOut()) {
        parent.checkOut();
      }
      List<Content> children = Collections.synchronizedList(new ArrayList<>());
      children.addAll(getChildren(parent));
      children.remove(child);
      parent.set(CHILDREN, children);
      parent.checkIn();
      if (contentRepository.getPublicationService().isPublished(parent)) {
        approveAndPublish(parent);
      }
    } else {
      List<Content> rootNodes = getTopLevelNodes();
      if (!rootNodes.isEmpty()) {
        rootNodes.remove(child);
        updateRootNodeSettings(rootNodes);
      }
    }
  }

  @Override
  @NonNull
  public TaxonomyNode createChild(@NonNull final TaxonomyNode parentNode,
                                  @Nullable final String defaultName) {
    Content parent = (parentNode.isRoot()) ? null : asContent(parentNode);
    ContentType type = calculateTaxonomyType(parent);
    Content folder = getTargetForNewChild(parent);

    String defaultNameNonNull = Strings.isNullOrEmpty(defaultName) ? NEW_KEYWORD : defaultName;
    Content content = contentRepository.createContentBuilder()
            .type(type)
            .parent(folder)
            .name(NEW_KEYWORD)
            .nameTemplate()
            .property(VALUE, defaultNameNonNull)
            .checkedIn()
            .create();

    updateContentName(content, defaultNameNonNull);

    if (parent != null) {
      if (!parent.isCheckedOut()) {
        parent.checkOut();
      }
      List<Content> children = Collections.synchronizedList(new ArrayList<>());
      children.addAll(getChildren(parent));
      children.add(content);
      parent.set(CHILDREN, children);
      parent.checkIn();
    } else {
      List<Content> rootNodes = getTopLevelNodes();
      if (!rootNodes.isEmpty()) {
        rootNodes.add(content);
        updateRootNodeSettings(rootNodes);
      }
    }

    return asNode(content);
  }

  @Override
  @NonNull
  public TaxonomyNode commit(@NonNull TaxonomyNode node) {
    Content content = asContent(node);
    Content parent = getParent(content);
    try {
      if (!content.isDeleted()) {
        //test if renaming is required
        String contentName = content.getName();
        String newName = getTaxonomyDocumentName(content, node.getName());
        if (!contentName.equals(newName)) {
          //check out document and...
          if (!content.isCheckedOut()) {
            content.checkOut();
          }

          //...check if we have checked out it with our session
          if (content.isCheckedOutByCurrentSession()) {
            // rename content
            String name = content.getString(VALUE);
            if (!StringUtils.isEmpty(name)) {
              content.rename(StringUtils.trim(newName));
            }
          } else {
            LOG.info("Skipped renaming taxonomy node, because it's checkout out by another user.");
          }
        }

        publish(content);

        // publish parent if necessary...(publishing of parent must be done before publishing the child node, otherwise "An internal link of this document could not be published.")
        if (parent != null && parent.getCheckedInVersion() != null && !contentRepository.getPublicationService().isPublished(parent.getCheckedInVersion())) {
          LOG.info("Publishing parent {} of {}", parent, content);
          if (parent.isCheckedOut()) {
            parent.checkIn();
          }
          approveAndPublish(parent);
        }

        return asNode(content);
      }
    } catch (Exception e) { //NOSONAR
      LOG.warn("Error committing {}: {}", node, e.getMessage());
    }
    return asNode(content);
  }

  /**
   * Used for the name matching strategy to resolve all matching taxonomies for text
   * by simple name matching.
   *
   * @return all children
   */
  @Override
  @NonNull
  public List<TaxonomyNode> getAllChildren() {
    List<TaxonomyNode> allChildren = new ArrayList<>();
    List<Content> matches = new ArrayList<>();
    findAll(rootFolder, matches);
    for (Content child : matches) {
      allChildren.add(asNode(child, false));
    }
    return allChildren;
  }

  @Override
  public String getKeywordType() {
    return taxonomyContentType.getName();
  }

  // === HELPER ========================================================================================================

  /**
   * Calculates the target folder when new nodes are created.
   *
   * @param parent the optional parent node
   * @return the target folder for the child to create
   */
  protected Content getTargetForNewChild(Content parent) {
    Content folder = rootFolder;

    //check if the corresponding parent folder is used
    if (parent != null && parent.isDocument()) {
      folder = parent.getParent();
    }

    if (maxDocumentsPerFolder > 0) {
      QueryService queryService = folder.getRepository().getQueryService();
      Collection<Content> children = queryService.getContentsFulfilling(folder.getChildDocuments(), "TYPE " + taxonomyContentType.getName() + ": BELOW ?0 AND isInProduction ORDER BY id DESC", rootFolder);
      if (children.size() < maxDocumentsPerFolder) {
        return folder;
      }

      Content folderWithFreeCapacity = findFolderWithFreeCapacity();
      if (folderWithFreeCapacity == null) {
        folderWithFreeCapacity = contentRepository.createSubfolders(rootFolder, UUID.randomUUID().toString());
      }
      folder = folderWithFreeCapacity;
    }

    return folder;
  }

  private Content findFolderWithFreeCapacity() {
    Collection<Content> subFolders = rootFolder.getChildrenFulfilling("TYPE Folder_");
    for (Content subFolder : subFolders) {
      int size = subFolder.getChildrenFulfilling("TYPE " + taxonomyContentType.getName()).size();
      if (size < maxDocumentsPerFolder) {
        return subFolder;
      }
    }
    return null;
  }

  private List<Content> getPath(Content content) {
    List<Content> path = new ArrayList<>();
    Content parent = content;
    while (parent != null) {
      path.add(0, parent);
      parent = getParent(parent);
    }
    return path;
  }

  private List<Content> getTopLevelNodes() {
    List<Content> nodesFromSettings = getTopLevelNodesFromSettings();
    if (!nodesFromSettings.isEmpty()) {
      return nodesFromSettings;
    }

    List<Content> allNodes = new ArrayList<>();
    findRootNodes(rootFolder, allNodes);
    return allNodes;
  }

  private List<Content> getTopLevelNodesFromSettings() {
    Content rootSettings = rootFolder.getChild(ROOT_SETTINGS_DOCUMENT);
    if (rootSettings != null && rootSettings.getType().getName().equals(TYPE_SETTINGS)) {
      Struct settings = rootSettings.getStruct(SETTINGS_STRUCT);
      return new ArrayList<>(settings.getLinks(ROOTS_LIST));
    }

    return Collections.emptyList();
  }

  private void updateRootNodeSettings(@NonNull List<Content> topNodes) {
    Content rootSettings = rootFolder.getChild(ROOT_SETTINGS_DOCUMENT);
    if (rootSettings != null && rootSettings.getType().getName().equals(TYPE_SETTINGS)) {
      if (!rootSettings.isCheckedOut()) {
        rootSettings.checkOut();
      }

      Struct settings = rootSettings.getStruct(SETTINGS_STRUCT);
      settings = settings.builder().set(ROOTS_LIST, topNodes).build();
      rootSettings.set(SETTINGS_STRUCT, settings);
      rootSettings.checkIn();
    }
  }

  /**
   * Returns true if the linked content is linked has weak link inside the linking content
   */
  private boolean isWeakLinked(@NonNull Content linkedContent,
                               @NonNull Content linkingContent) {
    //check all link descriptors....
    List<CapPropertyDescriptor> descriptors = linkingContent.getType().getDescriptors();
    for (CapPropertyDescriptor descriptor : descriptors) {
      //..if they link the content and if they are not weak link
      if (descriptor.getType().equals(CapPropertyDescriptorType.LINK)) {
        LinkPropertyDescriptor linkPropertyDescriptor = (LinkPropertyDescriptor) descriptor;
        List<Content> links = linkingContent.getLinks(descriptor.getName());
        if (links.contains(linkedContent) && !linkPropertyDescriptor.isWeakLink()) {
          return false;
        }
      } else if (descriptor.getType().equals(CapPropertyDescriptorType.STRUCT)) {
        Struct struct = linkingContent.getStruct(descriptor.getName());
        if (struct != null && containsLink(struct, linkedContent)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Checks if the content is linked inside the struct
   *
   * @param struct        the struct property to check
   * @param linkedContent the content to search for
   */
  private boolean containsLink(@NonNull Struct struct, Content linkedContent) {
    Map<String, Object> properties = struct.toNestedMaps();
    List<Content> result = new ArrayList<>();
    collectStructReferences(properties, linkedContent, result);
    return !result.isEmpty();
  }

  /**
   * Helper for struct search
   */
  private void collectStructReferences(@NonNull Map<String, Object> properties,
                                       @NonNull Content linkedContent,
                                       @NonNull List<Content> result) {
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Map) {
        Map<String, Object> nestedMap = (Map<String, Object>) value;
        collectStructReferences(nestedMap, linkedContent, result);
      } else if (value instanceof List) {
        List<Object> list = (List<Object>) value;
        for (Object listItem : list) {
          if (listItem instanceof Map) {
            collectStructReferences((Map<String, Object>) listItem, linkedContent, result);
          } else if (listItem instanceof Content) {
            if (listItem.equals(linkedContent)) {
              result.add((Content) listItem);
            }
          }
        }
      }
    }
  }


  private void updateContentName(@NonNull Content content, @NonNull String defaultName) {
    int index = 1;
    String updatedName = defaultName;
    Content parentContent = content.getParent();
    while (parentContent != null && parentContent.getChild(updatedName) != null) {
      updatedName = defaultName + " (" + index + ")";
      index++;
    }

    try {
      if (!content.getName().equals(defaultName)) {
        content.rename(updatedName);
      }
    } catch (Exception e) {
      LOG.warn("Failed to rename new taxonomy node, keeping default name: {}", e.getMessage());
    }
  }

  /**
   * Content should be publish after each change.
   *
   * @param content the content to publish
   */
  private void publish(@NonNull Content content) {
    try {
      if (content.isCheckedOutByCurrentSession()) {
        content.checkIn();
        approveAndPublish(content);
      }
    } catch (Exception e) {
      LOG.error("Error publishing {}: {}", content, e.getMessage());
    }
  }

  /**
   * Returns the name that is used after a taxonomy has been renamed.
   * The new value of the "value" field will be used as document name too.
   *
   * @param content  The content to rename.
   * @param nodeName The name of the node we rename for
   * @return The new document name or the original one if the nodeName is not set.
   */
  @NonNull
  protected String getTaxonomyDocumentName(@NonNull Content content, @Nullable String nodeName) {
    if (!StringUtils.isEmpty(nodeName)) {
      String formattedName = nodeName.replace('/', '_');
      int renamingIndex = 0;
      //run duplicate check
      // Checkmarx complains about "Unchecked input for loop condition" here.
      // That is a false positive.
      // The Content is checked and the loop itself is correct
      while (content.getParent() != null && content.getParent().getChildDocumentsByName().containsKey(formattedName)) {
        Content child = content.getParent().getChild(formattedName);
        //increase counter for all children that have the same name, exclude active content
        if (!child.getId().equals(content.getId())) {
          renamingIndex++;
          formattedName = formattedName + "(" + renamingIndex + ")";
        } else {
          break;
        }
      }
      return formattedName;
    }
    return content.getName();
  }

  /**
   * Returns the content for the given reference.
   *
   * @param ref given reference
   * @return content
   */
  @Nullable
  private Content getContent(@NonNull String ref) {
    return contentRepository.getContent(TaxonomyUtil.asContentId(ref));
  }

  /**
   * Approves and publishes the given content, used
   * when a taxonomy content has been changed.
   *
   * @param content The content to approve and publish.
   */
  private void approveAndPublish(@NonNull Content content) {
    try {
      if (content.isFolder()) {
        return;
      }

      LOG.info("Publishing taxonomy node {}", content);
      PublicationService publisher = contentRepository.getPublicationService();
      Version checkedInVersion = content.getCheckedInVersion();
      publisher.approve(checkedInVersion);
      publisher.approvePlace(content);
      //publish the folder containing the content
      Content parentFolder = content.getParent();
      if (!publisher.isPublished(parentFolder)) {
        publisher.approvePlace(parentFolder);
        publisher.publish(parentFolder);
      }
      publisher.publish(content);
    } catch (Exception e) {
      LOG.error("Publication of taxonomy node '{}' failed: {}", content, e.getMessage());
    }
  }

  /**
   * Returns the first referrer of the given content to determine the path
   * of a taxonomy node.
   *
   * @param content The taxonomy content to search the referrer for.
   * @return parent content
   */
  @Nullable
  protected Content getParent(@NonNull Content content) {
    return content.getReferrerWithDescriptorFulfilling(taxonomyContentType.getName(), CHILDREN, "isInProduction");
  }


  /**
   * Converts the given list of nodes to a taxonomy node list representation.
   *
   * @param contents The contents to create the list for.
   * @param offset   The offset value if used or -1.
   * @param count    The count of the items if used or -1.
   * @param addRoot  If true, the root is added to the node list, used when a path is build as list.
   * @return The taxonomy node list representation.
   */
  @NonNull
  protected TaxonomyNodeList asNodeList(@NonNull List<Content> contents, int offset, int count, boolean addRoot) {
    List<TaxonomyNode> nodes = new ArrayList<>();
    //used for path info
    if (addRoot) {
      nodes.add(getRoot());
    }

    int totalSize = contents.size();
    List<Content> contentList = new ArrayList<>(contents);
    if (offset > -1 && count > -1) {
      int lastIndex = offset + count;
      if (lastIndex > totalSize) {
        lastIndex = totalSize;
      }
      contentList = contents.subList(offset, lastIndex);
    }

    for (Content c : contentList) {
      TaxonomyNode n = asNode(c);
      nodes.add(n);
    }
    return new TaxonomyNodeList(nodes);
  }

  /**
   * Converts a content object to a taxonomy node instance.
   *
   * @param content The content object to convert.
   * @return The taxonomy node representation.
   */
  @NonNull
  protected TaxonomyNode asNode(@NonNull Content content) {
    return asNode(content, true);
  }

  /**
   * Converts a content object to a taxonomy node instance.
   * For performance issues, the path information calculation can be skipped.
   *
   * @param content       The content object to convert.
   * @param buildPathInfo set to true to skip the path, level and leaf calculation for the resulting node
   * @return The taxonomy node representation.
   */
  @NonNull
  protected TaxonomyNode asNode(@NonNull Content content, boolean buildPathInfo) {
    TaxonomyNode node = createEmptyNode();
    node.setName(getNodeName(content));
    node.setRef(TaxonomyUtil.asNodeRef(content.getId()));
    node.setExtendable(true);
    node.setSiteId(getSiteId());
    node.setType(taxonomyContentType.getName());

    if (buildPathInfo) {
      node.setLeaf(!hasChildren(content));
      node.setLevel(getPath(content).size());
    }

    return node;
  }

  @NonNull
  protected String getNodeName(@NonNull Content content) {
    if (this.taxonomyLocalizationStrategy != null) {
      return taxonomyLocalizationStrategy.getDisplayName(content, null);
    }

    String name = content.getString(VALUE);
    if (Strings.isNullOrEmpty(name)) {
      name = content.getName();
    }
    return name;
  }

  @NonNull
  protected Content asContent(@NonNull TaxonomyNode node) {
    Content content = contentRepository.getContent(TaxonomyUtil.asContentId(node.getRef()));
    if (content == null) {
      throw new IllegalStateException(String.format("Given TaxonomyNode with id %s can not be converted to a Content", node.getTaxonomyId()));
    }
    return content;
  }


  private void setMaxDocumentsPerFolder(int max) {
    this.maxDocumentsPerFolder = max;

    if (maxDocumentsPerFolder > 0) {
      int size = rootFolder.getChildrenFulfilling("TYPE " + taxonomyContentType.getName()).size();
      if (size > maxDocumentsPerFolder) {
        this.maxDocumentsPerFolder = ((size + 99) / 100) * 100;
        LOG.warn("Cannot apply a value maxDocumentsPerFolder=" + max
                + " on " + this.getClass().getSimpleName() + " for taxonomy folder "
                + rootFolder.getPath() + ", since there are already more documents in this folder. The value was auto-corrected to " + maxDocumentsPerFolder);
      }
    }
  }


  /**
   * Calculates the type of this taxonomy.
   * Since there must be at least 1x node to create this class, we know that there is always
   * another content to derive the type from
   *
   * @param parent the parent node
   * @return the concrete type of this taxonomy
   */
  private ContentType calculateTaxonomyType(Content parent) {
    if (parent == null) {
      Set<Content> children = rootFolder.getChildren();
      for (Content child : children) {
        if (child.getType().isSubtypeOf(taxonomyContentType)) {
          return child.getType();
        }
      }

      return taxonomyContentType;
    }

    return parent.getType();
  }

  /**
   * Recursively collects the nodes from the taxonomy that have no parent
   *
   * @param folder  The folder to lookup keywords in.
   * @param matches The list of to fill up with matches.
   */
  protected void findRootNodes(@NonNull Content folder, @NonNull List<Content> matches) {
    Collection<Content> nodes = folder.getChildrenFulfilling("TYPE " + taxonomyContentType.getName());
    for (Content taxonomyNode : nodes) {
      if (taxonomyNode.getReferrerWithDescriptorFulfilling(taxonomyContentType.getName(), CHILDREN, "isInProduction") == null
              && !folder.getRepository().getPublicationService().isToBeDeleted(taxonomyNode)) { //NOSONAR
        matches.add(taxonomyNode);
      }
    }

    Collection<Content> subFolders = folder.getSubfolders();
    for (Content subFolder : subFolders) {
      findRootNodes(subFolder, matches);
    }
  }

  /**
   * Collects all nodes
   *
   * @param folder  The folder to lookup keywords in.
   * @param matches the list that contains all nodes
   */
  protected void findAll(@NonNull Content folder, List<Content> matches) {
    Collection<Content> nodes = folder.getChildrenFulfilling("TYPE " + taxonomyContentType.getName());
    for (Content taxonomyNode : nodes) {
      if (!folder.getRepository().getPublicationService().isToBeDeleted(taxonomyNode)) { //NOSONAR
        matches.add(taxonomyNode);
      }
    }

    Collection<Content> subFolders = folder.getSubfolders();
    for (Content subFolder : subFolders) {
      findAll(subFolder, matches);
    }
  }

  /**
   * Cached reading of children.
   * The getContentsFulfilling takes care that the result is cached on the client.
   */
  protected List<Content> getChildren(Content taxonomy) {
    QueryService queryService = taxonomy.getRepository().getQueryService();
    Collection<Content> children = queryService.getContentsFulfilling(taxonomy.getLinks("children"), "TYPE " + taxonomyContentType.getName() + ": BELOW ?0 AND isInProduction ORDER BY id DESC", rootFolder);
    return new ArrayList<>(children);
  }

  /**
   * Cached reading of children.
   * The getContentFulfilling takes care that the result is cached on the client.
   * We only want to know if there are children, 
   * so <code>getContentFulfilling</code> instead of <code>getContentsFulfilling</code> is used.
   */
  protected boolean hasChildren(Content taxonomy) {
    QueryService queryService = taxonomy.getRepository().getQueryService();
    Content child = queryService.getContentFulfilling(taxonomy.getLinks("children"), "TYPE " + taxonomyContentType.getName() + ": BELOW ?0 AND isInProduction ORDER BY id DESC", rootFolder);
    return child != null;
  }
}
