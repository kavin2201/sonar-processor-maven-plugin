package com.sonarprocessor.mojo;

import com.sonarprocessor.main.SonarProcessor;
import com.sonarprocessor.models.SonarProcessorModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/** SonarProcessorMojo */
@Mojo(name = "sonar-processor", defaultPhase = LifecyclePhase.INSTALL)
public class SonarProcessorMojo extends AbstractMojo {

    /** SP COMMENT : SOURCE_MAPPING */
    public static final SuffixMapping SOURCE_MAPPING =
            new SuffixMapping(".java", new HashSet<>(Arrays.asList(".java")));

    @Parameter(required = true, readonly = true, property = "project.build.sourceDirectory")
    protected File sourceDirectory;

    @Parameter(required = true, readonly = true, property = "project.build.outputDirectory")
    protected File outputDirectory;

    @Parameter(required = true, readonly = true, property = "project")
    protected MavenProject project;

    @Parameter(defaultValue = "false", property = "sonar.processor.skip")
    protected boolean skip;

    @Parameter(defaultValue = "false", property = "sonar.processor.format.import")
    protected boolean formatImport;

    @Parameter(defaultValue = "true", property = "sonar.processor.format")
    protected boolean format;

    @Parameter(defaultValue = "ALL", property = "sonar.processor.rule")
    protected String rule;

    @Inject private SonarProcessor sonarProcessor;

    /**
     * execute method to start the sonar processor
     *
     * @throws MojoExecutionException MojoExecutionException
     * @throws MojoFailureException MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping sonar processor due to plugin configuration.");
            return;
        }
        // if ("pom".equals(project.getPackaging())) {
        // getLog().info("Project packaging is POM, skipping...");
        // return;
        try {
            SonarProcessorModel sonarProcessorModel = new SonarProcessorModel();
            Set<File> sourceFiles = new HashSet<>();
            sourceFiles.addAll(findFilesToReformat(sourceDirectory, outputDirectory));
            getLog().info("Total number of files for the execution: " + sourceFiles.size());
            List<Path> javaFiles =
                    sourceFiles
                            .parallelStream()
                            .map(
                                    file -> {
                                        return Paths.get(file.toURI());
                                    })
                            .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(javaFiles)) {
                getLog().info("No files found for execution..");
                return;
            }
            sonarProcessorModel.setFiles(javaFiles);
            sonarProcessorModel.setPath(sourceDirectory.getAbsolutePath());
            sonarProcessorModel.setRule(rule);
            sonarProcessorModel.setFormatImport(formatImport);
            sonarProcessorModel.setFormat(format);
            sonarProcessor.analyze(sonarProcessorModel);
        } catch (Exception e) {
            getLog().error(e.getMessage());
        }
    }

    /**
     * findFilesToReformat
     *
     * @param sourceDirectory {File}
     * @param outputDirectory {File}
     * @throws MojoExecutionException {MojoExecutionException}
     * @return Set<File>
     */
    private Set<File> findFilesToReformat(File sourceDirectory, File outputDirectory)
            throws MojoExecutionException {
        if (sourceDirectory.exists()) {
            try {
                SourceInclusionScanner scanner = getSourceInclusionScanner(false);
                scanner.addSourceMapping(SOURCE_MAPPING);
                Set<File> sourceFiles =
                        scanner.getIncludedSources(sourceDirectory, outputDirectory);
                return sourceFiles;
            } catch (InclusionScanException e) {
                throw new MojoExecutionException(
                        String.format("ERROR_SCANNING_PATH", sourceDirectory.getPath()), e);
            }
        } else {
            getLog().info(String.format("DIRECTORY_MISSING", sourceDirectory.getPath()));
            return Collections.emptySet();
        }
    }

    /**
     * getSourceInclusionScanner
     *
     * @param includeStale {boolean}
     * @return SourceInclusionScanner
     */
    protected SourceInclusionScanner getSourceInclusionScanner(boolean includeStale) {
        return includeStale
                ? new SimpleSourceInclusionScanner(
                        Collections.singleton("**/*"), Collections.emptySet())
                : new StaleSourceScanner(1024);
    }
}
