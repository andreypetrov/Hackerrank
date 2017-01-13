import java.util.List;

/**
 * Created by user1 on 16-12-23.
 */
public class U {
    public static void print(long[] input) {
        System.out.print("[");
        for (long i:input) {
            System.out.print(i + ", ");
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

    public static void print(long input) {
        System.out.println(input);
    }

    public static void print(int input) {
        System.out.println(input);
    }

    public static void print(String input) {
        System.out.println(input);
    }

    public static void print(int[][] input) {
        for (int i = 0; i < input.length; i++) {
            System.out.print("[");
            for (int j = 0; j < input[i].length; j++) {
                int next = input[i][j];
                System.out.print(next + ", ");
            }
            System.out.println("]");
        }
    }

    public static void print(boolean[][] input) {
        for (int i = 0; i < input.length; i++) {
            System.out.print("[");
            for (int j = 0; j < input[i].length; j++) {
                boolean next = input[i][j];
                System.out.print((next ? "1":"0") + ", ");
            }
            System.out.println("]");
        }
    }

    public static void print(String[][] input) {
        for (int i = 0; i < input.length; i++) {
            System.out.print("[");
            for (int j = 0; j < input[i].length; j++) {
                System.out.print(input[i][j] + ", ");
            }
            System.out.println("]");
        }
    }

    public static void print(String[] input) {
        System.out.print("[");
        for (String i:input) {
            System.out.print(i + ", ");
        }
        System.out.println("]");
    }


    public static void print(Integer[] input) {
        System.out.print("[");
        for (Integer i:input) {
            System.out.print(i + ", ");
        }
        System.out.println("]");

    }
}
