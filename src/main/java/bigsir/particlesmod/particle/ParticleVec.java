package bigsir.particlesmod.particle;

import bigsir.particlesmod.gl.DataProvider;
import bigsir.particlesmod.gl.DynamicFloatBuffer;
import bigsir.particlesmod.utils.Collision;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.world.World;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class ParticleVec implements DataProvider {
	public World world;
	public float x, y, z;
	public float ox, oy, oz;
	public float dx, dy, dz;
	public static float TERMINAL_VELOCITY = -1.4F;
	public int lifetime;
	public int ticks = 0;
	public boolean remove = false;
	public int blockLight;
	public int skyLight;
	public static List<IconCoordinate> coords;
	public float u, v;
	public AABB bb;

	public ParticleVec(World world, float x, float y, float z) {
		this.world = world;
		this.x = this.ox = x;
		this.y = this.oy = y;
		this.z = this.oz = z;
		this.lifetime = 40;
		double size = 0.25;
		this.bb = AABB.getPermanentBB(x - size, y - size, z - size, x + size, y + size, z + size);
	}

	public void setUV(int id) {
		if (coords == null) {
			coords = new ArrayList<>(TextureRegistry.particleAtlas.textureMap.values());
		}

		IconCoordinate randCoord = coords.get(world.rand.nextInt(coords.size()));
		//IconCoordinate randCoord = TextureRegistry.getTexture("particlesmod:particle/rain");
		//u = (float) randCoord.getIconUMin();
		//v = (float) randCoord.getIconVMin();
		//u = 5/8f + world.rand.nextInt(3) / 8f;
		//v = 2/8f;
		u = (float) randCoord.getIconUMin();
		v = (float) randCoord.getIconVMin();
	}

	public void accel(float dx, float dy, float dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	public void tick() {
		if (this.lifetime-- <= 0) {
			remove = true;
		}

		this.ox = this.x;
		this.oy = this.y;
		this.oz = this.z;

		// Movemenet
		float l = (float) Math.sqrt(this.dx * this.dx + this.dy * this.dy + this.dz * this.dz);
		float size = 0.1F;
		Collision.BB box = new Collision.BB(this.x - size, this.y - size, this.z - size, this. x + size, this.y + size, this.z + size);
		boolean collide = Collision.collide(world, box, new double[]{this.dx, this.dy, this.dz});
		this.x = (float) (box.corners[0][0] + size);
		this.y = (float) (box.corners[0][1] + size);
		this.z = (float) (box.corners[0][2] + size);
		if (collide) {
			this.dx *= 0.9F;
			this.dz *= 0.9F;
		}

		//Debug.change("vec");

		/*Vec3 collision = RayTrace.trace(world, this.x, this.y, this.z, this.dx, this.dy, this.dz, l);
		this.x = (float) collision.x;
		this.y = (float) collision.y;
		this.z = (float) collision.z;*/

		/*this.x += dx;
		this.y += dy;
		this.z += dz;

		// since checking for collision when dx < 1 is a bit cheaper
		//Vec3 collision = ((WorldPerf) world).checkBlockCollisionBetweenPoints(this.ox, this.oy, this.oz, this.ox, this.y, this.oz);
		HitResult collision = world.checkBlockCollisionBetweenPoints(Vec3.getPermanentVec3(this.ox, this.oy, this.oz), Vec3.getPermanentVec3(this.ox, this.y, this.oz));
		if (collision != null) {
			this.x = (float) collision.location.x + collision.side.getOffsetX() * 0.001f;
			this.y = (float) collision.location.y + collision.side.getOffsetY() * 0.001f;
			this.z = (float) collision.location.z + collision.side.getOffsetZ() * 0.001f;
		}*/

		// Do whatever should be tied to current position here

		this.dy -= 0.04F;
		//if (dy < TERMINAL_VELOCITY) dy = TERMINAL_VELOCITY;
		this.dx *= 0.98F;
		this.dy *= 0.98F;
		this.dz *= 0.98F;

		int fx = MathHelper.floor(x);
		int fy = MathHelper.floor(y);
		int fz = MathHelper.floor(z);

		this.blockLight = world.getBlockLightValue(fx, fy, fz);
		this.skyLight = world.getSavedLightValue(LightLayer.Sky, fx, fy, fz);

		ticks++;
	}

	public boolean hasMoved() { // If all deltas are zero then we haven't moved
		return this.dx != 0 || this.dy != 0 || this.dz != 0;
	}

	public boolean isNew() {
		return this.ticks == 1;
	}

	public boolean doRemove() {
		return remove;
	}

	/*
	 Passing our dynamic buffer here instead of the internal buffer
	 is not very good since it couples client logic to "server" logic
	 but it works for now
	 */
	@Override
	public void packData(DynamicFloatBuffer buffer, int index, int dataSize) {
		int subIndex = index / 4;
		int shift = index - subIndex * 4;

		// BLOCK 0
		if(hasMoved()) buffer.put(index * 3, this.x, this.y, this.z);
		// BLOCK 1
		buffer.put(dataSize * 3 + index * 3, this.ox, this. oy, this.oz);
		// BLOCK 2
		buffer.put(dataSize * 6 + index * 2, u);
		buffer.put(dataSize * 6 + index * 2 + 1, v);
		// BLOCK 3
		int data = this.blockLight << (8 * shift + 4) | this.skyLight << (8 * shift);
		buffer.putSub(dataSize * 8 + subIndex, data, shift * 8, 0xff);
	}

	@Override
	public boolean removedNextTick() {
		return lifetime - 1 <= 0;
	}
}
