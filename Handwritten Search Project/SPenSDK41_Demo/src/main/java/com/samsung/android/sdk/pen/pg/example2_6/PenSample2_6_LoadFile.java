package com.samsung.android.sdk.pen.pg.example2_6;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingTextInfo;
import com.samsung.android.sdk.pen.document.SpenInvalidPasswordException;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectLine;
import com.samsung.android.sdk.pen.document.SpenObjectShape;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.SpenUnsupportedTypeException;
import com.samsung.android.sdk.pen.document.SpenUnsupportedVersionException;
import com.samsung.android.sdk.pen.document.shapeeffect.SpenFillColorEffect;
import com.samsung.android.sdk.pen.document.shapeeffect.SpenLineColorEffect;
import com.samsung.android.sdk.pen.document.shapeeffect.SpenLineStyleEffect;
import com.samsung.android.sdk.pen.document.textspan.SpenFontSizeSpan;
import com.samsung.android.sdk.pen.document.textspan.SpenLineSpacingParagraph;
import com.samsung.android.sdk.pen.document.textspan.SpenTextParagraphBase;
import com.samsung.android.sdk.pen.document.textspan.SpenTextSpanBase;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTextChangeListener;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.pen.SpenPenInfo;
import com.samsung.android.sdk.pen.pen.SpenPenManager;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.pg.tool.ShapeAdapter;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingTextLayout;
import com.samsung.spensdk3.example.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static android.os.SystemClock.sleep;


public class PenSample2_6_LoadFile extends Activity {

    //Documento y página con los que captaremos la información que se quiere buscar
    SpenPageDoc mSpenPageDoc2;
    SpenNoteDoc mSpenNoteDoc2;
    //Lista con los Histogramas de cada palabra
    LinkedList<LinkedList<Double>> ListHistogramsLoad = new LinkedList<>();
    LinkedList<Double> ListHistogramsSearch = new LinkedList<>();

    //Listas de objetos con la información preprocesada
    LinkedList<SpenObjectStroke> ListObjectsLoadNormalized = new LinkedList<>();
    LinkedList<SpenObjectStroke> ListObjectsSearchNormalized = new LinkedList<>();

    //Listas de objetos desde las cuales se extrae la información
    LinkedList<SpenObjectStroke> ListObjectsSearch = new LinkedList<>();
    LinkedList<SpenObjectStroke> ListObjectsLoad = new LinkedList<>();
    //Listas de objetos básicas que aún se tienen que castear a ObjectStroke
    ArrayList<SpenObjectBase> ListObjectsSearchBase = new ArrayList<>();
    ArrayList<SpenObjectBase> ListObjectsLoadBase = new ArrayList<>();
    //Distancia entre objetos
    LinkedList<Double> Distancia = new LinkedList<>();


    //Para calcular los vectores de características
//    boolean FileLoaded = false;
    //Umbral establecido como distancia máxima para considerarse un nuevo objeto
    double umbral;
    //Distancia entre lineas. Si es mayor, nueva linea
    double new_line = 15;
    //Number of clusters
    private static int NUM_CLUSTERS = 150;
    //Lista con indices de la Palabra
    LinkedList<Integer> Indices = new LinkedList<>();
    //Lista con vectores de características
    LinkedList<FeatureVectorClass> Features = new LinkedList<>();
    //LKista con vectores tipo de características, los centroides
    LinkedList<FeatureVectorClass> FeaturesCentroids = new LinkedList<>();
    //Total palabras en el documento
    float TotalWords;
    //Problems with vicinity = 1
    double Repetidas = 0;
    //Problems with vicinity = 3
    double Repetidas2 = 0;
    //Problems that has gone through the security barrier
    double Repetidas3 = 0;

    private final int MODE_PEN = 0;
    private final int MODE_IMG_OBJ = 1;
    private final int MODE_TEXT_OBJ = 2;
    private final int MODE_STROKE_OBJ = 3;
    private final int MODE_SHAPE_OBJ = 4;
    private final int MODE_LINE_OBJ = 5;

    private final int SHAPE_NUM = 78;
    private final int LINE_NUM = 3;

    private final int CONTEXT_MENU_PROPERTIES_ID = 0;

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    private FrameLayout mSettingView;
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingTextLayout mTextSettingView;

    private Dialog mShapeSelectionDialog;
    private ShapeAdapter mShapesAdapter;
    private SpenFillColorEffect mFillColorEffect;
    private SpenLineStyleEffect mLineStyleEffect;
    private SpenLineColorEffect mLineColorEffect;

    //private ImageView mPenBtn;
    private ImageView mAcceptBtn;
    private ImageView mClearBtn;
    private ImageView mSearchBtn;
    private ImageView mLoadFileBtn;
    private ProgressBar mProgressBar;

    private int mPreMode = MODE_PEN;
    private int mMode = MODE_PEN;
    private Rect mScreenRect;
    private File mFilePath;
    private boolean mIsDiscard = false;
    private int mToolType = SpenSurfaceView.TOOL_SPEN;

    private int mShapeObjNumber;

    private int mObjSelectedType;
    private int mArrowBeginType;
    private int mArrowBeginSize;
    private int mArrowEndType;
    private int mArrowEndSize;

    private Dialog mShapePropertiesDialog;
    private RelativeLayout spenViewLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_file);
        mContext = this;

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

        spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create PenSettingView
        mPenSettingView = new SpenSettingPenLayout(getApplicationContext(), "", spenViewLayout);

        // Create TextSettingView
        HashMap<String, String> hashMapFont = new HashMap<String, String>();
        hashMapFont.put("Droid Sans Georgian", "/system/fonts/DroidSansGeorgian.ttf");
        hashMapFont.put("Droid Serif", "/system/fonts/DroidSerif-Regular.ttf");
        hashMapFont.put("Droid Sans", "/system/fonts/DroidSans.ttf");
        hashMapFont.put("Droid Sans Mono", "/system/fonts/DroidSansMono.ttf");
        mTextSettingView = (SpenSettingTextLayout) findViewById(R.id.settingTextLayout);
        mTextSettingView.initialize("", hashMapFont, spenViewLayout);

        mSettingView = (FrameLayout) findViewById(R.id.settingView);
        mSettingView.addView(mPenSettingView);

        // Create SpenSurfaceView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        mSpenSurfaceView.setToolTipEnabled(true);
        spenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mTextSettingView.setCanvasView(mSpenSurfaceView);

        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
        // Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, mScreenRect.width(), mScreenRect.height());
            mSpenNoteDoc2 = new SpenNoteDoc(mContext, mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        // Add a Page to NoteDoc and get an instance and set it to the member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc2 = mSpenNoteDoc2.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc2.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
        mSpenPageDoc2.clearHistory();
        // Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        initSettingInfo();
        // Register the listener
        mSpenSurfaceView.setTouchListener(mPenTouchListener);
        mSpenSurfaceView.setPreTouchListener(onPreTouchSurfaceViewListener);
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
        mSpenSurfaceView.setTextChangeListener(mTextChangeListener);
        mSpenSurfaceView.setControlListener(mControlListener);

        // Set a button
        mAcceptBtn = (ImageView) findViewById(R.id.AcceptBtn);
        mAcceptBtn.setOnClickListener(mAcceptBtnClickListener);

        mClearBtn = (ImageView) findViewById(R.id.ClearBtn);
        mClearBtn.setOnClickListener(mClearBtnClickListener);

        mSearchBtn = (ImageView) findViewById(R.id.SearchBtn);
        mSearchBtn.setOnClickListener(mSearchBtnClickListener);

        mLoadFileBtn = (ImageView) findViewById(R.id.loadFileBtn);
        mLoadFileBtn.setOnClickListener(mLoadFileBtnClickListener);

        mProgressBar = new ProgressBar(this);

        mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);

