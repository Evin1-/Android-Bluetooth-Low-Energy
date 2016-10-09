package com.loopcupcakes.examples.bluetoothleexample;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jonathanhavstad on 10/8/16.
 */

public class BluetoothLEAdapter extends RecyclerView.Adapter<BluetoothLEAdapter.ViewHolder> {
    private List<BluetoothDevice> bluetoothData;
    private OnBluetoothItemClickHandler onBluetoothItemClickHandler;

    public BluetoothLEAdapter(List<BluetoothDevice> bluetoothData,
                              OnBluetoothItemClickHandler onBluetoothItemClickHandler) {
        this.bluetoothData = bluetoothData;
        this.onBluetoothItemClickHandler = onBluetoothItemClickHandler;
    }

    public interface OnBluetoothItemClickHandler {
        void onBluetoothItemClicked(int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.bluetooth_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (bluetoothData != null) {
            BluetoothDevice bluetoothDevice = bluetoothData.get(position);
            holder.bluetoothItemTextview
                    .setText(bluetoothDevice.getAddress() + " " + bluetoothDevice.getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BluetoothLEAdapter.this.onBluetoothItemClickHandler != null) {
                        onBluetoothItemClickHandler.onBluetoothItemClicked(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (bluetoothData != null ? bluetoothData.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView bluetoothItemTextview;
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.bluetoothItemTextview =
                    (TextView) itemView.findViewById(R.id.bluetooth_item_textview);
        }
    }
}
