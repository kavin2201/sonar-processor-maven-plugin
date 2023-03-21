package com.sonarprocessor.main;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.google.googlejavaformat.java.FormatterException;
import com.sonarprocessor.models.S109ProcessModel;
import com.sonarprocessor.models.SonarProcessorModel;
import com.sonarprocessor.sonarutils.Navigator;
import com.sonarprocessor.sonarutils.SourceFormatter;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** SonarProcessor */
@Named
@Singleton
public class SonarProcessor {

    /** prepareDefaultData */
    private void prepareDefaultData() {
        S109ProcessModel.setConstantFileName("ApplicationConstants");
    }

    /**
     * reorder
     *
     * @param unOrderedCompilationUnit {CompilationUnit}
     * @param file file
     * @return CompilationUnit
     */
    private void reorder(CompilationUnit unOrderedCompilationUnit, Path file) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                unOrderedCompilationUnit
                        .getClassByName(file.getFileName().toString().replace(".java", ""))
                        .get();
        fieldSorting(classOrInterfaceDeclaration);
        NodeList<BodyDeclaration<?>> nodeList = classOrInterfaceDeclaration.getMembers();
        nodeList.sort(
                Comparator.comparing(
                        member -> {
                            if (member.isFieldDeclaration()) {
                                return 0;
                            } else if (member.isConstructorDeclaration()) {
                                return 1;
                            } else if (member.isMethodDeclaration()) {
                                return 2;
                            } else {
                                return 3;
                            }
                        }));
        // nodeList.sort(Comparator.comparing(SonarProcessor::isStatic).reversed());
        classOrInterfaceDeclaration.setMembers(nodeList);
    }

    /**
     * fieldSorting
     *
     * @param classOrInterfaceDeclaration {ClassOrInterfaceDeclaration}
     */
    private void fieldSorting(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<FieldDeclaration> fields = new ArrayList<>(classOrInterfaceDeclaration.getFields());
        if (!fields.isEmpty()) {
            fields.sort(
                    Comparator.comparing(
                            fieldDeclaration -> {
                                if ((fieldDeclaration
                                                        .getModifiers()
                                                        .contains(Modifier.privateModifier())
                                                || fieldDeclaration
                                                        .getModifiers()
                                                        .contains(Modifier.publicModifier()))
                                        && fieldDeclaration
                                                .getModifiers()
                                                .contains(Modifier.finalModifier())
                                        && fieldDeclaration
                                                .getModifiers()
                                                .contains(Modifier.staticModifier())) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            }));
        }
        classOrInterfaceDeclaration.getMembers().removeAll(classOrInterfaceDeclaration.getFields());
        // Add the sorted fields one by one
        for (FieldDeclaration field : fields) {
            classOrInterfaceDeclaration.addMember(field);
        }
    }

    /**
     * write
     *
     * @param formatterSource {String}
     * @param file {File}
     */
    private void write(String formatterSource, File file) {
        try {
            new PrintWriter(file.getAbsoluteFile()).close();
            Writer fWriter =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(file), StandardCharsets.UTF_8));
            // Writing into file
            // Note: The content taken above inside the
            // string
            fWriter.write(formatterSource);
            // Printing the contents of a file
            // System.out.println(formatterSource);
            // Closing the file writing connection
            fWriter.close();
        } catch (Exception e) {
            System.out.println("Exception on file writing : " + e.getMessage());
        }
    }

    /**
     * analyze
     *
     * @param sonarProcessorModel {SonarProcessorModel}
     */
    public void analyze(SonarProcessorModel sonarProcessorModel) {
        Navigator navigator = new Navigator();
        prepareDefaultData();
        sonarProcessorModel
                .getFiles()
                .parallelStream()
                .forEach(
                        file -> {
                            try {
                                JavaParserTypeSolver javaParserTypeSolver =
                                        new JavaParserTypeSolver(sonarProcessorModel.getPath());
                                JavaSymbolSolver javaSymbolSolver =
                                        new JavaSymbolSolver(javaParserTypeSolver);

                                // Set the parser configuration
                                ParserConfiguration parserConfiguration = new ParserConfiguration();
                                parserConfiguration.setSymbolResolver(javaSymbolSolver);
                                JavaParser jp = new JavaParser(parserConfiguration);
                                // Parsing the file and resolve the issues
                                ParseResult<CompilationUnit> cu = jp.parse(file);
                                navigator.fix(cu.getResult().get(), sonarProcessorModel.getRule());

                                // Re-ordering the class members
                                reorder(cu.getResult().get(), file);
                                // Format and write the source code
                                String formatterSource = cu.getResult().get().toString();
                                if (Boolean.TRUE.equals(sonarProcessorModel.getFormat())) {
                                    // Format and write the source code
                                    formatterSource =
                                            SourceFormatter.format(
                                                    cu.getResult().get().toString(),
                                                    sonarProcessorModel);
                                }
                                write(formatterSource, file.toFile());
                            } catch (FormatterException | FileNotFoundException e) {
                                System.out.println(
                                        e.getMessage() + " ========== " + file.getFileName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        });
    }
}
