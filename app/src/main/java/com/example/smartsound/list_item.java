package com.example.smartsound;

import java.util.Date;

public class list_item {

    //추가한 변수
    private String usetime;
    private String name;
    private String size;
    private String write_date;
    private boolean chknum;
    private String filename;


    public list_item(boolean chknum, String usetime, String name,String size, String write_date, String filename) {
        this.chknum = chknum;
        this.usetime = usetime;
        this.name = name;
        this.size = size;
        this.write_date = write_date;
        this.filename = filename;
    }
    public list_item(String name, String write_date){
        this.name = name;
        this.write_date = write_date;
    }



    public String getUsetime() {
        return usetime;
    }

    public void setUsetime(String usetime) {
        this.usetime = usetime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    public String getWrite_date() {
        return write_date;
    }

    public void setWrite_date(String write_date) {
        this.write_date = write_date;
    }

    public boolean isChknum() {
        return chknum;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}
