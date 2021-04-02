FROM maven:3-openjdk-11 as build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /build/src/
RUN mvn package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:11-slim
VOLUME /tmp
ARG DEPENDENCY=/build/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-noverify","-Dspring.jmx.enabled=false","-XX:TieredStopAtLevel=1","-Dspring.config.location=classpath:/application.yml","-cp","app:app/lib/*","dev.gjoris.cognito.demo.DemoApplication"]