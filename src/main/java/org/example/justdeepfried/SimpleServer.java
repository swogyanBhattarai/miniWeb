package org.example.justdeepfried;

import com.sun.net.httpserver.HttpServer;
import org.example.justdeepfried.annotations.PathParam;
import org.example.justdeepfried.dto.Records;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.*;

public class SimpleServer {

    public static void startServer(Loader loader) {
        final int port = 8080;
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", exchange -> {
                OutputStream responseBody = exchange.getResponseBody();

                String path = exchange.getRequestURI().toString();

                List<Records> routes = loader.getRoute(exchange.getRequestMethod());

                Records matching = null;

                String[] pathSplit = path.split("/");

                for (Records records : routes) {
                    String[] recordSplit = records.path().split("/");

                    if (pathSplit.length != recordSplit.length) {
                        continue;
                    }

                    boolean matches = true;
                    for (int i = 1; i < recordSplit.length; i++) {
                        if (!recordSplit[i].startsWith("{") && !recordSplit[i].equals(pathSplit[i])) {
                            matches = false;
                            break;
                        }
                    }

                    if (matches) {
                        matching = records;
                        break;
                    }
                }

                if (matching != null) {
                    Parameter[] parameters = matching.method().getParameters();
                    String[] split = matching.path().split("/");

                    List<Object> args = new ArrayList<>();

                    for (Parameter params : parameters) {
                        PathParam pathParam = params.getAnnotation(PathParam.class);

                        if (pathParam != null) {
                            for (int i = 1; i < split.length; i++) {
                                if (split[i].equals("{" + pathParam.value() + "}")) {
                                    args.add(convertToType(pathSplit[i], params.getType()));
                                }
                            }
                        }
                    }

                    try {
                        Object invoke = matching.method().invoke(matching.classInstance(), args.toArray());

                        if (invoke instanceof String response) {
                            byte[] res = response.getBytes();

                            exchange.sendResponseHeaders(200, res.length);

                            responseBody.write(res);
                        }

                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    byte[] error = "Internal Server Error".getBytes();
                    exchange.sendResponseHeaders(500, error.length);
                    responseBody.write(error);
                }

                responseBody.close();
            });

            server.start();
            System.out.println("Server started on Port: " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object convertToType(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == UUID.class) {
            return UUID.fromString(value);
        }

        throw new RuntimeException("Unsupported datatype!");
    }

}
