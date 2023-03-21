# sonar-processor-maven-plugin

# Overview
SonarQube works by analyzing the source code of an application, looking for various issues such as coding errors, code smells, security vulnerabilities, and performance issues. The analysis is performed using a set of pre-defined rules or quality profiles that can be customized to fit the specific needs of a project or organization. It can help development teams to improve the quality and security of their code, reduce technical debt, and ensure that their applications meet the highest standards of performance and reliability. To automate of fixing these scanned issues, SonarQube issue resolver utility implemented. Currently, this utility will cover some of code smells and vulnerabilities(rules).

# Problem Statement
Currently, we are manually fixing the SonarQube issues in existing repository java classes.
Planning to reduce the manual work, implemented a utility to resolve the issues in a simple way.

# Plugin
```
    <plugin>
        <groupId>com.sonarprocessor</groupId>
        <artifactId>sonar-processor-maven-plugin</artifactId>
        <version>1.1</version>
        <executions>
          <execution>
            <id>sonar-processor</id>
            <configuration>
              <sonar.processor.format.import>true</sonar.processor.format.import>
              <sonar.processor.format>false</sonar.processor.format>
            </configuration>
            <goals>
              <goal>sonar-processor</goal>
            </goals>
            <phase>process-sources</phase>
          </execution>
        </executions>
      </plugin>
```

# Covered rules
1. Utility will remove all unused imports. 
2. By default, utility will format the source code by using GoogleFormatter rules.
3. Utility will sort the class members.(static block, final fields, other fields, all the constructors, methods, then other members like inner classes, enums etc.)
4. Rule java:S1176 > Public types, methods and fields (API) should be documented with Javadoc
5. Rule java:S103 > Lines should not be too long
6. Rule java:S2039 > Member variable visibility should be specified
7. Rule java:S121 > Control structures should use curly braces
8. Rule java:S117 > Local variable and method parameter names should comply with a naming convention
