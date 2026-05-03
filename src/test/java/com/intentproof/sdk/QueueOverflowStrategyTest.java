package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class QueueOverflowStrategyTest {

  @Test
  void wireRoundTrip() {
    assertEquals("drop-newest", QueueOverflowStrategy.DROP_NEWEST.wireName());
    assertEquals("drop-oldest", QueueOverflowStrategy.DROP_OLDEST.wireName());
    assertEquals(QueueOverflowStrategy.DROP_NEWEST, QueueOverflowStrategy.fromWire(null));
    assertEquals(QueueOverflowStrategy.DROP_NEWEST, QueueOverflowStrategy.fromWire("drop-newest"));
    assertEquals(QueueOverflowStrategy.DROP_OLDEST, QueueOverflowStrategy.fromWire("drop-oldest"));
    assertThrows(IllegalArgumentException.class, () -> QueueOverflowStrategy.fromWire("unknown"));
  }
}
