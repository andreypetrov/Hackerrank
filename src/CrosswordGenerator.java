import java.util.*;

/**
 * TODO remove board edge checks as we always make a board that can fit all words
 * Created by Andrey Petrov on 17-01-10.
 */
public class CrosswordGenerator {
    /*public static String[] words = {"the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog"};
 */
   public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming"};


  /* public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog"};

  /* public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "completely", "numb", "and", "then", "some", "more", "it", "is", "never", "enough"};

*/
   /* public static String[] words = {"ttttttttttto", "hello", "world", "task", "korea",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "numb", "and", "then", "some", "more", "it", "is", "never", "enough"};


    public static String[] words = {"hello", "world", "task", "korea",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "numb", "and", "then", "some", "more","it","is","never","enough",
            "ta", "tb", "tc", "td", "te", "tf", "tg", "th", "ti", "tj", "tk", "tl", "tm", "tn",
            "to", "tp"};*/



    public static Map<Character, Set<String>> letterCounts;
    public static Map<String, Set<String>> neighbours;
    public static Map<String, Boolean> used;
    public static Map<String, WordData> usedWordsData = new HashMap<String, WordData>();

    public static char[][] bestBoard;
    public static List<char[][]> bestBoards;

    public static int boardSize = 0;

    public static int bestBoardWordSize = 0;
    public static int bestBoardScore = 0;

    public static long startTimeTotal = 0;
    public static long startTimePerBoard = 0;

    public static final long TIME_LIMIT_IN_SECONDS_TOTAL = 15 * 1000;
    public static final long TIME_LIMIT_IN_SECONDS_PER_BOARD = 5 * 1000;
    public static long generateNextBoardInvocationsCount = 0;
    public static final Direction initialDirection = Direction.HORIZONTAL;


    public static enum Direction {
        HORIZONTAL(0, 1),
        VERTICAL(1, 0);

        //vector of movement
        int dRow;
        int dColumn;

        Direction(int dRow, int dColumn) {
            this.dRow = dRow;
            this.dColumn = dColumn;
        }
    }


    public static class WordData {
        int r;
        int c;
        Direction direction;

        public WordData(int r, int c, Direction direction) {
            this.r = r;
            this.c = c;
            this.direction = direction;
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
        boardSize = calculateMaxNeededBoardSize(words);
        print(words);
        printLetterCounts(letterCounts);

        //start in the middle of the board so that we can expand in any direction we need
        int firstWordR = boardSize / 2;
        int firstWordC = boardSize / 2;


        char[][] board = initBoard();
        bestBoards = new ArrayList<char[][]>();
        bestBoard = board;
        bestBoardWordSize = 1;

        startTimeTotal = System.currentTimeMillis();
        for (String word : words) {
            WordData wordData = new WordData(firstWordR, firstWordC, initialDirection);
            generateBoardStartingFromWord(word, wordData, board);
        }

        printResults();
    }

    /**
     * max length needs to be twice the length of half of the words (those that are longest).
     *
     * @param words needs to be sorted by length in descending order
     * @return
     */
    private static int calculateMaxNeededBoardSize(String[] words) {
        int size = 0;
        int count = words.length / 2 + words.length % 2;
        for (int i = 0; i < count; i++) {
            size += words[i].length();
        }
        size+=2;
        size+=10; //add 10 just in case for buffer, so that checking of the edges of the board won't matter. In the future we can even remove the boarder checks from the algorithm
        return size;
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
        System.out.println("words with neighbours: " + words.length);
        System.out.println("generateNextBoardInvocationsCount: " + generateNextBoardInvocationsCount);
        System.out.println("bestBoardScore: " + bestBoardScore);
        System.out.println("bestBoardWordSize: " + bestBoardWordSize);
        System.out.println("bestBoard count: " + bestBoards.size());
    }


