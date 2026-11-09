@echo off
rem Потрібен JDK 21 і Gradle 9.x у PATH (або покладіть gradlew з шаблону Fabric)
gradle build
echo Готовий мод: build\libs\visualmod-1.0.0.jar
pause
