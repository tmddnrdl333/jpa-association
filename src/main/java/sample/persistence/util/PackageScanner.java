package sample.persistence.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PackageScanner {

    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final char PACKAGE_DELIMITER = '.';


    public static List<Class<?>> scan(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (isNotExists(directory)) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            addClass(packageName, classes, file);
        }

        return classes;
    }

    private static void addClass(String packageName, List<Class<?>> classes, File file) throws ClassNotFoundException {
        if (file.isDirectory()) {
            classes.addAll(findClasses(file, packageName + "." + file.getName()));
            return;
        }

        if (file.getName().endsWith(CLASS_FILE_SUFFIX)) {
            String className = packageName + PACKAGE_DELIMITER + file.getName().substring(0, file.getName().length() - CLASS_FILE_SUFFIX.length());
            classes.add(Class.forName(className));
            return;
        }
    }

    private static boolean isNotExists(File directory) {
        return !directory.exists();
    }

}
