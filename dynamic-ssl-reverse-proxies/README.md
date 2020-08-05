# Demonstrate use of dynamic client ssl context with configuration of 2 reverse proxies

Build wildfly server with following feature branches:



## Generate certificates for 2 different mutual ssl contexts (client1 has mutual trust with server1 and client2 with server2)

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
This batch configures client1-ssl-context and client2-ss-context, 
configures dynamic-client-ssl-context that uses client1-ssl-context for port 9443 and client2-ssl-context for 10443.
Then it configures 2 reverse proxies, first for server running on port 9443 and second running on 10443. 
Both reverse proxies use configured dynamic-client-ssl-context

# Run and configure 2 servers

Run first server with offset 1000 and configure it with server-ssl-context that uses server1 keystore and server1 truststore:

```
./standalone.sh -Djboss.socket.binding.port-offset=1000
```

Run second server with offset 2000 and configure it with server-ssl-context that uses server2 keystore and server2 truststore:

```
./standalone.sh -Djboss.socket.binding.port-offset=2000
```


# Test the dynamic client ssl context 
Access to both https://localhost:8443/proxy and https://localhost:8443/proxy2  will successfully return Welcome to WildFly page.
Server running on port 8443 will use dynamic ssl context to ping the other 2 servers with appropriate SSL contexts.
The /proxy URL pings server running on port 9443 and the /proxy2 URL pings server running on port 10443.
