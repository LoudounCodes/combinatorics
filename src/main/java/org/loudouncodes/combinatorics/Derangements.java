package org.loudouncodes.combinatorics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Derangements — full-length permutations {@code p} of {@code {0,1,...,n-1}} with no fixed points
 * ({@code p[i] != i} for all {@code i}).
 *
 * <p>Order is lexicographic, induced by the underlying permutation order. Returned arrays are
 * defensive copies; iterators obey the standard contract.
 */
public final class Derangements {

  private Derangements() {
    /* no instances */
  }

  /**
   * Entry point for derangements over {@code {0,1,...,n-1}}.
   *
   * @param n size of the permutation (must be {@code >= 0})
   * @return a builder for constructing derangement iterables and counts
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static Builder of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be non-negative");
    return new Builder(n);
  }

  /** Fluent builder for derangement enumeration and counts. */
  public static final class Builder {
    private final int n;

    private Builder(int n) {
      this.n = n;
    }

    /**
     * Enumerate all derangements in lexicographic order.
     *
     * @return an iterable over all derangements of {@code n}
     */
    public All all() {
      return new All(n);
    }

    /**
     * Total number of derangements {@code !n} (subfactorial).
     *
     * <p>Uses the recurrence {@code !0 = 1}, {@code !1 = 0}, {@code !n = (n-1) * (!(n-1) +
     * !(n-2))}. Note: returns a {@code long} and will overflow for sufficiently large {@code n};
     * intended for classroom-scale values.
     *
     * @return {@code !n} (the subfactorial of {@code n})
     */
    public long size() {
      if (n == 0) return 1L;
      if (n == 1) return 0L;
      long d0 = 1L; // !0
      long d1 = 0L; // !1
      long dk = 0L;
      for (int k = 2; k <= n; k++) {
        dk = (k - 1L) * (d1 + d0);
        d0 = d1;
        d1 = dk;
      }
      return dk;
    }
  }

  /** Iterable wrapper for all derangements of a given size. */
  public static final class All implements Iterable<int[]> {
    private final int n;

    private All(int n) {
      this.n = n;
    }

    @Override
    public Iterator<int[]> iterator() {
      return new It(n);
    }

    /**
     * Number of derangements (subfactorial {@code !n}).
     *
     * @return {@code !n}
     */
    public long size() {
      if (n == 0) return 1L;
      if (n == 1) return 0L;
      long d0 = 1L; // !0
      long d1 = 0L; // !1
      long dk = 0L;
      for (int k = 2; k <= n; k++) {
        dk = (k - 1L) * (d1 + d0);
        d0 = d1;
        d1 = dk;
      }
      return dk;
    }
  }

  // ---------------------------------------------------------------------------
  // Filtering iterator: iterate all permutations in lex order and skip those
  // with fixed points. This preserves lex order among derangements and keeps
  // the implementation simple.
  // ---------------------------------------------------------------------------
  private static final class It implements Iterator<int[]> {
    private final Iterator<int[]> base; // permutations iterator over n elements
    private int[] next; // next derangement to return (null if none left)

    It(int n) {
      this.base = Permutations.of(n).take(n).iterator();
      advance();
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public int[] next() {
      if (next == null) throw new NoSuchElementException("Derangements exhausted");
      int[] out = Arrays.copyOf(next, next.length); // defensive copy
      advance();
      return out;
    }

    private void advance() {
      next = null;
      while (base.hasNext()) {
        int[] p = base.next();
        if (isDerangement(p)) {
          next = p;
          return;
        }
      }
    }

    private static boolean isDerangement(int[] p) {
      for (int i = 0; i < p.length; i++) {
        if (p[i] == i) return false;
      }
      return true;
    }
  }

  /**
   * Demo entry point.
   *
   * @param args command-line arguments (unused)
   */
  public static void main(String[] args) {
    int n = 4;
    System.out.println("!" + n + " = " + Derangements.of(n).size());
    for (int[] d : Derangements.of(n).all()) {
      System.out.println(Arrays.toString(d));
    }
  }
}
