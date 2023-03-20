package com.sonarprocessor.main;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.google.googlejavaformat.java.FormatterException;
import com.sonarprocessor.models.S109ProcessModel;
import com.sonarprocessor.sonarutils.Navigator;
import com.sonarprocessor.sonarutils.SourceFormatter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;

/** SonarProcessor */
@Named
@Singleton
public class SonarProcessor {

    /**
     * main
     *
     * @param args {String[]}
     */
    public static void main(String[] args) {
        SonarProcessor jp = new SonarProcessor();
        if (args.length == 2) {
            String path = args[1];
            String rule = args[0];
            // jp.commit();
            jp.resolveIssues(rule, path, null);
        } else {
            System.out.println(
                    "Expected arguments are missing. Required " + "parameters : Rule and Path");
        }
    }

    /**
     * isStatic
     *
     * @param member {BodyDeclaration<?>}
     * @return boolean
     */
    private static boolean isStatic(BodyDeclaration<?> member) {
        return member instanceof InitializerDeclaration
                && ((InitializerDeclaration) member).isStatic();
    }

    /**
     * resolveIssues
     *
     * @param rule {String}
     * @param pathss {String}
     * @param progressBar1 progressBar1
     */
    public void resolveIssues(String rule, String pathss, JProgressBar progressBar1) {
        // List<File> files = new ArrayList<>();
        File file = new File(pathss);
        if (file.exists()) {
            // readFiles(files, file.listFiles());
            List<Path> files = readAllFiles(file);
            analyze(files, rule, progressBar1, pathss);
            System.out.println(
                    "============== Total processed files : "
                            + files.size()
                            + " ===================");
        } else {
            System.out.println("Invalid/No files to process in the given folder");
        }
    }

