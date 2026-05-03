package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SnapshotExtendedCoverageTest {

  @Test
  void snapshotRedactKeysAllBlankNormalizesToNullSet() {
    assertNotNull(
        Snapshot.snapshot(
            Map.of("a", 1),
            SerializeOptions.builder().redactKeys(java.util.Arrays.asList("", "  ")).build()));
  }

  @Test
  void snapshotCharSequenceShortFloatDoubleAtomicIntegerAndFinitePaths() {
    assertNotNull(Snapshot.snapshot(new StringBuilder("abcde")));
    assertNotNull(Snapshot.snapshot((short) 8));
    assertNotNull(Snapshot.snapshot(2.5f));
    assertNotNull(Snapshot.snapshot(1.25d));
    assertNotNull(Snapshot.snapshot(new java.util.concurrent.atomic.AtomicInteger(9)));
  }

  @Test
  void snapshotCircularArrayCollectionIterableAndNullMapKey() {
    Object[] self = new Object[1];
    self[0] = self;
    assertEquals(List.of("[Circular]"), Snapshot.snapshot(self));

    java.util.List<Object> ring = new java.util.ArrayList<>();
    ring.add(ring);
    assertEquals(List.of("[Circular]"), Snapshot.snapshot(ring));

    class SelfIterable implements Iterable<Object> {
      private boolean hasNext = true;

      @Override
      public java.util.Iterator<Object> iterator() {
        return new java.util.Iterator<Object>() {
          @Override
          public boolean hasNext() {
            return hasNext;
          }

          @Override
          public Object next() {
            hasNext = false;
            return SelfIterable.this;
          }
        };
      }
    }
    assertEquals(List.of("[Circular]"), Snapshot.snapshot(new SelfIterable()));
    assertEquals(
        "[Array]",
        Snapshot.snapshot(new SelfIterable(), SerializeOptions.builder().maxDepth(0).build())
            .toString());

    java.util.Map<Object, Object> nullKey = new java.util.LinkedHashMap<>();
    nullKey.put(null, 1);
    @SuppressWarnings("unchecked")
    Map<String, Object> out = (Map<String, Object>) Snapshot.snapshot(nullKey);
    assertTrue(out.containsKey("null"));

    assertNotNull(Snapshot.snapshot('z'));
    assertEquals(Boolean.TRUE, Snapshot.snapshot(Boolean.TRUE));
    assertNotNull(Snapshot.snapshot(new java.util.concurrent.atomic.AtomicLong(2L)));
    assertNotNull(
        Snapshot.snapshot(
            Map.of("a", 1), SerializeOptions.builder().redactKeys(List.of("password")).build()));
  }

  @Test
  void snapshotNumericBranchesAndDepth() {
    assertNotNull(Snapshot.snapshot(Float.MAX_VALUE));
    assertNotNull(Snapshot.snapshot(Double.NaN));
    assertNotNull(Snapshot.snapshot(new BigInteger("999999999999999999999999999999999999999999")));
    assertNotNull(Snapshot.snapshot(new BigDecimal("1e400")));
    Object deeper =
        Snapshot.snapshot(
            Map.of("n", Map.of("x", 1)), SerializeOptions.builder().maxDepth(0).build());
    assertEquals("[Object]", deeper.toString());
    Object arrCap =
        Snapshot.snapshot(new int[] {1}, SerializeOptions.builder().maxDepth(0).build());
    assertEquals("[Array]", arrCap.toString());
    Object arrWalk =
        Snapshot.snapshot(new byte[] {1, 2}, SerializeOptions.builder().maxKeys(1).build());
    assertNotNull(arrWalk);
    Object colExtra =
        Snapshot.snapshot(List.of(1, 2, 3), SerializeOptions.builder().maxKeys(2).build());
    assertNotNull(colExtra);
    class Endless implements Iterable<Integer> {
      @Override
      public java.util.Iterator<Integer> iterator() {
        return new java.util.Iterator<Integer>() {
          int n;

          @Override
          public boolean hasNext() {
            return n < 5;
          }

          @Override
          public Integer next() {
            return n++;
          }
        };
      }
    }
    assertNotNull(Snapshot.snapshot(new Endless(), SerializeOptions.builder().maxKeys(2).build()));
    Object limNeg =
        Snapshot.snapshot(
            "hi", SerializeOptions.builder().maxDepth(-1).maxKeys(-1).maxStringLength(-1).build());
    assertEquals("hi", limNeg);
  }

  @Test
  @SuppressWarnings("unchecked")
  void snapshotRedactKeysNullWhenListOnlyHasEmptyStrings() {
    SerializeOptions opts = SerializeOptions.builder().redactKeys(List.of("")).build();
    Map<String, Object> out =
        (Map<String, Object>) Snapshot.snapshot(Map.of("password", "x"), opts);
    assertEquals("x", out.get("password"));
  }

  @Test
  void snapshotGenericNumberNanSerializesAsDoubleString() {
    Number odd =
        new Number() {
          @Override
          public int intValue() {
            return 0;
          }

          @Override
          public long longValue() {
            return 0L;
          }

          @Override
          public float floatValue() {
            return 0f;
          }

          @Override
          public double doubleValue() {
            return Double.NaN;
          }
        };
    assertEquals("NaN", Snapshot.snapshot(odd));
  }
}
