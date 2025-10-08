package org.loudouncodes.combinatorics;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BinaryGrayTest {

  // ---------- helpers ----------

  private static int hamming1(int a, int b) {
    return Integer.bitCount(a ^ b);
  }

  private static int hamming1(long a, long b) {
    return Long.bitCount(a ^ b);
  }

  private static int hamming1(BigInteger a, BigInteger b) {
    return a.xor(b).bitCount();
  }

  private static int[] bitsToSubset(int[] bits) {
    int c = 0;
    for (int v : bits) if (v == 1) c++;
    int[] out = new int[c];
    int w = 0;
    for (int i = 0; i < bits.length; i++) if (bits[i] == 1) out[w++] = i;
    return out;
  }

  private static int subsetSymmDiffCard(int[] a, int[] b) {
    int i = 0, j = 0, c = 0;
    while (i < a.length || j < b.length) {
      if (i == a.length) {
        c += (b.length - j);
        break;
      }
      if (j == b.length) {
        c += (a.length - i);
        break;
      }
      if (a[i] == b[j]) {
        i++;
        j++;
      } else if (a[i] < b[j]) {
        c++;
        i++;
      } else {
        c++;
        j++;
      }
    }
    return c;
  }

  private static int bitsToMask(int[] bits) {
    int m = 0;
    for (int i = 0; i < bits.length; i++) if (bits[i] == 1) m |= (1 << i);
    return m;
  }

  // ---------- asInts ----------

  @Test
  @DisplayName("asInts (n=3): exact reflected Gray sequence and Hamming-1 property")
  void asInts_n3_sequence() {
    List<Integer> seq = new ArrayList<>();
    for (int g : BinaryGray.of(3).asInts()) seq.add(g);

    int[] expected = {0, 1, 3, 2, 6, 7, 5, 4};
    assertEquals(expected.length, seq.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], seq.get(i).intValue(), "position " + i);
    }

    for (int i = 1; i < seq.size(); i++) {
      assertEquals(
          1,
          hamming1(seq.get(i - 1), seq.get(i)),
          "consecutive ints should differ by one bit at " + i);
    }

    // size() / sizeExact()
    var view = BinaryGray.of(3).asInts();
    assertEquals(8L, view.size());
    assertEquals(BigInteger.valueOf(8), view.sizeExact());
  }

  @Test
  @DisplayName("asInts: iterator contract—exhaustion throws NoSuchElementException")
  void asInts_iterator_contract() {
    Iterator<Integer> it = BinaryGray.of(2).asInts().iterator();
    assertTrue(it.hasNext());
    assertEquals(0, it.next());
    assertTrue(it.hasNext());
    assertEquals(1, it.next());
    assertTrue(it.hasNext());
    assertEquals(3, it.next());
    assertTrue(it.hasNext());
    assertEquals(2, it.next());
    assertFalse(it.hasNext());
    assertThrows(java.util.NoSuchElementException.class, it::next);
  }

  // ---------- asBits ----------

  @Test
  @DisplayName(
      "asBits (n=4): matches asInts as masks; Hamming-1 between successive bit vectors; defensive copies")
  void asBits_matches_ints_and_gray_property() {
    var ints = new ArrayList<Integer>();
    for (int g : BinaryGray.of(4).asInts()) ints.add(g);

    int prevMask = -1;
    int[] prevBits = null;

    int idx = 0;
    for (int[] bits : BinaryGray.of(4).asBits()) {
      // defensive copy (fresh array each time)
      if (prevBits != null) {
        assertNotSame(prevBits, bits);
      }
      // compute mask and compare with asInts
      int mask = bitsToMask(bits);
      assertEquals(ints.get(idx).intValue(), mask, "mask mismatch at position " + idx);

      // Hamming-1 between successive bit vectors
      if (prevMask != -1) {
        assertEquals(1, hamming1(prevMask, mask), "bits differ by one bit at " + idx);
      }

      // try to mutate returned array; must not affect iterator state
      if (bits.length > 0) bits[0] ^= 1;

      prevBits = bits;
      prevMask = mask;
      idx++;
    }
    assertEquals(16, idx);
  }

  // ---------- asSubsets ----------

  @Test
  @DisplayName("asSubsets (n=4): symmetric difference size 1; matches masks from asInts")
  void asSubsets_gray_property_and_map_to_masks() {
    var ints = new ArrayList<Integer>();
    for (int g : BinaryGray.of(4).asInts()) ints.add(g);

    int[] prev = null;
    int idx = 0;
    for (int[] subset : BinaryGray.of(4).asSubsets()) {
      // symm. diff with previous is exactly 1
      if (prev != null) {
        assertEquals(1, subsetSymmDiffCard(prev, subset), "subset change size 1 at " + idx);
      }
      // convert subset->mask and compare to asInts
      int mask = 0;
      for (int v : subset) mask |= (1 << v);
      assertEquals(ints.get(idx).intValue(), mask, "subset mask mismatch at " + idx);

      prev = subset.clone(); // avoid aliasing
      idx++;
    }
    assertEquals(16, idx);
  }

  // ---------- asLongMasks ----------

  @Test
  @DisplayName("asLongMasks (n=6): matches asInts masks; Hamming-1 property")
  void asLongMasks_matches_ints() {
    var ints = new ArrayList<Integer>();
    for (int g : BinaryGray.of(6).asInts()) ints.add(g);

    long prev = -1L;
    int idx = 0;
    for (long m : BinaryGray.of(6).asLongMasks()) {
      assertEquals((long) (ints.get(idx) & 0xFFFFFFFFL), m, "mask mismatch at " + idx);
      if (prev != -1L) {
        assertEquals(1, hamming1(prev, m), "long masks differ by one bit at " + idx);
      }
      prev = m;
      idx++;
    }
    assertEquals(64, idx);
  }

  // ---------- asBigMasks ----------

  @Test
  @DisplayName("asBigMasks (n=3): matches BigInteger masks; Hamming-1 property")
  void asBigMasks_small() {
    List<BigInteger> seq = new ArrayList<>();
    for (BigInteger g : BinaryGray.of(3).asBigMasks()) seq.add(g);

    BigInteger[] expected = {
      BigInteger.ZERO,
      BigInteger.ONE,
      BigInteger.valueOf(3),
      BigInteger.valueOf(2),
      BigInteger.valueOf(6),
      BigInteger.valueOf(7),
      BigInteger.valueOf(5),
      BigInteger.valueOf(4)
    };
    assertArrayEquals(expected, seq.toArray());

    for (int i = 1; i < seq.size(); i++) {
      assertEquals(1, hamming1(seq.get(i - 1), seq.get(i)));
    }
  }

  @Test
  @DisplayName("asBigMasks: sizeExact works for large n without iterating")
  void asBigMasks_large_size_only() {
    var view = BinaryGray.of(70).asBigMasks();
    assertEquals(BigInteger.ONE.shiftLeft(70), view.sizeExact());
    // don't iterate; just ensure size() saturates
    assertEquals(Long.MAX_VALUE, view.size());
  }

  // ---------- asToggles ----------

  @Test
  @DisplayName("asToggles (n=5): reconstruct states and match asBits sequence")
  void asToggles_reconstructs_bits_sequence() {
    // Build reference sequence from asBits
    List<int[]> ref = new ArrayList<>();
    for (int[] b : BinaryGray.of(5).asBits()) ref.add(b);

    // Now reconstruct states via toggles
    boolean[] cur = new boolean[5];
    List<int[]> recon = new ArrayList<>();
    recon.add(new int[] {0, 0, 0, 0, 0}); // initial state
    for (BinaryGray.Toggle t : BinaryGray.of(5).asToggles()) {
      assertTrue(t.index >= 0 && t.index < 5);
      cur[t.index] = t.value;
      int[] b = new int[5];
      for (int i = 0; i < 5; i++) b[i] = cur[i] ? 1 : 0;
      recon.add(b);
    }

    assertEquals(ref.size(), recon.size(), "number of states mismatch");
    for (int i = 0; i < ref.size(); i++) {
      assertArrayEquals(ref.get(i), recon.get(i), "state mismatch at " + i);
    }

    var togglesView = BinaryGray.of(5).asToggles();
    assertEquals(BigInteger.ONE.shiftLeft(5).subtract(BigInteger.ONE), togglesView.sizeExact());
  }

  // ---------- edge cases & validation ----------

  @Test
  @DisplayName("Edge case n=0: exactly one state, no toggles")
  void edge_n0() {
    var ints = BinaryGray.of(0).asInts();
    Iterator<Integer> it = ints.iterator();
    assertTrue(it.hasNext());
    assertEquals(0, it.next().intValue());
    assertFalse(it.hasNext());

    var bits = BinaryGray.of(0).asBits();
    Iterator<int[]> ib = bits.iterator();
    assertTrue(ib.hasNext());
    assertArrayEquals(new int[0], ib.next());
    assertFalse(ib.hasNext());

    var subs = BinaryGray.of(0).asSubsets();
    Iterator<int[]> is = subs.iterator();
    assertTrue(is.hasNext());
    assertArrayEquals(new int[0], is.next());
    assertFalse(is.hasNext());

    var toggles = BinaryGray.of(0).asToggles();
    assertEquals(BigInteger.ZERO, toggles.sizeExact());
    assertFalse(toggles.iterator().hasNext());
  }

  @Test
  @DisplayName("Validation: asInts rejects n>31; asLongMasks rejects n>62")
  void validation_limits() {
    assertThrows(IllegalArgumentException.class, () -> BinaryGray.of(32).asInts());
    assertDoesNotThrow(() -> BinaryGray.of(31).asInts());

    assertThrows(IllegalArgumentException.class, () -> BinaryGray.of(63).asLongMasks());
    assertDoesNotThrow(() -> BinaryGray.of(62).asLongMasks());
  }
}
