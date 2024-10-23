import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

public class DG_ItemDisplayDumper {
    public static void dump(File path) throws IOException {


        StringBuilder builder = new StringBuilder(40000);



        short[] damageU2Diff = new short[65536];

        short j = 0;
        for (yr item : yr.e) {

            short id = j++;

            if(item==null)
                continue;//doesnt exist


            builder.append("Item ").append(id).append('\n');

            builder.append("Name: ").append(item.e()).append('\n');
            builder.append("Texture: ").append(item.getTextureFile()).append('\n');


            HashMap<Short,Integer> damage2TexLoc = new HashMap<>();

            int fTex = 0x1337DEAD;

            int prevVal = 0x1337DEAD;
            int iconIdx = 0x1337DEAD;
            boolean iconAddMeta = true;

            boolean oneTex = true;
            boolean canCrash = false;

            for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
                try {
                    int tex = item.b(i);
                    damage2TexLoc.put((short) i,tex);
                    if(fTex==0x1337DEAD)
                        fTex = tex;

                    else if(fTex!=tex)
                        oneTex = false;

                    if(iconAddMeta) {
                        if(prevVal==0x1337DEAD) {
                            iconIdx = tex-i;
                            prevVal = tex-1;

                        }
                        if(prevVal!=tex-1)
                            iconAddMeta = false;
                        else
                            prevVal = tex;
                    }
                }
                catch (Exception ignored) {
                    damage2TexLoc.put((short) i,0x1337DEAD);
                    canCrash = true;

                    if(iconAddMeta && prevVal!=0x1337DEAD)
                        prevVal++;
                }
            }

            if(canCrash)
                builder.append("CanCrash: true\n");

            if(oneTex) {
                builder.append("Icon*: ").append(fTex).append('\n');
            }
            else if(iconAddMeta) {
                builder.append("Icon+Dmg: ").append(iconIdx).append('\n');
            }
            else {



                builder.append("Icon0: TODO").append('\n');
            }


        }


        Files.write(new File(path,"items.txt").toPath(),builder.toString().getBytes(StandardCharsets.UTF_8));

    }
}
