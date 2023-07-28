package com.amazonaws.kinesisvideo.utils;

import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.ALGORITHM_AWS4_HMAC_SHA_256;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.AWS4_REQUEST_TYPE;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.METHOD;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.SERVICE;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.SIGNED_HEADERS;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.X_AMZ_ALGORITHM;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.X_AMZ_CREDENTIAL;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.X_AMZ_DATE;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.X_AMZ_EXPIRES;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.X_AMZ_SECURITY_TOKEN;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.X_AMZ_SIGNATURE;
import static com.amazonaws.kinesisvideo.utils.AwsV4SignerConstants.X_AMZ_SIGNED_HEADERS;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.buildQueryParamsMap;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.createCredentialScope;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.getCanonicalRequest;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.getCanonicalUri;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.getCanonicalizedQueryString;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.getDateStamp;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.getSignatureKey;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.getTimeStamp;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.hmacSha256;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.sign;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.signString;
import static com.amazonaws.kinesisvideo.utils.AwsV4Signer.urlEncode;
import static org.junit.Assert.assertEquals;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.amazonaws.util.BinaryUtils;

import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

// https://docs.aws.amazon.com/general/latest/gr/signature-v4-test-suite.html#signature-v4-test-suite-example
public class AwsV4SignerTest {

    @Test
    public void when_signMasterURLWithTemporaryCredentials_then_returnValidSignedURL() {
        final String masterURIToSignProtocolAndHost = "wss://m-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com";
        final URI uriToSign = URI.create(masterURIToSignProtocolAndHost + "?X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123");
        final String accessKeyId = "AKIAIOSFODNN7EXAMPLE";
        final String secretKeyId = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
        final String sessionToken = "AQoEXAMPLEH4aoAH0gNCAPyJxz4BlCFFxWNE1OPTgk5TthT+FvwqnKwRcOIfrRh3c/LTo6UDdyJwOOvEVPvLXCrrrUtdnniCEXAMPLE/IvU1dYUg2RVAJBanLiHb4IgRmpRV3zrkuWJOgQs8IZZaIv2BXIa2R4OlgkBN9bkUDNCJiBeb/AXlzBBko7b15fjrBs2+cTQtpZ3CYWFXG8C5zqx37wnOE49mRl/+OtkIKGO7fAE";
        final String region = "us-west-2";
        final long dateMilli = 1690186022951L;

        final URI expected = URI.create(masterURIToSignProtocolAndHost + "/?" + X_AMZ_ALGORITHM + "=" + ALGORITHM_AWS4_HMAC_SHA_256 + "&X-Amz-ChannelARN=arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123&" + X_AMZ_CREDENTIAL + "=AKIAIOSFODNN7EXAMPLE%2F20230724%2F" + region + "%2F" + SERVICE + "%2F" + AWS4_REQUEST_TYPE + "&" + X_AMZ_DATE + "=20230724T080702Z&" + X_AMZ_EXPIRES + "=299&" + X_AMZ_SECURITY_TOKEN + "=AQoEXAMPLEH4aoAH0gNCAPyJxz4BlCFFxWNE1OPTgk5TthT%2BFvwqnKwRcOIfrRh3c%2FLTo6UDdyJwOOvEVPvLXCrrrUtdnniCEXAMPLE%2FIvU1dYUg2RVAJBanLiHb4IgRmpRV3zrkuWJOgQs8IZZaIv2BXIa2R4OlgkBN9bkUDNCJiBeb%2FAXlzBBko7b15fjrBs2%2BcTQtpZ3CYWFXG8C5zqx37wnOE49mRl%2F%2BOtkIKGO7fAE&" + X_AMZ_SIGNED_HEADERS + "=" + SIGNED_HEADERS + "&" + X_AMZ_SIGNATURE + "=f8fed632bbe38ac920c7ed2eeaba1a4ba5e2b1bd7aada9f852708112eab76baa");
        final URI actual = sign(uriToSign, accessKeyId, secretKeyId, sessionToken, region, dateMilli);

        assertEquals(expected, actual);
    }

