package net.laserdiamond.ultimatemanhunt.capability;

import net.laserdiamond.laserutils.capability.AbstractCapability;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID)
public class UMPlayerCapability extends AbstractCapability<Entity, UMPlayer>
{

    public static final Capability<UMPlayer> UM_PLAYER = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            AbstractCapability.attachCapability(event, UM_PLAYER, UltimateManhunt.fromUMPath("ultimate_manhunt"), UMPlayerCapability::new);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event)
    {
        AbstractCapability.cloneOnPlayerDeath(event, UM_PLAYER);
    }

    private UMPlayer umPlayer = null;

    protected UMPlayerCapability(Entity obj) {
        super(obj);
    }

    @Override
    protected Capability<UMPlayer> createCapability() {
        return UM_PLAYER;
    }

    @Override
    protected UMPlayer createCapabilityData()
    {
        if (this.umPlayer == null)
        {
            this.umPlayer = new UMPlayer(this.obj.getUUID());
        }
        return this.umPlayer;
    }
}
