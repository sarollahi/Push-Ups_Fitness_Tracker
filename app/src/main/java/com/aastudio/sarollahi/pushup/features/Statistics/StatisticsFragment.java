package com.aastudio.sarollahi.pushup.features.Statistics;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aastudio.sarollahi.pushup.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/**
 * Handles displaying statistical information about the users sessions
 */
public class StatisticsFragment extends Fragment implements StatisticsContract.View{

    private StatisticsFragmentInterface listener;
    private StatisticsPresenter presenter;

    public StatisticsFragment() {}

    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        presenter = new StatisticsPresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.loadData();
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StatisticsFragmentInterface) {
            listener = (StatisticsFragmentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StatisticsFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onViewDestroyed();
    }

    @Override
    public void setSessionHighScore(int highscore) {
        TextView view = (TextView) getView().findViewById(R.id.text_best_session);
        view.setText(String.valueOf(highscore));
    }

    @Override
    public void setDayHighScore(int highscore) {
        TextView view = (TextView) getView().findViewById(R.id.text_best_day);
        view.setText(String.valueOf(highscore));
    }

    @Override
    public void setTotalPushups(int totalPushups) {
        TextView view = (TextView) getView().findViewById(R.id.text_all_time);
        view.setText(String.valueOf(totalPushups));
    }

    @Override
    public void setTimesGoalReached(int times) {
        TextView view = (TextView) getView().findViewById(R.id.text_goal_reached);
        view.setText(String.valueOf(times));
    }

    @Override
    public void setTimesGoalFailed(int times) {
        TextView view = (TextView) getView().findViewById(R.id.text_goal_failed);
        view.setText(String.valueOf(times));
    }

    @Override
    public void setTodayTotalPushups(int todayTotalPushups) {
        TextView view = (TextView) getView().findViewById(R.id.text_completed_today);
        view.setText(String.valueOf(todayTotalPushups));
    }

    @Override
    public void setGraph(LineGraphSeries<DataPoint> series) {
        series.setDrawBackground(true);
        series.setAnimated(true);
        series.setDrawDataPoints(true);
        GraphView graphView = (GraphView) getView().findViewById(R.id.graph);
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Number of Push-Ups");
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Sessions from oldest to newest");
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getContext()));
        graphView.getGridLabelRenderer().setNumHorizontalLabels(3);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMaxX(series.getHighestValueX());
        graphView.getViewport().setMinX(series.getLowestValueX());
        graphView.getGridLabelRenderer().setHumanRounding(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.addSeries(series);
    }

    public interface StatisticsFragmentInterface {

    }
}
