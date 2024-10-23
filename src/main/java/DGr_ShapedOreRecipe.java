import forge.oredict.ShapedOreRecipe;

import java.lang.reflect.Field;

public class DGr_ShapedOreRecipe {

    private static final Class<ShapedOreRecipe> CLASS = ShapedOreRecipe.class;



    private static final Field INPUTS;
    private static final Field W;
    private static final Field H;
    private static final Field ALLOW_MIRRORING;

    static {
        try {
            INPUTS = CLASS.getDeclaredField("input");
            W = CLASS.getDeclaredField("width");
            H = CLASS.getDeclaredField("height");
            ALLOW_MIRRORING = CLASS.getDeclaredField("mirriored");

            INPUTS.setAccessible(true);
            W.setAccessible(true);
            H.setAccessible(true);
            ALLOW_MIRRORING.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object[] inputs(ShapedOreRecipe o) {
        try {
            return (Object[]) INPUTS.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static int w(ShapedOreRecipe o) {
        try {
            return  W.getInt(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static int h(ShapedOreRecipe o) {
        try {
            return  H.getInt(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean allowMirroring(ShapedOreRecipe o) {
        try {
            return  ALLOW_MIRRORING.getBoolean(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
