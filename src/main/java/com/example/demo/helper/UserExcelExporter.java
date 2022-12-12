package com.example.demo.helper;

import com.example.demo.entity.Response;
import com.example.demo.entity.SparePart;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class UserExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<SparePart> listUsers;
    private final static String NAME = "Spare Parts.xlsx";

    public UserExcelExporter(List<SparePart> list) {
        this.listUsers = list;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("SpareParts");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        createCell(row, 0, "Cost", style);
        createCell(row, 1, "Description", style);
        createCell(row, 2, "Url", style);

    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (SparePart sparePart : listUsers) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, sparePart.getCost(), style);
            createCell(row, columnCount++, sparePart.getDescription(), style);
            createCell(row, columnCount++, sparePart.getUrl(), style);
        }
    }

    public File export() throws IOException {
        writeHeaderLine();
        writeDataLines();
        File file = new File(NAME);
        FileOutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        return file;
    }
}