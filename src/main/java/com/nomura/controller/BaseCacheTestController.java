package com.nomura.controller;

import com.nomura.model.dto.VehicleFullInfo;
import com.nomura.service.BussinessService;
import com.nomura.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/baseCache")
public class BaseCacheTestController {

    @Autowired
    private BussinessService bussinessService;

    @GetMapping("/{vehicleId}")
    public VehicleFullInfo getVehicleFullInfo(@PathVariable("vehicleId") Integer vehicleId) {
        return bussinessService.getVehicleFullInfo(vehicleId);
    }
}
