import java.lang.reflect.Field;
import java.util.List;

public class DGr_aif {

    private static final Class<aif> CLASS = aif.class;



    private static final Field INPUTS;

    static {
        try {
            INPUTS = CLASS.getDeclaredField("b");

            INPUTS.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<aan> inputs(aif o) {
        try {
            return (List<aan>) INPUTS.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
