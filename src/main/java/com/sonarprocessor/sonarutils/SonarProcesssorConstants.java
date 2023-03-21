package com.sonarprocessor.sonarutils;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** SonarProcesssorConstants */
public class SonarProcesssorConstants {

    /** SP COMMENT : S1176 */
    public static final String S1176 = "S1176";

    /** SP COMMENT : S2039 */
    public static final String S2039 = "S2039";

    /** SP COMMENT : ALL */
    public static final String ALL = "ALL";

    /** SP COMMENT : S121 */
    public static final String S121 = "S121";

    /** SP COMMENT : getSupportedRules */
    public static final List<String> getSupportedRules = Arrays.asList(S2039, S1176, S121);

    /** SP COMMENT : S1444 */
    public static final String S1444 = "S1444";

    /** SP COMMENT : S117 */
    public static final String S117 = "S117";

    /** SP COMMENT : S138 */
    public static final String S138 = "S138";

    /** SP COMMENT : S109 */
    public static final String S109 = "S109";

    /** SP COMMENT : getAllSupportedRules */
    public static final Map<String, String> // .put(S109, "Magic numbers should not be used")
            getAllSupportedRules =
                    ImmutableMap.<String, String>builder()
                            .put(
                                    S2039,
                                    "It will make class fields as private, if access "
                                            + "specifiers not found for the field")
                            .put(S121, "Control " + "structures should use curly braces (If loops)")
                            .put(S138, "Methods should not have too many lines")
                            .put(
                                    S117,
                                    "Local variable and method parameter names should comply with a naming convention")
                            .put(S1444, "\"public static\" fields should be constant")
                            .put(
                                    S1176,
                                    "Public types, methods and fields (API) should be "
                                            + "documented with Javadoc")
                            .build();

    /** SP COMMENT : RULE_SEPARATE */
    public static final String RULE_SEPARATE = " - ";

    /** SP COMMENT : CONSTANT_APPEND_STRING */
    public static final String CONSTANT_APPEND_STRING = "SP_CONST_";

    /** SP COMMENT : DOT */
    public static final String DOT = ".";

    /** SP COMMENT : FOUND_UNCOMPILED */
    public static final String FOUND_UNCOMPILED =
            "Found %d uncompiled/modified files in %s to reformat.";

    /** SP COMMENT : ERROR_SCANNING_PATH */
    public static final String ERROR_SCANNING_PATH =
            "Error scanning source path: '%s' for  files to reformat.";

    /** SP COMMENT : DIRECTORY_MISSING */
    public static final String DIRECTORY_MISSING =
            "Directory %s does not exist, skipping file collection.";
}
