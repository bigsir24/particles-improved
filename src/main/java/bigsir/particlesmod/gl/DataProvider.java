package bigsir.particlesmod.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface DataProvider {
	void packData(DynamicFloatBuffer buffer, int index, int dataSize);
	boolean removedNextTick();
}
