/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.project001;

import java.awt.Component;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author SIAM
 */
public class CountDown extends Timer {
    TypingPage Instance;
    int Interval;
    Timer timer;
    int delay, period, elapsed, remaining;

    public CountDown(Component parent, int from) {
        Instance = (TypingPage)parent;
        Interval = from;
        delay = 1000;
        period = 1000;
        elapsed = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                remaining = setInterval();
                Instance.UpdateTimeInUI(elapsed, remaining);
            }
        }, delay, period);
    }

    private final int setInterval() {
        elapsed++;
        if (Interval == 1) {
            timer.cancel();
            Instance.CountDownFinished();
        }
        return --Interval;
    }

    public void forceStop() {
        timer.cancel();
        Instance.CountDownFinished();
    }
}
