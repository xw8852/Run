package com.android.ivymobi.pedometer;

import java.util.Calendar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ivymobi.pedometer.util.DateUtils;
import com.android.ivymobi.runapp.R;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;

@InjectActivity(id = R.layout.history_activity)
public class DataHitoryActivity extends BaseActivity {
    @InjectView(id = R.id.dates)
    RadioGroup mGroup;
    @InjectView(id = R.id.title_left)
    View mLeftTitle;
    @InjectView(id = R.id.title)
    TextView mTitleView;
    @InjectView(id = R.id.title_right)
    View mRightTitle;
    @InjectView(id = R.id.speed)
    TextView mSpeed;
    @InjectView(id = R.id.distance)
    TextView mDistance;
    @InjectView(id = R.id.maxSpeed)
    TextView mMaxSpeed;
    @InjectView(id = R.id.times)
    TextView mTimes;
    @InjectView(id = R.id.view1)
    LinearLayout mChartLayout;
    @InjectView(id = R.id.d_left)
    View mLeft;
    @InjectView(id = R.id.d_right)
    View mRight;
    @InjectView(id = R.id.d_content)
    TextView mDContent;
    GraphicalView mchartView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
        mTitleView.setText("历史统计");
        double[] minValues = new double[] { 55, 50, 40, 30, 20, 20, 30, 40, 50, 55 };
        double[] maxValues = new double[] { 85, 95, 100, 95, 85, 85, 95, 100, 95, 85 };
        // 用于 保存点集数据 ，包括每条曲线的X，Y坐标

        final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();// 使用与柱状图
        RangeCategorySeries series = new RangeCategorySeries("");// 括号内为图表底部的文字
        for (int k = 0; k < minValues.length; k++) {
            series.add("1月", 0, maxValues[k]);
        }
        dataset.addSeries(series.toXYSeries());

        int[] colors = new int[] { Color.BLACK };// 青色蓝绿色
        final XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        setChartSettings(renderer, "标题", "x", "y", 0.5, 12.5, 0, 150, Color.GRAY, Color.LTGRAY);// 画笔的颜色预先定义成浅灰色

        renderer.setBarSpacing(0.01);// 设置间距
        renderer.setXLabels(0);// 设置 X 轴不显示数字（改用我们手动添加的文字标签））;//设置X轴显示的刻度标签的个数
        for (int i = 0; i < minValues.length; i++) {
            renderer.addXTextLabel(i + 1, i + "月");
        }
        renderer.setYLabels(15);// 设置合适的刻度，在轴上显示的数量是 MAX / labels
        renderer.setMargins(new int[] { 30, 70, 10, 0 });// 图形 4 边距 设置4边留白
                                                         // 设置图表的外边框
        renderer.setPanLimits(new double[] { 0, 20, 0, 100 });
        renderer.setYLabelsAlign(Align.RIGHT);// 设置y轴显示的分列，默认是 Align.CENTER
        renderer.setPanEnabled(true, false);// 设置x方向可以滑动，y方向不可以滑动
        renderer.setZoomEnabled(false, false);// 设置x，y方向都不可以放大或缩小
        renderer.setClickEnabled(true);
        SimpleSeriesRenderer r = renderer.getSeriesRendererAt(0);
        r.setDisplayChartValues(true);// 设置是否在主题上方显示值
        r.setChartValuesTextSize(24);// 柱体上方字的大小
        r.setChartValuesSpacing(3);// 柱体上方字的与柱体顶部的距离
        r.setGradientEnabled(true);
        r.setGradientStart(-20, Color.WHITE);
        r.setGradientStop(100, 0xff426c78);
        mchartView = ChartFactory.getRangeBarChartView(this, dataset, renderer, Type.DEFAULT);
        mChartLayout.removeAllViews();
        mChartLayout.addView(mchartView);

