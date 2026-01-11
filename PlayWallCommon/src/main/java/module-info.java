open module de.tobias.playwall.common {
	exports de.tobias.playwall.common.net;
	exports de.tobias.playwall.common.utils;
	exports de.tobias.playwall.common.api;
	exports de.tobias.playwall.common.api.history;
	exports de.tobias.playwall.common.api.project;
	exports de.tobias.playwall.common.api.project.model;

	requires com.fasterxml.jackson.annotation;
	requires de.thecodelabs.libUtils;
	requires static lombok;
}