    @Test
    public void when_signViewerURLWithTemporaryCredentials_then_returnValidSignedURL() {
        final String viewerURIToSignProtocolAndHost = "wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com";
        final URI uriToSign = URI.create(viewerURIToSignProtocolAndHost + "?X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123&X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557");

        final String accessKeyId = "AKIAIOSFODNN7EXAMPLE";
        final String secretKeyId = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
        final String sessionToken = "AQoEXAMPLEH4aoAH0gNCAPyJxz4BlCFFxWNE1OPTgk5TthT+FvwqnKwRcOIfrRh3c/LTo6UDdyJwOOvEVPvLXCrrrUtdnniCEXAMPLE/IvU1dYUg2RVAJBanLiHb4IgRmpRV3zrkuWJOgQs8IZZaIv2BXIa2R4OlgkBN9bkUDNCJiBeb/AXlzBBko7b15fjrBs2+cTQtpZ3CYWFXG8C5zqx37wnOE49mRl/+OtkIKGO7fAE";
        final String region = "us-west-2";
        final long dateMilli = 1690186022958L;

        final URI expected = URI.create(viewerURIToSignProtocolAndHost + "/?" + X_AMZ_ALGORITHM + "=" + ALGORITHM_AWS4_HMAC_SHA_256 + "&X-Amz-ChannelARN=arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123&X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557&" + X_AMZ_CREDENTIAL + "=" + accessKeyId + "%2F20230724%2F" + region + "%2F" + SERVICE + "%2F" + AWS4_REQUEST_TYPE + "&" + X_AMZ_DATE + "=20230724T080702Z&" + X_AMZ_EXPIRES + "=299&" + X_AMZ_SECURITY_TOKEN + "=AQoEXAMPLEH4aoAH0gNCAPyJxz4BlCFFxWNE1OPTgk5TthT%2BFvwqnKwRcOIfrRh3c%2FLTo6UDdyJwOOvEVPvLXCrrrUtdnniCEXAMPLE%2FIvU1dYUg2RVAJBanLiHb4IgRmpRV3zrkuWJOgQs8IZZaIv2BXIa2R4OlgkBN9bkUDNCJiBeb%2FAXlzBBko7b15fjrBs2%2BcTQtpZ3CYWFXG8C5zqx37wnOE49mRl%2F%2BOtkIKGO7fAE&" + X_AMZ_SIGNED_HEADERS + "=" + SIGNED_HEADERS + "&" + X_AMZ_SIGNATURE + "=77ea5ff8ede2e22aa268a3a068f1ad3a5d92f0fa8a427579f9e6376e97139761");
        final URI actual = sign(uriToSign, accessKeyId, secretKeyId, sessionToken, region, dateMilli);

        assertEquals(expected, actual);
    }

    @Test
    public void when_signMasterURLWithLongTermCredentials_then_returnValidSignedURL() {
        final String masterURIToSignProtocolAndHost = "wss://m-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com";
        final URI uriToSign = URI.create(masterURIToSignProtocolAndHost + "?X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123");
        final String accessKeyId = "AKIAIOSFODJJ7EXAMPLE";
        final String secretKeyId = "wJalrXUtnFEMI/K7MDENG/bPxQQiCYEXAMPLEKEY";
        final String sessionToken = null;
        final String region = "us-west-2";
        final long dateMilli = 1690186022101L;

        final URI expected = URI.create(masterURIToSignProtocolAndHost + "/?" + X_AMZ_ALGORITHM + "=" + ALGORITHM_AWS4_HMAC_SHA_256 + "&X-Amz-ChannelARN=arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123&" + X_AMZ_CREDENTIAL + "=" + accessKeyId + "%2F20230724%2F" + region + "%2F" + SERVICE + "%2F" + AWS4_REQUEST_TYPE + "&" + X_AMZ_DATE + "=20230724T080702Z&" + X_AMZ_EXPIRES + "=299&" + X_AMZ_SIGNED_HEADERS + "=" + SIGNED_HEADERS + "&" + X_AMZ_SIGNATURE + "=0bbef329f0d9d3e68635f7b844ac684c7764a0c228ca013232d935c111b9a370");
        final URI actual = sign(uriToSign, accessKeyId, secretKeyId, sessionToken, region, dateMilli);

        assertEquals(expected, actual);
    }

