package de.tobias.playwall.common.api.settings.audiodevices;

public record AudioDeviceInstance(String name, String driver, boolean isDefault, boolean isError)
{
}
