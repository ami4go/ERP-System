package edu.univ.erp.domain;

import javax.swing.table.DefaultTableModel;
import java.util.Map;

public class GradebookData {
    public DefaultTableModel model;

    // Maps the "Raw Score" Column Index -> Assessment Object
    // We use this to know which columns contain editable scores and what their math rules are.
    public Map<Integer, Assessment> assessmentMap;

    public Map<Integer, Integer> rowMap;

    public GradebookData(DefaultTableModel model, Map<Integer, Assessment> assessmentMap, Map<Integer, Integer> rowMap) {
        this.model = model;
        this.assessmentMap = assessmentMap;
        this.rowMap = rowMap;
    }
}