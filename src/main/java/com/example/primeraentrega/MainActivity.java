package com.example.primeraentrega;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMovies;
    private MovieAdapter movieAdapter;

    private View dialogView;

    private List<Movie> movieList;

    private ImageView imageView;

    private Uri selectedImageUri;

    private DatabaseHelper databaseHelper;

    private AlertDialog addMovieDialog;

    private EditText movieTitleEditText;

    private RatingBar movieRatingBar;
    private CheckBox movieSeenCheckBox;

    private boolean isDialogOpen = false;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;


    // Creación del intent para la galería
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        // Aquí puedes manejar la URI de la imagen seleccionada
                        selectedImageUri = result;
                        imageView = dialogView.findViewById(R.id.image_view_selected_image);
                        imageView.setImageURI(result);
                    }
                }
            });

    private ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean success) {
                    if (success) {
                        // La foto se tomó correctamente, puedes manejarla aquí
                        // Por ejemplo, mostrar la imagen en tu ImageView
                        imageView.setImageURI(imageUri);
                    } else {
                        // La operación fue cancelada o falló
                    }
                }
            });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewMovies = findViewById(R.id.recycler_view_movies);
        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(this));

        movieList = new ArrayList<>();

        // Inicializar el helper de la base de datos
        databaseHelper = new DatabaseHelper(this);

        //Cargar la lista de películas al abrir la aplicación
        loadMovies();

        movieAdapter = new MovieAdapter(movieList,this);
        movieAdapter.setMainActivity(this);
        recyclerViewMovies.setAdapter(movieAdapter);

        if (savedInstanceState != null) {
            isDialogOpen = savedInstanceState.getBoolean("isDialogOpen", false);
            if (isDialogOpen) {
                dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_movie, null);
                createAddMovieDialog();
            }
        }

        //Crear botón para añadir una película

        Button addButton = findViewById(R.id.btn_add_item);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMovieDialog();
            }
        });

        //Crear botón para eliminar todas las películas

        ImageButton deleteAllButton = findViewById(R.id.btn_clear_database);

        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });



    }

    private void showAddMovieDialog(){
        if (!isDialogOpen) {
            dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_movie, null);
            createAddMovieDialog();
            isDialogOpen = true;
        }
    }

    private void createAddMovieDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = getLayoutInflater();
