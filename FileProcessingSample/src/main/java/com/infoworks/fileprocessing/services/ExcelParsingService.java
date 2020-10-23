package com.infoworks.fileprocessing.services;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelParsingService {

    public enum SupportedExtension{
        Xlsx(".xlsx"), Xls(".xls");

        private String value;

        SupportedExtension(String value) {
            this.value = value;
        }

        public String value(){return value;}
    }

    public Map<Integer, List<String>> read(InputStream inputStream, Integer sheetAt) throws IOException {
        Workbook workbook = WorkbookFactory.create(inputStream);
        Map res = parseContent(workbook, sheetAt);
        workbook.close();
        return res;
    }

    public Map<Integer, List<String>> read(File file, Integer sheetAt) throws IOException {
        Workbook workbook = WorkbookFactory.create(file);
        Map res = parseContent(workbook, sheetAt);
        workbook.close();
        return res;
    }

    private Map<Integer, List<String>> parseContent(Workbook workbook, Integer sheetAt) throws IOException {
        //DoTheMath:
        Sheet sheet = workbook.getSheetAt(sheetAt);
        Map<Integer, List<String>> data = new HashMap<>();
        int i = 0;
        for (Row row : sheet) {
            data.put(i, new ArrayList<>());
            for (Cell cell : row) {
                switch (cell.getCellTypeEnum()) {
                    case STRING:
                        data.get(new Integer(i)).add(cell.getRichStringCellValue().getString());
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            data.get(i).add(cell.getDateCellValue() + "");
                        } else {
                            data.get(i).add(cell.getNumericCellValue() + "");
                        }
                        break;
                    case BOOLEAN:
                        data.get(i).add(cell.getBooleanCellValue() + "");
                        break;
                    default:
                        data.get(new Integer(i)).add(" ");
                }
            }
            i++;
        }
        return data;
    }

    public void write(boolean xssf, OutputStream outputStream, String sheetName, Map<Integer, List<String>> data) throws IOException {
        Workbook workbook = WorkbookFactory.create(xssf);
        if (workbook != null){
            writeContent(workbook, sheetName, data);
            workbook.write(outputStream);
            workbook.close();
        }
    }

    private void writeContent(Workbook workbook, String sheetName, Map<Integer, List<String>> data) throws IOException {
        //DoTheMath:
        Sheet sheet = workbook.createSheet(sheetName);
        for (Map.Entry<Integer, List<String>> entry : data.entrySet()){
            Row row = sheet.createRow(entry.getKey());
            int cellIndex = 0;
            for (String cellVal : entry.getValue()) {
                Cell cell = row.createCell(cellIndex);
                cell.setCellValue(cellVal);
                sheet.autoSizeColumn(cellIndex);
                cellIndex++;
            }
        }
    }

}
