package cn.xiaomayo.notebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainWindowList_Adapter extends BaseAdapter {

    private List<MainWindowListCell> data;
    private Context context;

    MainWindowList_Adapter(Context context){
        this.context = context;
        data = new ArrayList<MainWindowListCell>();
    }

    public void add(MainWindowListCell celldata){
        data.add(celldata);
        notifyDataSetChanged();
    }


    public void add_front(MainWindowListCell celldata){
        data.add(0,celldata);
    }


    public void remove(int i){
        data.remove(i);

    }

    public void sort_(){
        Collections.sort(data, new Comparator<MainWindowListCell>() {
            @Override
            public int compare(MainWindowListCell o1, MainWindowListCell o2) {
                int res = o1.getTime().compareTo(o2.getTime());
                if(res<0) return 1;
                else return -1;
            }
        });


    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public MainWindowListCell getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) view = LayoutInflater.from(context).inflate(R.layout.mainwin_grid_cell,null);
        MainWindowListCell celldata =getItem(i);
        TextView title,date;
        title = view.findViewById(R.id.title);
        date = view.findViewById(R.id.date);
        title.setText(celldata.getTitle());
        date.setText(celldata.getTime());

        return view;


    }
}
