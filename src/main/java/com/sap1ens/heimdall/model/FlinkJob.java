package com.sap1ens.heimdall.model;

import java.util.Map;

public record FlinkJob(
    String id,
    String name,
    String status,
    FlinkJobType type,
    Long startTime,
    String shortImage,
    String flinkVersion,
    Integer parallelism,
    Map<String, FlinkJobResources> resources,
    Map<String, String> metadata) {}
