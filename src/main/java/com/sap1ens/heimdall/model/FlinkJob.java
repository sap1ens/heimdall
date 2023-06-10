package com.sap1ens.heimdall.model;

public record FlinkJob(
    String id, String name, String status, FlinkJobType type, Long startTime, String shortImage, Integer parallelism) {}
