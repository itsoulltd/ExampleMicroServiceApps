package com.infoworks.fileprocessing.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentParsingService {

    public enum SupportedExtension{
        Xlsx(".xlsx"), Xls(".xls");

        private String value;

        SupportedExtension(String value) {
            this.value = value;
        }

        public String value(){return value;}
    }

    public Map<Integer, List<String>> read(InputStream inputStream, String fileName) throws IOException {
        if (fileName.endsWith(SupportedExtension.Xlsx.value())){
            Workbook workbook = new XSSFWorkbook(inputStream);
            Map res = parseContent(workbook);
            workbook.close();
            return res;
        }else if(fileName.endsWith(SupportedExtension.Xls.value())){
            Workbook workbook = new HSSFWorkbook(inputStream);
            Map res = parseContent(workbook);
            workbook.close();
            return res;
        }
        return new HashMap<>();
    }

    private Map<Integer, List<String>> parseContent(Workbook workbook) {
        //DoTheMath:
        Sheet sheet = workbook.getSheetAt(0);
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

    public void write(OutputStream outputStream, String fileName, Map<Integer, List<String>> data) throws IOException {
        Workbook workbook = null;
        if (fileName.endsWith(SupportedExtension.Xlsx.value())){
            workbook = new XSSFWorkbook();
        }else if(fileName.endsWith(SupportedExtension.Xls.value())){
            workbook = new HSSFWorkbook();
        }
        if (workbook != null){
            writeContent(workbook, data);
            workbook.write(outputStream);
            workbook.close();
        }
    }

    private void writeContent(Workbook workbook, Map<Integer, List<String>> data) throws IOException {
        //DoTheMath:
        Sheet sheet = workbook.createSheet();
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
