# LoudounCodes Combinatorics Library

[Project doumentation, javadocs, and build reports](https://loudouncodes.github.io/Combinatorics/)


A small, classroom-friendly Java library for **generating combinatorial objects**.  
Built for teaching, puzzles, and games — not for grinding through massive research datasets.

---

## ✨ What does it do?

This package exposes **iterable generators** that play nicely with Java’s enhanced `for` loop.  
Instead of writing nested loops or one-off enumeration code, you can just say “give me all the combinations” and iterate them.

### Core classes (fluent API)

- **Combinations** – `Combinations.of(n).choose(k)`  
  All size-`k` subsets of `{0..n-1}`, unordered, no repeats.
- **CombinationsWithRepetition** – `CombinationsWithRepetition.of(n).multichoose(k)`  
  All size-`k` multisets (nondecreasing tuples), repeats allowed.
- **Permutations** – `Permutations.of(n).take(k)`  
  All ordered `k`-tuples from `{0..n-1}`, no repeats.
- **Derangements** – `Derangements.of(n).all()`  
  Full-length permutations of `{0..n-1}` where no element is in its original position.
- **CartesianProduct** – `CartesianProduct.of(d0, d1, ..., dm)`  
  Tuples from multiple domains; coordinate `i` ranges `0..di-1`.
- **IndexingAdapter<E>** – `new IndexingAdapter<>(Iterable<int[]>, List<E>)`  
  Bridges from `int[]` indices to real objects so you can iterate `List<E>` tuples instead of just indices.

All generators:

- are **Iterable** (perfect for enhanced `for`),
- return results in **lexicographic order**,
- and return **defensive copies** so student code can’t corrupt iterator state.

---

## 📚 Quick examples

### 1) Choose teams (combinations)
```java
import java.util.*;
import org.loudouncodes.combinatorics.*;

List<String> names = List.of("Ada", "Grace", "Edsger", "Barbara");

for (List<String> pair :
    new IndexingAdapter<>(Combinations.of(names.size()).choose(2), names)) {
  System.out.println(pair);
}
// [Ada, Grace], [Ada, Edsger], [Ada, Barbara], [Grace, Edsger], ...

2) Order tasks (permutations)

import java.util.*;
import org.loudouncodes.combinatorics.*;

List<String> tasks = List.of("A", "B", "C");

for (List<String> seq :
    new IndexingAdapter<>(Permutations.of(tasks.size()).take(2), tasks)) {
  System.out.println(seq);
}
// [A, B], [A, C], [B, A], [B, C], [C, A], [C, B]

3) Secret Santa (derangements)

import java.util.*;
import org.loudouncodes.combinatorics.*;

List<String> people = List.of("Ann", "Ben", "Cam", "Dee");

for (int[] p : Derangements.of(people.size()).all()) {
  for (int i = 0; i < people.size(); i++) {
    System.out.printf("%s → %s%n", people.get(i), people.get(p[i]));
  }
  System.out.println("---");
}

4) Build a Set deck (Cartesian product)

import java.util.*;
import org.loudouncodes.combinatorics.*;

String[][] attrs = {
  {"1","2","3"},
  {"red","green","purple"},
  {"oval","squiggle","diamond"},
  {"solid","striped","open"}
};

for (int[] t : CartesianProduct.of(3, 3, 3, 3)) {
  String card = attrs[0][t[0]] + " " + attrs[1][t[1]] + " " +
                attrs[2][t[2]] + " " + attrs[3][t[3]];
  System.out.println(card);
}
// 81 lines like "1 red oval solid", "1 red oval striped", ...

5) “Multichoose” toppings (combinations with repetition)

import java.util.*;
import org.loudouncodes.combinatorics.*;

List<String> toppings = List.of("Pepperoni","Sausage","Mushrooms","Onions");

for (List<String> pick :
    new IndexingAdapter<>(CombinationsWithRepetition.of(toppings.size()).multichoose(2), toppings)) {
  System.out.println(pick);
}
// [Pepperoni, Pepperoni], [Pepperoni, Sausage], [Pepperoni, Mushrooms], ...


⸻

🏫 Why this library?
	•	Teaching – lets students replace messy nested loops with a simple enhanced for.
	•	Puzzles & games – brute-force small search spaces (Set, anagrams, Secret Santa, etc.).
	•	Safe & clear – each generator returns fresh copies of results; you can mutate them without breaking iteration.
	•	Lightweight – a handful of focused classes, all plain Java.

⸻

🔢 Counts at a glance

Each fluent view exposes a size():
	•	Combinations.of(n).choose(k).size() → C(n, k)
	•	CombinationsWithRepetition.of(n).multichoose(k).size() → C(n + k - 1, k)
	•	Permutations.of(n).take(k).size() → P(n, k) = n! / (n-k)!
	•	Derangements.of(n).all().size() → !n (subfactorial)
	•	CartesianProduct.of(d0, d1, ..., dm).size() → ∏ di

These return a long. For classroom-scale numbers this is perfect; large inputs may overflow.

⸻

🧪 Testing
	•	JUnit 5 + AssertJ
Tests cover lexicographic order, edge cases (k=0, k=n, n=0), iterator contracts, counts, and defensive copies.

Run them with:

./gradlew -x javadoc test


⸻

🔧 Build

Build a jar (and sources/javadoc jars):

./gradlew clean build

Artifacts appear in build/libs/.

⸻

🚀 Stretch goals (future expansion)

If this ever grows into a full discrete math teaching library, here’s what could be added:
	•	PowerSet – all subsets of a set (size 2^n).
	•	IntegerPartitions – break an integer into summands.
	•	SetPartitions – divide a set into unlabeled groups (Bell numbers).
	•	Compositions – ordered summands of an integer.
	•	More adapters – ArrayAdapter<E>, mapping helpers, stream adapters.
	•	RandomCombinatorics – uniform sampling (e.g., random k-subset).
	•	CombinatoricsUtils – factorial, choose, permute, subfactorial as static helpers.
	•	BigInteger size() – exact counts for large n.
	•	Visualizations – Ferrers/Young diagrams for partitions.

For now, these remain stretch goals — the core library focuses on classroom-scale tasks that stay tractable and fun.

⸻

🤝 Contributing

We welcome pull requests that keep to the spirit of the library:
	•	Small, clear, well-documented classes.
	•	Doclint-safe Javadoc with examples a high schooler can read.
	•	Unit tests (JUnit 5 + AssertJ) that verify correctness, edge cases, and iterator contracts.
	•	Avoid premature optimization — clarity is king.
	•	New features should include classroom-style examples in Javadoc and, if relevant, updates to this README.
  • See the contributing.md file for more details (it wins if this doc is out of date)

Workflow
	1.	Fork the repo & create a feature branch.
	2.	Add your feature with tests and documentation.
	3.	Open a PR describing your change and its educational value.
	4.	CI must pass before merge.

⸻

📜 License

MIT License – free to use, adapt, and teach with.
(see license.txt for complete details)

