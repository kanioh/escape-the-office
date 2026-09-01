# ---- ビルドステージ ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 依存の解決とソースのコピーを別レイヤーに分ける。
# ソースだけ変更したときに依存の再ダウンロードを走らせないため
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
# テストはDB接続を必要とするため、イメージビルドでは実行しない
RUN ./mvnw -B package -DskipTests

# ---- 実行ステージ ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --create-home --shell /usr/sbin/nologin appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
