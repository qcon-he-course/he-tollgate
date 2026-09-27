# 3. Maven wrapper and SDKMAN

Date: 2022-03-08

Status: Accepted

## Context

Developers and the build server used different Maven and JDK versions, and builds that passed locally failed in
Jenkins.

## Decision

The repository carries the Maven wrapper (`./mvnw`), pinned to Maven 3.8.6, and a `.sdkmanrc` naming the JDK.
Everyone builds with `./mvnw`.

## Consequences

One Maven version everywhere. Developers who do not use SDKMAN must install the JDK in `.sdkmanrc` themselves.
