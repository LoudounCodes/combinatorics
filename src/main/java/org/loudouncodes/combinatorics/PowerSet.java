package org.loudouncodes.combinatorics;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * PowerSet — all subsets of {0,1,...,n-1}.
 *
 * <p>Default order is size-then-lexicographic: k = 0, 1, ..., n; for each k, emit all
 * k-combinations in lexicographic order.
 *
 * <h2>Examples</h2>
 *
 * <pre>{@code
 * // n = 3 emits (in size-then-lex order):
 * // []            (k=0)
 * // [0] [1] [2]   (k=1)
 * // [0,1] [0,2] [1,2]  (k=2)
 * // [0,1,2]       (k=3)
 * for (int[] subset : PowerSet.of(3)) {
 *   // use subset
 * }
 * }</pre>
 *
 * <h2>Guarantees</h2>
 *
 * <ul>
 *   <li>Immutable spec; lazy iteration.
 *   <li>Deterministic order (size-then-lex).
 *   <li>Defensive copies: each returned {@code int[]} is a fresh snapshot.
 *   <li>Iterator contract: {@code hasNext()} / {@code next()} consistent; exhaustion throws {@link
 *       java.util.NoSuchElementException}.
 *   <li>Argument validation: {@code n >= 0}, else {@link IllegalArgumentException}.
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

  /**
   * Entry point: all subsets of {@code {0,1,...,n-1}}.
   *
   * @param n size of the ground set (must be &ge; 0)
   * @return a {@code PowerSet} iterable over all subsets
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static PowerSet of(int n) {
    return new PowerSet(n);
  }

  /**
   * Total number of subsets.
   *
   * @return {@code 2^n} as a {@link java.math.BigInteger}
   */
  public BigInteger count() {
    return BigInteger.ONE.shiftLeft(n); // 2^n
  }

  /**
   * Convenience alias for {@link #count()} returning a best-effort {@code long}.
   *
   * <p>If {@code 2^n} does not fit in a signed 64-bit value, returns {@link Long#MAX_VALUE}.
   *
   * @return approximate count as a long (saturates at {@code Long.MAX_VALUE} if overflow)
   */
  public long size() {
    BigInteger c = count();
    return c.bitLength() <= 63 ? c.longValue() : Long.MAX_VALUE;
  }

  /**
   * Ground-set size.
   *
   * @return the value of {@code n}
   */
  public int n() {
    return n;
  }

  /**
   * Returns an iterator over all subsets in size-then-lexicographic order.
   *
   * @return iterator yielding defensive copies of each subset
   */
  @Override
  public Iterator<int[]> iterator() {
    return new It(n);
  }

  // ---------------------------------------------------------------------------
  // Iterator: walks k = 0..n; for each k, iterates k-combinations in lex order.
  // Self-contained (no dependency on Combinations).
  // ---------------------------------------------------------------------------
  private static final class It implements Iterator<int[]> {
    private final int n;
    private int k; // current subset size
    private CombIt combIt; // iterator over current k-combinations
    private boolean initialized; // whether first k has been set
    private boolean done;

    It(int n) {
      this.n = n;
      this.k = 0;
      this.initialized = false;
      this.done = false;
      this.combIt = null;
    }

    @Override
    public boolean hasNext() {
      if (done) return false;

      // Initialize first k (k=0) on first call.
      if (!initialized) {
        combIt = new CombIt(n, 0);
        initialized = true;
        return true; // exactly one empty combination for k=0
      }

      // If current k still has combos, good.
      if (combIt != null && combIt.hasNext()) {
        return true;
      }

      // Advance k to the next size that has combinations.
      while (true) {
        k++;
        if (k > n) {
          done = true;
          return false;
        }
        combIt = new CombIt(n, k);
        if (combIt.hasNext()) {
          return true;
        }
      }
    }

    @Override
    public int[] next() {
      if (!hasNext()) {
        throw new NoSuchElementException("PowerSet exhausted");
      }
      // CombIt returns a fresh array; still copy defensively for uniform guarantees.
      int[] tuple = combIt.next();
      return Arrays.copyOf(tuple, tuple.length);
    }
  }

  // ---------------------------------------------------------------------------
  // Lexicographic k-combination iterator over {0..n-1}
  //
  // Emits all size-k subsets in lex order:
  //   start: [0,1,2,...,k-1]
  //   after returning the current tuple, precompute the successor; if none, mark done=true.
  // Contract fixes:
  //   - k == 0: exactly one emission (the empty set), then hasNext() == false.
  //   - Last element is returned; no premature NoSuchElementException.
  // ---------------------------------------------------------------------------
  private static final class CombIt implements Iterator<int[]> {
    private final int n, k;
    private final int[] c; // current combination state
    private boolean firstEmitted;
    private boolean done;

    CombIt(int n, int k) {
      this.n = n;
      this.k = k;
      if (k < 0 || k > n) {
        this.c = null;
        this.done = true;
        this.firstEmitted = true;
      } else {
        this.c = new int[k];
        for (int i = 0; i < k; i++) c[i] = i;
        this.done = false;
        this.firstEmitted = false;
      }
    }

    @Override
    public boolean hasNext() {
      if (done) return false;
      if (k == 0) return !firstEmitted; // only the empty set once
      return true; // for k>0, done is authoritative
    }

    @Override
    public int[] next() {
      if (!hasNext()) throw new NoSuchElementException("Combinations exhausted");

      // Emit the current tuple
      int[] out = Arrays.copyOf(c, k);

      if (!firstEmitted) {
        firstEmitted = true;
        if (k == 0) {
          // empty set was just returned; nothing else to emit
          done = true;
        } else {
          // prepare successor for k>0
          advance();
        }
        return out;
      }

      // k>0 and not the first emission: prepare the next state AFTER returning 'out'
      advance();
      return out;
    }

    // Compute successor into 'c'; if none exists, mark done=true.
    private void advance() {
      if (k == 0) {
        done = true;
        return;
      } // defensive
      int i = k - 1;
      while (i >= 0 && c[i] == n - k + i) i--;
      if (i < 0) {
        done = true; // no successor; we've just returned the last tuple
      } else {
        c[i]++;
        for (int j = i + 1; j < k; j++) c[j] = c[j - 1] + 1;
      }
    }
  }
}
