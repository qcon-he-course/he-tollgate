# Spring Boot 3 upgrade

Owner: Platform Modernisation. Started February 2023.

Spring Boot 2.7 is the last 2.x line and its open-source support ends in November 2023. Boot 3 needs Java 17 and
moves from `javax.*` to `jakarta.*`, so the upgrade touches most of the codebase. We are doing it in small steps
that can each be merged on their own, keeping the 2.7 build green throughout. Work that has to switch over in one
go sits behind the `boot3-prep` profile until the switch.

The target version is the `boot3.version` property in the parent POM.

## Checklist

1. [x] Build and run on Java 17 (`.sdkmanrc`, `maven.compiler.release`).
2. [x] Maven wrapper, so everyone builds with the same Maven version.
3. [x] Spring Boot 2.7.x, the last 2.x line.
4. [x] `SecurityFilterChain` bean instead of `WebSecurityConfigurerAdapter` (`SecurityFilterChainConfig`, `boot3-prep` profile).
5. [x] `@EnableMethodSecurity` instead of `@EnableGlobalMethodSecurity` (in `SecurityFilterChainConfig`).
6. [x] Path pattern parser instead of the ant path matcher (`boot3-prep` profile).
7. [x] Tests on embedded PostgreSQL instead of H2 (ADR-0011).
8. [ ] Remove `LegacySecurityConfig` and the `boot3-prep` profile; `SecurityFilterChainConfig` becomes the only security configuration.
9. [ ] Replace springfox with springdoc-openapi 2 and remove `SpringfoxWorkaroundConfig`.
10. [ ] `javax.*` to `jakarta.*`: persistence, validation, servlet, mail, annotations.
11. [ ] Hibernate 6: check native queries, `@Fetch` settings and identifier generation on every entity.
12. [ ] `thymeleaf-extras-springsecurity5` to `thymeleaf-extras-springsecurity6`.
13. [ ] Remove `LegacyPermitPdfWriter` and the iText 2.1.7 dependency once no pre-2023 permits need reprinting.
14. [ ] Lift the Lombok version pin in the parent POM.
15. [ ] `antMatchers` to `requestMatchers` in the security configuration.
16. [ ] Replace commons-lang 2.6 with commons-lang3.
17. [ ] Let the Boot BOM manage the PostgreSQL driver instead of the hard-coded 42.2.18.
18. [ ] Review actuator exposure and security against the Boot 3 defaults.
19. [ ] Set the Spring Boot parent to `${boot3.version}` and run the full regression suite, then a week in UAT.

## Trying the new security configuration

```sh
./mvnw -pl tollgate-app spring-boot:run -Dspring-boot.run.profiles=local,boot3-prep
```

Staff login, roles, method security and API keys should behave exactly as without the profile. The Swagger UI shows
no operations under this profile: springfox cannot read path-pattern mappings (item 9).
