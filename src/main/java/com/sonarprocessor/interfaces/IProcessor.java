package com.sonarprocessor.interfaces;

import com.github.javaparser.ast.CompilationUnit;

/**
 * IProcessor
 */
public interface IProcessor {

    /**
     * process
     *
     * @param compilationUnit {TypeDeclaration}
     */
    void process(CompilationUnit compilationUnit);
}
