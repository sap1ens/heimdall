package com.sap1ens.heimdall.model;

public record FlinkJob(
    String name, String status, Long startTime, String shortImage, Integer parallelism) {}
