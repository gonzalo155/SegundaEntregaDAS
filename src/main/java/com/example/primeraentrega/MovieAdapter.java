package com.example.primeraentrega;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private Context context;

    private MainActivity mainActivity;

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public MovieAdapter(List<Movie> movieList, Context context) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        //holder.imageViewMoviePoster.setImageURI(movie.getImageUri());
        holder.imageViewMoviePoster.setImageBitmap(movie.getImageBitmap());
        holder.movieTitleEditText.setText(movie.getTitle());
        holder.ratingBar.setRating(movie.getRating());
        holder.checkBox.setChecked(movie.isViewed());

        holder.movieTitleEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.movieTitleEditText.setFocusableInTouchMode(true); // Hacer enfocable cuando se haga clic
                holder.movieTitleEditText.requestFocus(); // Obtener el foco para mostrar el cursor
            }
        });

        holder.movieTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String originalTitle= movie.getTitle();
                    movie.setTitle(holder.movieTitleEditText.getText().toString());
                    holder.movieTitleEditText.setFocusableInTouchMode(false);
                    updateMovie(movie,originalTitle);
                }
            }
        });

        // Establecer un TextChangedListener en el EditText para actualizar el título en la base de datos en tiempo real
        holder.movieTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesitamos hacer nada aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No necesitamos hacer nada aquí
            }

            @Override
            public void afterTextChanged(Editable s) {
                String originalTitle=movie.getTitle();
                // Actualizar el título de la película y guardar los cambios en la base de datos después de que el texto cambie
                movie.setTitle(s.toString());
                updateMovie(movie,originalTitle);
            }
        });

        holder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                movie.setRating(rating);
                updateMovie(movie,movie.getTitle());
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                movie.setViewed(isChecked);
                updateMovie(movie,movie.getTitle());

                if (isChecked && mainActivity != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mainActivity.sendNotification(movie.getTitle());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewMoviePoster;
        EditText movieTitleEditText;
        RatingBar ratingBar;
        CheckBox checkBox;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewMoviePoster = itemView.findViewById(R.id.image_view_movie_poster);
            movieTitleEditText = itemView.findViewById(R.id.edit_text_movie_title);
            ratingBar = itemView.findViewById(R.id.rating_bar_movie_rating);
            checkBox = itemView.findViewById(R.id.check_box_movie_status);
        }
    }

    private void updateMovie(Movie movie,String originalTitle) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.updateMovie(movie,originalTitle);
    }
}
