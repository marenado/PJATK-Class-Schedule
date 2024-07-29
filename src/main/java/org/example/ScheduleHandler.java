package org.example;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ScheduleHandler {
        private static String getFormDataAsString(Map<String, String> formData) {
            StringBuilder formBodyBuilder = new StringBuilder();
            for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
                if (formBodyBuilder.length() > 0)
                    formBodyBuilder.append("&");
                formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
            }
            return formBodyBuilder.toString();
        }

        public static String getSchedule(String date) throws IOException, InterruptedException {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            Map<String, String> formData = new HashMap<>();
            formData.put("DataPicker$dateInput", date);
            formData.put("DataPicker_dateInput_ClientState", "{\"enabled\":true,\"emptyMessage\":\"\",\"validationText\":\"" + date + "-00-00-00\",\"valueAsString\":\"" + date + "-00-00-00\",\"minDateStr\":\"1980-01-01-00-00-00\",\"maxDateStr\":\"2099-12-31-00-00-00\",\"lastSetTextBoxValue\":\"" + date + "\"}");
            formData.put("__EVENTTARGET", "DataPicker");

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
                    .uri(URI.create("https://planzajec.pjwstk.edu.pl/PlanOgolny3.aspx"))
                    .setHeader("User-Agent", "Mozilla/5.0") // add request header
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("referer", "https://planzajec.pjwstk.edu.pl/PlanOgolny3.aspx")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        }
}
