/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.utils.kernel.battery;

import android.content.Context;
import android.support.annotation.NonNull;

import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by willi on 26.06.16.
 */
public class Battery {

    private static Battery sInstance;

    public static Battery getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new Battery(context);
        }
        return sInstance;
    }
    private static  final String SODA="/sys/kernel/fast_charge";
    private static final String FORCE_FAST_CHARGE = "/sys/kernel/fast_charge/force_fast_charge";
    private static final String BLX = "/sys/devices/virtual/misc/batterylifeextender/charging_limit";

    private static final String CHARGE_RATE = "/sys/kernel/thundercharge_control";
    private static final String CHARGE_RATE_ENABLE = CHARGE_RATE + "/enabled";
    private static final String CUSTOM_CURRENT = CHARGE_RATE + "/custom_current";
    private static final String CHARGE_RATE_SODA = SODA + "/maximum_qc_current";
    private static final String FULL_CHARGE_EVERY_SODA= SODA + "/full_charge_every";
    private static final String CHARGES_COUNTER_SODA = SODA + "/charges_counter";
    private static final String RECHARGE_AT_SODA = SODA + "/recharge_at";
    private static final String CHARGE_LIMIT_SODA = SODA + "/charge_limit";
    private int mCapacity;

    private Battery(Context context) {
        if (mCapacity == 0) {
            try {
                Class<?> powerProfile = Class.forName("com.android.internal.os.PowerProfile");
                Constructor constructor = powerProfile.getDeclaredConstructor(Context.class);
                Object powerProInstance = constructor.newInstance(context);
                Method batteryCap = powerProfile.getMethod("getBatteryCapacity");
                mCapacity = Math.round((long) (double) batteryCap.invoke(powerProInstance));
            } catch (Exception e) {
                e.printStackTrace();
                mCapacity = 0;
            }
        }
    }
    public void setChargeLimit(int value,Context context)
    {
        run(Control.write(String.valueOf(value), CHARGE_LIMIT_SODA), CHARGE_LIMIT_SODA, context);
    }
    public void setFullChargeEvery(int value, Context context)
    {
        run(Control.write(String.valueOf(value), FULL_CHARGE_EVERY_SODA), FULL_CHARGE_EVERY_SODA, context);
    }
    public void setRechargeAt(int value, Context context)
    {
        run(Control.write(String.valueOf(value), RECHARGE_AT_SODA), RECHARGE_AT_SODA, context);
    }
    public void setChargingCurrent(int value, Context context) {
        run(Control.write(String.valueOf(value), CHARGE_RATE_SODA), CHARGE_RATE_SODA, context);
    }


    public int getChargingCurrent() {
        return Utils.strToInt(Utils.readFile(CHARGE_RATE_SODA));
    }
    public int getChargeLimit() {
        return Utils.strToInt(Utils.readFile(CHARGE_LIMIT_SODA));
    }
    public int getFullChargeEvery() {
        return Utils.strToInt(Utils.readFile(FULL_CHARGE_EVERY_SODA));
    }
    public int getRechargeAt() {
        return Utils.strToInt(Utils.readFile(RECHARGE_AT_SODA));
    }
    public int getChargeCounter() {
        return Utils.strToInt(Utils.readFile(CHARGES_COUNTER_SODA));
    }
    public boolean hasChargingCurrent() {
        return Utils.existFile(CHARGE_RATE_SODA);
    }
    public boolean hasChargeLimit() {
        return Utils.existFile(CHARGE_LIMIT_SODA);
    }
    public boolean hasFullChargeEvery() {
        return Utils.existFile(FULL_CHARGE_EVERY_SODA);
    }
    public boolean hasRechargeAt() {
        return Utils.existFile(RECHARGE_AT_SODA);
    }
    public boolean hasChargeCounter() {
        return Utils.existFile(CHARGES_COUNTER_SODA);
    }



    public void enableChargeRate(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", CHARGE_RATE_ENABLE), CHARGE_RATE_ENABLE, context);
    }

    public boolean isChargeRateEnabled() {
        return true;
    }

    public boolean hasChargeRateEnable() {
        return true;
    }

    public void setBlx(int value, Context context) {
        run(Control.write(String.valueOf(value == 0 ? 101 : value - 1), BLX), BLX, context);
    }

    public int getBlx() {
        int value = Utils.strToInt(Utils.readFile(BLX));
        return value > 100 ? 0 : value + 1;
    }

    public boolean hasBlx() {
        return Utils.existFile(BLX);
    }

    public void enableForceFastCharge(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", FORCE_FAST_CHARGE), FORCE_FAST_CHARGE, context);
    }

    public boolean isForceFastChargeEnabled() {
        return Utils.readFile(FORCE_FAST_CHARGE).equals("1");
    }

    public boolean hasForceFastCharge() {
        return Utils.existFile(FORCE_FAST_CHARGE);
    }

    public int getCapacity() {
        return mCapacity;
    }

    public boolean hasCapacity() {
        return getCapacity() != 0;
    }

    public boolean supported() {
        return hasCapacity();
    }

    private void run(String command, String id, Context context) {
        Control.runSetting(command, ApplyOnBootFragment.BATTERY, id, context);
    }

}
