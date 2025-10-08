package org.loudouncodes.combinatorics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Fluent API for generating full-length derangements (permutations with no fixed points)
 * of the domain {@code {0,1,...,n-1}} in lexicographic order.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * for (int[] d : Derangements.of(5).all()) {
 *   // d[i] != i for all i
 * }
 * }</pre>
 *
 * <h2>Definition</h2>
 * A derangement of size {@code n} is a permutation {@code p} of {@code {0..n-1}}
 * such that {@code p[i] != i} for every position {@code i}.
 *
 * <h2>Counting</h2>
 * The number of derangements (subfactorial) is:
 * <pre>{@code
 * D(0)=1, D(1)=0, and D(n)=(n-1)*(D(n-1)+D(n-2))
 * }</pre>
 * Often approximated by {@code n!/e}, but this class computes {@code D(n)} exactly using the
 * recurrence into a {@code long} for classroom-scale values.
 *
 * <h2>Order of generation</h2>
 * Lexicographic over full-length arrays by iterating all permutations in lexicographic order
 * and skipping any that contains a fixed point. This guarantees correctness and uniqueness and
 * is perfectly fine for teaching-scale {@code n}.
 *
 * <h2>Edge cases</h2>
 * <ul>
 *   <li>{@code n == 0}: one empty array {@code []}.</li>
 *   <li>{@code n == 1}: none.</li>
 *   <li>{@code n < 0}: throws {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * <p><strong>Implementation note:</strong> Each {@link Iterator#next()} returns a defensive copy of
 * the internal array.</p>
 */
public final class Derangements {

  private Derangements() {}

  /** Creates a builder for derangements on {@code {0..n-1}}. */
  public static Builder of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be >= 0");
    return new Builder(n);
  }

  /** Builder capturing the domain size {@code n}. */
  public static final class Builder {
    private final int n;
    private Builder(int n) { this.n = n; }

    /** Returns an iterable over all derangements of size {@code n} in lexicographic order. */
    public All all() {
      return new All(n);
    }
  }

  /**
   * Iterable view of all derangements on {@code {0..n-1}}.
   * Provides {@link #size()} (subfactorial !n) and supports enhanced-for.
   */
  public static final class All implements Iterable<int[]> {
    private final int n;

    private All(int n) {
      this.n = n;
    }

    /**
     * Number of derangements (!n) via the recurrence:
     * D(0)=1, D(1)=0, D(n)=(n-1)*(D(n-1)+D(n-2)).
     * <p>Returns a {@code long}; intended for classroom-scale inputs.</p>
     */
    public long size() {
      if (n == 0) return 1L;
      if (n == 1) return 0L;
      long d0 = 1L;  // D(0)
      long d1 = 0L;  // D(1)
      long d = 0L;
      for (int k = 2; k <= n; k++) {
        d = (k - 1L) * (d1 + d0);
        d0 = d1;
        d1 = d;
      }
      return d;
    }

    @Override
    public Iterator<int[]> iterator() {
      return new DerangementIterator(n);
    }
  }

  /**
   * Iterator that enumerates derangements in lexicographic order by scanning permutations
   * and skipping those with fixed points. Simple, correct, and fast enough for classroom sizes.
   */
  private static final class DerangementIterator implements Iterator<int[]> {
    private final int n;
    private final int[] cur;   // current permutation
    private boolean hasNext;

    DerangementIterator(int n) {
      this.n = n;

      if (n == 0) {
        // Single empty derangement
        this.cur = new int[0];
        this.hasNext = true; // will emit [] then stop
        return;
      }
      if (n == 1) {
        // No derangements
        this.cur = new int[0];
        this.hasNext = false;
        return;
      }

      // Start from the first permutation and advance until we hit the first derangement.
      this.cur = new int[n];
      for (int i = 0; i < n; i++) cur[i] = i; // [0,1,2,...,n-1]
      this.hasNext = advanceToNextDerangement(true);
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();

      if (n == 0) {
        // Emit [] once
        hasNext = false;
        return new int[0];
      }

      int[] out = cur.clone();       // defensive copy for caller
      hasNext = advanceToNextDerangement(false);
      return out;
    }

    /** Move to the next derangement; if includeCurrent is true, also consider cur as a candidate. */
    private boolean advanceToNextDerangement(boolean includeCurrent) {
      if (includeCurrent && isDerangement(cur)) {
        return true;
      }
      while (nextPermutation(cur)) {
        if (isDerangement(cur)) return true;
      }
      return false;
    }

    /** Check no fixed points. */
    private boolean isDerangement(int[] p) {
      for (int i = 0; i < n; i++) {
        if (p[i] == i) return false;
      }
      return true;
    }

    /** Standard in-place next permutation (lexicographic). Returns false if already at last. */
    private boolean nextPermutation(int[] a) {
      int i = a.length - 2;
      while (i >= 0 && a[i] >= a[i + 1]) i--;
      if (i < 0) return false;
      int j = a.length - 1;
      while (a[j] <= a[i]) j--;
      swap(a, i, j);
      reverse(a, i + 1, a.length - 1);
      return true;
    }

    private void swap(int[] a, int i, int j) {
      int t = a[i]; a[i] = a[j]; a[j] = t;
    }

    private void reverse(int[] a, int lo, int hi) {
      while (lo < hi) {
        swap(a, lo++, hi--);
      }
    }
  }

  /** Simple demo. */
  public static void main(String[] args) {
    Derangements.All all = Derangements.of(4).all();
    System.out.println("!4 = " + all.size());
    for (int[] d : all) {
      System.out.println(Arrays.toString(d));
    }
  }
}
