package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class IntentProofClientWrapFormsTest {

  @Test
  void wrapRunnableSupplierBiFunctionAndWrapAll() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    var opt = WrapOptions.builder().intent("r").action("a.r").build();

    Runnable r = c.wrap(opt, () -> {});
    r.run();

    Supplier<Integer> sup =
        c.wrap(WrapOptions.builder().intent("s").action("a.s").build(), () -> 7);
    assertEquals(7, sup.get());

    Function<String, String> f =
        c.wrap(
            WrapOptions.builder().intent("f").action("a.f").build(), (String t) -> t.toUpperCase());
    assertEquals("HI", f.apply("hi"));

    java.util.function.BiFunction<Integer, Integer, Integer> bf =
        c.wrap(WrapOptions.builder().intent("bf").action("a.bf").build(), Integer::sum);
    assertEquals(5, bf.apply(2, 3));

    Function<Object[], String> all =
        c.wrapAll(
            WrapOptions.builder().intent("all").action("a.all").build(),
            a -> "n=" + (a == null ? 0 : a.length));
    assertEquals("n=0", all.apply(null));
    assertEquals("n=2", all.apply(new Object[] {1, 2}));

    assertEquals(6, mem.getEvents().size());
  }
}
