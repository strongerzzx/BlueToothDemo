package com.example.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public
/**
 * 作者：zzx on 2020/11/9 14:39
 *  作用： xxxx
 */
class BlueRvAdapter extends RecyclerView.Adapter<BlueRvAdapter.InnerViewHolder> {

    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private onBlueDeviceClickListener mOnBlueDeviceClickListener;

    @NonNull
    @Override
    public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.blue_item_view, parent, false);
        return new InnerViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
        BluetoothDevice device = mDeviceList.get(position);
        holder.name.setText("设备名:"+device.getName());
        holder.address.setText("地址:"+device.getAddress());

        if (mOnBlueDeviceClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnBlueDeviceClickListener.onBlueDeviceClick(device);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDeviceList==null?0:mDeviceList.size();
    }

    public void setData(List<BluetoothDevice> deviceList) {
        mDeviceList.clear();
        if (deviceList != null) {
            mDeviceList.addAll(deviceList);
        }
        notifyDataSetChanged();
    }

    public class InnerViewHolder extends RecyclerView.ViewHolder {
        private TextView name ;
        private TextView address;
        public InnerViewHolder(@NonNull View itemView) {
            super(itemView);
            name= itemView.findViewById(R.id.blue_name);
            address =itemView.findViewById(R.id.blue_address);
        }
    }

    public void setOnBlueDeviceClickListener(onBlueDeviceClickListener onBlueDeviceClickListener) {
        mOnBlueDeviceClickListener = onBlueDeviceClickListener;
    }

    public interface onBlueDeviceClickListener{
        void onBlueDeviceClick(BluetoothDevice device);
    }
}

