

## Generate certificates for 2 different mutual ssl contexts

```
keytool -genkeypair -alias localhost1 -keyalg RSA -keysize 1024 -validity 365 -keystore server1.keystore.jks -dname "CN=localhost1" -keypass secret -storepass secret

keytool -genkeypair -alias client1 -keyalg RSA -keysize 1024 -validity 365 -keystore client1.keystore.jks -dname "CN=client1" -keypass secret -storepass secret

keytool -exportcert  -keystore server1.keystore.jks -alias localhost1 -keypass secret -storepass secret -file server1.cer

keytool -exportcert  -keystore client1.keystore.jks -alias client1 -keypass secret -storepass secret -file client1.cer

keytool -importcert -keystore server1.truststore.jks -storepass secret -alias client1 -trustcacerts -file client1.cer

keytool -importcert -keystore client1.truststore.jks -storepass secret -alias localhost1 -trustcacerts -file server1.cer

keytool -genkeypair -alias localhost2 -keyalg RSA -keysize 1024 -validity 365 -keystore server2.keystore.jks -dname "CN=localhost2" -keypass secret -storepass secret

keytool -genkeypair -alias client2 -keyalg RSA -keysize 1024 -validity 365 -keystore client2.keystore.jks -dname "CN=client2" -keypass secret -storepass secret

keytool -exportcert  -keystore server2.keystore.jks -alias localhost2 -keypass secret -storepass secret -file server2.cer

keytool -exportcert  -keystore client2.keystore.jks -alias client2 -keypass secret -storepass secret -file client2.cer

keytool -importcert -keystore server2.truststore.jks -storepass secret -alias client2 -trustcacerts -file client2.cer

keytool -importcert -keystore client2.truststore.jks -storepass secret -alias localhost2 -trustcacerts -file server2.cer
```
