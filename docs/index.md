# LoudounCodes Combinatorics

*A small, classroom-friendly Java library for combinations, permutations, derangements, power sets, Cartesian products, and more—built for teaching and tinkering.*

## What this library is for

* **Learning by doing.** Generate combinatorial objects and loop over them with simple, readable code.
* **Fluent API that reads like math.**
  `Combinations.of(12).choose(3)` → all 3-subsets of `{0..11}`
  `Permutations.of(5).take(2)` → all ordered pairs from `{0..4}`
  `PowerSet.of(4)` → all subsets of `{0..3}`
* **Safe for classrooms.** Iterators are lazy, deterministic, validate arguments, and return **defensive copies** so student code can’t accidentally mutate iterator state.
* **Built-in counts.** Most views expose `size()`/`sizeExact()` so you can discuss *how many* without enumerating.

---

## Quick tour (fluent API in plain English)

A **fluent API** is a style where you *chain* method calls so code reads like a sentence:

```java
Combinations.of(12).choose(3)        // “choose 3 from 12 (no repetition)”
Combinations.of(6).withRepetition().choose(4) // “choose 4 from 6 with repetition”
Permutations.of(5).take(3)           // “take ordered 3-tuples without repetition”
PowerSet.of(5)                        // “all subsets of a 5-element set”
CartesianProduct.of(2,3,2)           // “mixed-radix tuples with dims 2×3×2”
Derangements.of(5).all()             // “all permutations with no fixed points”
```

Each call returns an **iterable view** of `int[]` index tuples (e.g., `[0,2,5]`). Use `IndexingAdapter` to map those indices onto real objects like strings, cards, toppings, etc.

---

## Example: pizza toppings (combinations → real objects)

```java
import java.util.List;
import org.loudouncodes.combinatorics.Combinations;
import org.loudouncodes.combinatorics.IndexingAdapter;

public class PizzaDemo {
  public static void main(String[] args) {
    List<String> toppings = List.of(
      "Pepperoni","Sausage","Mushrooms","Onions","Green Peppers","Black Olives",
      "Spinach","Bacon","Ham","Pineapple","Tomatoes","Extra Cheese"
    );

    int n = toppings.size();

    // 1) 3-topping pizzas (no repetition)
    var combos = Combinations.of(n).choose(3); // default: without repetition
    var pizzas = new IndexingAdapter<>(combos, toppings);
    System.out.println("Three-topping pizzas (no rep):");
    for (var pizza : pizzas) {
      System.out.println(pizza);
    }
    // If your Combinations view exposes counts:
    // System.out.println("Total: " + combos.size());      // long
    // System.out.println("Exact: " + combos.sizeExact()); // BigInteger

    // 2) 3 scoops from 6 flavors (with repetition — like ice cream)
    var scoops = Combinations.of(6).withRepetition().choose(3);
    System.out.println("\nThree scoops from 6 flavors (with repetition):");
    for (int[] pick : scoops) {
      // pick is nondecreasing indices such as [0,0,2]
      System.out.println(java.util.Arrays.toString(pick));
    }
  }
}
```

**What’s going on?**

* `Combinations.of(n).choose(k)` yields k-element index sets like `[0,4,7]`.
* `IndexingAdapter<>(Iterable<int[]>, List<E>)` turns those indices into real lists (here, lists of topping names).
* With repetition, you’ll see **nondecreasing** arrays (e.g., `[0,0,2]`).

---

## Core building blocks (at a glance)

| Concept                 | Fluent entry                                    | Returns             | Notes                                       |
| ----------------------- | ----------------------------------------------- | ------------------- | ------------------------------------------- |
| Combinations (no rep)   | `Combinations.of(n).choose(k)`                  | `Iterable<int[]>`   | Lexicographic order; `size()` = C(n,k)      |
| Combinations (with rep) | `Combinations.of(n).withRepetition().choose(k)` | `Iterable<int[]>`   | Nondecreasing arrays; `size()` = C(n+k−1,k) |
| Permutations (k-tuples) | `Permutations.of(n).take(k)`                    | `Iterable<int[]>`   | Lexicographic; `size()` = P(n,k)            |
| Derangements            | `Derangements.of(n).all()`                      | `Iterable<int[]>`   | No fixed points; `size()` = subfactorial    |
| Power set               | `PowerSet.of(n)`                                | `Iterable<int[]>`   | Size-then-lex order; `count()` = 2^n        |
| Cartesian product       | `CartesianProduct.of(d0,d1,...)`                | `Iterable<int[]>`   | Rightmost coordinate varies fastest         |
| Index → object          | `new IndexingAdapter<>(tuples, data)`           | `Iterable<List<E>>` | Defensive copies each step                  |
| (Optional) Gray codes   | `BinaryGray.of(n).asBits()`                     | `Iterable<int[]>`   | Minimal-change sequences for demos          |

---

## Design goals

* **Readable & fluent.** Code should look like the math you’re teaching.
* **Deterministic & lazy.** Iterables generate in a documented order without pre-materializing.
* **Safe for students.** All outputs are **fresh arrays** (defensive copies); invalid inputs throw clear `IllegalArgumentException`s; iterators follow the `hasNext()/next()` contract and throw on exhaustion.
* **Count before you iterate.** Many views provide `size()`/`sizeExact()` so you can reason about feasibility first.
* **Classroom-scale performance.** Plenty fast for n you’d reasonably use in labs and exercises.

---

## Reports

* **Tests:** [HTML report](./reports/tests/index.html)
* **Coverage (JaCoCo):** [HTML report](./reports/coverage/index.html)
* **PMD (static analysis):**

  * [Main](./reports/pmd/main.html)
  * [Test](./reports/pmd/test.html)

## API Docs

* [Javadoc](./javadoc/)

---

### Tips for students

* The library works over **indices** (`int[]`) so you can remap to any domain (names, cards, colors) using `IndexingAdapter`.
* When in doubt about how many results you’ll get, call `size()` or `sizeExact()` first.
* If you see an exception, read it—it’s usually an input check doing you a favor (e.g., `k > n` without repetition).
