package org.loudouncodes.combinatorics;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generates all <em>derangements</em> of {@code {0,1,...,n-1}} and exposes them via Java's enhanced
 * {@code for} loop by implementing {@link Iterable}{@code <int[]>}. A derangement is a permutation
 * with <strong>no fixed points</strong>, i.e., for every position {@code i}, {@code p[i] != i}.
 *
 * <h2>What is a derangement?</h2>
 *
 * <p>A derangement of size {@code n} is a permutation of {@code 0..n-1} such that no element stays
 * in its original position. For example:
 *
 * <ul>
 *   <li>{@code n = 3}: derangements are {@code [1,2,0]} and {@code [2,0,1]} (but not {@code
 *       [1,0,2]} because {@code p[2] = 2}).
 *   <li>{@code n = 1}: none exist.
 *   <li>{@code n = 0}: by convention there is one (the empty permutation).
 * </ul>
 *
 * <h2>How many are there?</h2>
 *
 * <p>The number of derangements (the <em>subfactorial</em>) is usually written {@code !n}. It
 * satisfies the recurrence:
 *
 * <pre>{@code
 * !0 = 1
 * !1 = 0
 * !n = (n - 1) * ( !(n - 1) + !(n - 2) )
 * }</pre>
 *
 * <p>This value is available via {@link #size()} and is close to {@code n! / e}.
 *
 * <h2>Order of generation</h2>
 *
 * <p>Derangements are produced in <strong>lexicographic order</strong> over the full length-{@code
 * n} arrays. The first emitted array is the lexicographically smallest derangement, and so on.
 *
 * <h2>Algorithm (iterative backtracking; no recursion)</h2>
 *
 * <p>We build the permutation from left to right with a depth pointer {@code depth} (0..n). At each
 * position {@code depth}, we try candidates {@code v} in ascending order, skipping those already
 * used and skipping {@code v == depth} (to avoid fixed points). When we assign a value, we advance
 * to the next depth and reset its candidate cursor; when we run out of candidates, we backtrack by
 * one depth and try the next candidate there. When {@code depth == n}, we have a complete
 * derangement to emit. This produces lexicographic order because we always try the smallest legal
 * value first and only increase positions when necessary.
 *
 * <h3>Why this is correct</h3>
 *
 * <ul>
 *   <li><strong>No fixed points:</strong> We never allow {@code p[i] == i} during construction.
 *   <li><strong>No duplicates:</strong> The search state is deterministic and each leaf is reached
 *       via a unique sequence of choices.
 *   <li><strong>Exhaustive and ordered:</strong> Standard backtracking over candidates tried in
 *       ascending order yields all solutions in lexicographic order.
 * </ul>
 *
 * <h2>Complexity</h2>
 *
 * <ul>
 *   <li><strong>Time:</strong> proportional to the number of derangements; the iterator does not
 *       enumerate non-derangements.
 *   <li><strong>Space:</strong> {@code O(n)} for the current permutation, a {@code used[]} bitmap,
 *       and a small cursor stack.
 * </ul>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>{@code n == 0}: one empty permutation {@code []} is emitted.
 *   <li>{@code n == 1}: no permutations are emitted.
 *   <li>Invalid inputs (negative) throw {@link IllegalArgumentException}.
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Derangements d = new Derangements(4);
 * System.out.println("Total: " + d.size()); // !4 = 9
 * for (int[] p : d) {
 *     System.out.println(java.util.Arrays.toString(p));
 * }
 * }</pre>
 *
 * <p><strong>Implementation note:</strong> Each {@link java.util.Iterator#next() Iterator.next()}
 * returns a defensive copy so callers may mutate their own copy without affecting the iterator's
 * internal state.
 *
 * @since 0.1.0
 */
public class Derangements implements Iterable<int[]> {

  private final int n;

  /**
   * Constructs a generator of derangements of {@code {0..n-1}}.
   *
   * @param n size of the permutation domain (must satisfy {@code n >= 0})
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public Derangements(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("Require n >= 0");
    }
    this.n = n;
  }

  /**
   * Returns the total number of derangements {@code !n} using the standard recurrence: {@code !0 =
   * 1}, {@code !1 = 0}, {@code !n = (n-1)(!(n-1)+!(n-2))}.
   *
   * <p>Note: this uses {@code long} and will overflow for larger {@code n}. For classroom-scale
   * {@code n} (e.g., {@code n <= 20}) it is typically fine. For arbitrary precision, provide a
   * {@code java.math.BigInteger} variant.
   *
   * @return the subfactorial {@code !n} as a {@code long}
   */
  public long size() {
    if (n == 0) return 1L;
    if (n == 1) return 0L;
    long dNm2 = 1L; // !0
    long dNm1 = 0L; // !1
    long dN = 0L;
    for (int i = 2; i <= n; i++) {
      dN = (i - 1L) * (dNm1 + dNm2);
      dNm2 = dNm1;
      dNm1 = dN;
    }
    return dN;
  }

  /**
   * Returns a fresh iterator that enumerates all derangements in lexicographic order.
   *
   * @return iterator over derangements of {@code 0..n-1}
   */
  @Override
  public Iterator<int[]> iterator() {
    return new DerangementIterator(n);
  }

  /**
   * Iterator implementing iterative backtracking to enumerate derangements in lexicographic order.
   *
   * <p>State invariant: positions {@code [0, depth)} are assigned distinct values in {@code
   * [0,n-1]} and for each assigned position {@code i}, {@code cur[i] != i}. The {@code used[v]}
   * bitmap tracks which values are currently in the permutation. The {@code next[v]} cursors store
   * the next candidate value to try at each depth.
   */
  private static final class DerangementIterator implements Iterator<int[]> {
    private final int n;
    private final int[] cur; // current partial assignment (valid up to 'depth')
    private final boolean[] used; // which values are already taken
    private final int[] next; // next candidate value to try per depth
    private int depth; // how many positions [0..depth-1] are currently assigned
    private boolean hasNext; // whether a next solution is prepared
    private int[] prepared; // snapshot of the next full derangement to return

    DerangementIterator(int n) {
      this.n = n;
      this.cur = new int[n];
      this.used = new boolean[n];
      this.next = new int[n + 1]; // one extra for convenience; next[depth] valid
      this.depth = 0;

      if (n == 0) {
        // Single empty derangement.
        this.prepared = new int[0];
        this.hasNext = true;
      } else if (n == 1) {
        // No derangements for n=1.
        this.hasNext = false;
      } else {
        // Initialize candidate cursors to 0 and prepare the first solution.
        for (int i = 0; i <= n; i++) next[i] = 0;
        advance(); // populate 'prepared' with the first solution, if any
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    /**
     * Returns the next derangement as a defensive copy. After returning, advances the internal
     * search state to prepare the following solution, if any.
     *
     * @throws NoSuchElementException if iteration is exhausted
     */
    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();
      int[] out = prepared.clone();
      advance(); // prepare the next solution
      return out;
    }

    /**
     * Continue the DFS from the current search state until we either find the next full derangement
     * or exhaust the search space. Sets {@code hasNext} and {@code prepared} accordingly.
     */
    private void advance() {
      // If we just emitted the n==0 empty case, mark exhausted now.
      if (n == 0) {
        hasNext = false;
        prepared = null;
        return;
      }

      while (true) {
        if (depth == n) {
          // Found a full derangement.
          if (prepared == null) prepared = cur.clone();
          else prepared = cur.clone();
          hasNext = true;

          // Backtrack one step so the next call can continue the search.
          depth--;
          int v = cur[depth];
          used[v] = false;
          next[depth] = v + 1; // try next candidate at this depth next time
          return;
        }

        // Try to assign position 'depth' starting from its next candidate.
        int start = next[depth];
        boolean advanced = false;
        for (int v = start; v < n; v++) {
          if (v == depth) continue; // avoid fixed point
          if (used[v]) continue; // avoid reuse
          // choose v at this depth
          cur[depth] = v;
          used[v] = true;
          depth++;
          next[depth] = 0; // reset next-candidate for the deeper level
          advanced = true;
          break;
        }

        if (advanced) {
          // go deeper
          continue;
        }

        // No candidate worked here -> backtrack.
        if (depth == 0) {
          // Search space exhausted.
          hasNext = false;
          prepared = null;
          return;
        }
        // Unchoose current depth-1 and try the next candidate there on the next loop.
        depth--;
        int v = cur[depth];
        used[v] = false;
        next[depth] = v + 1;
      }
    }
  }

  /**
   * Small demo for quick sanity checks.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    Derangements d = new Derangements(4);
    System.out.println("Total: " + d.size()); // !4 = 9
    for (int[] p : d) {
      System.out.println(java.util.Arrays.toString(p));
    }
  }
}
