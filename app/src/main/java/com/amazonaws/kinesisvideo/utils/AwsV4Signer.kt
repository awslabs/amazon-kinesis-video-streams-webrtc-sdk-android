package com.amazonaws.kinesisvideo.utils

import com.amazonaws.util.BinaryUtils
import com.amazonaws.util.DateUtils
import com.google.common.collect.ImmutableMap
import com.google.common.hash.Hashing
import org.apache.commons.lang3.StringUtils
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.Date
import java.util.Optional
import java.util.StringJoiner
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

const val ALGORITHM_AWS4_HMAC_SHA_256 = "AWS4-HMAC-SHA256"
const val AWS4_REQUEST_TYPE = "aws4_request"
const val SERVICE = "kinesisvideo"
const val X_AMZ_ALGORITHM = "X-Amz-Algorithm"
const val X_AMZ_CREDENTIAL = "X-Amz-Credential"
const val X_AMZ_DATE = "X-Amz-Date"
const val X_AMZ_EXPIRES = "X-Amz-Expires"
const val X_AMZ_SECURITY_TOKEN = "X-Amz-Security-Token"
const val X_AMZ_SIGNATURE = "X-Amz-Signature"
const val X_AMZ_SIGNED_HEADERS = "X-Amz-SignedHeaders"
const val NEW_LINE_DELIMITER = "\n"
const val DATE_PATTERN = "yyyyMMdd"
const val TIME_PATTERN = "yyyyMMdd'T'HHmmss'Z'"
const val METHOD = "GET"
const val SIGNED_HEADERS = "host"

object AwsV4Signer {
    // Guide - https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html
    // Implementation based on https://docs.aws.amazon.com/general/latest/gr/sigv4-signed-request-examples.html#sig-v4-examples-get-query-string

    // Guide - https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html
    // Implementation based on https://docs.aws.amazon.com/general/latest/gr/sigv4-signed-request-examples.html#sig-v4-examples-get-query-string
    /**
     * Constructs a WebRTC WebSocket connect URI with Query Parameters to connect to Kinesis Video Signaling.
     *
     * @param uri          The URL to sign.
     *
     *
     * **Connect as Master URL** - GetSignalingChannelEndpoint (master role) + Query Parameters: Channel ARN as X-Amz-ChannelARN
     * [Additional info](https://docs.aws.amazon.com/kinesisvideostreams-webrtc-dg/latest/devguide/kvswebrtc-websocket-apis-2.html)
     *
     *
     * **Connect as Viewer URL** - GetSignalingChannelEndpoint (viewer role) + Query Parameters: Channel ARN as X-Amz-ChannelARN & Client Id as X-Amz-ClientId
     * [Additional info](https://docs.aws.amazon.com/kinesisvideostreams-webrtc-dg/latest/devguide/kvswebrtc-websocket-apis-1.html)
     *
     *
     * Viewer URL example: wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com?X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123&amp;X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557
     *
     *
     * **Note**: The Signaling Channel Endpoints are different depending on the role (master/viewer) specified in GetSignalingChannelEndpoint API call.
     * [Additional info](https://docs.aws.amazon.com/kinesisvideostreams/latest/dg/API_SingleMasterChannelEndpointConfiguration.html#KinesisVideo-Type-SingleMasterChannelEndpointConfiguration-Role)
     * @param accessKey    AWS Access Key Id.
     * @param secretKey    AWS Secret Key.
     * @param sessionToken AWS Session Token, if applicable. Otherwise, can be `null` or an empty String (`""`).
     * @param wssUri       Same as URL to sign, excluding query parameters.
     * @param region       AWS region. Example: us-west-2.
     * @param dateMilli    Date at which this request to be signed. Milliseconds since epoch.
     * @return Presigned WebSocket URL you can use to connect to Kinesis Video Signaling.
     * @see [Kinesis Video Streams WebRTC Websocket APIs](https://docs.aws.amazon.com/kinesisvideostreams-webrtc-dg/latest/devguide/kvswebrtc-websocket-apis.html)
     */
    @JvmStatic
    fun sign(
        uri: URI, accessKey: String, secretKey: String,
        sessionToken: String?, wssUri: URI, region: String,
        dateMilli: Long
    ): URI? {
        // Step 1. Create canonical request.
        val amzDate = getTimeStamp(dateMilli)
        val datestamp = getDateStamp(dateMilli)
        val queryParamsMap =
            buildQueryParamsMap(uri, accessKey, sessionToken, region, amzDate, datestamp)
        val canonicalQuerystring = getCanonicalizedQueryString(queryParamsMap)
        val canonicalRequest = getCanonicalRequest(uri, canonicalQuerystring)

        // Step 2. Construct StringToSign.
        val stringToSign =
            signString(amzDate, createCredentialScope(region, datestamp), canonicalRequest)

        // Step 3. Calculate the signature.
        val signatureKey =
            getSignatureKey(secretKey, datestamp, region, SERVICE)
        val signature = BinaryUtils.toHex(hmacSha256(stringToSign, signatureKey))

        // Step 4. Combine steps 1 and 3 to form the final URL.
        val signedCanonicalQueryString =
            canonicalQuerystring + "&" + X_AMZ_SIGNATURE + "=" + signature
        return URI.create(
            wssUri.scheme + "://" + wssUri.host + "/?" + getCanonicalUri(uri).substring(
                1
            ) + signedCanonicalQueryString
        )
    }

