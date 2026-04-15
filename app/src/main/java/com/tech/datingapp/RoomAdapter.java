package com.tech.datingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    Context context;
    ArrayList<Map<String, Object>> roomList;

    public RoomAdapter(Context context, ArrayList<Map<String, Object>> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room_card, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Map<String, Object> room = roomList.get(position);
        
        String roomName = String.valueOf(room.get("roomName"));
        String roomType = String.valueOf(room.get("roomType"));
        // Safely extract online count (handles both int and long)
        String onlineCount = "1";
        if(room.get("onlineCount") != null) {
            onlineCount = String.valueOf(room.get("onlineCount"));
        }

        holder.tvRoomTitle.setText(roomName);
        holder.tvRoomType.setText(roomType + " Room");
        holder.tvOnlineCount.setText("👥 " + onlineCount + " online");

        // Join button click (Room me bhejna)
        holder.btnJoinDynamic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatroomActivity.class);
                intent.putExtra("ROOM_NAME", roomName);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    // ViewHolder Class
    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomTitle, tvOnlineCount, tvRoomType;
        Button btnJoinDynamic;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvOnlineCount = itemView.findViewById(R.id.tvOnlineCount);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            btnJoinDynamic = itemView.findViewById(R.id.btnJoinDynamic);
        }
    }
}
