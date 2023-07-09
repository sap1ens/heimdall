# Heimdall

Heimdall is a dashboard for operating Flink jobs and deployments. Built-in Flink UI is extremely useful when dealing with
a single job, but what if you have 10, 20 or 100 jobs? Heimdall helps to keep track of all your Flink jobs by searching, filtering, sorting and navigating them.

Read more in my blog: [Heimdall: Making Operating Flink Deployments a Bit Easier](https://sap1ens.com/blog/2023/07/09/heimdall-making-operating-flink-deployments-a-bit-easier/).

Note: currently, Heimdall only supports Flink jobs deployed with [Flink Kubernetes Operator](https://ci.apache.org/projects/flink/flink-kubernetes-operator-docs-stable/).

![](docs/assets/demo.gif)

## Quick (and Dirty) Start

```bash
kubectl apply -f https://raw.githubusercontent.com/sap1ens/heimdall/main/tools/k8s-operator/service-account.yaml
kubectl run heimdall --image=ghcr.io/sap1ens/heimdall:0.4.0 --port=8080 --overrides='{ "spec": { "serviceAccount": "heimdall-service-account" }  }'
kubectl port-forward heimdall 8080:8080
open http://localhost:8080
```

## Installation

Heimdall is available as a Docker container [here](https://github.com/sap1ens/heimdall/pkgs/container/heimdall). Since only 
Flink Kubernetes Operator as available at the moment you'd typically deploy Heimdall as a pod or deployment in the same cluster.

Heimdall uses port 8080 and exposes liveness and readiness endpoints at `/q/health/live` and `/q/health/ready`.

## Job Locators

Job locator (implements `FlinkJobLocator` interface) is a mechanism for discovering running Flink jobs in Heimdall.

### K8sOperatorFlinkJobLocator

Can be enabled with `heimdall.joblocator.k8s-operator.enabled` config. Currently, enabled by default. It loads 
`flinkdeployment` custom resource (CR) created by the Flink Kubernetes Operator.

A service account with read-only access to `flinkdeployment` CR is required. See [tools/k8s-operator/service-account.yaml](tools/k8s-operator/service-account.yaml) for an example.

## Configuration

### Common

| Environment variable                      | Default                           | Description                                                                                                                    |
|-------------------------------------------|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| QUARKUS_HTTP_CORS_ORIGINS                 | http://localhost:5173             | Comma separated list of valid origins allowed for CORS. Change to `http://localhost:8001` when using `kubectl proxy`.          |
| HEIMDALL_PATTERNS_DISPLAY_NAME            | $jobName                          | Pattern for showing Flink job name. Metadata fields (e.g. Kubernetes labels) can be accessed via `$metadata.labelName` syntax. |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_FLINK_UI  | http://localhost/$jobName/ui      | Pattern for the Flink UI endpoint. `$jobName` will be substituted. Set to an empty string to disable.                          |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_FLINK_API | http://localhost/$jobName/api     | Pattern for the Flink API endpoint. `$jobName` will be substituted. Set to an empty string to disable.                         |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_METRICS   | http://localhost/$jobName/metrics | Pattern for the Metrics endpoint. `$jobName` will be substituted. Set to an empty string to disable.                           |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_LOGS      | http://localhost/$jobName/logs    | Pattern for the Logs endpoint. `$jobName` will be substituted. Set to an empty string to disable.                              |

### K8sOperatorFlinkJobLocator

| Environment variable                                | Default | Description                  |
|-----------------------------------------------------|---------|------------------------------|
| HEIMDALL_JOBLOCATOR_K8S_OPERATOR_ENABLED            | true    | Is this locator enabled?     |
| HEIMDALL_JOBLOCATOR_K8S_OPERATOR_NAMESPACE_TO_WATCH | default | Kubernetes namespace to use. |

## Development

This project uses:

- Java 17 and [Quarkus](https://quarkus.io) on the back-end.
- JavaScript and [Svelte](https://svelte.dev) on the front-end.

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```bash
./gradlew quarkusDev
```

### Packaging and running the application

The application can be packaged using:

```bash
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```bash
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Sponsors

This project is sponsored by [Goldsky](https://goldsky.com) ❤️.
