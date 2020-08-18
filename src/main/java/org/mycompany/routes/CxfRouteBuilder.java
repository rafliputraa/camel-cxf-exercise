package org.mycompany.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.spi.RestConfiguration;
import org.springframework.stereotype.Component;
import org.tempuri.Add;
import org.tempuri.AddResponse;

@Component
public class CxfRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		restConfiguration().contextPath("/calculator").host("{{server.address}}").port("{{conf.port}}")
				.component("jetty");

		rest().id("calculate")
		.post("add").id("post-calculate-add").consumes("application/json").to("direct:calculateAdd");

		from("direct:calculateAdd")
		.id("route-calculate-add")
		.unmarshal().json(JsonLibrary.Jackson, Add.class)
		.log("This is a log of Body Initial: ${body.intA}, ${body.intB}")
		.bean(Add.class, "setValue(${body.intA}, ${body.intB})")
		.log("This is a log of params: ${body}")
		.setHeader("operationName", constant("Add"))
		.to("cxf:bean:cxfSoapServiceEndpoint")
		.convertBodyTo(String.class)
		.log("This is a log of Body Result: ${body}");
	}

}
