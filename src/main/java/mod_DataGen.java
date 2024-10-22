import cpw.mods.fml.common.FMLCommonHandler;
import dan200.computer.core.Computer;
import dan200.turtle.shared.CCTurtle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class mod_DataGen extends BaseMod{


    @Override
    public void load() {
        System.out.println("Hello world! (DATAGEN)");
    }


    @Override
    public String getVersion() {
        return "1.0";
    }


    @Override
    public void modsLoaded() {
/*
        try {
            testFinder();
            //testPrint();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }*/

        File minecraftDir = FMLCommonHandler.instance().getMinecraftRootDirectory();
        File dumpDir = new File(minecraftDir, "datagen"); // Folder containing JARs and ZIPs



        try {
            Files.createDirectories(dumpDir.toPath());

            DG_RecipeDumper.dump(dumpDir);
            DG_EEValueDumper.dump(dumpDir);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}