    /**
     * Create a new board starting with the given word
     *
     * @param word
     * @param wordData
     * @param board
     */
    public static final void generateBoardStartingFromWord(String word, WordData wordData, char[][] board) {
        int candidateBoardScore = calculateCandidateBoardScore(word, wordData, -1, -1, board);

        if (candidateBoardScore >= 0) {
            useWord(word, wordData, board);


            bestBoards.add(board);
            startTimePerBoard = System.currentTimeMillis();
            int initialBoardWordSize = 1;
            int initialBoardScore = 0;
            int wordsLeft = words.length - 1;


            generateNextBoard(word, wordData, board, initialBoardWordSize, initialBoardScore, wordsLeft);

            //reset after
            unuseWord(word, wordData, board, null);
        }
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

                        int crossingR = previousWordData.r + i * previousWordData.direction.dRow;  //candidateDirection == Direction.HORIZONTAL ? previousWordData.r + i : previousWordData.r;
                        int crossingC = previousWordData.c + i * previousWordData.direction.dColumn; //candidateDirection == Direction.HORIZONTAL ? previousWordData.c : previousWordData.c + i;

                        //check for more than one occurrence to try all possible crossings, because the letter may repeat in the second word
                        for (int j = 0; j < candidateWord.length(); j++) { //find where is this letter in the second word.
                            char candidateCrossLetter = candidateWord.charAt(j);

                            if (crossLetter == candidateCrossLetter) { //try solution, we found the indexes of the crossing
                                int candidateR = candidateDirection == Direction.HORIZONTAL ? previousWordData.r + i : previousWordData.r - j; //shift starting x
                                //int candidateR = previousWordData.r + i*previousWordData.direction.dRow - j*previousWordData.direction.dColumn;
                                int candidateC = candidateDirection == Direction.HORIZONTAL ? previousWordData.c - j : previousWordData.c + i; //shift starting y
                                //int candidateC = previousWordData.c - j*previousWordData.direction.dRow + i*previousWordData.direction.dColumn;

                                WordData candidateWordData = new WordData(candidateR, candidateC, candidateDirection);
                                int candidateBoardScore = calculateCandidateBoardScore(candidateWord, candidateWordData, crossingR, crossingC, board);

                                if (candidateBoardScore >= 0) { //the candidate is good, so let's place it and explore the option further

                                    useWord(candidateWord, candidateWordData, board);

                                    int newBoardWordSize = wordsUsedCount + 1;
                                    int newBoardScore = boardScore + candidateBoardScore;
                                    keepBoardIfGood(board, newBoardWordSize, newBoardScore);

                                    if (bestBoardWordSize == words.length)
                                        return; //this is a global maximum, so cut earlier to prune

                                    //Drill down into all possible already added words
                                    for (String word : words) {
                                        if (used.get(word)) {
                                            WordData wordData = usedWordsData.get(word);
                                            generateNextBoard(word, wordData, board, newBoardWordSize, newBoardScore, wordsLeftCount - 1); //after going deep add the word back in
                                        }
                                    }

                                    //reset the board and everything when backtracking.
                                    unuseWord(candidateWord, candidateWordData, board, previousWord);

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
     *
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
     *
     * @param word
     * @param wordData
     * @param board
     */
    public static void useWord(String word, WordData wordData, char[][] board) {
        used.put(word, true);
        usedWordsData.put(word, wordData);
        writeWordToBoard(word, wordData, board);
    }

    /**
     * Remove a word from board (unroll the useWord action)
     *
     * @param word
     * @param wordData
     * @param board
     * @param previousWord
     */
    public static void unuseWord(String word, WordData wordData, char[][] board, String previousWord) {
        //We care about word coordinates and direction only if they are in the used map, so no need to modify the other maps - xUsedWords, yUsedWords, and directionUsedWords;
        used.put(word, false);
        unwriteWordFromBoard(word, wordData, board);
    }


    /**
     * If the board is the best we have found until now (or one of the best), then keep a copy of it
     *
     * @param board
     * @param boardWordSize
     * @param boardScore
     */
    public static void keepBoardIfGood(char[][] board, int boardWordSize, int boardScore) {
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
     * Write the word to the board at the given row and column
     *
     * @param word
     * @param wordData
     * @param board
     */
    public static void writeWordToBoard(String word, WordData wordData, char[][] board) {
        int currentR = wordData.r;
        int currentC = wordData.c;
        for (int i = 0; i < word.length(); i++) {
            board[currentR][currentC] = word.charAt(i);
            currentR += wordData.direction.dRow;
            currentC += wordData.direction.dColumn;
        }
    }

    public static void unwriteWordFromBoard(String word, WordData wordData, char[][] board) {
        int currentR = wordData.r;
        int currentC = wordData.c;
        for (int i = 0; i < word.length(); i++) {
            if (hasEmptyNeighbours(currentR, currentC, wordData.direction, board)) {
                board[currentR][currentC] = '_';
            }
            currentR += wordData.direction.dRow;
            currentC += wordData.direction.dColumn;
        }
    }


    /**
     * Check whether a word can be written at starting coordinates r and c in the given direction
     *
     * @param word
     * @param wordData
     * @param intersectionWithPreviousWordR
     * @param intersectionWithPreviousWordC
     * @param board
     * @return score of potential placement
     */
    public static int calculateCandidateBoardScore(String word,
                                                   WordData wordData,
                                                   int intersectionWithPreviousWordR,
                                                   int intersectionWithPreviousWordC,
                                                   char[][] board) {
        int r = wordData.r;
        int c = wordData.c;
        Direction direction = wordData.direction;

        if (r < 0 || c < 0 || r >= board.length || c >= board[0].length) return -1;


        int score = 0; //default score is 1 if there are no crossings with other words besides the previous one
        int wordLength = word.length();

        int verticalWordLength = wordLength * direction.dRow;
        int horizontalWordLength = wordLength * direction.dColumn;
        if (r + verticalWordLength > board.length) return -1; //no space to put in the word
        if (c + horizontalWordLength > board[0].length) return -1; //no space to put in the word


        if (direction == Direction.VERTICAL) {
            //check whether the element before first letter and element after last letter are free. Words should not touch others
            if (r + verticalWordLength < board.length && board[r + verticalWordLength][c] != '_') return -1;
            if (r - direction.dRow > 0 && board[r - 1][c] != '_') return -1;

            for (int i = 0; i < wordLength; i++) {
                int currentR = r + i;
                if (currentR == intersectionWithPreviousWordR) continue; //no need to check the intersected letter

                char currentLetter = word.charAt(i);
                int target = board[currentR][c];

                if (target == '_') { //empty, so good candidate if it has valid neighbours
                    if (!hasEmptyNeighbours(currentR, c, direction, board)) return -1;
                } else if (target != currentLetter) {
                    return -1;//letter is not matching
                } else {// target == current
                    //word is crossing another word, increase score
                    score++;
                }
            }
        } else { //direction = Direction.HORIZONTAL
            //check whether the element before first letter and element after last letter are free. Words should not touch others
            if (c + wordLength < board[0].length && board[r][c + wordLength] != '_') return -1;
            if (c - 1 > 0 && board[r][c - 1] != '_') return -1;


            for (int i = 0; i < wordLength; i++) {
                int currentC = c + i;
                if (currentC == intersectionWithPreviousWordC) continue; //no need to check the intersected letter
                char currentLetter = word.charAt(i);
                int target = board[r][currentC];
                if (target == '_') { //empty, so good candidate if it has valid neighbours
                    if (!hasEmptyNeighbours(r, currentC, direction, board)) return -1;
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
     * @param r
     * @param c
     * @param direction
     * @return
     */

    private static boolean hasEmptyNeighbours(int r, int c, Direction direction, char[][] board) {
        if (direction == Direction.HORIZONTAL) {
            //if element on top is occupied, means we are touching another word
            if (r - 1 > 0 && board[r - 1][c] != '_') return false;
            //if the element on bottom is occupied, means we are touching another word
            if (r + 1 < board.length && board[r + 1][c] != '_') return false;
        } else { //direction == Direction.VERTICAL
            //if the element on left is occupied, means we are touching another word
            if (c - 1 > 0 && board[r][c - 1] != '_') return false;
            //if the element on right is occupied, means we are touching another word
            if (c + 1 < board[0].length && board[r][c + 1] != '_') return false;
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
        char[][] grid = new char[boardSize][boardSize];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = '_';
            }
            System.out.println();
        }
        return grid;
    }

    public static void printMinimalBoard(char[][] board) {
        int startRow = boardSize;
        int startColumn = boardSize;
        int endRow = 0;
        int endColumn = 0;
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                if (board[r][c] != '_') {
                    startRow = Math.min(startRow, r);
                    startColumn = Math.min(startColumn, c);
                    endRow = Math.max(endRow, r);
                    endColumn = Math.max(endColumn, c);
                }
            }
        }

        System.out.println("Board:");
        for (int r = startRow; r <= endRow; r++) {
            for (int c = startColumn; c <= endColumn; c++) {
                System.out.print(board[r][c] + " ");
            }
            System.out.println();
        }
    }

    public static void printBoard(char[][] board) {
        System.out.println("Board:");
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                System.out.print(board[r][c] + " ");
            }
            System.out.println();
        }
    }

    //later optimize to useWord same board
    public static char[][] copyBoard(char[][] board) {
        char[][] newBoard = new char[board.length][board[0].length];
        for (int r = 0; r < board.length; r++) { //r is row index
            for (int c = 0; c < board[0].length; c++) { //c is column index
                newBoard[r][c] = board[r][c];
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