//        selectButton(mLoadFileBtn);
        initShapeSelectionDialog();

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SnoteData/";
        mFilePath = new File(filePath);

        if (isSpenFeatureEnabled == false) {
            mToolType = SpenSurfaceView.TOOL_FINGER;
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger.",
                    Toast.LENGTH_SHORT).show();
        } else {
            mToolType = SpenSurfaceView.TOOL_SPEN;
        }
        mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
        checkPermission();
        //We retrieve the centroid types FeaturesCentroids
        GetData();
        loadNoteFile();
    }

    private void initSettingInfo() {
        // Initialize Pen settings
        List<SpenPenInfo> penList = new ArrayList<SpenPenInfo>();
        SpenPenManager penManager = new SpenPenManager(mContext);
        penList = penManager.getPenInfoList();
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        for (SpenPenInfo info : penList) {
            if (info.name.equalsIgnoreCase("Brush")) {
                penInfo.name = info.className;
                break;
            }
        }
        penInfo.color = Color.BLUE;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);

        // Initialize text settings
        SpenSettingTextInfo textInfo = new SpenSettingTextInfo();
        int mCanvasWidth = mScreenRect.width();

        if (mSpenSurfaceView != null) {
            if (mSpenSurfaceView.getCanvasWidth() < mSpenSurfaceView.getCanvasHeight()) {
                mCanvasWidth = mSpenSurfaceView.getCanvasWidth();
            } else {
                mCanvasWidth = mSpenSurfaceView.getCanvasHeight();
            }
            if (mCanvasWidth == 0) {
                mCanvasWidth = mScreenRect.width();
            }
        }
        textInfo.size = Math.round(18 * mCanvasWidth / 360);
        mSpenSurfaceView.setTextSettingInfo(textInfo);
        mTextSettingView.setInfo(textInfo);
    }

    private final SpenTouchListener mPenTouchListener = new SpenTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getToolType(0) == mToolType) {
                // Check if the control is created.
                SpenControlBase control = mSpenSurfaceView.getControl();
                if (control == null) {
                    // When Pen touches the display while it is in Add ObjectImage mode
                    if (mMode == MODE_IMG_OBJ) {
                        // Set a bitmap file to ObjectImage.
                        SpenObjectImage imgObj = new SpenObjectImage();
                        Bitmap imageBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                R.drawable.ic_launcher);
                        imgObj.setImage(imageBitmap);

                        // Set the location to insert ObjectImage and add it to PageDoc.
                        PointF canvasPos = getCanvasPoint(event);
                        RectF rect = new RectF(canvasPos.x - (imageBitmap.getWidth() / 2), canvasPos.y
                                - (imageBitmap.getHeight() / 2), canvasPos.x + (imageBitmap.getWidth() / 2),
                                canvasPos.y + (imageBitmap.getHeight() / 2));
                        imgObj.setRect(rect, true);
                        mSpenPageDoc.appendObject(imgObj);
                        mSpenSurfaceView.update();

                        imageBitmap.recycle();
                        return true;
                        // When Pen touches the display while it is in Add ObjectTextBox mode
                    } else if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_TEXT) {
                        // Set the location to insert ObjectTextBox and add it to PageDoc.
                        SpenObjectTextBox textObj = new SpenObjectTextBox();
                        PointF canvasPos = getCanvasPoint(event);
                        float x = canvasPos.x;
                        float y = canvasPos.y;
                        float textBoxHeight = getTextBoxDefaultHeight(textObj);
                        if ((y + textBoxHeight) > mSpenPageDoc.getHeight()) {
                            y = mSpenPageDoc.getHeight() - textBoxHeight;
                        }
                        RectF rect = new RectF(x, y, x + 350, y + textBoxHeight);
                        textObj.setRect(rect, true);
                        mSpenPageDoc.appendObject(textObj);
                        mSpenPageDoc.selectObject(textObj);
                        mSpenSurfaceView.update();
                        // When Pen touches the display while it is in Add ObjectStroke mode
                    } else if (mMode == MODE_STROKE_OBJ) {
                        // Set the location to insert ObjectStroke and add it to PageDoc.
                        PointF canvasPos = getCanvasPoint(event);
                        float posX = canvasPos.x;
                        int pointSize = 157;

                        PointF[] points = new PointF[pointSize];
                        float[] pressures = new float[pointSize];
                        int[] timestamps = new int[pointSize];

                        for (int i = 0; i < pointSize; i++) {
                            points[i] = new PointF();
                            points[i].x = posX++;
                            points[i].y = (float) (canvasPos.y + Math.sin(.04 * i) * 50);
                            pressures[i] = 1;
                            timestamps[i] = (int) android.os.SystemClock.uptimeMillis();
                        }

                        SpenObjectStroke strokeObj = new SpenObjectStroke(mPenSettingView.getInfo().name, points,
                                pressures, timestamps);
                        strokeObj.setPenSize(mPenSettingView.getInfo().size);
                        strokeObj.setColor(mPenSettingView.getInfo().color);
                        mSpenPageDoc.appendObject(strokeObj);
                        mSpenSurfaceView.update();
                    } else if (mMode == MODE_SHAPE_OBJ) {

                        SpenObjectShape shapeObj = null;
                        try {
                            shapeObj = new SpenObjectShape(mShapeObjNumber);
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Not supported shape type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }

                        PointF canvasPos = getCanvasPoint(event);
                        RectF rect = new RectF(canvasPos.x - 150, canvasPos.y - 150, canvasPos.x + 150,
                                canvasPos.y + 150);
                        shapeObj.setRect(rect, false);

                        SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                        lineStyle.setWidth(4);
                        shapeObj.setLineStyleEffect(lineStyle);

                        mSpenPageDoc.appendObject(shapeObj);
                        mSpenSurfaceView.update();

                    } else if (mMode == MODE_LINE_OBJ) {
                        SpenObjectLine line = null;

                        try {
                            PointF canvasPos = getCanvasPoint(event);
                            RectF rect = new RectF(canvasPos.x - 200, canvasPos.y - 200, canvasPos.x + 200,
                                    canvasPos.y + 200);
                            line = new SpenObjectLine(mShapeObjNumber, new PointF(rect.left, rect.top), new PointF(
                                    rect.right, rect.bottom));
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Not supported line type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }

                        SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                        lineStyle.setWidth(4);
                        line.setLineStyleEffect(lineStyle);

                        mSpenPageDoc.appendObject(line);
                        mSpenSurfaceView.update();
                    }
                }
            }
            return false;
        }
    };

    private float getTextBoxDefaultHeight(SpenObjectTextBox textBox) {
        if (textBox == null) {
            return 0;
        }

        float height = 0, lineSpacing = 0, lineSpacePercent = 1.3f;
        float margin = textBox.getTopMargin() + textBox.getBottomMargin();

        ArrayList<SpenTextParagraphBase> pInfo = textBox.getTextParagraph();
        if (pInfo != null) {
            for (SpenTextParagraphBase info : pInfo) {
                if (info instanceof SpenLineSpacingParagraph) {
                    if (((SpenLineSpacingParagraph) info).getLineSpacingType() ==
                            SpenLineSpacingParagraph.TYPE_PERCENT) {
                        lineSpacePercent = ((SpenLineSpacingParagraph) info).getLineSpacing();
                    } else if (((SpenLineSpacingParagraph) info).getLineSpacingType() ==
                            SpenLineSpacingParagraph.TYPE_PIXEL) {
                        lineSpacing = ((SpenLineSpacingParagraph) info).getLineSpacing();
                    }
                }
            }
        }

        if (lineSpacing != 0) {
            height = lineSpacing + margin;
        } else {
            float fontSize = mSpenPageDoc.getWidth() / 20;
            ArrayList<SpenTextSpanBase> sInfo =
                    textBox.findTextSpan(textBox.getCursorPosition(), textBox.getCursorPosition());
            if (sInfo != null) {
                for (SpenTextSpanBase info : sInfo) {
                    if (info instanceof SpenFontSizeSpan) {
                        fontSize = ((SpenFontSizeSpan) info).getSize();
                        break;
                    }
                }
            }
            height = fontSize * lineSpacePercent;
        }

        return height;
    }

    private void applyTextSetting(SpenObjectTextBox textObj) {
        textObj.setTextLineSpacingInfo(SpenLineSpacingParagraph.TYPE_PERCENT, 1.3f);
        textObj.setFontSize(72);
    }

    private PointF getCanvasPoint(MotionEvent event) {
        float panX = mSpenSurfaceView.getPan().x;
        float panY = mSpenSurfaceView.getPan().y;
        float zoom = mSpenSurfaceView.getZoomRatio();
        return new PointF(event.getX() / zoom + panX, event.getY() / zoom + panY);
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

    OnClickListener mSearchBtnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            //Visibilities
            mAcceptBtn.setVisibility(View.VISIBLE);
            mClearBtn.setVisibility(View.VISIBLE);
            mLoadFileBtn.setVisibility(View.GONE);
            mSearchBtn.setVisibility(View.GONE);

            if (mSpenPageDoc2.getObjectCount(true) > 0) {
                mSpenNoteDoc2.getPage(0).removeAllObject();
            }
            mSpenSurfaceView.setPageDoc(mSpenPageDoc2, true);
//            mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            mProgressBar.setMessage("Loading");
//            mProgressBar.setIndeterminate(true);
//            mProgressBar.setProgress(0);
        }
    };
    OnClickListener mAcceptBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            if (mSpenPageDoc2.getObjectCount(true) > 0) {
                //visibilities
                mAcceptBtn.setVisibility(View.GONE);
                mClearBtn.setVisibility(View.GONE);
                mLoadFileBtn.setVisibility(View.VISIBLE);
                mSearchBtn.setVisibility(View.VISIBLE);
//                mProgressBar.setVisibility(View.VISIBLE);

                //initialize structures
                //Diferencia entre Histogramas
                LinkedList<Double> Difference = new LinkedList<>();
                LinkedList<Integer> Word = new LinkedList<>();
                //Clear structures
                ListObjectsSearchBase.clear();
                ListObjectsSearch.clear();
                ListHistogramsSearch.clear();
                ListObjectsSearchNormalized.clear();
                //Retrieving word given
                ListObjectsSearchBase = mSpenPageDoc2.getObjectList(SpenPageDoc.FIND_TYPE_STROKE);

                //Converting objects to stroke objects
                for (SpenObjectBase spenObjectBase : ListObjectsSearchBase) {
                    ListObjectsSearch.add((SpenObjectStroke) spenObjectBase);
                }
                //Normalizing stroke objects
                NormalizeDataSearch();
                for (int i = 0; i < NUM_CLUSTERS; i++) {
                    ListHistogramsSearch.add(0.0);
                }
                //Creating Feature Vectors and Histograms for the search word
                GetFeatureVectorsSearch();
                //Normalizing Histograms
                NormalizeHistogramsSearch(ListHistogramsSearch);
                if (Features.size() == 0) {
                    //Creating Feature Vectors and Histograms for the load file
                    GetFeatureVectors2(TotalWords);
                    //Normalizing Histograms
                    NormalizeHistograms(ListHistogramsLoad);
                }

                double distance;
                for (int i = 0; i < TotalWords; i++) {
                    boolean added = false;
                    distance = CalculateHistogramDifference(ListHistogramsSearch, ListHistogramsLoad.get(i));

                    if (i == 0) {
                        Difference.addFirst(distance);
                        Word.addFirst(i);
                    } else {
                        for (int j = 0; j < i; j++) {
                            if ((distance > Difference.get(j)) & distance != 0 & !added) {
                                Difference.add(j, distance);
                                Word.add(j, i);
                                added = true;
                            }
                        }
                        if (!Word.contains(i) & !added) {
                            Difference.addLast(distance);
                            Word.addLast(i);
                        }

                    }
                }
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, false);
                for (int i = 0; i < mSpenPageDoc.getObjectCount(true); i++) {
//                    mSpenPageDoc.getObject(i).setVisibility(false);
                    SpenObjectStroke Ugh = (SpenObjectStroke) mSpenPageDoc.getObject(i);
                    Ugh.setColor(0);
                }
                double max = Difference.getFirst();
                for (int j = 0; j < Word.size(); j++) {
                    boolean draw = false;
                    if (j == 0 | Difference.get(j) > 0.65) {
                        draw = true;
                    } else {
                        if ((max - Difference.get(j)) < 0.10 & Difference.get(j)>0.35) {
                            draw = true;
                        }
                    }
                    if (draw) {
                        //Para la palabra que más se parece a la escrita, le cambiamos el color de la tinta
                        LinkedList<Integer> List = GetWord(Word.get(j), TotalWords);
                        for (int i = 0; i < List.size(); i++) {
                            SpenObjectStroke Aux;
                            Aux = (SpenObjectStroke) mSpenPageDoc.getObject(List.get(i));
                            Aux.setColor(255551);
                        }
                    }
                }