    /**
     * analyze
     *
     * @param files {List<File>}
     * @param rule {String}
     * @param path path
     */
    public void analyze(List<Path> files, String rule, String path) {
        Navigator navigator = new Navigator();
        AtomicLong index = new AtomicLong();
        files.parallelStream()
                .forEach(
                        file -> {
                            try {
                                // To check the variable caller methods
                                // CombinedTypeSolver cm = new CombinedTypeSolver();
                                // cm.add(
                                // new JavaParserTypeSolver(new File(
                                // SonarProcessor.class
                                // .getProtectionDomain()
                                // .getCodeSource()
                                // .getLocation()
                                // .toURI())
                                // .getPath()));
                                // JavaSymbolSolver javaSymbolSolver =
                                // new JavaSymbolSolver(cm);
                                JavaParserTypeSolver javaParserTypeSolver =
                                        new JavaParserTypeSolver(path);
                                JavaSymbolSolver javaSymbolSolver =
                                        new JavaSymbolSolver(javaParserTypeSolver);
                                // JarTypeSolver jarTypeSolver =
                                // new JarTypeSolver(new File(
                                // SonarProcessor.class
                                // .getProtectionDomain()
                                // .getCodeSource()
                                // .getLocation()
                                // .toURI())
                                // .getPath());
                                // JavaSymbolSolver javaSymbolSolver = new
                                // JavaSymbolSolver(jarTypeSolver);
                                // Set the parser configuration
                                ParserConfiguration parserConfiguration = new ParserConfiguration();
                                parserConfiguration.setSymbolResolver(javaSymbolSolver);
                                JavaParser jp = new JavaParser(parserConfiguration);
                                // Parsing the file and resolve the issues
                                ParseResult<CompilationUnit> cu = jp.parse(file);
                                navigator.fix(cu.getResult().get(), rule);
                                // Re-ordering the class members
                                reorder(cu.getResult().get(), file);
                                // Format and write the source code
                                String formatterSource =
                                        SourceFormatter.format(cu.getResult().get().toString());
                                write(formatterSource, file.toFile());
                            } catch (FormatterException | FileNotFoundException e) {
                                System.out.println(
                                        e.getMessage() + " ========== " + file.getFileName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    /**
     * readAllFiles
     *
     * @param file {File}
     * @return List<Path>
     */
    public List<Path> readAllFiles(File file) {
        try {
            List<Path> javaFiles =
                    Files.walk(Paths.get(file.getAbsolutePath()))
                            .filter(path -> path.toString().endsWith(".java"))
                            .collect(Collectors.toList());
            return javaFiles;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * readFiles
     *
     * @param files {List<File>}
     * @param listFiles {File[]}
     */
    public void readFiles(List<File> files, File[] listFiles) {
        for (File file : listFiles) {
            if (file.isDirectory()) {
                readFiles(files, file.listFiles());
            } else {
                if (file.getName().endsWith(".java")) {
                    files.add(file);
                }
            }
        }
    }

    /**
     * analyze
     *
     * @param files {List<File>}
     * @param rule {String}
     * @param progressBar1 progressBar1
     * @param path path
     */
    public void analyze(List<Path> files, String rule, JProgressBar progressBar1, String path) {
        Navigator navigator = new Navigator();
        AtomicLong index = new AtomicLong();
        prepareDefaultData();
        files.parallelStream()
                .forEach(
                        file -> {
                            try {
                                // To check the variable caller methods
                                // CombinedTypeSolver cm = new CombinedTypeSolver();
                                // cm.add(
                                // new JavaParserTypeSolver(new File(
                                // SonarProcessor.class
                                // .getProtectionDomain()
                                // .getCodeSource()
                                // .getLocation()
                                // .toURI())
                                // .getPath()));
                                // JavaSymbolSolver javaSymbolSolver =
                                // new JavaSymbolSolver(cm);
                                JavaParserTypeSolver javaParserTypeSolver =
                                        new JavaParserTypeSolver(path);
                                JavaSymbolSolver javaSymbolSolver =
                                        new JavaSymbolSolver(javaParserTypeSolver);
                                // JarTypeSolver jarTypeSolver =
                                // new JarTypeSolver(new File(
                                // SonarProcessor.class
                                // .getProtectionDomain()
                                // .getCodeSource()
                                // .getLocation()
                                // .toURI())
                                // .getPath());
                                // JavaSymbolSolver javaSymbolSolver = new
                                // JavaSymbolSolver(jarTypeSolver);
                                // Set the parser configuration
                                ParserConfiguration parserConfiguration = new ParserConfiguration();
                                parserConfiguration.setSymbolResolver(javaSymbolSolver);
                                JavaParser jp = new JavaParser(parserConfiguration);
                                // Parsing the file and resolve the issues
                                ParseResult<CompilationUnit> cu = jp.parse(file);
                                navigator.fix(cu.getResult().get(), rule);
                                // resolveTypeResolver(cu.getResult().get());
                                // stackTraceChange(cu.getResult().get());
                                // Re-ordering the class members
                                reorder(cu.getResult().get(), file);
                                // Format and write the source code
                                String formatterSource =
                                        SourceFormatter.format(cu.getResult().get().toString());
                                write(formatterSource, file.toFile());
                            } catch (FormatterException | FileNotFoundException e) {
                                System.out.println(
                                        e.getMessage() + " ========== " + file.getFileName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                            if (progressBar1 != null) {
                                progressBar1.setValue(
                                        (int) ((index.incrementAndGet() * 100) / files.size()));
                            }
                        });
    }

    /**
     * stackTraceChange
     *
     * @param compilationUnit {CompilationUnit}
     */
    private void stackTraceChange(CompilationUnit compilationUnit) {
        Optional<VariableDeclarator> loggerVar =
                compilationUnit.findFirst(
                        VariableDeclarator.class,
                        vd -> {
                            String type = vd.getTypeAsString();
                            return type.endsWith("Logger") && type.contains("org.slf4j");
                        });
        NameExpr loggerExpr = new NameExpr(loggerVar.get().getNameAsString());
        compilationUnit.accept(new StackTraceModifier(loggerExpr), null);
    }

    /** prepareDefaultData */
    private void prepareDefaultData() {
        S109ProcessModel.setConstantFileName("ApplicationConstants");
    }

    /**
     * resolveTypeResolver
     *
     * @param cu {CompilationUnit}
     */
    private void resolveTypeResolver(CompilationUnit cu) {
        cu.findAll(LambdaExpr.class)
                .forEach(
                        lambda -> {
                            ResolvedType targetType = lambda.calculateResolvedType();
                            ResolvedTypeParameterDeclaration functionalInterfaceType =
                                    targetType.asTypeParameter();
                            List<ResolvedType> parameterTypes =
                                    Arrays.asList(functionalInterfaceType.getLowerBound());
                            for (int i = 0; i < lambda.getParameters().size(); i++) {
                                Parameter param = lambda.getParameters().get(i);
                                Type type = param.getType();
                                try {
                                    type = (Type) type.resolve();
                                } catch (UnsolvedSymbolException e) {
                                    JavaParser javaParser = new JavaParser();
                                    ParseResult<Type> parseResult =
                                            javaParser.parseType(parameterTypes.get(i).describe());
                                    type = parseResult.getResult().get();
                                }
                                System.out.println("Resolved type: " + type);
                            }
                        });
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
//        nodeList.sort(Comparator.comparing(SonarProcessor::isStatic).reversed());
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
        // Collections.sort(fields, (f1, f2) ->
        // f1.getVariables().get(0).getNameAsString().compareTo(f2.getVariables().get(0).getNameAsString()));
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
            System.out.println(formatterSource);
            // Closing the file writing connection
            fWriter.close();
        } catch (Exception e) {
            System.out.println("Exception on file writing : " + e.getMessage());
        }
    }

    /** commit */
    private void commit() {
        // UsernamePasswordCredentialsProvider credentialsProvider =
        // new UsernamePasswordCredentialsProvider("p.kavinraj@gmail.com",
        // "Kavin@2201");
        // Git.lsRemoteRepository();
    }

    /** StackTraceModifier */
    public class StackTraceModifier extends ModifierVisitor<Void> {

        private final NameExpr loggerExpr;

        public StackTraceModifier(NameExpr loggerExpr) {
            this.loggerExpr = loggerExpr;
        }

        /**
         * visit
         *
         * @param n {MethodCallExpr}
         * @param arg {Void}
         * @return Visitable
         */
        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            if (n.getNameAsString().equals("printStackTrace")) {
                MethodCallExpr loggerCall = new MethodCallExpr(loggerExpr, "severe");
                loggerCall.addArgument(n.getArgument(0));
                return new ExpressionStmt(loggerCall);
            }
            return super.visit(n, arg);
        }
    }
}
