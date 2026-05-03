package com.intentproof.sdk;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** JSON-safe serializer for {@link ExecutionEvent} inputs and outputs. */
public final class Snapshot {
  private static final int DEFAULT_MAX_DEPTH = 6;
  private static final int DEFAULT_MAX_KEYS = 50;
  private static final String ARRAY_CAP = "[Array]";
  private static final String OBJECT_CAP = "[Object]";
  private static final String CIRCULAR = "[Circular]";
  private static final String SNAPSHOT_ERR = "[SnapshotError]";
  private static final String UNSERIAL = "[Unserializable]";

  private Snapshot() {}

  /**
   * Produces a JSON-friendly structure with depth, key, string, and redaction limits.
   *
   * @param value JVM value to convert
   * @return JSON-friendly structure, or a sentinel string on hard failure
   */
  public static Object snapshot(Object value) {
    return snapshot(value, SerializeOptions.builder().build());
  }

  /**
   * Same as {@link #snapshot(Object)} with explicit {@link SerializeOptions}.
   *
   * @param value JVM value to convert
   * @param options depth, key, string, and redaction limits
   * @return JSON-friendly structure, or a sentinel string on hard failure
   */
  public static Object snapshot(Object value, SerializeOptions options) {
    int maxDepth = snapshotLimit(options.maxDepth(), DEFAULT_MAX_DEPTH);
    int maxKeys = snapshotLimit(options.maxKeys(), DEFAULT_MAX_KEYS);
    Integer maxStringLength = snapshotStringLimit(options.maxStringLength());
    Set<String> redact = normalizeRedactSet(options.redactKeys());
    Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    try {
      return walk(value, 0, maxDepth, maxKeys, maxStringLength, redact, seen);
    } catch (Exception | StackOverflowError e) {
      return SNAPSHOT_ERR;
    }
  }

  private static int snapshotLimit(Integer n, int fallback) {
    if (n == null) {
      return fallback;
    }
    int i = n.intValue();
    return i < 0 ? fallback : i;
  }

  private static Integer snapshotStringLimit(Integer n) {
    if (n == null) {
      return null;
    }
    int i = n.intValue();
    return i < 0 ? null : i;
  }

  private static Set<String> normalizeRedactSet(List<String> redactKeys) {
    if (redactKeys == null || redactKeys.isEmpty()) {
      return null;
    }
    Set<String> set = new HashSet<>();
    for (String k : redactKeys) {
      if (k != null && !k.isEmpty()) {
        set.add(k.toLowerCase(Locale.ROOT));
      }
    }
    return set.isEmpty() ? null : set;
  }

  private static boolean shouldRedactKey(String key, Set<String> redact) {
    return redact != null && redact.contains(key.toLowerCase(Locale.ROOT));
  }

  private static String truncateString(String s, Integer maxLen) {
    if (maxLen == null || s.length() <= maxLen) {
      return s;
    }
    int over = s.length() - maxLen;
    return s.substring(0, maxLen) + "…[truncated " + over + " chars]";
  }

  private static Object walk(
      Object v,
      int depth,
      int maxDepth,
      int maxKeys,
      Integer maxStringLength,
      Set<String> redact,
      Set<Object> seen) {
    if (v == null) {
      return null;
    }
    if (v instanceof String s) {
      return truncateString(s, maxStringLength);
    }
    if (v instanceof CharSequence cs) {
      return truncateString(cs.toString(), maxStringLength);
    }
    if (v instanceof Boolean) {
      return v;
    }
    if (v instanceof Integer || v instanceof Long || v instanceof Short || v instanceof Byte) {
      return ((Number) v).longValue();
    }
    if (v instanceof Float f) {
      float x = f.floatValue();
      if (Float.isNaN(x) || Float.isInfinite(x)) {
        return Float.toString(x);
      }
      return x;
    }
    if (v instanceof Double d) {
      double x = d.doubleValue();
      if (Double.isNaN(x) || Double.isInfinite(x)) {
        return Double.toString(x);
      }
      return x;
    }
    if (v instanceof BigInteger bi) {
      return bi.toString();
    }
    if (v instanceof BigDecimal bd) {
      return bd.toPlainString();
    }
    if (v instanceof Number n) {
      double x = n.doubleValue();
      if (Double.isNaN(x) || Double.isInfinite(x)) {
        return Double.toString(x);
      }
      return x;
    }
    if (v instanceof Enum<?> en) {
      return en.name();
    }
    if (v instanceof UUID u) {
      return u.toString();
    }
    if (v instanceof Instant instant) {
      return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(instant);
    }
    if (v instanceof Date date) {
      return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(date.toInstant());
    }

    if (v instanceof Class<?> cl) {
      return "[Function " + cl.getName() + "]";
    }

    if (v instanceof java.lang.reflect.Method m) {
      return "[Function " + m.getName() + "]";
    }

    if (v.getClass().isArray()) {
      return walkArray(v, depth, maxDepth, maxKeys, maxStringLength, redact, seen);
    }

    if (v instanceof Collection<?> col) {
      return walkCollection(col, depth, maxDepth, maxKeys, maxStringLength, redact, seen);
    }

    if (v instanceof Map<?, ?> map) {
      return walkMap(map, depth, maxDepth, maxKeys, maxStringLength, redact, seen);
    }

    if (v instanceof Iterable<?> it) {
      return walkIterable(it, depth, maxDepth, maxKeys, maxStringLength, redact, seen);
    }

    try {
      return String.valueOf(v);
    } catch (Exception e) {
      return UNSERIAL;
    }
  }

