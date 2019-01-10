package com.samsung.android.sdk.pen.pg.tool;

import java.util.ArrayList;
import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.samsung.spensdk3.example.R;

public class ShapeAdapter extends ArrayAdapter<Integer> {

    Context mContext;
    public static SparseIntArray mMapShapeLineDrawable = null;;

    public ShapeAdapter(Context context, ArrayList<Integer> shapes) {
        super(context, 0, shapes);
        mContext = context;
        if (mMapShapeLineDrawable != null) {
            return;
        }

        mMapShapeLineDrawable = new SparseIntArray();

        mMapShapeLineDrawable.put(0, R.drawable.shape_icon_1);
        mMapShapeLineDrawable.put(1, R.drawable.shape_icon_2);
        mMapShapeLineDrawable.put(2, R.drawable.shape_icon_3);
        mMapShapeLineDrawable.put(3, R.drawable.shape_icon_4);
        mMapShapeLineDrawable.put(4, R.drawable.shape_icon_5);
        mMapShapeLineDrawable.put(5, R.drawable.shape_icon_6);
        mMapShapeLineDrawable.put(6, R.drawable.shape_icon_7);
        mMapShapeLineDrawable.put(7, R.drawable.shape_icon_8);
        mMapShapeLineDrawable.put(8, R.drawable.shape_icon_9);
        mMapShapeLineDrawable.put(9, R.drawable.shape_icon_10);
        mMapShapeLineDrawable.put(10, R.drawable.shape_icon_11);
        mMapShapeLineDrawable.put(11, R.drawable.shape_icon_12);
        mMapShapeLineDrawable.put(12, R.drawable.shape_icon_13);
        mMapShapeLineDrawable.put(13, R.drawable.shape_icon_14);
        mMapShapeLineDrawable.put(14, R.drawable.shape_icon_15);
        mMapShapeLineDrawable.put(15, R.drawable.shape_icon_16);
        mMapShapeLineDrawable.put(16, R.drawable.shape_icon_17);
        mMapShapeLineDrawable.put(17, R.drawable.shape_icon_18);
        mMapShapeLineDrawable.put(18, R.drawable.shape_icon_19);
        mMapShapeLineDrawable.put(19, R.drawable.shape_icon_20);
        mMapShapeLineDrawable.put(20, R.drawable.shape_icon_21);
        mMapShapeLineDrawable.put(21, R.drawable.shape_icon_22);
        mMapShapeLineDrawable.put(22, R.drawable.shape_icon_23);
        mMapShapeLineDrawable.put(23, R.drawable.shape_icon_24);
        mMapShapeLineDrawable.put(24, R.drawable.shape_icon_25);
        mMapShapeLineDrawable.put(25, R.drawable.shape_icon_26);
        mMapShapeLineDrawable.put(26, R.drawable.shape_icon_27);
        mMapShapeLineDrawable.put(27, R.drawable.shape_icon_28);
        mMapShapeLineDrawable.put(28, R.drawable.shape_icon_29);
        mMapShapeLineDrawable.put(29, R.drawable.shape_icon_30);
        mMapShapeLineDrawable.put(30, R.drawable.shape_icon_31);
        mMapShapeLineDrawable.put(31, R.drawable.shape_icon_32);
        mMapShapeLineDrawable.put(32, R.drawable.shape_icon_33);
        mMapShapeLineDrawable.put(33, R.drawable.shape_icon_34);
        mMapShapeLineDrawable.put(34, R.drawable.shape_icon_35);
        mMapShapeLineDrawable.put(35, R.drawable.shape_icon_36);
        mMapShapeLineDrawable.put(36, R.drawable.shape_icon_37);
        mMapShapeLineDrawable.put(37, R.drawable.shape_icon_38);
        mMapShapeLineDrawable.put(38, R.drawable.shape_icon_39);
        mMapShapeLineDrawable.put(39, R.drawable.shape_icon_40);
        mMapShapeLineDrawable.put(40, R.drawable.shape_icon_41);
        mMapShapeLineDrawable.put(41, R.drawable.shape_icon_42);
        mMapShapeLineDrawable.put(42, R.drawable.shape_icon_43);
        mMapShapeLineDrawable.put(43, R.drawable.shape_icon_44);
        mMapShapeLineDrawable.put(44, R.drawable.shape_icon_45);
        mMapShapeLineDrawable.put(45, R.drawable.shape_icon_46);
        mMapShapeLineDrawable.put(46, R.drawable.shape_icon_47);
        mMapShapeLineDrawable.put(47, R.drawable.shape_icon_48);
        mMapShapeLineDrawable.put(48, R.drawable.shape_icon_49);
        mMapShapeLineDrawable.put(49, R.drawable.shape_icon_50);
        mMapShapeLineDrawable.put(50, R.drawable.shape_icon_51);
        mMapShapeLineDrawable.put(51, R.drawable.shape_icon_52);
        mMapShapeLineDrawable.put(52, R.drawable.shape_icon_53);
        mMapShapeLineDrawable.put(53, R.drawable.shape_icon_54);
        mMapShapeLineDrawable.put(54, R.drawable.shape_icon_55);
        mMapShapeLineDrawable.put(55, R.drawable.shape_icon_56);
        mMapShapeLineDrawable.put(56, R.drawable.shape_icon_57);
        mMapShapeLineDrawable.put(57, R.drawable.shape_icon_58);
        mMapShapeLineDrawable.put(58, R.drawable.shape_icon_59);
        mMapShapeLineDrawable.put(59, R.drawable.shape_icon_60);
        mMapShapeLineDrawable.put(60, R.drawable.shape_icon_61);
        mMapShapeLineDrawable.put(61, R.drawable.shape_icon_62);
        mMapShapeLineDrawable.put(62, R.drawable.shape_icon_63);
        mMapShapeLineDrawable.put(63, R.drawable.shape_icon_64);
        mMapShapeLineDrawable.put(64, R.drawable.shape_icon_65);
        mMapShapeLineDrawable.put(65, R.drawable.shape_icon_66);
        mMapShapeLineDrawable.put(66, R.drawable.shape_icon_67);
        mMapShapeLineDrawable.put(67, R.drawable.shape_icon_68);
        mMapShapeLineDrawable.put(68, R.drawable.shape_icon_69);
        mMapShapeLineDrawable.put(69, R.drawable.shape_icon_70);
        mMapShapeLineDrawable.put(70, R.drawable.shape_icon_71);
        mMapShapeLineDrawable.put(71, R.drawable.shape_icon_72);
        mMapShapeLineDrawable.put(72, R.drawable.shape_icon_73);
        mMapShapeLineDrawable.put(73, R.drawable.shape_icon_74);
        mMapShapeLineDrawable.put(74, R.drawable.shape_icon_75);
        mMapShapeLineDrawable.put(75, R.drawable.shape_icon_76);
        mMapShapeLineDrawable.put(76, R.drawable.shape_icon_77);
        mMapShapeLineDrawable.put(77, R.drawable.shape_icon_78);

        // For line
        mMapShapeLineDrawable.put(78, R.drawable.connector_straight);
        mMapShapeLineDrawable.put(79, R.drawable.connector_elbow);
        mMapShapeLineDrawable.put(80, R.drawable.connector_curved);
    }

    @Override
    public Integer getItem(int position) {
        return mMapShapeLineDrawable.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View noteView;
        noteView = new View(mContext);
        noteView = inflater.inflate(R.layout.gridview_item, null);

        ImageView gridImage = (ImageView) noteView.findViewById(R.id.gridImage);

        gridImage.setImageResource(getItem(position));
        return noteView;
    }
}
