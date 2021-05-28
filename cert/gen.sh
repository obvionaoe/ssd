#!/bin/bash

rm */*.pem

# 1. Generate a private key and self-signed certificate of the Certificate Authority (CA).
openssl req -x509 -newkey rsa:4096 -days 365 -nodes -keyout ca/key.pem -out ca/cert.pem -subj "/C=PT/ST=Porto/L=Porto/O=grupoE/OU=ca/CN=localhost/emailAddress=grupoE@ssd.dcc.fc.up.pt"

# SERVER

# 2. Generate a private key and certificate signing request (CSR) for our server.
openssl req -newkey rsa:4096 -nodes -keyout server/key.pem -out server/req.pem -subj "/C=PT/ST=Porto/L=Porto/O=grupoE/OU=server/CN=localhost/emailAddress=grupoE@ssd.dcc.fc.up.pt"

# 3. Use the CA's private key to sign our web server's CSR and get back the signed certificate.
openssl x509 -req -in server/req.pem -CA ca/cert.pem -CAkey ca/key.pem -CAcreateserial -out server/cert.pem -extfile server/ext.cnf

# CLIENT

# 2. Generate a private key and certificate signing request (CSR) for our client.
openssl req -newkey rsa:4096 -nodes -keyout client/key.pem -out client/req.pem -subj "/C=PT/ST=Porto/L=Porto/O=grupoE/OU=client/CN=localhost/emailAddress=grupoE@ssd.dcc.fc.up.pt"

# 3. Use the CA's private key to sign our client's CSR and get back the signed certificate.
openssl x509 -req -in client/req.pem -CA ca/cert.pem -CAkey ca/key.pem -CAcreateserial -out client/cert.pem -extfile client/ext.cnf
