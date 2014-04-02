/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.run.demo.pedometer.listener;

import android.content.Context;
import android.os.Vibrator;

/**
 * 振动时一步检测。通常,这不是连接,用于调试目的
 * Vibrates whenever a step is detected.
 * Normally, this is not attached, used for debugging purposes.
 * @author Levente Bagi
 */
public class StepBuzzer implements IStepListener {
    
    private Context mContext;
    private Vibrator mVibrator;
    
    public StepBuzzer(Context context) {
        mContext = context;
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    public void onStep() {
        buzz();
    }
    
    public void passValue() {
        
    }
    
    private void buzz() {
        mVibrator.vibrate(50);
    }
}

