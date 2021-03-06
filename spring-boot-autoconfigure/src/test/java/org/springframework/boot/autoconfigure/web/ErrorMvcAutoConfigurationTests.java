/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.web;

import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ErrorMvcAutoConfiguration}.
 *
 * @author Brian Clozel
 * @author Phillip Webb
 */
public class ErrorMvcAutoConfigurationTests {

	@Rule
	public OutputCapture outputCapture = new OutputCapture();

	@Test
	public void renderContainsViewWithExceptionDetails() throws Exception {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		try {
			context.setServletContext(new MockServletContext());
			context.register(ServerProperties.class, ErrorMvcAutoConfiguration.class);
			context.refresh();
			View errorView = context.getBean("error", View.class);
			ErrorAttributes errorAttributes = context.getBean(ErrorAttributes.class);
			DispatcherServletWebRequest webRequest = createWebRequest(
					new IllegalStateException("Exception message"), false);
			errorView.render(errorAttributes.getErrorAttributes(webRequest, true),
					webRequest.getRequest(), webRequest.getResponse());
			assertThat(((MockHttpServletResponse) webRequest.getResponse())
					.getContentAsString()).contains("<div>Exception message</div>");
		}
		finally {
			context.close();
		}
	}

	private DispatcherServletWebRequest createWebRequest(Exception ex,
			boolean committed) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/path");
		MockHttpServletResponse response = new MockHttpServletResponse();
		DispatcherServletWebRequest webRequest = new DispatcherServletWebRequest(request,
				response);
		webRequest.setAttribute("javax.servlet.error.exception", ex,
				RequestAttributes.SCOPE_REQUEST);
		webRequest.setAttribute("javax.servlet.error.request_uri", "/path",
				RequestAttributes.SCOPE_REQUEST);
		response.setCommitted(committed);
		response.setOutputStreamAccessAllowed(!committed);
		response.setWriterAccessAllowed(!committed);
		return webRequest;
	}

}
