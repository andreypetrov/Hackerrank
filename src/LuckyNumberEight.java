import java.util.Scanner;

/**
 * Created by Andrey Petrov on 17-01-12.
 */
public class LuckyNumberEight {
    String[] two = {"16", "24", "32", "40", "48", "56", "64", "72", "80", "88", "96"};
    String[] twoZeroIndexed = {"000", "008", "016", "024", "032", "040", "048", "056", "064", "072", "080", "088", "096"};
    String[] three = {"104", "112", "120", "128", "136", "144", "152", "160", "168", "176", "184", "192", "200",
            "208", "216", "224", "232", "240", "248", "256", "264", "272", "280", "288", "296",
            "304", "312", "320", "328", "336", "344", "352", "360", "368", "376", "384", "392", "400",
            "408", "416", "424", "432", "440", "448", "456", "464", "472", "480", "488", "496",
            "504", "512", "520", "528", "536", "544", "552", "560", "568", "576", "584", "592", "600",
            "608", "616", "624", "632", "640", "648", "656", "664", "672", "680", "688", "696",
            "704", "712", "720", "728", "736", "744", "752", "760", "768", "776", "784", "792", "800",
            "808", "816", "824", "832", "840", "848", "856", "864", "872", "880", "888", "896",
            "904", "912", "920", "928", "936", "944", "952", "960", "968", "976", "984", "992"};


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        //int n = in.nextInt();
        String number = in.next();
        // your code goes here


        System.out.println(number);
    }

    public static long countOneTwoAndThrees() {

        return 0;
    }


}
