package com.rn.apigateway.payload;

public class AuthenticationBody {
    private String id;
    private Iterable<String> roles;




    public AuthenticationBody(String id, Iterable<String> roles) {
        this.id = id;
        this.roles = roles;
    }




    public String getId() {
        return id;
    }

    public Iterable<String> getRoles() {
        return roles;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRoles(Iterable<String> roles) {
        this.roles = roles;
    }
}
