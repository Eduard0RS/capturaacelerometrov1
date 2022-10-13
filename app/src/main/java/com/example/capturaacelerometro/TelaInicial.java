package com.example.capturaacelerometro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TelaInicial extends AppCompatActivity {
        public static EditText nome;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tela_inicial);
            getSupportActionBar().hide();

            nome =(EditText) findViewById(R.id.txtum);

            final Button botaook = findViewById(R.id.botaook);

            botaook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println(nome.getText());
                    startActivity(new Intent(TelaInicial.this, MainActivity.class));

                }
            });
        }
    }
