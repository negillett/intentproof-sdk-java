package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class VersionHolderTest {

  @Test
  void readsImplementationVersionFromPackage() {
    Package pkg = mock(Package.class);
    when(pkg.getImplementationVersion()).thenReturn("9.8.7-test");
    assertEquals("9.8.7-test", VersionHolder.implementationVersionOrDev(pkg));
  }

  @Test
  void fallsBackWhenPackageOrVersionMissing() {
    Package pkg = mock(Package.class);
    when(pkg.getImplementationVersion()).thenReturn(null);
    assertEquals("0.0.0-dev", VersionHolder.implementationVersionOrDev(pkg));
    assertEquals("0.0.0-dev", VersionHolder.implementationVersionOrDev(null));
  }
}
