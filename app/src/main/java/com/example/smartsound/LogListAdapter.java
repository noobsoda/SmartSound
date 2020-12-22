package com.example.smartsound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LogListAdapter extends BaseAdapter implements View.OnClickListener  {
    Context context;
    ArrayList<list_item> list_itemArrayList;
    ViewHolder viewHolder;
    class ViewHolder{
        TextView name_textView;
        TextView date_textView;
    }
    public LogListAdapter(Context context, ArrayList<list_item> list_itemArrayList) {
        this.context = context;
        this.list_itemArrayList = list_itemArrayList;
    }



    @Override
    public int getCount() {
        return this.list_itemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return list_itemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.logitem, null);
            viewHolder = new ViewHolder();
            viewHolder.name_textView = (TextView)convertView.findViewById(R.id.txt_logname);
            viewHolder.date_textView = (TextView)convertView.findViewById(R.id.txt_logDate);
            convertView.setTag(viewHolder);

        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.name_textView.setText(list_itemArrayList.get(position).getName());
        viewHolder.date_textView.setText(list_itemArrayList.get(position).getWrite_date());

        return convertView;
    }
    public void onClick(View v){

    }

}


