package net.laserdiamond.ultimatemanhunt.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.laserdiamond.laserutils.util.AssetSkipModel;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class WindTorchItem extends Item implements AssetSkipModel {

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    private static final UUID BASE_ATTACK_KNOCKBACK_UUID = UUID.fromString("10ece3e2-ecd1-4525-abe1-81ce91dfd55c");

    public WindTorchItem(Properties pProperties)
    {
        super(pProperties.durability(50));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifierBuilder = ImmutableMultimap.builder();
        modifierBuilder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "mainhand.attributes", -3.2f, AttributeModifier.Operation.ADDITION));
        modifierBuilder.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(BASE_ATTACK_KNOCKBACK_UUID, "mainhand.attribute", 200, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = modifierBuilder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pSlot) {
        return pSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pSlot);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, living -> living.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }



    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected)
    {
        if (pLevel.isClientSide)
        {
            return; // Ensure we are on the server
        }
        if (pEntity instanceof Player player)
        {
            if (!UMGame.isWindTorchEnabled()) // Is the wind torch enabled?
            {
                // Not enabled. Remove from inventory
                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() instanceof WindTorchItem, -1, player.inventoryMenu.getCraftSlots());
            }
        }

    }
}
