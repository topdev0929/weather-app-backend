package com.weatherapi.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weatherapi.countrycodes.CountryCodes;
import com.weatherapi.model.FiveDayHourlyWeather;
import com.weatherapi.model.Weather;
import com.weatherapi.service.WeatherService;

@Controller
@RequestMapping("/")
public class WeatherAPIController implements ErrorController {

	private static final String ERROR_PATH = "/error";

	@Autowired
	WeatherService wService;

	private List<String> days;
	private List<List<FiveDayHourlyWeather>> weatherData;

	@RequestMapping(value = ERROR_PATH)
	public String errorPage(Model model) {

		CountryCodes codes = new CountryCodes();

		model.addAttribute("codes", codes.getAllCountryCodes());

		return "weather_view";

	}

	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}

	// Sets the search page and loads the ISO codes table.
	@RequestMapping("/")
	public String getWeatherView(Model model, CountryCodes codes) {

		model.addAttribute("codes", codes.getAllCountryCodes());

		return "weather_view";

	}

	// Allows you to search for weather in city + country (ISO) or just city alone.
	@GetMapping("/current/weather")
	public ResponseEntity<?> getCurrentWeatherDataForCityAndCountry(
			@RequestParam("city") String city,
			@RequestParam("country") String country,
			Model model) throws IOException {

		Weather weather;
		weather = this.wService.getWeatherDataCity(city, country);

		if (weather != null) {
			return ResponseEntity.ok(weather);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Weather data not found");
		}
	}

	@GetMapping("/five_day/weather")
	public ResponseEntity<?> getFiveDayForecast(
			@RequestParam("city") String city,
			@RequestParam("country") String country,
			Model model) throws IOException {

		city = city.substring(0, 1).toUpperCase() + city.substring(1);

		Map<String, List<FiveDayHourlyWeather>> fiveDay = this.wService.getHourlyWeather(city, country);

		if (!fiveDay.isEmpty()) {
			getDays(fiveDay);
			getDataForEachDay(fiveDay);

			Map<String, Object> response = new HashMap<>();
			response.put("days", this.days);
			response.put("weatherData", this.weatherData);
			return ResponseEntity.ok(response);
		} else {
			CountryCodes codes = new CountryCodes();
			model.addAttribute("error", true);
			model.addAttribute("codes", codes.getAllCountryCodes());

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Weather data not found");
		}

	}

	public void getDays(Map<String, List<FiveDayHourlyWeather>> fiveDay) {

		this.days = new ArrayList<>();

		for (String day : fiveDay.keySet()) {

			this.days.add(day);

		}

	}

	public void getDataForEachDay(Map<String, List<FiveDayHourlyWeather>> fiveDay) {

		this.weatherData = new ArrayList<>();

		for (String list : fiveDay.keySet()) {

			this.weatherData.add(fiveDay.get(list));

		}

	}

}
