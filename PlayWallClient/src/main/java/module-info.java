open module de.tobias.playwall.client {
	requires de.thecodelabs.libLogger;
	requires de.thecodelabs.libJfx;
	requires de.thecodelabs.libUtils;
	requires javafx.fxml;
	requires javafx.controls;
	requires java.net.http;
	requires de.tobias.playwall.common;
	requires com.google.gson;

	exports de.tobias.playwall.client.project;
}