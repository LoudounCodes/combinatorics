package org.loudouncodes.combinatorics;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Binary Gray codes of length {@code n}.
 *
 * <p>Fluent entrypoint:
 *
 * <pre>{@code
 * // integers 0..(2^n-1) in reflected Gray order
 * for (int g : BinaryGray.of(3).asInts()) {
 *   // 0, 1, 3, 2, 6, 7, 5, 4
 * }
 *
 * // bit vectors (0/1) of length n, minimal-change order (one bit flips each step)
 * for (int[] bits : BinaryGray.of(3).asBits()) {
 *   // [0,0,0], [1,0,0], [1,1,0], [0,1,0], ...
 * }
 *
 * // subsets as sorted index lists
 * for (int[] S : BinaryGray.of(4).asSubsets()) {
 *   // [], [0], [0,1], [1], [1,2], [0,1,2], ...
 * }
 * }</pre>
 *
 * <p>Views also available as {@code long}/{@link java.math.BigInteger BigInteger} bitmasks and as a
 * stream of minimal-change {@link BinaryGray.Toggle} events. All iterators return defensive copies
 * and obey the {@code hasNext()}/{@code next()} contract (exhaustion throws {@link
 * java.util.NoSuchElementException}).
 *
 * @since 0.3.0
 */
public final class BinaryGray {

  private BinaryGray() {}

  /**
   * Create a builder for Gray codes with {@code n} bits.
   *
   * @param n number of bits, must be {@code >= 0}
   * @return a builder for {@code n}-bit Gray sequences
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static Builder of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be >= 0");
    return new Builder(n);
  }

  /** Builder bound to {@code n} bits, offering multiple views of the same Gray sequence. */
  public static final class Builder {
    private final int n;

    private Builder(int n) {
      this.n = n;
    }

    /**
     * Integers in reflected Gray order.
     *
     * <p>Each element is {@code g = i ^ (i >>> 1)} for {@code i = 0..2^n-1}.
     *
     * @return iterable over {@code int} Gray codes
     * @throws IllegalArgumentException if {@code n > 31} (does not fit in {@code int})
     */
    public AsInts asInts() {
      if (n > 31)
        throw new IllegalArgumentException(
            "asInts() supports n up to 31; use asLongMasks() or asBigMasks()");
      return new AsInts(n);
    }

    /**
     * Bit vectors (arrays of 0/1) of length {@code n} in minimal-change order.
     *
     * @return iterable over {@code int[]} bit vectors
     */
    public AsBits asBits() {
      return new AsBits(n);
    }

    /**
     * Subsets represented as sorted index lists (ascending) in minimal-change order.
     *
     * @return iterable over {@code int[]} of selected indices
     */
    public AsSubsets asSubsets() {
      return new AsSubsets(n);
    }

    /**
     * Gray codes as {@code long} bitmasks (bit i ↔ element i).
     *
     * <p>Note: see {@link BinaryGray.AsLongMasks#size()} and {@link
     * BinaryGray.AsLongMasks#sizeExact()} for count semantics.
     *
     * @return iterable over {@code long} Gray masks
     * @throws IllegalArgumentException if {@code n > 62} (count does not fit in signed {@code
     *     long})
     */
    public AsLongMasks asLongMasks() {
      if (n > 62)
        throw new IllegalArgumentException(
            "asLongMasks() supports n up to 62; use asBigMasks() for larger n");
      return new AsLongMasks(n);
    }

    /**
     * Gray codes as {@link BigInteger} bitmasks (no size limit).
     *
     * @return iterable over {@link BigInteger} Gray masks
     */
    public AsBigMasks asBigMasks() {
      return new AsBigMasks(n);
    }

    /**
     * Minimal-change delta stream. Each element indicates which bit flipped and its new value.
     *
     * <p>Emits {@code 2^n - 1} toggles (there are that many steps between {@code 2^n} states).
     *
     * @return iterable over {@link Toggle} events
     */
    public AsToggles asToggles() {
      return new AsToggles(n);
    }
  }

  // ----------------------------------------------------------------------------------------------
  // Public views
  // ----------------------------------------------------------------------------------------------

  /** Iterable over {@code int} Gray codes, {@code n <= 31}. */
  public static final class AsInts implements Iterable<Integer> {
    private final int n;

    private AsInts(int n) {
      this.n = n;
    }

    /**
     * Returns the number of elements in the sequence.
     *
     * @return {@code 2^n} (fits in {@code long} for {@code n <= 62})
     */
    public long size() {
      return 1L << n; // n <= 31 here, so safe
    }

    /**
     * Returns the exact number of elements in the sequence.
     *
     * @return {@code 2^n}
     */
    public BigInteger sizeExact() {
      return BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public Iterator<Integer> iterator() {
      return new IntsIt(n);
    }
  }

