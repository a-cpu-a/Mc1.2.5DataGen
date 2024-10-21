package datagen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class StubGen {

    /**
     * escape()
     *
     * Escape a give String to make it safe to be printed or stored.
     *
     * @param s The input String.
     * @return The output String.
     **/
    public static String escape(String s){
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                //.replace("\'", "\\'")      // <== not necessary
                .replace("\"", "\\\"");
    }

    /**either empty, or has space at the end*/
    static String createModsStr(int mods,boolean synthetic,
                                boolean isClass,boolean addFinal,
                                boolean isMethod) {

        String ret = "";

        if(synthetic)                               ret+="/*synthetic*/ ";
        if(Modifier.isPrivate(mods))                ret+="private ";
        if(Modifier.isProtected(mods))              ret+="protected ";
        if(Modifier.isPublic(mods))                 ret+="public ";
        if(Modifier.isStatic(mods)) {
            if(isClass)
                ret+="/*static*/ ";
            else
                ret+="static ";
        }
        if(addFinal && Modifier.isFinal(mods))      ret+="final ";
        if(Modifier.isTransient(mods))              ret+="transient ";
        if(Modifier.isStrict(mods))                 ret+="strict ";
        if(Modifier.isSynchronized(mods))           ret+="synchronized ";
        if(Modifier.isVolatile(mods)) {

            if(isMethod || isClass)
                ret+="/*volatile*/ ";
            else
                ret+="volatile ";
        }
        if(Modifier.isAbstract(mods))               ret+="abstract ";
        if(Modifier.isNative(mods))                 ret+="native ";
        if(!isClass && Modifier.isInterface(mods))  ret+="/*interface*/ ";

        return ret;
    }

    public static String genFileStubs(Class<?> c) {

        StringBuilder ret = new StringBuilder();

        if(c.getPackage()!=null && !c.getPackage().getName().isEmpty())
        {

            ret.append("package ").append(safeName(c.getPackage().getName())).append(";\n");
        }

        ret.append("\n");
        ret.append("/*Stubclass*/\n");


        //TODO: imports

        boolean isEnum = c.isEnum();
        int mods = c.getModifiers();
        String type = "enum";

        if(!isEnum) {
            if(Modifier.isInterface(mods))
                type = "interface";
            else
                type="class";
        }

        String classShortName=c.getName();

        if(classShortName.indexOf('.')!=-1)
            classShortName = classShortName.substring(classShortName.indexOf('.')+1);

        ret.append(createModsStr(mods, c.isSynthetic(), !isEnum,!isEnum,false)).append(type).append(" ").append(safeName(classShortName)).append(" {");

        //tab count
        int s = 1;


        ret.append(tb(s));

        if(isEnum)
            ret.append("; //this is cuz enums are in field form already");

        /*
        * Order (local)
        *
        * Fields
        * Synthetic fields
        *
        * Methods
        * Synthetic methods
        *
        * */

        //Fields
        {
            /*
            * Field type order
            *
            *
            * Stat final
            *
            * Stat
            *
            * Final
            *
            * Other
            *
            * (type order)
            * Pub
            * Prot
            * Default
            * Priv
            *
            * */

            List<Map<String,Field>> syntheticNFieldTypeNAccess2Names
                    = new ArrayList<>(2*4*4);
            for (int i = 0; i < 32; i++) {
                syntheticNFieldTypeNAccess2Names.add(new TreeMap<String,Field>());
            }

            for (Field f : c.getDeclaredFields()) {

                byte fType = 3;
                byte access = 0;

                int fMods = f.getModifiers();

                if(Modifier.isFinal(fMods))
                    fType-=1;
                if(Modifier.isStatic(fMods))
                    fType-=2;

                if(!Modifier.isPublic(fMods))
                {
                    if(Modifier.isProtected(fMods))
                        access = 1;
                    else if(Modifier.isPrivate(fMods))
                        access = 3;
                    else
                        access = 2;
                }

                syntheticNFieldTypeNAccess2Names.get((fType<<2) |(f.isSynthetic()?0:(1<<4)) | access).put(safeName(f.getName()),f);

            }


            boolean spaceOut = false;

            for (Map<String,Field> fields : syntheticNFieldTypeNAccess2Names) {

                for (Map.Entry<String,Field> e : fields.entrySet()) {
                    Field value = e.getValue();

                    ret.append(tb(s)).append(createModsStr(value.getModifiers(), value.isSynthetic(),false,true,false)).append(safeName(value.getType().getName())).append(' ').append(e.getKey());

                    //final, or a static non-object/string
                    if(Modifier.isFinal(value.getModifiers())
                            || (
                                Modifier.isStatic(value.getModifiers()) &&
                            (!(value.getType().isAssignableFrom(Object.class))
                                    || value.getType().isAssignableFrom(String.class))
                            ))
                        ret.append(" = ").append(getValidFieldValueStr(value));

                    ret.append(';');
                    spaceOut = true;
                }

                if(spaceOut)
                    ret.append(tb(s));
                spaceOut = false;

            }

        }

        //Methods


        {
            /*
             * Method order
             *
             * Static
             * Normal
             *
             * */


            List<Map<String, Method>> syntheticNStaticNAccess2Names
                    = new ArrayList<>(2*2*4);

            for (int i = 0; i < 16; i++) {
                syntheticNStaticNAccess2Names.add(new TreeMap<String,Method>());
            }

            for (Method m : c.getDeclaredMethods()) {

                String name = m.getName();

                if(isEnum) {

                    if(name.equals("values")
                    || name.equals("valueOf"))
                        continue;
                }

                byte fType = 0;
                byte access = 0;

                int fMods = m.getModifiers();

                if(Modifier.isStatic(fMods))
                    fType+=1;

                if(!Modifier.isPublic(fMods))
                {
                    if(Modifier.isProtected(fMods))
                        access = 1;
                    else if(Modifier.isPrivate(fMods))
                        access = 3;
                    else
                        access = 2;
                }

                syntheticNStaticNAccess2Names.get((fType<<2) |(m.isSynthetic()?0:(1<<3)) | access).put(safeName(name),m);

            }


            boolean spaceOut = false;

            for (Map<String,Method> methods : syntheticNStaticNAccess2Names) {

                for (Map.Entry<String,Method> e : methods.entrySet()) {
                    Method value = e.getValue();

                    int modifiers = value.getModifiers();

                    ret.append(tb(s))
                            .append(createModsStr(modifiers, value.isSynthetic(),false,true,true))
                            .append(safeName(value.getReturnType().getName()))
                            .append(' ').append(e.getKey()).append("(");

                    int i = 0;
                    Class<?>[] params = value.getParameterTypes();
                    for (Class<?> param : params) {

                        ret.append(safeName(param.getName())).append(" p").append(i++);
                        if(i<params.length)
                            ret.append(", ");

                    }
                    //if(params.length!=0) //remove comma
                    //    ret.deleteCharAt(ret.length()-1);

                    ret.append(")");
                    if(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
                        ret.append(';');
                    }
                    else {
                        ret.append(" {");

                        if(value.getReturnType().isAssignableFrom(void.class))
                            ret.append('}');
                        else {
                            s++;
                            ret.append(tb(s)).append("return ").append(getValidValueStr(value.getReturnType())).append(';');
                            s--;
                            ret.append(tb(s)).append("}");
                        }
                    }

                    spaceOut = true;
                }


                if(spaceOut)
                    ret.append(tb(s));
                spaceOut = false;

            }

        }



        ret.append("\n}\n");

        return ret.toString();

    }

    public static String safeName(String name) {


        boolean array = name.startsWith("[");

        if(array) {
            //decode it


            //remove array thing
            name = name.substring(1);

            switch (name.charAt(0)) {
                case 'B':
                    name="byte";
                    break;
                case 'C':
                    name="char";
                    break;
                case 'D':
                    name="double";
                    break;
                case 'F':
                    name="float";
                    break;
                case 'I':
                    name="int";
                    break;
                case 'J':
                    name="long";
                    break;
                case 'S':
                    name="short";
                    break;
                case 'Z':
                    name="boolean";
                    break;
                case 'L':
                    name=name.substring(1,name.length()-1);//remove L,;
                    name = name.replace('/','.');
                    break;
            }
        }

        switch (name) {
            case "if":
                name= "$WasInvalid$if";
                break;
            case "do":
                name= "$WasInvalid$do";
                break;
            case "for":
                name= "$WasInvalid$for";
                break;
            case "new":
                name= "$WasInvalid$new";
                break;
            case "try":
                name= "$WasInvalid$try";
                break;
        }

        if(array)
            return name+"[]";

        return name;
    }

    private static String getValidFieldValueStr(Field f) {

        Class<?> type = f.getType();
        f.setAccessible(true);

        if(Modifier.isStatic(f.getModifiers())) {
            try {
                String v = f.get(null).toString();
                if(type==byte.class)
                    return v;
                else if(type==short.class)
                    return v;
                else if(type==int.class)
                    return v;
                else if(type==long.class)
                    return v;
                else if(type==boolean.class)
                    return v;
                else if(type==String.class)
                    return "\""+escape(v)+"\"";
            } catch (Exception ignored) {
            }
        }


        return getValidValueStr(type);
    }
    private static String getValidValueStr(Class<?> type) {

        if(type==byte.class)
            return "0";
        else if(type==short.class)
            return "0";
        else if(type==int.class)
            return "0";
        else if(type==long.class)
            return "0";
        else if(type==boolean.class)
            return "false";
        else if(type==String.class)
            return "\"\"";

        return "null";
    }

    private static String tb(int tabs) {
        final StringBuffer outputBuffer = new StringBuffer(tabs+1);
        outputBuffer.append('\n');
        for (int i = 0; i < tabs; i++){
            outputBuffer.append('\t');
        }
        return outputBuffer.toString();
    }
}
