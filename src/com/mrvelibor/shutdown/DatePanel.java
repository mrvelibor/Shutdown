package com.mrvelibor.shutdown;

import java.util.Calendar;
import java.util.Date;

import javax.swing.JComboBox;

public class DatePanel extends ShutdownPanel {
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] HOURS = new String[24];
	private static final String[] MINUTES = new String[60];
	private static final String[] DAYS = new String[2];
	static {
		for(int i = 0; i < HOURS.length; ++i) {
			HOURS[i] = Integer.toString(i);
			if(i < 10) HOURS[i] = 0 + HOURS[i];
		}
		for(int i = 0; i < MINUTES.length; ++i) {
			MINUTES[i] = Integer.toString(i);
			if(i < 10) MINUTES[i] = 0 + MINUTES[i];
		}
		DAYS[0] = "today";
		DAYS[1] = "tomorrow";
	}
	
	private final JComboBox<String> mHours, mMinutes, mDay;
	
	public DatePanel() {
		super();
		
		mHours = new JComboBox<>(HOURS);
		mHours.setSelectedIndex(0);
		add(mHours);
		
		mMinutes = new JComboBox<>(MINUTES);
		mMinutes.setSelectedIndex(0);
		add(mMinutes);
		
		mDay = new JComboBox<>(DAYS);
		mDay.setSelectedIndex(0);
		add(mDay);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		mHours.setEnabled(enabled);
		mMinutes.setEnabled(enabled);
		mDay.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	
	@Override
	public Date getShutdownTime() {
		Calendar cal = Calendar.getInstance();
		
		int day = mDay.getSelectedIndex(), hr = mHours.getSelectedIndex(), min = mMinutes.getSelectedIndex();
		cal.add(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hr);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);
		
		return cal.getTime();
	}
	
}
