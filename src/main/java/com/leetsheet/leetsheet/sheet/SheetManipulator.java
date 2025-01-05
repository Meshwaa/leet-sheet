package com.leetsheet.leetsheet.sheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.leetsheet.leetsheet.leetcode.LeetCodeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SheetManipulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SheetManipulator.class);
    @Value("${spreadsheet.id}")
    private String spreadsheetId;

    @Autowired
    private LeetCodeClient leetCodeClient;

    @Autowired
    private Sheets sheetsService;

    public void manipulateSheet() throws IOException {
        var sheets = sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets();

        for (var sheet : sheets) {
            var range = sheet.getProperties().getTitle() + "!C6:C"; // Fetch problem url for each row
            var response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            var values = response.getValues();
            var titleSlugs = new ArrayList<String>();
            for (var row : values) {
                for (var cell : row) {
                    var problemName = fetchProblemName((String) cell);
                    titleSlugs.add(problemName);
                }
            }
            updateStatusInSheet(titleSlugs, sheet);
            LOGGER.info("Successfully updated sheet: {}", sheet.getProperties().getTitle());
        }
    }

    private void updateStatusInSheet(List<String> titleSlugs, Sheet sheet) throws IOException {
        // Fetch the status list using LeetCodeClient
        var statusList = leetCodeClient.fetchLeetcodeStatuses(titleSlugs);

        var requests = new ArrayList<Request>();
        for (var i = 0; i < statusList.size(); i++) {
            var text = statusList.get(i);
            var color = getStatusColor(text);
            // Create format request to apply color
            requests.add(createTextFormatRequest(5 + i, color, sheet.getProperties().getSheetId(), text));
        }
        // Batch update the spreadsheet with formatting
        var batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        // Execute the batch update for formatting
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
    }

    private Request createTextFormatRequest(int rowIndex, Color color, Integer sheetId, String text) {
        var textFormat = new TextFormat().setForegroundColor(color);
        var cellFormat = new CellFormat().setTextFormat(textFormat);

        return new Request().setRepeatCell(new RepeatCellRequest()
                .setRange(new GridRange()
                        .setSheetId(sheetId) // Adjust sheet ID as needed
                        .setStartRowIndex(rowIndex)
                        .setEndRowIndex(rowIndex + 1)
                        .setStartColumnIndex(4) // For E column
                        .setEndColumnIndex(5))
                .setCell(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(text)).setUserEnteredFormat(cellFormat))
                .setFields("userEnteredValue,userEnteredFormat.textFormat.foregroundColor"));
    }

    private Color getStatusColor(String status) {
        var color = new Color();

        switch (status) {
            case "Solved" -> color.setGreen(1.6f); // Green for "Solved"
            case "Attempted" -> color.setRed(0.8f).setGreen(0.5f); // Yellow for "Attempted"
            default -> color.setRed(1.0f); // Red for "Pending"
        }
        return color;
    }

    private String fetchProblemName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}