package de.tobias.playwall.server.net.inspector;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocalhostOnlyFilterTest
{
	private final LocalhostOnlyFilter filter = new LocalhostOnlyFilter();

	@Test
	void loopbackAddressesAreAllowed() throws Exception
	{
		for(String address : new String[]{"127.0.0.1", "0:0:0:0:0:0:0:1", "::1"})
		{
			final FilterChain chain = mock(FilterChain.class);
			final MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRemoteAddr(address);
			final MockHttpServletResponse response = new MockHttpServletResponse();

			filter.doFilter(request, response, chain);

			verify(chain).doFilter(request, response);
			assertEquals(200, response.getStatus());
		}
	}

	@Test
	void remoteAddressesAreRejected() throws Exception
	{
		final FilterChain chain = mock(FilterChain.class);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("192.168.1.20");
		final MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		verify(chain, never()).doFilter(any(), any());
		assertEquals(403, response.getStatus());
	}
}
