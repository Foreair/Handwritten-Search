package com.samsung.android.sdk.pen.pg.example4_2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectContainer;
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenObjectRuntime;
import com.samsung.android.sdk.pen.engine.SpenObjectRuntimeInfo;
import com.samsung.android.sdk.pen.engine.SpenObjectRuntimeManager;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.plugin.interfaces.SpenObjectRuntimeInterface;
import com.samsung.spensdk3.example.R;

public class PenSample4_2_SORList extends Activity {

    private final int CONTEXT_MENU_RUN_ID = 0;

    private Context mContext;
    private Activity mActivity;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    RelativeLayout mSpenViewLayout;

    private ImageView mSelectionBtn;
    private ImageView mPenBtn;
    private ImageView mVideoBtn;

    private SpenObjectRuntimeManager mSpenObjectRuntimeManager;
    private List<SpenObjectRuntimeInfo> mSpenObjectRuntimeInfoList;
    private SpenObjectRuntimeInfo mObjectRuntimeInfo;
    private SpenObjectRuntime mObjectRuntime;

    private int mToolType = SpenSurfaceView.TOOL_SPEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sor_list);
        mContext = this;
        mActivity = this;

        // Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if (SDKUtils.processUnsupportedException(this, e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        mSpenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create SpenSurfaceView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        mSpenSurfaceView.setToolTipEnabled(true);
        mSpenSurfaceView.setZoomable(false);
        mSpenViewLayout.addView(mSpenSurfaceView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
        // Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        // Add a Page to NoteDoc, get an instance, and set it to the member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
        // Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        initPenSettingInfo();
        // Register the listener
        mSpenSurfaceView.setControlListener(mControlListener);
        mSpenSurfaceView.setPreTouchListener(onPreTouchSurfaceViewListener);

        // Set a button
        mSelectionBtn = (ImageView) findViewById(R.id.selectionBtn);
        mSelectionBtn.setOnClickListener(mSelectionBtnClickListener);

        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mVideoBtn = (ImageView) findViewById(R.id.videoBtn);
        mVideoBtn.setOnClickListener(mVideoBtnClickListener);

        selectButton(mPenBtn);

        // Set ObjectRuntimeManager
        mSpenObjectRuntimeManager = new SpenObjectRuntimeManager(mActivity);
        mSpenObjectRuntimeInfoList = new ArrayList<SpenObjectRuntimeInfo>();
        mSpenObjectRuntimeInfoList = mSpenObjectRuntimeManager.getObjectRuntimeInfoList();

        if (isSpenFeatureEnabled == false) {
            mToolType = SpenSurfaceView.TOOL_FINGER;
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger.",
                    Toast.LENGTH_SHORT).show();
        } else {
            mToolType = SpenSurfaceView.TOOL_SPEN;
        }
        mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
    }

    private void initPenSettingInfo() {
        // Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLUE;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
    }

    private SpenTouchListener onPreTouchSurfaceViewListener = new SpenTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    enableButton(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    enableButton(true);
                    break;
            }
            return false;
        }
    };

    private final OnClickListener mSelectionBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            selectButton(mSelectionBtn);
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_SELECTION);
        }
    };

    private final OnClickListener mPenBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            selectButton(mPenBtn);
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
        }
    };

    private final OnClickListener mVideoBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(checkPermission()){
                return;
            }
            mSpenSurfaceView.closeControl();
            createObjectRuntime();
        }
    };

    SpenControlListener mControlListener = new SpenControlListener() {

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> objectList, ArrayList<Rect> relativeRectList,
                ArrayList<SpenContextMenuItemInfo> menu, ArrayList<Integer> styleList, int pressType, PointF point) {
            if (objectList == null) {
                return false;
            }
            // If SOR is available, display the context menu.
            if (objectList.get(0).getSorInfo() != null) {
                menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_RUN_ID, "Run", true));
                objectList.get(0).setRotatable(false);
                return true;
            }
            return true;
        }

        @Override
        public boolean onMenuSelected(ArrayList<SpenObjectBase> objectList, int itemId) {
            if (objectList == null) {
                return true;
            }

            if (itemId == CONTEXT_MENU_RUN_ID) {
                SpenObjectBase object = objectList.get(0);
                mSpenSurfaceView.getControl().setContextMenuVisible(false);
                mSpenSurfaceView.getControl().setStyle(SpenControlBase.STYLE_BORDER_STATIC);
                mSpenSurfaceView.getControl().setActivated(true);

                String sorInfo = object.getSorInfo();
                try {
                    if (sorInfo.equalsIgnoreCase(mObjectRuntimeInfo.name) == false) {
                        mObjectRuntime = mSpenObjectRuntimeManager.createObjectRuntime(sorInfo);
                    }
                    // Set the listener and play the object.
                    mObjectRuntime.setListener(objectRuntimelistener);
                    mObjectRuntime.start(object, getRealRect(object.getRect()), mSpenSurfaceView.getPan(),
                            mSpenSurfaceView.getZoomRatio(), mSpenSurfaceView.getFrameStartPosition(), mSpenViewLayout);
                    mSpenSurfaceView.update();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        public void onObjectChanged(ArrayList<SpenObjectBase> object) {
        }

        @Override
        public void onRectChanged(RectF rect, SpenObjectBase object) {
        }

        @Override
        public void onRotationChanged(float angle, SpenObjectBase objectBase) {
        }

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> objectList) {
            if (mObjectRuntime != null) {
                mObjectRuntime.stop(true);
                mObjectRuntime = null;
            }
            return false;
        }
    };

    void createObjectRuntime() {
        if (mSpenObjectRuntimeInfoList == null || mSpenObjectRuntimeInfoList.size() == 0) {
            return;
        }

        String objectRuntimeInfoList[] = new String[mSpenObjectRuntimeInfoList.size()];
        for (int i = 0; i < mSpenObjectRuntimeInfoList.size(); i++) {
            objectRuntimeInfoList[i] = mSpenObjectRuntimeInfoList.get(i).name;
        }

        new AlertDialog.Builder(mContext).setTitle("Select Object Runtime")
                .setItems(objectRuntimeInfoList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            SpenObjectRuntimeInfo info = mSpenObjectRuntimeInfoList.get(which);
                            mObjectRuntime = mSpenObjectRuntimeManager.createObjectRuntime(info);

                            mObjectRuntimeInfo = info;
                            startObjectRuntime();

                            return;
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "ObjectRuntimeInfo class not found.", Toast.LENGTH_SHORT).show();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "Failed to access the ObjectRuntimeInfo constructor.",
                                    Toast.LENGTH_SHORT).show();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "Failed to access the ObjectRuntimeInfo field or method.",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "ObjectRuntimeInfo is not loaded.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    void startObjectRuntime() {
        if (mObjectRuntime == null) {
            Toast.makeText(mContext, "ObjectRuntime is not loaded \n Load Plugin First !!", Toast.LENGTH_SHORT).show();
            return;
        }

        SpenObjectBase objectBase = null;
        switch (mObjectRuntime.getType()) {
        case SpenObjectRuntimeInterface.TYPE_NONE:
            return;
        case SpenObjectRuntimeInterface.TYPE_IMAGE:
            objectBase = new SpenObjectImage();
            break;
        case SpenObjectRuntimeInterface.TYPE_STROKE:
            objectBase = new SpenObjectStroke();
            break;
        case SpenObjectRuntimeInterface.TYPE_CONTAINER:
            objectBase = new SpenObjectContainer();
            break;
        default:
            break;
        }
        if (objectBase == null) {
            Toast.makeText(mContext, "Has no selected object.", Toast.LENGTH_SHORT).show();
            return;
        }
        objectBase.setSorInfo(mObjectRuntimeInfo.className);
        if (!mObjectRuntimeInfo.name.equalsIgnoreCase("Video")) {
            objectBase.setRect(new RectF(0, 0, 500, 500), false);
        }
        objectBase.setOutOfViewEnabled(false);

        mObjectRuntime.setListener(objectRuntimelistener);
        mSpenPageDoc.appendObject(objectBase);
        mSpenPageDoc.selectObject(objectBase);
        mSpenSurfaceView.update();
        mSpenSurfaceView.getControl().setContextMenuVisible(false);
        mSpenSurfaceView.getControl().setStyle(SpenControlBase.STYLE_BORDER_STATIC);
        RectF sorRect;
        if (mObjectRuntimeInfo.name.equalsIgnoreCase("Video")) {
            sorRect = new RectF(0, 0, mSpenPageDoc.getWidth(), mSpenPageDoc.getHeight());
        } else {
            sorRect = new RectF(getRealRect(objectBase.getRect()));
        }
        mObjectRuntime.start(objectBase, sorRect, mSpenSurfaceView.getPan(), mSpenSurfaceView.getZoomRatio(),
                mSpenSurfaceView.getFrameStartPosition(), mSpenViewLayout);
    }

    SpenObjectRuntime.UpdateListener objectRuntimelistener = new SpenObjectRuntime.UpdateListener() {

        @Override
        public void onCompleted(Object objectBase) {
            if (mSpenSurfaceView != null) {
                SpenControlBase control = mSpenSurfaceView.getControl();
                if (control != null) {
                    control.setContextMenuVisible(true);
                    mSpenSurfaceView.updateScreenFrameBuffer();
                    mSpenSurfaceView.update();
                }
            }
            mObjectRuntime = null;
        }

        @Override
        public void onObjectUpdated(RectF rect, Object objectBase) {
            if (mSpenSurfaceView != null) {
                SpenControlBase control = mSpenSurfaceView.getControl();
                if (control != null) {
                    control.fit();
                    control.invalidate();
                    mSpenSurfaceView.update();
                }
            }
        }

        @Override
        public void onCanceled(int state, Object objectBase) {
            if (state == SpenObjectRuntimeInterface.CANCEL_STATE_INSERT) {
                mSpenPageDoc.removeObject((SpenObjectBase) objectBase);
                mSpenPageDoc.removeSelectedObject();
                mSpenSurfaceView.closeControl();
                mSpenSurfaceView.update();
            } else if (state == SpenObjectRuntimeInterface.CANCEL_STATE_RUN) {
                mSpenSurfaceView.closeControl();
                mSpenSurfaceView.update();
            }
            mObjectRuntime = null;
        }
    };


    private void enableButton(boolean isEnable) {
        mSelectionBtn.setEnabled(isEnable);
        mPenBtn.setEnabled(isEnable);
        mVideoBtn.setEnabled(isEnable);
    }

    private void selectButton(View v) {
        // Enable or disable the button according to the current mode.
        mSelectionBtn.setSelected(false);
        mPenBtn.setSelected(false);
        v.setSelected(true);
    }

    private RectF getRealRect(RectF rect) {
        float panX = mSpenSurfaceView.getPan().x;
        float panY = mSpenSurfaceView.getPan().y;
        float zoom = mSpenSurfaceView.getZoomRatio();
        PointF startPoint = mSpenSurfaceView.getFrameStartPosition();
        RectF realRect = new RectF();
        realRect.set((rect.left - panX) * zoom + startPoint.x, (rect.top - panY) * zoom + startPoint.y,
                (rect.right - panX) * zoom + startPoint.x, (rect.bottom - panY) * zoom + startPoint.y);
        return realRect;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpenObjectRuntimeManager != null) {
            if (mObjectRuntime != null) {
                mObjectRuntime.stop(true);
                mSpenObjectRuntimeManager.unload(mObjectRuntime);
            }
            mSpenObjectRuntimeManager.close();
        }

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }

        if (mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    }


    public static final int SDK_VERSION = Build.VERSION.SDK_INT;
    private static final int PEMISSION_REQUEST_CODE = 1;
    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermission() {
        if (SDK_VERSION < 23) {
            return false;
        }
        List<String> permissionList = new ArrayList<String>(Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE));
        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            permissionList.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
            permissionList.remove(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionList.size()>0) {
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), PEMISSION_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PEMISSION_REQUEST_CODE) {
            if (grantResults != null ) {
                for(int result: grantResults){
                    if(result!= PackageManager.PERMISSION_GRANTED){
                        finish();
                    }
                }

            }
        }
    }
}