package com.example.RoboboHomeAssistantApp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView textViewResult;
    String device,IP,devType,token;

    // Valores del cuadro donde se asigna el texto del entity ID y la IP de Home Assistant en el app, respectivamente
    EditText devID;
    EditText HA_IP;
    EditText LLToken;

    //Botones de "ok" de los valores de entity ID y IP en la interfaz del app
    Button sendID;
    Button sendIP;
    Button sendToken;

    // Boton para la automatización
    Button switch1;


    private HA_Api ha_Api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se asocian las instancias con su respectivo ID de la intefaz gráfica
        devID = findViewById(R.id.devID);
        HA_IP = findViewById(R.id.HA_IP);
        LLToken = findViewById(R.id.LLToken);
        sendID = findViewById(R.id.sendID);
        sendIP = findViewById(R.id.sendIP);
        sendToken = findViewById(R.id.sendToken);

        textViewResult = findViewById(R.id.text_view_result);
        Button btn_On = findViewById(R.id.button_On);
        Button btn_Off = findViewById(R.id.button_Off);
        Button btn_Read = findViewById(R.id.button_ReadState);
        Button btn_List = findViewById(R.id.button_List);

        switch1 = findViewById(R.id.switch1);


        //Se establecen las acciones por realizar al presionar cada botón.

        sendIP.setOnClickListener(new View.OnClickListener() {
            /*Al presionar el botón de la dirección IP, se crea una instancia de Retrofit, la cual indica el...
            ... url base del cual se pedirá/enviará información.
            La direción ip que se debe colocar debe ser la IP de home assistant. Por ejemplo: 192.168.100.74
            */
            @Override
            public void onClick(View view) {
                IP = HA_IP.getText().toString();
                showToast("http://"+IP+":8123/");
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://"+IP+":8123/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ha_Api = retrofit.create(HA_Api.class);
            }
        });

        sendToken.setOnClickListener(new View.OnClickListener() {
            /*Al presionar el botón del Long Live Token, se almacena el valor provisto por el usuario de la forma que...
            ... Home Assistant lo solicita: "Authorization : Bearer LongLiveToken".
            Este token es alimentado como parámetro en cada una de las funciones de GET y POST.
            */
            @Override
            public void onClick(View view) {
                token = "Bearer "+LLToken.getText().toString();
                showToast(token);
            }
        });

        sendID.setOnClickListener(new View.OnClickListener() {
            /* Al presionar el botón del entity ID, si el nombre contiene "switch", se asigna este como el tipo de ...
            ... dispositivo en el comando POST.
            Esto se debe a que los entity IDs de los dispositivos que pueden ser actuados, siempre comenzarán la forma...
            ... switch.XXXX o light.XXXX
            */
            @Override
            public void onClick(View view) {
                device = devID.getText().toString();
                showToast(device+" connected");
                if (device.contains("switch")){
                    devType="switch";
                }
                else{
                    devType="light";
                }
            }
        });


        btn_Read.setOnClickListener(new View.OnClickListener() {
            /*Al presionar el botón de Read State, se llama a la función de deviceRead para el...
            ... entity ID indicado
            */
            @Override
            public void onClick(View view) {
                deviceRead(device,token);
            }
        });

        btn_Off.setOnClickListener(new View.OnClickListener() {
            /*Al presionar el botón de turn off, se llama la función turnState indicando apagar el ...
            ... dispositivo, junto con el entity ID y el tipo de dispositivo.
            */
            @Override
            public void onClick(View view) {
                turnState("off",device,devType,token);
                textViewResult.setText("Cambio de estado: " +"off"+ "\n");
            }
        });
        btn_On.setOnClickListener(new View.OnClickListener() {
            /*Al presionar el botón de turn on, se llama la función turnState indicando encender el ...
            ... dispositivo, junto con el entity ID y el tipo de dispositivo.
            */
            @Override
            public void onClick(View view) {
                turnState("on",device,devType,token);
                textViewResult.setText("Cambio de estado: " +"on"+ "\n");
            }
        });

        btn_List.setOnClickListener(new View.OnClickListener() {
            /*Al presionar el botón de List Devices, se llama la función deviceStates indicando listar ...
            ... todos los dispositivos disponibles en Home Assistant.
            */
            @Override
            public void onClick(View view) {
                deviceStates(token);
            }
        });

        switch1.setOnClickListener(new View.OnClickListener() {
            /*Al presionar el botón de Automatization, se llama la función automationLight indicando ...
            ... encender el dispositivo escrito en el campo de Entity ID escrito por el usuario, si...
            ... fuera el caso de que se detecta un umbral menor a 6.0 lx por el sensor de detección de luz.
            La automatización correrá por un total de 20 segundos.
            */
            @Override
            public void onClick(View view) {
                Handler handler1 = new Handler();
                int a = 0;
                int total = 20;
                int time_result = total-a;
                while(a<total){
                    int finalA = a;
                    int finalT = total;

                    handler1.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            int time_result;
                            automationLight("on",device,devType,token);
                            time_result=finalT-finalA;

                            textViewResult.setText("\n"+"Tiempo de automatización: "+String.valueOf(time_result) +"\n");
                        }
                    }, 1000*a );
                    a++;

                }
            }
        });
    }

    private void automationLight(String change, String deviceID, String deviceType,String ha_token){
        /* Se encarga de realizar una función automatizada. Inicialmente, se realiza la lectura de un sensor...
        ... predeterminado que deberá llamarse en Home assistant como "sensor.iluminacion". Si este sensor detecta...
        ... un valor menor a un umbral de 6 lx, se hará un llamado a la función turnState() para encender el dispositivo...
        ... que el usuario indique por medio de la interfaz gráfica.
         Parámetros de entrada:
        -change: el estado por establecer, recibirá "on" u "off" dependiendo del botón que se presione en la interfaz.
        -deviceID: es el entity id del dispositivo. Este se indica por medio del input del usuario en la interfaz gráfica.
        -deviceType: tipo de dispositivo sea switch o light. Este nuevamente es extraído a partir del entity ID...
        ...obtenido del input del usuario en la interfaz. Esto se debe a que el URL será distinto dependiendo del tipo ...
        ... de dispositivo.
        -ha_token: long live token.
        */

        Call<Post> call = ha_Api.getPost("sensor.iluminacion",ha_token);
        call.enqueue(new Callback<Post>() {

            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                double i = Double.parseDouble(response.body().getState() );
                if( i < 6){
                    turnState(change,deviceID,deviceType,ha_token);
                }

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                textViewResult.setText("FAIL");
            }
        });

    }



    private void deviceRead(String device2Read,String ha_token){
        /*Se encarga de leer el estado de un dispositivo específico.
        Su parámetro de entrada será el entity ID, el cual es obtenido a partir del valor obtenido ...
        ... de la interfaz gráfica escrito por el usuario, y el Long Live Token.
        */

        Call<Post> call = ha_Api.getPost(device2Read,ha_token);

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                textViewResult.setText("");
                String content = "";
                content += "ID: " + response.body().getEntity_id() + "\n";
                content += "State: " + response.body().getState() + "\n\n";

                textViewResult.append("Información del dispositivo: " + "\n\n");
                textViewResult.append(content);

                if (!response.isSuccessful()) {
                    textViewResult.setText("Code: " + response.code());
                    return;
                }

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                textViewResult.setText("FAIL");
            }
        });
    }

    private void turnState(String change, String deviceID, String deviceType,String ha_token){
        /* Se encarga de enviar un comando a un dispositivo en específico. Solamente es posible actuar sobre...
        ... switches (smart plugs) o luces. Parámetros de entrada:
        -change: el estado por establecer, recibirá "on" u "off" dependiendo del botón que se presione en la interfaz.
        -deviceID: es el entity id del dispositivo. Este se indica por medio del input del usuario en la interfaz gráfica.
        -deviceType: tipo de dispositivo sea switch o light. Este nuevamente es extraído a partir del entity ID...
        ...obtenido del input del usuario en la interfaz. Esto se debe a que el URL será distinto dependiendo del tipo ...
        ... de dispositivo.
        -ha_token: long live token.
        */
        Post post = new Post(deviceID);
        Call<List<Post>> call= ha_Api.createPost(post,deviceType,change,ha_token);

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                //Imprime en pantalla el cambio de estado
                //textViewResult.setText("Cambio de estado: " +change+ "\n");
                if (!response.isSuccessful()) {
                    textViewResult.setText("Code: " + response.code());
                    return;
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                textViewResult.setText(t.getMessage());

            }
        });
    }

    private void deviceStates(String ha_token){
        /* Se encarga de la lectura de todos los dispositivos conectados en Home Assistant. Devuelve tanto el entity ID como su estado correspondiente.
        Recibe en su entrada el long live token provisto por el usuario.
        */
        Call<List<Post>> call = ha_Api.getStates(ha_token);
        //call.enqueue(new Callback<Post>()
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                textViewResult.setText("");
                if (!response.isSuccessful()) {
                    textViewResult.setText("Code: " + response.code());
                    return;
                }

                List<Post> posts = response.body();

                // Recorre la lista obtenida de todos los elementos disponibles
                for (Post post : posts) {
                    String content = "";
                    content += "ID: " + post.getEntity_id() + "\n";
                    content += "State: " + post.getState() + "\n\n";

                    textViewResult.append(content);
                }

            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                textViewResult.setText(t.getMessage());

            }
        });


    }

    private void showToast(String text){
        /* Se encarga de mostrar en pantalla un mensaje deseado por el programador.
        */
        Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
    }
}