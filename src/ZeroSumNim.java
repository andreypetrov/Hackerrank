/**
 * Created by user1 on 16-12-23.
 */
import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

public class ZeroSumNim {
    public static Map<State, Boolean> stateCache = new HashMap<State, Boolean>();
    public static Set<Integer> uniqueXors = new HashSet<Integer>();
    public static Set<Integer> previousXors = new HashSet<Integer>();


    //public static long evaluationCount;
    //public static int[][] losing = {{1, 2, 4}, {1, 3, 5}, {1, 6, 8}, {1, 7, 9}, {1, 10, 12},
      //                {4, 5, 6}};

    //public static int[][] winning = {{1, 2, 3}, {1, 2, 5}, {1, 2, 6}, {1, 2, 7}, {1, 2, 8}};

    public static <E> void print(Set<E> input) {
        for (E e : input) {
            System.out.println(e);
        }
    }

    public static void print(long[] input) {
        System.out.print("[");
        for (long i:input) {
            System.out.print(i + ", ");
        }
        System.out.println("]");
    }

    public static void print(boolean[] input) {
        System.out.print("[");
        for (boolean i:input) {
            System.out.print((i? "1" : "0") + ", ");
        }
        System.out.println("]");
    }

    public static void print(int[] input) {
        System.out.print("[");
        for (long i:input) {
            System.out.print(i + ", ");
        }
        System.out.println("]");
    }

    public static <E extends State> void print(List<E> input) {
        System.out.print("[");
        for (E i:input) {
            print(i);
            print("____");
        }
        System.out.println("]");
    }

    public static void print(long input) {
        System.out.println(input);
    }

    public static void print(int input) {
        System.out.println(input);
    }

    public static void print(String input) {
        System.out.println(input);
    }

    public static void print(State state) {
        print(state.p);
        print(state.canSkipP);
    }

    static class State {
        public int[] p; // every p has at least one element
        public boolean[] canSkipP;

        public static State createInitial(int[] p) {
            boolean[] canSkipP = new boolean[p.length];
            for (int i = 0; i < canSkipP.length; i++) {
                canSkipP[i] = true;
            }
            return new State(p, canSkipP);
        }

        public State(int[] p, boolean[] canSkipP) {
            this.p = p;
            this.canSkipP = canSkipP;
        }

        public List<State> generateFutureStates() {
            List<State> futureStates = new ArrayList<State>();
            futureStates.addAll(generateFutureStatesBySkipping());
            futureStates.addAll(generateFutureStatesByStandardNimMove());
           // print("Future states: ");
           // print(futureStates);
            return futureStates;
        }

        public List<State> generateFutureStatesByStandardNimMove() {
            List<State> futureStates = new ArrayList<State>();
            for (int i = 0; i<p.length; i++) {
                futureStates.addAll(generateFutureStatesByStandardNimMoveFromPile(i));
            }
            return futureStates;
        }

        public State generatePileZeroState(int pileNumber) {
            int[] newP = new int[p.length-1];
            boolean[] newCanSkipP = new boolean[p.length-1];
            int newIndex = 0;
            for (int i = 0; i < p.length; i ++) {
                if (i != pileNumber) { //skip only that one specific number
                    newP[newIndex] = p[i];
                    newCanSkipP[newIndex] = canSkipP[i];
                    newIndex++;
                }
            }
            return new State(newP, newCanSkipP);
        }

        public List<State> generateFutureStatesByStandardNimMoveFromPile(int pileNumber) {
            List<State> futureStates = new ArrayList<State>();
            futureStates.add(generatePileZeroState(pileNumber));
            int max = p[pileNumber];
            for (int i = 1; i<max; i++) {
                boolean[] futureSkips = Arrays.copyOf(canSkipP, canSkipP.length);
                int[] futureP = Arrays.copyOf(p, p.length);
                futureP[pileNumber] = i;
                State futureState = new State(futureP, futureSkips);
                futureStates.add(futureState);
            }
            return futureStates;
        }



        public List<State> generateFutureStatesBySkipping() {
            List<State> futureStates = new ArrayList<State>();
            for (int i = 0; i<canSkipP.length; i++) {
                if (canSkipP[i]) {
                    //can skip, so create a future state, flipping this skip to false, and keeping all else as they are
                    boolean[] futureSkips = Arrays.copyOf(canSkipP, canSkipP.length);
                    futureSkips[i] = false;
                    int[] futureP = Arrays.copyOf(p, p.length);
                    State futureState = new State(futureP, futureSkips);
                    futureStates.add(futureState);
                }
            }
            return futureStates;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;

            for (int i = 0; i < p.length; i++) {
                result = prime * result + p[i];
                result = prime * result + (canSkipP[i] ? 1 : 0);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof State))
                return false;
            State other = (State) obj;

