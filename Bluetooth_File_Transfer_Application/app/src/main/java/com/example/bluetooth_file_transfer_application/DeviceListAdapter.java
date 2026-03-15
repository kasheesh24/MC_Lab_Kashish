package com.example.bluetooth_file_transfer_application;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private final OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public DeviceListAdapter(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    public void addDevice(BluetoothDevice device) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            notifyItemInserted(deviceList.size() - 1);
        }
    }

    public void clear() {
        deviceList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        try {
            holder.tvName.setText(device.getName() != null ? device.getName() : "Unknown Device");
        } catch (SecurityException e) {
            holder.tvName.setText("Unknown Device (No Permission)");
        }
        holder.tvAddress.setText(device.getAddress());
        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_device_name);
            tvAddress = itemView.findViewById(R.id.tv_device_address);
        }
    }
}
