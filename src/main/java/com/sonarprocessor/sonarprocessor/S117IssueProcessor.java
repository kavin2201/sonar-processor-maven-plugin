package com.sonarprocessor.sonarprocessor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.sonarprocessor.interfaces.IProcessor;
import com.sonarprocessor.sonarutils.SonarUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** S117IssueProcessor */
public class S117IssueProcessor implements IProcessor {

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
                                checkAndAddNamingConvention(member, compilationUnit);
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
                                    checkAndAddNamingConvention(bodyDeclaration, compilationUnit);
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
                                checkAndAddNamingConvention(innerMember, compilationUnit);
                            } else if (innerMember instanceof ClassOrInterfaceDeclaration) {
                                checkAndAddNamingConventionClsorI(innerMember, compilationUnit);
                            }
                        });
    }

    /**
     * checkAndAddNamingConvention
     *
     * @param member {BodyDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void checkAndAddNamingConvention(
            BodyDeclaration member, CompilationUnit compilationUnit) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) member;
        List<NameExpr> blockStatements = methodDeclaration.findAll(NameExpr.class);
        for (NameExpr nameExpr : blockStatements) {
            try {
                ResolvedValueDeclaration resolvedParameter = nameExpr.resolve();
                if (resolvedParameter.isParameter()
                        && !isLowerCamelCase(resolvedParameter.getName())) {
                    nameExpr.setName(SonarUtil.convertString(String.valueOf(nameExpr.getName())));
                }
                // if (resolvedParameter.isVariable() && isNotFinal(resolvedParameter) &&
                // !isLowerCamelCase(resolvedParameter.getName())) {
                // nameExpr.setName(SourceFormatter.convertString(String.valueOf(nameExpr.getName())));
                // }
            } catch (Exception e) {
                System.out.println("Exception on resolve fields");
            }
        }
        for (Parameter param : methodDeclaration.findAll(Parameter.class)) {
            if (!isLowerCamelCase(param.getNameAsString())) {
                param.setName(SonarUtil.convertString(String.valueOf(param.getNameAsString())));
            }
        }
        // localVariableRenaming(methodDeclaration, compilationUnit);
    }

    /**
     * localVariableRenaming
     *
     * @param methodDeclaration {MethodDeclaration}
     * @param compilationUnit {CompilationUnit}
     */
    private void localVariableRenaming(
            MethodDeclaration methodDeclaration, CompilationUnit compilationUnit) {
        // Find all the VariableDeclarationExpr nodes in the current method
        List<VariableDeclarationExpr> vars =
                methodDeclaration.findAll(VariableDeclarationExpr.class);
        // Find all FieldDeclaration nodes in the AST
        List<FieldDeclaration> fields = compilationUnit.findAll(FieldDeclaration.class);
        // Create a set to store the names of the global variables
        Set<String> globalVars = new HashSet<>();
        // Loop over all the fields
        for (FieldDeclaration field : fields) {
            // Find all the VariableDeclarator nodes in the current field
            NodeList<VariableDeclarator> globalFields = field.getVariables();
            // Add the names of the variables to the set of global variables
            globalFields.forEach(var -> globalVars.add(var.getNameAsString()));
        }
        // Loop over all the variable declarations
        for (VariableDeclarationExpr varDecl : vars) {
            // Find all the VariableDeclarator nodes in the current variable declaration
            NodeList<VariableDeclarator> varsInDecl = varDecl.getVariables();
            // Loop over all the variables in the current declaration
            for (int i = 0; i < varsInDecl.size(); i++) {
                VariableDeclarator var = varsInDecl.get(i);
                ResolvedValueDeclaration resolvedVariable = var.resolve();
                // Check if the current variable is final
                if (!resolvedVariable.isField()
                        && !resolvedVariable.isEnumConstant()
                        && !resolvedVariable.isParameter()
                        && !varDecl.isFinal()
                        && !isLowerCamelCase(var.getNameAsString())) {
                    String oldName = var.getNameAsString();
                    String newName = SonarUtil.convertString(String.valueOf(var.getNameAsString()));
                    // Rename the current variable
                    var.setName(newName);
                    // Replace all usages of the old name with the new name
                    methodDeclaration.findAll(NameExpr.class).stream()
                            .filter(
                                    ne ->
                                            !ne.resolve().isField()
                                                    && !ne.resolve().isEnumConstant()
                                                    && !ne.resolve().isParameter()
                                                    && !isLowerCamelCase(var.getNameAsString())
                                                    && ne.getNameAsString().equals(oldName))
                            .forEach(ne -> ne.setName(newName));
                }
            }
        }
    }

    /**
     * isNotFinal
     *
     * @param resolvedParameter {ResolvedValueDeclaration}
     * @return boolean
     */
    private boolean isNotFinal(ResolvedValueDeclaration resolvedParameter) {
        return false;
    }

    /**
     * isLowerCamelCase
     *
     * @param name {String}
     * @return boolean
     */
    private boolean isLowerCamelCase(String name) {
        return Pattern.matches("^[a-z][a-zA-Z0-9]*$", name);
    }
}
