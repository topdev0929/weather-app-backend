package com.weatherapi.controller;

import com.weatherapi.model.FiveDayHourlyWeather;
import com.weatherapi.model.Weather;
import com.weatherapi.service.WeatherService;
import com.weatherapi.countrycodes.CountryCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WeatherAPIControllerTest {

    @InjectMocks
    private WeatherAPIController weatherAPIController;

    @Mock
    private WeatherService weatherService;

    @Mock
    private Model model;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWeatherView() {
        weatherAPIController.getWeatherView(model, new CountryCodes());
        verify(model).addAttribute(eq("codes"), any());
    }

    @Test
    void testGetCurrentWeatherDataForCityAndCountry_Success() throws IOException {
        // Arrange
        Weather mockWeather = new Weather();
        when(weatherService.getWeatherDataCity("Berlin", "DE")).thenReturn(mockWeather);

        // Act
        ResponseEntity<?> response = weatherAPIController.getCurrentWeatherDataForCityAndCountry("Berlin", "DE", model);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockWeather);
    }

    @Test
    void testGetCurrentWeatherDataForCityAndCountry_NotFound() throws IOException {
        // Arrange
        when(weatherService.getWeatherDataCity("UnknownCity", "UnknownCountry")).thenReturn(null);

        // Act
        ResponseEntity<?> response = weatherAPIController.getCurrentWeatherDataForCityAndCountry("UnknownCity",
                "UnknownCountry", model);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Weather data not found");
    }

    @Test
    void testGetFiveDayForecast_Success() throws IOException {
        // Arrange
        Map<String, List<FiveDayHourlyWeather>> mockFiveDay = new HashMap<>();
        mockFiveDay.put("2024-10-17", new ArrayList<>());
        when(weatherService.getHourlyWeather("Berlin", "DE")).thenReturn(mockFiveDay);

        // Act
        ResponseEntity<?> response = weatherAPIController.getFiveDayForecast("Berlin", "DE", model);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody).containsKey("days");
        assertThat(responseBody).containsKey("weatherData");
    }

    @Test
    void testGetFiveDayForecast_NotFound() throws IOException {
        // Arrange
        when(weatherService.getHourlyWeather("UnknownCity", "UnknownCountry")).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<?> response = weatherAPIController.getFiveDayForecast("UnknownCity", "UnknownCountry", model);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Weather data not found");
    }

    @Test
    void testErrorPage() {
        // Act
        String viewName = weatherAPIController.errorPage(model);

        // Assert
        verify(model).addAttribute(eq("codes"), any());
        assertThat(viewName).isEqualTo("weather_view");
    }
}
