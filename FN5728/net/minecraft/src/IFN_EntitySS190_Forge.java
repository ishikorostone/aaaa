package net.minecraft.src;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class IFN_EntitySS190_Forge extends IFN_EntitySS190 implements IEntityAdditionalSpawnData {

	public IFN_EntitySS190_Forge(World world) {
		super(world);
	}

	public IFN_EntitySS190_Forge(World world, double d, double d1, double d2) {
		super(world, d, d1, d2);
	}

	public IFN_EntitySS190_Forge(World world, EntityLivingBase entityliving, float f) {
		super(world, entityliving, f);
	}

	public IFN_EntitySS190_Forge(World world, EntityLivingBase entityliving, float f, float speed) {
		super(world, entityliving, f, speed);
	}

	// Forge用
	@Override
	public void writeSpawnData(ByteArrayDataOutput data) {
		// 通常の方法では速度が足りないので特別仕様
		data.writeInt(thrower == null ? entityId : thrower.entityId);
		data.writeInt(Float.floatToIntBits((float)motionX));
		data.writeInt(Float.floatToIntBits((float)motionY));
		data.writeInt(Float.floatToIntBits((float)motionZ));
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data) {
		// 通常の方法では速度が足りないので特別仕様
		int lthrower = data.readInt();
		if (lthrower != 0) {
			Entity lentity = worldObj.getEntityByID(lthrower);
			if (lentity instanceof EntityLivingBase) {
				thrower = (EntityLivingBase)lentity;
			}
		}
		motionX = (double)Float.intBitsToFloat(data.readInt());
		motionY = (double)Float.intBitsToFloat(data.readInt());
		motionZ = (double)Float.intBitsToFloat(data.readInt());
		setVelocity(motionX, motionY, motionZ);
	}

}
