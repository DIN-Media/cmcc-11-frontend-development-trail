package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.objectserver.dataviews.AssumesIdentity;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualEntityTest {

  @Test
  void testEqualsHasCode() {
    var assumesIdentity = new MyAssumesIdentity();
    assumesIdentity.setString("value");
    assertThat(VirtualEntity.ofBean(assumesIdentity))
            .returns("value", MyAssumesIdentity::getString)
            // it's not equal to the original bean, but equal to all virtual entities of the original bean
            .isNotEqualTo(assumesIdentity)
            .hasSameHashCodeAs(VirtualEntity.ofBean(assumesIdentity))
            .isEqualTo(VirtualEntity.ofBean(assumesIdentity));
  }

  @Test
  void testToString() {
    var assumesIdentity = new MyAssumesIdentity();
    assumesIdentity.setString("value");
    assertThat(VirtualEntity.ofBean(assumesIdentity))
            .hasToString("VirtualEntity[MyAssumesIdentity{string='value'}]");
  }

  static class MyAssumesIdentity implements AssumesIdentity {

    private String string;

    public String getString() {
      return string;
    }

    public void setString(String string) {
      this.string = string;
    }

    @Override
    public void assumeIdentity(Object bean) {
      string = ((MyAssumesIdentity)bean).string;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MyAssumesIdentity that = (MyAssumesIdentity) o;
      return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
      return Objects.hash(string);
    }

    @Override
    public String toString() {
      return "MyAssumesIdentity{" +
              "string='" + string + '\'' +
              '}';
    }
  }
}
