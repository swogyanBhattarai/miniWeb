package org.example.justdeepfried.service;

import org.example.justdeepfried.annotations.API;
import org.example.justdeepfried.annotations.GET;
import org.example.justdeepfried.annotations.PathParam;

@API("/test")
public class TestService {

    @GET
    public String returnSomething() {
        return "This is getting called";
    }

    @GET("/new-path")
    public String returnSomething2() {
        return "This is wrong";
    }

    @GET("/new-path/{id}")
    public String returnSomething3() {
        return "This is path param";
    }

    @GET("/new-path/{id}/{id2}")
    public String returnSomething4(@PathParam("id2") int id2, @PathParam("id") int id) {
        return "This is path param " + id + " " + id2;
    }
}
