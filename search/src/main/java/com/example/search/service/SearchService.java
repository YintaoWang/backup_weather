package com.example.search.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface SearchService {
    List<Map> searchWeatherByNames(String[] cities);
}
