import java.util.*;

/**
 * Created by Andrey Petrov on 17-01-10.
 */
public class CrosswordGenerator {
   /*public static String[] words = {"the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog"};
*/
   public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming"};


  /*  public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog"};
*/
  /* public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "completely", "numb", "and", "then", "some", "more", "it", "is", "never", "enough"};


    /*public static String[] words = {"ttttttttttto", "hello", "world", "task", "korea",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "numb", "and", "then", "some", "more","it","is","never","enough"};


   /* public static String[] words = {"hello", "world", "task", "korea",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "numb", "and", "then", "some", "more","it","is","never","enough",
            "ta", "tb", "tc", "td", "te", "tf", "tg", "th", "ti", "tj", "tk", "tl", "tm", "tn",
            "to", "tp"};
*/

    public static class WordData {
        int startX;
        int startY;
        Direction direction;

        public WordData(int startX, int startY, Direction direction) {
            this.startX = startX;
            this.startY = startY;
            this.direction = direction;
        }
    }


    public static Map<Character, Set<String>> letterCounts;
    public static Map<String, Set<String>> neighbours;
    public static Map<String, Boolean> used;
    public static Map<String, WordData> usedWordsData = new HashMap<String, WordData>();

//    public static Map<String, Integer> xUsedWords = new HashMap<String, Integer>();
//    public static Map<String, Integer> yUsedWords = new HashMap<String, Integer>();
//   public static Map<String, Direction> directionUsedWords = new HashMap<String, Direction>();


    public static char[][] bestBoard;
    public static List<char[][]> bestBoards;

    public static int bestBoardWordSize = 0;
    public static int bestBoardScore = 0;

    public static long startTimeTotal = 0;
    public static long startTimePerBoard = 0;

    public static final long TIME_LIMIT_IN_SECONDS_TOTAL = 15 * 1000;
    public static final long TIME_LIMIT_IN_SECONDS_PER_BOARD = 15 * 1000;
    public static final int BOARD_SIZE = 50;
    public static long generateNextBoardInvocationsCount = 0;
    public static final Direction initialDirection = Direction.HORIZONTAL;

    public static enum Direction {
        HORIZONTAL(0, 1),
        VERTICAL(1, 0);

        int dx;
        int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    /**
     * Assuming no word repetitions
     * create several crosswords and choose the best. best means with max words from the original list
     */
    public static void generateCrossword() {
        print(words);
        letterCounts = countLetters(words);
        neighbours = findNeighbours(letterCounts, words);
        words = removeWordsWithoutNeighbours(words, neighbours);
        used = generatedUsedMap(words);

        Arrays.sort(words, new StringLengthDescendingComparator());
        print(words);
        printLetterCounts(letterCounts);

        //start in the middle of the board so that we can expand in any direction we need
        int firstWordX = BOARD_SIZE / 2;
        int firstWordY = BOARD_SIZE / 2;


        char[][] board = initBoard();
        bestBoards = new ArrayList<char[][]>();
        bestBoard = board;
        bestBoardWordSize = 1;

        startTimeTotal = System.currentTimeMillis();
        for (String word : words) {
            generateBoardStartingFromWord(word, firstWordX, firstWordY, board);
        }

        printResults();
    }

    public static void printResults() {
        /*for (char[][] aBestBoard : bestBoards) {
            printMinimalBoard(aBestBoard);
        }*/

        printMinimalBoard(bestBoard);
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTimeTotal) / 1000;
        System.out.println("Time taken: " + duration + " seconds");
        System.out.println("end");
        System.out.println("generateNextBoardInvocationsCount: " + generateNextBoardInvocationsCount);
        System.out.println("bestBoardScore: " + bestBoardScore);
        System.out.println("bestBoardWordSize: " + bestBoardWordSize);
        System.out.println("bestBoard count: " + bestBoards.size());
    }


    /**
     * Create a new board starting with the given word
     * @param word
     * @param firstWordX
     * @param firstWordY
     * @param board
     */
    public static final void generateBoardStartingFromWord(String word, int firstWordX, int firstWordY, char[][] board) {
        useWord(word, firstWordX, firstWordY, initialDirection, board);

        bestBoards.add(board);
        startTimePerBoard = System.currentTimeMillis();
        int initialBoardWordSize = 1;
        int initialBoardScore = 0;
        int wordsLeft = words.length - 1;


        WordData wordData = new WordData(firstWordX, firstWordY, initialDirection);
        generateNextBoard(word, wordData, board, initialBoardWordSize, initialBoardScore, wordsLeft);

        //reset after
        unuseWord(word, firstWordX, firstWordY, initialDirection, board, null);
    }