        mGroup.setOnCheckedChangeListener(checkedChangeListener);
        mLeft.setOnClickListener(changeDateClickListener);
        mRight.setOnClickListener(changeDateClickListener);
        checkedChangeListener.onCheckedChanged(mGroup, R.id.day);
        mchartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeriesSelection seriesSelection = mchartView.getCurrentSeriesAndPoint();
                double[] xy = mchartView.toRealPoint(0);
                if (seriesSelection == null) {
                    System.out.println("No chart element was clicked");

                } else {
                    if(dataset.getSeriesCount()>1){
                        dataset.removeSeries(1);
                    }
                    RangeCategorySeries series = new RangeCategorySeries("");
//                    series.add(seriesSelection.getXValue(), 0);
                    series.add(0, seriesSelection.getValue());
                    dataset.addSeries(series.toXYSeries());
                    mchartView = ChartFactory.getRangeBarChartView(DataHitoryActivity.this, dataset, renderer, Type.DEFAULT);
                    mChartLayout.removeAllViews();
                    mChartLayout.addView(mchartView);
                    System.out.println("Chart element in series index " + seriesSelection.getSeriesIndex() + " data point index "
                            + seriesSelection.getPointIndex() + " was clicked" + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                            + seriesSelection.getValue() + " clicked point value X=" + (float) xy[0] + ", Y=" + (float) xy[1]);
                }
            }
        });
    }

    View.OnClickListener changeDateClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            if (v.getId() == R.id.d_left)
                --dx;
            else {
                if (dx == 0)
                    return;
                dx = Math.min(0, ++dx);
            }
            if (dx == 0) {
                checkedChangeListener.onCheckedChanged(mGroup, mGroup.getCheckedRadioButtonId());
                return;
            }
            switch (mGroup.getCheckedRadioButtonId()) {
            case R.id.day:
                calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR) + dx);
                mDContent.setText("第" + calendar.get(Calendar.WEEK_OF_YEAR) + "周");
                calendar.set(Calendar.DAY_OF_WEEK, 1);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.DAY_OF_WEEK, 0);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                endDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                pushChartData(startDate, endDate);
                break;
            case R.id.month:
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + dx);
                mDContent.setText((calendar.get(Calendar.MONTH) + 1) + "月");
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
                calendar.set(Calendar.DAY_OF_MONTH, 0);
                endDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                pushChartData(startDate, endDate);
                break;
            case R.id.year:
                calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + dx);
                mDContent.setText(calendar.get(Calendar.YEAR) + "");
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
                calendar.set(Calendar.DAY_OF_YEAR, 0);
                endDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                pushChartData(startDate, endDate);
                break;

            default:
                break;
            }
        }

    };
    int dx = 0;
    RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Calendar calendar = null;
            switch (group.getCheckedRadioButtonId()) {
            case R.id.day:
                calendar = Calendar.getInstance();
                mDContent.setText("本周");
                calendar.set(Calendar.DAY_OF_WEEK, 1);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.DAY_OF_WEEK, 0);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                endDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                pushChartData(startDate, endDate);
                break;
            case R.id.month:
                mDContent.setText("本月");
                calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
                calendar.set(Calendar.DAY_OF_MONTH, 0);
                endDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                pushChartData(startDate, endDate);
                break;
            case R.id.year:
                mDContent.setText("今年");
                calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
                calendar.set(Calendar.DAY_OF_YEAR, 0);
                endDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                pushChartData(startDate, endDate);
                break;

            default:
                break;
            }
        }

    };
    String startDate;
    String endDate;

    public void pushChartData(String startDate, String endDate) {
        System.out.println("startDate--" + startDate);
        System.out.println("endDate--" + endDate);
        // showLoadingDialog(R.string.loadingData);
        // Request request = new Request(Config.SEVER_WORKOUT_HISTORY +
        // "?session_id=" + UserUtil.getSession() + "&start_date=" + startDate
        // + "&end_date=" + endDate);
        // Manager.getInstance().execute(Manager.CMD_GET_STRING, request, null);
    }

    protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(colors[i]);
            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle, String yTitle, double xMin, double xMax,
            double yMin, double yMax, int axesColor, int labelsColor) {
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.WHITE);
        // renderer.setChartTitle(title);
        // renderer.setXTitle(xTitle);
        // renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(Color.BLACK);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
    }
}
