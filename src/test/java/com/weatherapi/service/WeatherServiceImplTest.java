package com.weatherapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.weatherapi.dao.WeatherDAO;
import com.weatherapi.model.FiveDayHourlyWeather;
import com.weatherapi.model.Weather;

public class WeatherServiceImplTest {

    @Mock
    private WeatherDAO wDAO;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private String city = "Berlin";
    private String country = "DE";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetWeatherDataCity() throws IOException {
        // Prepare JSON response
        String jsonResponse = "{\"name\":\"Berlin\",\"sys\":{\"country\":\"DE\"},\"main\":{\"humidity\":80,\"pressure\":1012,\"temp\":288.15,\"feels_like\":14.0,\"temp_max\":16.0,\"temp_min\":14.0},\"timezone\":3600,\"weather\":[{\"main\":\"Clear\",\"description\":\"clear sky\"}]}";
        when(wDAO.getWeatherDataCity(city, country)).thenReturn(jsonResponse);

        Weather weather = weatherService.getWeatherDataCity(city, country);

        assertEquals("Berlin", weather.getCity());
        assertEquals("DE", weather.getCountryISOCode());
        assertEquals(80, weather.getHumidity());
        assertEquals(1012, weather.getPressure());
        assertEquals(15.0, weather.getTemperature());
        assertEquals("Clear", weather.getWeather());
        assertEquals("clear sky", weather.getWeatherDesc());
    }

    @Test
    public void testGetHourlyWeather() throws IOException {
        // Prepare a sample valid JSON response for hourly weather
        String hourlyWeatherResponse = "{"
                + "\"city\":{\"name\":\"Berlin\",\"country\":\"DE\"},"
                + "\"list\":["
                + "   {"
                + "      \"dt_txt\":\"2024-10-17 21:00:00\","
                + "      \"main\":{\"humidity\":80,\"pressure\":1012,\"temp\":288.15,\"temp_max\":16.0,\"temp_min\":14.0},"
                + "      \"weather\":[{\"main\":\"Clear\",\"description\":\"clear sky\"}]"
                + "   }"
                + "]"
                + "}";

        // Mock the DAO to return the predefined JSON response
        when(wDAO.getHourlyWeatherData(city, country)).thenReturn(hourlyWeatherResponse);

        // Call the method to test
        Map<String, List<FiveDayHourlyWeather>> hourlyWeather = weatherService.getHourlyWeather(city, country);

        // Debug output to ensure the returned map is correct
        System.out.println("Returned Hourly Weather Map: " + hourlyWeather); // Debugging output

        // Validate the parsed data
        assertNotNull(hourlyWeather, "Hourly weather map should not be null");
        assertEquals(1, hourlyWeather.size(), "There should be one day of hourly weather data");

        // Check the weather details for the first day
        List<FiveDayHourlyWeather> dailyWeatherList = hourlyWeather.get("Thursday"); // Adjust for actual day in your
                                                                                     // setup
        assertNotNull(dailyWeatherList, "The weather list for the day should not be null");
        assertEquals(1, dailyWeatherList.size(), "There should be one hourly forecast entry for this day");

        // Validate the details of the first hourly weather entry
        FiveDayHourlyWeather weatherEntry = dailyWeatherList.get(0);
        assertEquals(80, weatherEntry.getHumidity(), "Humidity should match");
        assertEquals(1012, weatherEntry.getPressure(), "Pressure should match");
        assertEquals(15.0, weatherEntry.getTemperature(), "Temperature should match");
        assertEquals("Clear", weatherEntry.getWeather(), "Weather condition should be 'Clear'");
        assertEquals("clear sky", weatherEntry.getWeatherDesc(), "Weather description should be 'clear sky'");
    }

}
