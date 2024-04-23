package com.example.primeraentrega;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button registerButton;

    private static final String SERVER_URL = "http://10.0.2.2/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        usernameEditText = findViewById(R.id.edit_text_username_register);
        passwordEditText = findViewById(R.id.edit_text_password_register);
        registerButton = findViewById(R.id.button_register_user);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Verifica si los campos de usuario y contraseña están vacíos
                if (username.isEmpty() || password.isEmpty()) {
                    // Muestra un Toast indicando que ambos campos deben completarse
                    Toast.makeText(RegisterActivity.this, "Por favor, introduce usuario y contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    // Envía las credenciales al servidor para el registro
                    registerUser(username, password);
                }
            }
        });
    }

    private void registerUser(final String username, final String password) {
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
                    handleRegisterResponse(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    // Manejar la excepción aquí (por ejemplo, mostrar un mensaje de error)
                    showErrorMessage("Error de conexión");
                }
            }
        }).start();
    }

    private void handleRegisterResponse(final String response) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (response.equals("Registro exitoso")) {
                    // Registro exitoso
                    Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                    // Puedes redirigir al usuario a la actividad de inicio de sesión o a la actividad principal
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Error en el registro
                    Toast.makeText(RegisterActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showErrorMessage(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
