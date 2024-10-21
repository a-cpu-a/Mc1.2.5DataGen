import java.io.IOException;
import java.net.URISyntaxException;

import static datagen.ClassFinder.testFinder;
import static datagen.GenStubs.testPrint;

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
        try {
            testFinder();
            //testPrint();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}