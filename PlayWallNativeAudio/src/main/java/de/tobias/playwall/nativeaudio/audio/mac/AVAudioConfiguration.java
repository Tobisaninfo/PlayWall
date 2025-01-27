package de.tobias.playwall.nativeaudio.audio.mac;

import de.tobias.playwall.nativeaudio.audio.mac.delegate.AVAudioPlayerBridgeDelegate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AVAudioConfiguration
{
	@Bean
	AVAudioBridgeHolder holder() {
		return new AVAudioBridgeHolder();
	}

	@Bean
	AVAudioPlayerBridgeDelegate delegate(AVAudioBridgeHolder holder)
	{
		return new AVAudioPlayerBridgeDelegate(holder);
	}
}
