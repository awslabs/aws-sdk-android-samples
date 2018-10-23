/**
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.cognito.sync.devauth.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.security.AlgorithmParameters;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This utility class provides a wrapper for the encryption used by the Cognito
 * Developer Sample Authentication sample
 */
public class AESEncryption {

    /**
     * Encryption algorithm used
     */
    public static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Decrypt a string with given key.
     * 
     * @param cipherText encrypted string
     * @param key the key used in decryption
     * @return a decrypted string
     */
    public static String unwrap(String cipherText, String key) throws Exception {
        byte[] dataToDecrypt = Base64.decodeBase64(cipherText.getBytes());
        byte[] initializationVector = new byte[16];
        byte[] data = new byte[dataToDecrypt.length - 16];

        System.arraycopy(dataToDecrypt, 0, initializationVector, 0, 16);
        System.arraycopy(dataToDecrypt, 16, data, 0, dataToDecrypt.length - 16);

        byte[] plainText = decrypt(data, key, initializationVector);
        return new String(plainText);
    }

    /**
     * Decrypt a cipher in bytes using the specified key
     * 
     * @param cipherBytes encrypted bytes
     * @param key the key used in decryption
     * @param iv
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] cipherBytes, String key, byte[] iv)
            throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        cipher.init(Cipher.DECRYPT_MODE, getKey(key), params);
        return cipher.doFinal(cipherBytes);
    }

    private static SecretKeySpec getKey(String key) throws Exception {
        return new SecretKeySpec(Hex.decodeHex(key.toCharArray()), "AES");
    }
}