            if (p.length != other.p.length || canSkipP.length != other.canSkipP.length) {
                return false;
            }
            for (int i = 0; i < p.length; i++) {
                if (p[i] != other.p[i]) return false;
                if (canSkipP[i] != other.canSkipP[i]) return false;
            }
            return true;
        }
    }





    public static boolean isWinning(State state) {
        if (stateCache.containsKey(state)) {
            //print("Cached state:");
            //print(state);
            return stateCache.get(state);
        }
        //evaluationCount++;
        //print("calculating state:");
        //print(state);


        if (state.p.length == 0) {
            stateCache.put(state, false);
            return false;
        }
        if (state.p.length == 1)  {
            stateCache.put(state, true);
            return true;
        }

        boolean noSkipsLeft = true;
        for (boolean skip : state.canSkipP) {
            if (skip) {
                noSkipsLeft = false;
                break;
            }
        }
        if (noSkipsLeft) {
            boolean result = isStandardNimWinner(state.p);
            stateCache.put(state, result);
            return result;
        }

        //optimization for n == 2
        if (state.p.length == 2 && state.p[0] == state.p[1] && state.canSkipP[0] == state.canSkipP[1]) { //either 0 or 2 skip
            stateCache.put(state,false);
            return false;
        }

        //generic case, check all possible future states, as per theory

        List<State> futureStates = state.generateFutureStates();
        for (State futureState : futureStates) {
            if (!isWinning(futureState)) {
                //you can put the game in at least one non-winning (0 sum in standard nim game) state
                stateCache.put(state, true);
                return true;
            }
        }
        //print("reached bottom");
        stateCache.put(state,false);
        return false;
    }

    /*private static boolean isLosingCached(State state) {
        for (boolean b : state.canSkipP) { //only check un-passed states
            if (!b) return false;
        }

        int[] pCopy = Arrays.copyOf(state.p, state.p.length);
        Arrays.sort(pCopy);
        for (int i = 0; i < losing.length; i++) {
            if (state.p.length == losing[i].length) {
                for (int j = 0; j < losing[i].length; j++) {
                    if (state.p[j] != losing[i][j]) break;
                }
                return true;
            }

        }
        return false;
    }*/


    public static boolean isStandardNimWinner(List<Integer> p) {
        int nimSum = 0;
        for (int p_i : p) {
            nimSum^=p_i;
        }
        return nimSum != 0;
    }

    public static boolean isStandardNimWinner(int[] p) {
        int nimSum = 0;
        for (int p_i : p) {
            nimSum^=p_i;
        }
        return nimSum != 0;
    }




    public static boolean isJohnWinner(int[] p) {
        State state = State.createInitial(p);
        //print("initial state:");
        //print(state);
        return isWinning(state);
    }


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();


        print(n);

        for (int i = 1; i < n-1; i++) {
            for (int j = i+1; j < n; j++) {
                for (int k = j+1; k <n+1; k++) {
                    for (int l = k+1; l<n+2;l++) {
                        for (int m = l + 1; l < n + 3; l++) {
                            for (int o = m + 1; l < n + 4; l++) {
                                for (int p = o + 1; l < n + 5; l++) {

                                    int[] input = {i, j, k, l, m, o, p};
                                    int xor = i ^ j ^ k ^ l ^ m ^ o ^ p;
                                    if (previousXors.contains(xor)) continue;

                                    previousXors.add(xor);
                                    if (!isJohnWinner(input)) {
                                        //print(input);
                                        uniqueXors.add(xor);
                                    }
                                }
                            }
                        }
                    }
//                        System.out.println(isJohnWinner(input) ? "W" : "L");
                    //3System.out.println()
                }
            }
        }

        print(uniqueXors);
      /*  while(true) {
            int n = 3;
            int[] p = new int[n];
            for (int p_i = 0; p_i < n; p_i++) {
                p[p_i] = in.nextInt();
            }

            System.out.println(isJohnWinner(p) ? "W" : "L");
            //print(evaluationCount);
        }*/
            // your code goes here

    }
}
