import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static ee.EEMaps.alchemicalValues;

public class DG_EEValueDumper {
    public static void dump(File path) throws IOException {


        StringBuilder builder = new StringBuilder(10000);

        TreeMap<Integer,StringBuilder> emc2Lines = new TreeMap<>();

        //id2(meta2emc)
        Set<Map.Entry<Integer,HashMap<Integer,Integer>>> emcSet = alchemicalValues.entrySet();
        for (Map.Entry<Integer,HashMap<Integer,Integer>> o : emcSet) {

            Integer id = o.getKey();
            Set<Map.Entry<Integer,Integer>> metaSet =  o .getValue().entrySet();

            for (Map.Entry<Integer,Integer> integer : metaSet) {
                Integer meta = integer.getKey();
                Integer emc = integer.getValue();
                aan itemStack = new aan(id, 0, meta);

                StringBuilder sb = emc2Lines.get(emc);
                if(sb==null) {
                    sb = new StringBuilder(100);
                    emc2Lines.put(emc,sb);
                }

                sb.append("Id_").append(id).append(", Dmg_")
                        .append(meta).append(", Emc_")
                        .append(emc).append(", Name: ")
                        .append(itemStack.a().e())//translation key / id
                        .append("\n");
            }
        }

        for (StringBuilder sb : emc2Lines.values()) {
            builder.append(sb);
        }

        Files.write(new File(path,"emc.txt").toPath(),builder.toString().getBytes(StandardCharsets.UTF_8));

    }
}