    @Test
    public void when_signViewerURLWithLongTermCredentials_then_returnValidSignedURL() {
        final String viewerURIToSignProtocolAndHost = "wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com";
        final URI uriToSign = URI.create(viewerURIToSignProtocolAndHost + "?X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123&X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557");
        final String accessKeyId = "AKIAIOSFODJJ7EXAMPLE";
        final String secretKeyId = "wJalrXUtnFEMI/K7MDENG/bPxQQiCYEXAMPLEKEY";
        final String sessionToken = "";
        final String region = "us-west-2";
        final long dateMilli = 1690186022208L;

        final URI expected = URI.create(viewerURIToSignProtocolAndHost + "/?" + X_AMZ_ALGORITHM + "=" + ALGORITHM_AWS4_HMAC_SHA_256 + "&X-Amz-ChannelARN=arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123&X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557&" + X_AMZ_CREDENTIAL + "=" + accessKeyId + "%2F20230724%2F" + region + "%2F" + SERVICE + "%2F" + AWS4_REQUEST_TYPE + "&" + X_AMZ_DATE + "=20230724T080702Z&" + X_AMZ_EXPIRES + "=299&" + X_AMZ_SIGNED_HEADERS + "=" + SIGNED_HEADERS + "&" + X_AMZ_SIGNATURE + "=cea541f699dc51bc53a55590ce817e63cc06fac2bdef4696b63e0889eb448f0b");
        final URI actual = sign(uriToSign, accessKeyId, secretKeyId, sessionToken, region, dateMilli);

        assertEquals(expected, actual);
    }

    @Test
    public void when_getCanonicalRequestWithQueryParameters_then_validCanonicalQueryStringReturned() {

        final String canonicalResultExpected =
                METHOD + "\n" +
                        "/\n" +
                        "Param1=value1&Param2=value2\n" +
                        "host:example.amazonaws.com\n" +
                        "\n" +
                        SIGNED_HEADERS + "\n" +
                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        final URI uri = URI.create("http://example.amazonaws.com");

        // Add these out of order to ensure that the query parameters are in alphabetical order
        final Map<String, String> paramsMap = new HashMap<String, String>() {{
            put("Param2", "value2");
            put("Param1", "value1");
        }};

        final String canonicalQuerystring = getCanonicalRequest(uri, getCanonicalizedQueryString(paramsMap));

        assertEquals(canonicalResultExpected, canonicalQuerystring);
    }

    @Test
    public void when_getCanonicalUriWithVariousResources_then_correctResourceReturned() {
        final String expectedResource = "/";

        final String actualResource = getCanonicalUri(URI.create("wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com"));

        assertEquals(expectedResource, actualResource);

        final String expectedResource2 = "/";

        final String actualResource2 = getCanonicalUri(URI.create("wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com/"));

        assertEquals(expectedResource2, actualResource2);

        final String expectedResource3 = "/hey";

        final String actualResource3 = getCanonicalUri(URI.create("wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com/hey"));

        assertEquals(expectedResource3, actualResource3);
    }