  /** Iterable over bit vectors (arrays of 0/1), minimal-change order, any {@code n >= 0}. */
  public static final class AsBits implements Iterable<int[]> {
    private final int n;

    private AsBits(int n) {
      this.n = n;
    }

    /**
     * Returns the (possibly saturated) number of elements in the sequence.
     *
     * @return {@code 2^n}, or {@code Long.MAX_VALUE} if the exact value exceeds {@code long}
     */
    public long size() {
      BigInteger s = sizeExact();
      return s.bitLength() <= 63 ? s.longValue() : Long.MAX_VALUE;
    }

    /**
     * Returns the exact number of elements in the sequence.
     *
     * @return {@code 2^n}
     */
    public BigInteger sizeExact() {
      return BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public Iterator<int[]> iterator() {
      return new BitsIt(n);
    }
  }

  /** Iterable over subsets as sorted index arrays, minimal-change order, any {@code n >= 0}. */
  public static final class AsSubsets implements Iterable<int[]> {
    private final int n;

    private AsSubsets(int n) {
      this.n = n;
    }

    /**
     * Returns the (possibly saturated) number of subsets.
     *
     * @return {@code 2^n}, or {@code Long.MAX_VALUE} if the exact value exceeds {@code long}
     */
    public long size() {
      BigInteger s = sizeExact();
      return s.bitLength() <= 63 ? s.longValue() : Long.MAX_VALUE;
    }

    /**
     * Returns the exact number of subsets.
     *
     * @return {@code 2^n}
     */
    public BigInteger sizeExact() {
      return BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public Iterator<int[]> iterator() {
      return new SubsetsIt(n);
    }
  }

  /** Iterable over {@code long} Gray bitmasks, {@code n <= 62}. */
  public static final class AsLongMasks implements Iterable<Long> {
    private final int n;

    private AsLongMasks(int n) {
      this.n = n;
    }

    /**
     * Returns the number of elements in the sequence.
     *
     * @return {@code 2^n}
     */
    public long size() {
      // n <= 62 per validation
      return 1L << n;
    }

    /**
     * Returns the exact number of elements in the sequence.
     *
     * @return {@code 2^n}
     */
    public BigInteger sizeExact() {
      return BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public Iterator<Long> iterator() {
      return new LongMasksIt(n);
    }
  }

  /** Iterable over {@link BigInteger} Gray bitmasks, any {@code n >= 0}. */
  public static final class AsBigMasks implements Iterable<BigInteger> {
    private final int n;

    private AsBigMasks(int n) {
      this.n = n;
    }

    /**
     * Returns the (possibly saturated) number of elements in the sequence.
     *
     * @return {@code 2^n}, or {@code Long.MAX_VALUE} if the exact value exceeds {@code long}
     */
    public long size() {
      BigInteger s = sizeExact();
      return s.bitLength() <= 63 ? s.longValue() : Long.MAX_VALUE;
    }

    /**
     * Returns the exact number of elements in the sequence.
     *
     * @return {@code 2^n}
     */
    public BigInteger sizeExact() {
      return BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public Iterator<BigInteger> iterator() {
      return new BigMasksIt(n);
    }
  }

  /** Iterable over minimal-change toggle events (length {@code 2^n - 1}). */
  public static final class AsToggles implements Iterable<Toggle> {
    private final int n;

    private AsToggles(int n) {
      this.n = n;
    }

    /**
     * Returns the (possibly saturated) number of toggles.
     *
     * @return {@code 2^n - 1}, or {@code Long.MAX_VALUE} if the exact value exceeds {@code long}
     */
    public long size() {
      BigInteger s = sizeExact();
      return s.bitLength() <= 63 ? s.longValue() : Long.MAX_VALUE;
    }

    /**
     * Returns the exact number of toggles.
     *
     * @return {@code 2^n - 1}
     */
    public BigInteger sizeExact() {
      return BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE);
    }

    @Override
    public Iterator<Toggle> iterator() {
      return new TogglesIt(n);
    }
  }

  /**
   * Minimal-change event: bit {@link #index} flipped to {@link #value}.
   *
   * <p>Index {@code 0} refers to the least-significant bit (LSB), matching the numeric masks.
   */
  public static final class Toggle {
    /** Bit position that flipped (0 = least-significant bit). */
    public final int index;

    /** The bit’s new value after the flip: {@code true} for 1, {@code false} for 0. */
    public final boolean value;

    /**
     * Constructs a toggle event.
     *
     * @param index bit index (0..n-1)
     * @param value new bit value after the flip
     */
    public Toggle(int index, boolean value) {
      this.index = index;
      this.value = value;
    }
  }

  // ----------------------------------------------------------------------------------------------
  // Iterators
  // ----------------------------------------------------------------------------------------------

