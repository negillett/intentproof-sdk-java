package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SnapshotWorkflowIntegrationTest {

  @Test
  void snapshotSerializesDiverseTypesAndIterablePath() {
    enum E {
      A
    }
    class Iter implements Iterable<String> {
      @Override
      public Iterator<String> iterator() {
        return List.of("x").iterator();
      }
    }
    Object m =
        Snapshot.snapshot(
            Map.of(
                "e",
                E.A,
                "u",
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                "d",
                new Date(0),
                "i",
                Instant.EPOCH,
                "n",
                Float.NaN,
                "x",
                Double.POSITIVE_INFINITY,
                "bi",
                java.math.BigInteger.ONE,
                "bd",
                java.math.BigDecimal.ONE,
                "iter",
                new Iter()),
            SerializeOptions.builder().maxDepth(20).build());
    assertNotNull(m);
    SerializeOptions built =
        SerializeOptions.builder().maxDepth(1).maxKeys(2).build().toBuilder().build();
    assertNotNull(Snapshot.snapshot(List.of(List.of(1)), built));
    java.util.AbstractCollection<String> bomb =
        new java.util.AbstractCollection<String>() {
          @Override
          public Iterator<String> iterator() {
            throw new RuntimeException("no iter");
          }

          @Override
          public int size() {
            return 1;
          }
        };
    assertEquals("[SnapshotError]", Snapshot.snapshot(bomb));
    class BadToString {
      @Override
      public String toString() {
        throw new RuntimeException("no");
      }
    }
    assertEquals("[Unserializable]", Snapshot.snapshot(new BadToString()));
    try {
      Method meth = String.class.getMethod("length");
      assertNotNull(Snapshot.snapshot(meth));
      assertNotNull(Snapshot.snapshot(String.class));
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void snapshotMapKeyThrowsUsesUnserialToken() {
    Map<String, Object> evil =
        new java.util.AbstractMap<String, Object>() {
          @Override
          public Object get(Object key) {
            throw new RuntimeException("no");
          }

          @Override
          public java.util.Set<Entry<String, Object>> entrySet() {
            return Map.of("k", (Object) 1).entrySet();
          }
        };
    @SuppressWarnings("unchecked")
    Map<String, Object> out = (Map<String, Object>) Snapshot.snapshot(evil);
    assertEquals("[Unserializable]", out.get("k"));
  }
}
