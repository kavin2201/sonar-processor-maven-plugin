package com.sonarprocessor.sonarprocessor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.sonarprocessor.interfaces.IProcessor;
import com.sonarprocessor.sonarutils.SonarUtil;

import java.util.List;

/**
 * S2039IssueProcessor class used to add the access specifiers to the field which are not having
 * modifiers
 *
 * @author Kavin
 */
public class S2039IssueProcessor implements IProcessor {

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
        System.out.println("============== S2039IssueProcessor started ===================");
        if (typeDeclaration instanceof EnumDeclaration) {
            addModifiersToField(typeDeclaration);
        } else if (typeDeclaration instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                    (ClassOrInterfaceDeclaration) typeDeclaration;
            if (classOrInterfaceDeclaration != null && !classOrInterfaceDeclaration.isInterface()) {
                addModifiersToField(typeDeclaration);
            }
        }
        System.out.println("============== S2039IssueProcessor end " + "===================");
    }

    /**
     * addModofiersToField
     *
     * @param typeDeclaration {TypeDeclaration}
     */
    private void addModifiersToField(TypeDeclaration typeDeclaration) {
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
        if (classOrInterfaceDeclaration != null && !classOrInterfaceDeclaration.isInterface()) {
            NodeList<BodyDeclaration<?>> innerClassMembers =
                    classOrInterfaceDeclaration.getMembers();
            innerClassMembers.stream()
                    .forEach(
                            innerClassMember -> {
                                if (innerClassMember instanceof FieldDeclaration) {
                                    addFieldModifiers(innerClassMember);
                                } else if (innerClassMember
                                        instanceof ClassOrInterfaceDeclaration) {
                                    addClassOrInterfaceModifiers(innerClassMember);
                                } else if (innerClassMember instanceof EnumDeclaration) {
                                    addEnumModifiers(innerClassMember);
                                }
                            });
        }
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
        if (fieldDeclaration.getModifiers().isEmpty()) {
            fieldDeclaration.setModifier(Modifier.Keyword.PRIVATE, true);
        }
    }
}
