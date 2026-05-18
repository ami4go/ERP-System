package edu.univ.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class CsvExporter {

    public static String exportGraduates(String program, List<Map<String, String>> studentData) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Graduates_" + program + "_" + timestamp + ".csv";

        try (FileWriter writer = new FileWriter(filename)) {
            // FIX: Changed Header from "Student ID" to "Username"
            writer.append("Username,Name,Roll No,Program,Admission Year,Final CGPA\n");

            for (Map<String, String> student : studentData) {
                // FIX: Use "username" key
                writer.append(student.getOrDefault("username", "N/A")).append(",");
                writer.append(student.getOrDefault("name", "")).append(",");
                writer.append(student.getOrDefault("roll", "")).append(",");
                writer.append(student.getOrDefault("program", "")).append(",");
                writer.append(student.getOrDefault("year", "")).append(",");
                writer.append(student.getOrDefault("cgpa", "")).append("\n");
            }

            return filename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}