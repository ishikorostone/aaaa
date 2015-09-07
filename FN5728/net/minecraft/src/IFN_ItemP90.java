package net.minecraft.src;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public class IFN_ItemP90 extends IFN_ItemFN5728 {

	public IFN_ItemP90(int i) {
		super(i);
		setMaxDamage(50);
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {
		if (!isReload(itemstack)) {
			// リロードしてない時のみ動作
			boolean lflag = cycleBolt(itemstack);
			entity = MMM_Helper.getAvatarPlayer(entity);
			
			if (entity != null && entity instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer)entity;
				if (entityplayer.isUsingItem() && itemstack == entityplayer.getCurrentEquippedItem()) {
					// 射撃中なら実行
					if (lflag) {
						// 発射
						int lj = getReload(itemstack);
//						mod_IFN_FN5728Guns.Debug(String.format("P90-FireWorks-remort:%b, vol:%04x", world.isRemote, lj));
						int li = getMaxDamage() - itemstack.getItemDamage();
						if (li > 0) {
							// 発射
							fireBullet(itemstack, world, entityplayer, 0F, 1.0F, 0.1F);
							resetBolt(itemstack);
							MMM_Helper.updateCheckinghSlot(entityplayer, itemstack);
						} else {
							// 弾切れ
							if (canReload(itemstack, entityplayer)) {
								entityplayer.stopUsingItem();
							}
						}
					} else {
						MMM_Helper.updateCheckinghSlot(entityplayer, itemstack);
					}
				}
			}
		}
		
		super.onUpdate(itemstack, world, entity, i, flag);
	}

	@Override
	public byte getCycleCount(ItemStack pItemstack) {
		return 1;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		itemstack = super.onItemRightClick(itemstack, world, entityplayer);
		if (!isReload(itemstack) && itemstack.getItemDamage() >= getMaxDamage()) {
			// 弾切れ
			world.playSoundAtEntity(entityplayer, "FN5728.emptyP90s", 1.0F, 1.0F);
		}
		return itemstack;
	}

	@Override
	public int reloadTime() {
		// 3.0Sec
		return 60;
	}

	@Override
	public void releaseMagazin(ItemStack itemstack, World world, Entity entity) {
		world.playSoundAtEntity(entity, "FN5728.releaseP90s", 1.0F, 1.0F);
		super.releaseMagazin(itemstack, world, entity);
	}

	@Override
	public void reloadMagazin(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		world.playSoundAtEntity(entityplayer, "FN5728.reloadP90s", 1.0F, 1.0F);
		super.reloadMagazin(itemstack, world, entityplayer);
	}

	@Override
	public boolean isWeaponFullAuto(ItemStack itemstack) {
		return true;
	}

}
