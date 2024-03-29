package ru.spbau.ourpedometer.persistens;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LightCollectorCalculator implements StatisticsCollector, StatisticsCalculator {
    private float stepHeightThreshold = 11;
    private int stepWidthThreshold = 20;

    long beginStepTime;
    boolean stepFlag = false;
    boolean beginStepTimeInitialized = false;

    StepsSaver stepsSaver;

    public LightCollectorCalculator(StepsSaver stepsSaver) {
        this.stepsSaver = stepsSaver;
    }

    @Override
    public void setStepHeightThreshold(float threshold) {
        stepHeightThreshold = threshold;
    }

    @Override
    public void setStepWidthThreshold(int threshold) {
        stepWidthThreshold = threshold;
    }

    @Override
    public synchronized int steps(Date startTime, Date stopTime) {
        return stepsSaver.getStepsCount(startTime, stopTime);
    }

    @Override
    public Iterable<StatisticsBean> getStatsByDateRange(Date startTime, Date stopTime) {
        return null;
    }

    @Override
    public synchronized void save(StatisticsBean st) {
        float length = (float) Math.sqrt(Math.pow(st.x(), 2) + Math.pow(st.y(), 2) + Math.pow(st.z(), 2));

        if (!beginStepTimeInitialized) {
            beginStepTime = st.time();
            beginStepTimeInitialized = true;
        }


        if (length > stepHeightThreshold) {
            if (!stepFlag) {
                beginStepTime = st.time();
                stepFlag = true;
            }
        } else {
            if (stepFlag) {
                if (st.time() - beginStepTime > stepWidthThreshold) {
                    //stepsCount++;
                    stepsSaver.addStep(new Date(st.time()));
                }
                stepFlag = false;
            }
        }
    }

    private int currentTimeInMillis(TimeUnit timeUnit){
        switch (timeUnit){
            case MINUTES:
                return  60*1000;

            case SECONDS:
                return  1000;

            default:
                return  1000;
        }
    }

    @Override
    public float speed(Date startTime, Date stopTime, TimeUnit timeUnit) {
        int steps = steps(startTime, stopTime);
        return (float)steps/ (float)currentTimeInMillis(timeUnit);
    }

    public float minSpeed(Date startTime, Date stopTime, TimeUnit timeUnit) {
        int delta = currentTimeInMillis(timeUnit);
        Long time = startTime.getTime() + delta;
        float min = speed(startTime, new Date(time), timeUnit);


        while(time + delta <= stopTime.getTime()){
            float speed = speed(new Date(time), new Date(time + delta), timeUnit);
            if(speed < min){
                min = speed;
            }

            time += delta;
        }

        return min;
    }

    public float maxSpeed(Date startTime, Date stopTime, TimeUnit timeUnit) {
        int delta = currentTimeInMillis(timeUnit);
        Long time = startTime.getTime() + delta;
        float max = speed(startTime, new Date(time), timeUnit);


        while(time + delta <= stopTime.getTime()){
            float speed = speed(new Date(time), new Date(time + delta), timeUnit);
            if(speed > max){
                max = speed;
            }

            time += delta;
        }

        return max;
    }

    @Override
    public void close() {
        stepsSaver.close();
    }
}