//                //Y dibujamos lo escrito en la página también
//                for (SpenObjectStroke iterator : ListObjectsSearchNormalized) {
//                    mSpenPageDoc.appendObject(iterator);
//                    iterator.setVisibility(true);
//                }

                mSpenSurfaceView.update();

            }
        }
    };
    OnClickListener mClearBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mSpenPageDoc2.getObjectCount(true) > 0) {
                mSpenNoteDoc2.getPage(0).removeAllObject();
                mSpenSurfaceView.update();
            }
        }
    };


    private final SpenControlListener mControlListener = new SpenControlListener() {

        @Override
        public void onRotationChanged(float arg0, SpenObjectBase arg1) {
        }

        @Override
        public void onRectChanged(RectF arg0, SpenObjectBase arg1) {
        }

        @Override
        public void onObjectChanged(ArrayList<SpenObjectBase> arg0) {
        }

        @Override
        public boolean onMenuSelected(ArrayList<SpenObjectBase> objectList, int itemId) {
            switch (itemId) {
                // Properties of object shape/line.
                case CONTEXT_MENU_PROPERTIES_ID:
                    shapeProperties();
                    mSpenSurfaceView.closeControl();
                    break;
                default:
                    break;
            }

            return true;
        }

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> objectList, ArrayList<Rect> relativeRectList,
                                 ArrayList<SpenContextMenuItemInfo> menu, ArrayList<Integer> styleList, int pressType, PointF point) {
            // Set the Context menu
            SpenObjectBase object = objectList.get(0);
            if (object.getType() == SpenObjectBase.TYPE_SHAPE || object.getType() == SpenObjectBase.TYPE_LINE) {
                menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_PROPERTIES_ID, "Properties", true));
            }

            return true;
        }

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> arg0) {
            return false;
        }
    };

    private void initShapeSelectionDialog() {
        ArrayList<Integer> shapes = new ArrayList<Integer>();
        for (int i = 0; i < SHAPE_NUM + LINE_NUM; i++) {
            shapes.add(i);
        }
        mShapesAdapter = new ShapeAdapter(mContext, shapes);

        mShapeSelectionDialog = new Dialog(mContext);
        mShapeSelectionDialog.setContentView(R.layout.dialog_shapes);
        mShapeSelectionDialog.setTitle("Shape/Line");

        GridView gridShapes = (GridView) mShapeSelectionDialog.findViewById(R.id.gridShapes);
        gridShapes.setAdapter(mShapesAdapter);
        gridShapes.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < SHAPE_NUM) {
                    mMode = MODE_SHAPE_OBJ;
                    mShapeObjNumber = position + 1;
                    SpenObjectShape shape = null;

                    try {
                        shape = new SpenObjectShape(mShapeObjNumber);
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Not supported shape type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    shape.setRect(new RectF(100, 100, 400, 400), false);

                    SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                    lineStyle.setWidth(4);
                    shape.setLineStyleEffect(lineStyle);

                    mSpenPageDoc.appendObject(shape);
                    mSpenSurfaceView.update();
                    mShapeSelectionDialog.dismiss();
                } else {
                    mMode = MODE_LINE_OBJ;
                    SpenObjectLine line = null;
                    mShapeObjNumber = position - SHAPE_NUM;

                    try {
                        line = new SpenObjectLine(mShapeObjNumber, new PointF(100, 100), new PointF(500, 500));
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Not supported line type: " + mShapeObjNumber, Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    SpenLineStyleEffect lineStyle = new SpenLineStyleEffect();
                    lineStyle.setWidth(4);
                    line.setLineStyleEffect(lineStyle);

                    mSpenPageDoc.appendObject(line);
                    mSpenSurfaceView.update();
                    mShapeSelectionDialog.dismiss();
                }
            }
        });
    }

    private void shapeProperties() {
        final SpenObjectBase object = mSpenPageDoc.getSelectedObject().get(0);
        mObjSelectedType = object.getType();

        mLineStyleEffect = new SpenLineStyleEffect();
        mLineColorEffect = new SpenLineColorEffect();

        if (mObjSelectedType == SpenObjectBase.TYPE_SHAPE) {
            ((SpenObjectShape) object).getLineStyleEffect(mLineStyleEffect);
            ((SpenObjectShape) object).getLineColorEffect(mLineColorEffect);
            mFillColorEffect = new SpenFillColorEffect();
            ((SpenObjectShape) object).getFillEffect(mFillColorEffect);
        } else if (mObjSelectedType == SpenObjectBase.TYPE_LINE) {
            ((SpenObjectLine) object).getLineStyleEffect(mLineStyleEffect);
            ((SpenObjectLine) object).getLineColorEffect(mLineColorEffect);
        }

        mShapePropertiesDialog = new Dialog(mContext);
        mShapePropertiesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mShapePropertiesDialog.setContentView(R.layout.dialog_shapes_properties);
        updatePropertiesDiablogLayout();

        // Fill Color Shape Object
        if (mObjSelectedType == SpenObjectBase.TYPE_SHAPE) {
            String arrayColor[] = new String[6];
            arrayColor[0] = "No Fill";
            arrayColor[1] = "Black";
            arrayColor[2] = "Blue";
            arrayColor[3] = "Red";
            arrayColor[4] = "Yellow";
            arrayColor[5] = "Green";

            final ArrayList<Integer> colors = new ArrayList<Integer>();
            colors.add(0);
            colors.add(Color.BLACK);
            colors.add(Color.BLUE);
            colors.add(Color.RED);
            colors.add(Color.YELLOW);
            colors.add(Color.GREEN);

            LinearLayout fillColorView = (LinearLayout) mShapePropertiesDialog.findViewById(R.id.fillColorView);
            fillColorView.setVisibility(View.VISIBLE);

            Spinner fillColorSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spFillColor);
            ArrayAdapter<String> adapterFillColor = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayColor);
            fillColorSpinner.setAdapter(adapterFillColor);
            fillColorSpinner.setSelection(colors.indexOf(mFillColorEffect.getSolidColor()));

            fillColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mFillColorEffect.setSolidColor(colors.get(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });
        }

        String arrayColor[] = new String[5];
        arrayColor[0] = "Black";
        arrayColor[1] = "Blue";
        arrayColor[2] = "Red";
        arrayColor[3] = "Yellow";
        arrayColor[4] = "Green";

        final ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.BLACK);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);

        // SPinner Line Color
        Spinner lineColorSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spLineColor);
        ArrayAdapter<String> adapterLineColor = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, arrayColor);
        lineColorSpinner.setAdapter(adapterLineColor);
        lineColorSpinner.setSelection(colors.indexOf(mLineColorEffect.getSolidColor()));
        lineColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                mLineColorEffect.setSolidColor(colors.get(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        final EditText tbWidthInput = (EditText) mShapePropertiesDialog.findViewById(R.id.tbWidth);
        tbWidthInput.setText("" + mLineStyleEffect.getWidth());

// SPinner Compound type
        final Spinner compTypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spCompType);
        final String arrayCompType[] = new String[5];
        arrayCompType[0] = "Simple";
        arrayCompType[1] = "Double";
        arrayCompType[2] = "Thin";
        arrayCompType[3] = "Thick";
        arrayCompType[4] = "Triple";

        ArrayAdapter<String> adapterCompTyper = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, arrayCompType);
        compTypeSpinner.setAdapter(adapterCompTyper);
        compTypeSpinner.setSelection(mLineStyleEffect.getCompoundType());
        compTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                try {
                    mLineStyleEffect.setCompoundType(index);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(mContext, "Not supported compound type: \"" + arrayCompType[index] + "\"",
                            Toast.LENGTH_SHORT).show();
                    compTypeSpinner.setSelection(mLineStyleEffect.getCompoundType());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // SPinner Dash type
        Spinner dashTypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spDashType);
        String arrayDashType[] = new String[8];
        arrayDashType[0] = "Solid";
        arrayDashType[1] = "Round Dot";
        arrayDashType[2] = "Square Dot";
        arrayDashType[3] = "Dash";
        arrayDashType[4] = "Dash Dot";
        arrayDashType[5] = "Long Dash";
        arrayDashType[6] = "Long Dash Dot";
        arrayDashType[7] = "Long Dash Dot Dot";

        ArrayAdapter<String> adapterDashTyper = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, arrayDashType);
        dashTypeSpinner.setAdapter(adapterDashTyper);
        dashTypeSpinner.setSelection(mLineStyleEffect.getDashType());
        dashTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                mLineStyleEffect.setDashType(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        if (mObjSelectedType == SpenObjectBase.TYPE_LINE) {
            // Enable arrow setting view
            LinearLayout arrowSettingView = (LinearLayout) mShapePropertiesDialog.findViewById(R.id.arrowSetting);
            arrowSettingView.setVisibility(View.VISIBLE);

            // SPinner Arrow Begin Type
            mArrowBeginType = mLineStyleEffect.getBeginArrowType();
            mArrowBeginSize = mLineStyleEffect.getBeginArrowSize();
            mArrowEndType = mLineStyleEffect.getEndArrowType();
            mArrowEndSize = mLineStyleEffect.getEndArrowSize();

            String arrayArrowType[] = new String[6];
            arrayArrowType[0] = "None";
            arrayArrowType[1] = "Arrow";
            arrayArrowType[2] = "Open Arrow";
            arrayArrowType[3] = "Stealth Arrow";
            arrayArrowType[4] = "Diamond Arrow";
            arrayArrowType[5] = "Oval Arrow";

            String arrayArrowSize[] = new String[3];
            arrayArrowSize[0] = "Normal";
            arrayArrowSize[1] = "Small";
            arrayArrowSize[2] = "Big";

            Spinner arrowBTypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowBeginType);
            ArrayAdapter<String> adapterArrowBType = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowType);

            arrowBTypeSpinner.setAdapter(adapterArrowBType);
            arrowBTypeSpinner.setSelection(mArrowBeginType);
            arrowBTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowBeginType = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            // SPinner Arrow Begin Size
            Spinner arrowBSizeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowBeginSize);
            ArrayAdapter<String> adapterArrowBSize = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowSize);

            arrowBSizeSpinner.setAdapter(adapterArrowBSize);
            arrowBSizeSpinner.setSelection(mArrowBeginSize);
            arrowBSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowBeginSize = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            // SPinner Arrow End Type
            Spinner arrowETypeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowEndType);
            ArrayAdapter<String> adapterArrowEType = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowType);

            arrowETypeSpinner.setAdapter(adapterArrowEType);
            arrowETypeSpinner.setSelection(mArrowEndType);
            arrowETypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowEndType = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            // SPinner Arrow End Size
            Spinner arrowESizeSpinner = (Spinner) mShapePropertiesDialog.findViewById(R.id.spArrowEndSize);
            ArrayAdapter<String> adapterArrowESize = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_dropdown_item, arrayArrowSize);

            arrowESizeSpinner.setAdapter(adapterArrowESize);
            arrowESizeSpinner.setSelection(mArrowEndSize);
            arrowESizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    mArrowEndSize = index;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });
        }

        Button btnOK = (Button) mShapePropertiesDialog.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mObjSelectedType == SpenObjectBase.TYPE_SHAPE) {
                    // Set Width
                    if (tbWidthInput.getText().length() == 0) {
                        tbWidthInput.setError("Please input value");
                        return;
                    }
                    float width = Float.parseFloat(tbWidthInput.getText().toString());
                    mLineStyleEffect.setWidth(width);

                    // Set fill color effect
                    ((SpenObjectShape) object).setFillEffect(mFillColorEffect);

                    ((SpenObjectShape) object).setLineStyleEffect(mLineStyleEffect);
                    ((SpenObjectShape) object).setLineColorEffect(mLineColorEffect);
                } else if (mObjSelectedType == SpenObjectBase.TYPE_LINE) {
                    // Set Width
                    if (tbWidthInput.getText().length() == 0) {
                        tbWidthInput.setError("Please input value");
                        return;
                    }
                    float width = Float.parseFloat(tbWidthInput.getText().toString());
                    mLineStyleEffect.setWidth(width);

                    mLineStyleEffect.setBeginArrow(mArrowBeginType, mArrowBeginSize);
                    mLineStyleEffect.setEndArrow(mArrowEndType, mArrowEndSize);

                    ((SpenObjectLine) object).setLineStyleEffect(mLineStyleEffect);
                    ((SpenObjectLine) object).setLineColorEffect(mLineColorEffect);
                }

                mSpenSurfaceView.update();
                mShapePropertiesDialog.dismiss();
            }
        });

        Button btnCancel = (Button) mShapePropertiesDialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mShapePropertiesDialog.dismiss();
            }
        });

        mShapePropertiesDialog.show();
        return;
    }

    private final OnClickListener mLoadFileBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (checkPermission()) {
                return;
            }
            mSpenSurfaceView.closeControl();
