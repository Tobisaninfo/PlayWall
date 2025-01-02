package de.tobias.playwall.server.common.audio;

public interface SpeedAdjustable {
	double currentRate();

	void setCurrentRate(double rate);
}
