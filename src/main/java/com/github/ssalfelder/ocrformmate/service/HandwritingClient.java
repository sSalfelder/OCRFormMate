package com.github.ssalfelder.ocrformmate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class HandwritingClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String recognize(File imageFile) throws IOException, InterruptedException {
        HttpRequest request = buildMultipartRequest(imageFile);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseRecognizedText(response.body());
        } else {
            throw new RuntimeException("OCR error: " + response.statusCode());
        }
    }

    private HttpRequest buildMultipartRequest(File file) throws IOException {
        String boundary = "----OCRBoundary1234";
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        String part1 = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"\r\n"
                + "Content-Type: image/png\r\n\r\n";
        String part2 = "\r\n--" + boundary + "--\r\n";

        byte[] payload = ByteBuffer
                .allocate(part1.getBytes().length + fileBytes.length + part2.getBytes().length)
                .put(part1.getBytes())
                .put(fileBytes)
                .put(part2.getBytes())
                .array();

        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/handwriting/recognize"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();
    }

    private String parseRecognizedText(String json) {
        try {
            JsonNode root = mapper.readTree(json);

            if (root.has("skipped") && root.get("skipped").asBoolean()) {
                return "__SKIPPED__"; // Platzhalter für: "Nur vorgedruckter Text erkannt"
            }

            return root.has("text") ? root.get("text").asText() : "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
