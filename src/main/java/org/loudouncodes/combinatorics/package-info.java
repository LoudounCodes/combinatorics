/**
 * Classroom-friendly generators for common combinatorial objects with a fluent API.
 *
 * <p>This package focuses on a small, practical toolkit for puzzles, games, and programming
 * exercises (e.g., pizza toppings, Set cards, small search spaces). All generators:
 * </p>
 *
 * <ul>
 *   <li>return results in a predictable <em>lexicographic order</em>,</li>
 *   <li>are <em>Iterable</em> so they work naturally with Java’s enhanced {@code for} loop, and</li>
 *   <li>return <em>defensive copies</em> from {@code next()} so student code can’t corrupt iterator state.</li>
 * </ul>
 *
 * <h2>Fluent API (at a glance)</h2>
 *
 * <table border="1" cellpadding="4" cellspacing="0">
 *   <caption>Fluent entrypoints and what they generate</caption>
 *   <thead>
 *     <tr>
 *       <th>Concept</th>
 *       <th>Class</th>
 *       <th>Fluent construction</th>
 *       <th>Yields</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>Combinations (no repetition)</td>
 *       <td>{@link org.loudouncodes.combinatorics.Combinations Combinations}</td>
 *       <td>{@code Combinations.of(n).choose(k)}</td>
 *       <td>{@code Iterable<int[]>} of size-{@code k} subsets</td>
 *     </tr>
 *     <tr>
 *       <td>Combinations with repetition</td>
 *       <td>{@link org.loudouncodes.combinatorics.CombinationsWithRepetition CombinationsWithRepetition}</td>
 *       <td>{@code CombinationsWithRepetition.of(n).multichoose(k)}</td>
 *       <td>{@code Iterable<int[]>} of nondecreasing size-{@code k} tuples</td>
 *     </tr>
 *     <tr>
 *       <td>Permutations (ordered k-tuples)</td>
 *       <td>{@link org.loudouncodes.combinatorics.Permutations Permutations}</td>
 *       <td>{@code Permutations.of(n).take(k)}</td>
 *       <td>{@code Iterable<int[]>} of length-{@code k} tuples, no repeats</td>
 *     </tr>
 *     <tr>
 *       <td>Derangements (no fixed points)</td>
 *       <td>{@link org.loudouncodes.combinatorics.Derangements Derangements}</td>
 *       <td>{@code Derangements.of(n).all()}</td>
 *       <td>{@code Iterable<int[]>} of full-length derangements</td>
 *     </tr>
 *     <tr>
 *       <td>Cartesian product (mixed-radix)</td>
 *       <td>{@link org.loudouncodes.combinatorics.CartesianProduct CartesianProduct}</td>
 *       <td>{@code CartesianProduct.of(d0, d1, ..., dm)}</td>
 *       <td>{@code Iterable<int[]>} of length-{@code m} tuples, {@code 0 <= t[i] < di}</td>
 *     </tr>
 *     <tr>
 *       <td>Index → object adapter</td>
 *       <td>{@link org.loudouncodes.combinatorics.IndexingAdapter IndexingAdapter}</td>
 *       <td>{@code new IndexingAdapter<>(Iterable<int[]>, List<E>)}</td>
 *       <td>{@code Iterable<List<E>>} mapped from index tuples</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <h2>Examples</h2>
 *
 * <h3>All 3-topping pizzas from 12 toppings (unordered)</h3>
 * <pre>{@code
 * List<String> toppings = List.of(
 *     "Pepperoni","Sausage","Mushrooms","Onions","Green Peppers","Black Olives",
 *     "Spinach","Bacon","Ham","Pineapple","Tomatoes","Extra Cheese"
 * );
 *
 * // indices only
 * for (int[] c : Combinations.of(toppings.size()).choose(3)) {
 *   System.out.println(Arrays.toString(c));
 * }
 *
 * // map indices → names
 * for (List<String> pizza
 *     : new IndexingAdapter<>(Combinations.of(toppings.size()).choose(3), toppings)) {
 *   System.out.println(pizza);
 * }
 * }</pre>
 *
 * <h3>Ordered 2-tuples from 4 items (no repeats)</h3>
 * <pre>{@code
 * for (int[] p : Permutations.of(4).take(2)) {
 *   System.out.println(Arrays.toString(p)); // [0,1], [0,2], [0,3], [1,0], ...
 * }
 * }</pre>
 *
 * <h3>Derangements of size 4 (no fixed points)</h3>
 * <pre>{@code
 * for (int[] d : Derangements.of(4).all()) {
 *   assert d.length == 4;
 *   for (int i = 0; i < 4; i++) assert d[i] != i;
 * }
 * }</pre>
 *
 * <h3>Cartesian product (rightmost coordinate varies fastest)</h3>
 * <pre>{@code
 * // 2 × 3 grid: [0,0], [0,1], [0,2], [1,0], [1,1], [1,2]
 * for (int[] t : CartesianProduct.of(2, 3)) {
 *   System.out.println(Arrays.toString(t));
 * }
 * }</pre>
 *
 * <h2>Conventions &amp; Guarantees</h2>
 * <ul>
 *   <li><strong>Lexicographic order:</strong> All sequences are emitted in a clear, documented order.</li>
 *   <li><strong>Defensive copies:</strong> Every {@code next()} returns a fresh array.</li>
 *   <li><strong>Edge cases:</strong> {@code k==0} emits one empty tuple; invalid inputs throw
 *       {@link IllegalArgumentException}.</li>
 *   <li><strong>Counts:</strong> Each fluent view provides {@code size()} returning the mathematical count
 *       (e.g., {@code C(n,k)}, {@code P(n,k)}, {@code !n}, product of dims). Values may overflow for large inputs,
 *       but the library targets classroom-scale sizes.</li>
 * </ul>
 *
 * @since 0.2.0
 */
package org.loudouncodes.combinatorics;
