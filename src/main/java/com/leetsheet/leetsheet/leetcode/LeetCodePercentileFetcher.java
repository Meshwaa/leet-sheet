package com.leetsheet.leetsheet.leetcode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LeetCodePercentileFetcher {

    public static void main(String[] args) {
        String questionSlug = "find-largest-value-in-each-tree-row"; // Problem slug
        String sessionCookie = "YOUR_SESSION_COOKIE"; // Replace with your valid session cookie

        // API Endpoint
        String url = "https://leetcode.com/graphql";

        // GraphQL payload
        String payload = "{"
                + "\"query\":\"query lastAcSubmissionCheck($questionSlug: String!) { lastAcSubmission(questionSlug: $questionSlug) { id runtimePercentile memoryPercentile } }\","
                + "\"variables\":{"
                + "\"questionSlug\":\"" + questionSlug + "\""
                + "},"
                + "\"operationName\":\"lastAcSubmissionCheck\""
                + "}";

        try {
            // Create HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Create HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "LEETCODE_SESSION=" + sessionCookie) // Pass session cookie for authentication
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            // Send request and receive response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response.body());

                // Extract last accepted submission details
                JsonNode lastAcSubmission = jsonResponse.at("/data/lastAcSubmission");
                if (!lastAcSubmission.isMissingNode()) {
                    String id = lastAcSubmission.path("id").asText("N/A");
                    String runtimePercentile = lastAcSubmission.path("runtimePercentile").asText("N/A");
                    String memoryPercentile = lastAcSubmission.path("memoryPercentile").asText("N/A");

                    // Print the results
                    System.out.println("Submission ID: " + id);
                    System.out.println("Runtime Percentile: " + runtimePercentile);
                    System.out.println("Memory Percentile: " + memoryPercentile);
                } else {
                    System.out.println("No data found for the last accepted submission.");
                }
            } else {
                // Handle non-200 responses
                System.out.println("Request failed: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
