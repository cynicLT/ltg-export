@echo off
rem generate key
openssl ecparam -out usage.key -name prime256v1 -genkey

rem generate certificate request
openssl req -new -sha256 -key usage.key -out usage.csr -subj "/C=LT/L=Vilnius/O=UAB Maxima/"

rem generate certificate
openssl x509 -req -sha256 -days 3560 -in usage.csr -CA root.crt -CAkey root.key -out usage.crt

rem delete certificate request
del usage.csr