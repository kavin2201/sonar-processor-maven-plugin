package com.sonarprocessor.sonarprocessor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import com.sonarprocessor.interfaces.IProcessor;
import com.sonarprocessor.sonarutils.SonarUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * S1176IssueProcessor to add the JavaDoc to all the public fields, all the methods, inner class
 * members and enums
 *
 * @author Kavin
 */
public class S1176IssueProcessor implements IProcessor {

    /**
     * getJavadocBlockTag
     *
     * @param param {JavadocBlockTag.Type}
     * @param param1 {String}
     * @return JavadocBlockTag
     */
    private static JavadocBlockTag getJavadocBlockTag(JavadocBlockTag.Type param, String param1) {
        JavadocBlockTag tag = new JavadocBlockTag(param, param1);
        return tag;
    }

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
        System.out.println("============== S1176IssueProcessor started ===================");
        if (typeDeclaration instanceof EnumDeclaration) {
            addEnumComments(typeDeclaration);
        } else {
            if (!isCommentAvailable(typeDeclaration)
                    && compilationUnit.getOrphanComments().isEmpty()) {
                JavadocDescriptionElement el =
                        new JavadocSnippet(typeDeclaration.getNameAsString());
                JavadocDescription description = new JavadocDescription(Arrays.asList(el));
                Javadoc doc = new Javadoc(description);
                NodeList<TypeParameter> typeParameters =
                        ((ClassOrInterfaceDeclaration) typeDeclaration).getTypeParameters();
                for (TypeParameter typeParameter : typeParameters) {
                    JavadocBlockTag tag =
                            getJavadocBlockTag(
                                    JavadocBlockTag.Type.PARAM,
                                    "<"
                                            + typeParameter.asString()
                                            + "> the parameter of the class");
                    doc.addBlockTag(tag);
                }
                typeDeclaration.setJavadocComment(doc);
            }
            addFieldOrMethodComments(typeDeclaration);
        }
        System.out.println("============== S1176IssueProcessor end " + "===================");
    }

    /**
     * validateExistingComment
     *
     * @param methodDeclaration {MethodDeclaration}
     */
    private void validateExistingComment(MethodDeclaration methodDeclaration) {
        Optional<Javadoc> existingComment = methodDeclaration.getJavadoc();
        if (existingComment.isPresent()) {
            JavadocDescription description = existingComment.get().getDescription();
            Javadoc doc = new Javadoc(description);
            List<JavadocBlockTag> blockTags = existingComment.get().getBlockTags();
            if (blockTags.isEmpty()) {
            } else {
                for (JavadocBlockTag blockTag : blockTags) {
                    String content = blockTag.getContent().toText();
                    JavadocBlockTag.Type type = blockTag.getType();
                    String name = "";
                    if (blockTag.getName().isPresent()) {
                        name = blockTag.getName().get();
                    }
                    if (content == null || content.isEmpty()) {
                        content = name;
                    }
                    name = name.replace(",", "");
                    if (type.equals(JavadocBlockTag.Type.PARAM) && !isLowerCamelCase(name)) {
                        name = SonarUtil.convertString(name);
                    }
                    doc.addBlockTag(getJavadocBlockTag(type, name + " " + content));
                }
            }
            methodDeclaration.setJavadocComment(doc);
        }
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

    /**
     * isCommentAvailable
     *
     * @param typeDeclaration {TypeDeclaration}
     * @return boolean
     */
    private boolean isCommentAvailable(TypeDeclaration typeDeclaration) {
        return typeDeclaration.getComment().isPresent()
                || typeDeclaration.getJavadoc().isPresent()
                || typeDeclaration.getJavadocComment().isPresent()
                || !typeDeclaration.getOrphanComments().isEmpty();
    }

    /**
     * addFieldOrMethodComments
     *
     * @param typeDeclaration {TypeDeclaration}
     */
    private void addFieldOrMethodComments(TypeDeclaration typeDeclaration) {
        List<BodyDeclaration> members = typeDeclaration.getMembers();
        members.stream()
                .forEach(
                        member -> {
                            if (member instanceof FieldDeclaration) {
                                addFieldComments(member);
                            } else if (member instanceof MethodDeclaration) {
                                addMethodComments(member);
                            } else if (member instanceof ClassOrInterfaceDeclaration) {
                                addClassOrInterfaceComments(member);
                            } else if (member instanceof ConstructorDeclaration) {
                                addConstructorComments(member);
                            } else if (member instanceof EnumDeclaration) {
                                addEnumComments(member);
                            }
                        });
    }

    /**
     * addEnumComments
     *
     * @param enumDeclaration {EnumDeclaration}
     */
    private void addEnumComments(EnumDeclaration enumDeclaration) {
        NodeList<EnumConstantDeclaration> entries = enumDeclaration.getEntries();
        for (EnumConstantDeclaration entry : entries) {
            NodeList<BodyDeclaration<?>> classBody = entry.getClassBody();
            classBody.stream()
                    .forEach(
                            bodyDeclaration -> {
                                if (bodyDeclaration instanceof FieldDeclaration) {
                                    addFieldComments(bodyDeclaration);
                                } else if (bodyDeclaration instanceof MethodDeclaration) {
                                    addMethodComments(bodyDeclaration);
                                }
                            });
        }
    }

    /**
     * addEnumComments
     *
     * @param member {BodyDeclaration}
     */
    private void addEnumComments(BodyDeclaration member) {
        EnumDeclaration enumDeclaration = (EnumDeclaration) member;
        addEnumComments(enumDeclaration);
        if (!isCommentAvailable(enumDeclaration)) {
            JavadocDescriptionElement el = new JavadocSnippet(enumDeclaration.getNameAsString());
            JavadocDescription description = new JavadocDescription(Arrays.asList(el));
            Javadoc doc = new Javadoc(description);
            enumDeclaration.setJavadocComment(doc);
        }
        addFieldOrMethodComments(enumDeclaration);
    }

    /**
     * addEnumComments
     *
     * @param member {TypeDeclaration}
     */
    private void addEnumComments(TypeDeclaration member) {
        EnumDeclaration enumDeclaration = (EnumDeclaration) member;
        addEnumComments(enumDeclaration);
        if (!isCommentAvailable(enumDeclaration)) {
            JavadocDescriptionElement el = new JavadocSnippet(enumDeclaration.getNameAsString());
            JavadocDescription description = new JavadocDescription(Arrays.asList(el));
            Javadoc doc = new Javadoc(description);
            enumDeclaration.setJavadocComment(doc);
        }
        addFieldOrMethodComments(enumDeclaration);
    }

    /**
     * addConstructorComments
     *
     * @param member {BodyDeclaration}
     */
    private void addConstructorComments(BodyDeclaration member) {
        ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) member;
        Optional<Comment> comment = constructorDeclaration.getComment();
        if (constructorDeclaration.isPublic() && !comment.isPresent()) {
            JavadocDescriptionElement el =
                    new JavadocSnippet(constructorDeclaration.getNameAsString());
            JavadocDescription description = new JavadocDescription(Arrays.asList(el));
            Javadoc doc = new Javadoc(description);
            NodeList<Parameter> parameters = constructorDeclaration.getParameters();
            for (Parameter param : parameters) {
                JavadocBlockTag tag =
                        getJavadocBlockTag(
                                JavadocBlockTag.Type.PARAM,
                                param.getNameAsString() + " {" + param.getTypeAsString() + "}");
                doc.addBlockTag(tag);
            }
            NodeList<ReferenceType> exceptions = constructorDeclaration.getThrownExceptions();
            for (ReferenceType exception : exceptions) {
                JavadocBlockTag tag =
                        getJavadocBlockTag(
                                JavadocBlockTag.Type.THROWS,
                                exception.getElementType().asString()
                                        + " {"
                                        + exception.getElementType().asString()
                                        + "}");
                doc.addBlockTag(tag);
            }
            constructorDeclaration.setJavadocComment(doc);
        }
    }

    /**
     * addClassOrInterfaceComments
     *
     * @param member {BodyDeclaration}
     */
    private void addClassOrInterfaceComments(BodyDeclaration member) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                (ClassOrInterfaceDeclaration) member;
        if (!isCommentAvailable(classOrInterfaceDeclaration)) {
            classOrInterfaceDeclaration.setJavadocComment(
                    classOrInterfaceDeclaration.getNameAsString());
        }
        addInnterFieldsComments(classOrInterfaceDeclaration);
    }

    /**
     * isCommentAvailable
     *
     * @param classOrInterfaceDeclaration {ClassOrInterfaceDeclaration}
     * @return boolean
     */
    private boolean isCommentAvailable(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return classOrInterfaceDeclaration.getComment().isPresent()
                || classOrInterfaceDeclaration.getJavadoc().isPresent()
                || classOrInterfaceDeclaration.getJavadocComment().isPresent()
                || !classOrInterfaceDeclaration.getOrphanComments().isEmpty();
    }

    /**
     * addInnterFieldsComments
     *
     * @param classOrInterfaceDeclaration {ClassOrInterfaceDeclaration}
     */
    private void addInnterFieldsComments(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        NodeList<BodyDeclaration<?>> members = classOrInterfaceDeclaration.getMembers();
        members.stream()
                .forEach(
                        member -> {
                            if (member instanceof FieldDeclaration) {
                                addFieldComments(member);
                            } else if (member instanceof MethodDeclaration) {
                                addMethodComments(member);
                            } else if (member instanceof MethodDeclaration) {
                                addClassOrInterfaceComments(member);
                            }
                        });
    }

    /**
     * addMethodComments
     *
     * @param member {BodyDeclaration}
     */
    private void addMethodComments(BodyDeclaration member) {
        MethodDeclaration method = (MethodDeclaration) member;
        if (!isCommentAvailable(method)) {
            JavadocDescriptionElement el = new JavadocSnippet(method.getNameAsString());
            JavadocDescription description = new JavadocDescription(Arrays.asList(el));
            Javadoc doc = new Javadoc(description);
            NodeList<Parameter> parameters = method.getParameters();
            for (Parameter param : parameters) {
                JavadocBlockTag tag =
                        getJavadocBlockTag(
                                JavadocBlockTag.Type.PARAM,
                                param.getNameAsString() + " {" + param.getTypeAsString() + "}");
                doc.addBlockTag(tag);
            }
            NodeList<ReferenceType> exceptions = method.getThrownExceptions();
            for (ReferenceType exception : exceptions) {
                JavadocBlockTag tag =
                        getJavadocBlockTag(
                                JavadocBlockTag.Type.THROWS,
                                exception.getElementType().asString()
                                        + " {"
                                        + exception.getElementType().asString()
                                        + "}");
                doc.addBlockTag(tag);
            }
            if (!method.getType().isVoidType()) {
                JavadocBlockTag tag =
                        getJavadocBlockTag(
                                JavadocBlockTag.Type.RETURN, method.getType().asString());
                doc.addBlockTag(tag);
            }
            method.setJavadocComment(doc);
        } else {
            validateExistingComment(method);
        }
    }

    /**
     * isCommentAvailable
     *
     * @param method {MethodDeclaration}
     * @return boolean
     */
    private boolean isCommentAvailable(MethodDeclaration method) {
        return method.getComment().isPresent()
                || method.getJavadoc().isPresent()
                || method.getJavadocComment().isPresent()
                || !method.getOrphanComments().isEmpty();
    }

    /**
     * addFieldComments
     *
     * @param member {BodyDeclaration}
     */
    private void addFieldComments(BodyDeclaration member) {
        FieldDeclaration field = (FieldDeclaration) member;
        NodeList<Modifier> modifiers = field.getModifiers();
        if (modifiers.isNonEmpty()
                && modifiers.stream().findFirst().isPresent()
                && modifiers.get(0).getKeyword().name().equalsIgnoreCase("public")) {
            if (!isCommentAvailable(field)) {
                Optional<VariableDeclarator> variable = field.getVariables().stream().findFirst();
                field.setJavadocComment("SP COMMENT : " + variable.get().getNameAsString());
            }
        }
    }

    /**
     * isCommentAvailable
     *
     * @param field {FieldDeclaration}
     * @return boolean
     */
    private boolean isCommentAvailable(FieldDeclaration field) {
        return field.getComment().isPresent()
                || field.getJavadocComment().isPresent()
                || field.getJavadoc().isPresent()
                || !field.getOrphanComments().isEmpty();
    }
}
