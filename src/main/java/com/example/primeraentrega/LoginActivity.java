package com.example.primeraentrega;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    private static final String SERVER_URL = "http://10.0.2.2/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        usernameEditText = findViewById(R.id.edit_text_username);
        passwordEditText = findViewById(R.id.edit_text_password);
        loginButton = findViewById(R.id.button_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Envía las credenciales al servidor
                loginUser(username, password);
            }
        });
    }

    private void loginUser(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construye la URL con los parámetros de usuario y contraseña
                    String urlParameters = "username=" + URLEncoder.encode(username, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8");

                    // Crea la conexión HTTP y establece el método y los parámetros
                    URL url = new URL(SERVER_URL + "?" + urlParameters);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Configura el tiempo de espera (timeout) en milisegundos
                    connection.setConnectTimeout(5000); // 5 segundos

                    // Lee la respuesta del servidor
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    final StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Procesa la respuesta en el hilo principal
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //Tratar el json

                            JSONObject jsonResponse = null;
                            String status = null;
                            try {
                                jsonResponse = new JSONObject(response.toString());
                                status = jsonResponse.getString("status");

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }


                            if (status.equals("success")) {
                                // Inicio de sesión exitoso
                                Toast.makeText(LoginActivity.this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Inicio de sesión fallido
                                Toast.makeText(LoginActivity.this, "Nombre de usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // Manejar la excepción aquí (por ejemplo, mostrar un mensaje de error)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    //Abrir pantalla de registro
    public void openRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

}
