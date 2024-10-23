import datagen.StubGen;
import forge.oredict.OreDictionary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class DG_OreDictDumper {
    public static void dump(File path) throws IOException {

        StringBuilder builder = new StringBuilder(10000);

        int i = 0;
        while (!OreDictionary.getOreName(i).equals("Unknown")) {

            String name = OreDictionary.getOreName(i);
            ArrayList<aan> items = OreDictionary.getOres(i);

            builder.append("Oredict \"").append(StubGen.escape(name)).append("\"\n");

            for (aan itemStack : items) {
                DG_RecipeDumper.dumpItem(builder,itemStack);
            }

            i++;
        }

        Files.write(new File(path,"ore.txt").toPath(),builder.toString().getBytes(StandardCharsets.UTF_8));

    }
}
