/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.restlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * This unit test verifies a single route can service multiple templates.
 * 
 * @version $Revision$
 */
public class RestletMultiUriTemplatesEndpointTest extends ContextTestSupport {

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("/users/{username}");
        list.add("/atom/collection/{id}/component/{cid}");

        JndiRegistry jndi = super.createRegistry();
        jndi.bind("uriTemplates", list);
        return jndi;
    }

    public void testPostUserUriPattern() throws Exception {
        HttpMethod method = new PostMethod("http://localhost:9080/users/homer");
        try {
            HttpClient client = new HttpClient();
            assertEquals(200, client.executeMethod(method));
            assertTrue(method.getResponseHeader("Content-Type").getValue().startsWith("text/plain"));
            assertEquals("POST homer", method.getResponseBodyAsString());
        } finally {
            method.releaseConnection();
        }

    }

    public void testGetAtomUriPattern() throws Exception {
        HttpMethod method = new GetMethod("http://localhost:9080/atom/collection/foo/component/bar");
        try {
            HttpClient client = new HttpClient();
            assertEquals(200, client.executeMethod(method));
            assertTrue(method.getResponseHeader("Content-Type").getValue().startsWith("text/plain"));
            assertEquals("GET foo bar", method.getResponseBodyAsString());
        } finally {
            method.releaseConnection();
        }

    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // START SNIPPET: routeDefinition
                from("restlet:http://localhost:9080?restletMethods=post,get&restletUriPatterns=#uriTemplates")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            // echo the method
                            String uri = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
                            String out = exchange.getIn().getHeader(Exchange.HTTP_METHOD, String.class);
                            if ("http://localhost:9080/users/homer".equals(uri)) {
                                exchange.getOut().setBody(out + " " + exchange.getIn().getHeader("username", String.class));
                            } else if ("http://localhost:9080/atom/collection/foo/component/bar".equals(uri)) {
                                exchange.getOut().setBody(out + " " + exchange.getIn().getHeader("id", String.class)
                                                          + " " + exchange.getIn().getHeader("cid", String.class));

                            }

                        }
                    });
                // END SNIPPET: routeDefinition

            }
        };
    }

}
