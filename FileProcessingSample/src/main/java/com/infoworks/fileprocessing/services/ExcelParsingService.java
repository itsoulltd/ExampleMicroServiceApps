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
import java.util.function.Consumer;

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

    public void read(InputStream inputStream
            , Integer bufferSize
            , Integer sheetAt
            , Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        //TODO:
        /*Workbook workbook = StreamingReader.builder()
                .rowCacheSize(pageSize)
                .bufferSize(bufferSize)
                .open(inputStream);*/
        Workbook workbook = WorkbookFactory.create(inputStream);
        readAsync(workbook, sheetAt, pageSize, consumer);
        workbook.close();
    }

    public void read(File file
            , Integer bufferSize
            , Integer sheetAt
            , Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        //TODO:
        /*Workbook workbook = StreamingReader.builder()
                .rowCacheSize(pageSize)
                .bufferSize(bufferSize)
                .open(inputStream);*/
        Workbook workbook = WorkbookFactory.create(file);
        readAsync(workbook, sheetAt, pageSize, consumer);
        workbook.close();
    }

    private void readAsync(Workbook workbook
            , Integer sheetAt, Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        //
        Sheet sheet = workbook.getSheetAt(sheetAt);
        int maxCount = sheet.getLastRowNum() + 1;
        int loopCount = (maxCount / pageSize) + 1;
        int index = 0;
        int start = 0;
        while (index < loopCount){
            int end = start + pageSize;
            if (end >= maxCount) end = maxCount;
            Map res = parseContent(workbook, sheetAt, start, end);
            if (consumer != null){
                consumer.accept(res);
            }
            //
            start += pageSize;
            index++;
        }
    }

    public Map<Integer, List<String>> read(InputStream inputStream, Integer sheetAt, Integer start, Integer end) throws IOException {
        Workbook workbook = WorkbookFactory.create(inputStream);
        Map res = parseContent(workbook, sheetAt, start, end);
        workbook.close();
        return res;
    }

    public Map<Integer, List<String>> read(File file, Integer sheetAt, Integer start, Integer end) throws IOException {
        Workbook workbook = WorkbookFactory.create(file);
        Map res = parseContent(workbook, sheetAt, start, end);
        workbook.close();
        return res;
    }

    private Map<Integer, List<String>> parseContent(Workbook workbook, Integer sheetAt, Integer start, Integer end) throws IOException {
        //DoTheMath:
        Sheet sheet = workbook.getSheetAt(sheetAt);
        Map<Integer, List<String>> data = new HashMap<>();
        //
        if (end <= 0 || end == Integer.MAX_VALUE){
            end = sheet.getLastRowNum() + 1;
        }
        start = (start < 0) ? 0 : start;
        int idx = (start >= end) ? 0 : start;
        //
        while (idx < end) {
            data.put(idx, new ArrayList<>());
            for (Cell cell : sheet.getRow(idx)) {
                addInto(data, idx, cell);
            }
            idx++;
        }
        return data;
    }

    private void addInto(Map<Integer, List<String>> data, int idx, Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case STRING:
                data.get(new Integer(idx)).add(cell.getRichStringCellValue().getString());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    data.get(idx).add(cell.getDateCellValue() + "");
                } else {
                    data.get(idx).add(cell.getNumericCellValue() + "");
                }
                break;
            case BOOLEAN:
                data.get(idx).add(cell.getBooleanCellValue() + "");
                break;
            default:
                data.get(new Integer(idx)).add(" ");
        }
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
        int rowIndex = 0;
        for (Map.Entry<Integer, List<String>> entry : data.entrySet()){
            Row row = sheet.createRow(rowIndex);
            int cellIndex = 0;
            for (String cellVal : entry.getValue()) {
                Cell cell = row.createCell(cellIndex);
                cell.setCellValue(cellVal);
                sheet.autoSizeColumn(cellIndex);
                cellIndex++;
            }
            rowIndex++;
        }
    }

}
