package org.wildfly.security.examples;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.wildfly.security.auth.server.IdentityCredentials;
import org.wildfly.security.credential.KeyPairCredential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.credential.PublicKeyCredential;
import org.wildfly.security.credential.SecretKeyCredential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStore.CredentialSourceProtectionParameter;
import org.wildfly.security.credential.store.CredentialStore.ProtectionParameter;
import org.wildfly.security.credential.store.WildFlyElytronCredentialStoreProvider;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.ClearPassword;
import org.wildfly.security.password.interfaces.MaskedPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.MaskedPasswordAlgorithmSpec;

public class CredentialStoreExample {

    private static final Provider CREDENTIAL_STORE_PROVIDER = new WildFlyElytronCredentialStoreProvider();
    private static final Provider PASSWORD_PROVIDER = new WildFlyElytronPasswordProvider();

    private static final int ITERATION = 120;
    private static final String SALT = "EImaging";
    private static final char[] KEY = "somearbitrarycrazystringthatdoesnotmatter".toCharArray();

    static {
        Security.addProvider(PASSWORD_PROVIDER);
    }

    private static void populateCredentialStore() throws Exception {

        Password storePassword = ClearPassword.createRaw(ClearPassword.ALGORITHM_CLEAR, "StorePassword".toCharArray());
        ProtectionParameter protectionParameter = new CredentialSourceProtectionParameter(IdentityCredentials.NONE.withCredential(new PasswordCredential(storePassword)));
        CredentialStore credentialStore = CredentialStore.getInstance("KeyStoreCredentialStore", CREDENTIAL_STORE_PROVIDER);
        Map<String, String> configuration = new HashMap<>();
        configuration.put("location", "mystore.cs");
        configuration.put("create", "true");

        credentialStore.initialize(configuration, protectionParameter);

        // Clear Password
        Password clearPassword = ClearPassword.createRaw(ClearPassword.ALGORITHM_CLEAR, "ExamplePassword".toCharArray());
        credentialStore.store("clearPassword", new PasswordCredential(clearPassword));

        credentialStore.flush();
    }

    private static MaskedPassword createMaskedPassword(char[] maskedPassword)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PasswordFactory passwordFactory =
                PasswordFactory.getInstance(MaskedPassword.ALGORITHM_MASKED_MD5_DES, CREDENTIAL_STORE_PROVIDER);
        MaskedPasswordAlgorithmSpec maskedAlgorithmSpec =
                new MaskedPasswordAlgorithmSpec(KEY, ITERATION, SALT.getBytes());
        EncryptablePasswordSpec
                encryptableSpec =
                new EncryptablePasswordSpec(maskedPassword, maskedAlgorithmSpec);
        return (MaskedPassword) passwordFactory.generatePassword(encryptableSpec);
    }

    private static void retrieveCredentials() throws Exception {

        // Get an instance of the CredentialStore
        CredentialStore credentialStore = CredentialStore.getInstance("KeyStoreCredentialStore", CREDENTIAL_STORE_PROVIDER);
        // Configure and Initialise the CredentialStore
        Map<String, String> configuration = new HashMap<>();
        configuration.put("location", "mystore.cs");

        MaskedPassword maskedPassword = createMaskedPassword("StorePassword".toCharArray());


        CredentialStore.ProtectionParameter protectionParameter =
                new CredentialStore.CredentialSourceProtectionParameter(
                        IdentityCredentials.NONE.withCredential(new PasswordCredential(maskedPassword)));

        credentialStore.initialize(configuration, protectionParameter);

        credentialStore.getAliases().stream().forEach(System.out::println);


        PasswordCredential passwordCredential = credentialStore.retrieve("clearpassword", PasswordCredential.class);
        Password password = passwordCredential.getPassword();
        System.out.println(new String(((ClearPassword) password).getPassword()));
    }

    public static void main(String[] args) throws Exception {

        /*
         * Create a ProtectionParameter for access to the store.
         */

        populateCredentialStore();

        retrieveCredentials();
    }

}