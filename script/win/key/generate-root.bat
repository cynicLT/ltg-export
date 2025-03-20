@echo off
rem generate key
openssl ecparam -out root.key -name prime256v1 -genkey

rem generate certificate request
openssl req -new -sha256 -key root.key -out root.csr -subj "/C=LT/L=Kaunas/O=JSC Beavers/"

rem generate certificate
openssl x509 -req -sha256 -days 3560 -in root.csr -signkey root.key -out root.crt

rem delete certificate request
del root.csr