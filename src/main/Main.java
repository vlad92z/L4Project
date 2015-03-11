package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import demodulation.BufferHolder;
import demodulation.Demodulator;
import demodulation.LinkedBuffer;
import demodulation.StaticValues;
import modulation.Modulator;
import modulation.StdAudio;

public class Main {

	public static void main(String[] args) {

		try {
			Demodulator demodulator = new Demodulator();
			BufferHolder holder = demodulator.getHolder();
			
			int frequency = 1200;
			double modIndex = 2.3;
			
			Modulator writer = new Modulator(20, frequency);		
			String myString = "mr12";
			
//			float[] message = writer.modulateAmplitude(myString, 0.0, StaticValues.ENCODING_UTF8_VAL);
			float[] message = writer.modulateFrequency(myString, modIndex, StaticValues.ENCODING_UTF8_VAL);
			highpass(message, frequency, modIndex);
//			for (float v : message) {
//				System.out.println(v);
//			}			
			if (true) return;
			
			int index = 0;
			float[] currentBuffer = new float[1024];
			for (int i = 0; i < 50; i++) {
				if (index == 1024) {
					index = 0;
					holder.addBuffer(new LinkedBuffer(currentBuffer));
					currentBuffer = new float[1024];
				}
				currentBuffer[index] = 0;
				index++;

			}
			for (float temp : message) {
				if (index == 1024) {
					index = 0;
					holder.addBuffer(new LinkedBuffer(currentBuffer));
					currentBuffer = new float[1024];
				}
				currentBuffer[index] = temp;
				index++;

			}
			if (index != 0) {
				for (int i = index; i < 1024; i++) {
					currentBuffer[index] = 0;
				}
				holder.addBuffer(new LinkedBuffer(currentBuffer));
			}
			demodulator.startDemodulation();
			
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
//		/**
//		 * SCAN FILE
//		 */
//		Demodulator demodulator = new Demodulator(1024, 22050, 20, 7080);
//		BufferHolder holder = demodulator.getHolder();
//
//		Scanner scan;
//		File file = new File("input.txt");
//		try {
//			scan = new Scanner(file);
//
//			float[] currentBuffer = new float[1024];
//			int index = 0;
//			while (scan.hasNextFloat()) {
//				if (index == 1024) {
//					index = 0;
//					holder.addBuffer(new LinkedBuffer(currentBuffer));
//					currentBuffer = new float[1024];
//				}
//				currentBuffer[index] = scan.nextFloat();
//				index++;
//
//			}
//			if (index != 0) {
//				for (int i = index; i < 1024; i++) {
//					currentBuffer[index] = 0;
//				}
//				holder.addBuffer(new LinkedBuffer(currentBuffer));
//			}
//
//			scan.close();
//			demodulator.startDemodulation();
//
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}

		try {
			// byte ptext[] = myString.getBytes("UTF-8");
			// float data[] = writer.modulateAmplitude(ptext, 0.2);
			// //StdAudio.save("frequency.wav", data);
			// double[] arr = StdAudio.read("frequency.wav");
			// Demodulator demod = new Demodulator(44100, 50, 3080);
			// double[] conv = demod.convolve(arr, 50);
			// for (double val : conv){
			// //System.out.println(val);
			// }
			//
			// String msg = demod.demodulateAmplitude(demod.convolve(arr, 5),
			// 0.5);
			// System.out.println(myString);
			// System.out.println(msg.trim());
			// System.out.println((msg.trim()).equals(myString.trim()));

			// writer.getSamples();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void highpass(float[] message, double baseFreq, double modIndex){
//		  if(argc < 4)
//		  {
//		      printf("Usage: %s n s f\n", argv[0]);
//		      printf("Butterworth Highpass filter.\n");
//		      printf("  n = filter order 2,4,6,...\n");
//		      printf("  s = sampling frequency\n");
//		      printf("  f = half power frequency\n");
//		      return(-1);
//		  }

		  int i, n = 4;// filter order
		  n = n/2;
		  double cutoffFreq = 2*(baseFreq*modIndex);
		  
		  double a = Math.tan(Math.PI*cutoffFreq/baseFreq);
		  double a2 = a*a;
		  double r;
		  
		  double[] A = new double[n];
		  double[] d1 = new double[n];
		  double[] d2 = new double[n];
		  double[] w0 = new double[n];
		  double[] w1 = new double[n];
		  double[] w2 = new double[n];
		  double x;

		  for(i=0; i<n; ++i){
		    r = Math.sin(Math.PI*(2.0*i+1.0)/(4.0*n));
		    baseFreq = a2 + 2.0*a*r + 1.0;
		    A[i] = 1.0/baseFreq;
		    d1[i] = 2.0*(1-a2)/baseFreq;
		    d2[i] = -(a2 - 2.0*a*r + 1.0)/baseFreq;}

		  for (float m : message){
			  x = m;
		    for(i=0; i<n; ++i){
		      w0[i] = d1[i]*w1[i] + d2[i]*w2[i] + x;
		      x = A[i]*(w0[i] - 2.0*w1[i] + w2[i]);
		      w2[i] = w1[i];
		      w1[i] = w0[i];}
		    System.out.println(x);}
		}

}
