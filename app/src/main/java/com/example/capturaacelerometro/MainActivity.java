package com.example.capturaacelerometro;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    String id;
    String nome;
    boolean status= false;
    boolean testador = true;
    boolean x=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        nome= String.valueOf(TelaInicial.nome.getText());
        id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void teste(View view) {
                if (testador) {
                    sensorLigado();
                } else {
                    sensorDesligado();
                }
                Log.e("TAG", "onClick: " + testador);
            }
    public void sensorLigado(){
        final Button botaoprincipal = findViewById(R.id.BotaoLigaDesliga);
        final TextView mensagem = findViewById(R.id.mensagem);
        mensagem.setText("Você esta com os sensores ligados.");
        Post("detector");
        botaoprincipal.setText("Desativar");
        Sensor accelerometer;
        SensorManager sensorManager;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        testador = false;
        status=false;
    }
    public void sensorDesligado(){
        final Button botaoprincipal = findViewById(R.id.BotaoLigaDesliga);
        final TextView mensagem = findViewById(R.id.mensagem);
        SensorManager sensorManager;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mensagem.setText("Os sensores estão desligados.");
        botaoprincipal.setText("Ativar");
        off();
        testador = true;
        x = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float sensorX;
        float sensorY;
        float sensorZ;
        sensorX = sensorEvent.values[0];
        sensorY = sensorEvent.values[1];
        sensorZ = sensorEvent.values[2];

        double ATotal = Math.sqrt(Math.pow(sensorX,2) + Math.pow(sensorY,2) + Math.pow(sensorZ,2));
        System.out.println("Teste"+ATotal);
            if (ATotal>40 && x) {
            status=true;
            notification();
            dialog();
            Post("detector");
            x=false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    public void Post(String url) {
        System.out.println("Entrou Post");
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("nome", String.valueOf(nome));
        json.addProperty("status", status);
        System.out.println("Criou JSON");

        Ion.with(this)
                .load("https://fall-protection.herokuapp.com/"+url)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        System.out.println("completou");
                    }
                });
        System.out.println("Finalizou");
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notification(){

        NotificationManager nm=(NotificationManager)  getSystemService(NOTIFICATION_SERVICE);

        //Parte Responsavel por Mostrar Notificação
        final String CHANNEL_ID = "HEADS_UP_NOTIFICATIONS";
        //int number_not= (int) ((int)(Math.random()*1000)-(Math.random()*2));
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Fall-Detection2",
                NotificationManager.IMPORTANCE_HIGH);
        Uri som= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        NotificationCompat.Builder builder =new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setTicker("Teste");
        builder.setContentTitle("Alerta Possivel Queda!!");
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_foreground));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentTitle("Fall-Detection");
        builder.setVibrate(new long[]{1500,1000,1000,1000});
        builder.setSound(som);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText("O Usuario: " +String.valueOf(nome)+ " de id: "+String.valueOf(id)+" pode ter sofrido uma queda."));

        nm.notify(R.drawable.ic_launcher_background,builder.build());
    }
    public void off(){
        Post("sair");
    }
    public void dialog(){
        AlertDialog.Builder msgBox=new AlertDialog.Builder(this);
        msgBox.setTitle("Alerta de Queda!!!>>!!!");
        msgBox.setIcon(android.R.drawable.stat_sys_warning);
        msgBox.setMessage("Olá está tudo bem com você?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Click em Sim", Toast.LENGTH_SHORT).show();
                x = true;
                status = false;
                Post("detector");
            }
        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Click em Não", Toast.LENGTH_SHORT).show();
                status = true;
                x=false;
                Uri number = Uri.parse("tel:192");
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
                off();
                sensorDesligado();
            }
        });
        msgBox.show();
    }

    @Override
    protected void onDestroy() {
        off();
        super.onDestroy();
    }
}