//            mSpenPageDoc.clearHistory();
            closeSettingView();
//            selectButton(mLoadFileBtn);
            loadNoteFile();

        }
    };

    private boolean saveNoteFile(final boolean isClose) {
        if (!mFilePath.exists()) {
            if (!mFilePath.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Prompt Save File dialog to get the file name
        // and get its save format option (note file or image).
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.save_file_dialog, (ViewGroup) findViewById(R.id.layout_root));

        AlertDialog.Builder builderSave = new AlertDialog.Builder(mContext);
        builderSave.setTitle("Enter file name");
        builderSave.setView(layout);

        final EditText inputPath = (EditText) layout.findViewById(R.id.input_path);
        inputPath.setText("Note");

        builderSave.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final RadioGroup selectFileExt = (RadioGroup) layout.findViewById(R.id.radioGroup);

                // Set the save directory for the file.
                String saveFilePath = mFilePath.getPath() + '/';
                String fileName = inputPath.getText().toString();
                if (!fileName.equals("")) {
                    saveFilePath += fileName;
                    int checkedRadioButtonId = selectFileExt.getCheckedRadioButtonId();
                    if (checkedRadioButtonId == R.id.radioNote) {
                        saveFilePath += ".spd";
                        saveNoteFile(saveFilePath);
                    } else if (checkedRadioButtonId == R.id.radioImage) {
                        saveFilePath += ".png";
                        captureSpenSurfaceView(saveFilePath);
                    } else {
                    }
                    if (isClose) {
                        finish();
                    }
                } else {
                    Toast.makeText(mContext, "Invalid filename !!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        builderSave.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isClose) {
                    finish();
                }
            }
        });

        AlertDialog dlgSave = builderSave.create();
        dlgSave.show();

        return true;
    }

    private boolean saveNoteFile(String strFileName) {
        try {
            // Save NoteDoc
            mSpenNoteDoc.save(strFileName, false);
            Toast.makeText(mContext, "Save success to " + strFileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot save NoteDoc file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void captureSpenSurfaceView(String strFileName) {

        // Capture the view
        Bitmap imgBitmap = mSpenSurfaceView.captureCurrentView(true);
        if (imgBitmap == null) {
            Toast.makeText(mContext, "Capture failed." + strFileName, Toast.LENGTH_SHORT).show();
            return;
        }

        OutputStream out = null;
        try {
            // Create FileOutputStream and save the captured image.
            out = new FileOutputStream(strFileName);
            imgBitmap.compress(CompressFormat.PNG, 100, out);
            // Save the note information.
            mSpenNoteDoc.save(out, false);
            out.close();
            Toast.makeText(mContext, "Captured images were stored in the file" + strFileName, Toast.LENGTH_SHORT)
                    .show();
        } catch (IOException e) {
            File tmpFile = new File(strFileName);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            Toast.makeText(mContext, "Failed to save the file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            File tmpFile = new File(strFileName);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            Toast.makeText(mContext, "Failed to save the file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        imgBitmap.recycle();
    }

    private void loadNoteFile() {
        // Load the file list.
        final String fileList[] = setFileList();
        if (fileList == null) {
            return;
        }

        // Prompt Load File dialog.
        AlertDialog Dialog = new AlertDialog.Builder(mContext).setTitle("Select file")
                .setItems(fileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strFilePath = mFilePath.getPath() + '/' + fileList[which];

                        try {
                            SpenObjectTextBox.setInitialCursorPos(SpenObjectTextBox.CURSOR_POS_END);
                            // Create NoteDoc with the selected file.
                            SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(mContext, strFilePath, mScreenRect.width(),
                                    SpenNoteDoc.MODE_WRITABLE, true);
                            //mSpenNoteDoc.close();
                            mSpenNoteDoc = tmpSpenNoteDoc;
                            int B = mSpenNoteDoc.getPageCount();
                            if (mSpenNoteDoc.getPageCount() == 0) {
                                mSpenPageDoc = mSpenNoteDoc.appendPage();

                            } else {
                                //Clearing structures
                                ListObjectsLoad.clear();
                                ListObjectsLoadBase.clear();
                                ListObjectsLoadNormalized.clear();
                                Indices.clear();
                                Features.clear();
                                Distancia.clear();

                                mSpenPageDoc = mSpenNoteDoc.getPage(mSpenNoteDoc.getLastEditedPageIndex());
                                //Cogemos los datos de la página que hemos cargado
                                ListObjectsLoadBase = mSpenPageDoc.getObjectList(SpenPageDoc.FIND_TYPE_STROKE);
                                //Los pasamos a ObjectStroke
                                for (SpenObjectBase iterator : ListObjectsLoadBase) {
                                    ListObjectsLoad.add((SpenObjectStroke) iterator);
                                }
                                CalculateUmbral();
                                TotalWords = GetTotalWords();

                                //We initialize the histograms
                                BoWCreateHistogram();
                                //Normalizamos la información(Creamos ListObjectsLoadNormalized)
                                NormalizeData();
//                                //Calculamos vectores de características y creamos los histogramas para cada palabra
//                                GetFeatureVectors2(TotalWords);
//                                int C = 0;
//                                int D = C;
////                                BoWTrainer();
//                                NormalizeHistograms(ListHistogramsLoad);

                                //Pruebas para quitar visibilidad a toda la página
//                                LinkedList<Integer> ListFalse = new LinkedList<>();
//                                for (int i = 0; i < TotalWords; i++) {
//                                    ListFalse.clear();
//                                    ListFalse = GetWord(i, TotalWords);
//                                    SetVisibilityFalse(ListFalse, ListObjectsLoad);
//                                }
//                                PointF[] Puntos = ListObjectsLoadNormalized.get(0).getPoints();
//                                PointF Punto = Puntos[0];
//                                mSpenPageDoc.appendObject(ListObjectsLoadNormalized.get(0));
//                                mSpenPageDoc.appendObject(ListObjectsLoadNormalized.get(1));
//                                mSpenPageDoc.appendObject(ListObjectsLoadNormalized.get(2));
//                                mSpenPageDoc.appendObject(ListObjectsLoadNormalized.get(3));


//                                for (int i = 0; i < 1; i++) {
//                                    Indices = GetWord(i, TotalWords);
//                                    SetVisibilityTrue(Indices, ListObjectsLoad);
//                                }
                            }
                            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                            mSpenSurfaceView.update();
                            //Visibilities
                            mSearchBtn.setVisibility(View.VISIBLE);
                            Toast.makeText(mContext, "Successfully loaded noteFile.", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(mContext, "Cannot open this file.", Toast.LENGTH_LONG).show();
                        } catch (SpenUnsupportedTypeException e) {
                            Toast.makeText(mContext, "This file is not supported.", Toast.LENGTH_LONG).show();
                        } catch (SpenInvalidPasswordException e) {
                            Toast.makeText(mContext, "This file is locked by a password.", Toast.LENGTH_LONG).show();
                        } catch (SpenUnsupportedVersionException e) {
                            Toast.makeText(mContext, "This file is the version that does not support.",
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Failed to load noteDoc.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }).show();
    }

    private double CalculateHistogramDifference(LinkedList<Double> A, LinkedList<Double> B) {
        //Le pasamos los dos histogramas
        double aux1 = 0, aux2 = 0, sum = 0, difference, denominator;
        //Por cada uno de los tipos de vector que hay hacemos los cálculos
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            sum += A.get(i) * B.get(i);
            aux1 += Math.pow(A.get(i), 2);
            aux2 += Math.pow(B.get(i), 2);
        }
        aux1 = Math.sqrt(aux1);
        aux2 = Math.sqrt(aux2);
        denominator = aux1 * aux2;
        difference = sum / denominator;
        return difference;
    }

    private void GetData() {
        try {
            FeaturesCentroids.clear();
            ObjectInputStream ois = new ObjectInputStream(getAssets().open("HSData.su"));

            FeatureVectorClass aux = (FeatureVectorClass) ois.readObject();
            int i = 0;
            while (aux != null) {
                FeaturesCentroids.add(i, aux);
                aux = (FeatureVectorClass) ois.readObject();
                i++;
            }
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void BoWTrainer() {
        //To calculate a file with the centroids
        KMeans kMeans = new KMeans();
        kMeans.init(Features);
        kMeans.calculate();
        kMeans.createFile();
    }

    private void BoWCreateHistogram() {
        ListHistogramsLoad.clear();
        for (int j = 0; j < TotalWords; j++) {
            //First we initialize the histogram list
            LinkedList<Double> Histogram = new LinkedList<>();
            for (int i = 0; i < NUM_CLUSTERS; i++) {
                Histogram.add(0.0);
            }
            ListHistogramsLoad.add(Histogram);
        }
    }

    private void NormalizeHistogramsSearch(LinkedList<Double> histogram) {
        double aux, sum = 0;
        for (int i = 0; i < histogram.size(); i++) {
            sum += histogram.get(i);
        }
        for (int i = 0; i < histogram.size(); i++) {
            aux = histogram.get(i);
            aux = aux / sum;
            histogram.set(i, aux);
        }
    }

    private void NormalizeHistograms(LinkedList<LinkedList<Double>> histograms) {
        double aux, sum = 0;
        for (LinkedList<Double> histogram : histograms) {
            for (int i = 0; i < histogram.size(); i++) {
                sum += histogram.get(i);
            }
            for (int i = 0; i < histogram.size(); i++) {
                aux = histogram.get(i);
                aux = aux / sum;
                histogram.set(i, aux);
            }
        }
    }


    private boolean isWorkable(SpenObjectStroke Aux, int Position, boolean neighbour) {
        //Returns true if the point is workable or false if it is not
        //neighbour true means that we are checking if the neighbour is workable for the vicinity of 1
        boolean repeated = true;
        double aux;
        PointF A;
        PointF B;
        //If the position is 0 or max, functions will return 0 so the point is workable
        if ((Position < 1) | (Position >= Aux.getPoints().length - 1)) {
            repeated = true;
            return repeated;
        } else {
            //If the position is between 0 and 3 or max-3 and max
            //We check if the vicinity of the point has the same x and y
            if ((Position < 3) | (Position >= Aux.getPoints().length - 3)) {
                A = Aux.getPoints()[Position - 1];
                B = Aux.getPoints()[Position + 1];
                aux = ((A.x - B.x) + (A.y - B.y));
                //if that is the case, we return false and skip the point
                if (aux == 0) {
                    repeated = false;
                    Repetidas++;
                    return repeated;
                } else {
                    //else we return true and operate with the point
                    repeated = true;
                    return repeated;
                }
            } else {
                //this means that the point is between 3 and (max-3)
                A = Aux.getPoints()[Position - 1];
                B = Aux.getPoints()[Position + 1];
                aux = ((A.x - B.x) + (A.y - B.y));
                //We check if the vicinity of the point has the same x and y
                if (aux == 0) {
                    repeated = false;
                    Repetidas++;
                    return repeated;
                }
                //we only calculate this if we are looking for the current point, not the neighbout
                if (!neighbour) {

                    A = Aux.getPoints()[Position - 3];
                    B = Aux.getPoints()[Position + 3];
                    aux = ((A.x - B.x) + (A.y - B.y));
                    //Repeating the process for vicinity 3
                    if (aux == 0) {
                        repeated = false;
                        Repetidas2++;
                        return repeated;
                    }
                }
            }
        }
        return repeated;
    }

    private RectF GetRectangleSearch(LinkedList<Integer> index) {
        RectF R = ListObjectsSearch.get(index.get(0)).getRect();
        if (index.size() > 1) {
            for (Integer iterator : index) {
                RectF aux = ListObjectsSearch.get(iterator).getRect();
                R.union(aux);
            }
        }
        return R;
    }

    private RectF GetRectangle(LinkedList<Integer> index) {
        RectF R = ListObjectsLoad.get(index.get(0)).getRect();
        if (index.size() > 1) {
            for (Integer iterator : index) {
                RectF aux = ListObjectsLoad.get(iterator).getRect();
                R.union(aux);
            }
        }
        return R;
    }

    private double CalculateDistance(SpenObjectStroke A, SpenObjectStroke B) {
        double MaxX = -999999.999;
        double MinX = 999999.999;
        double Dif = 0;
        double dx, dy;
        //Buscamos la X más a la derecha del primer objeto
        for (double iterator : A.getXPoints()) {
            if (MaxX < iterator) {
                MaxX = iterator;
            }
        }
        //Y la X más a la izquierda del segundo objeto
        for (double iterator : B.getXPoints()) {
            if (MinX > iterator) {
                MinX = iterator;
            }
        }
        //Calculamos la cercanía y en función de eso, determinamos un umbral
        Dif = Math.abs(MaxX - MinX);
        dx = Math.abs(A.getRect().centerX() - B.getRect().centerX());
        dy = Math.abs(A.getRect().centerY() - B.getRect().centerY());
        //Si el siguiente objeto está hacia la izquierda del primero, revisamos que se trate de acentos
        if ((MaxX > MinX) & (Dif > umbral) & (dy < new_line)) {
            Dif = dx;
//            Dif = Math.abs(MaxX - MinX);
//            for (double iterator : B.getXPoints()) {
//                Aux = Math.abs(MaxX - iterator);
//                if (Aux < Dif) {
//                    Dif = Aux;
//                }
//            }
            return Dif;
        }
        return Dif;
    }

    private void CalculateUmbral() {
        double n, sum = 0, extra = 0;
        Distancia.clear();
        n = ListObjectsLoad.size();
        for (int i = 0; i < ListObjectsLoad.size() - 1; i++) {
            Distancia.add(CalculateDistance(ListObjectsLoad.get(i), ListObjectsLoad.get(i + 1)));
            sum += CalculateDistance(ListObjectsLoad.get(i), ListObjectsLoad.get(i + 1));
        }
        umbral = (sum / n) - extra;
    }

    private float GetTotalWords() {
        float Total = 1;
        for (int i = 0; i < Distancia.size(); i++) {
            if (Distancia.get(i) > umbral) {
                Total++;
            }
        }
        return Total;
    }

    private LinkedList GetWord(float position, float TotalW) {
        int Palabra = 0;
        LinkedList<Integer> IndicesBase = new LinkedList<>();

        if (position < TotalW) {
            for (int i = 0; i < Distancia.size(); i++) {
                if (Distancia.get(i) > umbral) {
                    if (Palabra == position) {
                        IndicesBase.add(i);
                        return IndicesBase;
                    }
                    Palabra++;
                } else {
                    if (Palabra == position) {
                        IndicesBase.add(i);
                    }
                }
            }
            return IndicesBase;
        }
        return IndicesBase;
    }

    private void GetFeatureVectorsSearch() {
        for (int i = 0; i < ListObjectsSearchNormalized.size(); i++) {
            SpenObjectStroke Aux = ListObjectsSearchNormalized.get(i);
            for (int j = 0; j < Aux.getPoints().length; j++) {
                double aux;
                if (isWorkable(Aux, j, false) & isWorkable(Aux, j - 1, true) & isWorkable(Aux, j + 1, true)) {
                    FeatureVectorClass FV = new FeatureVectorClass();
                    FV.setX_coordinate(Aux.getXPoints()[j]);
                    FV.setY_coordinate(Aux.getYPoints()[j]);
                    //True for the Cosinus direction
                    aux = CalculateWritingDirection(Aux, j, true);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setWriting_direction_X(aux);
                    } else {
                        Repetidas3++;
                    }
                    //False for the Sinus direction
                    aux = CalculateWritingDirection(Aux, j, false);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setWriting_direction_Y(aux);
                    } else {
                        Repetidas3++;
                    }
                    //True for the Cosinus Curvature
                    aux = CalculateCurvature(Aux, j, true);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setCurvature_X(aux);
                    } else {
                        Repetidas3++;
                        aux = CalculateCurvature(Aux, j, true);
                    }
                    //False for the Sinus Curvature
                    aux = CalculateCurvature(Aux, j, false);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setCurvature_Y(aux);
                    } else {
                        Repetidas3++;
                    }
                    aux = CalculateVicinityAspect(Aux, j);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setvicinity_aspect(aux);
                    } else {
                        Repetidas3++;
                    }
                    //True for the Cosinus slope
                    aux = CalculateVicinitySlope(Aux, j, true);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setVicinity_slope_X(aux);
                    } else {
                        Repetidas3++;
                    }
                    //False for the Sinus slope
                    aux = CalculateVicinitySlope(Aux, j, false);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setVicinity_slope_Y(aux);
                    } else {
                        Repetidas3++;
                    }
                    aux = CalculateVicinityCurliness(Aux, j);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setvicinity_curliness(aux);
                    } else {
                        Repetidas3++;
                    }
                    aux = CalculateVicinityLinearity(Aux, j);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setvicinity_linearity(aux);
                    } else {
                        Repetidas3++;
                    }
                    double distance = Double.MAX_VALUE;
                    int cluster = -1;
                    double sum;
                    //Then, we calculate the vector type (closest centroid) for the current feature vector
                    for (int l = 0; l < FeaturesCentroids.size(); l++) {
                        aux = FeatureVectorClass.distance(FV, FeaturesCentroids.get(l));
                        if (aux < distance) {
                            distance = aux;
                            cluster = l;
                        }
                    }
                    FV.setCluster(cluster);
                    sum = ListHistogramsSearch.get(cluster);
                    sum++;
                    ListHistogramsSearch.set(cluster, sum);
                }
            }
        }
    }

    private void GetFeatureVectors2(float TotalW) {
        int Palabra = 0;
        float Progress = 0;
        float cast;
        float n = ListObjectsLoadNormalized.size();
        Indices = GetWord(Palabra, TotalW);
        for (int i = 0; i < ListObjectsLoadNormalized.size(); i++) {
            int lastobject = Indices.getLast();
            if (lastobject < i) {
                Palabra++;
                Indices = GetWord(Palabra, TotalW);
            }
            SpenObjectStroke Aux = ListObjectsLoadNormalized.get(i);
            for (int j = 0; j < Aux.getPoints().length; j++) {
                double aux;
                if (isWorkable(Aux, j, false) & isWorkable(Aux, j - 1, true) & isWorkable(Aux, j + 1, true)) {
                    FeatureVectorClass FV = new FeatureVectorClass();
                    FV.setWord(Palabra);
                    FV.setX_coordinate(Aux.getXPoints()[j]);
                    FV.setY_coordinate(Aux.getYPoints()[j]);
                    //True for the Cosinus direction
                    aux = CalculateWritingDirection(Aux, j, true);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setWriting_direction_X(aux);
                    } else {
                        Repetidas3++;
                    }
                    //False for the Sinus direction
                    aux = CalculateWritingDirection(Aux, j, false);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setWriting_direction_Y(aux);
                    } else {
                        Repetidas3++;
                    }
                    //True for the Cosinus Curvature
                    aux = CalculateCurvature(Aux, j, true);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setCurvature_X(aux);
                    } else {
                        Repetidas3++;
                        aux = CalculateCurvature(Aux, j, true);
                    }
                    //False for the Sinus Curvature
                    aux = CalculateCurvature(Aux, j, false);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setCurvature_Y(aux);
                    } else {
                        Repetidas3++;
                    }
                    aux = CalculateVicinityAspect(Aux, j);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setvicinity_aspect(aux);
                    } else {
                        Repetidas3++;
                    }
                    //True for the Cosinus slope
                    aux = CalculateVicinitySlope(Aux, j, true);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setVicinity_slope_X(aux);
                    } else {
                        Repetidas3++;
                    }
                    //False for the Sinus slope
                    aux = CalculateVicinitySlope(Aux, j, false);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setVicinity_slope_Y(aux);
                    } else {
                        Repetidas3++;
                    }
                    aux = CalculateVicinityCurliness(Aux, j);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setvicinity_curliness(aux);
                    } else {
                        Repetidas3++;
                    }
                    aux = CalculateVicinityLinearity(Aux, j);
                    if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
                        FV.setvicinity_linearity(aux);
                    } else {
                        Repetidas3++;
                    }
                    double distance = Double.MAX_VALUE;
                    int cluster = -1;
                    double sum;
                    //Then, we calculate the vector type (closest centroid) for the current feature vector
                    for (int l = 0; l < FeaturesCentroids.size(); l++) {
                        aux = FeatureVectorClass.distance(FV, FeaturesCentroids.get(l));
                        if (aux < distance) {
                            distance = aux;
                            cluster = l;
                        }
                    }
                    //And save it in the variable cluster
                    FV.setCluster(cluster);
                    sum = ListHistogramsLoad.get((int) Palabra).get(cluster);
                    sum++;
                    ListHistogramsLoad.get((int) Palabra).set(cluster, sum);
                    Features.add(FV);
                }
            }
