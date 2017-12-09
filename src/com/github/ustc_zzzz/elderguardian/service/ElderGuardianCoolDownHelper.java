package com.github.ustc_zzzz.elderguardian.service;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.api.CoolDownHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class ElderGuardianCoolDownHelper implements CoolDownHelper
{
    private final long currentThreadID;
    private final Table<Player, String, Long> coolDownExpireTable;

    ElderGuardianCoolDownHelper(ElderGuardian plugin)
    {
        this.currentThreadID = Thread.currentThread().getId();
        this.coolDownExpireTable = Tables.newCustomTable(new WeakHashMap<>(), HashMap::new);
        Sponge.getEventManager().registerListener(plugin, MoveEntityEvent.Teleport.class, new TeleportEntity());
    }

    @Override
    public long getCoolDown(String key, Player player)
    {
        this.checkThreadID();
        Long expireTime = this.coolDownExpireTable.get(player, key);
        if (expireTime == null) return 0;
        long coolDown = expireTime - this.now(player);
        if (coolDown > 0) return coolDown;
        this.coolDownExpireTable.remove(player, key);
        return 0;
    }

    @Override
    public long setCoolDown(String key, long coolDown, Player player)
    {
        this.checkThreadID();
        long now = this.now(player);
        Long expireTime = this.coolDownExpireTable.put(player, key, coolDown + now);
        return expireTime != null && expireTime > now ? expireTime - now : 0;
    }

    @Override
    public long addCoolDown(String key, long coolDown, Player player)
    {
        this.checkThreadID();
        long now = this.now(player);
        Long expireTime = this.coolDownExpireTable.get(player, key);
        long exactExpireTime = expireTime != null && expireTime > now ? expireTime : now;
        this.coolDownExpireTable.put(player, key, coolDown + exactExpireTime);
        return exactExpireTime - now;
    }

    private long now(Player player)
    {
        return player.getWorld().getProperties().getTotalTime();
    }

    private void checkThreadID()
    {
        String error = "CoolDownHelper does not support multi threading";
        Preconditions.checkArgument(this.currentThreadID == Thread.currentThread().getId(), error);
    }

    private class TeleportEntity implements EventListener<MoveEntityEvent.Teleport>
    {
        @Override
        public void handle(MoveEntityEvent.Teleport event) throws Exception
        {
            Entity targetEntity = event.getTargetEntity();
            if (!(targetEntity instanceof Player)) return;

            World fromExtent = event.getFromTransform().getExtent();
            World toExtent = event.getToTransform().getExtent();
            if (!Objects.equals(fromExtent, toExtent)) return;

            long fromExtentNow = fromExtent.getProperties().getTotalTime();
            long toExtentNow = toExtent.getProperties().getTotalTime();
            long timeDifference = toExtentNow - fromExtentNow;

            Table<Player, String, Long> coolDownExpireTable = ElderGuardianCoolDownHelper.this.coolDownExpireTable;
            coolDownExpireTable.row((Player) targetEntity).replaceAll((k, v) -> v + timeDifference);
        }
    }
}
