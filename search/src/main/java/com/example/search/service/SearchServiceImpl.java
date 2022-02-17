package com.example.search.service;

import com.example.search.config.DetailsEndpointConfig;
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
    public List<Map> searchWeatherByNames(String[] cities) {
        int size = cities.length;
        CompletableFuture<Object>[] futures = new CompletableFuture[size];
        for (int i = 0; i < cities.length; i++) {
//            CompletableFuture f = new CompletableFuture();
//            City[] cities = restTemplate.getForObject(EndpointConfig.queryWeatherByCity + city, City[].class);
//            final int index = i;
            String url = DetailsEndpointConfig.findCityIdByName + cities[i];
            System.out.println(url);
//            ResponseEntity<Object[]> responseEntity =
//                    restTemplate.getForEntity(BASE_URL, Object[].class);
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> restTemplate.getForEntity(url, Object.class), pool);
            futures[i] = future;
        }

        CompletableFuture finalFuture = CompletableFuture.allOf(futures)
                .thenApply(Void -> {
                    List<Object> woeids = new ArrayList<>();
                    for (CompletableFuture<Object> f : futures) {
//                        System.out.println();
                        woeids.add(f.join());
                    }
                    System.out.println(woeids);
                    return woeids;
                });
//                .thenApply(lists -> {
//                    for (List<Integer> list : lists) {
//                        for (Integer id : list) {
//                            CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> restTemplate.getForObject("test1" + id, HashMap.class), pool);
//                        }
//                    }
//                });


        List li = new ArrayList();
        Map<String, List> map = new HashMap<>();
        map.put("", (List) finalFuture.join());
        li.add(map);
        return li;
//        return finalFuture.join();
    }
}
