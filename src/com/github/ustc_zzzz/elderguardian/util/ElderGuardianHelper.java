package com.github.ustc_zzzz.elderguardian.util;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Coerce;

import java.util.Optional;

/**
 * @author ustc_zzzz
 */
public class ElderGuardianHelper
{
    public static Vector3d getPlayerHeadingVector(Player player, double norm)
    {
        double pitch = player.getHeadRotation().getX(), yaw = player.getHeadRotation().getY();
        double yawCos = TrigMath.cos(-yaw * TrigMath.DEG_TO_RAD - TrigMath.PI);
        double yawSin = TrigMath.sin(-yaw * TrigMath.DEG_TO_RAD - TrigMath.PI);
        double pitchCos = -TrigMath.cos(-pitch * TrigMath.DEG_TO_RAD);
        double pitchSin = TrigMath.sin(-pitch * TrigMath.DEG_TO_RAD);
        return Vector3d.from(yawSin * pitchCos * norm, pitchSin * norm, yawCos * pitchCos * norm);
    }

    public static Optional<PotionEffect> getPotionEffect(String s, int duration)
    {
        int colonFirst = s.indexOf(':'), colonIndex = s.lastIndexOf(':');
        String potionTypeString = colonFirst == colonIndex ? s : s.substring(0, colonIndex);
        int amplifier = colonFirst == colonIndex ? 0 : Coerce.toInteger(s.substring(colonIndex + 1));
        Optional<PotionEffectType> potionType = Sponge.getRegistry().getType(PotionEffectType.class, potionTypeString);
        return potionType.map(type -> PotionEffect.of(type, amplifier, duration));
    }

    private ElderGuardianHelper()
    {
        // nothing
    }
}
