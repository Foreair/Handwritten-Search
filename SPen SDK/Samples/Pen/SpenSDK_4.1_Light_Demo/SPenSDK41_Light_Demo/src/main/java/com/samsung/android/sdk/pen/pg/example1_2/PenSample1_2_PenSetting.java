package com.samsung.android.sdk.pen.pg.example1_2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenPenChangeListener;
import com.samsung.android.sdk.pen.engine.SpenSimpleSurfaceView;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.settingui.SpenPenPresetPreviewManager;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout.PresetListener;
import com.samsung.spensdk4light.example.R;

public class PenSample1_2_PenSetting extends Activity {

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSimpleSurfaceView mSpenSimpleSurfaceView;
    private SpenSettingPenLayout mPenSettingView;
    private LinearLayout mLayoutPreset;
    private ImageButton mBtnPreset;
    private Button mBtnEditPreset;
    private ImageButton mBtnAddPreset;
    private ImageView mPenBtn;
    private final float SCREEN_UNIT = 360.0f;
    private static final int PRESET_MODE_VIEW = 1;
    private static final int PRESET_MODE_EDIT = 2;
    private int mPresetViewMode;
    private List<SpenSettingPenInfo> mFavoriteList;
    private SpenPenPresetPreviewManager mSpenPenPresetPreviewManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pen_setting);
        mContext = getApplicationContext();

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

        FrameLayout spenViewContainer = (FrameLayout) findViewById(R.id.spenViewContainer);
        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create PenSettingView
        mPenSettingView = new SpenSettingPenLayout(mContext, "", spenViewLayout);
        spenViewContainer.addView(mPenSettingView);

        // Create SpenView
        mSpenSimpleSurfaceView = new SpenSimpleSurfaceView(mContext);
        if (mSpenSimpleSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        mSpenSimpleSurfaceView.setToolTipEnabled(true);
        spenViewLayout.addView(mSpenSimpleSurfaceView);
        mPenSettingView.setCanvasView(mSpenSimpleSurfaceView);

        // Get the dimension of the device screen
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc", Toast.LENGTH_SHORT).show();
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
        mSpenSimpleSurfaceView.setPageDoc(mSpenPageDoc, true);

        initPenSettingInfo();
        mSpenSimpleSurfaceView.setColorPickerListener(mColorPickerListener);

        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mBtnPreset = (ImageButton) findViewById(R.id.btnPreset);
        mBtnPreset.setOnClickListener(onClickShowPresetListener);
        mBtnAddPreset = (ImageButton) findViewById(R.id.btnAdd);
        setRippleBackground(mBtnAddPreset);
        mBtnAddPreset.setOnClickListener(onClickAddPresetListener);
        mBtnEditPreset = (Button) findViewById(R.id.btnEditPreset);
        setRippleBackground(mBtnEditPreset);
        mBtnEditPreset.setOnClickListener(onClickEditPresetListener);
        mPenSettingView.setPresetListener(onPresetListener);
        mSpenSimpleSurfaceView.setPenChangeListener(onPenChangeListenner);
        mSpenSimpleSurfaceView.setOnTouchListener(onTouchSurfaceViewListener);
        mLayoutPreset = (LinearLayout) findViewById(R.id.layoutPreset);

        //Load favorite pens
        mFavoriteList=new ArrayList<SpenSettingPenInfo>();
        SharedPreferences mFavoritePen=getSharedPreferences
                ("sample_favorite_pen", MODE_PRIVATE);
        int sizeFavorite= mFavoritePen.getInt("size_favorite",0);
        for(int i=0;i<sizeFavorite;i++){
            String penInfor=mFavoritePen.getString("pen"+i,"");
            if(penInfor!=null){

                String[] favoriteInfo = penInfor.split("\\s+");
                if (favoriteInfo.length >= 3) {


                            SpenSettingPenInfo settingPenInfo = new SpenSettingPenInfo();
                            settingPenInfo.name = favoriteInfo[0];
                            settingPenInfo.color = Integer.parseInt(favoriteInfo[1]);
                            settingPenInfo.size = Float.parseFloat(favoriteInfo[2]);

                           if (favoriteInfo.length >= 4&&favoriteInfo[3]!=null) {
                               settingPenInfo.advancedSetting = favoriteInfo[3];
                           }else
                              settingPenInfo.advancedSetting="";
                    mFavoriteList.add(settingPenInfo);
                        }
                }
        }
        mSpenPenPresetPreviewManager=new SpenPenPresetPreviewManager(mContext);
        initPresetLayout();
        setPresetViewMode(PRESET_MODE_VIEW);

        if (isSpenFeatureEnabled == false) {
            mSpenSimpleSurfaceView.setToolTypeAction(SpenSimpleSurfaceView.TOOL_FINGER, SpenSimpleSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger",
                    Toast.LENGTH_SHORT).show();
        } else {
            mSpenSimpleSurfaceView.setToolTypeAction(SpenSimpleSurfaceView.TOOL_SPEN, SpenSimpleSurfaceView.ACTION_STROKE);
        }

    }