    @Test
    public void when_buildQueryParamsMapWithLongTermCredentials_then_mapContainsCorrectParametersAndDoesNotContainXAmzSecurityToken() {
        // This URI has two query parameters that we expect to get added to the map: X-Amz-ChannelARN
        // We also expect the query parameter values to be url encoded.
        final URI testUri = URI.create("wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com?X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123");
        final String testAccessKeyId = "AKIDEXAMPLE";
        final String testSessionToken = null; // since session token is not provided, we expect X-Amz-Security-Token to not be present in the map
        final String testRegion = "us-west-2";
        final String testTimestamp = "20230724T000000Z";
        final String testDatestamp = "20230724";

        final Map<String, String> expectedQueryParams = new HashMap<>();
        expectedQueryParams.put(X_AMZ_ALGORITHM, ALGORITHM_AWS4_HMAC_SHA_256);
        expectedQueryParams.put(X_AMZ_CREDENTIAL, testAccessKeyId + "%2F" + testDatestamp + "%2F" + testRegion + "%2F" + SERVICE + "%2F" + AWS4_REQUEST_TYPE);
        expectedQueryParams.put(X_AMZ_DATE, testTimestamp);
        expectedQueryParams.put(X_AMZ_EXPIRES, "299");
        expectedQueryParams.put(X_AMZ_SIGNED_HEADERS, SIGNED_HEADERS);
        expectedQueryParams.put("X-Amz-ChannelARN", "arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123");

        final Map<String, String> actualQueryParams = buildQueryParamsMap(testUri,
                testAccessKeyId,
                testSessionToken,
                testRegion,
                testTimestamp,
                testDatestamp);

        assertEquals(expectedQueryParams, actualQueryParams);
    }

    @Test
    public void when_buildQueryParamsMapWithTemporaryCredentials_then_mapContainsCorrectParameters() {
        // This URI has two query parameters that we expect to get added to the map: X-Amz-ChannelARN and X-Amz-ClientId
        // We also expect the query parameter values to be url encoded.
        final URI testUri = URI.create("wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com?X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123&X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557");
        final String testAccessKeyId = "AKIDEXAMPLE";
        final String testSessionToken = "SSEXAMPLE";
        final String testRegion = "us-west-2";
        final String testTimestamp = "20230724T000000Z";
        final String testDatestamp = "20230724";

        final Map<String, String> expectedQueryParams = new HashMap<>();
        expectedQueryParams.put(X_AMZ_ALGORITHM, ALGORITHM_AWS4_HMAC_SHA_256);
        expectedQueryParams.put(X_AMZ_CREDENTIAL, "AKIDEXAMPLE%2F" + testDatestamp + "%2F" + testRegion + "%2F" + SERVICE + "%2F" + AWS4_REQUEST_TYPE);
        expectedQueryParams.put(X_AMZ_DATE, testTimestamp);
        expectedQueryParams.put(X_AMZ_EXPIRES, "299");
        expectedQueryParams.put(X_AMZ_SIGNED_HEADERS, SIGNED_HEADERS);
        expectedQueryParams.put(X_AMZ_SECURITY_TOKEN, "SSEXAMPLE");
        expectedQueryParams.put("X-Amz-ChannelARN", "arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123");
        expectedQueryParams.put("X-Amz-ClientId", "d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557");

        final Map<String, String> actualQueryParams = buildQueryParamsMap(testUri,
                testAccessKeyId,
                testSessionToken,
                testRegion,
                testTimestamp,
                testDatestamp);

        assertEquals(expectedQueryParams, actualQueryParams);
    }

    @Test
    public void when_signStringWithExampleInputs_then_validStringToSignIsReturned() {

        final String credentialScope = "AKIDEXAMPLE/20150830/us-east-1/service/aws4_request";
        final String requestDate = "20150830T123600Z";
        final String canonicalRequest = METHOD + "\n" +
                "/\n" +
                "Param1=value1&Param2=value2\n" +
                "host:example.amazonaws.com\n" +
                "x-amz-date:20150830T123600Z\n" +
                "\n" +
                SIGNED_HEADERS + ";x-amz-date\n" +
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        final String expected = "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "AKIDEXAMPLE/20150830/us-east-1/service/" + AWS4_REQUEST_TYPE + "\n" +
                "816cd5b414d056048ba4f7c5386d6e0533120fb1fcfa93762cf0fc39e2cf19e0";

        final String actual = signString(requestDate, credentialScope, canonicalRequest);

        assertEquals(expected, actual);

    }

