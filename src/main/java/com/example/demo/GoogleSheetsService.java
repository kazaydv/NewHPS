package com.example.demo;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Easy Spray Order System";
    private static final String SPREADSHEET_ID = "1d5bL0IOwCJ9BBqIhs_LX2LZ7SVAYB8D3Af0swE0oSKg";

    public void addOrderToSheet(Order order) throws IOException, GeneralSecurityException {
        // 1. Load Credentials
        InputStream in = GoogleSheetsService.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new IOException("File not found: credentials.json in resources folder");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));


        Sheets service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // 3. Find the ABSOLUTE last row (Ignoring gaps in the middle)
        // We read Column A to count all existing rows including your manual gaps
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "Sheet1!A:A")
                .execute();

        List<List<Object>> values = response.getValues();
        int trueLastRow = (values == null) ? 0 : values.size();

        // 4. Set Nepal Timestamp (UTC+5:45)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String nepalTime = ZonedDateTime.now(ZoneId.of("Asia/Kathmandu")).format(formatter);

        // 5. Prepare the data row
        List<Object> dataRow = Arrays.asList(
                nepalTime,
                order.getName(),
                order.getPhone(),
                order.getAddress(),
                order.getProvince(),
                order.getPackageType(),
                order.getTotal()
        );

        ValueRange body = new ValueRange().setValues(Collections.singletonList(dataRow));

        // 6. Use .update() instead of .append()
        // This targets the row exactly after the last occupied row in the sheet
        String targetRange = "Sheet1!A" + (trueLastRow + 1);

        service.spreadsheets().values()
                .update(SPREADSHEET_ID, targetRange, body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        System.out.println("Order successfully saved at row " + (trueLastRow + 1) + " (Nepal Time: " + nepalTime + ")");
    }
}