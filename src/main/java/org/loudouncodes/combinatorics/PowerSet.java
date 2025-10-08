package org.loudouncodes.combinatorics;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * PowerSet — all subsets of {0,1,...,n-1}.
 *
 * <p>Default order is size-then-lexicographic:
 * k = 0, 1, ..., n; for each k, emit all k-combinations in lexicographic order.
 * This matches the library’s conventions (lex order by default) and
 * leverages the existing {@code Combinations.of(n).choose(k)} iterator.
 *
 * <h3>Examples</h3>
 * <pre>{@code
 * for (int[] subset : PowerSet.of(3)) {
 *   // emits:
 *   // []           (k=0)
 *   // [0] [1] [2]  (k=1)
 *   // [0,1] [0,2] [1,2]  (k=2)
 *   // [0,1,2]      (k=3)
 * }
 * }</pre>
 *
 * <h3>Guarantees</h3>
 * <ul>
 *   <li>Immutable spec; lazy iteration</li>
 *   <li>Deterministic order (size-then-lex)</li>
 *   <li>Defensive copies: each returned {@code int[]} is a fresh snapshot</li>
 *   <li>Iterator contract: {@code hasNext()} / {@code next()} consistent; exhaustion throws {@link NoSuchElementException}</li>
 *   <li>Argument validation: {@code n >= 0}, else {@link IllegalArgumentException}</li>
 * </ul>
 */
public final class PowerSet implements Iterable<int[]> {

  private final int n;

  private PowerSet(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("n must be non-negative");
    }
    this.n = n;
  }

  /** Entry point: all subsets of {@code {0,1,...,n-1}}. */
  public static PowerSet of(int n) {
    return new PowerSet(n);
  }

  /** Total number of subsets = 2^n (returned as {@link BigInteger}). */
  public BigInteger count() {
    return BigInteger.ONE.shiftLeft(n); // 2^n
  }

  /** Size parameter for callers that care. */
  public int n() {
    return n;
  }

  @Override
  public Iterator<int[]> iterator() {
    return new It(n);
  }

  // ---------------------------------------------------------------------------
  // Iterator: walks k = 0..n; for each k, iterates Combinations.of(n).choose(k)
  // ---------------------------------------------------------------------------
  private static final class It implements Iterator<int[]> {
    private final int n;
    private int k; // current subset size
    private Iterator<int[]> combIt; // iterator over combinations of size k
    private boolean done;

    It(int n) {
      this.n = n;
      this.k = 0;
      // initialize to k=0 combinations (a single empty subset)
      this.combIt = Combinations.of(n).choose(0).iterator();
      this.done = (n < 0); // unreachable due to validation, but keep state explicit
    }

    @Override
    public boolean hasNext() {
      if (done) return false;

      // If current combinations iterator still has elements, we’re good.
      if (combIt.hasNext()) {
        return true;
      }

      // Advance k until we either find a non-empty combinations iterator or exhaust k > n.
      while (!combIt.hasNext()) {
        k++;
        if (k > n) {
          done = true;
          return false;
        }
        combIt = Combinations.of(n).choose(k).iterator();
      }
      return true;
    }

    @Override
    public int[] next() {
      if (!hasNext()) {
        throw new NoSuchElementException("PowerSet exhausted");
      }
      // Defensive copy (even though Combinations already returns snapshots)
      int[] tuple = combIt.next();
      return Arrays.copyOf(tuple, tuple.length);
    }
  }
}
