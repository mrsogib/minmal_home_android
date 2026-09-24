@echo off
set DIR=%~dp0
set CLASSPATH=%DIR%gradle\wrapper\gradle-wrapper.jar

if not exist "%CLASSPATH%" (
  echo gradle-wrapper.jar not found. Open this project in Android Studio and
  echo let it regenerate the wrapper, or run: gradle wrapper --gradle-version 8.7
  exit /b 1
)

java -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
