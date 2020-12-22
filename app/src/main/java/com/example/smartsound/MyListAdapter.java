package com.example.smartsound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter implements View.OnClickListener  {
    Context context;
    ArrayList<list_item> list_itemArrayList;
    ViewHolder viewHolder;
    class ViewHolder{
        TextView name_textView;
        TextView usetime_textView;
        TextView size_textView;
        TextView date_textView;
        CheckBox checkBox;
    }
    public MyListAdapter(Context context, ArrayList<list_item> list_itemArrayList, ListChkboxClickListener clickListener) {
        this.context = context;
        this.list_itemArrayList = list_itemArrayList;
        this.listChkboxClickListener = clickListener;
    }

    public interface ListChkboxClickListener{
        void onListChkClick(int position);
    }

    private ListChkboxClickListener listChkboxClickListener;


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

        final int pos = position;
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
            viewHolder = new ViewHolder();
            viewHolder.name_textView = (TextView)convertView.findViewById(R.id.txt_name);
            viewHolder.usetime_textView = (TextView)convertView.findViewById(R.id.txt_time);
            viewHolder.size_textView = (TextView)convertView.findViewById(R.id.txt_size);
            viewHolder.date_textView = (TextView)convertView.findViewById(R.id.txt_Date);
            viewHolder.checkBox = convertView.findViewById(R.id.chk_check);
            convertView.setTag(viewHolder);

        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }


        viewHolder.name_textView.setText(list_itemArrayList.get(position).getName());
        viewHolder.usetime_textView.setText(list_itemArrayList.get(position).getUsetime());
        viewHolder.size_textView.setText(list_itemArrayList.get(position).getSize());
        viewHolder.date_textView.setText(list_itemArrayList.get(position).getWrite_date());
        viewHolder.checkBox.setChecked(list_itemArrayList.get(position).isChknum());

        viewHolder.checkBox.setTag(position);
        viewHolder.checkBox.setOnClickListener(this);


        return convertView;
    }
    public void onClick(View v){
        if(this.listChkboxClickListener != null){
            this.listChkboxClickListener.onListChkClick((int)v.getTag());
        }
    }

}

