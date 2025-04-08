package net.laserdiamond.ultimatemanhunt.item;

import net.laserdiamond.laserutils.util.AssetSkipModel;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WindTorchItem extends Item implements AssetSkipModel {

    public WindTorchItem(Properties pProperties)
    {
        super(pProperties.durability(50)
                .attributes(createAttributes())
                .component(DataComponents.TOOL, createToolProperties()));
    }

    private static ItemAttributeModifiers createAttributes()
    {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UltimateManhunt.fromRMPath("base_attack_knockback"), 200F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    private static Tool createToolProperties()
    {
        return new Tool(List.of(), 1.0F, 2);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
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
