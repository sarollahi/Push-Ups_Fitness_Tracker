package com.aastudio.sarollahi.pushup.features.Statistics;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by Seyed Ahmad Sarollahi 01/06/2020.
 */

interface StatisticsContract {

    interface View {
        void setSessionHighScore(int highscore);
        void setDayHighScore(int highscore);
        void setTotalPushups(int totalPushups);
        void setTimesGoalReached(int times);
        void setTimesGoalFailed(int times);
        void setTodayTotalPushups(int todayTotalPushups);
        void setGraph(LineGraphSeries<DataPoint> series);
    }
}
