package com.leetsheet.leetsheet.leetcode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Component
public class LeetCodeClient {
    private static final String LEETCODE_GRAPHQL_ENDPOINT = "https://leetcode.com/graphql/";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_REFERER = "referer";
    private static final String HEADER_COOKIE = "Cookie";

    @Value("${leetcode.username}")
    private String leetcodeUsername;

    @Value("${leetcode.sessionId}")
    private String leetcodeSessionId;

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public List<String> fetchLeetcodeStatuses(List<String> titleSlugs) {
        var executorService = Executors.newFixedThreadPool(15);
        var tasks = new ArrayList<Callable<String>>();

        for (var titleSlug : titleSlugs) {
            tasks.add(() -> fetchStatusForTitleSlug(titleSlug));
        }
        try {
            var futures = executorService.invokeAll(tasks);
            var statuses = new ArrayList<String>();
            for (var future : futures) {
                statuses.add(future.get());
            }
            return statuses;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error while fetching statuses", e);
        } finally {
            executorService.shutdown();
        }
    }

    private String fetchStatusForTitleSlug(String titleSlug) {
        var query = createGraphQLQuery(titleSlug);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(LEETCODE_GRAPHQL_ENDPOINT))
                .header(HEADER_CONTENT_TYPE, "application/json")
                .header(HEADER_REFERER, "https://leetcode.com/" + leetcodeUsername + "/")
                .header(HEADER_COOKIE, "LEETCODE_SESSION=" + leetcodeSessionId)
                .POST(HttpRequest.BodyPublishers.ofString(query, StandardCharsets.UTF_8))
                .build();

        try {
            // Send the request and get the response
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return extractStatus(response.body());
            } else {
                throw new RuntimeException("Error while fetching status of problem " + titleSlug + " with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error while fetching status of problem " + titleSlug, e);
        }
    }

    private String extractStatus(String responseBody) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        var responseNode = objectMapper.readTree(responseBody);
        var statusNode = responseNode.path("data").path("question").path("status");
        return processStatus(statusNode);
    }

    private String createGraphQLQuery(String titleSlug) {
        return String.format(
                "{\"query\":\"query userQuestionStatus($titleSlug: String!) { "
                        + "question(titleSlug: $titleSlug) { "
                        + "status "
                        + "} "
                        + "}\",\"variables\":{\"titleSlug\":\"%s\"}}",
                titleSlug);
    }

    private String processStatus(JsonNode status) {
        if (status.asText().equals("ac")) {
            return "Solved";
        } else if (status.asText().equals("notac")) {
            return "Attempted";
        } else {
            return "Pending";
        }
    }
}
