package com.nomura.model.dto;

import com.nomura.model.po.Maintenance;
import com.nomura.model.po.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleFullInfo {
    private Vehicle vehicle;
    private List<Maintenance> maintenance;
}
