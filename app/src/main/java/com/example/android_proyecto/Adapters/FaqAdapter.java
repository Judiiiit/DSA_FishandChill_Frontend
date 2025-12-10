package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.Faq;
import com.example.android_proyecto.R;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private List<Faq> faqs;

    public FaqAdapter(List<Faq> faqs) {
        this.faqs = faqs;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        Faq faq = faqs.get(position);
        holder.tvQuestion.setText(faq.getQuestion());
        holder.tvAnswer.setText(faq.getAnswer());
    }

    @Override
    public int getItemCount() {
        return faqs != null ? faqs.size() : 0;
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuestion;
        TextView tvAnswer;

        ImageView imgArrow;


        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            imgArrow = itemView.findViewById(R.id.imgArrow);
            imgArrow.setOnClickListener(v -> {

                if (tvAnswer.getVisibility() == View.GONE) {
                    tvAnswer.setVisibility(View.VISIBLE);
                    imgArrow.setImageResource(android.R.drawable.arrow_up_float);
                }
                else {
                    tvAnswer.setVisibility(View.GONE);
                }
            });
        }
    }
}
