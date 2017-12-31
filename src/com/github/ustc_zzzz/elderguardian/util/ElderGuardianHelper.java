package com.github.ustc_zzzz.elderguardian.util;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.HeaderMode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Coerce;

import java.io.*;
import java.util.Objects;
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

    public static String swapUnderlinesAndDashes(String original)
    {
        // helper method for hocon config files because hocon prefers dashes
        char[] newChars = original.toCharArray();
        for (int i = 0; i < newChars.length; ++i)
        {
            switch (newChars[i])
            {
            case '-':
                newChars[i] = '_';
                break;
            case '_':
                newChars[i] = '-';
                break;
            default:
                // do nothing
            }
        }
        return new String(newChars);
    }

    public static ItemStack fromJson(String json) throws ObjectMappingException
    {
        try
        {
            StringReader reader = new StringReader(json);
            GsonConfigurationLoader loader = GsonConfigurationLoader
                    .builder().setSource(() -> new BufferedReader(reader)).build();
            return Objects.requireNonNull(loader.load().getValue(TypeToken.of(ItemStack.class)));
        }
        catch (NullPointerException | IOException e)
        {
            throw new ObjectMappingException(e);
        }
    }

    public static String toJson(ItemStack item) throws ObjectMappingException
    {
        try
        {
            StringWriter writer = new StringWriter();
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                    .setIndent(0).setSink(() -> new BufferedWriter(writer)).setHeaderMode(HeaderMode.NONE).build();
            loader.save(loader.createEmptyNode().setValue(TypeToken.of(ItemStack.class), Objects.requireNonNull(item)));
            return writer.toString();
        }
        catch (NullPointerException | IOException e)
        {
            throw new ObjectMappingException(e);
        }
    }

    public static boolean matchWildcard(String wildcard, String value)
    {
        // TODO: should be replaced by the implementation of the plugin itself
        return FilenameUtils.wildcardMatch(value, wildcard);
    }

    private ElderGuardianHelper()
    {
        // nothing
    }
}