//            cast = i;
//            Progress = (cast/n)*100;
//            mProgressBar.setProgress((int) Progress);
//            mSpenSurfaceView.update();
        }
    }

//    private void GetFeatureVectors(float TotalW) {
//        for (int i = 0; i < TotalW; i++) {
//            float Palabra = i;
//            Indices = GetWord(i, TotalW);
//            for (Integer iterator : Indices) {
//                SpenObjectStroke Aux = ListObjectsLoadNormalized.get(iterator);
//                for (int j = 0; j < Aux.getPoints().length; j++) {
//                    double aux;
//                    if (isWorkable(Aux, j, false) & isWorkable(Aux, j - 1, true) & isWorkable(Aux, j + 1, true)) {
//                        FeatureVectorClass FV = new FeatureVectorClass();
//                        FV.setWord(Palabra);
//                        FV.setX_coordinate(Aux.getXPoints()[j]);
//                        FV.setY_coordinate(Aux.getYPoints()[j]);
//                        //True for the Cosinus direction
//                        aux = CalculateWritingDirection(Aux, j, true);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setWriting_direction_X(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        //False for the Sinus direction
//                        aux = CalculateWritingDirection(Aux, j, false);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setWriting_direction_Y(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        //True for the Cosinus Curvature
//                        aux = CalculateCurvature(Aux, j, true);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setCurvature_X(aux);
//                        } else {
//                            Repetidas3++;
//                            aux = CalculateCurvature(Aux, j, true);
//                        }
//                        //False for the Sinus Curvature
//                        aux = CalculateCurvature(Aux, j, false);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setCurvature_Y(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        aux = CalculateVicinityAspect(Aux, j);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setvicinity_aspect(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        //True for the Cosinus slope
//                        aux = CalculateVicinitySlope(Aux, j, true);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setVicinity_slope_X(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        //False for the Sinus slope
//                        aux = CalculateVicinitySlope(Aux, j, false);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setVicinity_slope_Y(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        aux = CalculateVicinityCurliness(Aux, j);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setvicinity_curliness(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        aux = CalculateVicinityLinearity(Aux, j);
//                        if ((Double.isNaN(aux) == false) & (Double.isInfinite(aux) == false)) {
//                            FV.setvicinity_linearity(aux);
//                        } else {
//                            Repetidas3++;
//                        }
//                        double distance = Double.MAX_VALUE;
//                        int cluster = -1;
//                        double sum;
//                        //Then, we calculate the vector type (closest centroid) for the current feature vector
//                        for (int l = 0; l < FeaturesCentroids.size(); l++) {
//                            aux = FeatureVectorClass.distance(FV, FeaturesCentroids.get(l));
//                            if (aux < distance) {
//                                distance = aux;
//                                cluster = l;
//                            }
//                        }
//                        //And save it in the variable cluster
//                        FV.setCluster(cluster);
//                        sum = ListHistogramsLoad.get((int) Palabra).get(cluster);
//                        sum++;
//                        ListHistogramsLoad.get((int) Palabra).set(cluster, sum);
//                        Features.add(FV);
//                    }
//                }
//            }
//        }
//    }

    private double CalculateCosSin(SpenObjectStroke Aux, int Position, int Vicinity_Distance, boolean Cosinus) {
        double Cos = 0;
        double Sin = 0;
        if (Vicinity_Distance == 1) {
//            PointF A = Aux.getPoints()[Position-Vicinity_Distance];
//            PointF B = Aux.getPoints()[Position+Vicinity_Distance];
            //Cuidamos los extremos para no acceder a posiciones que no existan
            if ((Position < 1) | (Position >= Aux.getPoints().length - 1)) {
                if (Cosinus) {
                    return Cos;
                } else {
                    return Sin;
                }
            } else {
                double S, X, Y;
                X = Aux.getXPoints()[Position - Vicinity_Distance] - Aux.getXPoints()[Position + Vicinity_Distance];
                Y = Aux.getYPoints()[Position - Vicinity_Distance] - Aux.getYPoints()[Position + Vicinity_Distance];
                S = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));
//                //Si el punto anterior y siguiente son el mismo
//                if(S==0){
//                    Repetidas ++;
//                    return Cos;
//                }
                Cos = X / S;
                Sin = Y / S;
                if (Cosinus) {
                    return Cos;
                } else {
                    return Sin;
                }
            }
        } else if (Vicinity_Distance == 3) {
            //Cuidamos los extremos para no acceder a posiciones que no existan
            if ((Position < 3) | (Position >= Aux.getPoints().length - 3)) {
                if (Cosinus) {
                    return Cos;
                } else {
                    return Sin;
                }
            } else {
                double S, X, Y = 0;
                X = Aux.getXPoints()[Position - Vicinity_Distance] - Aux.getXPoints()[Position + Vicinity_Distance];
                Y = Aux.getYPoints()[Position - Vicinity_Distance] - Aux.getYPoints()[Position + Vicinity_Distance];
                S = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));
                //Si el punto anterior y siguiente son el mismo
