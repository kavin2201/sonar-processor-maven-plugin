package com.sonarprocessor.sonarprocessor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.sonarprocessor.interfaces.IProcessor;
import com.sonarprocessor.models.S109ProcessModel;
import com.sonarprocessor.sonarutils.SonarProcesssorConstants;
import com.sonarprocessor.sonarutils.SonarUtil;

import java.util.HashMap;
import java.util.Map;

/** S109IssueProcessor */
public class S109IssueProcessor implements IProcessor {

    private Map<Object, String> singleTonMap = new HashMap<>();

    /**
     * process
     *
     * @param compilationUnit {CompilationUnit}
     */
    @Override
    public void process(CompilationUnit compilationUnit) {
        TypeDeclaration typeDeclaration = SonarUtil.getTypeDeclaration(compilationUnit);
        if (typeDeclaration == null) {
            return;
        }
        if (typeDeclaration instanceof EnumDeclaration) {
            enumNamingConvention(typeDeclaration, compilationUnit);
        }
        checkNamingConvention(typeDeclaration, compilationUnit);
    }

    /**
     * checkNamingConvention
     *
     * @param typeDeclaration {TypeDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void checkNamingConvention(
            TypeDeclaration typeDeclaration, CompilationUnit compilationUnit) {
        NodeList<BodyDeclaration> bodyDeclarations = typeDeclaration.getMembers();
        bodyDeclarations.stream()
                .forEach(
                        member -> {
                            if (member instanceof MethodDeclaration) {
                                changeMagicNumbers((MethodDeclaration) member, compilationUnit);
                            } else if (member instanceof ClassOrInterfaceDeclaration) {
                                checkAndAddNamingConventionClsorI(member, compilationUnit);
                            } else if (member instanceof EnumDeclaration) {
                                enumNamingConvention(member, compilationUnit);
                            }
                        });
    }

    /**
     * enumNamingConvention
     *
     * @param member {TypeDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void enumNamingConvention(TypeDeclaration member, CompilationUnit compilationUnit) {
        EnumDeclaration enumDeclaration = (EnumDeclaration) member;
        checkNamingConvention(enumDeclaration, compilationUnit);
    }

    /**
     * enumNamingConvention
     *
     * @param member {BodyDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void enumNamingConvention(BodyDeclaration member, CompilationUnit compilationUnit) {
        EnumDeclaration enumDeclaration = (EnumDeclaration) member;
        checkNamingConvention(enumDeclaration, compilationUnit);
    }

    /**
     * checkNamingConvention
     *
     * @param enumDeclaration {EnumDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void checkNamingConvention(
            EnumDeclaration enumDeclaration, CompilationUnit compilationUnit) {
        NodeList<EnumConstantDeclaration> entries = enumDeclaration.getEntries();
        for (EnumConstantDeclaration entry : entries) {
            NodeList<BodyDeclaration<?>> classBody = entry.getClassBody();
            classBody.stream()
                    .forEach(
                            bodyDeclaration -> {
                                if (bodyDeclaration instanceof MethodDeclaration) {
                                    changeMagicNumbers(
                                            (MethodDeclaration) bodyDeclaration, compilationUnit);
                                } else if (bodyDeclaration instanceof ClassOrInterfaceDeclaration) {
                                    checkAndAddNamingConventionClsorI(
                                            bodyDeclaration, compilationUnit);
                                }
                            });
        }
    }

    /**
     * checkAndAddNamingConventionClsorI
     *
     * @param member {BodyDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void checkAndAddNamingConventionClsorI(
            BodyDeclaration member, CompilationUnit compilationUnit) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                (ClassOrInterfaceDeclaration) member;
        NodeList<BodyDeclaration<?>> bodyDeclarations = classOrInterfaceDeclaration.getMembers();
        bodyDeclarations.stream()
                .forEach(
                        innerMember -> {
                            if (innerMember instanceof MethodDeclaration) {
                                changeMagicNumbers(
                                        (MethodDeclaration) innerMember, compilationUnit);
                            } else if (innerMember instanceof ClassOrInterfaceDeclaration) {
                                checkAndAddNamingConventionClsorI(innerMember, compilationUnit);
                            }
                        });
    }

    /**
     * changeMagicNumbers
     *
     * @param methodDeclaration {MethodDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void changeMagicNumbers(
            MethodDeclaration methodDeclaration, CompilationUnit compilationUnit) {
        MagicNumberVisitor magicNumberVisitor = new MagicNumberVisitor();
        magicNumberVisitor.visit(methodDeclaration, null);
    }

    class MagicNumberVisitor extends VoidVisitorAdapter<Void> {

        /**
         * visit
         *
         * @param n {IntegerLiteralExpr}
         * @param arg {Void}
         */
        @Override
        public void visit(IntegerLiteralExpr n, Void arg) {
            int value = n.asInt();
            if (isMagicNumber(value)) {
                if (S109ProcessModel.containsKey(value)) {
                    n.replace(new NameExpr(S109ProcessModel.get(value)));
                } else {
                    // Replace magic number with a constant or a variable
                    String generatedValue =
                            S109ProcessModel.getConstantFileName()
                                    + SonarProcesssorConstants.DOT
                                    + SonarProcesssorConstants.CONSTANT_APPEND_STRING
                                    + SonarUtil.numberToWords(value);
                    n.replace(new NameExpr(generatedValue));
                    S109ProcessModel.put(value, generatedValue);
                }
            }
        }

        /**
         * visit
         *
         * @param n {DoubleLiteralExpr}
         * @param arg {Void}
         */
        @Override
        public void visit(DoubleLiteralExpr n, Void arg) {
            double value = n.asDouble();
            boolean isFloat = n.toString().endsWith("f") || n.toString().endsWith("F");
            if (S109ProcessModel.containsKey(value)) {
                n.replace(new NameExpr(S109ProcessModel.get(value)));
            } else {
                // Replace magic number with a constant or a variable
                String generatedValue =
                        S109ProcessModel.getConstantFileName()
                                + SonarProcesssorConstants.DOT
                                + SonarProcesssorConstants.CONSTANT_APPEND_STRING
                                + (isFloat ? "FLOAT_" : "DOUBLE_")
                                + SonarUtil.numberToWords(value);
                n.replace(new NameExpr(generatedValue));
                S109ProcessModel.put(value, generatedValue);
            }
        }

        /**
         * visit
         *
         * @param n {LongLiteralExpr}
         * @param arg {Void}
         */
        @Override
        public void visit(LongLiteralExpr n, Void arg) {
            long value = n.asLong();
            if (S109ProcessModel.containsKey(value)) {
                n.replace(new NameExpr(S109ProcessModel.get(value)));
            } else {
                // Replace magic number with a constant or a variable
                String generatedValue =
                        S109ProcessModel.getConstantFileName()
                                + SonarProcesssorConstants.DOT
                                + SonarProcesssorConstants.CONSTANT_APPEND_STRING
                                + "LONG_"
                                + SonarUtil.numberToWords(value);
                n.replace(new NameExpr(generatedValue));
                S109ProcessModel.put(value, generatedValue);
            }
        }

        // float a = 1.0f;
        // float ab = 1f;
        // double aa = 1;
        // double aaa = 1.2222;
        // double ed = 313.1232;
        // long asd = 324423;
        // long sads = 3213L;
        private boolean isMagicNumber(int value) {
            // TODO: implement logic to check if the value is a magic number
            return true;
        }
    }
}
