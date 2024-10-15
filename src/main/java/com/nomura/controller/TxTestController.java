package com.nomura.controller;

import com.nomura.model.dto.TxTestReq;
import com.nomura.model.dto.VehicleInfoReq;

import com.nomura.service.BussinessService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    public void localTxTest(@RequestBody TxTestReq req){
        bussinessService.saveMaintenanceInfo(req);
    }
}
