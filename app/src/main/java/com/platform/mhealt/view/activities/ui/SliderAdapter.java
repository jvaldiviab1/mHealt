package com.platform.mhealt.view.activities.ui;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.platform.mhealt.R;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.rest,
    };

    public String[] slide_headlines = {
            "RUN",
    };

    public String[] slide_texts = {
            "Comience su entrenamiento.\n"+"El aplicativo registrara su actividad fisica en el transcurso de 5 MIN \n de su menor esfuerzo."
    };

    @Override
    public int getCount() {
        return slide_headlines.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = (ImageView)view.findViewById(R.id.slideImageID);
        TextView slideHeadlineView = (TextView)view.findViewById(R.id.slideHeadlineID);
        TextView slideTextView = (TextView)view.findViewById(R.id.slideTextID);

        slideImageView.setImageResource(slide_images[position]);
        slideHeadlineView.setText(slide_headlines[position]);
        slideTextView.setText(slide_texts[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}
