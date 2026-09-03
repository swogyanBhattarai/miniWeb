package org.example.justdeepfried.service;

import org.example.justdeepfried.annotations.API;
import org.example.justdeepfried.annotations.GET;
import org.example.justdeepfried.annotations.PathParam;

@API("/user")
public class UserService {

    @GET
    public String getName() {
        return "Swogyan Bhattarai";
    }

    @GET("/new-path/{id}")
    public String returnSomething2(@PathParam("id") int id) {
        return "This is " + id;
    }

    @GET("/new-path")
    public String returnSomething3() {
        return "This is somewthing";
    }
}
