package com.emr.gds.soap;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * EMRPMH
 * Physician-facing Past Medical History input form (JavaFX 21+ / 25).
 *
 * Features
 * - Patient header: Patient ID, Physician, Visit Date
 * - PMH items: Disease, Onset date, Status (Active/Inactive/Resolved), Notes -> add to list
 * - Surgeries/Procedures list
 * - Allergies, Medications, Family Hx, Social Hx text areas
 * - Export to compact EMR note text with dash-prefixed lines
 *
 * Integrates as a reusable control: new EMRPMH() and add to any Scene.
 * No external libraries required.
 */
public class EMRPMH extends Dialog<Void> {

    // Header
    private final TextField tfPatientId = new TextField();
    private final TextField tfPhysician = new TextField();
    private final DatePicker dpVisitDate = new DatePicker(LocalDate.now());

    // PMH item composer
    private final TextField tfDisease = new TextField();
    private final DatePicker dpOnset = new DatePicker();
    private final ComboBox<Status> cbStatus = new ComboBox<>();
    private final TextArea taDiseaseNotes = new TextArea();

    private final Button btnAddDisease = new Button("Add PMH");
    private final Button btnRemoveDisease = new Button("Remove Selected");

    private final ListView<PMHItem> lvPMH = new ListView<>();

    // Surgeries/Procedures
    private final TextField tfSurgery = new TextField();
    private final DatePicker dpSurgeryDate = new DatePicker();
    private final TextArea taSurgeryNotes = new TextArea();
    private final Button btnAddSurgery = new Button("Add Surgery/Procedure");
    private final Button btnRemoveSurgery = new Button("Remove Selected");
    private final ListView<SurgeryItem> lvSurgery = new ListView<>();

    // Free text sections
    private final TextArea taAllergies = new TextArea();
    private final TextArea taMedications = new TextArea();
    private final TextArea taFamilyHx = new TextArea();
    private final TextArea taSocialHx = new TextArea();

    // Footer actions
    private final Button btnClearAll = new Button("Clear All");
    private final Button btnExport = new Button("Export EMR Note");

    // Data containers
    private final ObservableList<PMHItem> pmhItems = FXCollections.observableArrayList();
    private final ObservableList<SurgeryItem> surgeryItems = FXCollections.observableArrayList();

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy.MM.dd");

 // Original (Invalid) Code Block
    public EMRPMH(TextArea textArea) {
        // 1. Set the Title/Header for the Dialog
        setTitle("Past Medical History (PMH) Editor");
        setHeaderText("Comprehensive PMH Input Form");
        
        // 2. Create the VBox to hold all sections
        VBox rootContent = new VBox(15, // VBox for vertical stacking and spacing
            buildHeader(),
            buildPMHBlock(),
            buildSurgeryBlock(),
            buildFreeTexts(),
            buildFooter()
        );
        rootContent.setPadding(new Insets(15)); // Add padding to the VBox content

        // 3. Attach the VBox to the DialogPane (Standard Dialog setup)
        getDialogPane().setContent(rootContent);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE); // Add a close button
        
        // 4. Initialization and Logic
        wireLogic();
        applyStyling(); 
        
        // Optional: Set a preferred size for the Dialog
        getDialogPane().setPrefWidth(900);
        getDialogPane().setPrefHeight(900);
        
