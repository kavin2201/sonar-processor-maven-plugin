package com.sonarprocessor.factory;

import com.sonarprocessor.interfaces.IProcessor;
import com.sonarprocessor.sonarprocessor.*;
import com.sonarprocessor.sonarutils.SonarProcesssorConstants;

/** SonarProcessorFactory */
public class SonarProcessorFactory {

    /**
     * getInstance
     *
     * @param rule {String}
     * @return IProcessor
     */
    public static IProcessor getInstance(String rule) {
        IProcessor navigator = null;
        if (rule.equalsIgnoreCase(SonarProcesssorConstants.S1176)) {
            navigator = new S1176IssueProcessor();
        } else if (rule.equalsIgnoreCase(SonarProcesssorConstants.S2039)) {
            navigator = new S2039IssueProcessor();
        } else if (rule.equalsIgnoreCase(SonarProcesssorConstants.S121)) {
            navigator = new S121IssueProcessor();
        } else if (rule.equalsIgnoreCase(SonarProcesssorConstants.S117)) {
            navigator = new S117IssueProcessor();
        } else if (rule.equalsIgnoreCase(SonarProcesssorConstants.S1444)) {
            navigator = new S1444IssueProcessor();
        } else if (rule.equalsIgnoreCase(SonarProcesssorConstants.S138)) {
            navigator = new S138IssueProcessor();
        } else if (rule.equalsIgnoreCase(SonarProcesssorConstants.S109)) {
            navigator = new S109IssueProcessor();
        }
        return navigator;
    }
}
