# Demonstrate use of dynamic client ssl context with configuration of 2 reverse proxies

Build wildfly server with following feature branches:

## Generate certificates for 2 different mutual ssl contexts in $JBOSS_HOME/standalone/configuration

```
keytool -genkeypair -alias localhost -keyalg RSA -keysize 1024 -validity 365 -keystore server1.keystore.jks -dname "CN=localhost" -keypass secret -storepass secret

keytool -genkeypair -alias client1 -keyalg RSA -keysize 1024 -validity 365 -keystore client1.keystore.jks -dname "CN=client1" -keypass secret -storepass secret

keytool -exportcert  -keystore server1.keystore.jks -alias localhost -keypass secret -storepass secret -file server1.cer

keytool -exportcert  -keystore client1.keystore.jks -alias client1 -keypass secret -storepass secret -file client1.cer

keytool -importcert -keystore server1.truststore.jks -storepass secret -alias client1 -trustcacerts -file client1.cer

keytool -importcert -keystore client1.truststore.jks -storepass secret -alias localhost -trustcacerts -file server1.cer

keytool -genkeypair -alias localhost -keyalg RSA -keysize 1024 -validity 365 -keystore server2.keystore.jks -dname "CN=localhost" -keypass secret -storepass secret

keytool -genkeypair -alias client2 -keyalg RSA -keysize 1024 -validity 365 -keystore client2.keystore.jks -dname "CN=client2" -keypass secret -storepass secret

keytool -exportcert  -keystore server2.keystore.jks -alias localhost -keypass secret -storepass secret -file server2.cer

keytool -exportcert  -keystore client2.keystore.jks -alias client2 -keypass secret -storepass secret -file client2.cer

keytool -importcert -keystore server2.truststore.jks -storepass secret -alias client2 -trustcacerts -file client2.cer

keytool -importcert -keystore client2.truststore.jks -storepass secret -alias localhost -trustcacerts -file server2.cer
```

## Run configure.cli batch 

# Test the dynamic client ssl context 
Access to both http://localhost:8080/proxy and http://localhost:8080/proxy2 will successfully return Welcome to WildFly page.
Server running on port 8080 will use dynamic ssl context to ping the other 2 ports with appropriate SSL contexts.
The /proxy URL pings server on port 9443 and the /proxy2 URL pings port 10443.
