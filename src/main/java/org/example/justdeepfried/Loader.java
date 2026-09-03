package org.example.justdeepfried;

import org.example.justdeepfried.annotations.*;
import org.example.justdeepfried.dto.Records;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Loader {

    private final List<Class<? extends Annotation>> httpAnnotations = List.of(
            GET.class,
            POST.class,
            PUT.class,
            DELETE.class
    );

    private final Map<String, List<Records>> requestMapping = new HashMap<>();

    private final Set<String> ApiMappings = new HashSet<>();

    public void scanClasses(String packageName) throws IOException, URISyntaxException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        String packagePath = packageName.replace(".", "/");

        Enumeration<URL> resources = loader.getResources(packagePath);

        List<String> classPaths = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            if (url.getProtocol().equals("file")) {
                Path path = Path.of(url.toURI());

                try (Stream<Path> walk = Files.walk(path)) {
                    walk.filter(f -> f.toString().endsWith(".class"))
                            .forEach(f -> {
                                Path relativize = path.relativize(f);
                                String relativeStr = relativize.toString();
                                classPaths.add(packageName + "." + relativeStr.substring(0, relativeStr.length() - 6).replace(File.separator, "."));
                            });
                }
            }
        }

        registerRoutes(classPaths, loader);
    }

    public void registerRoutes(List<String> classPaths, ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (String path : classPaths) {
            Class<?> clazz = loader.loadClass(path);

            API api = clazz.getAnnotation(API.class);

            if (api != null) {

                if (ApiMappings.contains(api.value())) {
                    throw new RuntimeException("Multiple classes cannot have the same API mapping: " + clazz.getSimpleName());
                } else {
                    ApiMappings.add(api.value());
                }

                Object instance = clazz.getDeclaredConstructor().newInstance();

                Method[] methods = clazz.getDeclaredMethods();

                for (Method method : methods) {
                    Annotation existing = null;
                    for (Class<? extends Annotation> http : httpAnnotations) {

                        Annotation annotation = method.getAnnotation(http);

                        if (annotation != null) {
                            if (existing != null) {
                                throw new RuntimeException("Method cannot have multiple HTTP annotations: " + method);
                            }
                            existing = annotation;
                        }
                    }

                    if (existing != null) {
                        String key = existing.annotationType().getSimpleName();
                        List<Records> records = requestMapping.computeIfAbsent(key, r -> new ArrayList<>());

                        String route = api.value() + getPath(existing);

                        records.add(new Records(instance, method, route));

                        System.out.println("Key being put in the request mapping: " + key + "\tRoute being put: " + route);
                    }
                }
            }
        }
    }

    private String getPath(Annotation annotation) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = annotation.annotationType().getDeclaredMethod("value");

        return (String) method.invoke(annotation);
    }

    public List<Records> getRoute(String httpMethod) {
        return requestMapping.getOrDefault(httpMethod, List.of());
    }

    public void start(Class<?> mainClass) {
        String packageName = mainClass.getPackageName();
        try {
            this.scanClasses(packageName);
        } catch (IOException | URISyntaxException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
