package com.sonarprocessor.sonarprocessor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.sonarprocessor.interfaces.IProcessor;
import com.sonarprocessor.sonarutils.SonarUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * S121IssueProcessor class will resolve missed curly braces of the loops. It will change all the if
 * loop and nested if loop.
 *
 * @author Kavin
 */
public class S121IssueProcessor implements IProcessor {

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
        System.out.println("============== S121IssueProcessor started ===================");
        if (typeDeclaration instanceof EnumDeclaration) {
            addEnumCurlyBraces(typeDeclaration);
        }
        addMissedCurlyBracesToStatements(typeDeclaration);
        System.out.println("============== S121IssueProcessor end " + "===================");
    }

    /**
     * addEnumCurlyBraces
     *
     * @param typeDeclaration {TypeDeclaration}
     */
    private void addEnumCurlyBraces(TypeDeclaration typeDeclaration) {
        EnumDeclaration enumDeclaration = (EnumDeclaration) typeDeclaration;
        NodeList<EnumConstantDeclaration> entries = enumDeclaration.getEntries();
        for (EnumConstantDeclaration entry : entries) {
            NodeList<BodyDeclaration<?>> classBody = entry.getClassBody();
            classBody.stream()
                    .forEach(
                            bodyDeclaration -> {
                                if (bodyDeclaration instanceof MethodDeclaration) {
                                    addMissedCurlyBracesToMethodStatements(bodyDeclaration);
                                } else if (bodyDeclaration instanceof ClassOrInterfaceDeclaration) {
                                    addClassOrInterfaceCurlyBraces(bodyDeclaration);
                                }
                            });
        }
    }

    /**
     * addMissedCurlyBracesToStatements
     *
     * @param typeDeclaration {TypeDeclaration}
     */
    private void addMissedCurlyBracesToStatements(TypeDeclaration typeDeclaration) {
        List<BodyDeclaration> members = typeDeclaration.getMembers();
        members.stream()
                .forEach(
                        member -> {
                            if (member instanceof MethodDeclaration) {
                                addMissedCurlyBracesToMethodStatements(member);
                            } else if (member instanceof ClassOrInterfaceDeclaration) {
                                addClassOrInterfaceCurlyBraces(member);
                            } else if (member instanceof EnumDeclaration) {
                                addEnumMissedCurlyBraces(member);
                            } else if (member instanceof ConstructorDeclaration) {
                                addMissedCurlyBracesToConstructor(member);
                            }
                        });
    }

    /**
     * addMissedCurlyBracesToConstructor
     *
     * @param member {BodyDeclaration}
     */
    private void addMissedCurlyBracesToConstructor(BodyDeclaration member) {
        ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) member;
        BlockStmt body = constructorDeclaration.getBody();
        if (body.getStatements().isNonEmpty()) {
            addMissedCurlyBracesToIfStatement(body);
        }
    }

    /**
     * addMissedCurlyBracesToMethodStatements
     *
     * @param member {BodyDeclaration}
     */
    private void addMissedCurlyBracesToMethodStatements(BodyDeclaration member) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) member;
        Optional<BlockStmt> body = methodDeclaration.getBody();
        if (body.isPresent()) {
            addMissedCurlyBracesToIfStatement(body.get());
        }
    }

    /**
     * addMissedCurlyBracesToIfStatement
     *
     * @param body {BlockStmt}
     */
    private void addMissedCurlyBracesToIfStatement(BlockStmt body) {
        NodeList<Statement> blockStmts = body.getStatements();
        addMissedCurlyBracesToIfStatement(blockStmts);
    }

    /**
     * addMissedCurlyBracesToIfStatement
     *
     * @param blockStmts {NodeList<Statement>}
     */
    private void addMissedCurlyBracesToIfStatement(NodeList<Statement> blockStmts) {
        LinkedHashMap<IfStmt, IfStmt> ifStatementIndexes = new LinkedHashMap<>();
        for (Statement blockStmt : blockStmts) {
            if (blockStmt instanceof IfStmt) {
                IfStmt ifStmt = new IfStmt();
                getNewIfStatement(blockStmt, ifStmt);
                IfStmt existingIf = ((IfStmt) blockStmt);
                ifStatementIndexes.put(existingIf, ifStmt);
            } else if (blockStmt instanceof TryStmt) {
                TryStmt tryStmt = (TryStmt) blockStmt;
                addMissedCurlyBracesToIfStatement(tryStmt.getTryBlock().getStatements());
            }
        }
        ifStatementIndexes
                .entrySet()
                .forEach(
                        map -> {
                            blockStmts.replace(map.getKey(), map.getValue());
                        });
    }

    /**
     * getNewIfStatement
     *
     * @param blockStmt {Statement}
     * @param ifStmt {IfStmt}
     */
    private void getNewIfStatement(Statement blockStmt, IfStmt ifStmt) {
        IfStmt existingIf = ((IfStmt) blockStmt);
        ifStmt.setCondition(((IfStmt) blockStmt).getCondition());
        if (existingIf.getThenStmt() instanceof BlockStmt) {
            ifStmt.setThenStmt(existingIf.getThenStmt());
        } else {
            BlockStmt blockStmt1 = new BlockStmt();
            blockStmt1.addStatement(existingIf.getThenStmt());
            ifStmt.setThenStmt(blockStmt1);
        }
        if (existingIf.getElseStmt().isPresent()) {
            Statement elseStatement = existingIf.getElseStmt().get();
            if (elseStatement instanceof IfStmt) {
                IfStmt ifStmt1 = new IfStmt();
                getNewIfStatement(elseStatement, ifStmt1);
                ifStmt.setElseStmt(ifStmt1);
            } else {
                if (elseStatement instanceof BlockStmt) {
                    ifStmt.setElseStmt(elseStatement);
                } else {
                    BlockStmt blockStmt1 = new BlockStmt();
                    blockStmt1.addStatement(elseStatement);
                    ifStmt.setElseStmt(blockStmt1);
                }
            }
        }
    }

    /**
     * addClassOrInterfaceCurlyBraces
     *
     * @param member {BodyDeclaration}
     */
    private void addClassOrInterfaceCurlyBraces(BodyDeclaration member) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                (ClassOrInterfaceDeclaration) member;
        NodeList<BodyDeclaration<?>> innerClassMembers = classOrInterfaceDeclaration.getMembers();
        innerClassMembers.stream()
                .forEach(
                        innerClassMember -> {
                            if (innerClassMember instanceof MethodDeclaration) {
                                addMissedCurlyBracesToMethodStatements(innerClassMember);
                            } else if (innerClassMember instanceof ClassOrInterfaceDeclaration) {
                                addClassOrInterfaceCurlyBraces(innerClassMember);
                            } else if (innerClassMember instanceof EnumDeclaration) {
                                addEnumMissedCurlyBraces(innerClassMember);
                            } else if (innerClassMember instanceof ConstructorDeclaration) {
                                addMissedCurlyBracesToConstructor(innerClassMember);
                            }
                        });
    }

    /**
     * addEnumMissedCurlyBraces
     *
     * @param member {BodyDeclaration}
     */
    private void addEnumMissedCurlyBraces(BodyDeclaration member) {
        EnumDeclaration classOrInterfaceDeclaration = (EnumDeclaration) member;
        NodeList<BodyDeclaration<?>> innerClassMembers = classOrInterfaceDeclaration.getMembers();
        innerClassMembers.stream()
                .forEach(
                        innerClassMember -> {
                            if (innerClassMember instanceof MethodDeclaration) {
                                addMissedCurlyBracesToMethodStatements(innerClassMember);
                            } else if (innerClassMember instanceof ClassOrInterfaceDeclaration) {
                                addClassOrInterfaceCurlyBraces(innerClassMember);
                            } else if (innerClassMember instanceof EnumDeclaration) {
                                addEnumMissedCurlyBraces(innerClassMember);
                            } else if (innerClassMember instanceof ConstructorDeclaration) {
                                addMissedCurlyBracesToConstructor(innerClassMember);
                            }
                        });
    }
}
