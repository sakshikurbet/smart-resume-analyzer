package com.mycompany.resumeanalyse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.regex.*;

public class ResumeGUI extends JFrame {

    private JTextArea textArea;
    private JButton uploadButton, analyzeButton, clearDBButton;
    private JLabel titleLabel;
    private JComboBox<String> roleDropdown;
    private String resumeText = "";

    public ResumeGUI() {
        setTitle("Resume Analyzer - Phase 2");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Title
        titleLabel = new JLabel("Resume Analyzer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        uploadButton = new JButton("Upload Resume (PDF)");
        uploadButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        uploadButton.setBackground(new Color(0, 102, 204));
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFocusPainted(false);
        uploadButton.addActionListener(new UploadAction());

        String[] roles = {"Web Developer", "Backend Developer", "Data Analyst", "Android Developer"};
        roleDropdown = new JComboBox<>(roles);
        roleDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        analyzeButton = new JButton("Analyze Resume");
        analyzeButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        analyzeButton.setBackground(new Color(0, 153, 51));
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setFocusPainted(false);
        analyzeButton.addActionListener(e -> analyzeResume());

        clearDBButton = new JButton("Clear Database");
        clearDBButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        clearDBButton.setBackground(new Color(204, 0, 0));
        clearDBButton.setForeground(Color.WHITE);
        clearDBButton.setFocusPainted(false);
        clearDBButton.addActionListener(e -> clearDatabase());

        JPanel topPanel = new JPanel(new BorderLayout(10,10));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel uploadPanel = new JPanel();
        uploadPanel.add(uploadButton);
        uploadPanel.add(new JLabel("Select Role:"));
        uploadPanel.add(roleDropdown);
        topPanel.add(uploadPanel, BorderLayout.CENTER);

        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(analyzeButton);
        bottomPanel.add(clearDBButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(mainPanel);
    }

    private class UploadAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                File selectedFile = fileChooser.getSelectedFile();
                loadResume(selectedFile);
            }
        }
    }

    private void loadResume(File file){
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            resumeText = stripper.getText(document);
            textArea.setText(resumeText);
            JOptionPane.showMessageDialog(this, "Resume loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Error reading PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void analyzeResume(){
        if(resumeText.isEmpty()){
            JOptionPane.showMessageDialog(this, "Please upload a resume first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = extractName(resumeText);
        String email = extractEmail(resumeText);
        String phone = extractPhone(resumeText);
        String education = extractEducation(resumeText);

        String selectedRole = roleDropdown.getSelectedItem().toString();
        SkillMatchResult result = getSkillMatch(resumeText, selectedRole);

        saveToDatabase(name, email, phone, result.matchedSkills, education, selectedRole, result.score);

        JOptionPane.showMessageDialog(this,
                "✅ Resume Analyzed Successfully!\n\n" +
                "Candidate: " + name + "\n" +
                "Role: " + selectedRole + "\n" +
                "Match Score: " + result.score + "%\n" +
                "Matched Skills: " + result.matchedSkills,
                "Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private String extractName(String text){
        String[] lines = text.split("\n");
        for(String line : lines){
            if(line.trim().length() > 3 && !line.contains("@")) return line.trim();
        }
        return "Unknown";
    }

    private String extractEmail(String text){
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "Not Found";
    }

    private String extractPhone(String text){
        Pattern pattern = Pattern.compile("\\+?\\d[\\d\\s\\-]{7,}\\d");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "Not Found";
    }

    private String extractEducation(String text){
        String[] educationKeywords = {"B.Tech", "M.Tech", "B.Sc", "M.Sc", "MBA", "PhD", "High School", "BCA", "MCA"};
        StringBuilder education = new StringBuilder();
        for(String edu : educationKeywords){
            if(text.contains(edu)) education.append(edu).append(", ");
        }
        if(education.length() > 0) education.setLength(education.length()-2);
        return education.length() > 0 ? education.toString() : "Not Found";
    }

    private class SkillMatchResult{
        int score;
        String matchedSkills;
    }

    private SkillMatchResult getSkillMatch(String text, String role){
        String[] skillsList;
        switch(role){
            case "Web Developer":
                skillsList = new String[]{"HTML","CSS","JavaScript","React","Node.js"};
                break;
            case "Backend Developer":
                skillsList = new String[]{"Java","Spring Boot","Python","Django","MySQL"};
                break;
            case "Data Analyst":
                skillsList = new String[]{"Python","SQL","Excel","Power BI","Pandas"};
                break;
            case "Android Developer":
                skillsList = new String[]{"Java","Kotlin","Android Studio","Firebase"};
                break;
            default:
                skillsList = new String[]{};
        }

        StringBuilder matched = new StringBuilder();
        int count = 0;
        for(String skill : skillsList){
            if(text.toLowerCase().contains(skill.toLowerCase())){
                count++;
                matched.append(skill).append(", ");
            }
        }
        if(matched.length() > 0) matched.setLength(matched.length()-2);

        SkillMatchResult result = new SkillMatchResult();
        result.score = skillsList.length>0 ? (count*100)/skillsList.length : 0;
        result.matchedSkills = matched.toString().isEmpty() ? "None" : matched.toString();
        return result;
    }

    private void saveToDatabase(String name, String email, String phone, String skills,
                                String education, String role, int score){
        try(Connection conn = DBConnection.getConnection()){
            String sql = "INSERT INTO candidate_resumes (name,email,phone,skills,education,role,score) VALUES(?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, skills);
            ps.setString(5, education);
            ps.setString(6, role);
            ps.setInt(7, score);

            ps.executeUpdate();
            ps.close();
        } catch(SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Database Error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDatabase() {

    // Ask user for ID
    String input = JOptionPane.showInputDialog(
            this,
            "Enter Resume ID to delete:",
            "Delete Resume",
            JOptionPane.QUESTION_MESSAGE
    );

    // If user cancels
    if (input == null || input.trim().isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "⚠ Deletion cancelled or ID not entered.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    int id;
    try {
        id = Integer.parseInt(input);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
                this,
                "❌ Please enter a valid numeric ID.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete resume with ID = " + id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "DELETE FROM candidate_resumes WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "✅ Resume deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "⚠ No resume found with ID = " + id,
                        "Not Found",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Database Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}



    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new ResumeGUI().setVisible(true));
    }
}
