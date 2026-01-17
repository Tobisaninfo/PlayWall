open module de.tobias.playwall.common {
	exports de.tobias.playwall.common.net;
	exports de.tobias.playwall.common.utils;
	exports de.tobias.playwall.common.api;
	exports de.tobias.playwall.common.api.history;
	exports de.tobias.playwall.common.api.project;
	exports de.tobias.playwall.common.api.project.model;
	exports de.tobias.playwall.common.api.pad.update;
	exports de.tobias.playwall.common.api.pad.request;
	exports de.tobias.playwall.common.api.page.request;
	exports de.tobias.playwall.common.api.project.request;
	exports de.tobias.playwall.common.api.project.update;

	requires com.fasterxml.jackson.annotation;
	requires de.thecodelabs.libUtils;
	requires static lombok;
}