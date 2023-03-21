package com.sonarprocessor.models;

import java.nio.file.Path;
import java.util.List;

/** SonarProcessorModel */
public class SonarProcessorModel {

    private String path;

    private String rule;

    private List<Path> files;

    private Boolean formatImport;

    private Boolean format;

    /**
     * getPath
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * setPath
     *
     * @param path {String}
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * getRule
     *
     * @return String
     */
    public String getRule() {
        return rule;
    }

    /**
     * setRule
     *
     * @param rule {String}
     */
    public void setRule(String rule) {
        this.rule = rule;
    }

    /**
     * getFiles
     *
     * @return List<Path>
     */
    public List<Path> getFiles() {
        return files;
    }

    /**
     * setFiles
     *
     * @param files {List<Path>}
     */
    public void setFiles(List<Path> files) {
        this.files = files;
    }

    /**
     * getFormatImport
     *
     * @return Boolean
     */
    public Boolean getFormatImport() {
        return formatImport;
    }

    /**
     * setFormatImport
     *
     * @param formatImport {Boolean}
     */
    public void setFormatImport(Boolean formatImport) {
        this.formatImport = formatImport;
    }

    /**
     * getFormat
     *
     * @return Boolean
     */
    public Boolean getFormat() {
        return format;
    }

    /**
     * setFormat
     *
     * @param format {Boolean}
     */
    public void setFormat(Boolean format) {
        this.format = format;
    }
}
