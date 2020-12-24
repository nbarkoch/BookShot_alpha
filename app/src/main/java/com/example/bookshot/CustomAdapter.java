package com.example.bookshot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    LayoutInflater inflater;
    List<Book> books;
    public ItemClickListener bookClickListener;

    public CustomAdapter(Context ctx, List<Book> books, ItemClickListener itemClickListener) {
        this.inflater = LayoutInflater.from(ctx);
        this.books = books;
        this.bookClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(R.layout.custom_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull ViewHolder holder, int position) {
        // bind the data
        holder.bookTitle.setText(books.get(position).getTitle());
        holder.bookAuthors.setText(books.get(position).getAuthors());
        Picasso.get().load(books.get(position).getBookImage()).into(holder.bookImage);
        holder.pageCount.setText(books.get(position).getPageCount());
        holder.publishDate.setText(books.get(position).getPublishDate());

        holder.chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookClickListener.onChooseClick(v, holder.getAdapterPosition());
            }
        });
        holder.infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookClickListener.onInfoClick(v, holder.getAdapterPosition());
            }
        });
    }


    @Override
    public int getItemCount() {
        return books.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle, bookAuthors, pageCount, publishDate;
        ImageView bookImage;
        Button chooseButton;
        Button infoButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthors = itemView.findViewById(R.id.bookAuthor);
            bookImage = itemView.findViewById(R.id.bookImage);
            pageCount = itemView.findViewById(R.id.pageCount);
            publishDate = itemView.findViewById(R.id.publishDate);
            chooseButton = itemView.findViewById(R.id.chooseButton);
            infoButton = itemView.findViewById(R.id.infoButton);
            // handle onClick

        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.bookClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onChooseClick(View v, int p);

        void onInfoClick(View v, int p);
    }


}
