package pers.teemo.accompany.view.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;
import java.util.function.Function;

public class SimpleBaseAdapter<T> extends BaseAdapter {
    private int resource;
    private Activity activity;
    private List<T> dataList;
    private Function<T, Long> getItemId;
    private DataConsumer<T> dataConsumer;

    public SimpleBaseAdapter(int resource, Activity activity, List<T> dataList, DataConsumer<T> dataConsumer) {
        this(resource, activity, dataList, null, dataConsumer);
    }

    public SimpleBaseAdapter(int resource, Activity activity, List<T> dataList, Function<T, Long> getItemId, DataConsumer<T> dataConsumer) {
        this.resource = resource;
        this.activity = activity;
        this.dataList = dataList;
        this.getItemId = getItemId;
        this.dataConsumer = dataConsumer;
    }

    @Override

    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItemId != null ? getItemId.apply(dataList.get(position)) : position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(resource, parent, false);
        }
        dataConsumer.accept(this, position, convertView, parent);
        return convertView;
    }

    public int getResource() {
        return resource;
    }

    public Activity getActivity() {
        return activity;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public Function<T, Long> getGetItemId() {
        return getItemId;
    }

    public DataConsumer getDataConsumer() {
        return dataConsumer;
    }

    @FunctionalInterface
    public interface DataConsumer<T> {
        void accept(SimpleBaseAdapter<T> adapter, int position, View convertView, ViewGroup parent);
    }
}
