package com.tanushaj.element;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.tanushaj.element.models.QuoteDto;

public class QuoteAlert {

    public void showDialog(Activity activity, QuoteDto quote){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.quotation_alert);

        TextView quoteTv = dialog.findViewById(R.id.quote_text);
        TextView authorTv = dialog.findViewById(R.id.quote_author);
        quoteTv.setText(quote.getQuotation());
        authorTv.setText(quote.getAuthor());

        Button dialogButton = dialog.findViewById(R.id.dismiss);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
