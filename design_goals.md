
# Design Goals for LoudounCodes Combinatorics Library

This library provides a **clean, fluent, classroom-friendly API** for iterating over common combinatorial objects. The goal is to support **recreational math, puzzles, and simple card/game simulations** without overwhelming students with full discrete math formalism.

---

## Core Design Principles

1. **Fluent, expressive API**
   - Entry point always begins with a static `of(n)` builder.
   - Selection verb distinguishes the combinatorial object:
     - `Combinations.of(n).choose(k)`
     - `Permutations.of(n).take(k)`
     - `Derangements.of(n).all()`
     - `CombinationsWithRepetition.of(n).choose(k)`
     - `CartesianProduct.of(a, b, c, ...)`
   - Fluent API reads naturally, aligns with math language, and makes parameter order unambiguous.

2. **Iterable-first**
   - All generators implement `Iterable<int[]>`.
   - Enhanced for-loops are the primary teaching idiom:
     ```java
     for (int[] combo : Combinations.of(12).choose(3)) {
         System.out.println(Arrays.toString(combo));
     }
     ```

3. **Defensive safety**
   - Every `next()` returns a defensive copy.
   - Mutating caller’s array cannot affect iterator state.

4. **Lexicographic order**
   - All generators emit results in a predictable, lexicographic ordering.
   - Ensures reproducibility, simplifies testing, and aids student understanding.

5. **Explicit edge cases**
   - `k=0` → one empty tuple.
   - `k=n` → full-length results (n! permutations, 1 combination, etc.).
   - Illegal arguments (negative sizes, k>n) → `IllegalArgumentException`.

6. **Counts as first-class**
   - Each iterable has a `.size()` method with the mathematical count:
     - Combinations: C(n,k)
     - Permutations: P(n,k)
     - Derangements: !n
     - Cartesian product: product of dimensions
   - Long return type, intended for classroom-scale n.

7. **Index-based design**
   - Generators yield integer indices only.
   - Mapping to real objects is handled by `IndexingAdapter`.

---

## Supporting Class: IndexingAdapter

- Purpose: bridge from `Iterable<int[]>` to `Iterable<List<E>>`.
- API is noun-like, not fluent:
  ```java
  List<String> toppings = List.of("Pepperoni", "Mushrooms", "Onions");
  for (List<String> pizza : new IndexingAdapter<>(Combinations.of(3).choose(2), toppings)) {
      System.out.println(pizza);
  }
````

* Returns **unmodifiable** lists of elements.
* Per-tuple bounds checking ensures safety.

---

## Included Generators

* **Combinations**: choose k out of n without repetition, order irrelevant.
* **CombinationsWithRepetition**: choose k out of n with repetition, nondecreasing order.
* **Permutations**: take k out of n without repetition, order matters.
* **Derangements**: permutations with no fixed points.
* **CartesianProduct**: product of multiple sets/domains.
* **IndexingAdapter**: bridges indices to objects.

---

## Not in Scope (yet)

* PowerSet (2^n growth, impractical beyond tiny n).
* IntegerPartitions / SetPartitions (number theory, super-exponential growth).
* Advanced stream/parallel APIs (possible future).
* Direct support for multiset combinatorics (can be built from CartesianProduct + filtering).

---

## Teaching/Usage Notes

* Enhanced-for loops are the primary idiom.
* Students never touch array internals inside iterators.
* Size methods are a way to tie back to binomial/multinomial formulas.
* Complexity growth can be demonstrated by showing `.size()` at increasing n.

---

## Example Usage

```java
// All 3-combinations of 12 toppings
for (int[] combo : Combinations.of(12).choose(3)) {
    System.out.println(Arrays.toString(combo));
}

// All 2-permutations of {0,1,2,3}
for (int[] p : Permutations.of(4).take(2)) {
    System.out.println(Arrays.toString(p));
}

// Derangements of 4 items
for (int[] d : Derangements.of(4).all()) {
    System.out.println(Arrays.toString(d));
}

// Full Set deck via Cartesian product (3^4 = 81)
for (int[] card : CartesianProduct.of(3, 3, 3, 3)) {
    System.out.println(Arrays.toString(card));
}
```

---

## Stretch Goals

* Stream adapters (`Stream<int[]>`).
* Multiset combinations.
* PowerSet, Partitions (with warnings about growth).
* Visualization tools for Ferrers diagrams, etc.

---

**In summary:**
The library is built to be **predictable, iterable, fluent, and classroom-friendly**, balancing rigor with usability. It gives students a concrete playground for exploring combinatorial search spaces in puzzles and games.

