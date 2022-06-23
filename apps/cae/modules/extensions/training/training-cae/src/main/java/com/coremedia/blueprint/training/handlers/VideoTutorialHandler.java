package com.coremedia.blueprint.training.handlers;

import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.training.contentbeans.CMVideoTutorialImpl;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.base.Strings;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Link
@RequestMapping
public class VideoTutorialHandler extends PageHandlerBase {

  public static final String URL_PATTERN = "/videotutorial/{id:\\d+}/{title}.{extension}";


  @RequestMapping( URL_PATTERN )
  public ModelAndView handleVideoTutorialLink(@PathVariable("id") CMVideoTutorialImpl target,
                                              @PathVariable("extension") String extension,
                                              HttpServletRequest request)
  {
    if (target==null) {
      return HandlerHelper.notFound();
    }

    Navigation context = getNavigation(target);
    User developer = UserVariantHelper.getUser(request);
    Page page = asPage(context, target, developer);

    String view = getViewForExtension(extension);

    ModelAndView modelAndView = null;
    if (view==null) {
      modelAndView = HandlerHelper.createModel(page);
    }
    else {
      modelAndView = HandlerHelper.createModelWithView(target, view);
    }

    addPageModel(modelAndView, page);

    return modelAndView;
  }

  @Link( uri=URL_PATTERN, type = CMVideoTutorialImpl.class )
  public Map<String,Object> createLink(CMVideoTutorialImpl target, String view) {
    Map<String, Object> placeholders = new HashMap<>();
	
	  String title = target.getTitle();
	  if (Strings.isNullOrEmpty(title)) {
	    title = "document";
    }
	  int id = target.getContentId();
	  String extension = getExtensionForView(view);
	
    placeholders.put("id", id);
    placeholders.put("title", title);
    placeholders.put("extension", extension);

    return placeholders;
  }

  private String getExtensionForView(String view) {
    return (view==null) ? "html" : view;
  }

  private String getViewForExtension(String extension) {
    return (extension==null || extension.equals("html")) ? null : extension;
  }


}
