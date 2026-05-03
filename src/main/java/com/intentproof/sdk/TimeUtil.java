package com.intentproof.sdk;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * UTC timestamps with millisecond precision and {@code Z} suffix (parity with Node/Python SDKs).
 */
final class TimeUtil {
  private static final DateTimeFormatter UTC_ISO_MS =
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
          .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
          .appendLiteral('Z')
          .toFormatter()
          .withZone(ZoneOffset.UTC);

  private TimeUtil() {}

  static String utcIsoMs(Instant instant) {
    Instant truncated =
        Instant.ofEpochMilli(java.util.Objects.requireNonNull(instant, "instant").toEpochMilli());
    return UTC_ISO_MS.format(truncated);
  }

  static Instant now() {
    return Instant.ofEpochMilli(Instant.now().toEpochMilli());
  }
}
