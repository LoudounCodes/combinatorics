package org.loudouncodes.combinatorics;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Combinations — k-subsets of {0,1,...,n-1}, with an opt-in flag for repetition.
 *
 * <p>Fluent usage:
 *
 * <ul>
 *   <li>No repetition (default): {@code Combinations.of(n).choose(k)}
 *   <li>With repetition: {@code Combinations.of(n).withRepetition().choose(k)}
 * </ul>
 *
 * <p>Order is lexicographic in both modes. Returned arrays are fresh defensive copies. Iterators
 * obey the contract: {@code hasNext()} becomes false at exhaustion and {@code next()} then throws
 * {@link java.util.NoSuchElementException}.
 */
public final class Combinations {

  private Combinations() {
    /* no instances */
  }

  /**
   * Entry point for combinations over the ground set {@code {0,1,...,n-1}}.
   *
   * @param n ground-set size (must be {@code >= 0})
   * @return an immutable specification for building combination iterables
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static Spec of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be non-negative");
    return new Spec(n, false); // default: without repetition
  }

  /** Fluent spec for combinations. Immutable value object. */
  public static final class Spec {
    private final int n;
    private final boolean repetition;

    private Spec(int n, boolean repetition) {
      this.n = n;
      this.repetition = repetition;
    }

    /**
     * Enable combinations with repetition (multiset combinations).
     *
     * @return a new {@code Spec} with repetition enabled
     */
    public Spec withRepetition() {
      return new Spec(n, true);
    }

    /**
     * Explicitly disable repetition (regular combinations).
     *
     * @return a new {@code Spec} with repetition disabled
     */
    public Spec withoutRepetition() {
      return new Spec(n, false);
    }

    /**
     * Terminal: enumerate all size-{@code k} combinations in lexicographic order.
     *
     * <p>When repetition is disabled (default), this yields the usual k-combinations (no repeats)
     * and requires {@code 0 <= k <= n}. When repetition is enabled via {@link #withRepetition()},
     * this yields multiset combinations (nondecreasing k-tuples), where {@code k >= 0} and {@code
     * n} may be smaller than {@code k}.
     *
     * @param k subset size (must be {@code >= 0}; and if repetition is disabled, {@code k <= n})
     * @return a sized iterable view yielding defensive copies in lexicographic order
     * @throws IllegalArgumentException if arguments are invalid
     */
    public KChoose choose(int k) {
      if (k < 0) throw new IllegalArgumentException("k must be non-negative");
      if (!repetition && k > n) {
        throw new IllegalArgumentException("k must be \u2264 n when repetition is disabled");
      }
      return new KChoose(n, k, repetition);
    }
  }

  /**
   * Sized iterable view over k-combinations (with or without repetition).
   *
   * <p>Implements {@link Iterable} so it drops into enhanced-for loops, and adds {@link #size()}
   * and {@link #sizeExact()} for counts.
   */
  public static final class KChoose implements Iterable<int[]> {
    private final int n, k;
    private final boolean repetition;

    KChoose(int n, int k, boolean repetition) {
      this.n = n;
      this.k = k;
      this.repetition = repetition;
    }

    /**
     * Number of combinations in this view.
     *
     * <p>No repetition: {@code C(n,k)}. With repetition: {@code C(n+k-1, k)}. This returns a {@code
     * long} and may overflow for very large inputs; for exact counts use {@link #sizeExact()}.
     *
     * @return the number of combinations (best-effort {@code long})
     */
    public long size() {
      if (k == 0) return 1L;
      if (!repetition) {
        if (k > n) return 0L; // defensive
        return binomLong(n, k);
      } else {
        if (n == 0) return 0L; // except k==0 handled above
        return binomLong(n + k - 1, k);
      }
    }

    /**
     * Exact number of combinations as a {@link BigInteger}.
     *
     * @return exact count ({@code C(n,k)} or {@code C(n+k-1,k)})
     */
    public BigInteger sizeExact() {
      if (k == 0) return BigInteger.ONE;
      if (!repetition) {
        if (k > n) return BigInteger.ZERO;
        return binomBig(n, k);
      } else {
        if (n == 0) return BigInteger.ZERO;
        return binomBig(n + k - 1, k);
      }
    }

    @Override
    public Iterator<int[]> iterator() {
      return repetition ? new WithRepIt(n, k) : new NoRepIt(n, k);
    }
  }

  // ---------------------------------------------------------------------------
  // Iterator for combinations WITHOUT repetition (lex order).
  //
  // State 'c' always holds the NEXT combination to return.
  // After emitting 'c', we advance it; if no successor exists, set c = null.
  //
  // First combination (k > 0): [0,1,2,...,k-1]
  // Successor rule: find rightmost i with c[i] < n - k + i; increment c[i];
  //                 then for j > i, set c[j] = c[j - 1] + 1.
  // k == 0: single empty combination.
  // ---------------------------------------------------------------------------
  private static final class NoRepIt implements Iterator<int[]> {
    private final int n, k;
    private int[] c; // null == exhausted

