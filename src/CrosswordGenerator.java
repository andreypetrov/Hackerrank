import java.util.*;

/**
 * Created by Andrey Petrov on 17-01-10.
 */
public class CrosswordGenerator {
    /*public static String[] words = {"the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog"};

    /*public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming"};

*/
  /*  public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog"};

/*    public static String[] words = {"hello", "world", "madbid", "interesting", "task", "korea", "programming",
                                    "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
                                    "keep", "going", "until", "you", "become", "completely", "numb", "and", "then", "some", "more","it","is","never","enough"};
    /*public static String[] words = {"ttttttttttto", "hello", "world", "task", "korea",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "numb", "and", "then", "some", "more","it","is","never","enough"};
*/
    public static String[] words = {"hello", "world", "task", "korea",
            "the", "quick", "brown", "fox", "jumped", "over", "lazy", "dog",
            "keep", "going", "until", "you", "become", "numb", "and", "then", "some", "more","it","is","never","enough",
            "ta", "tb", "tc", "td", "te", "tf", "tg", "th", "ti", "tj", "tk", "tl", "tm", "tn",
            "to", "tp"};

    public static Map<Character, Set<String>> letterCounts;
    public static Map<String, Set<String>> neighbours;
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
        HORIZONTAL,
        VERTICAL
    }

    /*public static class Word {
        public int x;
        public int y;
        public Direction direction;
        public String word;
        public Set<String> neighbours;

    }*/




    //assuming no word repetitions
    //create several crosswords and choose the best. best means with max words from the original list
    public static void generateCrossword() {
        Arrays.sort(words, new StringLengthDescendingComparator());
        print(words);
        letterCounts = countLetters(words);
        neighbours = findNeighbours(letterCounts, words);
        words = removeWordsWithoutNeighbours(words, neighbours);
        Arrays.sort(words, new StringLengthDescendingComparator());
        print(words);
        printLetterCounts(letterCounts);


        //print(neighbours);

    //    List<Word> wordsList = generateWordsList(words, neighbours);
    //    Collections.sort(wordsList, new WordNeighboursAndLengthDescendingComparator());
    //    print(wordsList);


        int firstWordX = BOARD_SIZE / 2;
        int firstWordY = BOARD_SIZE / 2;


        char[][] board = initBoard();
        bestBoards = new ArrayList<char[][]>();
        bestBoard = board;
        bestBoardWordSize = 1;

        startTimeTotal = System.currentTimeMillis();
        for (String word : words) {
            generateBoardStartingFromWord(words[0], firstWordX, firstWordY, board);
        }



        /*for (char[][] aBestBoard : bestBoards) {
            printMinimalBoard(aBestBoard);
        }*/

        printMinimalBoard(bestBoard);
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTimeTotal)/1000;
        System.out.println("Time taken: " + duration + " seconds");

        System.out.println("end");
        System.out.println("generateNextBoardInvocationsCount: " + generateNextBoardInvocationsCount);
        System.out.println("bestBoardScore: " + bestBoardScore);
        System.out.println("bestBoardWordSize: " + bestBoardWordSize);
        System.out.println("bestBoard count: " + bestBoards.size());


    }

    public static final void generateBoardStartingFromWord(String word, int firstWordX, int firstWordY, char[][] board) {
        Set<String> unusedWords = new HashSet<String>(Arrays.asList(words));
        Set<String> usedWords = new HashSet<String>();
        unusedWords.remove(word);
        usedWords.add(word);
        Map<String, Integer> xUsedWords = new HashMap<String, Integer>();
        Map<String, Integer> yUsedWords = new HashMap<String, Integer>();
        Map<String, Direction> directionUsedWords = new HashMap<String, Direction>();
        xUsedWords.put(word, firstWordX);
        yUsedWords.put(word, firstWordY);
        directionUsedWords.put(word, initialDirection);
        bestBoards.add(board);
        writeWordToBoard(word, firstWordX, firstWordY, initialDirection, board);
        startTimePerBoard = System.currentTimeMillis();
        int initialBoardWordSize = 1;
        int initialBoardScore = 0;
        generateNextBoard(word, firstWordX, firstWordY, usedWords, unusedWords, opposite(initialDirection), board, initialBoardWordSize, initialBoardScore, xUsedWords, yUsedWords, directionUsedWords);
    }







    //later optimize to use same board
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


    /**
     * Generate the next board. Try attaching all words to the previous one.
     * Optimized to return earlier, if no better word count can be achieved.
     * If we remove the several early returns, then we can play for score based on crossings
     *
     * @param xPreviousWord
     * @param yPreviousWord
     * @param previousWord
     * @param unusedWords
     * @param candidateDirection
     * @param board
     * @param boardWordSize
     */
    public static void generateNextBoard(String previousWord,
                                         int xPreviousWord,
                                         int yPreviousWord,
                                         Set<String> usedWords,
                                         Set<String> unusedWords,
                                         Direction candidateDirection,
                                         char[][] board,
                                         int boardWordSize,
                                         int boardScore,
                                         Map<String, Integer> xUsedWords,
                                         Map<String, Integer> yUsedWords,
                                         Map<String, Direction> directionUsedWords) {

        long currentTime = System.currentTimeMillis();
        if (currentTime - startTimePerBoard >= TIME_LIMIT_IN_SECONDS_PER_BOARD) return; //kill execution if time limit is reached
        if (currentTime - startTimeTotal >= TIME_LIMIT_IN_SECONDS_TOTAL) return;
        generateNextBoardInvocationsCount++;
        if (unusedWords.size() == 0) return; //bottom of recursion

        if (unusedWords.size() + boardWordSize <= bestBoardWordSize) return; //we will not find a better solution down this path
        if (bestBoardWordSize == words.length) return; //this is a global maximum so stop searching for better solutions (here better means with more words, not with more crossings)


        Set<String> unusedWordsCopy = new HashSet<String>();
        unusedWordsCopy.addAll(unusedWords);


        //for (String previousWord : usedWords) {
            Set<String> previousWordNeighbours = new HashSet<String>();
            previousWordNeighbours.addAll(neighbours.get(previousWord));
            //Iterator<String> iterator = neighbours.get(previousWord).iterator();
            for (String candidateWord : previousWordNeighbours) { //iterate first by candidate words
                if (unusedWordsCopy.contains(candidateWord)) {
                    for (int i = 0; i < previousWord.length(); i++) { //iterate over previous word letters and check which letter the two words can be crossed on
                        char crossLetter = previousWord.charAt(i);
                        if (letterCounts.get(crossLetter).contains(candidateWord)) { //found a matching letter between the two words.

                            // TODO check for more than one occurrence to try all possible crossings, because the letter may repeat in the second word?
                            for (int j = 0; j < candidateWord.length(); j++) { //find where is this letter in the second word.
                                char candidateCrossLetter = candidateWord.charAt(j);

                                int crossingX = candidateDirection == Direction.HORIZONTAL ? xPreviousWord + i : xPreviousWord;
                                int crossingY = candidateDirection == Direction.HORIZONTAL ? yPreviousWord : yPreviousWord + i;


                                if (crossLetter == candidateCrossLetter) { //try solution, we found the indexes of the crossing
                                    int candidateX = candidateDirection == Direction.HORIZONTAL ? xPreviousWord + i : xPreviousWord - j; //shift starting x
                                    int candidateY = candidateDirection == Direction.HORIZONTAL ? yPreviousWord - j : yPreviousWord + i; //shift starting y

                                    int candidateBoardScore = calculateCandidateBoardScore(candidateWord,
                                            candidateX,
                                            candidateY,
                                            candidateDirection,
                                            crossingX,
                                            crossingY,
                                            board);

                                    if (candidateBoardScore >= 0) {
                                        char[][] newBoard = board;
                                        writeWordToBoard(candidateWord, candidateX, candidateY, candidateDirection, newBoard);
                                        int newBoardWordSize = boardWordSize + 1;
                                        int newBoardScore = boardScore + candidateBoardScore;
                                        if (newBoardWordSize > bestBoardWordSize) {
                                            bestBoardWordSize = newBoardWordSize;
                                            bestBoardScore = newBoardScore;
                                            bestBoard = copyBoard(newBoard); //copy successful boards to keep, as we are overwriting the board in our backtracking
                                            bestBoards = new ArrayList<char[][]>();
                                            bestBoards.add(bestBoard);
                                            if (bestBoardWordSize == words.length) return; //this is a global maximum
                                        } else if (newBoardWordSize == bestBoardWordSize) {
                                            bestBoards.add(newBoard);
                                            if (newBoardScore > bestBoardScore) {
                                                bestBoardScore = newBoardScore;
                                                bestBoard = copyBoard(newBoard);
                                            }
                                        }

                                        Set<String> usedWordsCopy = new HashSet<String>();
                                        usedWordsCopy.addAll(usedWords);

                                        usedWordsCopy.add(candidateWord);
                                        xUsedWords.put(candidateWord, candidateX);
                                        yUsedWords.put(candidateWord, candidateY);
                                        directionUsedWords.put(candidateWord, candidateDirection);

                                        neighbours.get(previousWord).remove(candidateWord);
                                        unusedWordsCopy.remove(candidateWord);

                                        //Drill down into all possible already added words
                                        for (String used : usedWordsCopy) {
                                            //remove the candidate, we don't want to use it anymore when going deeper.
                                            //TODO instead of copying boards, add here writing and unwriting of the words in the same board


                                            Direction nextDirection = opposite(directionUsedWords.get(used));
                                            int nextX = xUsedWords.get(used);
                                            int nextY = yUsedWords.get(used);
                                            generateNextBoard(used, nextX, nextY, usedWordsCopy, unusedWordsCopy, nextDirection, newBoard, newBoardWordSize, newBoardScore, xUsedWords, yUsedWords, directionUsedWords); //after going deep add the word back in

                                            //add back the candidate to use for future cases
                                        }


                                        //reset the board and everything when backtracking.
                                        unwriteWordFromBoard(candidateWord, candidateX, candidateY, candidateDirection, newBoard);
                                        neighbours.get(previousWord).add(candidateWord);
                                        unusedWordsCopy.add(candidateWord);

                                        xUsedWords.remove(candidateWord);
                                        yUsedWords.remove(candidateWord);
                                        directionUsedWords.remove(candidateWord);

                                    }

                                }
                            }
                        }
                    }


                }
           // }
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
        if (direction == Direction.VERTICAL) {
            for (int i = 0; i < word.length(); i++) {
                board[x + i][y] = word.charAt(i);
            }
        } else {
            for (int i = 0; i < word.length(); i++) {
                board[x][y + i] = word.charAt(i);
            }
        }

        //print(board);
    }

    public static void unwriteWordFromBoard(String word, int x, int y, Direction direction, char[][] board) {
        if (direction == Direction.VERTICAL) {
            for (int i = 0; i < word.length(); i++) {
                int newX = x+i;
                if (hasEmptyNeighbours(newX, y, direction, board)) {
                    board[newX][y] = '_';
                }
            }
        } else {
            for (int i = 0; i < word.length(); i++) {
                int newY = y+i;
                if (hasEmptyNeighbours(x, newY, direction, board)) {
                    board[x][newY] = '_';
                }
            }
        }

        //print(board);
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

    private static void print(Set<String> neighbours) {
        for (String neighbour : neighbours) {
            System.out.print(neighbour + ", ");
        }
        System.out.println();
    }

    /*private static void print(List<Word> wordsList) {
        for (Word word : wordsList) {
            System.out.print(word.word + ": ");
            print(word.neighbours);
        }
        System.out.println();
    }*/


    /*private static List<Word> generateWordsList(String[] words, Map<String, Set<String>> neighbours) {
        List<Word> result = new ArrayList<Word>();
        for (String wordString : words) {
            Word word = new Word();
            word.word = wordString;
            word.neighbours = neighbours.get(wordString);
            result.add(word);
        }
        return result;
    }*/

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


    public static void printLetterCounts(Map<Character, Set<String>> input) {
        for (Map.Entry<Character, Set<String>> entry : input.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for (String word : entry.getValue()) {
                System.out.print(word + ", ");
            }
            System.out.println();
        }
    }

    public static void print(Map<String, Set<String>> input) {
        for (Map.Entry<String, Set<String>> entry : input.entrySet()) {
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
                if (board[i][j]!='_') {
                    startX = Math.min(startX, i);
                    startY = Math.min(startY, j);
                    endX = Math.max(endX, i);
                    endY = Math.max(endY, j);
                }
            }
        }

        System.out.println("Board:");
        for (int i = startX; i <= endX ; i++) {
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


    public static class StringLengthDescendingComparator implements java.util.Comparator<String> {
        public int compare(String s1, String s2) {
            return s2.length() - s1.length();
        }
    }


    /*public static class WordNeighboursAndLengthDescendingComparator implements java.util.Comparator<Word> {
        public int compare(Word w1, Word w2) { //sort first by neighbours, next by length
            int neighboursDistance = w2.neighbours.size() - w1.neighbours.size();
            if (neighboursDistance != 0) {
                return neighboursDistance;
            }
            return w2.word.length() - w1.word.length();
        }
    }*/

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
