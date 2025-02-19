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
package reactor.netty.examples.documentation.http.client.pool.config;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

public class Application {

	public static void main(String[] args) {
		ConnectionProvider provider =
				ConnectionProvider.builder("custom")
				                  .maxConnections(50)
				                  .maxIdleTime(Duration.ofSeconds(20))           //<1>
				                  .maxLifeTime(Duration.ofSeconds(60))           //<2>
				                  .pendingAcquireTimeout(Duration.ofSeconds(60)) //<3>
				                  .evictInBackground(Duration.ofSeconds(120))    //<4>
				                  .build();

		HttpClient client = HttpClient.create(provider);

		String response =
				client.get()
				      .uri("https://example.com/")
				      .responseContent()
				      .aggregate()
				      .asString()
				      .block();

		System.out.println("Response " + response);

		provider.disposeLater()
		        .block();
	}
}