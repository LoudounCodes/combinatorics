package org.loudouncodes.combinatorics;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Iterator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CountsTest {

  // ---------- PowerSet ----------
  @Test
  @DisplayName("PowerSet: count()==2^n and size() agrees for small n")
  void powerSetCounts() {
    for (int n = 0; n <= 12; n++) {
      BigInteger expected = BigInteger.ONE.shiftLeft(n);
      BigInteger count = PowerSet.of(n).count();
      assertEquals(expected, count, "count mismatch for n=" + n);
      long size = PowerSet.of(n).size();
      long expectedLong = expected.bitLength() <= 63 ? expected.longValue() : Long.MAX_VALUE;
      assertEquals(expectedLong, size, "size mismatch for n=" + n);
    }
  }

  // ---------- CartesianProduct ----------
  @Test
  @DisplayName("CartesianProduct: size() and sizeExact() match product, incl. zero dim")
  void cartesianCounts() {
    CartesianProduct.Product p1 = CartesianProduct.of(3, 4, 2);
    assertEquals(24L, p1.size());
    assertEquals(BigInteger.valueOf(24), p1.sizeExact());

    CartesianProduct.Product p2 = CartesianProduct.of();
    assertEquals(1L, p2.size());
    assertEquals(BigInteger.ONE, p2.sizeExact());

    CartesianProduct.Product p3 = CartesianProduct.of(3, 0, 5);
    assertEquals(0L, p3.size());
    assertEquals(BigInteger.ZERO, p3.sizeExact());
  }

  // ---------- Permutations ----------
  @Test
  @DisplayName("Permutations: size() = P(n,k), sizeExact() agrees")
  void permutationsCounts() {
    Permutations.KTake p = Permutations.of(6).take(3);
    assertEquals(120L, p.size()); // 6*5*4
    assertEquals(BigInteger.valueOf(120), p.sizeExact());
  }

  // ---------- Combinations (no repetition) ----------
  @Test
  @DisplayName("Combinations (no rep): size() = C(n,k), sizeExact() agrees")
  void combinationsNoRepCounts() {
    Combinations.KChoose c = Combinations.of(12).choose(3);
    assertEquals(220L, c.size());
    assertEquals(BigInteger.valueOf(220), c.sizeExact());
  }

  // ---------- Combinations (with repetition) ----------
  @Test
  @DisplayName("Combinations (with rep): size() = C(n+k-1,k), sizeExact() agrees")
  void combinationsWithRepCounts() {
    Combinations.KChoose c = Combinations.of(5).withRepetition().choose(3); // C(7,3)=35
    assertEquals(35L, c.size());
    assertEquals(BigInteger.valueOf(35), c.sizeExact());
  }

  // ---------- Derangements ----------
  @Test
  @DisplayName("Derangements: All.size() equals Builder.size() and known values for n=0..8")
  void derangementsCounts() {
    long[] known = {1, 0, 1, 2, 9, 44, 265, 1854, 14833};
    for (int n = 0; n < known.length; n++) {
      long a = Derangements.of(n).all().size();
      long b = Derangements.of(n).size();
      assertEquals(known[n], a, "All.size mismatch for n=" + n);
      assertEquals(known[n], b, "Builder.size mismatch for n=" + n);
      // sanity: enumeration count matches size()
      int enumCount = 0;
      for (Iterator<int[]> it = Derangements.of(n).all().iterator(); it.hasNext(); ) {
        enumCount++;
        it.next();
      }
      assertEquals(known[n], enumCount, "enumeration count mismatch for n=" + n);
    }
  }
}
