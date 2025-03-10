package com.athena.v2.libraries.utils;

import com.athena.v2.libraries.dtos.responses.*;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class MapperUtils {

    public AttendanceReportDetailedResponseDTO toDetailedReport(AttendanceReportResponseDTO basicReport, String courseName) {

        SessionAttendanceResponseDTO sessionAttendance = SessionAttendanceResponseDTO.builder()
                .sessionId(basicReport.classroomId())
                .sessionDate(basicReport.sessionDate())
                .totalStudents(basicReport.totalStudents())
                .presentStudents(basicReport.presentStudents())
                .absentStudents(basicReport.absentStudents())
                .lateStudents(basicReport.lateStudents())
                .attendanceDetails(basicReport.attendanceDetails())
            .build();

        ReportSummaryResponseDTO summary = ReportSummaryResponseDTO.builder()
                .totalSessions(1)
                .totalStudents(basicReport.totalStudents())
                .averageAttendanceRate(calculateAttendanceRate(basicReport))
                .statusDistribution(basicReport.statusDistribution())
                .metrics(Map.of(
                        "attendanceRate", calculateAttendanceRate(basicReport),
                        "lateRate", calculateLateRate(basicReport)
                ))
            .build();

        return AttendanceReportDetailedResponseDTO.builder()
                .courseId(basicReport.courseId())
                .courseName(courseName)
                .reportGeneratedAt(basicReport.lastUpdated())
                .periodStart(basicReport.sessionDate())
                .periodEnd(basicReport.sessionDate())
                .summary(summary)
                .sessions(List.of(sessionAttendance))
                .trends(Collections.emptyMap())
           .build();
        }

    private double calculateAttendanceRate(AttendanceReportResponseDTO report) {
        if (report.totalStudents() == 0) return 0.0;
        return (double) report.presentStudents() / report.totalStudents() * 100;
    }

    private double calculateLateRate(AttendanceReportResponseDTO report) {
        if (report.totalStudents() == 0) return 0.0;
        return (double) report.lateStudents() / report.totalStudents() * 100;
    }

    public AttendanceReportResponseDTO toBasicReport(AttendanceReportDetailedResponseDTO detailedReport) {
        SessionAttendanceResponseDTO firstSession = detailedReport.sessions().get(0);

        return AttendanceReportResponseDTO.builder()
                .classroomId(firstSession.sessionId())
                .courseId(detailedReport.courseId())
                .sessionDate(firstSession.sessionDate())
                .totalStudents(firstSession.totalStudents())
                .presentStudents(firstSession.presentStudents())
                .absentStudents(firstSession.absentStudents())
                .lateStudents(firstSession.lateStudents())
                .attendanceDetails(firstSession.attendanceDetails())
                .statusDistribution(detailedReport.summary().statusDistribution())
                .lastUpdated(detailedReport.reportGeneratedAt())
           .build();
        }
}
