package net.minecraft.src;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet23VehicleSpawn;
import net.minecraft.util.MathHelper;

public class IFN_PacketSS190Spawn extends Packet23VehicleSpawn {
	
	public IFN_PacketSS190Spawn(Entity par1Entity, int par2, int par3) {
		super();
		
		this.entityId = par1Entity.entityId;
		this.xPosition = MathHelper.floor_double(par1Entity.posX * 32.0D);
		this.yPosition = MathHelper.floor_double(par1Entity.posY * 32.0D);
		this.zPosition = MathHelper.floor_double(par1Entity.posZ * 32.0D);
		this.type = par2;
		this.throwerEntityId = par3;
		this.speedX = Float.floatToIntBits((float)par1Entity.motionX);
		this.speedY = Float.floatToIntBits((float)par1Entity.motionY);
		this.speedZ = Float.floatToIntBits((float)par1Entity.motionZ);
	}

	@Override
	public void readPacketData(DataInput par1DataInput) throws IOException {
		this.entityId = par1DataInput.readInt();
		this.type = par1DataInput.readByte();
		this.xPosition = par1DataInput.readInt();
		this.yPosition = par1DataInput.readInt();
		this.zPosition = par1DataInput.readInt();
		this.throwerEntityId = par1DataInput.readInt();
		
		if (this.throwerEntityId > 0) {
			this.speedX = par1DataInput.readInt();
			this.speedY = par1DataInput.readInt();
			this.speedZ = par1DataInput.readInt();
		}
	}

	@Override
	public void writePacketData(DataOutput par1DataOutput) throws IOException {
		par1DataOutput.writeInt(this.entityId);
		par1DataOutput.writeByte(this.type);
		par1DataOutput.writeInt(this.xPosition);
		par1DataOutput.writeInt(this.yPosition);
		par1DataOutput.writeInt(this.zPosition);
		par1DataOutput.writeInt(this.throwerEntityId);

		if (this.throwerEntityId > 0) {
			par1DataOutput.writeInt(this.speedX);
			par1DataOutput.writeInt(this.speedY);
			par1DataOutput.writeInt(this.speedZ);
		}
	}
	
	@Override
	public int getPacketSize() {
		return 21 + (throwerEntityId > 0 ? 12 : 0);
	}


	@Override
	public void processPacket(NetHandler par1NetHandler) {
		if (par1NetHandler instanceof NetClientHandler) {
			Minecraft mc = MMM_Helper.mc;
			WorldClient lworld = mc.theWorld;
			double lx = (double)this.xPosition / 32.0D;
			double ly = (double)this.yPosition / 32.0D;
			double lz = (double)this.zPosition / 32.0D;
			
			Entity le = (mc.thePlayer.entityId == throwerEntityId) ? mc.thePlayer : lworld.getEntityByID(throwerEntityId);
			if (le instanceof EntityLivingBase) {
				IFN_EntitySS190 lentity = new IFN_EntitySS190(lworld, lx, ly, lz);
				lentity.serverPosX = this.xPosition;
				lentity.serverPosY = this.yPosition;
				lentity.serverPosZ = this.zPosition;
				lentity.rotationYaw = 0.0F;
				lentity.rotationPitch = 0.0F;
				lentity.entityId = this.entityId;
				lentity.thrower = (EntityLivingBase)le;
				lentity.setVelocity((double)Float.intBitsToFloat(this.speedX), (double)Float.intBitsToFloat(this.speedY), (double)Float.intBitsToFloat(this.speedZ));
				lworld.addEntityToWorld(this.entityId, lentity);
			}
		}
	}

}