    NoRepIt(int n, int k) {
      this.n = n;
      this.k = k;
      if (k == 0) {
        this.c = new int[0];
      } else if (k > n) {
        // Should be prevented by validation, but keep iterator robust.
        this.c = null;
      } else {
        this.c = new int[k];
        for (int i = 0; i < k; i++) c[i] = i;
      }
    }

    @Override
    public boolean hasNext() {
      return c != null;
    }

    @Override
    public int[] next() {
      if (c == null) throw new NoSuchElementException("Combinations exhausted");
      int[] out = Arrays.copyOf(c, k); // defensive copy

      // advance to next combination
      if (k == 0) {
        c = null; // only one empty combination
        return out;
      }
      int i = k - 1;
      while (i >= 0 && c[i] == n - k + i) i--;
      if (i < 0) {
        c = null; // last one was just returned
      } else {
        c[i]++;
        for (int j = i + 1; j < k; j++) c[j] = c[j - 1] + 1;
      }
      return out;
    }
  }

  // ---------------------------------------------------------------------------
  // Iterator for combinations WITH repetition (multichoose, lex order).
  //
  // Represent each multiset as a nondecreasing k-tuple 'a' with values in 0..n-1.
  // State 'a' always holds the NEXT tuple to return.
  // After emitting 'a', we advance it; if no successor exists, set a = null.
  //
  // First (k > 0): [0,0,...,0]
  // Successor: find rightmost i with a[i] < n - 1; increment a[i]; set all j>i to a[i].
  // Edge cases:
  //   - k == 0: single empty combination.
  //   - n == 0 && k > 0: no combinations (a = null).
  // ---------------------------------------------------------------------------
  private static final class WithRepIt implements Iterator<int[]> {
    private final int n, k;
    private int[] a; // null == exhausted

    WithRepIt(int n, int k) {
      this.n = n;
      this.k = k;
      if (k == 0) {
        this.a = new int[0];
      } else if (n == 0) {
        this.a = null; // no elements to choose from, except when k==0 handled above
      } else {
        this.a = new int[k]; // all zeros to start
      }
    }

    @Override
    public boolean hasNext() {
      return a != null;
    }

    @Override
    public int[] next() {
      if (a == null) throw new NoSuchElementException("Combinations-with-repetition exhausted");
      int[] out = Arrays.copyOf(a, k); // defensive copy

      // advance to next multiset (nondecreasing tuple)
      if (k == 0) {
        a = null; // only one empty combination
        return out;
      }
      int i = k - 1;
      while (i >= 0 && a[i] == n - 1) i--;
      if (i < 0) {
        a = null; // last one was just returned
      } else {
        a[i]++;
        for (int j = i + 1; j < k; j++) a[j] = a[i];
      }
      return out;
    }
  }

  // ---------------------------------------------------------------------------
  // Binomial helpers (long & BigInteger) with reduction to limit overflow.
  // ---------------------------------------------------------------------------

  private static long binomLong(int n, int k) {
    if (k < 0 || k > n) return 0L;
    k = Math.min(k, n - k);
    long res = 1L;
    for (int i = 1; i <= k; i++) {
      long num = n - (k - i); // n-k+1, n-k+2, ..., n
      long den = i;
      long g = gcd(num, den);
      num /= g;
      den /= g;

      // reduce denominator against current result if possible
      long g2 = gcd(res, den);
      if (g2 > 1) {
        res /= g2;
        den /= g2;
      }

      // multiply (may still overflow for very large results; acceptable for classroom sizes)
      res *= num;

      // by construction, den should now be 1
    }
    return res;
  }

  private static BigInteger binomBig(int n, int k) {
    if (k < 0 || k > n) return BigInteger.ZERO;
    k = Math.min(k, n - k);
    BigInteger res = BigInteger.ONE;
    for (int i = 1; i <= k; i++) {
      int num = n - (k - i);
      int den = i;
      int g = (int) gcd(num, den);
      num /= g;
      den /= g;

      // reduce denominator against res if possible (exact division)
      BigInteger denBI = BigInteger.valueOf(den);
      BigInteger[] div = res.divideAndRemainder(denBI);
      if (div[1].equals(BigInteger.ZERO)) {
        res = div[0];
        den = 1;
      }

      res = res.multiply(BigInteger.valueOf(num));
      if (den != 1) {
        res = res.divide(BigInteger.valueOf(den)); // exact
      }
    }
    return res;
  }

  private static long gcd(long a, long b) {
    a = Math.abs(a);
    b = Math.abs(b);
    while (b != 0) {
      long t = a % b;
      a = b;
      b = t;
    }
    return a;
  }
}
