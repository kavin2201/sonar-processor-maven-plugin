package com.sonarprocessor.sonarprocessor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.sonarprocessor.interfaces.IProcessor;
import com.sonarprocessor.sonarutils.SonarUtil;

import java.util.List;

/** S1444IssueProcessor */
public class S1444IssueProcessor implements IProcessor {

    /**
     * process
     *
     * @param compilationUnit {TypeDeclaration}
     */
    public void process(CompilationUnit compilationUnit) {
        TypeDeclaration typeDeclaration = SonarUtil.getTypeDeclaration(compilationUnit);
        if (typeDeclaration == null) {
            return;
        }
        System.out.println("============== S1444IssueProcessor started ===================");
        addFinalModifiersToField(typeDeclaration);
        System.out.println("============== S1444IssueProcessor end " + "===================");
    }

    /**
     * addFinalModifiersToField
     *
     * @param typeDeclaration {TypeDeclaration}
     */
    private void addFinalModifiersToField(TypeDeclaration typeDeclaration) {
        List<BodyDeclaration> members = typeDeclaration.getMembers();
        members.stream()
                .forEach(
                        member -> {
                            if (member instanceof FieldDeclaration) {
                                addFieldModifiers(member);
                            } else if (member instanceof ClassOrInterfaceDeclaration) {
                                addClassOrInterfaceModifiers(member);
                            } else if (member instanceof EnumDeclaration) {
                                addEnumModifiers(member);
                            }
                        });
    }

    /**
     * addClassOrInterfaceModifiers
     *
     * @param member {BodyDeclaration}
     */
    private void addClassOrInterfaceModifiers(BodyDeclaration member) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                (ClassOrInterfaceDeclaration) member;
        NodeList<BodyDeclaration<?>> innerClassMembers = classOrInterfaceDeclaration.getMembers();
        innerClassMembers.stream()
                .forEach(
                        innerClassMember -> {
                            if (innerClassMember instanceof FieldDeclaration) {
                                addFieldModifiers(innerClassMember);
                            } else if (innerClassMember instanceof ClassOrInterfaceDeclaration) {
                                addClassOrInterfaceModifiers(innerClassMember);
                            } else if (innerClassMember instanceof EnumDeclaration) {
                                addEnumModifiers(innerClassMember);
                            }
                        });
    }

    /**
     * addEnumModifiers
     *
     * @param member {BodyDeclaration}
     */
    private void addEnumModifiers(BodyDeclaration member) {
        EnumDeclaration classOrInterfaceDeclaration = (EnumDeclaration) member;
        NodeList<BodyDeclaration<?>> innerClassMembers = classOrInterfaceDeclaration.getMembers();
        innerClassMembers.stream()
                .forEach(
                        innerClassMember -> {
                            if (innerClassMember instanceof FieldDeclaration) {
                                addFieldModifiers(innerClassMember);
                            } else if (innerClassMember instanceof ClassOrInterfaceDeclaration) {
                                addClassOrInterfaceModifiers(innerClassMember);
                            } else if (innerClassMember instanceof EnumDeclaration) {
                                addEnumModifiers(innerClassMember);
                            }
                        });
    }

    /**
     * addFieldModifiers
     *
     * @param member {BodyDeclaration}
     */
    private void addFieldModifiers(BodyDeclaration member) {
        FieldDeclaration fieldDeclaration = (FieldDeclaration) member;
        NodeList<Modifier> modifier = fieldDeclaration.getModifiers();
        if (modifier.toString().contains("public")
                && modifier.toString().contains("static")
                && !modifier.toString().contains("final")) {
            fieldDeclaration.addModifier(Modifier.Keyword.FINAL);
        }
    }
}
