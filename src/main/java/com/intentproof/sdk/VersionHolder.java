package com.intentproof.sdk;

/** Reads {@link Package} manifest metadata when the SDK is loaded from a versioned artifact. */
final class VersionHolder {
  private VersionHolder() {}

  static String implementationVersionOrDev() {
    return implementationVersionOrDev(IntentProof.class.getPackage());
  }

  static String implementationVersionOrDev(Package pkg) {
    if (pkg != null && pkg.getImplementationVersion() != null) {
      return pkg.getImplementationVersion();
    }
    return "0.0.0-dev";
  }
}
