package com.al.boneylink.toiletcommon.views;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.al.boneylink.corebaselib.utils.DisplayUtil;
import com.al.boneylink.toiletcommon.R;
import com.al.boneylink.toiletcommon.presenter.SXToiletPresenter;
import com.al.boneylink.toiletcommon.utils.FileUtils;
import com.al.boneylink.toiletcommon.utils.PointNewLocal;
import com.al.boneylink.toiletcommon.utils.ToiletFileUtils;
import com.al.boneylink.toiletcommon.widgets.MovableNewItem;
import com.al.boneylink.toiletcommon.widgets.SXSatisfaction;
import com.al.boneylink.toiletcommon.widgets.SXSensorItem;
import com.al.boneylink.toiletcommon.widgets.SXSensorItem2;
import com.al.boneylink.toiletcommon.widgets.SXToiletMarkItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;

import static com.al.boneylink.toiletcommon.utils.ToiletFileUtils.TOILET_COPY_SUB_PATH;

public class SXToiletActivity extends BaseToiletNewActivity<SXToiletActivity, SXToiletPresenter> {
String TAG="SXToiletActivity";
    @Bind(R.id.movable_area_container)
    RelativeLayout movableAreaContainer;

    @Bind(R.id.main_main_toilet_container)
    public LinearLayout mapContainer;

    @Bind(R.id.video_container)
    VideoView videoView;

    @Bind(R.id.date_time_week)
    TextView dateTimeWeek;

    @Bind(R.id.date_time_date)
    TextView dateTimeDate;

    @Bind(R.id.date_time_time)
    TextView dateTimeTime;

    @Bind(R.id.weather_temp_num)
    public TextView weatherTempNum;

    @Bind(R.id.weather_desp)
    public TextView weatherDesp;

    @Bind(R.id.title_view)
    public TextView titleText;

    @Bind(R.id.root_layout_container)
    public RelativeLayout layoutContainer;

    //绑定男卫生间信息
    @Bind(R.id.man_tempwet)
    public SXSensorItem2 manTempWetView;
    @Bind(R.id.man_pm25)
    public SXSensorItem manPm25View;
    @Bind(R.id.man_nh3)
    public SXSensorItem manNh3View;
    @Bind(R.id.man_h2s)
    public SXSensorItem manH2sView;
    @Bind(R.id.man_ch4)
    public SXSensorItem manCh4View;

    //绑定女卫生间信息
    @Bind(R.id.woman_tempwet)
    public SXSensorItem2 womanTempWetView;
    @Bind(R.id.woman_pm25)
    public SXSensorItem womanPm25View;
    @Bind(R.id.woman_nh3)
    public SXSensorItem womanNh3View;
    @Bind(R.id.woman_h2s)
    public SXSensorItem womanH2sView;
    @Bind(R.id.woman_ch4)
    public SXSensorItem womanCh4View;

    @Bind(R.id.common_pressure)
    public SXSensorItem commonPressureView;


    @Bind(R.id.man_today_num)
    public TextView manTodayNumView;
    @Bind(R.id.man_total_num)
    public TextView manTotalNumView;

    @Bind(R.id.woman_today_num)
    public TextView womanTodayNumView;
    @Bind(R.id.woman_total_num)
    public TextView womanTotalNumView;

    public SXToiletMarkItem manMarkItem;
    public SXToiletMarkItem womanMarkItem;

    public MovableNewItem<SXToiletMarkItem> manMovableItem;
    public MovableNewItem<SXToiletMarkItem> womanMovableItem;

    @Bind(R.id.invest_container)
    public SXSatisfaction investContainer;

    Handler handler = new Handler();

    Context context;

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        context = this;
        setDateTimeVal();
        clockTicTok();

        manMarkItem = new SXToiletMarkItem(getViewContext());
        manMarkItem.setIconName("sx_ic_t_man");
        womanMarkItem = new SXToiletMarkItem(getViewContext());
        womanMarkItem.setIconName("sx_ic_t_woman");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //this指当前activity
        int parentWidth = dm.widthPixels;
        int parentHeight = dm.heightPixels;

        womanMovableItem = new MovableNewItem<>(this, new PointNewLocal(0.45, 0.155, parentWidth, parentHeight), womanMarkItem);
        manMovableItem = new MovableNewItem<>(this, new PointNewLocal(0.165, 0.440, parentWidth, parentHeight), manMarkItem);

        movableAreaContainer.addView(manMovableItem);
        movableAreaContainer.addView(womanMovableItem);

        manMovableItem.setMoveable(true);
        womanMovableItem.setMoveable(true);
    }

    @Override
    public RelativeLayout getMovableAreaContainer() {
        return movableAreaContainer;
    }

    @Override
    public View getMapContainer() {
        return mapContainer;
    }

    @Override
    public Map<String, Integer> getImageSources() {
        Map<String, Integer> imageSources = new HashMap<>(0);
       