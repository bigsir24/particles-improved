package bigsir.particlesmod.utils;

import net.minecraft.core.block.Block;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.world.World;

import java.util.ArrayList;

public class Collision {
	public static final ArrayList<AABB> blockColliders = new ArrayList<>();
	public static class BB {
		public static final AABB VANILLA_BOUNDS = AABB.getPermanentBB(0, 0, 0, 1, 1, 1);
		public static final BB MODDED_BOUNDS = new BB(0, 0, 0, 1, 1, 1);
		public double[][] corners = new double[2][3];
		public BB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			this.corners[0][0] = minX;
			this.corners[0][1] = minY;
			this.corners[0][2] = minZ;
			this.corners[1][0] = maxX;
			this.corners[1][1] = maxY;
			this.corners[1][2] = maxZ;
		}

		public AABB asVanilla() {
			VANILLA_BOUNDS.set(corners[0][0], corners[0][1], corners[0][2], corners[1][0], corners[1][1], corners[1][2]);
			return VANILLA_BOUNDS;
		}

		public static BB fromVanilla(AABB aabb) {
			MODDED_BOUNDS.corners[0][0] = aabb.minX;
			MODDED_BOUNDS.corners[0][1] = aabb.minY;
			MODDED_BOUNDS.corners[0][2] = aabb.minZ;
			MODDED_BOUNDS.corners[1][0] = aabb.maxX;
			MODDED_BOUNDS.corners[1][1] = aabb.maxY;
			MODDED_BOUNDS.corners[1][2] = aabb.maxZ;
			return MODDED_BOUNDS;
		}

		public void move(double x, double y, double z) {
			MODDED_BOUNDS.corners[0][0] += x;
			MODDED_BOUNDS.corners[0][1] += y;
			MODDED_BOUNDS.corners[0][2] += z;
			MODDED_BOUNDS.corners[1][0] += x;
			MODDED_BOUNDS.corners[1][1] += y;
			MODDED_BOUNDS.corners[1][2] += z;
		}

		public void move(int axis, double offset) {
			this.corners[0][axis] += offset;
			this.corners[1][axis] += offset;
		}
	}

	public static final int[] iterators = new int[3];
	public static final MinMax[] clipArray = new MinMax[]{Math::max, Math::min};

	public static boolean collide(World world, BB collider, double[] vec) {
		boolean moved = false;

		for (int dir = 0; dir < 3; dir++) {
			double delta = vec[dir];
			int sign = Math.max(-(int) Math.signum(delta), 0);
			double cOuter = collider.corners[~sign & 1][dir];
			double cOuterMove = cOuter + delta;

			int start = MathHelper.floor(Math.min(cOuterMove, cOuter));
			int end = MathHelper.floor(Math.max(cOuterMove, cOuter));

			int i1 = (dir + 1) % 3;
			int i2 = (dir + 2) % 3;
			int end1 = MathHelper.floor(collider.corners[1][i1]);
			int end2 = MathHelper.floor(collider.corners[1][i2]);

			double clippedPosition = cOuter;

			for (iterators[dir] = start; iterators[dir] <= end; iterators[dir]++) {
				for (iterators[i1] = MathHelper.floor(collider.corners[0][i1]); iterators[i1] <= end1; iterators[i1]++) {
					for (iterators[i2] = MathHelper.floor(collider.corners[0][i2]); iterators[i2] <= end2; iterators[i2]++) {
						blockColliders.clear();

						int fx = iterators[0];
						int fy = iterators[1];
						int fz = iterators[2];

						Block<?> block = world.getBlock(fx, fy, fz);
						if (block == null) {
							clippedPosition = clipArray[~sign & 1].clip(clipArray[sign].clip(cOuterMove, cOuter), clippedPosition);
							continue;
						} // Do whatever when our block is air, supposedly move the bb on a given axis

						collider.corners[~sign & 1][dir] = cOuterMove; // FIXME this is not ideal
						block.getCollidingBoundingBoxes(world, fx, fy, fz, collider.asVanilla(), blockColliders);
						collider.corners[~sign & 1][dir] = cOuter;

						//System.out.println(block.getLogic().getCollisionBoundingBoxFromPool(world, fx, fy, fz));
						//System.out.println(collider.asVanilla());

						for (AABB vanillaBB : blockColliders) {
							BB bb = BB.fromVanilla(vanillaBB);
							double axisClose = bb.corners[sign][dir];
							double axisFar = bb.corners[~sign & 1][dir];

							//If the signs of these do not match, we have a collision, otherwise we can proceed
							// This basically means that if the axis closest to this direction of movement is contained
							// by the previous line and the current line then they're colliding
							//
							//If the bb was already inside a block, this will allow it to pass through
							double outerMoveDiff = axisClose - cOuterMove;
							double outerDiff = axisClose - cOuter;
							// Degenerate cases:
							// if outerDiff is 0 it should be considered a collision
							// BUT if outerMoveDiff is 0, it should NOT be considered a collision

							if (outerDiff == 0 || outerDiff * outerMoveDiff < 0) {
								clippedPosition = clipArray[sign].clip(cOuter - outerMoveDiff, clippedPosition);
								//TODO quit early if clippedPosition is equal to cOuter (because then we can't move further anyway)
								//System.out.println(cOuter);
							}
						}
					}
				}
			}

			moved |= cOuterMove - clippedPosition != delta;
			collider.move(dir, cOuterMove - clippedPosition);
		}

		return moved;
	}

	public interface MinMax {
		double clip(double a, double b);
	}
}
