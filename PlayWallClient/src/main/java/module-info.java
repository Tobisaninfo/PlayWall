open module de.tobias.playwall.client {
	requires de.thecodelabs.libLogger;
	requires de.thecodelabs.libJfx;
	requires de.thecodelabs.libUtils;
	requires de.thecodelabs.libStorage;
	requires javafx.fxml;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.swing;
	requires org.controlsfx.controls;
	requires java.net.http;
	requires de.tobias.playwall.common;
	requires com.google.gson;

	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jsr310;

	requires org.apache.commons.cli;
	requires io.github.classgraph;

	requires static lombok;
}