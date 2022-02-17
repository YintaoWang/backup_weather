package com.example.search.service;

import com.example.search.config.DetailsEndpointConfig;
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
        CompletableFuture<Object>[] futures = new CompletableFuture[size];
        for (int i = 0; i < size; i++) {
            String url = DetailsEndpointConfig.findCityIdByName + cities[i];
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> restTemplate.getForEntity(url, Object.class), pool);
            futures[i] = future;
        }

        //get all the city ids (may contain duplicates (ex: search by "...?cities=chi,chicago"), need to remove duplicates or not?)
        CompletableFuture<ArrayList> finalFuture = CompletableFuture.allOf(futures)
                .thenApply(Void -> {
                    List<Object> woeids = new ArrayList<>();
                    for (CompletableFuture<Object> f : futures) {
                        woeids.add(f.join());
                    }
                    return woeids;
                }).thenApply(woeids -> {
                    ArrayList ids = new ArrayList();
                    for (Object o : woeids) {
                        ResponseEntity responseEntity = (ResponseEntity) o;
                        LinkedHashMap linkedHashMap = (LinkedHashMap) responseEntity.getBody();
                        //may contain duplicates: not sure if we need to remove duplicates.
                        ids.addAll((ArrayList) linkedHashMap.get("data"));
                    }
                    return ids;
                });
        ArrayList<Integer> ids = finalFuture.join();

        //search weather by each id
        CompletableFuture<Object>[] futures2 = new CompletableFuture[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            String url = DetailsEndpointConfig.findWeatherById + ids.get(i);
            System.out.println(url);
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> restTemplate.getForEntity(url, Object.class), pool);
            futures2[i] = future;
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
    }
}