private void saveFavorite(){
    SharedPreferences mFavoritePen=getSharedPreferences
            ("sample_favorite_pen", MODE_PRIVATE);
    SharedPreferences.Editor edit=mFavoritePen.edit();
    edit.putInt("size_favorite",mFavoriteList.size());
    for(int i=0;i<mFavoriteList.size();i++){
        String presetInfo = mFavoriteList.get(i).name + " " + mFavoriteList.get(i).color + " "
                + mFavoriteList.get(i).size + " " + mFavoriteList.get(i).advancedSetting;
        edit.putString("pen"+i,presetInfo);
    }
    edit.commit();
}

    private final OnClickListener onClickShowPresetListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            mBtnPreset.setVisibility(View.GONE);
            mBtnEditPreset.setVisibility(View.VISIBLE);
            mLayoutPreset.setVisibility(View.VISIBLE);
            setPresetViewMode(PRESET_MODE_VIEW);
        }
    };

    private final OnClickListener onClickAddPresetListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_FAVORITE);
            mPenSettingView.setVisibility(View.VISIBLE);
        }
    };

    private final OnClickListener onClickEditPresetListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (mPresetViewMode == PRESET_MODE_EDIT) {
                setPresetViewMode(PRESET_MODE_VIEW);
            } else {
                setPresetViewMode(PRESET_MODE_EDIT);
            }
        }
    };

    private final PresetListener onPresetListener = new PresetListener() {

        @Override
        public void onSelected(int index) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDeleted(int index) {
            // TODO Auto-generated method stub
            if (index < mLayoutPreset.getChildCount() - 1) {
                mLayoutPreset.removeViewAt(index);
            }
        }

        @Override
        public void onChanged(int index) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAdded(SpenSettingPenInfo info) {
            // TODO Auto-generated method stub

           if(info!=null) {

              // check over maximum of pens
               if(mFavoriteList.size()==9)
               {
                   Toast.makeText(mContext,"Maximum of pens is 9",Toast.LENGTH_SHORT).show();
                   return;
               }
               for(int i=0;i<mFavoriteList.size();i++)
                   if(mFavoriteList.get(i).name.equalsIgnoreCase(info.name)&&mFavoriteList.get(i).color==info.color&&mFavoriteList.get(i).size==info.size&&mFavoriteList.get(i).advancedSetting.equalsIgnoreCase(info.advancedSetting)) {
                   Toast.makeText(mContext,"Already exists",Toast.LENGTH_SHORT).show();
                       return;
                   }

               SpenSettingPenInfo mTmp =new SpenSettingPenInfo();
               mTmp.name=info.name;
               mTmp.color=info.color;
               mTmp.size=info.size;
               mTmp.advancedSetting=info.advancedSetting;

               mFavoriteList.add(mTmp);
           }
            mPenSettingView.setVisibility(View.GONE);
            initPresetLayout();
        }
    };

    private final OnTouchListener onTouchSurfaceViewListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mSpenSimpleSurfaceView.getToolTypeAction(SpenSimpleSurfaceView.TOOL_FINGER) == SpenSimpleSurfaceView.ACTION_STROKE
                        || mSpenSimpleSurfaceView.getToolTypeAction(SpenSimpleSurfaceView.TOOL_SPEN) == SpenSimpleSurfaceView.ACTION_STROKE) {
                    mLayoutPreset.setVisibility(View.GONE);
                    mBtnEditPreset.setVisibility(View.GONE);
                    mBtnPreset.setVisibility(View.VISIBLE);
                }
            }
            return false;
        }
    };

    private final SpenPenChangeListener onPenChangeListenner = new SpenPenChangeListener() {
        @Override
        public void onChanged(SpenSettingPenInfo info) {
            // TODO Auto-generated method stub
            int indexSelected = -1;
            for (int i = 0; i < mFavoriteList.size(); i++) {
                SpenSettingPenInfo presetInfo = mFavoriteList.get(i);
                if (!presetInfo.name.equalsIgnoreCase(info.name)) {
                    continue;
                }
                if (presetInfo.size != info.size) {
                    continue;
                }
                if (!presetInfo.advancedSetting.equalsIgnoreCase(info.advancedSetting)) {
                    continue;
                }
                if (presetInfo.color != info.color) {
                    continue;
                }
                indexSelected = i;
                break;
            }
            selectPresetByIndex(indexSelected);
        }
    };

    private int getImageResource(String name) {
        int image = R.drawable.favorite_pen_pencil;
        if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.Pencil")) {
            image = R.drawable.favorite_pen_pencil;
            // colorPreview = R.drawable.mini_toolbar_preview_pencil;
        } else if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.InkPen")) {
            image = R.drawable.favorite_pen_pen;
            // colorPreview = R.drawable.mini_toolbar_preview_pen;
        } else if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.Marker")) {
            image = R.drawable.favorite_pen_marker;
            // colorPreview = R.drawable.mini_toolbar_preview_marker;
        } else if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.ChineseBrush")
                || name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.Beautify")) {
            image = R.drawable.favorite_pen_chinabrush;
            // colorPreview = R.drawable.mini_toolbar_preview_chinabrush;
        } else if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.Brush")) {
            image = R.drawable.favorite_pen_chinabrush;
            // colorPreview = R.drawable.mini_toolbar_preview_brush;
        } else if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.MagicPen")) {
            image = R.drawable.favorite_pen_correctpen;
            // colorPreview = R.drawable.preview_alpha;
        } else if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.FountainPen")) {
            image = R.drawable.favorite_pen_fountainpen;
            // colorPreview = R.drawable.mini_toolbar_preview_fountainpen;
        } else if (name.equalsIgnoreCase("com.samsung.android.sdk.pen.pen.preload.ObliquePen")) {
            image = R.drawable.favorite_pen_calligraphypen;
            // colorPreview = R.drawable.mini_toolbar_preview_caligraphypen;
        }
        return image;
    }

    public Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap CropBitmap(Bitmap source, int w, int h) {
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), h
                * (int) getResources().getDisplayMetrics().density);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    private void initPresetLayout() {
            mLayoutPreset.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < mFavoriteList.size(); i++) {
                       FrameLayout buttonPresetLayout = (FrameLayout) inflater.inflate(R.layout.button_preset, null);

            ImageView previewImage = (ImageView) buttonPresetLayout.findViewById(R.id.imgPreview);
            // LayoutParams mImageParams = previewImage.getLayoutParams();
            // mImageParams.height = (int) (presetList.get(i).getPenSize());
            // previewImage.setLayoutParams(mImageParams);
            if (mFavoriteList.get(i).name.equals("com.samsung.android.sdk.pen.pen.preload.MagicPen")) {
                float penAlpha = (mFavoriteList.get(i).color >> 24) & 0xFF;

                previewImage.setAlpha((float) (Math.round(penAlpha / 255.0F * 99) / 100.0));
                int progress = Math.round(((mFavoriteList.get(i).size * SCREEN_UNIT) / 1440 - 2.4f) * 10);
                float alphaWidth = (float) (progress * 6.3 / 140);

                previewImage.setImageBitmap(getResizedBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.favorite_pen_preview_correctpen),
                        (int) (32 * getResources().getDisplayMetrics().density), (int) (alphaWidth * getResources()
                                .getDisplayMetrics().density)));

            } else {
                previewImage.setImageBitmap(mSpenPenPresetPreviewManager.getPresetPreview(mFavoriteList.get(i).name,mFavoriteList.get(i).color,mFavoriteList.get(i).size,mFavoriteList.get(i).advancedSetting));
            }

            ImageView penImage = (ImageView) buttonPresetLayout.findViewById(R.id.imgPen);
            penImage.setImageBitmap(CropBitmap(
                    BitmapFactory.decodeResource(getResources(), getImageResource(mFavoriteList.get(i).name)), 32,
                    47));
            setRippleBackground(penImage);

            String presetInfo = mFavoriteList.get(i).name + " " + mFavoriteList.get(i).color + " "
                    + mFavoriteList.get(i).size + " " + mFavoriteList.get(i).advancedSetting;

            penImage.setTag(presetInfo);
            penImage.setOnClickListener(onSelectPreset);
            penImage.setOnLongClickListener(presetLongClick);
            ImageButton imgDelete = (ImageButton) buttonPresetLayout.findViewById(R.id.btnDelete);
            imgDelete.setTag(presetInfo);
            imgDelete.setOnClickListener(onDeletePreset);
            mLayoutPreset.addView(buttonPresetLayout);
        }
        mLayoutPreset.addView(mBtnAddPreset);
        selectPresetByInfo(mSpenSimpleSurfaceView.getPenSettingInfo());
    }

    private void setRippleBackground(View view) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            view.setBackground(new RippleDrawable(ColorStateList.valueOf(Color.LTGRAY), null, null));
        }
    }

    private void setPresetViewMode(int mode) {
        mPresetViewMode = mode;
        if (mPresetViewMode == PRESET_MODE_VIEW) {
            mBtnEditPreset.setText("EDIT");
            mBtnAddPreset.setVisibility(View.VISIBLE);
        } else if (mPresetViewMode == PRESET_MODE_EDIT) {
            mBtnEditPreset.setText("DONE");
            mBtnAddPreset.setVisibility(View.GONE);
        }

        for (int i = 0; i < mLayoutPreset.getChildCount() - 1; i++) {
            FrameLayout buttonPresetLayout = (FrameLayout) mLayoutPreset.getChildAt(i);
            ImageButton imgDelete = (ImageButton) buttonPresetLayout.findViewById(R.id.btnDelete);
            if (mPresetViewMode == PRESET_MODE_VIEW) {
                imgDelete.setVisibility(View.GONE);
            } else if (mPresetViewMode == PRESET_MODE_EDIT) {
                imgDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    private final OnClickListener onSelectPreset = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPresetViewMode == PRESET_MODE_EDIT) {
                return;
            }

            String tag = (String) (v.getTag());
            String[] presetInfo = tag.split("\\s+");
            if (presetInfo.length >= 3) {
                for (int i = 0; i < mFavoriteList.size(); i++) {
                    if (mFavoriteList.get(i).name.equalsIgnoreCase(presetInfo[0])
                            && mFavoriteList.get(i).color == Integer.parseInt(presetInfo[1])
                            && mFavoriteList.get(i).size == Float.parseFloat(presetInfo[2])) {

                        SpenSettingPenInfo settingPenInfo = new SpenSettingPenInfo();
                        settingPenInfo.name = presetInfo[0];
                        settingPenInfo.color = Integer.parseInt(presetInfo[1]);
                        settingPenInfo.size = Float.parseFloat(presetInfo[2]);
                        if (presetInfo.length >= 4) {
                            settingPenInfo.advancedSetting = mFavoriteList.get(i).advancedSetting;
                        }
                        mSpenSimpleSurfaceView.setPenSettingInfo(settingPenInfo);
                        mPenSettingView.setInfo(settingPenInfo);
                        selectPresetByIndex(i);

                        break;
                    }
                }

            }
        }
    };

    private final OnClickListener onDeletePreset = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String tag = (String) (v.getTag());
            String[] presetInfoString = tag.split("\\s+");
            if (presetInfoString.length >= 3) {
                SpenSettingPenInfo info = new SpenSettingPenInfo();
                info.name = presetInfoString[0];
                info.color = Integer.parseInt(presetInfoString[1]);
                info.size = Float.parseFloat(presetInfoString[2]);
                if (presetInfoString.length >= 4) {
                    info.advancedSetting = presetInfoString[3];
                }

             //   List<SpenPenPresetInfo> presetList = mPenSettingView.getPenPresetInfoList();
                for (int i = 0; i < mFavoriteList.size(); i++) {
                    if (!mFavoriteList.get(i).name.equalsIgnoreCase(info.name)) {
                        continue;
                    }
                    if (mFavoriteList.get(i).size != info.size) {
                        continue;
                    }
                    if (!mFavoriteList.get(i).advancedSetting.equalsIgnoreCase(info.advancedSetting)) {
                        continue;
                    }
                    if (mFavoriteList.get(i).color != info.color) {
                        continue;
                    }
                    mFavoriteList.remove(i);
                    mLayoutPreset.removeViewAt(i);
                    break;
                }
            }
        }
    };

    private final OnLongClickListener presetLongClick = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            if (mPresetViewMode == PRESET_MODE_EDIT) {
                setPresetViewMode(PRESET_MODE_VIEW);
            } else {
                setPresetViewMode(PRESET_MODE_EDIT);
            }
            return true;
        }
    };

    private void selectPresetByIndex(int indexSelected) {
      //  List<SpenPenPresetInfo> presetList = mPenSettingView.getPenPresetInfoList();
        for (int i = 0; i < mLayoutPreset.getChildCount() - 1; i++) {
            if (i < mFavoriteList.size()) {
                FrameLayout buttonPresetLayout = (FrameLayout) mLayoutPreset.getChildAt(i);
                ImageView penImage = (ImageView) buttonPresetLayout.findViewById(R.id.imgPen);
                if (i == indexSelected) {
                    LayoutParams params = penImage.getLayoutParams();
                    params.height = (int) (62f * getResources().getDisplayMetrics().density);
                    penImage.setLayoutParams(params);
                    penImage.setImageResource(getImageResource(mFavoriteList.get(i).name));
                } else {
                    LayoutParams params = penImage.getLayoutParams();
                    if (params.height == (int) (62f * getResources().getDisplayMetrics().density)) {
                        params.height = (int) (47f * getResources().getDisplayMetrics().density);
                        penImage.setLayoutParams(params);
                        penImage.setImageBitmap(CropBitmap(BitmapFactory.decodeResource(getResources(),
                                getImageResource(mFavoriteList.get(i).name)), 32, 47));
                    }
                }
            }
        }
    }

    private void selectPresetByInfo(SpenSettingPenInfo info) {
        for (int i = 0; i < mFavoriteList.size(); i++) {
            SpenSettingPenInfo presetInfo = mFavoriteList.get(i);
            if (!presetInfo.name.equalsIgnoreCase(info.name)) {
                continue;
            }
            if (presetInfo.size != info.size) {
                continue;
            }
            if (!presetInfo.advancedSetting.equalsIgnoreCase(info.advancedSetting)) {
                continue;
            }
            if (presetInfo.color != info.color) {
                continue;
            }
            selectPresetByIndex(i);
            break;
        }
    }

    public boolean isSelectedPreset(int index) {
        if (index == 2) {
            return true;
        }
        return false;
    }

    private void initPenSettingInfo() {
        // Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLUE;
        penInfo.size = 10;
        mSpenSimpleSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);
    }

    private final OnClickListener mPenBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // If PenSettingView is open, close it.
            if (mPenSettingView.isShown()) {
                mPenSettingView.setVisibility(View.GONE);
                // If PenSettingView is not open, open it.
            } else {
                mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_NORMAL);
                mPenSettingView.setVisibility(View.VISIBLE);
            }
        }
    };

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                penInfo.color = color;
                mPenSettingView.setInfo(penInfo);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPenSettingView != null) {
            mPenSettingView.close();
        }
        if (mSpenSimpleSurfaceView != null) {
            mSpenSimpleSurfaceView.close();
            mSpenSimpleSurfaceView = null;
        }

        if (mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    };

    @Override
    protected void onPause() {
        saveFavorite();
        super.onPause();
    }
}