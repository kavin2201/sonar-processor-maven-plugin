package com.sonarprocessor.sonarutils;

import com.github.javaparser.ast.CompilationUnit;
import com.sonarprocessor.factory.SonarProcessorFactory;

/** Navigator */
public class Navigator {

    /**
     * fix
     *
     * @param compilationUnit {CompilationUnit}
     * @param rule {String}
     */
    public void fix(CompilationUnit compilationUnit, String rule) {
        if (rule.equalsIgnoreCase(SonarProcesssorConstants.ALL)) {
            resolveAllIssues(compilationUnit);
        } else {
            SonarProcessorFactory.getInstance(rule).process(compilationUnit);
        }
    }

    /**
     * resolveAllIssues
     *
     * @param compilationUnit {CompilationUnit}
     */
    private void resolveAllIssues(CompilationUnit compilationUnit) {
        for (String rule : SonarProcesssorConstants.getAllSupportedRules.keySet()) {
            SonarProcessorFactory.getInstance(rule).process(compilationUnit);
        }
    }
}
