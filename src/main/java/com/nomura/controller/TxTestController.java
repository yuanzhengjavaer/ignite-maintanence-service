package com.nomura.controller;

import com.nomura.model.dto.VehicleInfoReq;
import com.nomura.model.po.Maintenance;
import com.nomura.service.BussinessService;
import com.nomura.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tx")
public class TxTestController {

    @Autowired
    private BussinessService bussinessService;

    @PostMapping("/distrbuted")
    public void distributedTxTest(@RequestBody List<VehicleInfoReq> reqList){
        bussinessService.saveVehicleInfoList(reqList);
    }

    @PostMapping("/local")
    public void localTxTest(@RequestBody List<Maintenance> maintenances){
        bussinessService.saveMaintenanceInfo(maintenances);
    }
}
