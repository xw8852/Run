package com.android.ivymobi.pedometer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.ivymobi.pedometer.data.BaseModel;
import com.android.ivymobi.pedometer.util.DateUtil;
import com.android.ivymobi.pedometer.util.DateUtils;
import com.android.ivymobi.pedometer.util.ToastUtil;
import com.android.ivymobi.pedometer.util.UserUtil;
import com.android.ivymobi.runapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msx7.annotations.Inject;
import com.msx7.annotations.InjectActivity;
import com.msx7.annotations.InjectView;
import com.msx7.core.Manager;
import com.msx7.core.command.IResponseListener;
import com.msx7.core.command.model.Request;
import com.msx7.core.command.model.Response;

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
    @InjectView(id = R.id.ave_speed)
    TextView mAveSpeed;
    GraphicalView mchartView;
    XYMultipleSeriesDataset dataset;
    XYMultipleSeriesRenderer renderer;

    XYSeries waterSeries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Inject.inject(this);
        mTitleView.setText("历史统计");


        mGroup.setOnCheckedChangeListener(checkedChangeListener);
        mLeft.setOnClickListener(changeDateClickListener);
        mRight.setOnClickListener(changeDateClickListener);

        checkedChangeListener.onCheckedChanged(mGroup, R.id.day);
    }

    View.OnClickListener chartClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SeriesSelection seriesSelection = mchartView.getCurrentSeriesAndPoint();
            System.out.println(new Gson().toJson(seriesSelection));
            double[] xy = mchartView.toRealPoint(0);
            if (seriesSelection == null) {
                System.out.println("No chart element was clicked");

            } else {
                execute(DataHitoryActivity.this, waterSeries, (int) seriesSelection.getXValue() - 1);
                return;
            }
        }

    };

    public void execute(Context context, XYSeries waterSeries, int index) {
        String[] titles = new String[] { "Crete Air Temperature" };
        List<double[]> x = new ArrayList<double[]>();

        x.add(new double[] { waterSeries.getX(index), waterSeries.getX(index) });

        List<double[]> values = new ArrayList<double[]>();
        values.add(new double[] { 0, waterSeries.getY(index) });
        int[] colors = new int[] { Color.GREEN };
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
        renderer.setPointSize(5.5f);
        XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(0);
        r.setLineWidth(5);
        r.setFillPoints(true);
        r.setDisplayChartValues(true);
        renderer.setMargins(new int[] { 30, 70, 10, 0 });// 图形 4 边距 设置4边留白
        setChartSettings(renderer, "Weather data", "Month", "Temperature", 0, 10, 0,
                waterSeries.getMaxY() + (waterSeries.getMaxY() / waterSeries.getItemCount()), Color.LTGRAY, Color.LTGRAY);
        renderer.setShowLegend(false);
        renderer.setXLabels(0);
        renderer.setYLabels(10);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GRAY);
        renderer.setXLabelsAlign(Align.RIGHT);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setZoomButtonsVisible(true);
        renderer.setPanLimits(new double[] { -waterSeries.getMaxX() * 50, waterSeries.getMaxX() * 50, 0, waterSeries.getMaxY()* 50, });

        renderer.setPanEnabled(true, false);// 设置x方向可以滑动，y方向不可以滑动
        renderer.setZoomEnabled(false, false);// 设置x，y方向都不可以放大或缩小
        renderer.setLabelsTextSize(30);

        renderer.setBarSpacing(0.5);
        XYSeriesRenderer waterRenderer = new XYSeriesRenderer();
        waterRenderer.setColor(Color.BLACK);
        waterRenderer.setGradientEnabled(true);
        waterRenderer.setGradientStart(0, Color.WHITE);
        waterRenderer.setGradientStop(27, 0xff426c78);
        waterRenderer.setChartValuesTextSize(25);

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        XYSeries series = new XYSeries(titles[0]);
        double[] xV = x.get(0);
        double[] yV = values.get(0);
        int seriesLength = xV.length;
        for (int k = 0; k < seriesLength; k++) {
            series.add(xV[k], yV[k]);

        }
        dataset.addSeries(series);

        dataset.addSeries(0, waterSeries);

        renderer.addSeriesRenderer(0, waterRenderer);
        waterRenderer.setDisplayChartValues(true);

        String[] types = new String[] { BarChart.TYPE, LineChart.TYPE };
