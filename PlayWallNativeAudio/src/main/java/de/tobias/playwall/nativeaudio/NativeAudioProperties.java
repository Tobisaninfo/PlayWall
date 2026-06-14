package de.tobias.playwall.nativeaudio;

import de.tobias.playwall.nativeaudio.audio.rust.RustLogLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "playwall.native-audio")
public class NativeAudioProperties
{
	private RustLogLevel logLevel = RustLogLevel.INFO;
}
