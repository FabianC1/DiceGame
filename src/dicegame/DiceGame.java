/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package dicegame;

//imports to make the code more efficient and useful
import java.util.Arrays;
import java.util.Scanner;

public class DiceGame {

    // Initial values that will be used across the entire program
    private static final int NUM_CATEGORIES = 7;
    private static final int SEQUENCE_SCORE = 20;
    private static final int NO_SCORE_ENTERED = -1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialized score tables with NO_SCORE_ENTERED
        int[] scoreTablePlayer1 = new int[NUM_CATEGORIES];
        Arrays.fill(scoreTablePlayer1, NO_SCORE_ENTERED);

        int[] scoreTablePlayer2 = new int[NUM_CATEGORIES];
        Arrays.fill(scoreTablePlayer2, NO_SCORE_ENTERED);

        boolean[] selectedCategoriesPlayer1 = new boolean[NUM_CATEGORIES];
        boolean[] selectedCategoriesPlayer2 = new boolean[NUM_CATEGORIES];

        for (int turn = 1; turn <= 7; turn++) {
            System.out.println("----- Turn " + turn + " -----");

            // Player 1's turn
            playTurn(scanner, "Player 1", scoreTablePlayer1, selectedCategoriesPlayer1);
            displayScoreTable(scoreTablePlayer1, scoreTablePlayer2);

            // Player 2's turn
            playTurn(scanner, "Player 2", scoreTablePlayer2, selectedCategoriesPlayer2);
            displayScoreTable(scoreTablePlayer1, scoreTablePlayer2);
        }

        // Runs the function determine winner
        determineWinner(scoreTablePlayer1, scoreTablePlayer2);