//                if(S==0){
//                    Repetidas ++;
//                    return Cos;
//                }
                Cos = X / S;
                Sin = Y / S;
                if (Cosinus) {
                    return Cos;
                } else {
                    return Sin;
                }
            }
        } else {
            return 0;
        }
    }

    private double CalculateWritingDirection(SpenObjectStroke Aux, int Position, boolean X) {
        double WritingDirection;
        double Cos, Sin;
        int Vicinity = 1;
        //Vicinity indicates the distance of the neighbourhood taken into account
        //true for getting the Cosinus back, false for the sinus
        Cos = CalculateCosSin(Aux, Position, Vicinity, true);
        Sin = CalculateCosSin(Aux, Position, Vicinity, false);
        if (X) {
            WritingDirection = Cos;
            return WritingDirection;
        } else {
            WritingDirection = Sin;
            return WritingDirection;
        }
    }

    private double CalculateCurvature(SpenObjectStroke Aux, int Position, boolean X) {
        double Curvature;
        double Cos = 0;
        double Sin = 0;
        int Vicinity = 1;

        if ((Position < Vicinity) | (Position >= Aux.getPoints().length - Vicinity)) {
            Curvature = Cos;
            return Curvature;
        } else {
            double A = CalculateCosSin(Aux, Position - 1, Vicinity, true);
            double B = CalculateCosSin(Aux, Position + 1, Vicinity, true);
            double C = CalculateCosSin(Aux, Position - 1, Vicinity, false);
            double D = CalculateCosSin(Aux, Position + 1, Vicinity, false);

            double aux = (A * B) + (C * D);


            Cos = (CalculateCosSin(Aux, Position - 1, Vicinity, true) * CalculateCosSin(Aux, Position + 1, Vicinity, true)) + (CalculateCosSin(Aux, Position - 1, Vicinity, false) * CalculateCosSin(Aux, Position + 1, Vicinity, false));
            Sin = (CalculateCosSin(Aux, Position - 1, Vicinity, true) * CalculateCosSin(Aux, Position + 1, Vicinity, false) - (CalculateCosSin(Aux, Position - 1, Vicinity, false)) * CalculateCosSin(Aux, Position + 1, Vicinity, true));
            if (X) {
                Curvature = Cos;
                return Curvature;
            } else {
                Curvature = Sin;
                return Curvature;
            }
        }
    }

    private double CalculateVicinityAspect(SpenObjectStroke Aux, int Position) {
        double Aspect, Numerador, Denominador;
        int N = Aux.getPoints().length;
        if ((Position < 3) | (Position >= N - 3)) {
            Aspect = 0;
            return Aspect;
        } else {
            Numerador = (Aux.getYPoints()[Position - 3] - Aux.getYPoints()[Position + 3]) * 2;
            Denominador = (Aux.getXPoints()[Position - 3] - Aux.getXPoints()[Position + 3]) + (Aux.getYPoints()[Position - 3] - Aux.getYPoints()[Position + 3]);
//            if(Denominador == 0){
//                Repetidas2++;
//                return 0;
//            }
            Aspect = (Numerador / Denominador) - 1;
            return Aspect;
        }
    }

    private double CalculateVicinityCurliness(SpenObjectStroke Aux, int Position) {
        double C = 0, L = 0, Max = 0;
        if ((Position < 3) | (Position >= Aux.getPoints().length - 3)) {
            return C;
        } else {
            for (int i = -3; i < 3; i++) {
                L = L + DistanceCurliness(Aux.getPoints()[Position + i], Aux.getPoints()[Position + (i + 1)]);
            }
            double MaxX = Math.abs(Aux.getXPoints()[Position - 3] - Aux.getXPoints()[Position + 3]);
            double MaxY = Math.abs(Aux.getYPoints()[Position - 3] - Aux.getYPoints()[Position + 3]);
            Max = Math.max(MaxX, MaxY);
            C = (L / Max) - 2;
            return C;
        }
    }

    private double CalculateVicinitySlope(SpenObjectStroke Aux, int Position, boolean X) {
        double VicinitySlope;
        double Cos, Sin;
        int Vicinity = 3;
        Cos = CalculateCosSin(Aux, Position, Vicinity, true);
        Sin = CalculateCosSin(Aux, Position, Vicinity, false);
        if (X) {
            VicinitySlope = Cos;
            return VicinitySlope;
        } else {
            VicinitySlope = Sin;
            return VicinitySlope;
        }
    }

    private double CalculateVicinityLinearity(SpenObjectStroke Aux, int Position) {
        double d = 0, aux = 0;
        double N = 7;
        if ((Position < 3) | (Position >= Aux.getPoints().length - 3)) {
            return d;
        } else {
            //We get the two points that delimit the neighbourhood of our current point
            PointF A = Aux.getPoints()[Position - 3];
            PointF B = Aux.getPoints()[Position + 3];

            //We calculate the shortest distance from the point to the line
            for (int i = -2; i < 3; i++) {
                double Distance;
                Distance = Math.abs(((B.y - A.y) * Aux.getXPoints()[Position + i]) - ((B.x - A.x) * Aux.getYPoints()[Position + i]) + (B.x * A.y) - (B.y * A.x)) / Math.sqrt(Math.pow((B.y - A.y), 2) + Math.pow((B.x - A.x), 2));
                Distance = Math.pow(Distance, 2);
                aux = aux + Distance;
            }
            d = (1 / N) * aux;
            return d;
        }
    }


    private double DistanceCurliness(PointF Punto, PointF Punto2) {
        float x1 = Punto.x;
        float y1 = Punto.y;
        float x2 = Punto2.x;
        float y2 = Punto2.y;
        double Distancia;

        Distancia = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
        return Distancia;
    }

    private void SetVisibilityFalse(LinkedList<Integer> Index, LinkedList<SpenObjectStroke> A) {
        for (int i = 0; i < Index.size(); i++) {
            A.get(Index.get(i)).setVisibility(false);
        }
    }

    private void SetVisibilityTrue(LinkedList<Integer> Index, LinkedList<SpenObjectStroke> A) {
        for (int i = 0; i < Index.size(); i++) {
            A.get(Index.get(i)).setVisibility(true);
        }
    }

    private void NormalizeDataSearch() {
        float offset = 150;
        for (int i = 0; i < ListObjectsSearch.size(); i++) {
            //Para cada índice, encontraremos el objeto al que señala y reescalaremos su rectángulo
            float bottom, top, left, right;
            double AR;
            SpenObjectStroke Aux = new SpenObjectStroke();
            //Asignamos al objeto creado una copia del objeto que está cargado en el documento
            Aux.copy(ListObjectsSearch.get(i));
            //Truco para calcular el rectangulo
            int size = ListObjectsSearch.size();
            LinkedList<Integer> Indices = new LinkedList<>();
            for (int j = 0; j < size; j++) {
                Indices.add(j, j);
            }
            //Creamos y asignamos a este nuevo objeto el rectángulo que engloba a TODA la palabra
            RectF aux = GetRectangleSearch(Indices);
            Aux.setRect(aux, true);
            //Calculamos aspect ratio
            AR = aux.width() / aux.height();
            //Asignamos al bottom, que por alguna razón está por encima del top, el valor de la altura que queremos que tenga
//            bottom = aux.top + offset;
            bottom = offset;
            //En función de la altura y del AR, calculamos la X
//            right = (float) (aux.left + (offset * AR));
            right = (float) (offset * AR);
            //Dejamos left y top igual
//            left = aux.left;
//            top = aux.top;
            left = 0;
            top = 0;
            //Creamos rectángulo con las coordenadas calculadas
            RectF R = new RectF(left, top, right, bottom);
            //Por último, escalamos. Importante el booleano a false
            Aux.setRect(R, false);
            //Establecemos tamaño de la línea
            Aux.setPenSize(5);
            //Asignamos el objeto en la nueva lista
            ListObjectsSearchNormalized.add(Aux);
        }
    }

    private void NormalizeData() {
        //offset = altura que tendrán todas nuestras palabras
        float offset = 150;
        //First we put the black tint to the loaded strokes
        for (SpenObjectStroke iterator : ListObjectsLoad) {
            iterator.setColor(0);
        }

        for (int j = 0; j < TotalWords; j++) {
            //Para cada palabra, cogemos sus indices
            Indices = GetWord(j, TotalWords);
            for (int i = 0; i < Indices.size(); i++) {
                //Para cada índice, encontraremos el objeto al que señala y reescalaremos su rectángulo
                float bottom, top, left, right;
                double AR;
                SpenObjectStroke Aux = new SpenObjectStroke();
                //Asignamos al objeto creado una copia del objeto que está cargado en el documento
                Aux.copy(ListObjectsLoad.get(Indices.get(i)));
                //Creamos y asignamos a este nuevo objeto el rectángulo que engloba a TODA la palabra
                RectF aux = GetRectangle(Indices);
                Aux.setRect(aux, true);
                //Calculamos aspect ratio
                AR = aux.width() / aux.height();
                //Asignamos al bottom, que por alguna razón está por encima del top, el valor de la altura que queremos que tenga
//                bottom = aux.top + offset;
                bottom = offset;
                //En función de la altura y del AR, calculamos la X
//                right = (float) (aux.left + (offset * AR));
                right = (float) (offset * AR);
                //Dejamos left y top igual
//                left = aux.left;
//                top = aux.top;
                left = 0;
                top = 0;
                //Creamos rectángulo con las coordenadas calculadas
                RectF R = new RectF(left, top, right, bottom);
                //Por último, escalamos. Importante el booleano a false
                Aux.setRect(R, false);
                //Establecemos tamaño de la línea
                Aux.setPenSize(5);
                //Asignamos el objeto en la nueva lista
                ListObjectsLoadNormalized.add(Aux);
            }
        }
    }

    private String[] setFileList() {
        // Call the file list under the directory in mFilePath.
        if (!mFilePath.exists()) {
            if (!mFilePath.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        // Filter in spd and png files.
        File[] fileList = mFilePath.listFiles(new txtFileFilter());
        if (fileList == null) {
            Toast.makeText(mContext, "File does not exist.", Toast.LENGTH_SHORT).show();
            return null;
        }

        int i = 0;
        String[] strFileList = new String[fileList.length];
        for (File file : fileList) {
            strFileList[i++] = file.getName();
        }

        return strFileList;
    }

    static class txtFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(".spd") || name.endsWith(".png"));
        }
    }

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                if (mMode == MODE_PEN) {
                    SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                    penInfo.color = color;
                    mPenSettingView.setInfo(penInfo);
                } else if (mMode == MODE_TEXT_OBJ || mMode == MODE_SHAPE_OBJ) {
                    SpenSettingTextInfo textInfo = mSpenSurfaceView.getTextSettingInfo();
                    textInfo.color = color;
                    mTextSettingView.setInfo(textInfo);
                }
            }
        }
    };

    SpenTextChangeListener mTextChangeListener = new SpenTextChangeListener() {

        @Override
        public boolean onSelectionChanged(int arg0, int arg1) {
            return false;
        }

        @Override
        public void onMoreButtonDown(SpenObjectTextBox arg0) {
        }

        @Override
        public void onChanged(SpenSettingTextInfo info, int state) {
            if (mTextSettingView != null) {
                if (state == CONTROL_STATE_SELECTED) {
                    mTextSettingView.setInfo(info);
                }
            }
        }

        @Override
        public void onFocusChanged(boolean gainFocus) {
            if (mTextSettingView != null) {
                if (gainFocus == true) {
                    // show text setting
                    mTextSettingView.setVisibility(View.VISIBLE);
                    mPreMode = mMode;
                    mMode = MODE_TEXT_OBJ;
                } else {
                    // hide text setting
                    mTextSettingView.setVisibility(View.GONE);
                    mMode = mPreMode;
                }
            }
        }
    };

    private void enableButton(boolean isEnable) {
        //mPenBtn.setEnabled(isEnable);
        mLoadFileBtn.setEnabled(isEnable);
        mSearchBtn.setEnabled(isEnable);
        mAcceptBtn.setEnabled(isEnable);
        mClearBtn.setEnabled(isEnable);
    }

    private void selectButton(View v) {
        // Enable or disable the button according to the current mode.
        //mPenBtn.setSelected(false);
        mSearchBtn.setSelected(false);
        mAcceptBtn.setSelected(false);
        mClearBtn.setSelected(false);

        v.setSelected(true);

        closeSettingView();
    }

    private void closeSettingView() {
        // Close all the setting views.
        mPenSettingView.setVisibility(SpenSurfaceView.GONE);
        mTextSettingView.setVisibility(SpenSurfaceView.GONE);
    }

    @Override
    public void onBackPressed() {
        if (mSpenPageDoc.getObjectCount(true) > 0 && mSpenPageDoc.isChanged()) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
            dlg.setIcon(mContext.getResources().getDrawable(android.R.drawable.ic_dialog_alert));
            dlg.setTitle(mContext.getResources().getString(R.string.app_name))
                    .setMessage("Do you want to exit after save?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (checkPermission()) {
                                return;
                            }
                            saveNoteFile(true);
                            dialog.dismiss();
                        }
                    }).setNeutralButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mIsDiscard = true;
                    finish();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
            dlg = null;
        } else {
            super.onBackPressed();
        }
    }

    private void updatePropertiesDiablogLayout() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getWindowManager().getDefaultDisplay().getRectSize(mScreenRect);

        RelativeLayout layout = (RelativeLayout) mShapePropertiesDialog.findViewById(R.id.shapePropertiesLayout);
        LayoutParams params = layout.getLayoutParams();

        params.height = (int) (335 * (metrics.densityDpi / 160f));
        if (400 * (metrics.densityDpi / 160f) > mScreenRect.height()) {
            params.height = mScreenRect.height() - 200;
            layout.setLayoutParams(params);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mSpenSurfaceView.cancelStroke();

        if (mShapePropertiesDialog != null && mShapePropertiesDialog.isShowing()) {
            updatePropertiesDiablogLayout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPenSettingView != null) {
            mPenSettingView.close();
        }
        if (mTextSettingView != null) {
            mTextSettingView.close();
        }

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.closeControl();
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }

        if (mSpenNoteDoc != null) {
            try {
                if (mIsDiscard) {
                    mSpenNoteDoc.discard();
                } else {
                    mSpenNoteDoc.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    }

    ;
    public static final int SDK_VERSION = Build.VERSION.SDK_INT;
    private static final int PEMISSION_REQUEST_CODE = 1;

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermission() {
        if (SDK_VERSION < 23) {
            return false;
        }
        List<String> permissionList = new ArrayList<String>(Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE));
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionList.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionList.remove(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionList.size() > 0) {
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), PEMISSION_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PEMISSION_REQUEST_CODE) {
            if (grantResults != null) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "permission: " + permissions[i] + " is denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}