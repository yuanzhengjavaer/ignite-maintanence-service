package com.nomura.service;

import com.nomura.components.remote.VehicleService;
import com.nomura.model.dto.TxTestReq;
import com.nomura.model.dto.VehicleFullInfo;
import com.nomura.model.dto.VehicleInfoReq;
import com.nomura.model.po.Maintenance;
import com.nomura.model.po.Vehicle;
import jdk.nashorn.internal.runtime.JSONListAdapter;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@DependsOn("ignite")
public class BussinessService {
    @Autowired
    private Ignite ignite;
    @Autowired
    private MaintenanceService maintenanceService;
    @Lazy
    @Autowired
    private VehicleService vehicleService;

//    @PostConstruct
//    public void setMaintenanceService() {
//        //get local service instance
//       maintenanceService = ignite.services().service(MaintenanceService.SERVICE_NAME);
//    }

    @Transactional(rollbackFor = Exception.class)
    public void saveMaintenanceInfo(TxTestReq req) {
        boolean needRollBack = req.isNeedRollBack();
        List<Maintenance> maintenances = (List<Maintenance>) req.getMaintenances();
        for (Maintenance newRecord : maintenances) {
            maintenanceService.putMaintenanceNew(newRecord);
        }

        if (needRollBack) {
            int a = 1 / 0;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveVehicleInfoList(List<VehicleInfoReq> reqList) {
        for (VehicleInfoReq req : reqList) {
            Vehicle vehicle = new Vehicle();
            BeanUtils.copyProperties(req, vehicle);
            Integer vehiclePk = vehicleService.addVehicle(vehicle);
            List<Maintenance> maintenances = req.getMaintenances();
            for (Maintenance maintenance : maintenances) {
                maintenanceService.putMaintenanceNew(maintenance);
            }
        }
    }


    public VehicleFullInfo getVehicleFullInfo(Integer vehicleId) {
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);
        List<Maintenance> maintenanceRecords = maintenanceService.getMaintenanceRecords(vehicleId);
        return new VehicleFullInfo(vehicle, maintenanceRecords);
    }

}
