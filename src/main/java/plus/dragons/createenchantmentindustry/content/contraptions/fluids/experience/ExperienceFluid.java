package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.materials.ExperienceNuggetItem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

public class ExperienceFluid extends VirtualFluid {

	protected final int xpRatio;

	public ExperienceFluid(int xpRatio, Properties properties) {
		super(properties);
		this.xpRatio = xpRatio;
	}

	public ExperienceFluid(Properties properties) {
		this(1, properties);
	}

	public ExperienceOrb convertToOrb(Level level, double x, double y, double z, int fluidAmount) {
		return new ExperienceOrb(level, x, y, z, fluidAmount);
	}

	public void drop(ServerLevel level, Vec3 pos, int realFluidAmount) {
		var fluidAmount = realFluidAmount / UNIT_PER_MB;
		while (fluidAmount > 0) {
			int orbSize = ExperienceOrb.getExperienceValue(fluidAmount);
			fluidAmount -= orbSize;
			if (!ExperienceOrb.tryMergeToExisting(level, pos, orbSize)) {
				level.addFreshEntity(this.convertToOrb(level, pos.x, pos.y, pos.z, orbSize));
			}
		}
	}

	public void awardOrDrop(@Nullable Player player, ServerLevel level, Vec3 pos, Vec3 speed, int realAmount) {
		var amount = realAmount / UNIT_PER_MB;
		Random random = new Random();
		if (random.nextInt(10) == 0) {
			if (player != null) {
				CeiAdvancements.WHERE_IS_MY_EXPERIENCE.getTrigger().trigger((ServerPlayer) player);
			}
			return;
		}
		var orb = this.convertToOrb(level, pos.x, pos.y, pos.z, amount);
		if (player == null) {
			if (!ExperienceOrb.tryMergeToExisting(level, pos, orb.value)) {
				orb.setDeltaMovement(speed);
				level.addFreshEntity(orb);
			}
		} else {
			int left = orb.repairPlayerItems(player, orb.value);
			if (left > 0) {
				player.giveExperiencePoints(left);
				this.applyAdditionalEffects(player, left);
			}
		}
	}

	public void applyAdditionalEffects(LivingEntity entity, int expAmount) {

	}

	public int getXpRatio() {
		return xpRatio;
	}

}
