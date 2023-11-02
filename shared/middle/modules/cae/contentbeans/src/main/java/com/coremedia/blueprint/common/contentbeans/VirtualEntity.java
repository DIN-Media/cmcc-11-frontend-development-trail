package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.objectserver.dataviews.AssumesIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.util.Objects;

import static java.lang.String.format;

/**
 * Marker interface for entities that represent a virtual (non-content) entity.
 */
public interface VirtualEntity {

  /**
   * Replace the given bean with a bean that additionally implements {@link VirtualEntity} and
   * assumed the identity of the given bean.
   *
   * <p>Caution: The method annotations of the given bean are not copied to the new bean.
   * Especially the {@link Autowired} annotation is not copied.</p>
   */
  static <T extends AssumesIdentity> T ofBean(T bean) {
    var aClass = bean.getClass();
    var enhancer = new Enhancer();
    enhancer.setSuperclass(aClass);
    enhancer.setInterfaces(new Class[] {VirtualEntity.class});
    enhancer.setCallbackType(MethodInterceptor.class);
    enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
      var methodName = method.getName();
      if ("equals".equals(methodName)) {
        return obj instanceof VirtualEntity && (boolean)proxy.invokeSuper(obj, args);
      } else if ("hashCode".equals(methodName)) {
        return Objects.hash(bean, VirtualEntity.class);
      } else if ("toString".equals(methodName)) {
        return format("VirtualEntity[%s]", bean);
      } else {
        return proxy.invokeSuper(obj, args);
      }
    });
    try {
      T result = (T) enhancer.create();
      result.assumeIdentity(bean);
      return result;
    } catch (Exception e) {
      throw new IllegalStateException("Unable to enhance " + aClass, e);
    }
  }
}
