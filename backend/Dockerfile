# Dockerfile (backend) - Spring Boot 서비스 하나를 이미지로 굽는다
#
# 서비스 3개(api-gateway, user-service, post-service)가 이 파일 하나를 함께 쓴다.
# 어느 서비스를 구울지는 빌드할 때 SERVICE 값으로 정한다.
#
#   docker build --build-arg SERVICE=user-service -t rlaeocjf/msa-user-service backend
#
# 두 단계(multi-stage)로 만든다.
#   1단계 build : JDK(컴파일러 포함)로 Gradle 빌드 → jar 파일 하나
#   2단계 run   : JRE(실행만 하는 가벼운 Java)에 jar 만 복사
#   → 최종 이미지에는 소스 코드도 Gradle 도 없다. 크기가 작아진다.

# ---------- 1단계: 빌드 ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# (1) Gradle 설정 파일만 먼저 복사하고 라이브러리를 받아 둔다.
#     소스만 고치고 build.gradle 은 그대로면, Docker 가 이 단계의 결과(캐시)를 다시 써서
#     라이브러리를 또 받지 않는다. → 두 번째 빌드부터 훨씬 빠르다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY api-gateway/build.gradle api-gateway/
COPY user-service/build.gradle user-service/
COPY post-service/build.gradle post-service/
RUN chmod +x gradlew \
 && ./gradlew --no-daemon -q :api-gateway:dependencies :user-service:dependencies :post-service:dependencies > /dev/null

# (2) 소스를 복사하고, 고른 서비스 하나만 jar 로 만든다.
COPY . .
ARG SERVICE
RUN ./gradlew --no-daemon -q :${SERVICE}:bootJar -x test

# ---------- 2단계: 실행 ----------
FROM eclipse-temurin:21-jre
ARG SERVICE
WORKDIR /app
COPY --from=build /app/${SERVICE}/build/libs/${SERVICE}-0.0.1-SNAPSHOT.jar app.jar

# 메모리를 적게 쓰도록 Java 를 설정한다 (무료 EC2 는 메모리가 1GB 뿐이라 서비스 3개가 나눠 써야 한다)
#   -Xmx128m                     : 객체를 담는 힙 메모리를 최대 128MB 로
#   -XX:+UseSerialGC             : 메모리를 적게 쓰는 가장 단순한 쓰레기 수거(GC) 방식
#   -Xss512k                     : 스레드 하나가 쓰는 스택 크기를 줄인다
#   -XX:TieredStopAtLevel=1      : 가장 가벼운 JIT 컴파일만 쓴다 (최고 속도 대신 메모리를 아낀다)
#   -XX:ReservedCodeCacheSize=32m, -XX:MaxMetaspaceSize=128m : 컴파일된 코드·클래스 정보 공간의 한도
#   (교안을 만들 때 내 PC 에서 잰 값: 서비스 하나 약 160~230MB. 이 설정이 없으면 약 220~300MB)
ENV JAVA_TOOL_OPTIONS="-Xmx128m -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=32m -XX:MaxMetaspaceSize=128m"

ENTRYPOINT ["java", "-jar", "app.jar"]
