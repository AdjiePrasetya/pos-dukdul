@echo off
echo ============================================
echo   Kasir Pintar - Duduk Dulu Coffee Shop
echo ============================================
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java tidak ditemukan. Install JDK 17+ dari https://adoptium.net
    pause
    exit /b 1
)

REM Check Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven tidak ditemukan. Install Maven dari https://maven.apache.org
    pause
    exit /b 1
)

echo [1/2] Building project...
mvn clean package -q

if %errorlevel% neq 0 (
    echo [ERROR] Build gagal! Cek error di atas.
    pause
    exit /b 1
)

echo [2/2] Menjalankan aplikasi...
java -jar target\dudukdulu-pos.jar

pause
