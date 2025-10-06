/**
 * A tiny, classroom-friendly library for generating common combinatorial objects you can iterate
 * with Java's enhanced {@code for}-loop. The goal is <em>practical puzzle power</em>: small,
 * predictable tools that make it easy to brute-force or explore toy problems (card games like
 * <em>Set</em>, recreational math, small search spaces), without dragging in a full discrete math
 * course.
 *
 * <h2>What lives in this package?</h2>
 *
 * <ul>
 *   <li><strong>{@link org.loudouncodes.combinatorics.Combinations Combinations}</strong> — all
 *       size-{@code k} subsets of {@code {0..n-1}} in lexicographic order. Unordered, no
 *       repetition. Iterable of {@code int[]}.
 *   <li><strong>{@link org.loudouncodes.combinatorics.Permutations Permutations}</strong> — all
 *       ordered {@code k}-tuples from {@code {0..n-1}} without repetition (so {@code nPk} total).
 *       Iterable of {@code int[]}.
 *   <li><strong>{@link org.loudouncodes.combinatorics.Derangements Derangements}</strong> —
 *       permutations of {@code 0..n-1} with no fixed points (for every {@code i}, {@code p[i] !=
 *       i}). Iterable of {@code int[]}.
 *   <li><strong>{@link org.loudouncodes.combinatorics.CombinationsWithRepetition
 *       CombinationsWithRepetition}</strong> — size-{@code k} multisets from {@code {0..n-1}}
 *       (repeats allowed), emitted as non-decreasing arrays. Iterable of {@code int[]}.
 *   <li><strong>{@link org.loudouncodes.combinatorics.CartesianProduct CartesianProduct}</strong> —
 *       all tuples from multiple domains (think nested loops flattened). Pass sizes like {@code
 *       (2,3,4)} and get every index triple where {@code 0 <= a < 2}, {@code 0 <= b < 3}, {@code 0
 *       <= c < 4}. Iterable of {@code int[]}.
 *   <li><strong>{@link org.loudouncodes.combinatorics.IndexingAdapter
 *       IndexingAdapter}&lt;E&gt;</strong> — bridges from <em>indices</em> to <em>elements</em>.
 *       Wrap any {@code Iterable<int[]>} together with a {@code List<E>} and iterate {@code
 *       List<E>} tuples (each result is an unmodifiable list).
 * </ul>
 *
 * <p>Each generator returns <em>defensive copies</em> on {@code next()}, so students can safely
 * mutate what they get without breaking the iterator.
 *
 * <h2>Why indices?</h2>
 *
 * <p>Every generator in this package works over the integer domain {@code {0..n-1}}. This keeps the
 * core algorithms tiny and fast. When you want real objects (cards, strings, students), use {@link
 * org.loudouncodes.combinatorics.IndexingAdapter} to map index tuples to element tuples.
 *
 * <h2>Quick examples</h2>
 *
 * <h3>1) Choose teams: combinations (unordered, no repeats)</h3>
 *
 * <pre>{@code
 * import java.util.*;
 * import org.loudouncodes.combinatorics.*;
 *
 * List<String> names = List.of("Ada", "Grace", "Edsger", "Barbara");
 * for (List<String> pair :
 *       new IndexingAdapter<>(new Combinations(2, names.size()), names)) {
 *     System.out.println(pair);
 * }
 * // [Ada, Grace], [Ada, Edsger], [Ada, Barbara], [Grace, Edsger], [Grace, Barbara], [Edsger, Barbara]
 * }</pre>
 *
 * <h3>2) Order tasks: permutations (ordered, no repeats)</h3>
 *
 * <pre>{@code
 * List<String> tasks = List.of("A", "B", "C");
 * for (List<String> seq :
 *       new IndexingAdapter<>(new Permutations(2, tasks.size()), tasks)) {
 *     System.out.println(seq);
 * }
 * // [A, B], [A, C], [B, A], [B, C], [C, A], [C, B]
 * }</pre>
 *
 * <h3>3) Secret-Santa style: derangements (no fixed points)</h3>
 *
 * <pre>{@code
 * List<String> people = List.of("Ann", "Ben", "Cam", "Dee");
 * for (int[] p : new Derangements(people.size())) {
 *     boolean ok = true;
 *     for (int i = 0; i < p.length; i++) if (p[i] == i) ok = false;
 *     if (ok) {
 *         for (int i = 0; i < p.length; i++) {
 *             System.out.printf("%s → %s  ", people.get(i), people.get(p[i]));
 *         }
 *         System.out.println();
 *     }
 * }
 * }</pre>
 *
 * <h3>4) Build the Set deck: Cartesian product + mapping</h3>
 *
 * <p>The game <em>Set</em> has 4 attributes with 3 values each. The deck is the 4-way product
 * {@code 3^4 = 81}:
 *
 * <pre>{@code
 * // Attribute domains (encoded 0..2 each): number, color, shape, fill
 * String[][] attrs = {
 *   {"1","2","3"},      // number
 *   {"red","green","purple"}, // color
 *   {"oval","squiggle","diamond"}, // shape
 *   {"solid","striped","open"}     // fill
 * };
 *
 * // Build a human-readable card list by mapping index tuples to strings
 * for (int[] t : new CartesianProduct(3,3,3,3)) {
 *   String card = attrs[0][t[0]] + " " + attrs[1][t[1]] + " " + attrs[2][t[2]] + " " + attrs[3][t[3]];
 *   System.out.println(card);
 * }
 * // 81 lines like: "1 red oval solid", "1 red oval striped", ...
 * }</pre>
 *
 * <h3>5) Triples from a 12-card layout: combinations again</h3>
 *
 * <pre>{@code
 * // Suppose 'layout' is a List<Card> of size 12
 * // Iterate all 3-card subsets; plug in your "isSet" predicate inside the loop.
 * for (int[] c : new Combinations(3, 12)) {
 *   // c[0], c[1], c[2] are indices into layout
 *   // if (isSet(layout.get(c[0]), layout.get(c[1]), layout.get(c[2]))) { ... }
 * }
 * }</pre>
 *
 * <h2>Performance notes</h2>
 *
 * <ul>
 *   <li>Each generator is lazy: nothing is precomputed; the next tuple is constructed on demand.
 *   <li>{@code size()} methods return counts in {@code long}; they overflow for large inputs by
 *       design (this is a classroom library; keep problem sizes modest).
 *   <li>Returned arrays (and lists from {@code IndexingAdapter}) are new objects per call so
 *       students can mutate them without affecting iteration.
 * </ul>
 *
 * <h2>What we intentionally did <em>not</em> include (yet)</h2>
 *
 * <p>General power sets, set partitions, and integer partitions blow up very quickly and are more
 * about discrete math than puzzle solving. If you ever need them, they can be added later as
 * separate iterables with the same style.
 *
 * <h2>Tip for teachers</h2>
 *
 * <p>A fun exercise is to have students replace nested loops with a single enhanced {@code for}
 * over one of these generators. It makes search code shorter and emphasizes the shape of the search
 * space (subsets vs. ordered tuples vs. products).
 *
 * @since 0.1.0
 */
package org.loudouncodes.combinatorics;
