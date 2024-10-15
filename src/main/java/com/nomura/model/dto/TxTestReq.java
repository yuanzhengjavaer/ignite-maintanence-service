package com.nomura.model.dto;

import com.nomura.model.po.Maintenance;
import lombok.Data;

import java.util.List;

@Data
public class TxTestReq {
    private List<Maintenance> maintenances;
    private boolean needRollBack;
}
