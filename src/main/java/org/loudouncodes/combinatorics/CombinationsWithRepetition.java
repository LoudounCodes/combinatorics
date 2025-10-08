package org.loudouncodes.combinatorics;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Fluent API for generating {@code k}-combinations <em>with repetition</em> from the
 * domain {@code {0,1,...,n-1}}. These are nondecreasing {@code int[]} arrays of length {@code k}.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * for (int[] m : CombinationsWithRepetition.of(5).multichoose(3)) {
 *   // e.g. [0,0,0], [0,0,1], [0,0,2], ..., [2,4,4], [3,4,4], [4,4,4]
 * }
 * }</pre>
 *
 * <h2>Order of generation</h2>
 * <p>Lexicographic over nondecreasing arrays. Start at {@code [0,0,...,0]} and advance by
 * bumping the rightmost position that can increase (i.e., {@code < n-1}) and setting the suffix
 * equal to the new value to keep the tuple nondecreasing.</p>
 *
 * <h2>Counting</h2>
 * <p>Total count is the multiset coefficient:
 * {@code C(n+k-1, k)} (also written as {@code (n multichoose k)}).</p>
 *
 * <h2>Edge cases</h2>
 * <ul>
 *   <li>{@code k == 0}: emits one empty array {@code []}.</li>
 *   <li>{@code n == 0}: emits one empty array iff {@code k == 0}; otherwise emits nothing.</li>
 *   <li>Invalid inputs (negative) throw {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * @since 0.2.0
 */
public final class CombinationsWithRepetition {

  private CombinationsWithRepetition() {}

  /**
   * Creates a builder for multichoose selections from {@code {0..n-1}}.
   *
   * @param n domain size (must be {@code >= 0})
   * @return builder bound to {@code n}
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static Builder of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be >= 0");
    return new Builder(n);
  }

  /** Builder capturing the domain size {@code n}. */
  public static final class Builder {
    private final int n;
    private Builder(int n) { this.n = n; }

    /**
     * Returns an iterable over all size-{@code k} combinations with repetition allowed
     * (nondecreasing {@code int[]} arrays).
     *
     * @param k selection size (must be {@code >= 0})
     * @return iterable view over multiset combinations
     * @throws IllegalArgumentException if {@code k < 0}
     */
    public KMultiChoose multichoose(int k) {
      if (k < 0) throw new IllegalArgumentException("k must be >= 0");
      return new KMultiChoose(k, n);
    }
  }

  /**
   * Iterable view of all {@code k}-combinations with repetition from {@code {0..n-1}}.
   * Emits nondecreasing arrays in lexicographic order and provides {@link #size()}.
   */
  public static final class KMultiChoose implements Iterable<int[]> {
    private final int k, n;

    private KMultiChoose(int k, int n) {
      this.k = k; this.n = n;
    }

    /**
     * Count = {@code C(n+k-1, k)} computed multiplicatively into a {@code long}.
     * For large inputs this may overflow; intended for classroom-scale values.
     */
    public long size() {
      if (k == 0) return 1L;
      if (n == 0) return 0L; // unless k==0, handled above
      // Compute C(n+k-1, k) = Π_{i=1..k} (n-1+i)/i
      long res = 1L;
      for (int i = 1; i <= k; i++) {
        res = (res * (long) (n - 1 + i)) / i;
      }
      return res;
    }

    @Override
    public Iterator<int[]> iterator() {
      return new WithRepIterator(k, n);
    }
  }

  /**
   * Iterator over nondecreasing {@code k}-tuples in lexicographic order.
   * State invariant: {@code 0 <= cur[0] <= cur[1] <= ... <= cur[k-1] < n}.
   */
  private static final class WithRepIterator implements Iterator<int[]> {
    private final int k, n;
    private final int[] cur;
    private boolean hasNext;

    WithRepIterator(int k, int n) {
      this.k = k;
      this.n = n;

      if (k == 0) {
        this.cur = new int[0];
        this.hasNext = true; // one empty tuple
      } else if (n == 0) {
        this.cur = new int[0];
        this.hasNext = false; // no tuples when n==0 and k>0
      } else {
        this.cur = new int[k];
        // Start at [0,0,...,0]
        for (int i = 0; i < k; i++) cur[i] = 0;
        this.hasNext = true;
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();
      int[] out = cur.clone(); // defensive copy
      hasNext = advance();
      return out;
    }

    /** Bump rightmost position that can increase; set suffix equal to that new value. */
    private boolean advance() {
      if (k == 0) return false; // already emitted single empty tuple
      // Find rightmost i such that cur[i] < n-1
      int i = k - 1;
      while (i >= 0 && cur[i] == n - 1) i--;
      if (i < 0) return false; // exhausted (was [n-1,...,n-1])

      cur[i]++;                 // bump this position
      int v = cur[i];
      for (int j = i + 1; j < k; j++) {
        cur[j] = v;             // keep nondecreasing by filling suffix with same value
      }
      return true;
    }
  }

  /** Small demo. */
  public static void main(String[] args) {
    var m = CombinationsWithRepetition.of(3).multichoose(2); // n=3 -> {0,1,2}, k=2
    System.out.println("C(3+2-1,2) = " + m.size()); // 6
    for (int[] a : m) {
      System.out.println(java.util.Arrays.toString(a));
    }
  }
}
