package com.mephi.b23902.kts;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;

public class ExcelHandler {
    private static final String FILE_NAME = "leaderboard.xlsx";

    public ArrayList<ScoreRecord> loadLeaderboard() {
        ArrayList<ScoreRecord> leaderboard = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(FILE_NAME))) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Пропускаем заголовок
                String name = row.getCell(0).getStringCellValue();
                int score = (int) row.getCell(1).getNumericCellValue();
                leaderboard.add(new ScoreRecord(name, score));
            }
            workbook.close();
        } catch (Exception e) {
            // Если файл не существует, возвращаем пустой список
        }
        return leaderboard;
    }

    public void saveLeaderboard(ArrayList<ScoreRecord> leaderboard) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Leaderboard");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Score");

            for (int i = 0; i < leaderboard.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(leaderboard.get(i).name);
                row.createCell(1).setCellValue(leaderboard.get(i).score);
            }

            try (FileOutputStream fos = new FileOutputStream(new File(FILE_NAME))) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}