  private static Object walkArray(
      Object arr,
      int depth,
      int maxDepth,
      int maxKeys,
      Integer maxStringLength,
      Set<String> redact,
      Set<Object> seen) {
    if (seen.contains(arr)) {
      return CIRCULAR;
    }
    seen.add(arr);
    try {
      if (depth >= maxDepth) {
        return ARRAY_CAP;
      }
      int len = java.lang.reflect.Array.getLength(arr);
      List<Object> out = new ArrayList<>(Math.min(len, maxKeys));
      int n = Math.min(len, maxKeys);
      for (int i = 0; i < n; i++) {
        out.add(
            walk(
                java.lang.reflect.Array.get(arr, i),
                depth + 1,
                maxDepth,
                maxKeys,
                maxStringLength,
                redact,
                seen));
      }
      return out;
    } finally {
      seen.remove(arr);
    }
  }

  @SuppressWarnings("CollectionUndefinedEquality")
  private static Object walkCollection(
      Collection<?> col,
      int depth,
      int maxDepth,
      int maxKeys,
      Integer maxStringLength,
      Set<String> redact,
      Set<Object> seen) {
    if (seen.contains(col)) {
      return CIRCULAR;
    }
    seen.add(col);
    try {
      if (depth >= maxDepth) {
        return ARRAY_CAP;
      }
      List<Object> out = new ArrayList<>();
      int count = 0;
      for (Object item : col) {
        if (count >= maxKeys) {
          break;
        }
        out.add(walk(item, depth + 1, maxDepth, maxKeys, maxStringLength, redact, seen));
        count++;
      }
      return out;
    } finally {
      seen.remove(col);
    }
  }

  @SuppressWarnings("CollectionUndefinedEquality")
  private static Object walkIterable(
      Iterable<?> it,
      int depth,
      int maxDepth,
      int maxKeys,
      Integer maxStringLength,
      Set<String> redact,
      Set<Object> seen) {
    if (seen.contains(it)) {
      return CIRCULAR;
    }
    seen.add(it);
    try {
      if (depth >= maxDepth) {
        return ARRAY_CAP;
      }
      List<Object> out = new ArrayList<>();
      int count = 0;
      for (Object item : it) {
        if (count >= maxKeys) {
          break;
        }
        out.add(walk(item, depth + 1, maxDepth, maxKeys, maxStringLength, redact, seen));
        count++;
      }
      return out;
    } finally {
      seen.remove(it);
    }
  }

  private static Object walkMap(
      Map<?, ?> map,
      int depth,
      int maxDepth,
      int maxKeys,
      Integer maxStringLength,
      Set<String> redact,
      Set<Object> seen) {
    if (seen.contains(map)) {
      return CIRCULAR;
    }
    seen.add(map);
    try {
      if (depth >= maxDepth) {
        return OBJECT_CAP;
      }
      Map<String, Object> out = new java.util.LinkedHashMap<>();
      List<?> keys = new ArrayList<>(map.keySet());
      int n = 0;
      for (int idx = 0; idx < keys.size(); idx++) {
        if (n >= maxKeys) {
          out.put("…", (keys.size() - maxKeys) + " more keys");
          break;
        }
        Object kObj = keys.get(idx);
        String k = kObj == null ? "null" : String.valueOf(kObj);
        try {
          if (shouldRedactKey(k, redact)) {
            out.put(k, "[REDACTED]");
          } else {
            out.put(
                k,
                walk(map.get(kObj), depth + 1, maxDepth, maxKeys, maxStringLength, redact, seen));
          }
        } catch (RuntimeException ex) {
          out.put(k, UNSERIAL);
        }
        n++;
      }
      return out;
    } finally {
      seen.remove(map);
    }
  }
}
