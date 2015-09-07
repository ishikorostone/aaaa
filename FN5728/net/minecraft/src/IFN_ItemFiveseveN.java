package net.minecraft.src;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public class IFN_ItemFiveseveN extends IFN_ItemFN5728 {

	public IFN_ItemFiveseveN(int i) {
		super(i);
		setMaxDamage(20);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int i) {
		if (!isReload(itemstack)) {
			// リロード中ではない
			if (itemstack.getItemDamage() < getMaxDamage()) {
				int j = getMaxItemUseDuration(itemstack) - i;
				float f = (float)j / 20F;
				f = (f * f + f * 2.0F) / 3F;
				if (f > 1.0F) {
					f = 1.0F;
				}
				fireBullet(itemstack, world, entityplayer, f, 0.838F, 1.0F);
			} else {
				// 弾切れ
				world.playSoundAtEntity(entityplayer, "FN5728.emptyFive-seveN", 1.0F, 1.0F);
			}
		}
		super.onPlayerStoppedUsing(itemstack, world, entityplayer, i);
	}

	@Override
	public int reloadTime() {
		// 2Sec
		return 40;
	}

	@Override
	public void releaseMagazin(ItemStack itemstack, World world, Entity entity) {
		mod_IFN_FN5728Guns.Debug(String.format("releaseMagazin-remort:%b", world.isRemote));
		world.playSoundAtEntity(entity, "FN5728.releaseFive-seveN", 1.0F, 1.0F);
		super.releaseMagazin(itemstack, world, entity);
	}

	@Override
	public void reloadMagazin(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		world.playSoundAtEntity(entityplayer, "FN5728.reloadFive-seveN", 1.0F, 1.0F);
		super.reloadMagazin(itemstack, world, entityplayer);
	}

}
