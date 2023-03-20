package com.sonarprocessor.sonarutils;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.RemoveUnusedImports;

/** SourceFormatter */
public class SourceFormatter {

    /**
     * format
     *
     * @param unformattedCode {String}
     * @return String
     * @throws FormatterException {FormatterException}
     */
    public static String format(String unformattedCode) throws FormatterException {
        unformattedCode = RemoveUnusedImports.removeUnusedImports(unformattedCode);
        String formattedSource =
                new Formatter(
                                JavaFormatterOptions.builder()
                                        .style(JavaFormatterOptions.Style.AOSP)
                                        .build())
                        .formatSourceAndFixImports(unformattedCode);
        return formattedSource;
    }
}
