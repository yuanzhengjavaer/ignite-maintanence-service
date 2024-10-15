package com.nomura.dao;



import com.nomura.model.po.Maintenance;

import java.util.List;

public interface IgniteMaintenanceDao{

    void insert(Maintenance maintenance);

    Maintenance getByPK(Integer id);
}
