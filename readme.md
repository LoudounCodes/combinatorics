# LoudounCodes Combinatorics Library

A small, classroom-friendly Java library for **generating combinatorial objects**.  
Built for teaching, puzzles, and games — not for grinding through massive research datasets.

---

## ✨ What does it do?

This package exposes **iterable generators** that play nicely with Java’s enhanced `for` loop.  
Instead of writing nested loops or one-off enumeration code, you can just say “give me all the combinations” and iterate them.

### Core classes

- **Combinations** – all size-`k` subsets of `{0..n-1}`, unordered, no repeats.
- **CombinationsWithRepetition** – all size-`k` multisets, repeats allowed.
- **Permutations** – all ordered `k`-tuples from `{0..n-1}`, no repeats.
- **Derangements** – permutations of `{0..n-1}` where no element is in its original position.
- **CartesianProduct** – tuples from multiple domains, e.g. `new CartesianProduct(2,3,4)` yields every triple with `0 ≤ a < 2`, `0 ≤ b < 3`, `0 ≤ c < 4`.
- **IndexingAdapter<E>** – bridges from `int[]` indices to real objects, so you can iterate `List<E>` tuples instead of just indices.

---

## 📚 Quick examples

### 1. Choose teams (combinations)
```java
List<String> names = List.of("Ada", "Grace", "Edsger", "Barbara");
for (List<String> pair :
      new IndexingAdapter<>(new Combinations(2, names.size()), names)) {
    System.out.println(pair);
}
// [Ada, Grace], [Ada, Edsger], [Ada, Barbara], ...
````

### 2. Order tasks (permutations)

```java
List<String> tasks = List.of("A","B","C");
for (List<String> seq :
      new IndexingAdapter<>(new Permutations(2, tasks.size()), tasks)) {
    System.out.println(seq);
}
// [A, B], [A, C], [B, A], ...
```

### 3. Secret Santa (derangements)

```java
List<String> people = List.of("Ann","Ben","Cam","Dee");
for (int[] p : new Derangements(people.size())) {
    // p is a permutation array; p[i] != i
    System.out.printf("%s → %s%n", people.get(0), people.get(p[0]));
}
```

### 4. Build the Set deck (Cartesian product)

```java
String[][] attrs = {
  {"1","2","3"},
  {"red","green","purple"},
  {"oval","squiggle","diamond"},
  {"solid","striped","open"}
};

for (int[] t : new CartesianProduct(3,3,3,3)) {
  String card = attrs[0][t[0]] + " " + attrs[1][t[1]] +
                " " + attrs[2][t[2]] + " " + attrs[3][t[3]];
  System.out.println(card);
}
// 81 lines like "1 red oval solid", "1 red oval striped", ...
```

---

## 🏫 Why this library?

* **Teaching** – lets students replace messy nested loops with a simple enhanced `for`.
* **Puzzles & games** – brute-force small search spaces (Set, anagrams, secret Santa, etc).
* **Safe & clear** – each generator returns fresh copies of results; you can mutate them without breaking iteration.
* **Lightweight** – just a handful of classes, each under 200 lines, all plain Java.

---

## 🚀 Stretch goals (future expansion)

If this ever grows into a full **discrete math teaching library**, here’s what could be added:

* **PowerSet** – all subsets of a set (size `2^n`).
* **IntegerPartitions** – break an integer into summands.
* **SetPartitions** – divide a set into unlabeled groups (Bell numbers).
* **Compositions** – ordered summands of an integer.
* **More adapters** – mapping directly to arrays, streams, etc.
* **BigInteger size()** – exact counts for large `n`.
* **Ferrers/Young diagram utilities** – visualizations for partitions.

For now, these remain **stretch goals** — the core library focuses on classroom-scale tasks that stay tractable and fun.

---

## 🤝 Contributing

We welcome pull requests that keep to the spirit of the library:

* **Small, clear, well-documented** classes.
* **Doclint-safe Javadoc** with examples that a high schooler could read.
* **Unit tests (JUnit 5 + AssertJ)** that verify correctness, edge cases, and iterator contracts.
* Avoid premature optimization — clarity is king.
* New features should come with classroom-style examples in Javadoc and, if relevant, updates to this README.

### Workflow

1. Fork the repo & create a feature branch.
2. Add your feature with tests and documentation.
3. Open a PR describing your change and its educational value.
4. CI must pass before merge.

---

## 📜 License

[MIT License](LICENSE) – free to use, adapt, and teach with.

---

