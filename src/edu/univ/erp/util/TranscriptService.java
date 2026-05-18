package edu.univ.erp.util;

import com.opencsv.CSVWriter;
import edu.univ.erp.auth.CurrentUserSession;
import edu.univ.erp.data.GradeRepository;
import edu.univ.erp.domain.TranscriptEntry;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Handles the business logic for generating transcript files.
 * REFACTORED: Uses Username lookup.
 */
public class TranscriptService {
    private GradeRepository gradeRepository;

    public TranscriptService() {
        this.gradeRepository = new GradeRepository();
    }

    /**
     * Generates a CSV transcript for the current student.
     * Shows a file chooser and writes the file.
     *
     * @param parentComponent The parent UI component (for centering the dialog)
     * @return A status message
     */
    public String generateTranscriptCSV(Component parentComponent) {
        // REFACTORED: Get Username instead of Profile ID
        String username = CurrentUserSession.getInstance().getUsername();

        if (username == null || username.isEmpty()) {
            return "Error: Could not find student profile.";
        }

        // 1. Get the data (Now passes String username)
        List<TranscriptEntry> transcriptData = gradeRepository.getTranscriptForStudent(username);

        if (transcriptData.isEmpty()) {
            return "No completed courses found. (Grades must be finalized by the instructor).";
        }

        // 2. Show "Save" dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript");
        fileChooser.setSelectedFile(new File("transcript.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        int userSelection = fileChooser.showSaveDialog(parentComponent);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return "File save cancelled.";
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
        }

        // 3. Write to the CSV file
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileToSave))) {
            // Write Header
            String[] header = {"Course Code", "Course Title", "Credits", "Final Grade"};
            writer.writeNext(header);

            // Write Data
            for (TranscriptEntry entry : transcriptData) {
                writer.writeNext(entry.toCsvRow());
            }

            return "Transcript saved successfully to " + fileToSave.getName();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Could not write file. " + e.getMessage();
        }
    }
}