package com.nomura.model.dto;

import com.nomura.model.po.Maintenance;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VehicleInfoReq {

    private Integer id;
    private String name;
    private Date year;
    private Double price;
    private List<Maintenance> maintenances;
}
