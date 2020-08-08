package com.infoworks.fileprocessing.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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

    public Map<Integer, List<String>> read(InputStream file, String fileName) throws IOException {
        if (fileName.endsWith(SupportedExtension.Xlsx.value())){
            return parseContent(new XSSFWorkbook(file));
        }else if(fileName.endsWith(SupportedExtension.Xls.value())){
            return parseContent(new HSSFWorkbook(file));
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

}
