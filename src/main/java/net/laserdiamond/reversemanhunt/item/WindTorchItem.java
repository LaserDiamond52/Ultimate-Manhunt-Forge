package net.laserdiamond.reversemanhunt.item;

import net.laserdiamond.laserutils.util.AssetSkipModel;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
                .add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ReverseManhunt.fromRMPath("base_attack_knockback"), 200F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    private static Tool createToolProperties()
    {
        return new Tool(List.of(), 1.0F, 2);
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }
}
