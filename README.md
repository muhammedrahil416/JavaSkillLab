# Smart Meal & Recipe Suggestion Engine

A deterministic, rule-based Java CLI application that recommends recipes
based on ingredients you have at home, expiry urgency, and personal exclusions.

---

## Project Structure

```
SmartMealSuggester/
├── src/
│   ├── main/
│   │   ├── java/mealsuggester/
│   │   │   ├── Main.java                     ← CLI entry point
│   │   │   ├── model/
│   │   │   │   ├── Recipe.java               ← Recipe data model
│   │   │   │   ├── ExpiryPriority.java       ← HIGH / MEDIUM / LOW enum
│   │   │   │   └── RecipeSuggestion.java     ← Result of scoring a recipe
│   │   │   ├── data/
│   │   │   │   └── RecipeLoader.java         ← Reads and parses recipes.csv
│   │   │   ├── service/
│   │   │   │   ├── RecipeMatcher.java        ← Scores a single recipe
│   │   │   │   ├── RecipeRankingEngine.java  ← Ranks all recipes
│   │   │   │   └── ExplanationGenerator.java ← Builds "Why suggested" text
│   │   │   └── util/
│   │   │       └── IngredientNormalizer.java ← Normalizes ingredient strings
│   │   └── resources/
│   │       └── recipes.csv                   ← 28-recipe dataset
│   └── README.md
└── .gitignore
```

---

## How to Compile and Run

### 1. Compile

From the `SmartMealSuggester/` directory:

```bash
# Windows (PowerShell)
javac -d out -sourcepath src/main/java (Get-ChildItem -Recurse -Filter *.java src/main/java | % { $_.FullName })

# Or using the provided compile script
```

Or manually:

```bash
mkdir out
javac -d out src/main/java/mealsuggester/model/*.java src/main/java/mealsuggester/util/*.java src/main/java/mealsuggester/data/*.java src/main/java/mealsuggester/service/*.java src/main/java/mealsuggester/Main.java
```

### 2. Copy the CSV resource

```bash
# Windows PowerShell
Copy-Item src/main/resources/recipes.csv out/
```

### 3. Run

```bash
java -cp out mealsuggester.Main
```

---

## Scoring Formula

| Component          | Formula                                        | Weight |
|--------------------|------------------------------------------------|--------|
| Overlap Ratio      | matched / total recipe ingredients             | ×50    |
| Expiry Urgency     | sum(priority values) / (3 × total ingredients) | ×40    |
| Missing Penalty    | min(1.0, missing / 5.0)                        | ×−10   |

**Total Score = (50 × overlapRatio) + (40 × expiryUrgencyRatio) − (10 × missingPenalty)**

Minimum overlap to qualify: **25%**

---

## Ranking Tiebreakers (in order)

1. Total Score — descending
2. HIGH-priority ingredients used — descending
3. Missing ingredient count — ascending
4. Preparation time — ascending

---

## Sample Session

```
Available ingredients: tomato, onion, spinach, paneer, garlic, pasta

Expiry priorities:
  spinach = HIGH
  paneer  = HIGH
  tomato  = MEDIUM

Excluded: mushroom, peanut

Top 3 recommendations

#1  Spinach Paneer Stir-Fry     Score: 73.3
#2  Tomato Pasta                Score: 68.3
#3  Vegetable Omelette          Score: 62.0
```

---

## Design Principles

- **No AI/ML** — fully deterministic rule-based scoring.
- **No external libraries** — standard Java only.
- **No build tools** — compile with `javac`, run with `java`.
- **Readable code** — suitable for college-level evaluation.

- ## 👨‍💻 Authors

**Muhammed Rahil** - [muhammedrahil416](https://github.com/muhammedrahil416)


---

## 📄 License

This project is intended for educational use.

