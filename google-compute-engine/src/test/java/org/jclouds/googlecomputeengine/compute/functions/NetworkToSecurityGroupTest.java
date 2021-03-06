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
package org.jclouds.googlecomputeengine.compute.functions;

import static com.google.common.collect.Iterators.singletonIterator;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.jclouds.googlecomputeengine.compute.functions.FirewallToIpPermissionTest.hasProtocol;
import static org.jclouds.googlecomputeengine.compute.functions.FirewallToIpPermissionTest.hasStartAndEndPort;
import static org.jclouds.googlecomputeengine.options.ListOptions.Builder.filter;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import org.jclouds.compute.domain.SecurityGroup;
import org.jclouds.date.internal.SimpleDateFormatDateService;
import org.jclouds.googlecloud.domain.ForwardingListPage;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.domain.Network;
import org.jclouds.googlecomputeengine.features.FirewallApi;
import org.jclouds.googlecomputeengine.options.ListOptions;
import org.jclouds.net.domain.IpProtocol;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class NetworkToSecurityGroupTest {

   @Test
   public void testApply() {
      FirewallToIpPermission fwToPerm = new FirewallToIpPermission();

      GoogleComputeEngineApi api = createMock(GoogleComputeEngineApi.class);
      FirewallApi fwApi = createMock(FirewallApi.class);

      ListOptions options = filter("network eq .*/party-test");
      expect(api.firewalls()).andReturn(fwApi);
      expect(fwApi.list(options)).andReturn(
            singletonIterator(ForwardingListPage.create(ImmutableList.of(FirewallToIpPermissionTest.fwForTest()), null)));

      replay(api, fwApi);

      Network network = Network.create( //
            "abcd", // id
            new SimpleDateFormatDateService().iso8601DateParse("2014-07-18T09:47:30.826-07:00"), // creationTimestamp
            URI.create("https://www.googleapis.com/compute/v1/projects/party/global/networks/party-test"),
            "party-test", // name
            "some description", // description
            "0.0.0.0/0", // rangeIPv4
            "1.2.3.4" // gatewayIPv4
      );

      NetworkToSecurityGroup netToSg = new NetworkToSecurityGroup(fwToPerm, api);

      SecurityGroup group = netToSg.apply(network);

      assertEquals(group.getId(), "party-test");
      assertEquals(group.getUri(), URI.create("https://www.googleapis.com/compute/v1/projects/party/global/networks/party-test"));
      assertEquals(group.getIpPermissions().size(), 3);
      assertTrue(Iterables.any(group.getIpPermissions(), Predicates.and(hasProtocol(IpProtocol.TCP),
              hasStartAndEndPort(1, 10))), "No permission found for TCP, ports 1-10");
      assertTrue(Iterables.any(group.getIpPermissions(), Predicates.and(hasProtocol(IpProtocol.TCP),
              hasStartAndEndPort(33, 33))), "No permission found for TCP, port 33");
      assertTrue(Iterables.any(group.getIpPermissions(), hasProtocol(IpProtocol.ICMP)),
              "No permission found for ICMP");
   }
}
