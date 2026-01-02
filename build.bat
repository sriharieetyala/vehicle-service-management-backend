@echo off
echo Building all VSMS microservices...
echo This may take 3-5 minutes...
echo.

cd /d %~dp0

echo [1/8] Building service-registry...
cd service-registry && call mvn package -DskipTests && cd ..

echo [2/8] Building config-server...
cd config-server && call mvn package -DskipTests && cd ..

echo [3/8] Building api-gateway...
cd api-gateway && call mvn  package -DskipTests && cd ..

echo [4/8] Building auth-service...
cd auth-service && call mvn package -DskipTests && cd ..

echo [5/8] Building vehicle-service...
cd vehicle-service && call mvn package -DskipTests && cd ..

echo [6/8] Building service-request-service...
cd service-request-service && call mvn package -DskipTests && cd ..

echo [7/8] Building inventory-service...
cd inventory-service && call mvn package -DskipTests && cd ..

echo [8/8] Building notification-service...
cd notification-service && call mvn package -DskipTests && cd ..

echo.
echo Done! All JARs built.
echo Now run: docker-compose up --build
pause
