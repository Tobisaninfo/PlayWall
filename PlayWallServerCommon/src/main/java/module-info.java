open module de.tobias.playwall.server.common {
	requires de.tobias.playwall.common;
	requires static lombok;
	requires de.thecodelabs.libUtils;
	requires com.fasterxml.jackson.annotation;
	exports de.tobias.playwall.server.common.audio;
}