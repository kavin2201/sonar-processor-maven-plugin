package com.sonarprocessor.sonarutils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import java.util.Arrays;
import java.util.Locale;

/** SonarUtil */
public class SonarUtil {

    /**
     * getTypeDeclaration
     *
     * @param compilationUnit {CompilationUnit}
     * @return TypeDeclaration
     */
    public static TypeDeclaration getTypeDeclaration(CompilationUnit compilationUnit) {
        NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
        if (types.isNonEmpty() && types.stream().findFirst().isPresent()) {
            return types.stream().findFirst().get();
        }
        return null;
    }

    /**
     * convertString
     *
     * @param s {String}
     * @return String
     */
    public static String convertString(String s) {
        s =
                s.replace("_", " ")
                        .replace("-", " ")
                        .replace("@", " ")
                        .replace("!", " ")
                        .replace("#", " ");
        // to keep track of spaces
        int ctr = 0;
        // variable to hold the length of the string
        int n = s.length();
        // converting the string expression to character array
        char[] ch = s.toCharArray();
        // keep track of indices of ch[ ] array
        int c = 0;
        // traversing through each character of the array
        for (int i = 0; i < n; i++) {
            // The first position of the array i.e., the first letter must be
            // converted to lower case as we are following lower camel case
            // in this program
            if ( // converting to lower case using the toLowerCase( ) in-built function
            i == 0) {
                ch[i] = Character.toLowerCase(ch[i]);
            }
            // as we need to remove all the spaces in between, we check for empty
            // spaces
            if (ch[i] == ' ') {
                // incrementing the space counter by 1
                ctr++;
                // converting the letter immediately after the space to upper case
                ch[i + 1] = Character.toUpperCase(ch[i + 1]);
                // continue the loop
                continue;
            } else // if the space is not encountered simply copy the character
            {
                ch[c++] = ch[i];
            }
        }
        // The size of new string will be reduced as the spaces have been removed
        // Thus, returning the new string with new size
        return String.valueOf(ch, 0, n - ctr);
    }

    /**
     * numberToWords
     *
     * @param n {long}
     * @return String
     */
    public static String numberToWords(long n) {
        long limit = 1000000000000L, curr_hun, t = 0;
        if (n == 0) {
            return ("ZERO");
        }
        // Array to store the powers of 10
        String[] multiplier = {"", "Trillion", "Billion", "Million", "Thousand"};
        multiplier = Arrays.stream(multiplier).map(String::toUpperCase).toArray(String[]::new);
        // Array to store numbers till 20
        String[] first_twenty = {
            "",
            "One",
            "Two",
            "Three",
            "Four",
            "Five",
            "Six",
            "Seven",
            "Eight",
            "Nine",
            "Ten",
            "Eleven",
            "Twelve",
            "Thirteen",
            "Fourteen",
            "Fifteen",
            "Sixteen",
            "Seventeen",
            "Eighteen",
            "Nineteen"
        };
        first_twenty = Arrays.stream(first_twenty).map(String::toUpperCase).toArray(String[]::new);
        // Array to store multiples of ten
        String[] tens = {
            "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        };
        tens = Arrays.stream(tens).map(String::toUpperCase).toArray(String[]::new);
        if (n < 20L) {
            return (first_twenty[(int) n]);
        }
        // If number is less than 20, return without any
        String answer = "";
        for (long i = n; i > 0; i %= limit, limit /= 1000) {
            // Store the value in multiplier[t], i.e n =
            // 1000000, then r = 1, for multiplier(million),
            // 0 for multipliers(trillion and billion)
            // multiplier here refers to the current
            // accessible limit
            curr_hun = i / limit;
            // It might be possible that the current
            // multiplier is bigger than your number
            while (curr_hun == 0) {
                // Set i as the remainder obtained when n
                // was divided by the limit
                i %= limit;
                // Divide the limit by 1000, shifts the
                // multiplier
                limit /= 1000;
                // Get the current value in hundreds, as
                // English system works in hundreds
                curr_hun = i / limit;
                // Shift the multiplier
                ++t;
            }
            // If current hundred is greater than 99, Add
            // the hundreds' place
            if (curr_hun > 99) {
                answer += (first_twenty[(int) curr_hun / 100] + " Hundred ");
            }
            // Bring the current hundred to tens
            curr_hun = curr_hun % 100;
            // If the value in tens belongs to [1,19], add
            // using the first_twenty
            if (curr_hun > 0 && curr_hun < 20) {
                answer += (first_twenty[(int) curr_hun] + " ");
            } else // If curr_hun is now a multiple of 10, but not
            // 0 Add the tens' value using the tens array
            if (curr_hun % 10 == 0 && curr_hun != 0) {
                answer += (tens[(int) curr_hun / 10 - 1] + " ");
            } else // If the value belongs to [21,99], excluding
            // the multiples of 10 Get the ten's place and
            // one's place, and print using the first_twenty
            // array
            if (curr_hun > 20 && curr_hun < 100) {
                answer +=
                        (tens[(int) curr_hun / 10 - 1]
                                + " "
                                + first_twenty[(int) curr_hun % 10]
                                + " ");
            }
            // If Multiplier has not become less than 1000,
            // shift it
            if (t < 4) {
                answer += (multiplier[(int) ++t] + " ");
            }
        }
        return doCapitalize(answer);
    }

    /**
     * numberToWords
     *
     * @param number {double}
     * @return String
     */
    public static String numberToWords(double number) {
        String doubleStr = Double.toString(number);
        // Split the string into whole and fractional parts
        String[] parts = doubleStr.split("\\.");
        int wholePart = Integer.parseInt(parts[0]);
        int decimalPart = Integer.parseInt(parts[1]);
        String wholePartStr = numberToWords(wholePart);
        String decimalPartStr = numberToWords(decimalPart);
        return wholePartStr + "_DOT_" + decimalPartStr;
    }

    /**
     * doCapitalize
     *
     * @param word {String}
     * @return String
     */
    private static String doCapitalize(String word) {
        if (word != null) {
            word = word.trim().replaceAll(" ", "_").toUpperCase(Locale.ROOT);
        }
        return word;
    }
}
