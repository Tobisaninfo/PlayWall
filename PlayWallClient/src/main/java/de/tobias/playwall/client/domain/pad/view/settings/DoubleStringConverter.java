package de.tobias.playwall.client.domain.pad.view.settings;

import javafx.util.StringConverter;

import java.text.DecimalFormat;

class DoubleStringConverter extends StringConverter<Double>
{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

	@Override
	public String toString(Double value)
	{
		if(value == null)
		{
			return DECIMAL_FORMAT.format(0);
		}
		return DECIMAL_FORMAT.format(value);
	}

	@Override
	public Double fromString(String text)
	{
		try
		{
			if(text == null || text.isEmpty())
			{
				return 0.0;
			}
			return Double.valueOf(text.replace(",", "."));
		}
		catch(NumberFormatException _)
		{
			return 0.0;
		}
	}
}
