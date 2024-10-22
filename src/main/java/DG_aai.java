import java.lang.reflect.Field;

public class DG_aai {

    private static final Class<aai> CLASS = aai.class;



    private static final Field WIDTH;
    private static final Field HEIGHT;
    private static final Field INPUTS;

    static {
        try {
            WIDTH = CLASS.getDeclaredField("b");
            HEIGHT = CLASS.getDeclaredField("c");
            INPUTS = CLASS.getDeclaredField("d");

            WIDTH.setAccessible(true);
            HEIGHT.setAccessible(true);
            INPUTS.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static int w(aai o) {
        try {
            return WIDTH.getInt(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static int h(aai o) {
        try {
            return HEIGHT.getInt(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static aan[] inputs(aai o) {
        try {
            return (aan[]) INPUTS.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
