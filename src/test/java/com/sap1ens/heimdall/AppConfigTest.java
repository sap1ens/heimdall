package com.sap1ens.heimdall;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AppConfigTest {

  @Inject AppConfig appConfig;

  @Test
  public void testK8sOperatorConfigExists() {
    assertNotNull(appConfig.joblocator());
    assertNotNull(appConfig.joblocator().k8sOperator());
  }

  @Test
  public void testNamespacesToWatchParsingSingleNamespace() {
    // This test verifies the default behavior from application.properties
    var namespaces = appConfig.joblocator().k8sOperator().namespacesToWatch();
    assertNotNull(namespaces);
    assertFalse(namespaces.isEmpty());
  }

  @Test
  public void testNamespacesToWatchParsingWithCommas() {
    // Testing the parsing logic with comma-separated namespaces
    // Create a mock implementation to test the parsing logic
    var mockConfig =
        new AppConfig() {
          @Override
          public Joblocator joblocator() {
            return new Joblocator() {
              @Override
              public K8sOperator k8sOperator() {
                return new K8sOperator() {
                  @Override
                  public boolean enabled() {
                    return true;
                  }

                  @Override
                  public String namespaceToWatch() {
                    return "ns1,ns2,ns3";
                  }
                };
              }
            };
          }

          @Override
          public java.util.Map<String, String> patterns() {
            return java.util.Map.of();
          }

          @Override
          public java.util.Map<String, String> endpointPathPatterns() {
            return java.util.Map.of();
          }
        };

    var namespaces = mockConfig.joblocator().k8sOperator().namespacesToWatch();
    assertEquals(3, namespaces.size());
    assertTrue(namespaces.contains("ns1"));
    assertTrue(namespaces.contains("ns2"));
    assertTrue(namespaces.contains("ns3"));
  }

  @Test
  public void testNamespacesToWatchParsingWithSpaces() {
    var mockConfig =
        new AppConfig() {
          @Override
          public Joblocator joblocator() {
            return new Joblocator() {
              @Override
              public K8sOperator k8sOperator() {
                return new K8sOperator() {
                  @Override
                  public boolean enabled() {
                    return true;
                  }

                  @Override
                  public String namespaceToWatch() {
                    return " ns1 , ns2 , ns3 ";
                  }
                };
              }
            };
          }

          @Override
          public java.util.Map<String, String> patterns() {
            return java.util.Map.of();
          }

          @Override
          public java.util.Map<String, String> endpointPathPatterns() {
            return java.util.Map.of();
          }
        };

    var namespaces = mockConfig.joblocator().k8sOperator().namespacesToWatch();
    assertEquals(3, namespaces.size());
    assertEquals(List.of("ns1", "ns2", "ns3"), namespaces);
  }

  @Test
  public void testNamespacesToWatchEmptyString() {
    var mockConfig =
        new AppConfig() {
          @Override
          public Joblocator joblocator() {
            return new Joblocator() {
              @Override
              public K8sOperator k8sOperator() {
                return new K8sOperator() {
                  @Override
                  public boolean enabled() {
                    return true;
                  }

                  @Override
                  public String namespaceToWatch() {
                    return "";
                  }
                };
              }
            };
          }

          @Override
          public java.util.Map<String, String> patterns() {
            return java.util.Map.of();
          }

          @Override
          public java.util.Map<String, String> endpointPathPatterns() {
            return java.util.Map.of();
          }
        };

    var namespaces = mockConfig.joblocator().k8sOperator().namespacesToWatch();
    assertEquals(List.of("default"), namespaces);
  }

  @Test
  public void testNamespacesToWatchNull() {
    var mockConfig =
        new AppConfig() {
          @Override
          public Joblocator joblocator() {
            return new Joblocator() {
              @Override
              public K8sOperator k8sOperator() {
                return new K8sOperator() {
                  @Override
                  public boolean enabled() {
                    return true;
                  }

                  @Override
                  public String namespaceToWatch() {
                    return null;
                  }
                };
              }
            };
          }

          @Override
          public java.util.Map<String, String> patterns() {
            return java.util.Map.of();
          }

          @Override
          public java.util.Map<String, String> endpointPathPatterns() {
            return java.util.Map.of();
          }
        };

    var namespaces = mockConfig.joblocator().k8sOperator().namespacesToWatch();
    assertEquals(List.of("default"), namespaces);
  }

  @Test
  public void testNamespacesToWatchFiltersEmptyEntries() {
    var mockConfig =
        new AppConfig() {
          @Override
          public Joblocator joblocator() {
            return new Joblocator() {
              @Override
              public K8sOperator k8sOperator() {
                return new K8sOperator() {
                  @Override
                  public boolean enabled() {
                    return true;
                  }

                  @Override
                  public String namespaceToWatch() {
                    return "ns1,,ns2, ,ns3";
                  }
                };
              }
            };
          }

          @Override
          public java.util.Map<String, String> patterns() {
            return java.util.Map.of();
          }

          @Override
          public java.util.Map<String, String> endpointPathPatterns() {
            return java.util.Map.of();
          }
        };

    var namespaces = mockConfig.joblocator().k8sOperator().namespacesToWatch();
    assertEquals(3, namespaces.size());
    assertEquals(List.of("ns1", "ns2", "ns3"), namespaces);
  }

  @Test
  public void testPatternsConfigExists() {
    assertNotNull(appConfig.patterns());
  }

  @Test
  public void testEndpointPathPatternsConfigExists() {
    assertNotNull(appConfig.endpointPathPatterns());
  }
}
