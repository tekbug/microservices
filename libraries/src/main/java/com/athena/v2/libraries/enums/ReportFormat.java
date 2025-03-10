package com.athena.v2.libraries.enums;

import lombok.Getter;

@Getter
public enum ReportFormat {
    PDF("pdf"),
    EXCEL("xslx"),
    CSV("csv");

    private final String fileName;

    ReportFormat(String fileName) {
        this.fileName = fileName;
    }

}