        scanner.close();
    }

    //creates a method which creates the category names
    private static String getCategoryName(int category) {
        switch (category) {
            case 1:
                return "Ones";
            case 2:
                return "Twos";
            case 3:
                return "Threes";
            case 4:
                return "Fours";
            case 5:
                return "Fives";
            case 6:
                return "Sixes";
            case 7:
                return "Sequence";
            default:
                return "";
        }
    }

    //creates a method where the player's turns are monitored and functioned. 
    private static void playTurn(Scanner scanner, String playerName, int[] scoreTable, boolean[] selectedCategories) {
        int[] dice = rollDice();

        for (int throwsLeft = 2; throwsLeft >= 0; throwsLeft--) {
            System.out.println(playerName + "'s turn - Dice: " + Arrays.toString(dice));
            System.out.println("Throws left: " + throwsLeft);

            if (throwsLeft == 0) {
                System.out.println("You cannot defer anymore. Please choose a category.");
                handleSetAside(scanner, playerName, scoreTable, dice, selectedCategories);
                break;
            }

            System.out.println("1. Set aside dice");
            System.out.println("2. Defer");

            int choice = scanner.nextInt();

            if (choice == 1) {
                handleSetAside(scanner, playerName, scoreTable, dice, selectedCategories);
                break;
            } else if (choice == 2) {
                dice = rollDice();
            }
        }
    }

    //creates a method which handles the "set aside" option, as well as the sequences, and scoring.
    private static void handleSetAside(Scanner scanner, String playerName, int[] scoreTable, int[] dice, boolean[] selectedCategories) {
        int category;
        boolean isValidCategory;

        do {
            System.out.println("Select category:");
            displayCategories();
            category = scanner.nextInt();

            isValidCategory = validateCategory(category, selectedCategories);

            if (!isValidCategory) {
                System.out.println("Invalid category. Choose a different one.");
            }
        } while (!isValidCategory);

        selectedCategories[category - 1] = true;

        if (category == 7) { // Sequence category
            System.out.println("Select dice to set aside (enter indices separated by spaces):");
            int[] diceToSetAside = readDiceIndices(scanner);

            int[] remainingDice = getRemainingDice(dice, diceToSetAside);
            Arrays.sort(remainingDice);

            System.out.println("Setting aside dice for Sequence:");
            displayDiceWithIndices(diceToSetAside);

            System.out.println("Sequence values:");
            for (int i = 1; i <= 5; i++) {
                System.out.print(i + ". ");
                if (Arrays.binarySearch(diceToSetAside, i - 1) >= 0) {
                    displayDiceWithValues(new int[]{i});
                } else {
                    System.out.println("MISSING");
                }
            }

            System.out.println();

            // Variable to track the number of throws the user has made in the current round
            int throwsMade = 0;

            while (throwsMade < 2) {
                System.out.println("1. Throw all remaining dice (" + remainingDice.length + ")");
                System.out.println("2. Set aside all dice");
                int choice = scanner.nextInt();
                System.out.println();

                if (choice == 1) {
                    throwsMade++;

                    // Re-roll the remaining dice
                    remainingDice = rollRemainingDice(remainingDice);
                    System.out.println("Re-thrown dice: " + Arrays.toString(remainingDice));
                    System.out.println();

                    if (checkForSequence(remainingDice)) {
                        System.out.println("Congratulations! You got a sequence!");
                        scoreTable[category - 1] += SEQUENCE_SCORE;
                        break;
                    }

                    // After each throw, ask the user again if they want to throw or set aside
                    if (throwsMade >= 2) {
                        System.out.println("You did not manage to get a sequence... unlucky!");
                        scoreTable[category - 1] = 0; // Set score to 0 for categories without a sequence
                        break;
                    }
                } else if (choice == 2) {
                    System.out.println("Setting aside all remaining dice.");
                    break;
                } else {
                    System.out.println("Invalid choice. Please choose 1 to throw all remaining dice or 2 to set aside all dice.");
                    System.out.println();
                }
            }

        } else { // Other categories
            int selectedNumber = category;
            int countMatching = countOccurrences(selectedNumber, dice);

            System.out.println(countMatching + " of your re-rolled dice have value " + selectedNumber + ", setting aside dice: "
                    + Arrays.toString(getMatchingDiceIndices(selectedNumber, dice)));
            System.out.println();

            int totalMatchingDice = countMatching;

            // First re-throw
            int[] remainingDice = getNonMatchingDice(selectedNumber, dice);
            System.out.println("Please enter 1 or 2:");
            System.out.println("1. Throw all remaining dice (" + countNonMatchingDice(selectedNumber, remainingDice) + ")");
            System.out.println("2. Set aside all dice");
            int firstChoice = scanner.nextInt();
            System.out.println();

            if (firstChoice == 1) {
                // Display the re-thrown dice with new random values
                remainingDice = rollRemainingDice(remainingDice);
                System.out.println("Re-thrown dice: " + Arrays.toString(remainingDice));
                System.out.println();

                int countMatchingAfterFirstThrow = countOccurrences(selectedNumber, remainingDice);
                int[] matchingDiceIndices = getMatchingDiceIndices(selectedNumber, remainingDice);

                System.out.println(countMatchingAfterFirstThrow + " of your re-rolled dice have value " + selectedNumber
                        + ", setting aside dice: " + Arrays.toString(matchingDiceIndices));
                System.out.println();

                totalMatchingDice += countMatchingAfterFirstThrow;

                // Second re-throw
                remainingDice = getNonMatchingDice(selectedNumber, remainingDice);
                System.out.println("Please enter 1 or 2:");
                System.out.println("1. Throw all remaining dice (" + countNonMatchingDice(selectedNumber, remainingDice) + ")");
                System.out.println("2. Set aside all dice");
                int secondChoice = scanner.nextInt();
                System.out.println();

                if (secondChoice == 1) {
                    // Display the re-thrown dice with new random values
                    remainingDice = rollRemainingDice(remainingDice);
                    System.out.println("Re-thrown dice: " + Arrays.toString(remainingDice));
                    System.out.println();

                    int countMatchingAfterSecondThrow = countOccurrences(selectedNumber, remainingDice);
                    int[] secondMatchingDiceIndices = getMatchingDiceIndices(selectedNumber, remainingDice);

                    System.out.println(countMatchingAfterSecondThrow + " of your dice have value " + selectedNumber
                            + ", setting aside dice: " + Arrays.toString(secondMatchingDiceIndices));
                    System.out.println();

                    totalMatchingDice += countMatchingAfterSecondThrow;
                } else if (secondChoice == 2) {
                    System.out.println("Setting aside all remaining dice.");
                    System.out.println();
                } else {
                    System.out.println("Invalid choice. Please choose 1 to throw all remaining dice or 2 to set aside all dice.");
                    System.out.println();
                }
            } else if (firstChoice == 2) {
                System.out.println("Setting aside all remaining dice.");
                System.out.println();
            } else {
                System.out.println("Invalid choice. Please choose 1 to throw all remaining dice or 2 to set aside all dice.");
                System.out.println();
            }

            // Display information about the number of dice rolled for the selected category
            System.out.println("You have rolled a total of: " + totalMatchingDice + " dice, for the category " + getCategoryName(category));
            System.out.println();

            // Scoring logic moved outside the loop
            int score = (totalMatchingDice > 0) ? totalMatchingDice * selectedNumber : 0;
            System.out.println(playerName + " scores " + score + " points in category " + getCategoryName(category));
            // Set score to 0 for categories without a match
            scoreTable[category - 1] = (totalMatchingDice > 0) ? score : 0;
        }
    }

    //creates a method in which the user must select a valid category
    private static boolean validateCategory(int category, boolean[] selectedCategories) {
        if (category < 1 || category > selectedCategories.length) {
            return false; // Category out of range
        }

        return !selectedCategories[category - 1]; // Return true if the category is not already selected
    }

    //creates a method in which the dice with indices are displayed (so 1 to 5 is 0 to 4)
    private static void displayDiceWithIndices(int[] indices) {
        for (int index : indices) {
            System.out.print((index + 1) + ". ");
            displayDiceWithValues(new int[]{index + 1});
        }
    }

    //creates a method where the dice are displayed with the values they have.
    private static void displayDiceWithValues(int[] values) {
        System.out.print("[");
        for (int i = 0; i < values.length; i++) {
            System.out.print(values[i]);
            if (i < values.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    //this method reads the dice indices
    private static int[] readDiceIndices(Scanner scanner) {
        scanner.nextLine(); // Consume the newline character
        String input = scanner.nextLine();
        String[] indicesStr = input.split("\\s+");
        int[] indices = new int[indicesStr.length];

        for (int i = 0; i < indicesStr.length; i++) {
            indices[i] = Integer.parseInt(indicesStr[i]) - 1;
        }

        return indices;
    }

    //this method calculates the remaining dice, that are not set aside
    private static int[] getRemainingDice(int[] dice, int[] diceToSetAside) {
        int remainingCount = dice.length - diceToSetAside.length;
        int[] remainingDice = new int[remainingCount];
        int index = 0;

        for (int i = 0; i < dice.length; i++) {
            if (Arrays.binarySearch(diceToSetAside, i) < 0) {
                remainingDice[index++] = dice[i];
            }
        }

        return remainingDice;
    }

    //this method checks if the rolled dice are in a sequence.
    private static boolean checkForSequence(int[] dice) {
        Arrays.sort(dice);
        for (int i = 0; i < dice.length; i++) {
            if (dice[i] != i + 1) {
                return false;
            }
        }
        return true;
    }

    // New method to roll only the remaining dice
    private static int[] rollRemainingDice(int[] remainingDiceIndices) {
        int remainingCount = remainingDiceIndices.length;
        int[] newDice = new int[remainingCount];
        for (int i = 0; i < remainingCount; i++) {
            newDice[i] = (int) (Math.random() * 6) + 1;
        }
        return newDice;
    }

    //this function displays the categories
    private static void displayCategories() {
        System.out.println("1. Ones");
        System.out.println("2. Twos");
        System.out.println("3. Threes");
        System.out.println("4. Fours");
        System.out.println("5. Fives");
        System.out.println("6. Sixes");
        System.out.println("7. Sequence");
    }

    // this function displays the score table, for both players, in a neat manner
    private static void displayScoreTable(int[] scoreTablePlayer1, int[] scoreTablePlayer2) {
        System.out.println("----- Score Table -----");
        System.out.printf("%-12s%-12s%-12s%n", "Category", "Player 1", "Player 2");

        for (int i = 0; i < NUM_CATEGORIES; i++) {
            String categoryName = getCategoryName(i + 1);
            String scorePlayer1 = formatScore(scoreTablePlayer1[i]);
            String scorePlayer2 = formatScore(scoreTablePlayer2[i]);

            System.out.printf("%-12s%-12s%-12s%n", categoryName, scorePlayer1, scorePlayer2);
        }

        System.out.println("--------------------------------------------");
        String totalScorePlayer1 = formatScore(calculateTotalScore(scoreTablePlayer1));
        String totalScorePlayer2 = formatScore(calculateTotalScore(scoreTablePlayer2));

        System.out.printf("%-12s%-12s%-12s%n", "TOTAL", totalScorePlayer1, totalScorePlayer2);
        System.out.println(); // Add an extra line for better readability
    }

    //this function calculates the total score of the rolled dice. 
    private static int calculateTotalScore(int[] scoreTable) {
        int totalScore = 0;
        for (int score : scoreTable) {
            if (score != NO_SCORE_ENTERED) {
                totalScore += score;
            }
        }
        return totalScore;
    }

    // Helper method to format the score for display
    private static String formatScore(int score) {
        return (score == NO_SCORE_ENTERED) ? "-" : String.valueOf(score);
    }

    //this function determines and displays who won at the end of each game.
    private static void determineWinner(int[] scoreTablePlayer1, int[] scoreTablePlayer2) {
        int totalScorePlayer1 = Arrays.stream(scoreTablePlayer1).sum();
        int totalScorePlayer2 = Arrays.stream(scoreTablePlayer2).sum();

        System.out.println("----- Game Over -----");
        System.out.println("Player 1 Total Score: " + totalScorePlayer1);
        System.out.println("Player 2 Total Score: " + totalScorePlayer2);

        if (totalScorePlayer1 > totalScorePlayer2) {
            System.out.println("Player 1 wins!");
        } else if (totalScorePlayer2 > totalScorePlayer1) {
            System.out.println("Player 2 wins!");
        } else {
            System.out.println("It's a tie!");
        }
    }

    //this method retrieves the matching dice with the chosen category
    private static int[] getMatchingDiceIndices(int target, int[] array) {
        int count = countOccurrences(target, array);
        int[] indices = new int[count];
        int index = 0;

        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                indices[index++] = i;
            }
        }

        return indices;
    }

    //this method retrieves the non-matching dice with the chosen category
    private static int[] getNonMatchingDice(int target, int[] array) {
        int count = countNonMatchingDice(target, array);
        int[] nonMatchingDice = new int[count];
        int index = 0;

        for (int i : array) {
            if (i != target) {
                nonMatchingDice[index++] = i;
            }
        }

        return nonMatchingDice;
    }

    //this method counts the non-matching dice with the chosen category
    private static int countNonMatchingDice(int target, int[] array) {
        int count = 0;
        for (int i : array) {
            if (i != target) {
                count++;
            }
        }
        return count;
    }

    //this method counts the matching dice with the chosen category
    private static int countOccurrences(int target, int[] array) {
        int count = 0;
        for (int i : array) {
            if (i == target) {
                count++;
            }
        }
        return count;
    }

    // this method simply rolls the dice
    private static int[] rollDice() {
        int[] dice = new int[5];
        for (int i = 0; i < 5; i++) {
            dice[i] = (int) (Math.random() * 6) + 1;
        }
        return dice;
    }
}