        // Fix for the 'setOnShown(14)' error: Remove the helper method call entirely
    }

    // ===== UI builders =====

    private void setOnShown(int i) {
		// TODO Auto-generated method stub
		
	}

	private Node buildHeader() {
        GridPane g = grid(3, 2);
        tfPatientId.setPromptText("e.g., 2025-000123");
        tfPhysician.setPromptText("Physician name");
        dpVisitDate.setConverter(new LocalDateConverter());

        g.add(label("Patient ID"), 0, 0); g.add(tfPatientId, 1, 0);
        g.add(label("Physician"), 0, 1); g.add(tfPhysician, 1, 1);
        g.add(label("Visit Date"), 0, 2); g.add(dpVisitDate, 1, 2);

        TitledPane tp = new TitledPane("Visit Header", g);
        tp.setCollapsible(false);
        return tp;
    }

    private Node buildPMHBlock() {
        GridPane composer = grid(4, 3);

        tfDisease.setPromptText("Disease / Condition");
        dpOnset.setPromptText("Onset date");
        cbStatus.setItems(FXCollections.observableArrayList(Status.values()));
        cbStatus.getSelectionModel().select(Status.ACTIVE);
        taDiseaseNotes.setPromptText("Notes (e.g., severity, control, complications)");
        taDiseaseNotes.setPrefRowCount(2);

        composer.add(label("Disease"), 0, 0); composer.add(tfDisease, 1, 0, 3, 1);
        composer.add(label("Onset"), 0, 1); composer.add(dpOnset, 1, 1);
        composer.add(label("Status"), 2, 1); composer.add(cbStatus, 3, 1);
        composer.add(label("Notes"), 0, 2); composer.add(taDiseaseNotes, 1, 2, 3, 1);

        HBox actions = new HBox(8, btnAddDisease, btnRemoveDisease);
        actions.setAlignment(Pos.CENTER_LEFT);

        lvPMH.setItems(pmhItems);
        lvPMH.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(PMHItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toDisplayString());
            }
        });
        lvPMH.setPrefHeight(180);

        VBox box = new VBox(10, composer, actions, lvPMH);
        TitledPane tp = new TitledPane("Past Medical History", box);
        tp.setCollapsible(false);
        return tp;
    }

    private Node buildSurgeryBlock() {
        GridPane composer = grid(3, 3);

        tfSurgery.setPromptText("Procedure / Operation");
        dpSurgeryDate.setPromptText("Date");
        taSurgeryNotes.setPromptText("Notes (e.g., surgeon, hospital, outcome)");
        taSurgeryNotes.setPrefRowCount(2);

        composer.add(label("Procedure"), 0, 0); composer.add(tfSurgery, 1, 0, 2, 1);
        composer.add(label("Date"), 0, 1); composer.add(dpSurgeryDate, 1, 1);
        composer.add(label("Notes"), 0, 2); composer.add(taSurgeryNotes, 1, 2, 2, 1);

        HBox actions = new HBox(8, btnAddSurgery, btnRemoveSurgery);
        actions.setAlignment(Pos.CENTER_LEFT);

        lvSurgery.setItems(surgeryItems);
        lvSurgery.setPrefHeight(160);
        lvSurgery.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(SurgeryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toDisplayString());
            }
        });

        VBox box = new VBox(10, composer, actions, lvSurgery);
        TitledPane tp = new TitledPane("Surgeries / Procedures", box);
        tp.setCollapsible(false);
        return tp;
    }

    private Node buildFreeTexts() {
        taAllergies.setPromptText("Allergies (drug/food/injection) and reactions");
        taMedications.setPromptText("Current medications (name dose route freq)");
        taFamilyHx.setPromptText("Family Hx (parents/siblings/children)");
        taSocialHx.setPromptText("Social Hx (smoking, alcohol, occupation, exercise)");

        taAllergies.setPrefRowCount(3);
        taMedications.setPrefRowCount(4);
        taFamilyHx.setPrefRowCount(3);
        taSocialHx.setPrefRowCount(3);

        GridPane g = grid(2, 4);
        g.add(sectionLabel("Allergies"), 0, 0); g.add(taAllergies, 1, 0);
        g.add(sectionLabel("Medications"), 0, 1); g.add(taMedications, 1, 1);
        g.add(sectionLabel("Family Hx"), 0, 2); g.add(taFamilyHx, 1, 2);
        g.add(sectionLabel("Social Hx"), 0, 3); g.add(taSocialHx, 1, 3);

        TitledPane tp = new TitledPane("Additional Histories", g);
        tp.setCollapsible(false);
        return tp;
    }

    private Node buildFooter() {
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, btnClearAll, spacer, btnExport);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    // ===== Behavior =====

    private void wireLogic() {
        btnAddDisease.setOnAction(e -> addDisease());
        btnRemoveDisease.setOnAction(e -> {
            PMHItem sel = lvPMH.getSelectionModel().getSelectedItem();
            if (sel != null) pmhItems.remove(sel);
        });

        btnAddSurgery.setOnAction(e -> addSurgery());
        btnRemoveSurgery.setOnAction(e -> {
            SurgeryItem sel = lvSurgery.getSelectionModel().getSelectedItem();
            if (sel != null) surgeryItems.remove(sel);
        });

        btnClearAll.setOnAction(e -> clearAll());
        btnExport.setOnAction(e -> {
            String text = exportAsEmrNote();
            // Show in a simple dialog for copy
            TextArea ta = new TextArea(text);
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefRowCount(24);
            Dialog<Void> dlg = new Dialog<>();
            dlg.setTitle("EMR PMH Export");
            dlg.getDialogPane().setContent(ta);
            dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dlg.showAndWait();
        });

        // Quick-add with Enter in Disease field
        tfDisease.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) addDisease();
        });
        tfSurgery.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) addSurgery();
        });
    }

    private void addDisease() {
        String disease = tfDisease.getText().trim();
        if (disease.isEmpty()) {
            warn("Disease/Condition is required."); return;
        }
        PMHItem item = new PMHItem(
                disease,
                dpOnset.getValue(),
                cbStatus.getValue() == null ? Status.ACTIVE : cbStatus.getValue(),
                taDiseaseNotes.getText().trim()
        );
        pmhItems.add(item);
        tfDisease.clear();
        dpOnset.setValue(null);
        cbStatus.getSelectionModel().select(Status.ACTIVE);
        taDiseaseNotes.clear();
        lvPMH.getSelectionModel().select(item);
    }

    private void addSurgery() {
        String proc = tfSurgery.getText().trim();
        if (proc.isEmpty()) {
            warn("Procedure/Operation is required."); return;
        }
        SurgeryItem s = new SurgeryItem(
                proc,
                dpSurgeryDate.getValue(),
                taSurgeryNotes.getText().trim()
        );
        surgeryItems.add(s);
        tfSurgery.clear();
        dpSurgeryDate.setValue(null);
        taSurgeryNotes.clear();
        lvSurgery.getSelectionModel().select(s);
    }

    private void clearAll() {
        tfPatientId.clear();
        tfPhysician.clear();
        dpVisitDate.setValue(LocalDate.now());

        tfDisease.clear(); dpOnset.setValue(null); cbStatus.getSelectionModel().select(Status.ACTIVE); taDiseaseNotes.clear();
        pmhItems.clear();

        tfSurgery.clear(); dpSurgeryDate.setValue(null); taSurgeryNotes.clear();
        surgeryItems.clear();

        taAllergies.clear();
        taMedications.clear();
        taFamilyHx.clear();
        taSocialHx.clear();
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void applyStyling() {
        // Minimal, modern spacing
        for (TextInputControl t : new TextInputControl[]{tfPatientId, tfPhysician, tfDisease, taDiseaseNotes, tfSurgery, taSurgeryNotes, taAllergies, taMedications, taFamilyHx, taSocialHx}) {
            t.setPromptText(t.getPromptText());
        }
        this.setWidth(true);
        VBox.setVgrow(lvPMH, Priority.SOMETIMES);
        VBox.setVgrow(lvSurgery, Priority.SOMETIMES);
    }

    private void setWidth(boolean b) {
		// TODO Auto-generated method stub
		
	}

	private static GridPane grid(int cols, int rows) {
        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(8);
        g.setPadding(new Insets(12));
        ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(Priority.ALWAYS);
        // First column for labels, second+ grow for inputs
        g.getColumnConstraints().addAll(new ColumnConstraints(), grow, new ColumnConstraints(), grow);
        return g;
    }

    private static Label label(String t) {
        Label l = new Label(t);
        l.setMinWidth(96);
        return l;
    }
    private static Label sectionLabel(String t) {
        Label l = new Label(t);
        l.setMinWidth(120);
        return l;
    }

    // ===== Public API =====

    /** Collects current UI state into a POJO. */
    public PMHData getData() {
        PMHData d = new PMHData();
        d.patientId.set(tfPatientId.getText().trim());
        d.physician.set(tfPhysician.getText().trim());
        d.visitDate.set(dpVisitDate.getValue());
        d.pmhItems.setAll(pmhItems);
        d.surgeries.setAll(surgeryItems);
        d.allergies.set(taAllergies.getText().trim());
        d.medications.set(taMedications.getText().trim());
        d.familyHx.set(taFamilyHx.getText().trim());
        d.socialHx.set(taSocialHx.getText().trim());
        return d;
    }

    /** Applies data to UI. */
    public void setData(PMHData data) {
        if (data == null) return;
        tfPatientId.setText(nz(data.patientId.get()));
        tfPhysician.setText(nz(data.physician.get()));
        dpVisitDate.setValue(data.visitDate.get() == null ? LocalDate.now() : data.visitDate.get());
        pmhItems.setAll(data.pmhItems);
        surgeryItems.setAll(data.surgeries);
        taAllergies.setText(nz(data.allergies.get()));
        taMedications.setText(nz(data.medications.get()));
        taFamilyHx.setText(nz(data.familyHx.get()));
        taSocialHx.setText(nz(data.socialHx.get()));
    }

    /** Exports a compact EMR note (dash-prefixed notes, plain text). */
    public String exportAsEmrNote() {
        PMHData d = getData();
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("PMH Note").append('\n');
        sb.append("- Patient ID   : ").append(nz(d.patientId.get())).append('\n');
        sb.append("- Physician    : ").append(nz(d.physician.get())).append('\n');
        sb.append("- Visit Date   : ").append(fmt(d.visitDate.get())).append('\n');

        // Allergies / Meds
        if (!isBlank(d.allergies.get())) {
            sb.append("- Allergy      : ").append(oneLine(d.allergies.get())).append('\n');
        } else {
            sb.append("- Allergy      : Denied / NKA (as of ").append(fmt(LocalDate.now())).append(")\n");
        }
        if (!isBlank(d.medications.get())) {
            sb.append("- Medications  : ").append(oneLine(d.medications.get())).append('\n');
        }

        // PMH list
        sb.append("\nPast Medical History\n");
        if (d.pmhItems.isEmpty()) {
            sb.append("- No significant past history recorded.\n");
        } else {
            for (PMHItem it : d.pmhItems) {
                sb.append("- ")
                  .append(it.disease.get())
                  .append("  ");
                if (it.onset.get() != null) {
                    sb.append("(onset ").append(fmt(it.onset.get())).append(")  ");
                }
                sb.append("[").append(it.status.get().label).append("]");
                if (!isBlank(it.notes.get())) {
                    sb.append(" : ").append(oneLine(it.notes.get()));
                }
                sb.append('\n');
            }
        }

        // Surgery
        sb.append("\nSurgeries / Procedures\n");
        if (d.surgeries.isEmpty()) {
            sb.append("- None recorded.\n");
        } else {
            for (SurgeryItem s : d.surgeries) {
                sb.append("- ").append(s.procedure.get());
                if (s.date.get() != null) sb.append(" (").append(fmt(s.date.get())).append(")");
                if (!isBlank(s.notes.get())) sb.append(" : ").append(oneLine(s.notes.get()));
                sb.append('\n');
            }
        }

        // Family / Social
        if (!isBlank(d.familyHx.get())) {
            sb.append("\nFamily History\n");
            for (String line : lines(d.familyHx.get())) sb.append("- ").append(line).append('\n');
        }
        if (!isBlank(d.socialHx.get())) {
            sb.append("\nSocial History\n");
            for (String line : lines(d.socialHx.get())) sb.append("- ").append(line).append('\n');
        }

        return sb.toString();
    }

    // ===== Helpers =====

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String nz(String s) { return s == null ? "" : s; }
    private static String fmt(LocalDate d) { return d == null ? "" : DTF.format(d); }
    private static String oneLine(String s) { return s.replaceAll("\\s+", " ").trim(); }
    private static String[] lines(String s) { return s.trim().split("\\R+"); }

    private static class LocalDateConverter extends StringConverter<LocalDate> {
        @Override public String toString(LocalDate date) { return date == null ? "" : DTF.format(date); }
        @Override public LocalDate fromString(String s) {
            if (s == null || s.isBlank()) return null;
            return LocalDate.parse(s.trim(), DTF);
        }
    }

    // ===== Data classes =====

    public enum Status {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        RESOLVED("Resolved");
        public final String label;
        Status(String l) { this.label = l; }
        @Override public String toString() { return label; }
    }

    public static final class PMHItem {
        public final StringProperty id = new SimpleStringProperty(UUID.randomUUID().toString());
        public final StringProperty disease = new SimpleStringProperty();
        public final ObjectProperty<LocalDate> onset = new SimpleObjectProperty<>();
        public final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.ACTIVE);
        public final StringProperty notes = new SimpleStringProperty();

        public PMHItem() { }
        public PMHItem(String disease, LocalDate onset, Status status, String notes) {
            this.disease.set(Objects.requireNonNullElse(disease, ""));
            this.onset.set(onset);
            this.status.set(status == null ? Status.ACTIVE : status);
            this.notes.set(Objects.requireNonNullElse(notes, ""));
        }
        public String toDisplayString() {
            StringBuilder sb = new StringBuilder(disease.get());
            if (onset.get() != null) sb.append("  (").append(DTF.format(onset.get())).append(')');
            sb.append("  [").append(status.get().label).append(']');
            if (!isBlank(notes.get())) sb.append("  — ").append(oneLine(notes.get()));
            return sb.toString();
        }
        @Override public String toString() { return toDisplayString(); }
    }

    public static final class SurgeryItem {
        public final StringProperty id = new SimpleStringProperty(UUID.randomUUID().toString());
        public final StringProperty procedure = new SimpleStringProperty();
        public final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
        public final StringProperty notes = new SimpleStringProperty();

        public SurgeryItem() { }
        public SurgeryItem(String procedure, LocalDate date, String notes) {
            this.procedure.set(Objects.requireNonNullElse(procedure, ""));
            this.date.set(date);
            this.notes.set(Objects.requireNonNullElse(notes, ""));
        }
        public String toDisplayString() {
            StringBuilder sb = new StringBuilder(procedure.get());
            if (date.get() != null) sb.append("  (").append(DTF.format(date.get())).append(')');
            if (!isBlank(notes.get())) sb.append("  — ").append(oneLine(notes.get()));
            return sb.toString();
        }
        @Override public String toString() { return toDisplayString(); }
    }

    public static final class PMHData {
        public final StringProperty patientId = new SimpleStringProperty();
        public final StringProperty physician = new SimpleStringProperty();
        public final ObjectProperty<LocalDate> visitDate = new SimpleObjectProperty<>();
        public final ObservableList<PMHItem> pmhItems = FXCollections.observableArrayList();
        public final ObservableList<SurgeryItem> surgeries = FXCollections.observableArrayList();
        public final StringProperty allergies = new SimpleStringProperty();
        public final StringProperty medications = new SimpleStringProperty();
        public final StringProperty familyHx = new SimpleStringProperty();
        public final StringProperty socialHx = new SimpleStringProperty();
    }
}