//        dialogView = inflater.inflate(R.layout.dialog_add_movie, null);
        builder.setView(dialogView);
        selectedImageUri=null;

        // Acciones para añadir la película o no (cancelar)

        builder.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Obtener los datos añadidos por el usuario y agregar la película a la lista
                movieTitleEditText = dialogView.findViewById(R.id.edit_text_movie_title);
                movieRatingBar = dialogView.findViewById(R.id.rating_bar_movie_rating);
                movieSeenCheckBox = dialogView.findViewById(R.id.check_box_movie_seen);


                String title = movieTitleEditText.getText().toString();

                if (title.isEmpty()) {
                    // Mostrar un mensaje de error indicando que el título es obligatorio
                    showAlert("Error", "El título es obligatorio.");
                    return; // Salir del método sin insertar la película
                }

                float rating = movieRatingBar.getRating();
                boolean seen = movieSeenCheckBox.isChecked();

                if (!isMovieAlreadyExist(title)) {

                    Movie newMovie = new Movie(title, rating, seen, null, null);

                    //Añadir película a la BD
                    addMovieToDatabase(newMovie);

                    // Mostrar diálogo de alerta de película añadida
                    showAlert("Película agregada", "La película se ha agregado correctamente a la lista.");
                } else {
                    // Mostrar diálogo de alerta de película duplicada
                    showAlert("Película duplicada", "La película ya está en la lista.");
                }

                rating=0;
                seen=false;


            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        //Botón de seleccionar imagen
        Button selectImageButton = dialogView.findViewById(R.id.button_select_image);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_select_image, null);
                builder.setView(dialogView);

                Button takePhotoButton = dialogView.findViewById(R.id.button_take_photo);
                Button chooseFromGalleryButton = dialogView.findViewById(R.id.button_choose_from_gallery);

                takePhotoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                    }
                });

                chooseFromGalleryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openGallery();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });





        addMovieDialog = builder.create();
        addMovieDialog.setCancelable(false);

        addMovieDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogOpen = false; // Marcar el diálogo como cerrado
            }
        });


        addMovieDialog.show();

    }


    private void openGallery() {
        //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch("image/*");

    }

    //Método para crear instancia del DatabaseHelper y añadir la película a la BD
    private void addMovieToDatabase(Movie movie) {
        //Guardar imagen en el almacenamiento externo y conseguir su URI
        String imagePath = "";
        if (selectedImageUri != null) {
            Bitmap imageBitmap;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imagePath = ImageStorageHelper.saveImageToExternalStorage(this, imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        movie.setImageUri(Uri.parse(imagePath));
        databaseHelper.addMovie(movie);

        //Añadir película a la lista
        //movieList.add(movie);

        loadMovies();
        selectedImageUri=null;
        // Notifica al adaptador que se ha añadido un nuevo elemento, para actualizar vista al momento
        movieAdapter.notifyDataSetChanged();
    }

    //Método para comprobar si la película existe en la BD
    private boolean isMovieAlreadyExist(String title) {
        for (Movie movie : movieList) {
            if (movie.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    // Método para mostrar una diálogo de alerta
    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para enviar una notificación local
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    protected void sendNotification(String titulo) {
        // Verificar y solicitar permisos
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar el permiso
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }

        // Crear NotificationManager y NotificationChannel si es necesario
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            // Configurar el canal...
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Configurar las características de la notificación...
        notificationBuilder.setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("Nueva película vista!")
                .setContentText("Has visto: " + titulo)
                .setSubText("Información extra")
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true);

        // Lanzar la notificación...
        notificationManager.notify(1, notificationBuilder.build());
    }

    private void loadMovies() {
        // Limpiar la lista antes de cargar las películas para evitar duplicados
        movieList.clear();

        //Obtiene todas las películas de la BD y las añade a la lista
        movieList.addAll(databaseHelper.getAllMovies());

        //Carga las imagenes de la BD en forma de Bitmap
        for (Movie movie : movieList) {
            Uri imageUri = movie.getImageUri();
            if (imageUri != null) {
                // Cargar la imagen utilizando el ImageStorageHelper
                Bitmap imageBitmap = ImageStorageHelper.loadImageFromStorage(imageUri.getPath());
                // Si la imagen se cargó correctamente, actualiza la película con la imagen cargada
                if (imageBitmap != null) {
                    movie.setImageBitmap(imageBitmap);
                }
            }
        }

    }

    // Guardar estado del dialogo
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDialogOpen", isDialogOpen);

        if (dialogView != null) {
            // Guardar otros atributos de la película
            EditText movieTitleEditText = dialogView.findViewById(R.id.edit_text_movie_title);
            String movieTitle = movieTitleEditText.getText().toString();
            outState.putString("movieTitle", movieTitle);

            RatingBar movieRatingBar = dialogView.findViewById(R.id.rating_bar_movie_rating);
            float movieRating = movieRatingBar.getRating();
            outState.putFloat("movieRating", movieRating);

            CheckBox movieSeenCheckBox = dialogView.findViewById(R.id.check_box_movie_seen);
            boolean movieSeen = movieSeenCheckBox.isChecked();
            outState.putBoolean("movieSeen", movieSeen);

            outState.putParcelable("selectedImageUri", selectedImageUri);
        }
    }

    // Recuperar estado del dialogo
    @SuppressLint("InflateParams")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("isDialogOpen", false)) {

            selectedImageUri = savedInstanceState.getParcelable("selectedImageUri");

            // Restaurar la imagen seleccionada en el ImageView
            if (isDialogOpen) {
                dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_movie, null);
                if (selectedImageUri != null) {
                    imageView = dialogView.findViewById(R.id.image_view_selected_image);
                    imageView.setImageURI(selectedImageUri);
                }

                // Restaurar otros atributos de la película
                String movieTitle = savedInstanceState.getString("movieTitle");
                EditText movieTitleEditText = dialogView.findViewById(R.id.edit_text_movie_title);
                movieTitleEditText.setText(movieTitle);

                float movieRating = savedInstanceState.getFloat("movieRating");
                RatingBar movieRatingBar = dialogView.findViewById(R.id.rating_bar_movie_rating);
                movieRatingBar.setRating(movieRating);

                boolean movieSeen = savedInstanceState.getBoolean("movieSeen");
                CheckBox movieSeenCheckBox = dialogView.findViewById(R.id.check_box_movie_seen);
                movieSeenCheckBox.setChecked(movieSeen);

                createAddMovieDialog();
            }
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Estás seguro de borrar toda la lista?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Llamar al método para borrar todas las películas
                        databaseHelper.deleteAllMovies();
                        // Limpiar la lista y notificar al adaptador
                        movieList.clear();
                        movieAdapter.notifyDataSetChanged();

                        // Mostrar diálogo de alerta de película añadida
                        showAlert("Lista eliminada", "Las películas se han eliminado de la lista");

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crear un archivo donde se guardará la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Manejar el error
            }
            // Continuar solo si el archivo se creó correctamente
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                cameraLauncher.launch(imageUri);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Crear un nombre de archivo único
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // La imagen se capturó correctamente, puedes guardarla
            saveImageToStorage();
        }
    }

    private void saveImageToStorage() {
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
               //guardar en BD remota (phpmyadmin)
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }






}
