package com.example.RoboboHomeAssistantApp;

/*
En este módulo se establece el tipo de datos que maneja el RESTful API de Home Assistant.
Existen 6 posibilidades en total para llamar, sin embargo en esta implementación se utilizan únicamente...
... el estado y el ID de cada dispositivo.
 */

public class Post {

    private String entity_id;

    private String state;

   /* private String attributes;

    private String last_changed;

    private String last_updated;

    private String context;*/

    public Post(String entity_id) {
        this.entity_id = entity_id;
    }

    public String getEntity_id() {
        return entity_id;
    }

    public String getState() {
        return state;
    }

    /*public String getAttributes() {
        return attributes;
    }

    public String getLast_changed() {
        return last_changed;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public String getContext() {
        return context;
    }*/
}