//        XYCombinedChartDef[] def=new XYCombinedChartDef[]{new XYCombinedChartDef(BarChart.TYPE, 0),new XYCombinedChartDef(LineChart.TYPE, 1)};
        mchartView = ChartFactory.getCombinedXYChartView(context, dataset, renderer, types);
        mChartLayout.removeAllViews();
        mChartLayout.addView(mchartView);
//        mchartView.setOnClickListener(chartClickListener);

    }

    protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setAxisTitleTextSize(16);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[] { 20, 30, 15, 20 });
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
        return renderer;
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
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                day = day == 1 ? 7 : day - 1;
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - day + 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 6);
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
            dx = 0;
            switch (group.getCheckedRadioButtonId()) {
            case R.id.day:
                calendar = Calendar.getInstance();
                mDContent.setText("本周");
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                day = day == 1 ? 7 : day - 1;

                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - day + 1);
                startDate = DateUtils.long2String(calendar.getTimeInMillis(), DateUtils.FORMAT_DATE_YYMMDD);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 6);

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
        mChartLayout.removeAllViews();
        mDistance.setText("- -");
        mSpeed.setText("- -");
        mTimes.setText("- -");
        mAveSpeed.setText("平均时速: - - km/h");
        mAveSpeed.setText("- -");
        showLoadingDialog(R.string.loadingData);
        Request request = new Request(Config.SEVER_WORKOUT_HISTORY + "?session_id=" + UserUtil.getSession() + "&start_date=" + startDate
                + "&end_date=" + endDate);
        System.out.println(request.url);
        Manager.getInstance().execute(Manager.CMD_GET_STRING, request, chartListener);
    }

    IResponseListener chartListener = new IResponseListener() {

        @Override
        public void onSuccess(Response response) {
            dismissLoadingDialog();
            String dataString = response.getData().toString();
            BaseModel<List<DataHistory>> data = new Gson().fromJson(dataString, new TypeToken<BaseModel<List<DataHistory>>>() {
            }.getType());
            if ("fail".equals(data.status)) {
                ToastUtil.showLongToast(data.message);
            } else {
                if (data.data == null || data.data.size() == 0) {
                    ToastUtil.showLongToast("数据为空");
                    return;
                }
                showChart(data.data);
            }
        }

        @Override
        public void onError(Response response) {
            dismissLoadingDialog();

        }

    };

    long maxSpeed = 0;

    void showChart(List<DataHistory> list) {
        long duration = 0;
        long distance = 0;
        long sumSpeed = 0;
        int sum = 0;
        long maxSpeed = 0;
        waterSeries = new XYSeries("");
        int i = 1;
        for (DataHistory dataHistory : list) {
            sumSpeed += dataHistory.max_speed;
            sum++;
            waterSeries.add(i++, dataHistory.max_speed);
            maxSpeed = Math.max(dataHistory.max_speed, maxSpeed);
            duration = Math.max(dataHistory.duration, duration);
            distance = Math.max(dataHistory.distance, distance);
        }
        mDistance.setText(distance + " km");
        mSpeed.setText(maxSpeed + " km/h");
        mTimes.setText(DateUtil.convertTime(duration * 1000));
        mAveSpeed.setText("平均时速: " + (sumSpeed / sum) + "km/h");
        execute(this, waterSeries, 0);

    }

    class DataHistory {
        public long id;
        public String uuid;
        public String workout_uuid;
        public int workout_type;
        public long distance;
        public long duration;
        public long max_speed;
        public long avg_speed;
        public long start_time;
        public long end_time;
        public long timestamp;
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
        renderer.setXLabels(1);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
    }
}
