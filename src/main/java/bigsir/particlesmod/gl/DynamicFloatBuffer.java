package bigsir.particlesmod.gl;

import bigsir.particlesmod.particle.ParticleHandler;
import bigsir.particlesmod.particle.ParticleVec;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL44;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class DynamicFloatBuffer {
	public IntBuffer buffer;
	public int growIncrement;
	public float minBufferSegment = 0.25F;
	// The maximum segment size below which bufferSubData calls will be merged (also taking "empty space" into account, which forms at the end of the buffer)
	// In most cases this is insignificant because particles will generally be updating state between game ticks,
	// but this allows for cheaper stationary particles, by preventing edge cases where two particles in both ends
	// of a large buffer would cause the whole buffer to be resent
	public List<IndexRange> ranges = new ArrayList<>();
	public int allocatedCapacity = 0; // In floats
	public boolean change = false;

	public DynamicFloatBuffer(int initialSize, int growIncrement) {
		this.buffer = BufferUtils.createIntBuffer(initialSize);
		this.growIncrement = Math.max(growIncrement, 1);
		resetIndices();
	}

	public void bufferData(int type, int bufferLocation, int usage) {
		if (allocatedCapacity == 0) { // Better condition needed
			GL20.glBindBuffer(type, bufferLocation);
			GL20.glBufferData(type, buffer, usage);
			GL44.glBindBufferBase(type, 0, bufferLocation);
			allocatedCapacity = buffer.capacity();
		}else if (change) {
			GL20.glBindBuffer(type, bufferLocation);
			buffer.clear();
			/*try { //FIXME removed until I actually fix the ranges
				for (IndexRange range : ranges) {
					buffer.limit(range.maxIndex + 1).position(range.minIndex);
					GL20.glBufferSubData(type, (long) range.minIndex * Float.BYTES, buffer);
				}
				GL44.glBindBufferBase(type, 0, bufferLocation);
			}catch (IllegalArgumentException e) {
				System.out.println(ranges.size());
			}*/
			GL20.glBufferSubData(type, 0, buffer);
			GL44.glBindBufferBase(type, 0, bufferLocation);
			change = false;
		}
	}

	public void markChange() {
		this.change = true;
	}

	public void resetIndices() {
		ranges.clear(); //FIXME this might cause issues with very small minBufferSegment values
		ranges.add(new IndexRange(0, Integer.MIN_VALUE));
	}

	public void putSub(int i, int data, int offset, int mask) {
		ensureSize(i);
		ensureRanges(i);

		int currentValue = buffer.get(i);
		currentValue &= ~(mask << offset);
		buffer.put(i, currentValue | data);

		markChange();
	}

	public void put(int i, int data) {
		ensureSize(i);
		ensureRanges(i);

		buffer.put(i, data);

		markChange();
	}

	public void put(int i, float data) {
		ensureSize(i);
		ensureRanges(i);

		buffer.put(i, Float.floatToRawIntBits(data));

		markChange();
	}

	public void put(int i, float data1, float data2, float data3) {
		ensureSize(i + 2);
		ensureRanges(i, i + 2);

		buffer.position(i);
		buffer.put(Float.floatToRawIntBits(data1));
		buffer.put(Float.floatToRawIntBits(data2));
		buffer.put(Float.floatToRawIntBits(data3));

		markChange();
	}

	public void put(int i, int data1, int data2, int data3) {
		ensureSize(i + 2);
		ensureRanges(i, i + 2);

		buffer.position(i);
		buffer.put(data1);
		buffer.put(data2);
		buffer.put(data3);

		markChange();
	}

	public void ensureRanges(int i) {
		ensureRanges(i ,i);
	}

	public void ensureRanges(int i1, int i2) {
		// Merge with the last range if they're sufficiently close, otherwise add a new range
		boolean merged = false;
		/*for (IndexRange range : ranges) { // Not too good
			if (range.mergeIfWithin(i1, i2, ParticleHandler.blockSize)) {
				merged = true;
				break;
			}
		}*/
		/*if (!merged) {
			ranges.add(new IndexRange(i1, i2));
		}*/
	}

	public IndexRange getHighestRange() {
		return ranges.get(0);
	}

	public int length() {
		return buffer.capacity();
	}

	public void resize(int size) {
		IntBuffer newBuffer = BufferUtils.createIntBuffer(size);
		buffer.position(0);
		buffer.limit(Math.min(size, buffer.capacity()));
		buffer = newBuffer.put(buffer);
		allocatedCapacity = 0;
	}

	public void ensureSize(int i) { //FIXME
		//if (i >= buffer.length) resize(buffer.length + Math.max(growIncrement, buffer.length - i + 1));
	}
}
