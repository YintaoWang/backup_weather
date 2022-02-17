package com.example.search.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public interface SearchService {
    List searchWeatherByNames(String[] cities);
//    List<Object> searchWeatherByNames(String[] cities);
}
