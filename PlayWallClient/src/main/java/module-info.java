open module de.tobias.playwall.client {
	requires de.thecodelabs.libJfx;
	requires de.thecodelabs.libUtils;
	requires de.thecodelabs.libStorage;
	requires javafx.base;
	requires javafx.fxml;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.swing;
	requires org.controlsfx.controls;
	requires java.net.http;
	requires de.tobias.playwall.common;
	requires com.google.gson;

	requires tools.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires tools.jackson.databind;

	requires org.apache.commons.cli;
	requires io.github.classgraph;

	requires org.slf4j;
	requires org.apache.logging.log4j.slf4j2.impl;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires java.xml;

	requires static lombok;

	requires org.scenicview.scenicview;
}