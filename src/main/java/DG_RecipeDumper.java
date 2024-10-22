import dan200.turtle.shared.ImpostorRecipe;
import datagen.StubGen;
import forge.oredict.OreDictionary;
import forge.oredict.ShapedOreRecipe;
import ic2.common.AdvRecipe;
import ic2.common.AdvShapelessRecipe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DG_RecipeDumper {

    private static Field damageInternalField;

    static {

        try {
            damageInternalField = aan.class.getDeclaredField("e");
            damageInternalField.setAccessible(true);
        } catch (NoSuchFieldException e) {
        }
    }

    public static void dumpItem(StringBuilder sb, aan itemStack) {

        if(itemStack==null) {
            sb.append("null\n");
            return;
        }

        if(itemStack.h()!=getItemstackDamageInternal(itemStack))
            sb.append("{ASSERT FALSE!}");

        sb.append("Id_").append(itemStack.c);

        if(itemStack.a!=1)
            sb.append(", ").append("Amt_").append(itemStack.a);

        if(itemStack.h()!=-1) {
            //damage is not ignored
            sb.append(", ").append("Dmg_").append(itemStack.h());
        }

        if(itemStack.d!=null && !itemStack.d.toString().equals("0 entries"))
            sb.append(", Tag_{ASSERT FALSE!}");
            //sb.append(", ").append("Tag_${").append(itemStack.d).append("}");

        sb.append("\n");

    }

    private static int getItemstackDamageInternal(aan itemStack) {

        try {
            return damageInternalField.getInt(itemStack);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dump(File path) throws IOException {

        StringBuilder builder = new StringBuilder();

        List<wf> recipes = fr.a().b();

        int shapedCount = 0;
        int shapelessCount = 0;

        for (wf o : recipes) {

            if(o instanceof ShapedOreRecipe) {
                if(o.getClass()!=ShapedOreRecipe.class) {
                    builder.append("TODO (ShapedOreRecipe): ").append(o).append("\n");
                }
                else {

                    builder.append("Shaped recipe ").append(shapedCount++).append(":\n");
                    builder.append("From: Forge\n");
                    builder.append("Output: ");dumpItem(builder,(o).b());
                    builder.append("Mirroring: ")
                            .append(DG_ShapedOreRecipe.allowMirroring((ShapedOreRecipe) o))
                            .append("\n");

                    int w = DG_ShapedOreRecipe.w((ShapedOreRecipe) o);
                    int h = DG_ShapedOreRecipe.h((ShapedOreRecipe) o);
                    Object[] inputs = DG_ShapedOreRecipe.inputs((ShapedOreRecipe) o);

                    if(inputs.length!=w*h)
                        builder.append("ASSERT FALSE!");

                    for (int i = 0; i < w; i++) {
                        for (int j = 0; j < h; j++) {
                            builder.append("Inputs").append(i).append(j).append(": ");
                            dumpOreDictItem(builder,inputs[i+j*w]);
                        }
                    }
                }
            }
            else if(o instanceof AdvRecipe) {
                if(o.getClass()!=AdvRecipe.class) {
                    builder.append("TODO (AdvRecipe): ").append(o).append("\n");
                }
                else {

                    builder.append("Shaped recipe ").append(shapedCount++).append(":\n");
                    builder.append("From: IC2\n");
                    builder.append("Output: ");dumpItem(builder,(o).b());
                    if(((AdvRecipe) o).hidden)
                        builder.append("Hidden: true\n");

                    int w = ((AdvRecipe) o).inputWidth;
                    int h = ((AdvRecipe) o).input.length/((AdvRecipe) o).inputWidth;
                    Object[] inputs = ((AdvRecipe) o).input;

                    if(inputs.length!=w*h)
                        builder.append("ASSERT FALSE!");

                    for (int i = 0; i < w; i++) {
                        for (int j = 0; j < h; j++) {
                            builder.append("Inputs").append(i).append(j).append(": ");
                            dumpOreDictItem(builder,inputs[i+j*w]);
                        }
                    }
                }
            }
            else if(o instanceof AdvShapelessRecipe) {

                if(o.getClass()!=AdvShapelessRecipe.class) {
                    builder.append("TODO (AdvShapelessRecipe): ").append(o).append("\n");
                }
                else {

                    builder.append("Shapeless recipe ").append(shapelessCount++).append(":\n");
                    builder.append("From: IC2\n");
                    builder.append("Output: ");dumpItem(builder,(o).b());
                    if(((AdvShapelessRecipe) o).hidden)
                        builder.append("Hidden: true\n");

                    Object[] inputs = ((AdvShapelessRecipe) o).input;

                    for (int i = 0; i < inputs.length; i++) {
                        builder.append("Inputs").append(i).append(": ");
                        dumpOreDictItem(builder,inputs[i]);
                    }
                }
            }
            else if(o instanceof aai) {//ShapedRecipes

                if(o instanceof ImpostorRecipe)
                    continue;//it doesnt exist (its only for NEI)

                if(o.getClass()!=aai.class) {
                    builder.append("TODO (aai): ").append(o).append("\n");
                }
                else {

                    builder.append("Shaped recipe ").append(shapedCount++).append(":\n");
                    builder.append("From: Vanilla\n");
                    builder.append("Output: ");dumpItem(builder,(o).b());
                    builder.append("Mirroring: true\n");

                    int w = DG_aai.w((aai) o);
                    int h = DG_aai.h((aai) o);
                    aan[] inputs = DG_aai.inputs((aai) o);

                    if(inputs.length!=w*h)
                        builder.append("ASSERT FALSE!");

                    for (int i = 0; i < w; i++) {
                        for (int j = 0; j < h; j++) {
                            builder.append("Inputs").append(i).append(j).append(": ");
                            dumpItem(builder,inputs[i+j*w]);
                        }
                    }
                }
            }
            else if(o instanceof aif) {//ShapelessRecipes

                if(o.getClass()!=aif.class) {
                    builder.append("TODO (aif): ").append(o).append("\n");
                }
                else {

                    builder.append("Shapeless recipe ").append(shapelessCount++).append(":\n");
                    builder.append("From: Vanilla\n");
                    builder.append("Output: ");dumpItem(builder,(o).b());

                    List<aan> inputs = DG_aif.inputs((aif) o);

                    for (int i = 0; i < inputs.size(); i++) {
                        builder.append("Inputs").append(i).append(": ");
                        dumpItem(builder,inputs.get(i));
                    }
                }
            }
            else {
                builder.append("TODO: ").append(o).append("\n");
            }
        }
        Files.write(new File(path,"recipes.txt").toPath(),builder.toString().getBytes(StandardCharsets.UTF_8));

    }

    private static void dumpOreDictItem(StringBuilder builder, Object input) {
        if(input instanceof String) {
            builder.append('"').append(StubGen.escape((String) input)).append('"').append("\n");
            return;
        }
        if(input instanceof ArrayList) {
            builder.append('"').append(StubGen.escape((String) oreDictItems2Name((ArrayList<?>) input))).append('"').append("\n");
            return;
        }
        dumpItem(builder, (aan) input);
    }

    private static Object oreDictItems2Name(ArrayList<?> input) {
        int i = 0;
        while (true) {
            if(OreDictionary.getOreName(i).equals("Unknown"))
                throw new RuntimeException("non-existant ore-dict "+input);

            if(OreDictionary.getOres(i)==input) {
                return OreDictionary.getOreName(i);
            }
            i++;
        }
    }
}
