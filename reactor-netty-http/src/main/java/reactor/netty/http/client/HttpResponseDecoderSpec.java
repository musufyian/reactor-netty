/*
 * Copyright (c) 2011-Present VMware, Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty.http.client;

import reactor.netty.http.HttpDecoderSpec;
import reactor.netty.http.server.HttpRequestDecoderSpec;

import java.util.Objects;

/**
 * A configuration builder to fine tune the {@link io.netty.handler.codec.http.HttpClientCodec}
 * (or more precisely the {@link io.netty.handler.codec.http.HttpClientCodec.Decoder}).
 * <p>
 * Defaults are accessible as constants {@link #DEFAULT_MAX_INITIAL_LINE_LENGTH}, {@link #DEFAULT_MAX_HEADER_SIZE},
 * {@link #DEFAULT_MAX_CHUNK_SIZE}, {@link #DEFAULT_INITIAL_BUFFER_SIZE}, {@link #DEFAULT_VALIDATE_HEADERS},
 * {@link #DEFAULT_FAIL_ON_MISSING_RESPONSE} and {@link #DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST}.
 *
 * @author Violeta Georgieva
 */
public final class HttpResponseDecoderSpec extends HttpDecoderSpec<HttpResponseDecoderSpec> {

	public static final boolean DEFAULT_FAIL_ON_MISSING_RESPONSE         = false;
	public static final boolean DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST = false;

	/**
	 * The maximum length of the content of the HTTP/2.0 clear-text upgrade request.
	 * By default the client will allow an upgrade request with up to 65536 as
	 * the maximum length of the aggregated content.
	 */
	public static final int DEFAULT_H2C_MAX_CONTENT_LENGTH = 65536;

	boolean failOnMissingResponse        = DEFAULT_FAIL_ON_MISSING_RESPONSE;
	boolean parseHttpAfterConnectRequest = DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST;

	HttpResponseDecoderSpec() {
		this.h2cMaxContentLength = DEFAULT_H2C_MAX_CONTENT_LENGTH;
	}

	@Override
	public HttpResponseDecoderSpec get() {
		return this;
	}

	/**
	 * Configure whether or not to throw an exception on a channel inactive
	 * in case there was a missing response
	 *
	 * @param failOnMissingResponse true - throw an exception on a channel inactive
	 *                              in case there was a missing response, otherwise false
	 * @return this option builder for further configuration
	 */
	public HttpResponseDecoderSpec failOnMissingResponse(boolean failOnMissingResponse) {
		this.failOnMissingResponse = failOnMissingResponse;
		return this;
	}

	/**
	 * Configure whether or not the HTTP decoding will continue even after HTTP CONNECT.
	 *
	 * @param parseHttpAfterConnectRequest true to continue HTTP decoding, otherwise false
	 * @return this option builder for further configuration
	 */
	public HttpResponseDecoderSpec parseHttpAfterConnectRequest(boolean parseHttpAfterConnectRequest) {
		this.parseHttpAfterConnectRequest = parseHttpAfterConnectRequest;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		HttpResponseDecoderSpec that = (HttpResponseDecoderSpec) o;
		return failOnMissingResponse == that.failOnMissingResponse &&
				parseHttpAfterConnectRequest == that.parseHttpAfterConnectRequest;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), failOnMissingResponse, parseHttpAfterConnectRequest);
	}

	/**
	 * Build a {@link HttpRequestDecoderSpec}.
	 */
	HttpResponseDecoderSpec build() {
		HttpResponseDecoderSpec decoder = new HttpResponseDecoderSpec();
		decoder.initialBufferSize = initialBufferSize;
		decoder.maxChunkSize = maxChunkSize;
		decoder.maxHeaderSize = maxHeaderSize;
		decoder.maxInitialLineLength = maxInitialLineLength;
		decoder.validateHeaders = validateHeaders;
		decoder.allowDuplicateContentLengths = allowDuplicateContentLengths;
		decoder.failOnMissingResponse = failOnMissingResponse;
		decoder.parseHttpAfterConnectRequest = parseHttpAfterConnectRequest;
		decoder.h2cMaxContentLength = h2cMaxContentLength;
		return decoder;
	}
}
