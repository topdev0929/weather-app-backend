package com.weatherapi.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherDAOImplTest {

    @Mock
    private OkHttpClient mockClient;

    @Mock
    private Call mockCall;

    @InjectMocks
    private WeatherDAOImpl weatherDAO;

    private String city = "Berlin";
    private String country = "DE";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetWeatherDataCity() throws IOException {
        // Mock the API response with fixed values for testing
        String jsonResponse = "{\"name\":\"Berlin\",\"sys\":{\"country\":\"DE\"},\"main\":{\"humidity\":80,\"pressure\":1012,\"temp\":288.15}}";

        // Create a real Response object
        Request request = new Request.Builder()
                .url("https://mockurl.com")
                .build();

        Response mockResponse = new Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(jsonResponse, okhttp3.MediaType.parse("application/json")))
                .build();

        // Setup the OkHttpClient and Response mocks
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        // Call the method to test
        String result = weatherDAO.getWeatherDataCity(city, country);

        // Use Jackson to parse the response and validate the structure
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(result);

        // Validate basic structure
        assertEquals("Berlin", rootNode.get("name").asText(), "City name should be Berlin");
        assertEquals("DE", rootNode.get("sys").get("country").asText(), "Country code should be DE");

        // Validate value ranges (you can adjust the ranges based on your needs)
        int humidity = rootNode.get("main").get("humidity").asInt();
        assertTrue(humidity >= 0 && humidity <= 100, "Humidity should be within valid range (0-100)");

        int pressure = rootNode.get("main").get("pressure").asInt();
        assertTrue(pressure >= 900 && pressure <= 1100, "Pressure should be within valid range (900-1100)");

        double temperature = rootNode.get("main").get("temp").asDouble();
        assertTrue(temperature >= 250 && temperature <= 320, "Temperature should be within valid range (250-320 K)");
    }

    @Test
    public void testGetHourlyWeatherData() throws IOException {
        // Mock the API response with fixed values for testing
        String jsonResponse = "{ \"city\": {\"name\": \"Berlin\", \"country\": \"DE\"}, \"list\": [{\"dt_txt\": \"2024-10-17 21:00:00\", \"main\": {\"temp\": 288.15}}]}";

        // Create a real Response object
        Request request = new Request.Builder()
                .url("https://mockurl.com")
                .build();

        Response mockResponse = new Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(jsonResponse, okhttp3.MediaType.parse("application/json")))
                .build();

        // Setup the OkHttpClient and Response mocks
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        // Call the method to test
        String result = weatherDAO.getHourlyWeatherData(city, country);

        // Use Jackson to parse the response and validate the structure
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(result);

        // Validate basic structure
        assertEquals("Berlin", rootNode.get("city").get("name").asText(), "City name should be Berlin");
        assertEquals("DE", rootNode.get("city").get("country").asText(), "Country code should be DE");

        // Validate the temperature of the first hourly entry
        JsonNode hourlyEntry = rootNode.get("list").get(0);
        double temperature = hourlyEntry.get("main").get("temp").asDouble();
        assertTrue(temperature >= 250 && temperature <= 320, "Temperature should be within valid range (250-320 K)");
    }
}