    @Test
    public void when_getSignatureKeyWithStringToSign_then_validSignatureKeyReturned() {

        final String stringToSign = ALGORITHM_AWS4_HMAC_SHA_256 + "\n" +
                "20150830T123600Z\n" +
                "20150830/us-east-1/iam/" + AWS4_REQUEST_TYPE + "\n" +
                "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";

        final byte[] signatureKeyBytes = getSignatureKey("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                "20150830", "us-east-1", "iam");

        final String expectedSignature = "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9";
        assertEquals(expectedSignature, BinaryUtils.toHex(signatureKeyBytes));

        final String expectedSignatureString = "5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";
        assertEquals(expectedSignatureString, BinaryUtils.toHex(hmacSha256(stringToSign, signatureKeyBytes)));

    }

    @Test
    public void when_urlEncodeStringWithSpecialCharacters_then_specialCharactersAreURLEncoded() {
        final String exampleArn = "arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123";

        final String expected = "arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123";

        final String actual = urlEncode(exampleArn);

        assertEquals(expected, actual);
    }

    @Test(expected = RuntimeException.class)
    public void when_urlEncodeGivenNull_then_exceptionIsThrown() {
        urlEncode(null);
    }

    @Test
    public void when_createCredentialScope_then_validCredentialScopeReturned() {
        final String expected = "20150930/us-west-2/" + SERVICE + "/" + AWS4_REQUEST_TYPE;

        final String actual = createCredentialScope("us-west-2", "20150930");

        assertEquals(expected, actual);
    }

    @Test
    public void when_hmacSha256GivenKeyAndData_then_correctSignatureIsReturned() {
        byte[] testKey = "testKey".getBytes(UTF_8);

        final String testData = "testData123";

        final String expectedHex = "f8117085c5b8be75d01ce86d16d04e90fedfc4be4668fe75d39e72c92da45568";

        final byte[] actualResult = hmacSha256(testData, testKey);

        final String actualHex = BinaryUtils.toHex(actualResult);

        assertEquals(expectedHex, actualHex);
    }

    @Test(expected = RuntimeException.class)
    public void when_HmacSha256GivenNulls_then_exceptionThrown() {
        hmacSha256(null, null);
    }

    @Test
    public void when_getTimeStampAroundMidnightUTC_then_dateAndTimeReturnedIsCorrectAndFormattedCorrectly() {
        // 1689984000000 = Saturday, July 22, 2023 12:00:00.000 AM (UTC)
        final String atMidnightUTC = getTimeStamp(1689984000000L);

        assertEquals("20230722T000000Z", atMidnightUTC);

        // 1689983999999 = Friday, July 21, 2023 11:59:59.999 AM (UTC)
        final String rightBeforeMidnightUTC = getTimeStamp(1689983999999L);

        assertEquals("20230721T235959Z", rightBeforeMidnightUTC);
    }

    @Test
    public void when_getDateStampAroundMidnightUTC_then_dateReturnedIsCorrectAndFormattedCorrectly() {
        // 1689984000000 = Saturday, July 22, 2023 12:00:00.000 AM (UTC)
        final String atMidnightUTC = getDateStamp(1689984000000L);

        assertEquals("20230722", atMidnightUTC);

        // 1689983999999 = Friday, July 21, 2023 11:59:59.999 AM (UTC)
        final String rightBeforeMidnightUTC = getDateStamp(1689983999999L);

        assertEquals("20230721", rightBeforeMidnightUTC);
    }

}
