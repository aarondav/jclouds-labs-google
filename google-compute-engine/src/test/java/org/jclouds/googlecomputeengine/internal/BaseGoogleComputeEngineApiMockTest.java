/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.googlecomputeengine.internal;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.GCE_IMAGE_PROJECTS;
import static org.jclouds.oauth.v2.OAuthConstants.NO_ALGORITHM;
import static org.jclouds.oauth.v2.config.CredentialType.BEARER_TOKEN_CREDENTIALS;
import static org.jclouds.oauth.v2.config.OAuthProperties.CREDENTIAL_TYPE;
import static org.jclouds.oauth.v2.config.OAuthProperties.SIGNATURE_OR_MAC_ALGORITHM;
import static org.jclouds.util.Strings2.toStringAndClose;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.GoogleComputeEngineProviderMetadata;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParser;
import com.google.inject.Module;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

/**
 * Tests need to run {@code singleThreaded = true) as otherwise tests will clash on the server field.
 * Sharing the server field means less code to write.
 */
public class BaseGoogleComputeEngineApiMockTest {

   protected final String identity = "party";
   protected final String credential = "1/8xbJqaOZXSUZbHLl5EOtu1pxz3fmmetKx9W8CV4t79M";
   protected final String openSshKey = GoogleComputeEngineTestModule.INSTANCE.openSshKey;

   protected MockWebServer server;

   protected GoogleComputeEngineApi api() {
      return builder().buildApi(GoogleComputeEngineApi.class);
   }

   protected ComputeService computeService() {
      return builder().buildView(ComputeServiceContext.class).getComputeService();
   }

   private ContextBuilder builder() {
      Properties overrides = new Properties();
      overrides.put(GCE_IMAGE_PROJECTS, "debian-cloud");
      overrides.put(CREDENTIAL_TYPE, BEARER_TOKEN_CREDENTIALS.toString());
      overrides.put(SIGNATURE_OR_MAC_ALGORITHM, NO_ALGORITHM); // TODO: this should be implied by the above.
      return ContextBuilder.newBuilder(new GoogleComputeEngineProviderMetadata())
            .credentials(identity, credential)
            .endpoint(url("/"))
            .overrides(overrides)
            .modules(modules);
   }

   private final Set<Module> modules = ImmutableSet
         .of(new ExecutorServiceModule(sameThreadExecutor()), GoogleComputeEngineTestModule.INSTANCE);

   final AtomicInteger suffix = new AtomicInteger();

   @BeforeMethod
   public void start() throws IOException {
      suffix.set(0);
      server = new MockWebServer();
      server.play();
   }

   protected String url(String path) {
      return "http://localhost:" + server.getPort() + path;
   }

   @AfterMethod(alwaysRun = true)
   public void stop() throws IOException {
      server.shutdown();
   }

   protected MockResponse jsonResponse(String resource) {
      return new MockResponse().addHeader("Content-Type", "application/json").setBody(stringFromResource(resource));
   }

   protected String stringFromResource(String resourceName) {
      try {
         return toStringAndClose(getClass().getResourceAsStream(resourceName))
               .replace("https://www.googleapis.com/compute/v1/", url("/"));
      } catch (IOException e) {
         throw propagate(e);
      }
   }

   protected MockResponse singleRegionSingleZoneResponse() {
      return new MockResponse().setBody("{\"items\":[" + stringFromResource("/region_get.json")
            .replace("\"" + url("/") + "projects/party/zones/us-central1-b\"", "") + "]}");
   }

   protected RecordedRequest assertSent(MockWebServer server, String method, String path) throws InterruptedException {
      RecordedRequest request = server.takeRequest();
      assertEquals(request.getMethod(), method);
      assertEquals(request.getPath(), path);
      assertEquals(request.getHeader("Accept"), APPLICATION_JSON);
      assertEquals(request.getHeader("Authorization"), "Bearer " + credential);
      return request;
   }

   protected RecordedRequest assertSent(MockWebServer server, String method, String path, String json)
         throws InterruptedException {
      RecordedRequest request = assertSent(server, method, path);
      assertEquals(request.getHeader("Content-Type"), APPLICATION_JSON);
      assertEquals(parser.parse(new String(request.getBody(), UTF_8)), parser.parse(json));
      return request;
   }

   /** So that we can ignore formatting. */
   private final JsonParser parser = new JsonParser();
}