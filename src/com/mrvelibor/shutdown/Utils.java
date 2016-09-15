package com.mrvelibor.shutdown;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Utils {
	
	private Utils() {}
	
	public static BufferedImage resizeImage(Image image, int width, int height, boolean alpha) {
		BufferedImage resized = new BufferedImage(width, height, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g = resized.createGraphics();
		if(alpha) g.setComposite(AlphaComposite.Src);
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		
		return resized;
	}
	
	public static Time getTime(long milis) {
		return new Time(milis);
	}
	
	public static class Time {
		
		public final int h, m, s;
		
		private Time(long milis) {
			int sec = (int) (milis / 1000);
			int min = sec / 60;
			sec %= 60;
			int hr = min / 60;
			min %= 60;
			
			h = hr;
			m = min;
			s = sec;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			if(h > 0 || m > 5) {
				if(h < 10) sb.append('0');
				sb.append(h).append(':');
				if(m < 10) sb.append('0');
				sb.append(m);
				sb.append(" hours");
			}
			else {
				if(m < 10) sb.append('0');
				sb.append(m).append(':');
				if(s < 10) sb.append('0');
				sb.append(s);
				sb.append(" minutes");
			}
			
			return sb.toString();
		}
		
	}
	
}
