/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.resumeanalyse;


import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFReader {
    
    public static String extractTextFromPDF(String filePath) {
        String text = "";
        try {
            File file = new File(filePath);
            PDDocument document = PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            text = pdfStripper.getText(document);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static void main(String[] args) {
        // Change this to the actual path of your PDF
        String pdfPath = "C:\\Users\\YourName\\Documents\\resume.pdf";
        String extractedText = extractTextFromPDF(pdfPath);
        System.out.println("---- Extracted Resume Text ----");
        System.out.println(extractedText);
    }
}

