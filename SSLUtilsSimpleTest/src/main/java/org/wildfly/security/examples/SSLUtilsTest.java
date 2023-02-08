/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.security.examples;

import org.wildfly.common.bytes.ByteStringBuilder;
import org.wildfly.security.keystore.KeyStoreUtil;
import org.wildfly.security.pem.Pem;
import org.wildfly.security.ssl.SSLContextBuilder;
import org.wildfly.security.x500.cert.SelfSignedX509CertificateAndSigningKey;
import org.wildfly.security.x500.cert.X509CertificateBuilder;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

import org.junit.Assert;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.util.function.Supplier;

/**
 * Examples for how to use CertificateGenerator
 *
 * @author <a href="mailto:jucook@redhat.com">Justin Cook</a>
 */
public class SSLUtilsTest {
    private ArrayList<String> aliases = new ArrayList<>();
    private ArrayList<String> distinguishedNames = new ArrayList<>();
    private ArrayList<char[]> keyPasswords = new ArrayList<>();
    private static String outputLocation = SSLUtilsTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "dynamic-certificates/custom/";

    private static File workingDir = getWorkingDir();

    private static final Supplier<Provider[]> providerSupplier = () -> Security.getProviders();

    public static void main(String[] args) throws Exception {


        SelfSignedX509CertificateAndSigningKey ca = SelfSignedX509CertificateAndSigningKey.builder()
                .setDn(new X500Principal("O=Root Certificate Authority, EMAILADDRESS=elytron@wildfly.org, C=UK, ST=Elytron, CN=Elytron CA"))
                .setKeyAlgorithmName("RSA")
                .setSignatureAlgorithmName("SHA256withRSA")
                .addExtension(false, "BasicConstraints", "CA:true,pathlen:2147483647")
                .build();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair generatedKeys = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = generatedKeys.getPublic();
        X509Certificate subjectCertificate = new X509CertificateBuilder()
                .setIssuerDn(ca.getSelfSignedCertificate().getIssuerX500Principal())
                .setSubjectDn(new X500Principal("O=Elytron, OU=Elytron, C=UK, ST=Elytron, CN=Firefly"))
                .setSignatureAlgorithmName("SHA256withRSA")
                .setSigningKey(ca.getSigningKey())
                .setPublicKey(publicKey)
                .build();
        ByteStringBuilder target = new ByteStringBuilder();
        Pem.generatePemX509Certificate(target, ca.getSelfSignedCertificate());
        Pem.generatePemX509Certificate(target, subjectCertificate);
        Files.write(Paths.get(workingDir.getPath(), "pem.pem"), target.toArray());

        KeyStore loadedStoreFromByteArrayInputStream = KeyStoreUtil.loadKeyStore(new ByteArrayInputStream(target.toArray()), "".toCharArray());
        Assert.assertNotNull(loadedStoreFromByteArrayInputStream);
        Assert.assertEquals(ca.getSelfSignedCertificate(), loadedStoreFromByteArrayInputStream.getCertificate(ca.getSelfSignedCertificate().getSubjectX500Principal().getName()));
        Assert.assertEquals(subjectCertificate, loadedStoreFromByteArrayInputStream.getCertificate(subjectCertificate.getSubjectX500Principal().getName()));

        KeyStore loadedStoreFromFileInputStream = KeyStoreUtil.loadKeyStore(providerSupplier, null, new FileInputStream(new File(workingDir, "pem.pem")), "pem.pem", "".toCharArray());
        Assert.assertNotNull(loadedStoreFromFileInputStream);
        Assert.assertEquals(ca.getSelfSignedCertificate(), loadedStoreFromFileInputStream.getCertificate(ca.getSelfSignedCertificate().getSubjectX500Principal().getName()));
        Assert.assertEquals(subjectCertificate, loadedStoreFromFileInputStream.getCertificate(subjectCertificate.getSubjectX500Principal().getName()));

        SSLContext sslContext = new SSLContextBuilder()
                .setKeyManager(getKeyManager(loadedStoreFromByteArrayInputStream, "".toCharArray()))
                // can also set anything else you need here like the trust manager, cipher suites, etc.
                .setWantClientAuth(true)
                .build()
                .create();

        System.out.println(sslContext.getSupportedSSLParameters().getWantClientAuth());
        System.out.println(sslContext.getProvider().getName());
        System.out.println(sslContext.getProtocol());
        System.out.println(loadedStoreFromByteArrayInputStream.aliases().asIterator().next().toString());
        System.out.println(loadedStoreFromByteArrayInputStream.aliases().asIterator().next().toString());
        Assert.assertEquals(loadedStoreFromByteArrayInputStream.aliases().asIterator().next().toString(), loadedStoreFromByteArrayInputStream.aliases().asIterator().next().toString());


    }

    private static X509ExtendedKeyManager getKeyManager(KeyStore keyStore, char[] password) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        for (KeyManager current : keyManagerFactory.getKeyManagers()) {
            if (current instanceof X509ExtendedKeyManager) {
                return (X509ExtendedKeyManager) current;
            }
        }
        throw new IllegalStateException("Unable to obtain X509ExtendedKeyManager.");
    }

    private static File getWorkingDir() {
        File workingDir = new File("./target/keystore");
        if (workingDir.exists() == false) {
            workingDir.mkdirs();
        }
        return workingDir;
    }
}
