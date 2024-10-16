package com.nomura.controller;

import com.nomura.service.CacheEventService;
import com.nomura.service.IgniteComputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/compute")
public class ComputeTestController {
    @Autowired
    private IgniteComputeService igniteComputeService;
    @Autowired
    private CacheEventService cacheEventService;

    @GetMapping("/roundRobin")
    public void roundRobinCompute() {
        igniteComputeService.roundRobinCompute();
    }

    @GetMapping("/broadCast")
    public void broadCastCompute() {
        igniteComputeService.broadCastCompute();
    }

    @PostMapping("/event")
    public void computeWhenEvent(@RequestBody Map<String, Object> req) {
        cacheEventService.put((Integer) req.getOrDefault("key", 1), (String) req.getOrDefault("val", "123"));
    }
}
