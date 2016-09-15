package com.mrvelibor.shutdown;

import java.util.Date;

import javax.swing.JPanel;

public abstract class ShutdownPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	public abstract Date getShutdownTime();
	
}
