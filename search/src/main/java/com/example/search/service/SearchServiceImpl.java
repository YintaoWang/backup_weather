package com.example.search.service;

import com.example.search.config.DetailsEndpointConfig;
import com.example.search.pojo.CityIds;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SearchServiceImpl implements SearchService {
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private final RestTemplate restTemplate;

    public SearchServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List searchWeatherByNames(String[] cities) {
        int size = cities.length;
        CompletableFuture<List>[] futures1 = new CompletableFuture[size];
        for (int i = 0; i < size; i++) {
            String url1 = DetailsEndpointConfig.findCityIdByName + cities[i];
//            System.out.println(cities[i]);
            String city = cities[i];
            CompletableFuture<List> future1 = CompletableFuture.supplyAsync(() -> {
                System.out.println(city + "\t" + Thread.currentThread().getName());
                //get ids by each city
                CityIds cityIds = restTemplate.getForObject(url1, CityIds.class);
                //get weather by each id
                CompletableFuture<Object>[] futures2 = new CompletableFuture[cityIds.getData().size()];
                for (int j = 0; j < cityIds.getData().size(); j++) {
                    String url2 = DetailsEndpointConfig.findWeatherById + cityIds.getData().get(j);
                    Integer id = cityIds.getData().get(j);
                    CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
                        System.out.println(id + "\t" + Thread.currentThread().getName());
                        Object weather = restTemplate.getForEntity(url2, Object.class);
                        return weather;
                    }, pool);
                    futures2[j] = future2;
                }
                CompletableFuture<List> finalFuture2 = CompletableFuture.allOf(futures2)
                        .thenApply(Void -> {
                            List<Object> weathers = new ArrayList<>();
                            for (CompletableFuture<Object> f : futures2) {
                                weathers.add(f.join());
                            }
                            return weathers;
                        });
                return finalFuture2.join();
            }, pool);
            futures1[i] = future1;
        }
        CompletableFuture<List> finalFuture1 = CompletableFuture.allOf(futures1)
                .thenApply(Void -> {
                    List<Object> weathers = new ArrayList<>();
                    for (CompletableFuture<List> f : futures1) {
                        weathers.addAll(f.join());
                    }
                    return weathers;
                });
        return finalFuture1.join();
    }
}
