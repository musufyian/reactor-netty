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

package reactor.netty.http.server;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * @author Stephane Maldini
 */
final class DefaultHttpServerRoutes implements HttpServerRoutes {

	private final CopyOnWriteArrayList<HttpRouteHandler> handlers =
			new CopyOnWriteArrayList<>();

	private final List<HttpRouteHandler> initialOrderHandlers = new ArrayList<>();

	private Comparator<HttpRouteHandlerMetadata> comparator;

	@Override
	public HttpServerRoutes directory(String uri, Path directory,
			@Nullable Function<HttpServerResponse, HttpServerResponse> interceptor) {
		Objects.requireNonNull(directory, "directory");
		return route(HttpPredicate.prefix(uri), (req, resp) -> {

			String prefix = URI.create(req.uri())
			                   .getPath()
			                   .replaceFirst(uri, "");

			if (!prefix.isEmpty() && prefix.charAt(0) == '/') {
				prefix = prefix.substring(1);
			}

			Path p = directory.resolve(prefix);
			if (Files.isReadable(p)) {

				if (interceptor != null) {
					return interceptor.apply(resp)
					                  .sendFile(p);
				}
				return resp.sendFile(p);
			}

			return resp.sendNotFound();
		});
	}

	@Override
	public HttpServerRoutes route(Predicate<? super HttpServerRequest> condition,
			BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler) {
		Objects.requireNonNull(condition, "condition");
		Objects.requireNonNull(handler, "handler");

		if (condition instanceof HttpPredicate) {
			HttpRouteHandler httpRouteHandler = new HttpRouteHandler(condition,
					handler,
					(HttpPredicate) condition, ((HttpPredicate) condition).uri);

			handlers.add(httpRouteHandler);
			initialOrderHandlers.add(httpRouteHandler);

		}
		else {
			HttpRouteHandler httpRouteHandler = new HttpRouteHandler(condition, handler, null, null);
			handlers.add(httpRouteHandler);
			initialOrderHandlers.add(httpRouteHandler);
		}

		if (this.comparator != null) {
			handlers.sort(this.comparator);
		}

		return this;
	}

	@Override
	public HttpServerRoutes comparator(Comparator<HttpRouteHandlerMetadata> comparator) {
		Objects.requireNonNull(comparator, "comparator");
		this.comparator = comparator;
		handlers.sort(comparator);
		return this;
	}

	@Override
	public HttpServerRoutes noComparator() {
		handlers.clear();
		handlers.addAll(initialOrderHandlers);
		return this;
	}

	@Override
	public Publisher<Void> apply(HttpServerRequest request, HttpServerResponse response) {
		final Iterator<HttpRouteHandler> iterator = handlers.iterator();
		HttpRouteHandler cursor;

		try {
			while (iterator.hasNext()) {
				cursor = iterator.next();
				if (cursor.test(request)) {
					return cursor.apply(request, response);
				}
			}
		}
		catch (Throwable t) {
			Exceptions.throwIfJvmFatal(t);
			return Mono.error(t); //500
		}

		return response.sendNotFound();
	}

	static final class HttpRouteHandler
			implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>>,
			Predicate<HttpServerRequest>, HttpRouteHandlerMetadata {

		final Predicate<? super HttpServerRequest> condition;
		final BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>>
				handler;
		final Function<? super String, Map<String, String>> resolver;

		final String path;

		HttpRouteHandler(Predicate<? super HttpServerRequest> condition,
				BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler,
				@Nullable Function<? super String, Map<String, String>> resolver,
				@Nullable String path) {
			this.condition = Objects.requireNonNull(condition, "condition");
			this.handler = Objects.requireNonNull(handler, "handler");
			this.resolver = resolver;
			this.path = path;
		}

		@Override
		public Publisher<Void> apply(HttpServerRequest request,
				HttpServerResponse response) {
			return handler.apply(request.paramsResolver(resolver), response);
		}

		@Override
		public boolean test(HttpServerRequest o) {
			return condition.test(o);
		}

		@Override
		public String getPath() {
			return path;
		}
	}
}
