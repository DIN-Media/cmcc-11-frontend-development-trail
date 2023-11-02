package com.coremedia.blueprint.example.uapi.tests.expert;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cache.config.CacheConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * <p>
 * This test demonstrates how to test your cache-key implementations.
 * While the cache-key is rather artificial, the focus is here, how
 * to get the CoreMedia cache injected.
 * </p>
 * <p>
 * Although this test is not strictly related to Unified API, cache keys are
 * often bound to repository states. Thus, for a concrete test, you most likely
 * require the {@link com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration}
 * along with the cache configuration shown here.
 * </p>
 */
@SpringJUnitConfig(SomeCacheKeyTest.LocalConfig.class)
class SomeCacheKeyTest {
  private static final UuidDependency GLOBAL_UUID_DEPENDENCY = new UuidDependency("GLOBAL");

  @Test
  void shouldCacheGeneratedUuid(@Autowired @NonNull CacheKeyOwner cacheKeyOwner) {
    UUID uuid1 = cacheKeyOwner.getUuidSingleton();
    UUID uuid2 = cacheKeyOwner.getUuidSingleton();
    assertThat(uuid1)
            .isEqualTo(uuid2);
  }

  @Test
  void shouldReevaluateOnCacheEviction(@Autowired @NonNull CacheKeyOwner cacheKeyOwner,
                                       @Autowired @NonNull Cache cache) {
    UUID uuid1 = cacheKeyOwner.getUuidSingleton();
    cache.invalidate(GLOBAL_UUID_DEPENDENCY);
    UUID uuid2 = cacheKeyOwner.getUuidSingleton();
    assertThat(uuid1)
            .isNotEqualTo(uuid2);
  }

  /**
   * This configuration is rather sparse, as it does not use the
   * {@link com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration} as shown
   * in other tests. But for a plain, more or less artificial cache-key,
   * which is not bound to the repository, we do not need it here. In more
   * production-oriented use-case tests, you most likely would like to
   * represent some content state within your cache-key.
   */
  @Configuration(proxyBeanMethods = false)
  @Import(CacheConfiguration.class)
  static class LocalConfig {
    @Bean
    @Scope(SCOPE_SINGLETON)
    CacheKeyOwner cacheKeyOwner(@NonNull Cache cache) {
      return new CacheKeyOwner(cache);
    }
  }

  /**
   * More or less typical production setup, where there is some business
   * logic, that internally manages its state via cached states.
   */
  static final class CacheKeyOwner {
    @NonNull
    private final Cache cache;

    public CacheKeyOwner(@NonNull Cache cache) {
      this.cache = requireNonNull(cache);
    }

    public UUID getUuidSingleton() {
      return cache.get(new SomeCacheKeyUnderTest());
    }
  }

  /**
   * Some artificial dependency, which when invalidated, will trigger the
   * invalidation of the cache key itself.
   */
  static class UuidDependency {
    private final String name;

    public UuidDependency(@NonNull String name) {
      this.name = requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      UuidDependency that = (UuidDependency) o;
      return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }

    /**
     * It is recommended that cache keys as well as their dependencies
     * implement a custom {@code toString()} representation. This helps
     * when browsing the cache.
     */
    @Override
    public String toString() {
      return format("dependency.uuid(%s)", name);
    }
  }

  /**
   * <p>
   * Artificial cache-key, that just provides random UUIDs. If used within a
   * cache, the cache will ensure that the UUID is generated when its global
   * dependency gets invalidated. Think of the UUID generation being some
   * expensive process, so that caching makes sense, actually.
   * </p>
   * <p>
   * Also not, that {@link SomeCacheKeyUnderTest#equals(Object)}
   * and {@link SomeCacheKeyUnderTest#hashCode()} are much simpler
   * here, than you would normally have it in production code.
   * </p>
   */
  static final class SomeCacheKeyUnderTest extends CacheKey<UUID> {
    @Override
    @NonNull
    public UUID evaluate(@NonNull Cache cache) {
      Cache.dependencyOn(GLOBAL_UUID_DEPENDENCY);
      return UUID.randomUUID();
    }

    /**
     * Example using the main memory caching of the Unified API. To do so,
     * {@code com.coremedia.cap.heap} must be returned.
     */
    @Override
    public String cacheClass(Cache cache, UUID value) {
      return "com.coremedia.cap.heap";
    }

    /**
     * Typical weight calculation, that takes the number of dependents into
     * account.
     */
    @Override
    public int weight(Object key, UUID value, int numDependents) {
      return 44 + 4 * numDependents;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
      return Objects.hash();
    }

    /**
     * Custom {@code toString()} helps when browsing the cache. Typically,
     * the representation also contains some field references.
     */
    @Override
    public String toString() {
      return "key.uuid";
    }
  }
}
