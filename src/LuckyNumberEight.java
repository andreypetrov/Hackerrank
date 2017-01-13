import java.util.*;

/**
 * Created by Andrey Petrov on 17-01-12.
 */
public class LuckyNumberEight {
    public static String[] two = {"16", "24", "32", "40", "48", "56", "64", "72", "80", "88", "96"};
    public static String[] three = {"104", "112", "120", "128", "136", "144", "152", "160", "168", "176", "184", "192", "200",
            "208", "216", "224", "232", "240", "248", "256", "264", "272", "280", "288", "296",
            "304", "312", "320", "328", "336", "344", "352", "360", "368", "376", "384", "392", "400",
            "408", "416", "424", "432", "440", "448", "456", "464", "472", "480", "488", "496",
            "504", "512", "520", "528", "536", "544", "552", "560", "568", "576", "584", "592", "600",
            "608", "616", "624", "632", "640", "648", "656", "664", "672", "680", "688", "696",
            "704", "712", "720", "728", "736", "744", "752", "760", "768", "776", "784", "792", "800",
            "808", "816", "824", "832", "840", "848", "856", "864", "872", "880", "888", "896",
            "904", "912", "920", "928", "936", "944", "952", "960", "968", "976", "984", "992"};
    public static String[] threeAll = {"000", "008", "016", "024", "032", "040", "048", "056", "064", "072", "080", "088", "096",
            "104", "112", "120", "128", "136", "144", "152", "160", "168", "176", "184", "192", "200",
            "208", "216", "224", "232", "240", "248", "256", "264", "272", "280", "288", "296",
            "304", "312", "320", "328", "336", "344", "352", "360", "368", "376", "384", "392", "400",
            "408", "416", "424", "432", "440", "448", "456", "464", "472", "480", "488", "496",
            "504", "512", "520", "528", "536", "544", "552", "560", "568", "576", "584", "592", "600",
            "608", "616", "624", "632", "640", "648", "656", "664", "672", "680", "688", "696",
            "704", "712", "720", "728", "736", "744", "752", "760", "768", "776", "784", "792", "800",
            "808", "816", "824", "832", "840", "848", "856", "864", "872", "880", "888", "896",
            "904", "912", "920", "928", "936", "944", "952", "960", "968", "976", "984", "992"};

    public static Set<String> twoSet = new HashSet<String>(Arrays.asList(two));
    public static Set<String> threeSet = new HashSet<String>(Arrays.asList(three));
    public static Set<String> threeAllSet = new HashSet<String>(Arrays.asList(three));

    public static void main(String[] args) {


        Scanner in = new Scanner(System.in);
        //int n = in.nextInt();
        String number = in.next();
        // your code goes here
        long count = countOneDigitNumbers(number) + countTwoDigitNumbers(number) + countThreeDigitNumbers(number);

        Integer[] powersOfTwo = powersOfTwo(1000000007);
        U.print(powersOfTwo);

        System.out.println(count);
    }

    public static long countFourAndMoreDigitNumbers(String number) {
        long count = 0;

        long countOfThrees = 0;
        long degreeOf2 = 1;
        //count three digit numbers

        //start from second element as we have already counted the pure three numbers, and we look for
        for (int i = 1; i < number.length()-2; i++) {
            //i is index but is also the count of elements before the given;
            String threeDigit = new StringBuilder().append(number.charAt(i)).append(number.charAt(i+1)).append(number.charAt(i+2)).toString();
            if (threeAllSet.contains(threeDigit)) {
                countOfThrees++;
                degreeOf2*=i;
            }
        }


        return 0;
    }

    public static Integer[] powersOfTwo(long number) {
        List<Integer> powersOf2 = new ArrayList<Integer>();
        for (int currentPow = 1; currentPow != 0; currentPow <<= 1)
        {
            if ((currentPow & number) != 0)
                powersOf2.add(currentPow);
        }

        Integer[] array = new Integer[powersOf2.size()];
        powersOf2.toArray(array);
        return array;
    }

    public static long countOneDigitNumbers(String number) { //the only one digit number is 8
        long count = 0;
        for (int i = 0; i < number.length(); i++) {
            if (number.charAt(i) == '8') count++;
        }
        return count;
    }

    public static long countTwoDigitNumbers(String number) {
        long count = 0;
        for (int i = 0; i < number.length()-1; i++) {
            String twoDigit = new StringBuilder().append(number.charAt(i)).append(number.charAt(i+1)).toString();
            if (twoSet.contains(twoDigit)) count++;
        }
        return count;
    }

    public static long countThreeDigitNumbers(String number) {
        long count = 0;
        for (int i = 0; i < number.length()-2; i++) {
            String threeDigit = new StringBuilder().append(number.charAt(i)).append(number.charAt(i+1)).append(number.charAt(i+2)).toString();
            if (threeSet.contains(threeDigit)) count++;
        }
        return count;
    }



}
