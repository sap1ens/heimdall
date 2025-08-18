package com.sap1ens.heimdall;

import io.smallrye.config.ConfigMapping;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ConfigMapping(prefix = "heimdall")
public interface AppConfig {
  Joblocator joblocator();

  Map<String, String> patterns();

  Map<String, String> endpointPathPatterns();

  interface Joblocator {
    K8sOperator k8sOperator();

    interface K8sOperator {
      boolean enabled();

      String namespaceToWatch();

      default List<String> namespacesToWatch() {
        String namespace = namespaceToWatch();
        if (namespace == null || namespace.trim().isEmpty()) {
          return List.of("default");
        }
        return Arrays.asList(namespace.split(",")).stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
      }
    }
  }
}
