package aoto.com.cameratest;

import android.os.Handler;
import android.os.Message;

/**
 * author:why
 * created on: 2019/6/20 19:01
 * description:
 */
public class MessageHelper {

    public static void sendMessage(Handler handler, int taskId){
        Message message= Message.obtain();
        message.what=taskId;
        handler.sendMessage(message);
    }
}
