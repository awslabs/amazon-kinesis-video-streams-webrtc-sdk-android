package com.amazonaws.kinesisvideo.utils;

import com.amazonaws.util.BinaryUtils;

import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.*;
import static org.junit.Assert.*;

public class AwsV4SignerTest {

// https://docs.aws.amazon.com/general/latest/gr/signature-v4-test-suite.html#signature-v4-test-suite-example

    @Test
    public void getCanonicalRequestTest() {

        String canonicalResultExpected=
                "GET\n" +
                        "/\n" +
                        "Param1=value1&Param2=value2\n" +
                        "host:example.amazonaws.com\n" +
                        "\n" +
                        "host\n" +
                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        URI uri = URI.create("http://example.amazonaws.com");

        Map<String, String> paramsMap =  new HashMap<String,String>() {{
            put ("Param1", "value1");
            put("Param2", "value2");
        }};

        String canonicalQuerystring = getCanonicalRequest(uri, getCanonicalizedQueryString(paramsMap));

        assertEquals(canonicalResultExpected,canonicalQuerystring);

    }

    @Test
    public void signStringTest() {

        String credentialScope = "AKIDEXAMPLE/20150830/us-east-1/service/aws4_request";
        String requestDate = "20150830T123600Z";
        String canonicalRequest = "GET\n" +
                "/\n" +
                "Param1=value1&Param2=value2\n" +
                "host:example.amazonaws.com\n" +
                "x-amz-date:20150830T123600Z\n" +
                "\n" +
                "host;x-amz-date\n" +
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        String result = "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "AKIDEXAMPLE/20150830/us-east-1/service/aws4_request\n" +
                "816cd5b414d056048ba4f7c5386d6e0533120fb1fcfa93762cf0fc39e2cf19e0";

        assertEquals(signString(requestDate, credentialScope, canonicalRequest), result);

    }

    @Test
    public void getSignatureKeyTest() {

        String stringToSign = "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "20150830/us-east-1/iam/aws4_request\n" +
                "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";

        byte[] signatureKeyBytes = getSignatureKey("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                "20150830", "us-east-1", "iam");

        String expectedSignature = "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9";
        assertEquals(expectedSignature, BinaryUtils.toHex(signatureKeyBytes));

        String expectedSignatureString = "5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";
        assertEquals(expectedSignatureString, BinaryUtils.toHex(hmacSha256(stringToSign, signatureKeyBytes)));

    }
}
