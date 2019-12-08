package com.rd.android.floatingcamera;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter{
    ArrayList<String> list;
    String white_balance;

    CustomAdapter(ArrayList<String> list, String white_balance)
    {
        this.list = list;
        this.white_balance = white_balance;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view==null)
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item,viewGroup,false);

        TextView textView = (TextView)view.findViewById(R.id.list_item_text);
        textView.setText(list.get(i));
        if (list.get(i).equals(white_balance))
            view.setBackgroundColor(Color.parseColor("#555555"));

        if (i == 0 && !list.get(i).equals(white_balance))
            view.setBackgroundColor(Color.parseColor("#777777"));

        return view;
    }
}
