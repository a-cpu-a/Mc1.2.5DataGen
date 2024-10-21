package datagen;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static datagen.StubGen.BASE_CLASS_NAME_PREFIX;
import static datagen.StubGen.CONSTRUCTOR_TAG;

public class ClassFinder {

    private static final String JAVA_PACKAGE_PREFIX = "java.";
    private static final String JAVAX_PACKAGE_PREFIX = "javax.";
    private static final String LWJGL_PACKAGE_PREFIX = "org.lwjgl.";
    private static final String JAVA_GAMES_PACKAGE_PREFIX = "net.java.games.";
    private static final String LUAJ_PACKAGE_PREFIX = "org.luaj.";
    private static final String MY_PACKAGE_PREFIX = "datagen.";
    private static final String MULTIMC_PACKAGE_PREFIX = "org.multimc";

    public static List<String> findClasses(URLClassLoader classLoader, String folderPath) throws IOException, URISyntaxException {
        Set<String> classNames = new HashSet<>();

        // Step 1: Get classes from URLClassLoader's URLs
        for (URL url : classLoader.getURLs()) {
            File filePath = new File(url.toURI());
            if (filePath.getName().endsWith(".jar") || filePath.getName().endsWith(".zip")) {
                classNames.addAll(getClassesFromJarOrZip(filePath));
            }
        }

        // Step 2: Get classes from JARs/ZIPs in the folder
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
                        classNames.addAll(getClassesFromJarOrZip(file));
                    }
                }
            }
        }

        // Step 3: Filter out built-in Java classes
        return filterOutJavaClasses(new ArrayList<>(classNames));
    }

    private static List<String> getClassesFromJarOrZip(File filePath) throws IOException {
        List<String> classNames = new ArrayList<>();
        JarFile jarFile = new JarFile(filePath);
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith(".class")) {

                if(entryName.contains("bcIntegration31x"))
                    continue;// bc 3.1 is for MC1.4.x, so no need for those

                // Convert the file path to a fully qualified class name
                String className = entryName.replace("/", ".").replace(".class", "");
                classNames.add(className);
            }
        }

        jarFile.close();
        return classNames;
    }

    private static List<String> filterOutJavaClasses(List<String> classNames) {
        List<String> filtered = new ArrayList<>();
        for (String className : classNames) {
            // Exclude built-in java, library classes
            if (!className.startsWith(JAVA_PACKAGE_PREFIX)
                    && !className.startsWith(LWJGL_PACKAGE_PREFIX)
                    && !className.startsWith(JAVA_GAMES_PACKAGE_PREFIX)
                    && !className.startsWith(LUAJ_PACKAGE_PREFIX)
                    && !className.startsWith(JAVAX_PACKAGE_PREFIX)
                    && !className.startsWith(MULTIMC_PACKAGE_PREFIX)
                    //exclude our code
                    && !className.startsWith(MY_PACKAGE_PREFIX)
                    && !className.endsWith("_DataGen")) {
                filtered.add(className);
            }
        }
        return filtered;
    }

    public static void testFinder() throws IOException, URISyntaxException {
        // Example usage
        URLClassLoader urlClassLoader = (URLClassLoader) ModClassLoader.class.getClassLoader();

        File minecraftDir = FMLCommonHandler.instance().getMinecraftRootDirectory();
        File modsDir = new File(minecraftDir, "mods"); // Folder containing JARs and ZIPs

        List<String> classList = findClasses(urlClassLoader, modsDir.getPath());

        Collections.shuffle(classList);


        File zipPath = new File(minecraftDir,"stubs.zip");

        System.out.println("Creating at '"+zipPath+"' (DATAGEN)");

        try (FileOutputStream fos = new FileOutputStream(zipPath)){
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for (String className : classList) {
                try {
                    Class<?> clz = Class.forName(className);
                    /*if(clz==null)
                        clz = Class.forName(className,true,urlClassLoader);

                    if(clz==null) {

                        System.err.println("(Error 87) AAAAAA helpepleplp "+className);
                        continue;
                    }*/

                    //Dump the class!
                    FileStubs stubs = StubGen.genFileStubs(clz);

                    String bText = stubs.fileText;
                    if(stubs.extensionThis!=null) {

                        String classPrefix = "";

                        if(className.equals("if") || className.equals("do") || className.equals("eu") || className.equals("ee"))
                            classPrefix = "$WasInvalid$";

                        bText = bText.replace("package "+StubGen.BASE_CLASS_PACKAGE+";","");
                        bText = bText.replace("class "+ BASE_CLASS_NAME_PREFIX,"class "+classPrefix);
                        bText = bText.replace("interface "+ BASE_CLASS_NAME_PREFIX,"interface "+classPrefix);

                        bText = bText.replace(StubGen.BASE_CLASS_PREFIX+"if ","$WasInvalid$if ");
                        bText = bText.replace(StubGen.BASE_CLASS_PREFIX+"do ","$WasInvalid$do ");
                        bText = bText.replace(StubGen.BASE_CLASS_PREFIX+"eu ","$WasInvalid$eu ");
                        bText = bText.replace(StubGen.BASE_CLASS_PREFIX+"ee ","$WasInvalid$ee ");

                        bText = bText.replace(CONSTRUCTOR_TAG+BASE_CLASS_NAME_PREFIX+"if","$WasInvalid$if");
                        bText = bText.replace(CONSTRUCTOR_TAG+BASE_CLASS_NAME_PREFIX+"do","$WasInvalid$do");
                        bText = bText.replace(CONSTRUCTOR_TAG+BASE_CLASS_NAME_PREFIX+"eu","$WasInvalid$eu");
                        bText = bText.replace(CONSTRUCTOR_TAG+BASE_CLASS_NAME_PREFIX+"ee","$WasInvalid$ee");

                        bText = bText.replace(StubGen.BASE_CLASS_PREFIX,"");
                        bText = bText.replace(
                                " extends ",
                                " extends "+stubs.extensionThis
                        );
                    }
                    bText = bText.replace(CONSTRUCTOR_TAG,"");
                    byte[] bytes = bText.getBytes(StandardCharsets.UTF_8);

                    ZipEntry zipEntry = new ZipEntry(
                            StubGen.safeRawName(className).replace('.','/')+".java"
                    );  // Create a new zip entry
                    //zipEntry.setSize(bytes.length);
                    zipEntry.setTime(0);

                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(bytes,0,bytes.length);
                    zipOut.closeEntry();

                    if(stubs.extensionThis!=null) {
                        String aString = stubs.fileText;


                        int idxOff = aString.indexOf("Stubclass")+7;
                        int idxClass = aString.substring(idxOff).indexOf("class");
                        if(idxClass==-1)
                            idxClass = aString.indexOf("interface");
                         else
                            idxClass+=idxOff;

                        int idxPublic = aString.indexOf("public");

                        if(idxPublic==-1 || idxPublic>idxClass) {
                            aString = new StringBuffer(aString)
                                    .insert(idxClass,"public/*default*/ ")
                                    .toString();
                        }

                        aString = aString.replace("final","/*final*/");

                        if(stubs.extensionAlt!=null) {
                            aString = aString.replace(
                                    " extends ",
                                    " extends "+stubs.extensionAlt
                            );
                        } else {
                            aString = aString.replace(
                                    " extends ",
                                    " "
                            );
                        }
                        //no need to super, if there are no constructors!
                        aString = aString.replace(CONSTRUCTOR_TAG,"void _$defused_$");

                        byte[] bytesA = aString
                                .getBytes(StandardCharsets.UTF_8);

                        zipEntry = new ZipEntry(
                                (stubs.altPrefix+className).replace('.','/')+".java"
                        );  // Create a new zip entry
                        //zipEntry.setSize(bytes.length);
                        zipEntry.setTime(0);

                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(bytesA,0,bytesA.length);
                        zipOut.closeEntry();
                    }

                    //System.out.println("(DATAGEN) "+className);


                } catch (Exception | NoClassDefFoundError | ExceptionInInitializerError e) {
                    System.out.println("(DATAGEN) "+className+" (ERROR 98)");
                    e.printStackTrace();
                }
            }

            zipOut.close();
        }

        System.out.println("(DATAGEN) Over!");
    }
}
