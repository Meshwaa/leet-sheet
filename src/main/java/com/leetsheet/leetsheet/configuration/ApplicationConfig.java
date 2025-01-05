package com.leetsheet.leetsheet.configuration;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class ApplicationConfig {
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private final List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    @Bean
    public Sheets getSheetsService(@Value("${credentials.file.path}") String credentialsFilePath) throws IOException,
            GeneralSecurityException {
        var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        var applicationName = "Google Sheets Manipulation";
        return new Sheets.Builder(HTTP_TRANSPORT, jsonFactory, authorize(HTTP_TRANSPORT, credentialsFilePath)).setApplicationName(applicationName).build();
    }

    public Credential authorize(NetHttpTransport HTTP_TRANSPORT, String credentialsFilePath) throws IOException {
        var in = ApplicationConfig.class.getResourceAsStream(credentialsFilePath);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + credentialsFilePath);
        }
        var clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
        var flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, jsonFactory,
                clientSecrets, scopes).setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens"))).setAccessType("offline").build();
        var receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
