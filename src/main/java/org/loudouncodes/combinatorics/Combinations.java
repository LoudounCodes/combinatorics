package org.loudouncodes.combinatorics;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Utilities for enumerating <em>k</em>-subsets (combinations) of the ground set {@code
 * {0,1,...,n-1}}.
 *
 * <h2>Overview</h2>
 *
 * <p>This class exposes a fluent builder for two common models:
 *
 * <ul>
 *   <li><strong>Without repetition (default):</strong> Each element may appear at most once in a
 *       combination. Enumeration order is <em>lexicographic</em>.
 *   <li><strong>With repetition (multiset combinations):</strong> Elements may repeat. We represent
 *       each multiset as a nondecreasing {@code int[]} of length {@code k}. Enumeration order is
 *       <em>lexicographic</em> over those arrays.
 * </ul>
 *
 * <p>Additionally, for the non-repetition model you may request <strong>Gray order</strong> (also
 * known as <em>revolving-door</em> order) via {@link Combinations.Spec#choose(int) choose(k)} →
 * {@link KChoose#inGrayOrder()}. In Gray order, successive combinations differ by exchanging
 * exactly one element (symmetric difference size 2), which is useful for certain incremental
 * algorithms.
 *
 * <h2>Fluent usage</h2>
 *
 * <pre>{@code
 * // No repetition (default), lexicographic:
 * for (int[] a : Combinations.of(5).choose(3)) {
 *   // ...
 * }
 *
 * // With repetition (multiset combinations), lexicographic:
 * for (int[] a : Combinations.of(3).withRepetition().choose(2)) {
 *   // yields [0,0],[0,1],[0,2],[1,1],[1,2],[2,2]
 * }
 *
 * // No repetition, Gray (revolving-door) order:
 * for (int[] a : Combinations.of(5).choose(3).inGrayOrder()) {
 *   // consecutive tuples differ by swapping exactly one element
 * }
 * }</pre>
 *
 * <h2>Contracts &amp; guarantees</h2>
 *
 * <ul>
 *   <li>All iterables return <strong>fresh defensive copies</strong> on each {@code next()}.
 *   <li>Iterators obey the standard contract: after exhaustion, {@code hasNext()} is false and
 *       {@code next()} throws {@link NoSuchElementException}.
 *   <li>Time per emitted combination is {@code O(k)}; memory is {@code O(k)}.
 *   <li>Counting methods are available via {@link KChoose#size()} (best-effort {@code long}) and
 *       {@link KChoose#sizeExact()} (exact {@link BigInteger}).
 * </ul>
 *
 * <h2>Pedagogical notes</h2>
 *
 * <ul>
 *   <li><em>Lexicographic order</em> for combinations without repetition starts at {@code
 *       [0,1,2,...,k-1]} and advances by the standard “rightmost bump” rule.
 *   <li><em>Lexicographic order</em> for combinations with repetition starts at {@code [0,0,...,0]}
 *       and advances by increasing the rightmost entry that can still grow.
 *   <li><em>Gray (revolving-door) order</em> is produced lazily by a stack-based traversal of the
 *       classic recursive decomposition: {@code G(n,k) = G(n-1,k) followed by reverse(G(n-1,k-1))
 *       with (n-1) added}.
 * </ul>
 */
public final class Combinations {

  private Combinations() {
    /* no instances */
  }

  /**
   * Creates a specification for combinations over the ground set {@code {0,1,...,n-1}}.
   *
   * @param n ground-set size (must be {@code >= 0})
   * @return an immutable {@link Spec} for configuring enumeration
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static Spec of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be non-negative");
    return new Spec(n, false); // default: without repetition
  }

  // ---------------------------------------------------------------------------
  // Fluent specification
  // ---------------------------------------------------------------------------

  /**
   * Immutable, fluent specification for building combination iterables.
   *
   * <p>Instances are cheap value objects. Methods never mutate, they return new {@code Spec}s.
   */
  public static final class Spec {
    private final int n;
    private final boolean repetition;

    private Spec(int n, boolean repetition) {
      this.n = n;
      this.repetition = repetition;
    }

    /**
     * Enables combinations <em>with</em> repetition (multiset combinations).
     *
     * @return a new {@code Spec} with repetition enabled
     */
    public Spec withRepetition() {
      return new Spec(n, true);
    }

    /**
     * Explicitly disables repetition (regular combinations).
     *
     * @return a new {@code Spec} with repetition disabled
     */
    public Spec withoutRepetition() {
      return new Spec(n, false);
    }

    /**
     * Finalizes the specification by choosing {@code k} elements.
     *
     * <p>When repetition is disabled (default), this yields ordinary combinations and requires
     * {@code 0 <= k <= n}. When repetition is enabled via {@link #withRepetition()}, this yields
     * multiset combinations (nondecreasing {@code k}-tuples), requiring only {@code k >= 0}.
     *
     * @param k subset size (must be {@code >= 0}; if repetition is disabled, also {@code k <= n})
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

  // ---------------------------------------------------------------------------
  // Iterable view (result of choose(k))
  // ---------------------------------------------------------------------------

  /**
   * Sized iterable over {@code k}-combinations (with or without repetition).
   *
   * <p>The default iterator returned by {@link #iterator()} enumerates in lexicographic order. For
   * the non-repetition model, a Gray-order iterable is available via {@link #inGrayOrder()}.
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
     * Returns the number of combinations in this view as a {@code long}.
     *
     * <p>No repetition: {@code C(n,k)}. With repetition: {@code C(n+k-1, k)}. This may overflow for
     * large inputs; use {@link #sizeExact()} if you need certainty.
     *
     * @return count as best-effort {@code long}
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
     * Returns the exact number of combinations as a {@link BigInteger}.
     *
     * <p>No repetition: {@code C(n,k)}. With repetition: {@code C(n+k-1, k)}.
     *
     * @return exact count
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

    /**
     * Returns an {@link Iterable} that enumerates the combinations in <strong>Gray
     * (revolving-door)</strong> order, where each successive combination differs from the previous
     * by swapping exactly one element (symmetric difference size 2).
     *
     * <p><strong>Model restriction:</strong> Gray order is defined only for combinations
     * <em>without</em> repetition; attempting to call this after {@code withRepetition()} will
     * throw. This iterable is independent of the default lexicographic iterator returned by {@link
     * #iterator()}.
     *
     * <p><strong>First/last tuples:</strong> The sequence begins with the lexicographically first
     * combination {@code [0,1,2,...,k-1]} (when {@code k>0}) and ends at {@code [n-k, ..., n-1]}.
     * For {@code k==0} there is a single empty combination.
     *
     * @return iterable over {@code int[]} in Gray order
     * @throws UnsupportedOperationException if repetition is enabled
     */
    public Iterable<int[]> inGrayOrder() {
      if (repetition) {
        throw new UnsupportedOperationException(
            "Gray order is only defined for combinations without repetition");
      }
      return () -> new GrayNoRepIt(n, k);
    }

    /**
     * Returns an iterator over combinations in <em>lexicographic</em> order.
     *
     * <p>Use {@link #inGrayOrder()} for revolving-door order instead (no repetition only).
     */
    @Override
    public Iterator<int[]> iterator() {
      return repetition ? new WithRepIt(n, k) : new NoRepIt(n, k);
    }
  }

  // ---------------------------------------------------------------------------
  // Iterators: without repetition, lexicographic order
  // ---------------------------------------------------------------------------

  /**
   * Lexicographic iterator for combinations without repetition.
   *
   * <p>State {@code c} holds the next combination. The successor rule is the standard “rightmost
   * bump”: find the rightmost index {@code i} such that {@code c[i] < n - k + i}, increment it,
   * then reset the tail to increasing values.
   */
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
  // Iterators: with repetition (multiset combinations), lexicographic order
  // ---------------------------------------------------------------------------

  /**
   * Lexicographic iterator for multiset combinations (with repetition).
   *
   * <p>We enumerate nondecreasing {@code k}-tuples {@code a} whose entries lie in {@code [0,n-1]}.
   * The successor rule increments the rightmost position that can still grow and copies its new
   * value to the tail, preserving nondecreasing order.
   */
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
  // Iterators: without repetition, Gray (revolving-door) order
  // ---------------------------------------------------------------------------

  /**
   * Gray-order iterator for combinations without repetition.
   *
   * <p>This is a lazy, stack-based traversal of the standard recursive decomposition: {@code G(n,k)
   * = G(n-1,k)} followed by {@code reverse(G(n-1,k-1))} with {@code (n-1)} included. Reversal is
   * achieved by toggling a boolean flag rather than materializing anything.
   *
   * <h3>Emitted tuples</h3>
   *
   * <ul>
   *   <li>If {@code k == 0}, a single empty tuple is emitted.
   *   <li>If {@code k == n}, the unique tuple {@code [0,1,...,n-1]} is emitted.
   *   <li>Otherwise, internal nodes descend according to the {@code rev} flag so that two
   *       consecutive leaves always differ by exactly one exchanged element.
   * </ul>
   *
   * @implNote We keep a stack of frames representing subproblems and a parallel “suffix” stack of
   *     the high elements that have been fixed (…, {@code n-2}, {@code n-1}). Leaves emit either
   *     {@code reverse(suffix)} (for {@code k==0}) or {@code [0..n-1] + reverse(suffix)} (for
   *     {@code k==n}).
   */
  private static final class GrayNoRepIt implements Iterator<int[]> {

    /** Stack frame for a subproblem {@code (n,k)} and its traversal state. */
    private static final class Frame {
      final int n, k;
      final boolean rev;
      int state; // 0 = first branch not yet taken; 1 = second pending; 2 = done
      boolean awaitingPop; // true iff we pushed (n-1) before descending to the last child

      Frame(int n, int k, boolean rev) {
        this.n = n;
        this.k = k;
        this.rev = rev;
        this.state = 0;
        this.awaitingPop = false;
      }
    }

    private final Deque<Frame> stack = new ArrayDeque<>();
    private final Deque<Integer> suffix = new ArrayDeque<>();
    private int[] next; // null == exhausted

    GrayNoRepIt(int n, int k) {
      if (k == 0) {
        // Single empty combination
        next = new int[0];
      } else if (k > n) {
        next = null;
      } else {
        stack.push(new Frame(n, k, false));
        computeNext();
      }
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public int[] next() {
      if (next == null) throw new NoSuchElementException("Combinations (Gray) exhausted");
      int[] out = next;
      computeNext();
      return out;
    }

    /** Advances {@link #next} to the next Gray-order combination or null if exhausted. */
    private void computeNext() {
      next = null;
      while (true) {
        if (stack.isEmpty()) {
          return; // exhausted
        }
        Frame f = stack.peek();

        // Leaf: k == 0  --> emit reverse(suffix)
        if (f.k == 0) {
          int len = suffix.size();
          int[] out = new int[len];
          int idx = 0;
          for (Iterator<Integer> it = suffix.descendingIterator(); it.hasNext(); ) {
            out[idx++] = it.next();
          }
          next = out;
          stack.pop(); // consume leaf
          onChildFinished();
          return;
        }

        // Leaf: k == n  --> emit [0..n-1] + reverse(suffix)
        if (f.k == f.n) {
          int lenLow = f.n;
          int lenSuf = suffix.size();
          int[] out = new int[lenLow + lenSuf];
          for (int i = 0; i < lenLow; i++) out[i] = i;
          int idx = lenLow;
          for (Iterator<Integer> it = suffix.descendingIterator(); it.hasNext(); ) {
            out[idx++] = it.next();
          }
          next = out;
          stack.pop(); // consume leaf
          onChildFinished();
          return;
        }

        // Internal node: descend according to rev and state
        if (!f.rev) {
          if (f.state == 0) {
            f.state = 1;
            stack.push(new Frame(f.n - 1, f.k, false)); // G(n-1,k)
            continue;
          } else if (f.state == 1) {
            f.state = 2;
            suffix.addLast(f.n - 1); // push high element
            f.awaitingPop = true;
            stack.push(new Frame(f.n - 1, f.k - 1, true)); // reverse G(n-1,k-1)
            continue;
          } else {
            stack.pop(); // done
            continue;
          }
        } else {
          if (f.state == 0) {
            f.state = 1;
            suffix.addLast(f.n - 1); // push high element first in reverse mode
            f.awaitingPop = true;
            stack.push(new Frame(f.n - 1, f.k - 1, false)); // forward G(n-1,k-1)
            continue;
          } else if (f.state == 1) {
            f.state = 2;
            stack.push(new Frame(f.n - 1, f.k, true)); // then G(n-1,k)
            continue;
          } else {
            stack.pop(); // done
            continue;
          }
        }
      }
    }

    /**
     * Cleans up after returning from a child: pop any high element that was pushed to enter that
     * child, and unwind fully completed frames.
     */
    private void onChildFinished() {
      while (!stack.isEmpty()) {
        Frame parent = stack.peek();
        if (parent.awaitingPop) {
          suffix.removeLast();
          parent.awaitingPop = false;
        }
        if (parent.state >= 2) {
          stack.pop(); // fully done, continue unwinding
          continue;
        }
        break; // parent has more to generate; leave it on stack
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Binomial helpers (long & BigInteger) with fraction reduction
  // ---------------------------------------------------------------------------

  /**
   * Computes {@code C(n,k)} as a {@code long}, reducing at each step to mitigate overflow.
   *
   * <p>Returns {@code 0} if {@code k<0} or {@code k>n}. May still overflow for very large results.
   */
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

      res *= num; // may overflow for huge values; acceptable for typical classroom parameters
      // by construction, den should now be 1
    }
    return res;
  }

  /**
   * Computes {@code C(n,k)} exactly as a {@link BigInteger}.
   *
   * <p>Returns {@link BigInteger#ZERO} if {@code k<0} or {@code k>n}.
   */
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

  /**
   * Greatest common divisor via Euclid's algorithm on nonnegative {@code long}s.
   *
   * @param a first value
   * @param b second value
   * @return {@code gcd(|a|,|b|)}
   */
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
