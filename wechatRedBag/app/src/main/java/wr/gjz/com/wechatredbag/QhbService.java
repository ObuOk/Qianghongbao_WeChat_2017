package wr.gjz.com.wechatredbag;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojianzhong on 2016/12/31.
 */

public class QhbService extends AccessibilityService {

    private ArrayList<AccessibilityNodeInfo> notes;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        notes = new ArrayList<>();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int eventType = event.getEventType();

        switch(eventType) {

            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:

                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text: texts
                         ) {
                        if (text.toString().contains("[微信红包]")) {
                            Parcelable parcelableData = event.getParcelableData();
                            if (parcelableData != null && parcelableData instanceof Notification) {

                                Notification notification = (Notification) parcelableData;
                                PendingIntent contentIntent = notification.contentIntent;
                                try {
                                    contentIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    }
                }

                break;

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED :

                AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
                CharSequence className = event.getClassName();
                Log.e("classname",className.toString());
                switch(className.toString()) {

                    case "com.tencent.mm.ui.LauncherUI":

                        clickLuckMoneyInLauncherUI(rootInActiveWindow);

                    break;

                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI":
                        openLuckyMoney(rootInActiveWindow);
                        break;

                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                        returnLauchUI(rootInActiveWindow);
                        break;


                }



                break;

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void returnLauchUI(AccessibilityNodeInfo rootInActiveWindow) {
        List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gr");

        for (AccessibilityNodeInfo info : accessibilityNodeInfosByViewId
             ) {
                if (info.isClickable()) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openLuckyMoney(AccessibilityNodeInfo node) {
        List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/be_");

        for (AccessibilityNodeInfo info : accessibilityNodeInfosByViewId
             ) {
                    if (info.isClickable()) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
        }
    }


    private void clickLuckMoneyInLauncherUI(AccessibilityNodeInfo rootInActiveWindow) {

        recycle(rootInActiveWindow);

        AccessibilityNodeInfo accessibilityNodeInfo = notes.get(notes.size() - 1);
        accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);


    }

    private void recycle(AccessibilityNodeInfo node) {

        if (node == null) {
            return;
        }

        if (node.getChildCount() == 0) {

            if (node.getText() != null) {

                if (node.getText().toString().contains("领取红包")) {

                    if (node.isClickable()) {

                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return;
                    }

                    AccessibilityNodeInfo parent = node.getParent();
                    while(parent != null) {

                        if (parent.isClickable()) {

                            notes.add(parent);
                            break;

                        }
                        parent = parent.getParent();

                    }

                }

            }

        }else {

                for (int i = 0; i < node.getChildCount(); i ++) {

                    recycle(node.getChild(i));

                }

        }


    }


    @Override
    public void onInterrupt() {

    }
}
