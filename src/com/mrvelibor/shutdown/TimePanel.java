package com.mrvelibor.shutdown;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class TimePanel extends ShutdownPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final JTextField mHours, mMinutes, mSeconds;
	
	public TimePanel() {
		super();
		
		mHours = new JTextField(3);
		mHours.setText("0");
		mHours.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {}
			
			@Override
			public void focusGained(FocusEvent e) {
				mHours.selectAll();
			}
		});
		add(mHours);
		add(new JLabel("h "));
		
		mMinutes = new JTextField(2);
		mMinutes.setText("00");
		mMinutes.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {}
			
			@Override
			public void focusGained(FocusEvent e) {
				mMinutes.selectAll();
			}
		});
		add(mMinutes);
		add(new JLabel("m "));
		
		mSeconds = new JTextField(2);
		mSeconds.setText("00");
		mSeconds.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {}
			
			@Override
			public void focusGained(FocusEvent e) {
				mSeconds.selectAll();
			}
		});
		add(mSeconds);
		add(new JLabel("s "));
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		mHours.setEnabled(enabled);
		mMinutes.setEnabled(enabled);
		mSeconds.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	
	@Override
	public Date getShutdownTime() {
		try {
			int sec = Integer.parseInt(mSeconds.getText());
			int min = Integer.parseInt(mMinutes.getText());
			int hr = Integer.parseInt(mHours.getText());
			if(sec < 0 || min < 0 || hr < 0) throw new NumberFormatException("Time must be positive.");
			
			if(sec >= 60) {
				min += sec / 60;
				sec %= 60;
				mSeconds.setText(Integer.toString(sec));
				mMinutes.setText(Integer.toString(min));
			}
			
			if(min >= 60) {
				hr += min / 60;
				min %= 60;
				mMinutes.setText(Integer.toString(min));
				mHours.setText(Integer.toString(hr));
			}
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, sec);
			cal.add(Calendar.MINUTE, min);
			cal.add(Calendar.HOUR, hr);
			
			return cal.getTime();
		} catch(NumberFormatException e) {
			e.printStackTrace();
			mHours.setText("0");
			mMinutes.setText("00");
			mSeconds.setText("00");
			return null;
		}
	}
	
}
