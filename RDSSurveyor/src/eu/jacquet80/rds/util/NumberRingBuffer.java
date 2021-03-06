package eu.jacquet80.rds.util;


public class NumberRingBuffer extends RingBuffer<Number> {

	public NumberRingBuffer(int length) {
		super(Number.class, length, 0);
	}

	public synchronized double getAverageValue() {
		double sum = 0;
		
		for(Number n : values) {
			sum += n.doubleValue();
		}
		
		return sum / length;
	}
}
