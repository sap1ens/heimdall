package com.sap1ens.heimdall;

import io.smallrye.config.ConfigMapping;


@ConfigMapping(prefix = "heimdall")
public interface AppConfig {
  Joblocator joblocator();

  EndpointPathPatterns endpointPathPatterns();

  interface Joblocator {
    K8sOperator k8sOperator();

    interface K8sOperator {
      boolean enabled();
      String namespaceToWatch();
    }
  }
  interface EndpointPathPatterns {
    String flinkUi();
    String flinkApi();
    String metrics();
    String logs();
  }
}
