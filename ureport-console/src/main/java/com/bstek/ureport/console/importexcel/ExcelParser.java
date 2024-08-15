/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.console.importexcel;

import com.bstek.ureport.definition.CellStyle;
import com.bstek.ureport.definition.PaperSize;
import com.bstek.ureport.definition.*;
import com.bstek.ureport.definition.value.ExpressionValue;
import com.bstek.ureport.definition.value.SimpleValue;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class ExcelParser {


    public List<ReportDefinition> parseAll(InputStream inputStream) throws Exception {
        XSSFWorkbook book = new XSSFWorkbook(inputStream);

        List<ReportDefinition> list = new ArrayList<>();
        for (int i = 0; i < book.getNumberOfSheets(); i++) {
            XSSFSheet sheet = book.getSheetAt(i);
            ReportDefinition reportDefinition = parseSheet(sheet);
            list.add(reportDefinition);
        }


        book.close();
        inputStream.close();
        return list;
    }

    public ReportDefinition parse(InputStream inputStream) throws Exception {
        XSSFWorkbook book = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = book.getSheetAt(0);

        ReportDefinition reportDefinition = parseSheet(sheet);

        book.close();
        inputStream.close();
        return reportDefinition;
    }

    private ReportDefinition parseSheet(XSSFSheet sheet) {
        ReportDefinition report = new ReportDefinition();
        List<RowDefinition> rowDefs = new ArrayList<>();
        report.setRows(rowDefs);
        List<ColumnDefinition> columnDefs = new ArrayList<>();
        report.setColumns(columnDefs);
        List<CellDefinition> cellDefs = new ArrayList<>();
        report.setCells(cellDefs);


        report.setReportFullName(sheet.getSheetName());


        int firstRow = 0;
        int rowCount = sheet.getPhysicalNumberOfRows();
        int maxColumnCount = buildMaxColumn(sheet);
        for (int i = firstRow; i < rowCount; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) {
                RowDefinition rowDef = new RowDefinition();
                rowDef.setHeight(20);
                rowDef.setRowNumber(i + 1);
                rowDefs.add(rowDef);
                addBlankCells(cellDefs, i + 1, maxColumnCount);
                continue;
            }
            RowDefinition rowDef = new RowDefinition();
            int height = row.getHeight() / 20;
            rowDef.setHeight(height);
            rowDef.setRowNumber(i + 1);
            rowDefs.add(rowDef);
            for (int j = 0; j < maxColumnCount; j++) {
                boolean isMergeRegion = isMergedRegion(sheet, i, j);
                if (isMergeRegion) {
                    continue;
                }
                XSSFCell cell = row.getCell(j);
                if (cell == null) {
                    CellDefinition cellDef = new CellDefinition();
                    cellDef.setValue(new SimpleValue(""));
                    cellDef.setRowNumber(i + 1);
                    cellDef.setColumnNumber(j + 1);
                    cellDef.setLeftParentCellName("root");
                    cellDef.setTopParentCellName("root");

                    cellDefs.add(cellDef);
                    continue;
                }
                Span span = getSpan(sheet, i, j);

                Object value = null;
                CellType cellType = cell.getCellType();

                String formula = null;
                switch (cellType) {
                    case STRING:
                        value = cell.getStringCellValue();
                        if (isFormal((String) value)) {
                            formula = ((String) value).substring(1);
                        }
                        break;
                    case BOOLEAN:
                        value = cell.getBooleanCellValue();
                        break;
                    case NUMERIC:
                        value = cell.getNumericCellValue();
                        break;
                    case FORMULA:
                        formula = cell.getCellFormula();
                        break;
                    default:
                        value = "";
                }
                CellDefinition cellDef = new CellDefinition();

                if (formula != null) {
                    ExpressionValue expressionValue = new ExpressionValue(formula);
                    cellDef.setValue(expressionValue);
                } else {
                    SimpleValue simpleValue = new SimpleValue(value != null ? value.toString() : "");
                    cellDef.setValue(simpleValue);
                }

                cellDef.setLeftParentCellName("root");
                cellDef.setTopParentCellName("root");
                cellDef.setRowNumber(i + 1);
                cellDef.setColumnNumber(j + 1);
                cellDef.setRowSpan(span.getRowSpan());
                cellDef.setColSpan(span.getColSpan());
                cellDef.setCellStyle(buildCellStyle(cell, sheet.getWorkbook()));
                cellDef.setName(convertToExcelCoordinate(i, j));
                cellDefs.add(cellDef);
            }
        }
        for (int i = 0; i < maxColumnCount; i++) {
            ColumnDefinition col = new ColumnDefinition();
            int width = sheet.getColumnWidth(i);
            col.setWidth(width / 37);
            col.setColumnNumber(i + 1);
            columnDefs.add(col);
        }

        Paper paper = new Paper();
        paper.setPaperType(PaperType.A4);
        paper.setOrientation(Orientation.portrait);
        paper.setPagingMode(PagingMode.fitpage);
        PaperSize pageSize = PaperType.A4.getPaperSize();
        paper.setWidth(pageSize.getWidth());
        paper.setHeight(paper.getHeight());
        report.setPaper(paper);

        return report;
    }


    private CellStyle buildCellStyle(XSSFCell cell, XSSFWorkbook book) {
        CellStyle style = new CellStyle();
        XSSFCellStyle cellStyle = cell.getCellStyle();
        HorizontalAlignment align = cellStyle.getAlignment();
        if (align.equals(HorizontalAlignment.CENTER)) {
            style.setAlign(Alignment.center);
        } else if (align.equals(HorizontalAlignment.RIGHT)) {
            style.setAlign(Alignment.right);
        } else {
            style.setAlign(Alignment.left);
        }
        VerticalAlignment valign = cellStyle.getVerticalAlignment();
        if (valign.equals(VerticalAlignment.BOTTOM)) {
            style.setValign(Alignment.bottom);
        } else if (valign.equals(VerticalAlignment.TOP)) {
            style.setValign(Alignment.top);
        } else if (valign.equals(VerticalAlignment.CENTER)) {
            style.setValign(Alignment.middle);
        } else {
            style.setValign(Alignment.middle);
        }
        XSSFFont font = cellStyle.getFont();
        if (font.getBold()) {
            style.setBold(true);
        }
        if (font.getItalic()) {
            style.setItalic(true);
        }
        if (font.getUnderline() != Font.U_NONE) {
            style.setUnderline(true);
        }
        XSSFColor color = font.getXSSFColor();
        if (color != null) {
            String rgb = color.getARGBHex();
            style.setForecolor(hex2Rgb(rgb));
        } else {
            style.setForecolor("0,0,0");
        }
        FillPatternType pattern = cellStyle.getFillPattern();
        if (pattern != null && pattern.equals(FillPatternType.SOLID_FOREGROUND)) {
            XSSFColor bgcolor = cellStyle.getFillForegroundColorColor();
            if (bgcolor != null) {
                String hex = bgcolor.getARGBHex();
                style.setBgcolor(hex2Rgb(hex));
            }
        }
        int fontSize = font.getFontHeight() / 20;
        style.setFontSize(fontSize);
        BorderStyle borderStyle = cellStyle.getBorderLeft();
        if (!borderStyle.equals(BorderStyle.NONE)) {
            Border border = new Border();
            border.setColor("0,0,0");
            border.setStyle(com.bstek.ureport.definition.BorderStyle.solid);
            border.setWidth(1);
            style.setLeftBorder(border);
        }
        borderStyle = cellStyle.getBorderRight();
        if (!borderStyle.equals(BorderStyle.NONE)) {
            Border border = new Border();
            border.setColor("0,0,0");
            border.setStyle(com.bstek.ureport.definition.BorderStyle.solid);
            border.setWidth(1);
            style.setRightBorder(border);
        }
        borderStyle = cellStyle.getBorderTop();
        if (!borderStyle.equals(BorderStyle.NONE)) {
            Border border = new Border();
            border.setColor("0,0,0");
            border.setStyle(com.bstek.ureport.definition.BorderStyle.solid);
            border.setWidth(1);
            style.setTopBorder(border);
        }
        borderStyle = cellStyle.getBorderBottom();
        if (!borderStyle.equals(BorderStyle.NONE)) {
            Border border = new Border();
            border.setColor("0,0,0");
            border.setStyle(com.bstek.ureport.definition.BorderStyle.solid);
            border.setWidth(1);
            style.setBottomBorder(border);
        }
        return style;
    }

    private String hex2Rgb(String colorStr) {
        return Integer.valueOf(colorStr.substring(2, 4), 16) + "," +
               Integer.valueOf(colorStr.substring(4, 6), 16) + "," +
               Integer.valueOf(colorStr.substring(6, 8), 16);
    }

    private Span getSpan(XSSFSheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            if (row == firstRow && column == firstColumn) {
                int lastRow = range.getLastRow();
                int rowSpan = lastRow - firstRow;
                if (rowSpan > 0) {
                    rowSpan++;
                }
                int colSpan = lastColumn - firstColumn;
                if (colSpan > 0) {
                    colSpan++;
                }
                return new Span(rowSpan, colSpan);
            }
        }
        return new Span(0, 0);
    }

    private boolean isMergedRegion(XSSFSheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row > firstRow && row < lastRow) {
                if (column > firstColumn && column < lastColumn) {
                    return true;
                }
            }
        }
        return false;
    }

    private int buildMaxColumn(XSSFSheet sheet) {
        int rowCount = sheet.getPhysicalNumberOfRows();
        int maxColumnCount = 0;
        for (int i = 0; i < rowCount; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            int columnCount = row.getPhysicalNumberOfCells();
            if (columnCount > maxColumnCount) {
                maxColumnCount = columnCount;
            }
        }
        return maxColumnCount;
    }

    protected void addBlankCells(List<CellDefinition> cellDefs, int rowNumber, int totalColumn) {
        for (int i = 0; i < totalColumn; i++) {
            CellDefinition cellDef = new CellDefinition();
            cellDef.setValue(new SimpleValue(""));
            cellDef.setRowNumber(rowNumber);
            cellDef.setColumnNumber(i + 1);
            cellDefs.add(cellDef);
        }
    }


    public static String convertToExcelCoordinate(int row, int column) {
        StringBuilder sb = new StringBuilder();

        while (column >= 0) {
            sb.insert(0, (char) ('A' + column % 26));
            column = column / 26 - 1;
        }

        sb.append(row + 1);

        return sb.toString();
    }


    public static void main(String[] args) {
        String v = "=C6+C7-C1*C2/C5+4";
        System.out.println(isFormal(v));
    }

    private static boolean isFormal(String v) {
        if (v == null || v.isEmpty()) {
            return false;
        }
        if (!v.startsWith("=")) {
            return false;
        }

        v = v.substring(1);
        System.out.println(v);

        String[] split = v.split("\\+|-|\\*|/");
        for (String s : split) {
            s = s.trim();

            // 判断是否Excel坐标或数字
            boolean ok = isExcelCoordsOrNumber(s);
            System.out.println(s + "：" + ok);
            if (!ok) {
                return false;
            }


        }
        return true;
    }

    private static boolean isExcelCoordsOrNumber(String v) {
        String pattern = "^[A-Z]{0,2}\\d+$"; // 正则表达式模式，匹配一个或两个字母加上一个或多个数字
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(v);
        return matcher.matches();
    }
}
