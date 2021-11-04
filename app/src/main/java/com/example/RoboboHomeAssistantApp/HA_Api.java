package com.example.RoboboHomeAssistantApp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/*
Esta clase se encarga de crear los métodos de llamada (GET) y publicación (POST).
-El primer método GET se encarga de la lectura de un único dispositivo. El valor de {dev2Read} se asigna como parámetro en la función..
...  deviceRead en MainActivity.java, y debe ser el entity ID de un dispositivo en Home Assistant.
- El segundo método de POST se encarga de encender o apagar un dispositivo, luz o smart plug. {devType} y {state} se asignan en la función...
... turnState en MainActivity.java, y uno será "on"/"off" dependiendo del cambio deseado, y el otro será "switch"/"light" dependiendo...
... del tipo de dispositivo por actuar.
-El tercer método de GET se encarga de la lectura de la lista de todos los dispositivos registrados en su instancia de Home Assistant..
- Todos los métodos tienen como argumento un header que es ingresado en la aplicación por el usuario, "Authorization", referente a ...
... al Long Live Token que se obtiene en Home Assistant y permite la comunicación con la RESTful API.
*/

public interface HA_Api {
    @Headers("Content-Type: application/json")
    @GET("api/states/{dev2Read}")
    Call<Post> getPost(@Path("dev2Read") String deviceID,
                       @Header("Authorization") String ha_Token);

    @Headers("Content-Type: application/json")
    @POST("api/services/{devType}/turn_{state}")
    Call<List<Post>> createPost(@Body Post post,
                                @Path("devType") String devType,
                                @Path("state") String onOff,
                                @Header("Authorization") String ha_Token);

    @Headers("Content-Type: application/json")
    @GET("api/states")
    Call<List<Post>> getStates(@Header("Authorization") String ha_Token);
}
