# Heimdall

![](src/main/webui/src/assets/heimdall-logo.png)

Heimdall is a dashboard for managing Flink jobs and deployment. Built-in Flink UI is extremely useful when dealing with
a single job, but what if you have 10 or 100? Heimdall helps to keep track of all your Flink jobs, search, filter and navigate them.

Currently, Heimdall only supports Flink jobs deployed with [Flink Kubernetes Operator](https://ci.apache.org/projects/flink/flink-kubernetes-operator-docs-stable/).

## Development

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

### Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.
