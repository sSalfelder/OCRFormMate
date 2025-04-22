package com.github.ssalfelder.ocrformmate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bytedeco.opencv.opencv_core.Rect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, String> recognizeWithFields(File imageFile, Map<String, Rect> fields, String formType) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // JSON für Feldkoordinaten erzeugen
        Map<String, int[]> jsonFields = new HashMap<>();
        for (Map.Entry<String, Rect> entry : fields.entrySet()) {
            Rect r = entry.getValue();
            jsonFields.put(entry.getKey(), new int[]{r.x(), r.y(), r.width(), r.height()});
        }
        String fieldsJson = mapper.writeValueAsString(jsonFields);

        // Multipart-Request vorbereiten
        String boundary = "----OCRBoundary1234";
        String part1 = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"image\"; filename=\"" + imageFile.getName() + "\"\r\n"
                + "Content-Type: image/png\r\n\r\n";
        String part2 = "\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"fields\"\r\n\r\n"
                + fieldsJson + "\r\n";
        String part3 = "\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"formType\"\r\n\r\n"
                + formType + "\r\n";
        String part4 = "--" + boundary + "--\r\n";

        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(part1.getBytes());
        body.write(imageBytes);
        body.write(part2.getBytes());
        body.write(part3.getBytes());
        body.write(part4.getBytes());

        // Anfrage an den neuen OCR-Endpoint
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:6000/handwriting/recognizeFields"))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // JSON → Map<String, String>
            return mapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});
        } else {
            throw new RuntimeException("OCR error: HTTP " + response.statusCode());
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
