package com.sap1ens.heimdall.service;

import com.sap1ens.heimdall.model.FlinkJob;
import java.util.List;

public interface FlinkJobLocator {
  List<FlinkJob> findAll();
}
