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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Easy Spray Order System";

    // तपाईँको Spreadsheet ID यहाँ सुरक्षित छ
    private static final String SPREADSHEET_ID = "1d5bL0IOwCJ9BBqIhs_LX2LZ7SVAYB8D3Af0swE0oSKg";

    public void addOrderToSheet(Order order) throws IOException, GeneralSecurityException {
        // १. Credentials लोड गर्नुहोस्
        InputStream in = GoogleSheetsService.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new IOException("फाइल फेला परेन: credentials.json (resources फोल्डरमा राख्नुहोस्)");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        // २. Sheets API Service सेटअप गर्नुहोस्
        Sheets service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // ३. मिति र समयलाई सफा बनाउनुहोस् (उदा: 2026-02-12 17:30)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String cleanTimestamp = LocalDateTime.now().format(formatter);

        // ४. सिटमा राख्ने डाटाको लहर (Row) तयार पार्नुहोस्
        List<Object> dataRow = Arrays.asList(
                cleanTimestamp,       // सफा मिति र समय
                order.getName(),
                order.getPhone(),
                order.getAddress(),
                order.getProvince(),
                order.getPackageType(),
                order.getTotal()
        );

        ValueRange body = new ValueRange().setValues(Arrays.asList(dataRow));

        // ५. डाटा थप्नुहोस् (Sheet1!A1 ले पहिलो खाली रो मा डाटा थप्छ)
        service.spreadsheets().values()
                .append(SPREADSHEET_ID, "Sheet1!A1", body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        System.out.println("सफलतापूर्वक गुगल सिटमा डाटा पठाइयो: " + cleanTimestamp);
    }
}