  /** int Gray codes via {@code g = i ^ (i >>> 1)}, {@code i = 0..2^n-1} (n <= 31). */
  private static final class IntsIt implements Iterator<Integer> {
    private final int limit;
    private int i = 0;

    IntsIt(int n) {
      this.limit = 1 << n;
    }

    @Override
    public boolean hasNext() {
      return i < limit;
    }

    @Override
    public Integer next() {
      if (!hasNext()) throw new NoSuchElementException();
      int g = i ^ (i >>> 1);
      i++;
      return Integer.valueOf(g);
    }
  }

  /** long Gray masks via {@code g = i ^ (i >>> 1)}, {@code i = 0..2^n-1} (n <= 62). */
  private static final class LongMasksIt implements Iterator<Long> {
    private final long limit;
    private long i = 0L;

    LongMasksIt(int n) {
      this.limit = 1L << n; // n<=62
    }

    @Override
    public boolean hasNext() {
      return i < limit;
    }

    @Override
    public Long next() {
      if (!hasNext()) throw new NoSuchElementException();
      long g = i ^ (i >>> 1);
      i++;
      return Long.valueOf(g);
    }
  }

  /** BigInteger Gray masks via {@code g = i ^ (i >> 1)}, {@code i = 0..2^n-1} (any n). */
  private static final class BigMasksIt implements Iterator<BigInteger> {
    private final BigInteger limit;
    private BigInteger i = BigInteger.ZERO;

    BigMasksIt(int n) {
      this.limit = BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public boolean hasNext() {
      return i.compareTo(limit) < 0;
    }

    @Override
    public BigInteger next() {
      if (!hasNext()) throw new NoSuchElementException();
      BigInteger g = i.xor(i.shiftRight(1));
      i = i.add(BigInteger.ONE);
      return g;
    }
  }

  /**
   * Bits iterator using the classic “trailing zeros” toggle rule.
   *
   * <p>State starts as all zeros. On step {@code s = 1..2^n-1}, flip bit at {@code r = tz(s)}.
   */
  private static final class BitsIt implements Iterator<int[]> {
    private final int n;
    private final int[] bits; // 0/1, current state
    private final BigInteger limit; // 2^n states
    private BigInteger emitted = BigInteger.ZERO; // states emitted so far

    BitsIt(int n) {
      this.n = n;
      this.bits = new int[n]; // all zeros
      this.limit = BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public boolean hasNext() {
      return emitted.compareTo(limit) < 0;
    }

    @Override
    public int[] next() {
      if (!hasNext()) throw new NoSuchElementException();
      if (emitted.signum() == 0) {
        emitted = BigInteger.ONE;
        return Arrays.copyOf(bits, n); // initial zeros
      }
      // flip bit at index = lowest set bit of emitted (the step counter)
      int r = emitted.getLowestSetBit(); // 0..n-1 for s in [1, 2^n-1]
      bits[r] ^= 1;
      emitted = emitted.add(BigInteger.ONE);
      return Arrays.copyOf(bits, n);
    }
  }

  /** Subsets iterator built over BitsIt, mapping 0/1 vectors to sorted index arrays. */
  private static final class SubsetsIt implements Iterator<int[]> {
    private final BitsIt bitsIt;

    SubsetsIt(int n) {
      this.bitsIt = new BitsIt(n);
    }

    @Override
    public boolean hasNext() {
      return bitsIt.hasNext();
    }

    @Override
    public int[] next() {
      int[] b = bitsIt.next();
      int cnt = 0;
      for (int v : b) if (v == 1) cnt++;
      int[] out = new int[cnt];
      int w = 0;
      for (int i = 0; i < b.length; i++) if (b[i] == 1) out[w++] = i;
      return out;
    }
  }

  /**
   * Toggle iterator: emits the minimal-change flip at each step (no initial state element).
   *
   * <p>Yields {@code 2^n - 1} items for {@code n} bits.
   */
  private static final class TogglesIt implements Iterator<Toggle> {
    private final int n;
    private final boolean[] cur; // current bits; we use it only to compute new value
    private final BigInteger limit; // number of states
    private BigInteger s = BigInteger.ONE; // step counter from 1..2^n-1

    TogglesIt(int n) {
      this.n = n;
      this.cur = new boolean[n];
      this.limit = BigInteger.ONE.shiftLeft(n);
    }

    @Override
    public boolean hasNext() {
      // there are (2^n - 1) toggles: s in [1, 2^n-1]
      return s.compareTo(limit) < 0;
    }

    @Override
    public Toggle next() {
      if (!hasNext()) throw new NoSuchElementException();
      int r = s.getLowestSetBit();
      cur[r] = !cur[r];
      Toggle t = new Toggle(r, cur[r]);
      s = s.add(BigInteger.ONE);
      return t;
    }
  }
}