    /**
     * Same as [.sign], except the `wssUri`
     * parameter is extracted from the `uri` parameter and not passed in.
     */
    @JvmStatic
    fun sign(
        uri: URI, accessKey: String, secretKey: String,
        sessionToken: String?, region: String, dateMillis: Long
    ): URI? {
        val wssUri = URI.create("wss://" + uri.host)
        return sign(uri, accessKey, secretKey, sessionToken, wssUri, region, dateMillis)
    }

    /**
     * Return a map of all of the query parameters as key-value pairs. The values will be
     * [.urlEncode]'d. Note: The query parameters in this map may not be in
     * sorted order.
     *
     *
     * The query parameters that are included in this map are:
     *
     *  1. {@value AwsV4SignerConstants#X_AMZ_ALGORITHM}
     *  1. {@value AwsV4SignerConstants#X_AMZ_CREDENTIAL}
     *  1. {@value AwsV4SignerConstants#X_AMZ_DATE}
     *  1. {@value AwsV4SignerConstants#X_AMZ_EXPIRES}
     *  1. {@value AwsV4SignerConstants#X_AMZ_SIGNED_HEADERS}
     *  1. {@value AwsV4SignerConstants#X_AMZ_SECURITY_TOKEN}, if the AWS Session Token is specified.
     *  1. And, the query parameters passed through `uri`.
     *
     *
     * @param uri          URL to sign.
     * @param accessKey    AWS Access Key Id.
     * @param sessionToken AWS Session Token. Can be null or an empty string if non-temporary credentials are used.
     * @param region       AWS region. Example: us-west-2.
     * @param amzDate      The result of [.getTimeStamp].
     * @param datestamp    The result of [.getDateStamp].
     * @return Map of the query parameters to be included.
     * @see [Authenticating Requests: Using Query Parameters
    ](https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-query-string-auth.html) */
    @JvmStatic
    fun buildQueryParamsMap(
        uri: URI,
        accessKey: String,
        sessionToken: String?,
        region: String?,
        amzDate: String,
        datestamp: String?
    ): Map<String, String> {
        val queryParamsBuilder = ImmutableMap.builder<String, String>()
            .put(
                X_AMZ_ALGORITHM,
                ALGORITHM_AWS4_HMAC_SHA_256
            )
            .put(
                X_AMZ_CREDENTIAL,
                urlEncode(accessKey + "/" + createCredentialScope(region, datestamp))
            )
            .put(
                X_AMZ_DATE,
                amzDate
            ) // The SigV4 signer has a maximum time limit of five minutes.
            // Once a connection is established, peers exchange signaling messages,
            // and the P2P connection is successful, the media P2P session
            // can continue for longer period of time.
            .put(X_AMZ_EXPIRES, "299")
            .put(X_AMZ_SIGNED_HEADERS, SIGNED_HEADERS)
        if (StringUtils.isNotEmpty(sessionToken)) {
            queryParamsBuilder.put(
                X_AMZ_SECURITY_TOKEN,
                urlEncode(sessionToken)
            )
        }

        // Add the query parameters included in the uri.
        // Note: query parameters follow the format: key1=val1&key2=val2&key3=val3
        if (StringUtils.isNotEmpty(uri.query)) {
            val params = uri.query.split("&".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (param in params) {
                val index = param.indexOf('=')
                if (index > 0) {
                    val paramKey = param.substring(0, index)
                    val paramValue = urlEncode(param.substring(index + 1))
                    queryParamsBuilder.put(paramKey, paramValue)
                }
            }
        }
        return queryParamsBuilder.build()
    }

    @JvmStatic
    fun getCanonicalizedQueryString(queryParamsMap: Map<String, String>): String {
        val queryKeys: List<String> = ArrayList(queryParamsMap.keys)
        Collections.sort(queryKeys)
        val builder = StringBuilder()
        for (i in queryKeys.indices) {
            builder.append(queryKeys[i]).append("=").append(queryParamsMap[queryKeys[i]])
            if (queryKeys.size - 1 > i) {
                builder.append("&")
            }
        }
        return builder.toString()
    }

    /**
     * Create and return the credential scope, which belongs in the X-Amz-Credential query parameter,
     * except for the access-key-id, and the slash that follows.
     *
     *
     * The format of the full scope is as follows:
     * `<access-key-id>/<datestamp>/<region>/kinesisvideo/aws4_request`
     *
     * @param region    AWS Region. For example, us-west-2.
     * @param datestamp The datestamp in "yyyyMMdd" format. For example, "20160801".
     * @return The scope, except for the access-key-id and the slash that follows.
     */
    @JvmStatic
    fun createCredentialScope(region: String?, datestamp: String?): String {
        return StringJoiner("/")
            .add(datestamp)
            .add(region)
            .add(SERVICE)
            .add(AWS4_REQUEST_TYPE)
            .toString()
    }

    /**
     * Constructs and returns the canonical request.
     *
     *
     * An example canonical request looks like the following:
     * <pre>
     * GET
     * /
     * X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-ChannelARN=arn%3Aaws%3Akinesisvideo%3Aus-west-2%3A123456789012%3Achannel%2Fdemo-channel%2F1234567890123&X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20230718%2Fus-west-2%2Fkinesisvideo%2Faws4_request&X-Amz-Date=20230718T191301Z&X-Amz-Expires=299&X-Amz-SignedHeaders=host
     * host:v-1a2b3c4d.kinesisvideo.us-west-2.amazonaws.com
     *
     * host
     * e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
    </pre> *
     *
     *
     * The format of the **canonical request** are as follows:
     *
     *  1. `HTTP method + "\n"` - With presigned URL's, it's always {@value AwsV4SignerConstants#METHOD}.
     *  1. `Canonical URI + "\n"` - Resource. In our case, it's always "/".
     *  1. `Canonical Query String + "\n"` - Sorted list of query parameters (and their values), excluding X-Amz-Signature. In our case: X-Amz-Algorithm, X-Amz-ChannelARN, X-Amz-ClientId (if viewer), X-Amz-Credential, X-Amz-Date, X-Amz-Expires.
     *  1. `Canonical Headers + "\n"` - In our case, we only have the required HTTP `host` header.
     *  1. `Signed Headers + "\n"` - Which headers, from the canonical headers, in alphabetical order.
     *  1. `Hashed Payload` - In our case, it's always the SHA-256 checksum of an empty string.
     *
     *
     * @param uri                  The URL to sign. For example, `wss://v-a1b2c3d4.kinesisvideo.us-west-2.amazonaws.com?
     * X-Amz-ChannelARN=arn:aws:kinesisvideo:us-west-2:123456789012:channel/demo-channel/1234567890123&X-Amz-ClientId=d7d1c6e2-9cb0-4d61-bea9-ecb3d3816557`.
     * @param canonicalQuerystring The Canonical Query String to use in the Canonical Request. Sorted list of query
     * parameters (and their values), excluding X-Amz-Signature, already URL-encoded.
     * @return The fully-constructed canonical request.
     * @see [](https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-query-string-auth.html.w28981aab9c27c27c11.UriEncode%28%29:~:text=or%20trailing%20whitespace.-,UriEncode
    @see <a href=)//docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html.create-canonical-request">Canonical request specification
     */
    @JvmStatic
    fun getCanonicalRequest(uri: URI, canonicalQuerystring: String?): String {
        val payloadHash =
            Hashing.sha256().hashString(StringUtils.EMPTY, StandardCharsets.UTF_8).toString()
        val canonicalUri = getCanonicalUri(uri)
        val canonicalHeaders = "host:" + uri.host + NEW_LINE_DELIMITER
        return StringJoiner(NEW_LINE_DELIMITER)
            .add(METHOD)
            .add(canonicalUri)
            .add(canonicalQuerystring)
            .add(canonicalHeaders)
            .add(SIGNED_HEADERS)
            .add(payloadHash)
            .toString()
    }

    @JvmStatic
    fun getCanonicalUri(uri: URI): String {
        return Optional.of(uri.path)
            .filter { s: String? ->
                !StringUtils.isEmpty(
                    s
                )
            }
            .orElse("/")
    }

    /**
     * Returns the following string. Each line (except the last one) is followed by a newline character.
     * <pre>
     * Algorithm
     * RequestDateTime
     * CredentialScope
     * HashedCanonicalRequest
    </pre> *
     *
     *
     * Here is an explanation of each line.
     *
     *  1. Algorithm - For SHA-256, we use AWS4-HMAC-SHA256.
     *  1. RequestDateTime - Timestamp from [.getTimeStamp].
     *  1. CredentialScope - Scope from [.createCredentialScope].
     *  1. HashedCanonicalRequest - Hash of [.getCanonicalRequest].
     * Since we use SHA-256, this is the SHA-256 digest of the canonical request.
     *
     *
     *
     * An example of a string to sign looks like the following:
     * <pre>
     * AWS4-HMAC-SHA256
     * 20150830T123600Z
     * AKIDEXAMPLE/20150830/us-west-2/kinesisvideo/aws4_request
     * 816cd5b414d056048ba4f7c5386d6e0533120fb1fcfa93762cf0fc39e2cf19e0
    </pre> *
     *
     * @param amzDate          The result of [.getTimeStamp].
     * @param credentialScope  the result of [.createCredentialScope].
     * @param canonicalRequest the result of [.getCanonicalRequest].
     * @return The string to sign.
     * @see [String to sign](https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html.create-string-to-sign)
     */
    @JvmStatic
    fun signString(amzDate: String?, credentialScope: String?, canonicalRequest: String?): String {
        return StringJoiner(NEW_LINE_DELIMITER)
            .add(ALGORITHM_AWS4_HMAC_SHA_256)
            .add(amzDate)
            .add(credentialScope)
            .add(Hashing.sha256().hashString(canonicalRequest!!, StandardCharsets.UTF_8).toString())
            .toString()
    }

    @JvmStatic
    fun urlEncode(str: String?): String {
        return URLEncoder.encode(str, StandardCharsets.UTF_8.name())
    }

    //  https://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
    @JvmStatic
    fun hmacSha256(data: String, key: ByteArray?): ByteArray {
        val algorithm = "HmacSHA256"
        val mac: Mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key, algorithm))
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Calculate and return the signature key. Note: the returned signature must be converted from
     * binary to hexadecimal representation (lowercase letters).
     *
     *
     * The formula is as follows:
     * <pre>
     * kDate = hash("AWS4" + Key, Date)
     * kRegion = hash(kDate, Region)
     * kService = hash(kRegion, ServiceName)
     * kSigning = hash(kService, "aws4_request")
     * ksignature = hash(kSigning, string-to-sign)
    </pre> *
     *
     * @param key         AWS secret access key.
     * @param dateStamp   Date used in the credential scope. Format: yyyyMMdd.
     * @param regionName  AWS region. Example: us-west-2.
     * @param serviceName The name of the service. Should be `kinesisvideo`.
     * @return `ksignature`, as specified above.
     * @see [Calculate signature](https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html.calculate-signature)
     */
    @JvmStatic
    fun getSignatureKey(
        key: String,
        dateStamp: String,
        regionName: String,
        serviceName: String
    ): ByteArray {
        val kSecret = "AWS4$key".toByteArray(StandardCharsets.UTF_8)
        val kDate = hmacSha256(dateStamp, kSecret)
        val kRegion = hmacSha256(regionName, kDate)
        val kService = hmacSha256(serviceName, kRegion)
        return hmacSha256(AWS4_REQUEST_TYPE, kService)
    }

