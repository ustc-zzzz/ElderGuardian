package com.github.ustc_zzzz.elderguardian.unsafe;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Throwables;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;

import java.lang.reflect.Field;

/**
 * @author ustc_zzzz
 */
public class SpongeUnimplemented
{
    private static final Class<?> ENTITY_FIREBALL_CLASS;
    private static final Field ACCELERATION_X_FIELD;
    private static final Field ACCELERATION_Y_FIELD;
    private static final Field ACCELERATION_Z_FIELD;

    public static void setFireballPower(Fireball fireball, Vector3d acceleration)
    {
        try
        {
            ACCELERATION_X_FIELD.set(fireball, acceleration.getX());
            ACCELERATION_Y_FIELD.set(fireball, acceleration.getY());
            ACCELERATION_Z_FIELD.set(fireball, acceleration.getZ());
        }
        catch (ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    static
    {
        try
        {
            ENTITY_FIREBALL_CLASS = Class.forName("net.minecraft.entity.projectile.EntityFireball");
            // noinspection JavaReflectionMemberAccess
            ACCELERATION_X_FIELD = ENTITY_FIREBALL_CLASS.getDeclaredField("field_70232_b");
            // noinspection JavaReflectionMemberAccess
            ACCELERATION_Y_FIELD = ENTITY_FIREBALL_CLASS.getDeclaredField("field_70233_c");
            // noinspection JavaReflectionMemberAccess
            ACCELERATION_Z_FIELD = ENTITY_FIREBALL_CLASS.getDeclaredField("field_70230_d");
        }
        catch (ReflectiveOperationException e)
        {
            throw Throwables.propagate(e);
        }
    }

    private SpongeUnimplemented()
    {
    }
}
