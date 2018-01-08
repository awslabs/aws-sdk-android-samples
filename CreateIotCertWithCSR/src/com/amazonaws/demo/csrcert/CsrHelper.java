/**
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.demo.csrcert;

import android.util.Base64;

import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.ExtensionsGenerator;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.spongycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.bc.BcRSAContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.io.IOException;
import java.security.KeyPair;

public class CsrHelper {
    /**
     * Create the certificate signing request (CSR) from private and public keys
     *
     * @param keyPair the KeyPair with private and public keys
     * @return PKCS10CertificationRequest with the certificate signing request
     *         (CSR) data
     * @throws IOException
     * @throws OperatorCreationException
     */
    public static PKCS10CertificationRequest generateCSR(KeyPair keyPair) throws IOException,
            OperatorCreationException {
        String principal = "CN=AWS IoT Certificate" + ", O=Amazon";
        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate()
                .getEncoded());
        AlgorithmIdentifier signatureAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder()
                .find("SHA1WITHRSA");
        AlgorithmIdentifier digestAlgorithm = new DefaultDigestAlgorithmIdentifierFinder()
                .find("SHA-1");
        ContentSigner signer = new BcRSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm)
                .build(privateKey);

        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
                new X500Name(principal), keyPair.getPublic());
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        extensionsGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(
                true));
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                extensionsGenerator.generate());
        PKCS10CertificationRequest csr = csrBuilder.build(signer);

        return csr;
    }

    /**
     * Generate the certificate signing request (CSR) Pem string given the keypair.
     * @param keyPair
     * @return the CSRPem String
     * @throws IOException
     * @throws OperatorCreationException
     */
    public static String generateCsrPemString(KeyPair keyPair) throws IOException,
            OperatorCreationException {
        PKCS10CertificationRequest csr = generateCSR(keyPair);
        byte[] derCSR = csr.getEncoded();
        return Base64.encodeToString(derCSR, Base64.NO_PADDING | Base64.NO_WRAP);
    }
}
