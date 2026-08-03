package com.sap1ens.heimdall;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithName;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.spi.Converter;

@ConfigMapping(prefix = "heimdall")
public interface AppConfig {
  Joblocator joblocator();

  Map<String, String> patterns();

  // An empty string is the documented way to disable an endpoint link, but
  // the default String converter turns it into null and fails startup
  // validation — pass values through raw and filter the empties out.
  @WithName("endpoint-path-patterns")
  Map<String, @WithConverter(RawStringConverter.class) String> rawEndpointPathPatterns();

  default Map<String, String> endpointPathPatterns() {
    return rawEndpointPathPatterns().entrySet().stream()
        .filter(entry -> !entry.getValue().isEmpty())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  class RawStringConverter implements Converter<String> {
    @Override
    public String convert(String value) {
      return value;
    }
  }

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
