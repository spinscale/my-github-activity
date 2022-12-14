FROM eclipse-temurin:19.0.1_10-jdk AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN ./gradlew --no-daemon clean assemble installDist


FROM eclipse-temurin:19.0.1_10-jdk
RUN addgroup --system firebolt && adduser --system firebolt --ingroup firebolt
USER firebolt:firebolt

WORKDIR /my-github-activity
COPY --from=build /home/gradle/src/build/install/my-github-activity/ /my-github-activity

EXPOSE 7000

ENTRYPOINT [ "/my-github-activity/bin/my-github-activity" ]
