package de.tobias.playwall.server.net.inspector;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The network viewer exposes all message payloads, so it is only reachable from the machine the server runs on.
 */
class LocalhostOnlyFilter extends OncePerRequestFilter
{
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
	{
		if(isLoopback(request.getRemoteAddr()))
		{
			filterChain.doFilter(request, response);
		}
		else
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "The network viewer is only available from localhost");
		}
	}

	static boolean isLoopback(String remoteAddress)
	{
		if(remoteAddress == null)
		{
			return false;
		}
		try
		{
			return InetAddress.getByName(remoteAddress).isLoopbackAddress();
		}
		catch(UnknownHostException e)
		{
			return false;
		}
	}
}
