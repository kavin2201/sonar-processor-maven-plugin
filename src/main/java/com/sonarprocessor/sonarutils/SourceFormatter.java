package com.sonarprocessor.sonarutils;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import com.sonarprocessor.models.SonarProcessorModel;

/** SourceFormatter */
public class SourceFormatter {

    /**
     * format
     *
     * @param unformattedCode {String}
     * @param sonarProcessorModel sonarProcessorModel
     * @return String
     * @throws FormatterException {FormatterException}
     */
    public static String format(String unformattedCode, SonarProcessorModel sonarProcessorModel)
            throws FormatterException {
        unformattedCode = RemoveUnusedImports.removeUnusedImports(unformattedCode);
        Formatter formatter =
                new Formatter(
                        JavaFormatterOptions.builder()
                                .style(JavaFormatterOptions.Style.AOSP)
                                .build());
        String formattedSource;
        if (Boolean.TRUE.equals(sonarProcessorModel.getFormatImport())) {
            formattedSource = formatter.formatSourceAndFixImports(unformattedCode);
        } else {
            formattedSource = formatter.formatSource(unformattedCode);
        }
        return formattedSource;
    }
}