    /**
     * Returns the date and time, formatted to follow the ISO 8601 standard,
     * which is the "yyyyMMddTHHmmssZ" format.
     *
     *
     * For example if the date and time was "08/01/2016 15:32:41.982-700"
     * then it must first be converted to UTC (Coordinated Universal Time)
     * and then submitted as "20160801T223241Z".
     *
     * @param dateMilli The milliseconds since epoch at which this request is to be signed at.
     * Must be the same as the one passed to [.getDateStamp] to avoid
     * signing issues at midnight UTC.
     * @return The date string, formatted to the ISO 8601 standard.
     */
    @JvmStatic
    fun getTimeStamp(dateMilli: Long): String {
        return DateUtils.format(TIME_PATTERN, Date(dateMilli))
    }

    /**
     * Returns the date in yyyyMMdd format.
     *
     *
     * For example if the date and time was "08/01/2016 15:32:41.982-700"
     * then it must first be converted to UTC (Coordinated Universal Time)
     * and "20160801" will be returned.
     *
     * @param dateMilli The milliseconds since epoch at which this request is to be signed at.
     * Must be the same as the one passed to [.getTimeStamp] to avoid
     * signing issues at midnight UTC.
     * @return The date string, without the current time.
     */
    @JvmStatic
    fun getDateStamp(dateMilli: Long): String {
        return DateUtils.format(DATE_PATTERN, Date(dateMilli))
    }
}