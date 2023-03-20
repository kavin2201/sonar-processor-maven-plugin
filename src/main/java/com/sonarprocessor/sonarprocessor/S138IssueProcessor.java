package com.sonarprocessor.sonarprocessor;

import com.github.javaparser.ast.CompilationUnit;
import com.sonarprocessor.interfaces.IProcessor;

/** S138IssueProcessor */
public class S138IssueProcessor implements IProcessor {

    /**
     * process
     *
     * @param compilationUnit {CompilationUnit}
     */
    @Override
    public void process(CompilationUnit compilationUnit) {
        System.out.println("Auto processed by formatting");
    }
}
