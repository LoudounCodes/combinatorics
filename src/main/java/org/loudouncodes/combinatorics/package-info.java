/**
 * Combinatorial generators with a fluent, classroom-friendly API.
 *
 * <h2>What’s here</h2>
 *
 * <ul>
 *   <li><b>Combinations</b> (with/without repetition)
 *   <li><b>Permutations</b> (k-permutations of n)
 *   <li><b>Derangements</b> (no fixed points)
 *   <li><b>Cartesian product</b> (mixed radix)
 *   <li><b>Power set</b> (size-then-lex order)
 *   <li><b>IndexingAdapter</b> (map int indices to real objects)
 * </ul>
 *
 * <h2>Fluent map</h2>
 *
 * <pre>{@code
 * // Combinations (no repetition)
 * for (int[] c : Combinations.of(5).choose(2)) {
 *   // [0,1], [0,2], ...
 * }
 *
 * // Combinations with repetition (multiset combinations)
 * for (int[] c : Combinations.of(3).withRepetition().choose(2)) {
 *   // [0,0], [0,1], ...
 * }
 *
 * // Permutations: ordered tuples without repetition
 * for (int[] p : Permutations.of(5).take(3)) {
 *   // [0,1,2], [0,1,3], ...
 * }
 *
 * // Derangements: full-length permutations with no fixed points
 * for (int[] d : Derangements.of(4).all()) {
 *   // [1,0,3,2], [1,2,3,0], ...
 * }
 *
 * // Cartesian product (rightmost coordinate varies fastest)
 * for (int[] t : CartesianProduct.of(2, 3)) {
 *   // [0,0], [0,1], [0,2], [1,0], ...
 * }
 *
 * // Power set (size-then-lex)
 * for (int[] s : PowerSet.of(3)) {
 *   // [], [0], [1], [2], [0,1], [0,2], [1,2], [0,1,2]
 * }
 * }</pre>
 *
 * <h2>Counting helpers</h2>
 *
 * <ul>
 *   <li>{@code Combinations.of(n).choose(k).size()} → C(n,k)
 *   <li>{@code Combinations.of(n).withRepetition().choose(k).size()} → C(n+k-1,k)
 *   <li>{@code Permutations.of(n).take(k).size()} → P(n,k) = n!/(n-k)!
 *   <li>{@code Derangements.of(n).all().size()} → !n (subfactorial)
 *   <li>{@code CartesianProduct.of(dims...).size()} → ∏ dims[i]; {@code sizeExact()} available
 *   <li>{@code PowerSet.of(n).count()} → 2^n (BigInteger); {@code size()} alias provided
 * </ul>
 *
 * <h2>Design guarantees</h2>
 *
 * <ul>
 *   <li>Lazy iteration; no pre-materialization.
 *   <li>Deterministic order (documented per class).
 *   <li>Defensive copies on outputs.
 *   <li>Strict iterator contract; exhaustion throws {@link java.util.NoSuchElementException}.
 *   <li>Fail-fast validation with {@link IllegalArgumentException}.
 * </ul>
 *
 * @since 0.2.0
 */
package org.loudouncodes.combinatorics;
