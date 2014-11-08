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
package org.jclouds.googlecomputeengine.features;

import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.COMPUTE_READONLY_SCOPE;
import static org.jclouds.googlecomputeengine.GoogleComputeEngineConstants.COMPUTE_SCOPE;
import static org.jclouds.googlecomputeengine.options.ListOptions.Builder.filter;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.domain.ListPage;
import org.jclouds.googlecomputeengine.domain.Operation;
import org.jclouds.googlecomputeengine.internal.BaseGoogleComputeEngineExpectTest;
import org.jclouds.googlecomputeengine.parse.ParseGlobalOperationListTest;
import org.jclouds.googlecomputeengine.parse.ParseGlobalOperationTest;
import org.jclouds.googlecomputeengine.parse.ParseRegionOperationTest;
import org.jclouds.googlecomputeengine.parse.ParseZoneOperationTest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

@Test(groups = "unit", testName = "OperationApiExpectTest")
public class OperationApiExpectTest extends BaseGoogleComputeEngineExpectTest<GoogleComputeEngineApi> {

   private static final String OPERATIONS_URL_PREFIX = BASE_URL + "/party/global/operations";
   private static final String REGION_OPERATIONS_URL_PREFIX = BASE_URL + "/party/regions/us-central1/operations";
   private static final String ZONE_OPERATIONS_URL_PREFIX = BASE_URL + "/party/zones/us-central1-a/operations";

   public static final HttpRequest GET_GLOBAL_OPERATION_REQUEST = HttpRequest
           .builder()
           .method("GET")
           .endpoint(OPERATIONS_URL_PREFIX + "/operation-1354084865060")
           .addHeader("Accept", "application/json")
           .addHeader("Authorization", "Bearer " + TOKEN).build();

   public static final HttpResponse GET_GLOBAL_OPERATION_RESPONSE = HttpResponse.builder().statusCode(200)
           .payload(staticPayloadFromResource("/global_operation.json")).build();

   public void get() throws Exception {

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, GET_GLOBAL_OPERATION_REQUEST, GET_GLOBAL_OPERATION_RESPONSE).getOperationApi("party");

