package de.tobias.playwall.server.net.inspector;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/network-log")
@RequiredArgsConstructor
class NetworkLogController
{
	private final NetworkLogService networkLogService;

	@GetMapping
	List<NetworkLogEntry> getEntries(@RequestParam(required = false) String client,
									 @RequestParam(required = false) NetworkLogEntry.Kind kind,
									 @RequestParam(defaultValue = "0") long afterId)
	{
		return networkLogService.query(client, kind, afterId);
	}

	@GetMapping("/clients")
	List<String> getClients()
	{
		return networkLogService.getClients();
	}

	@DeleteMapping
	void clear()
	{
		networkLogService.clear();
	}
}
