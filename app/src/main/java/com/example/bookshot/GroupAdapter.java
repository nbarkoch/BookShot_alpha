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

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    LayoutInflater inflater;
    List<Book> books;
    public ItemGroupClickListener bookClickListener;

    public GroupAdapter(Context ctx, List<Book> books, ItemGroupClickListener itemGroupClickListener) {
        this.inflater = LayoutInflater.from(ctx);
        this.books = books;
        this.bookClickListener = itemGroupClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(R.layout.multi_list_layout, parent, false);
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
        holder.moreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookClickListener.onMoreInfoClick(v, holder.getAdapterPosition());
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
        Button moreInfoButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthors = itemView.findViewById(R.id.bookAuthor);
            bookImage = itemView.findViewById(R.id.bookImage);
            pageCount = itemView.findViewById(R.id.pageCount);
            publishDate = itemView.findViewById(R.id.publishDate);
            chooseButton = itemView.findViewById(R.id.chooseButton);
            moreInfoButton = itemView.findViewById(R.id.moreInfoButton);
            // handle onClick

        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemGroupClickListener itemGroupClickListener) {
        this.bookClickListener = itemGroupClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemGroupClickListener {
        void onChooseClick(View v, int p);

        void onMoreInfoClick(View v, int p);
    }


}

