@echo off
REM ============================================================
REM  Smart Meal Suggestion Engine - Compile and Run Script
REM  Run this from the SmartMealSuggester\ directory
REM ============================================================

echo Compiling Smart Meal Suggestion Engine...
if not exist out mkdir out

javac -d out ^
  src/main/java/mealsuggester/model/ExpiryPriority.java ^
  src/main/java/mealsuggester/model/Recipe.java ^
  src/main/java/mealsuggester/model/RecipeSuggestion.java ^
  src/main/java/mealsuggester/util/IngredientNormalizer.java ^
  src/main/java/mealsuggester/data/RecipeLoader.java ^
  src/main/java/mealsuggester/service/ExplanationGenerator.java ^
  src/main/java/mealsuggester/service/RecipeMatcher.java ^
  src/main/java/mealsuggester/service/RecipeRankingEngine.java ^
  src/main/java/mealsuggester/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Check the error messages above.
    pause
    exit /b 1
)

echo Compilation successful!
echo Copying recipe dataset...
copy src\main\resources\recipes.csv out\ >nul

echo Starting application...
echo.
java -cp out mealsuggester.Main
pause