    /**
     * Generate the next board. Try attaching a new word to one of the previous ones.
     * Optimized to return earlier, if no better word count can be achieved.
     * If we remove the several early returns, then we can play for score based on crossings
     *
     * @param previousWord
     * @param previousWordData
     * @param board
     * @param wordsUsedCount
     * @param boardScore
     * @param wordsLeftCount
     */
    public static void generateNextBoard(String previousWord,
                                         WordData previousWordData,
                                         char[][] board,
                                         int wordsUsedCount,
                                         int boardScore,
                                         int wordsLeftCount) {

        Direction candidateDirection = opposite(previousWordData.direction);

        generateNextBoardInvocationsCount++;
        if (shouldTerminate(wordsLeftCount, wordsUsedCount)) return;

        for (String candidateWord : neighbours.get(previousWord)) { //iterate first by candidate words
            if (!used.get(candidateWord)) {
                for (int i = 0; i < previousWord.length(); i++) { //iterate over previous word letters and check which letter the two words can be crossed on
                    char crossLetter = previousWord.charAt(i);
                    if (letterCounts.get(crossLetter).contains(candidateWord)) { //found a matching letter between the two words.

                        int crossingX = candidateDirection == Direction.HORIZONTAL ? previousWordData.startX + i : previousWordData.startX;
                        int crossingY = candidateDirection == Direction.HORIZONTAL ? previousWordData.startY : previousWordData.startY + i;

                        //check for more than one occurrence to try all possible crossings, because the letter may repeat in the second word?
                        for (int j = 0; j < candidateWord.length(); j++) { //find where is this letter in the second word.
                            char candidateCrossLetter = candidateWord.charAt(j);

                            if (crossLetter == candidateCrossLetter) { //try solution, we found the indexes of the crossing
                                int candidateX = candidateDirection == Direction.HORIZONTAL ? previousWordData.startX + i : previousWordData.startX - j; //shift starting x
                                int candidateY = candidateDirection == Direction.HORIZONTAL ? previousWordData.startY - j : previousWordData.startY + i; //shift starting y

                                int candidateBoardScore = calculateCandidateBoardScore(candidateWord,
                                        candidateX,
                                        candidateY,
                                        candidateDirection,
                                        crossingX,
                                        crossingY,
                                        board);

                                if (candidateBoardScore >= 0) {

                                    useWord(candidateWord, candidateX, candidateY, candidateDirection, board);

                                    int newBoardWordSize = wordsUsedCount + 1;
                                    int newBoardScore = boardScore + candidateBoardScore;
                                    keepBoardIfGood(board, newBoardWordSize, newBoardScore);

                                    if (bestBoardWordSize == words.length) return; //this is a global maximum, so cut earlier to prune

                                    //Drill down into all possible already added words
                                    for (String word : words) {
                                        if (used.get(word)) {
                                            WordData wordData = usedWordsData.get(word);
                                            generateNextBoard(word, wordData, board, newBoardWordSize, newBoardScore, wordsLeftCount - 1); //after going deep add the word back in
                                        }
                                    }

                                    //reset the board and everything when backtracking.
                                    unuseWord(candidateWord, candidateX, candidateY, candidateDirection, board, previousWord);

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check whether the backtracking have reached a terminal condition, i.e. bottom of recursion
     * @param wordsLeftCount
     * @param wordsUsedCount
     * @return
     */
    private static boolean shouldTerminate(int wordsLeftCount, int wordsUsedCount) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTimePerBoard >= TIME_LIMIT_IN_SECONDS_PER_BOARD)
            return true; //kill execution if time limit is reached
        if (currentTime - startTimeTotal >= TIME_LIMIT_IN_SECONDS_TOTAL) return true;
        //bottom of recursion
        if (wordsLeftCount == 0) return true;
        //we will not find a better solution down this path
        if (wordsLeftCount + wordsUsedCount <= bestBoardWordSize) return true;
        //this is a global maximum so stop searching for better solutions (here better means with more words, not with more crossings)
        if (bestBoardWordSize == words.length) return true;

        return false;
    }


    /**
     * Add a word to board
     * @param word
     * @param x
     * @param y
     * @param direction
     * @param board
     */
    public static void useWord(String word, int x, int y, Direction direction, char[][] board) {
        used.put(word, true);
        WordData wordData = new WordData(x, y, direction);
        usedWordsData.put(word, wordData);
        writeWordToBoard(word, x, y, direction, board);
    }

    /**
     * Remove a word from board (unroll the useWord action)
     * @param word
     * @param x
     * @param y
     * @param direction
     * @param board
     * @param previousWord
     */
    public static void unuseWord(String word, int x, int y, Direction direction, char[][] board, String previousWord) {
        //We care about word coordinates and direction only if they are in the used map, so no need to modify the other maps - xUsedWords, yUsedWords, and directionUsedWords;
        used.put(word, false);
        unwriteWordFromBoard(word, x, y, direction, board);
    }



    /**
     * If the board is the best we have found until now (or one of the best), then keep a copy of it
     * @param board
     * @param boardWordSize
     * @param boardScore
     */
    public static void keepBoardIfGood (char[][] board, int boardWordSize, int boardScore) {
        if (boardWordSize > bestBoardWordSize) {
            bestBoardWordSize = boardWordSize;
            bestBoardScore = boardScore;
            bestBoard = copyBoard(board); //copy successful boards to keep, as we are overwriting the board in our backtracking
            bestBoards = new ArrayList<char[][]>();
            bestBoards.add(bestBoard);
        } else if (boardWordSize == bestBoardWordSize) {
            bestBoards.add(board);
            if (boardScore > bestBoardScore) {
                bestBoardScore = boardScore;
                bestBoard = copyBoard(board);
            }
        }

    }


    /**
     * Write the word to the board at the given x and y
     *
     * @param word
     * @param x
     * @param y
     * @param direction
     * @param board
     */
    public static void writeWordToBoard(String word, int x, int y, Direction direction, char[][] board) {
        int currentX = x;
        int currentY = y;
        for (int i = 0; i < word.length(); i++) {
            board[currentX][currentY] = word.charAt(i);
            currentX += direction.dx;
            currentY += direction.dy;
        }
    }

    public static void unwriteWordFromBoard(String word, int x, int y, Direction direction, char[][] board) {
        int currentX = x;
        int currentY = y;
        for (int i = 0; i < word.length(); i++) {
            if (hasEmptyNeighbours(currentX, currentY, direction, board)) {
                board[currentX][currentY] = '_';
            }
            currentX += direction.dx;
            currentY += direction.dy;
        }
    }


    /**
     * Check whether a word can be written at starting coordinates x and y in the given direction
     *
     * @param word
     * @param x
     * @param y
     * @param direction
     * @param intersectionWithPreviousWordX
     * @param intersectionWithPreviousWordY
     * @param board
     * @return score of potential placement
     */
    public static int calculateCandidateBoardScore(String word,
                                                   int x,
                                                   int y,
                                                   Direction direction,
                                                   int intersectionWithPreviousWordX,
                                                   int intersectionWithPreviousWordY,
                                                   char[][] board) {
        if (x < 0 || y < 0 || x > board.length || y > board[0].length) return -1;

        int score = 0; //default score is 1 if there are no crossings with other words besides the previous one
        int wordLength = word.length();
        if (direction == Direction.VERTICAL) {
            if (x + wordLength > board.length) return -1; //no space to put in the word


            //check whether the element before first letter and element after last letter are free. Words should not touch others
            if (x + wordLength < board.length && board[x + wordLength][y] != '_') return -1;
            if (x - 1 > 0 && board[x - 1][y] != '_') return -1;


            for (int i = 0; i < wordLength; i++) {
                int currentX = x + i;
                if (currentX == intersectionWithPreviousWordX) continue; //no need to check the intersected letter

                char currentLetter = word.charAt(i);
                int target = board[currentX][y];

                if (target == '_') { //empty, so good candidate if it has valid neighbours
                    if (!hasEmptyNeighbours(currentX, y, direction, board)) return -1;
                } else if (target != currentLetter) {
                    return -1;//letter is not matching
                } else {// target == current
                    //word is crossing another word, increase score
                    score++;
                }
            }
        } else { //direction = Direction.HORIZONTAL
            if (y + wordLength > board[0].length) return -1; //no space to put in the word

            //check whether the element before first letter and element after last letter are free. Words should not touch others
            if (y + wordLength < board[0].length && board[x][y + wordLength] != '_') return -1;
            if (y - 1 > 0 && board[x][y - 1] != '_') return -1;


            for (int i = 0; i < wordLength; i++) {
                int currentY = y + i;
                if (currentY == intersectionWithPreviousWordY) continue; //no need to check the intersected letter
                char currentLetter = word.charAt(i);
                int target = board[x][currentY];
                if (target == '_') { //empty, so good candidate if it has valid neighbours
                    if (!hasEmptyNeighbours(x, currentY, direction, board)) return -1;
                } else if (target != word.charAt(i)) {
                    return -1;//letter is not matching
                } else {
                    //word is crossing another word, increase score
                    score++;
                }
            }
        }
        return score;
    }

    /**
     * Check whether the neighbours are valid. Touching another word should not happen.
     *
     * @param x
     * @param y
     * @param direction
     * @return
     */

    private static boolean hasEmptyNeighbours(int x, int y, Direction direction, char[][] board) {
        if (direction == Direction.HORIZONTAL) {
            //if element on top is occupied, means we are touching another word
            if (x - 1 > 0 && board[x - 1][y] != '_') return false;
            //if the element on bottom is occupied, means we are touching another word
            if (x + 1 < board.length && board[x + 1][y] != '_') return false;
        } else { //direction == Direction.VERTICAL
            //if the element on left is occupied, means we are touching another word
            if (y - 1 > 0 && board[x][y - 1] != '_') return false;
            //if the element on right is occupied, means we are touching another word
            if (y + 1 < board[0].length && board[x][y + 1] != '_') return false;
        }
        return true;
    }


    public static Map<String, Set<String>> findNeighbours(Map<Character, Set<String>> letterCounts, String[] words) {

        Map<String, Set<String>> neighbours = new HashMap<String, Set<String>>();
        for (String word : words) {
            Set<String> wordNeighbours = new HashSet<String>();
            neighbours.put(word, wordNeighbours); //assuming no word repetitions in the original strings array
            for (int i = 0; i < word.length(); i++) {
                char letter = word.charAt(i);
                Set<String> neighboursByLetter = letterCounts.get(letter);
                for (String neighbour : neighboursByLetter) {
                    if (!neighbour.equals(word)) {
                        wordNeighbours.add(neighbour);
                    }
                }
            }
        }
        return neighbours;
    }

    public static Map<Character, Set<String>> countLetters(String[] words) {
        Map<Character, Set<String>> letterCounts = new HashMap<Character, Set<String>>();
        for (String word : words) {
            for (int i = 0; i < word.length(); i++) {
                char letter = word.charAt(i);
                if (!letterCounts.containsKey(letter)) {
                    letterCounts.put(letter, new HashSet<String>());
                }
                letterCounts.get(letter).add(word);
            }
        }
        return letterCounts;
    }


    private static Map<String, Boolean> generatedUsedMap(String[] words) {
        Map<String, Boolean> used = new HashMap<String, Boolean>();
        for (String word : words) {
            used.put(word, false);
        }
        return used;
    }

    public static void printLetterCounts(Map<Character, Set<String>> input) {
        for (Map.Entry<Character, Set<String>> entry : input.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for (String word : entry.getValue()) {
                System.out.print(word + ", ");
            }
            System.out.println();
        }
    }

    public static void print(String[] words) {
        for (String word : words) {
            System.out.print(word + ", ");
        }
        System.out.println();
    }

    public static char[][] initBoard() {
        char[][] grid = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = '_';
            }
            System.out.println();
        }
        return grid;
    }

    public static void printMinimalBoard(char[][] board) {
        int startX = BOARD_SIZE;
        int startY = BOARD_SIZE;
        int endX = 0;
        int endY = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != '_') {
                    startX = Math.min(startX, i);
                    startY = Math.min(startY, j);
                    endX = Math.max(endX, i);
                    endY = Math.max(endY, j);
                }
            }
        }

        System.out.println("Board:");
        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void printBoard(char[][] board) {
        System.out.println("Board:");
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    //later optimize to useWord same board
    public static char[][] copyBoard(char[][] board) {
        char[][] newBoard = new char[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                newBoard[i][j] = board[i][j];
            }
        }
        return newBoard;
    }

    public static Direction opposite(Direction direction) {
        if (direction == Direction.VERTICAL) return Direction.HORIZONTAL;
        else return Direction.VERTICAL;
    }


    public static class StringLengthDescendingComparator implements java.util.Comparator<String> {
        public int compare(String s1, String s2) {
            return s2.length() - s1.length();
        }
    }

    public static String[] removeWordsWithoutNeighbours(String[] words, Map<String, Set<String>> neighbours) {
        Set<String> wordsWithNeighbours = new HashSet<String>();
        for (String word : words) {
            if (neighbours.get(word).size() > 0) {
                wordsWithNeighbours.add(word);
            }
        }
        return wordsWithNeighbours.toArray(new String[wordsWithNeighbours.size()]);
    }
}
