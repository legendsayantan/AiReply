package com.legendsayantan.aireply;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.shrikanthravi.chatview.data.Message;
import com.shrikanthravi.chatview.widget.ChatView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    static ChatView chatView;
    static String pastmsg = "";
    static Activity activity;
    static AsyncTask<Void , Void , Void> asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        activity=this;
        chatView = findViewById(R.id.chatView);
        chatView.setOnClickSendButtonListener(new ChatView.OnClickSendButtonListener() {
            @Override
            public void onSendButtonClick(String body) {
                if(body.isEmpty())return;
                Message message = new Message();
                message.setBody(body);
                message.setType(Message.RightSimpleMessage);
                message.setTime("User - "+new SimpleDateFormat("HH:mm dd MMM").format(new Date(System.currentTimeMillis())));
                chatView.addMessage(message);
                createInit(body);
                startInit();
            }
        });
        View icon = chatView.findViewById(R.id.expandIconView);
        RecyclerView rv = chatView.findViewById(R.id.chatRV);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int y = displayMetrics.heightPixels-500;
        icon.setOnClickListener(view -> rv.smoothScrollBy(0,-y));
    }

    @Override
    protected void onResume() {
        new ColourTheme(this);
        chatView.setChatViewBackgroundColor(ColourTheme.getVibrantColor());
        chatView.setLeftBubbleLayoutColor(ColourTheme.getLightColor());
        chatView.setRightBubbleLayoutColor(ColourTheme.getDarkColor());
        chatView.setLeftBubbleTextColor(ColourTheme.getDarkColor());
        chatView.setRightBubbleTextColor(ColourTheme.getLightColor());
        chatView.setTimeTextColor(ColourTheme.getLightColor());
        TextView textView = findViewById(R.id.top);
        ColourTheme.initTextView(textView);
        super.onResume();
    }
    public static void createInit(String text){
        asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String reply = PythonService.getResponse(text);
                    reply = reply
                    .replace("$$id$$","LegendSayantan")
                    .replace("$$name$$","Sayantan_Bot")
                    .replace("$$user$$","user");
                    reply = reply.replace("\n"," ");
                    Message response = new Message();
                    response.setBody(reply);
                    response.setType(Message.LeftSimpleMessage);
                    response.setTime("Bot - "+new SimpleDateFormat("HH:mm dd MMM").format(new Date(System.currentTimeMillis())));
                    System.out.println("Reply "+reply);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatView.addMessage(response);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
    public static void startInit() {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}