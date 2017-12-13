package com.github.ustc_zzzz.elderguardian.stat;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3d;
import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.AABB;

import java.util.List;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
public class StatAttract extends ElderGuardianStatBase
{
    private Optional<Task> task = Optional.empty();

    public StatAttract(ElderGuardian plugin)
    {
        super(plugin, "attract");
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.attract.defaultTemplate";
    }

    @Override
    public void onLoreStatEnable()
    {
        super.onLoreStatEnable();
        if (!this.task.isPresent())
        {
            this.task = Optional.of(Task.builder().name("StatAttractTask")
                    .intervalTicks(1).execute(this::executeTask).submit(this.getPluginInstance()));
        }
    }

    @Override
    public void onLoreStatDisable()
    {
        super.onLoreStatDisable();
        if (this.task.isPresent())
        {
            this.task.get().cancel();
            this.task = Optional.empty();
        }
    }

    private int getRadius(DataView data)
    {
        return data.getInt(DataQuery.of("radius")).orElse(0);
    }

    private double getMaxSpeed(DataView data)
    {
        return data.getDouble(DataQuery.of("max-speed")).orElse(0.0);
    }

    private void executeTask(Task task)
    {
        for (Player player : Sponge.getServer().getOnlinePlayers())
        {
            List<DataContainer> stats = this.getStatsInHand(player);
            int radius = stats.stream().mapToInt(this::getRadius).reduce(Math::max).orElse(0);
            double maxSpeed = stats.stream().mapToDouble(this::getMaxSpeed).reduce(Math::min).orElse(0);
            if (radius <= 0 || maxSpeed <= 0) continue;

            Optional<AABB> aabbOptional = player.getBoundingBox();
            if (!aabbOptional.isPresent()) return;
            AABB aabb = aabbOptional.get();

            Vector3d playerPosition = player.getLocation().getPosition();
            double multiplier = maxSpeed / GenericMath.sqrt(radius - 1);
            double diameter = radius * 2;

            AABB range = aabb.expand(diameter, diameter, diameter);
            for (Entity entity : player.getWorld().getIntersectingEntities(range, e -> e instanceof Creature))
            {
                Vector3d diffVector = entity.getLocation().getPosition().sub(playerPosition);
                double distance = GenericMath.sqrt(diffVector.lengthSquared());
                if (distance >= 1 && distance <= radius)
                {
                    double velocityValue = -GenericMath.sqrt(distance - 1) * multiplier;
                    Vector3d velocity = diffVector.normalize().mul(velocityValue);
                    entity.setVelocity(velocity);
                }
            }
        }
    }
}
