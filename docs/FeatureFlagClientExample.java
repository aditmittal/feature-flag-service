package com.example.featureflag.docs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FeatureFlagClientExample {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {

        String project = "payments";
        String flag = "checkout-v2";
        String user = "alice";

        String url = String.format(
                "http://localhost:8080/projects/%s/eval?flag=%s&user=%s",
                project,
                flag,
                user
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + response.statusCode());
        System.out.println("Evaluation result: " + response.body());
    }
}