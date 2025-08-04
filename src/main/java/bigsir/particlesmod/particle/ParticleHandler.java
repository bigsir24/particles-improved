package bigsir.particlesmod.particle;

import bigsir.particlesmod.gl.DynamicFloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleHandler {
	public static List<ParticleVec> particles = new ArrayList<>();

	public static final int DATA_SIZE = 8;
	public static DynamicFloatBuffer floatBuffer = new DynamicFloatBuffer(1, 10);
	public static int removeNextCount = 0;
	public static int blockSize = 0;

	// Allocate reasonably big buffers -> bufferSubData to change contents -> possibly store "empty" slots for future realloc ->
	// possibly resize buffer if too many empty slots (although this probably isn't needed)

	public static void simulate(World world) {
		Random rand = world.rand;
		Player player = Minecraft.getMinecraft().thePlayer;

		for (int i = 0; i < 200; i++) {
			//ParticleVec particle = new ParticleVec(world, (float) ((float) player.x + 4 + (rand.nextGaussian()) * 32), (float) player.y + 32 + rand.nextFloat(), (float) ((float) player.z + (rand.nextGaussian()) * 32));
			ParticleVec particle = new ParticleVec(world, 0, 23, 0);
			particles.add(particle);
			particle.setUV(0);
			float speed = 1F;
			particle.accel((rand.nextFloat() - 0.5F) * speed, 0, (rand.nextFloat() - 0.5F) * speed);
			//double windDir = Math.toRadians(world.worldType.getWindManager().getWindDirection(world, 0, 500, 0) * 360F);
			//particle.accel((float) Math.cos(windDir) *speed, -0.7F, (float) Math.sin(windDir) * speed);
		}

		/*
		 	We should already *roughly* know the size of our buffer here from the amount of particles
		 	yet to be added, and the amount that will expire in the "next" tick (lifetime - 1)
		 	the only discrepancy should be caused by particles being removed unexpectedly
		 	(ie manually removed or particles that immediately expire)
		 */

		int particleCount = blockSize = particles.size();
		int dataSize = particleCount * DATA_SIZE + MathHelper.ceil(particleCount / 4.0F);
		floatBuffer.resetIndices();
		if (floatBuffer.length() < dataSize) floatBuffer.resize(dataSize);

		floatBuffer.buffer.clear();
		removeNextCount = 0;
		for (int i = 0; i < particles.size(); i++) {
			ParticleVec pVec = particles.get(i);
			pVec.tick();

			int lastIndex = particles.size() - 1;
			if(pVec.doRemove()) { //Not particularly good
				if (i == lastIndex) { //TODO if we removed a particle we should try and put a new one in its place before resizing
					particles.remove(lastIndex);
				}else {
					ParticleVec last = particles.set(lastIndex, pVec);
					particles.remove(lastIndex);
					particles.set(i, last);
				}
			}else {
				if (pVec.removedNextTick()) removeNextCount++;
			}
			pVec.packData(floatBuffer, i, particleCount);
		}
		floatBuffer.buffer.clear();

		/*floatBuffer.resetIndices(); // Reset render state
		if (floatBuffer.length() < particles.size() * DATA_SIZE) floatBuffer.resize(particles.size() * DATA_SIZE);

		floatBuffer.buffer.clear();

		// This could be part of the tick loop, but that would require a reasonable
		// resizing method to avoid creating too many new arrays
		for (int i = 0; i < particles.size(); i++) {
			ParticleVec p = particles.get(i);
			if (p.hasMoved() || p.isNew()) {
				floatBuffer.put(i * DATA_SIZE, p.x, p.y, p.z);
				floatBuffer.put(i * DATA_SIZE + 3, p.dx, p.dy, p.dz);
				floatBuffer.put(i * DATA_SIZE + 6, p.u);
				floatBuffer.put(i * DATA_SIZE + 7, p.v);
				floatBuffer.put(i * DATA_SIZE + 8, p.blockLight);
				floatBuffer.put(i * DATA_SIZE + 9, p.skyLight);
			}else {

					//Always update the delta, this ensures that we don't lerp stale positions,
					//but with the current implementation this means updating the entire buffer
					//even if the position hasn't changed
					//Could have its own buffer if this is ever an issue

				floatBuffer.put(i * DATA_SIZE + 3, p.dx, p.dy, p.dz);
			}
		}
		floatBuffer.buffer.clear();*/

	}
}
