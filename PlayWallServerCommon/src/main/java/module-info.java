open module de.tobias.playwall.server.common {
	requires de.tobias.playwall.common;
	requires static lombok;
	requires de.thecodelabs.libUtils;
	requires com.fasterxml.jackson.annotation;
	requires spring.context;
	requires org.slf4j;
	exports de.tobias.playwall.server.common.audio;
	exports de.tobias.playwall.server.common.project;
}