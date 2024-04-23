package com.example.primeraentrega;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre de la base de datos
    private static final String DATABASE_NAME = "movie_database";

    // Versión de la base de datos
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla de películas
    private static final String TABLE_MOVIES = "movies";

    // Nombre de las columnas de la tabla de películas
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_SEEN = "seen";
    private static final String COLUMN_IMAGE_URI = "image_uri";

    // Consulta SQL para crear la tabla de películas
    private static final String CREATE_TABLE_MOVIES = "CREATE TABLE " + TABLE_MOVIES + "("
            //+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TITLE + " TEXT PRIMARY KEY,"
            + COLUMN_RATING + " REAL,"
            + COLUMN_SEEN + " INTEGER,"
            + COLUMN_IMAGE_URI + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de películas cuando se crea la base de datos por primera vez
        db.execSQL(CREATE_TABLE_MOVIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Manejar la actualización de la base de datos si es necesario
        // Aquí puedes implementar la lógica para migrar los datos de la versión antigua a la nueva
    }

    // Método para agregar una nueva película a la base de datos
    public long addMovie(Movie movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_RATING, movie.getRating());
        values.put(COLUMN_SEEN, movie.isViewed() ? 1 : 0);
        values.put(COLUMN_IMAGE_URI, movie.getImageUri().toString());
        long id = db.insert(TABLE_MOVIES, null, values);
        db.close();
        return id;
    }

    // Método para obtener todas las películas de la base de datos
    @SuppressLint("Range")
    public List<Movie> getAllMovies() {
        List<Movie> movieList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MOVIES, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Movie movie = new Movie(null,0,false,null, null);
                //movie.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                movie.setRating(cursor.getFloat(cursor.getColumnIndex(COLUMN_RATING)));
                movie.setViewed(cursor.getInt(cursor.getColumnIndex(COLUMN_SEEN)) == 1);
                movie.setImageUri(Uri.parse(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI))));
                movieList.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return movieList;
    }

    // Método para actualizar una película en la base de datos
    public int updateMovie(Movie movie,String originalTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_RATING, movie.getRating());
        values.put(COLUMN_SEEN, movie.isViewed() ? 1 : 0);
        values.put(COLUMN_IMAGE_URI, movie.getImageUri().toString());
        int rowsAffected = db.update(TABLE_MOVIES, values, COLUMN_TITLE + " = ?",
                new String[]{String.valueOf(originalTitle)});
        db.close();
        return rowsAffected;
    }

    // Método para eliminar todas las películas de la base de datos
    public void deleteAllMovies() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("movies", null, null);
        db.close();
    }

}