      assertEquals(api.get(GET_GLOBAL_OPERATION_REQUEST.getEndpoint()),
              new ParseGlobalOperationTest().expected());
   }

   public void getResponseIs4xx() throws Exception {

      HttpResponse operationResponse = HttpResponse.builder().statusCode(404).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, GET_GLOBAL_OPERATION_REQUEST, operationResponse).getOperationApi("party");

      assertNull(api.get(GET_GLOBAL_OPERATION_REQUEST.getEndpoint()));
   }

   public void delete() throws Exception {
      HttpRequest delete = HttpRequest
              .builder()
              .method("DELETE")
              .endpoint(OPERATIONS_URL_PREFIX + "/operation-1352178598164-4cdcc9d031510-4aa46279")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(204).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_SCOPE),
              TOKEN_RESPONSE, delete, operationResponse).getOperationApi("party");

      api.delete(delete.getEndpoint());
   }

   public void deleteResponseIs4xx() throws Exception {
      HttpRequest delete = HttpRequest
              .builder()
              .method("DELETE")
              .endpoint(OPERATIONS_URL_PREFIX + "/operation-1352178598164-4cdcc9d031510-4aa46279")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(404).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_SCOPE),
              TOKEN_RESPONSE, delete, operationResponse).getOperationApi("party");

      api.delete(delete.getEndpoint());
   }

   public void list() {
      HttpRequest get = HttpRequest
              .builder()
              .method("GET")
              .endpoint(OPERATIONS_URL_PREFIX)
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(200)
              .payload(payloadFromResource("/global_operation_list.json")).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertEquals(api.list().next().toString(),
              new ParseGlobalOperationListTest().expected().toString());
   }

   public void listResponseIs4xx() {
      HttpRequest get = HttpRequest
            .builder()
            .method("GET")
            .endpoint(OPERATIONS_URL_PREFIX)
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(404).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
            TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertFalse(api.list().hasNext());
   }

   public void listWithOptions() {
      HttpRequest get = HttpRequest
              .builder()
              .method("GET")
              .endpoint(OPERATIONS_URL_PREFIX +
                      "?pageToken=CglPUEVSQVRJT04SOzU5MDQyMTQ4Nzg1Mi5vcG" +
                      "VyYXRpb24tMTM1MjI0NDI1ODAzMC00Y2RkYmU2YTJkNmIwLWVkMzIyMzQz&" +
                      "filter=" +
                      "status%20eq%20done&" +
                      "maxResults=3")
              .addHeader("Accept", "application/json")
              .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(200)
              .payload(payloadFromResource("/global_operation_list.json")).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
              TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertEquals(api.listPage("CglPUEVSQVRJT04SOzU5MDQyMTQ4Nzg1Mi5vcGVyYXRpb24tMTM1Mj" +
              "I0NDI1ODAzMC00Y2RkYmU2YTJkNmIwLWVkMzIyMzQz",
              filter("status eq done").maxResults(3)).toString(),
              new ParseGlobalOperationListTest().expected().toString());
   }

   private ListPage<Operation> regionList() {
      return ListPage.create( //
            ImmutableList.of(new ParseRegionOperationTest().expected()), // items
            null // nextPageToken
      );
   }

   public void listInRegion() {
      HttpRequest get = HttpRequest
            .builder()
            .method("GET")
            .endpoint(REGION_OPERATIONS_URL_PREFIX)
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(200)
            .payload(payloadFromResource("/region_operation_list.json")).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
            TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertEquals(api.listInRegion("us-central1").next().toString(), regionList().toString());
   }

   public void listInRegionResponseIs4xx() {
      HttpRequest get = HttpRequest
            .builder()
            .method("GET")
            .endpoint(REGION_OPERATIONS_URL_PREFIX)
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(404).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
            TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertFalse(api.listInRegion("us-central1").hasNext());
   }

   public void listPageInRegion() {
      HttpRequest get = HttpRequest
            .builder()
            .method("GET")
            .endpoint(REGION_OPERATIONS_URL_PREFIX +
                  "?pageToken=CglPUEVSQVRJT04SOzU5MDQyMTQ4Nzg1Mi5vcG" +
                  "VyYXRpb24tMTM1MjI0NDI1ODAzMC00Y2RkYmU2YTJkNmIwLWVkMzIyMzQz&" +
                  "filter=" +
                  "status%20eq%20done&" +
                  "maxResults=3")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(200)
            .payload(payloadFromResource("/region_operation_list.json")).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
            TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertEquals(api.listPageInRegion("us-central1",
                  "CglPUEVSQVRJT04SOzU5MDQyMTQ4Nzg1Mi5vcGVyYXRpb24tMTM1MjI0NDI1ODAzMC00Y2RkYmU2YTJkNmIwLWVkMzIyMzQz",
                  filter("status eq done").maxResults(3)).toString(), regionList().toString());
   }


   private ListPage<Operation> zoneList() {
      return ListPage.create( //
            ImmutableList.of(new ParseZoneOperationTest().expected()), // items
            null // nextPageToken
      );
   }

   public void listInZone() {
      HttpRequest get = HttpRequest
            .builder()
            .method("GET")
            .endpoint(ZONE_OPERATIONS_URL_PREFIX)
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(200)
            .payload(payloadFromResource("/zone_operation_list.json")).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
            TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertEquals(api.listInZone("us-central1-a").next().toString(), zoneList().toString());
   }

   public void listInZoneResponseIs4xx() {
      HttpRequest get = HttpRequest
            .builder()
            .method("GET")
            .endpoint(ZONE_OPERATIONS_URL_PREFIX)
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(404).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
            TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertFalse(api.listInZone("us-central1-a").hasNext());
   }

   public void listPageInZone() {
      HttpRequest get = HttpRequest
            .builder()
            .method("GET")
            .endpoint(ZONE_OPERATIONS_URL_PREFIX +
                  "?pageToken=CglPUEVSQVRJT04SOzU5MDQyMTQ4Nzg1Mi5vcG" +
                  "VyYXRpb24tMTM1MjI0NDI1ODAzMC00Y2RkYmU2YTJkNmIwLWVkMzIyMzQz&" +
                  "filter=" +
                  "status%20eq%20done&" +
                  "maxResults=3")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse operationResponse = HttpResponse.builder().statusCode(200)
            .payload(payloadFromResource("/zone_operation_list.json")).build();

      OperationApi api = requestsSendResponses(requestForScopes(COMPUTE_READONLY_SCOPE),
            TOKEN_RESPONSE, get, operationResponse).getOperationApi("party");

      assertEquals(api.listPageInZone("us-central1-a",
            "CglPUEVSQVRJT04SOzU5MDQyMTQ4Nzg1Mi5vcGVyYXRpb24tMTM1MjI0NDI1ODAzMC00Y2RkYmU2YTJkNmIwLWVkMzIyMzQz",
            filter("status eq done").maxResults(3)).toString(), zoneList().toString());
   }
}