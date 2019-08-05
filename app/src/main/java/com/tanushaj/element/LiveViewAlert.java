package com.tanushaj.element;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanushaj.element.models.QuoteDto;

public class LiveViewAlert {

    Dialog dialog;
    View stressBackground;
        TextView stressText;

    public void showDialog(final Activity activity){


        LocalBroadcastManager.getInstance(activity).registerReceiver(messageRec,
                new IntentFilter("stress_detection_event"));

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.live_view_alert);

        stressBackground = dialog.findViewById(R.id.stress_background);
        stressText = dialog.findViewById(R.id.stress_text);
        Button disconnect = dialog.findViewById(R.id.disconnectBtn);
        Button connect = dialog.findViewById(R.id.connectBtn);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("disconnect_event");
                intent.putExtra("message", 2);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("disconnect_event");
                intent.putExtra("message", 1);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }
        });
//        quoteTv.setText(quote.getQuotation());
//        authorTv.setText(quote.getAuthor());

        Button dialogButton = dialog.findViewById(R.id.dismiss);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(messageRec);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private BroadcastReceiver messageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int stress = intent.getIntExtra("message", -1);
            if(stress > 0){
                stressBackground.setBackgroundColor(Color.RED);
                stressText.setText("Stressed");
            }else{
                stressBackground.setBackgroundColor(Color.GREEN);
                stressText.setText("Not Stressed");
            }
        }
    };

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.disconnectBtn:
//                break;
//            case R.id.connectBtn:
//                break;
//            case R.id.dismiss:
//                break;
//
//        }
//    }
}
