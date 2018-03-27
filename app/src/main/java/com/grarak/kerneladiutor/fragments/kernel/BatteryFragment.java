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
package com.grarak.kerneladiutor.fragments.kernel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.fragments.DescriptionFragment;
import com.grarak.kerneladiutor.fragments.recyclerview.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.battery.Battery;
import com.grarak.kerneladiutor.views.recyclerview.CardView;
import com.grarak.kerneladiutor.views.recyclerview.RecyclerViewItem;
import com.grarak.kerneladiutor.views.recyclerview.SeekBarView;
import com.grarak.kerneladiutor.views.recyclerview.StatsView;
import com.grarak.kerneladiutor.views.recyclerview.SwitchView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 26.06.16.
 */
public class BatteryFragment extends RecyclerViewFragment {

    private Battery mBattery;

    private StatsView mLevel;
    private StatsView mVoltage,chargesNum;

    private int mBatteryLevel;
    private int mBatteryVoltage;

    @Override
    protected void init() {
        super.init();

        mBattery = Battery.getInstance(getActivity());
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        levelInit(items);
        voltageInit(items);
        if (mBattery.hasForceFastCharge()) {
            forceFastChargeInit(items);
        }
        if (mBattery.hasBlx()) {
            blxInit(items);
        }
        chargeRateInit(items);
    }

    @Override
    protected void postInit() {
        super.postInit();

        if (itemsSize() > 2) {
            addViewPagerFragment(ApplyOnBootFragment.newInstance(this));
        }
        addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.capacity),
                mBattery.getCapacity() + getString(R.string.mah)));
    }

    private void levelInit(List<RecyclerViewItem> items) {
        mLevel = new StatsView();
        mLevel.setTitle(getString(R.string.level));

        items.add(mLevel);
    }

    private void voltageInit(List<RecyclerViewItem> items) {
        mVoltage = new StatsView();
        mVoltage.setTitle(getString(R.string.voltage));

        items.add(mVoltage);
    }

    private void forceFastChargeInit(List<RecyclerViewItem> items) {
        SwitchView forceFastCharge = new SwitchView();
        forceFastCharge.setTitle(getString(R.string.usb_fast_charge));
        forceFastCharge.setSummary(getString(R.string.usb_fast_charge_summary));
        forceFastCharge.setChecked(mBattery.isForceFastChargeEnabled());
        forceFastCharge.addOnSwitchListener((switchView, isChecked)
                -> mBattery.enableForceFastCharge(isChecked, getActivity()));

        items.add(forceFastCharge);
    }

    private void blxInit(List<RecyclerViewItem> items) {
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.disabled));
        for (int i = 0; i <= 100; i++) {
            list.add(String.valueOf(i));
        }

        SeekBarView blx = new SeekBarView();
        blx.setTitle(getString(R.string.blx));
        blx.setSummary(getString(R.string.blx_summary));
        blx.setItems(list);
        blx.setProgress(mBattery.getBlx());
        blx.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
            @Override
            public void onStop(SeekBarView seekBarView, int position, String value) {
                mBattery.setBlx(position, getActivity());
            }

            @Override
            public void onMove(SeekBarView seekBarView, int position, String value) {
            }
        });

        items.add(blx);
    }

    private void chargeRateInit(List<RecyclerViewItem> items) {
        CardView chargeRateCard = new CardView();
        chargeRateCard.setTitle(getString(R.string.charge_rate));

//        if (mBattery.hasChargeRateEnable()) {
//            SwitchView chargeRate = new SwitchView();
//            chargeRate.setSummary(getString(R.string.charge_rate));
//            chargeRate.setChecked(mBattery.isChargeRateEnabled());
//            chargeRate.addOnSwitchListener((switchView, isChecked)
//                    -> mBattery.enableChargeRate(isChecked, getActivity()));
//
//            chargeRateCard.addItem(chargeRate);
//        }

        if (mBattery.hasChargingCurrent()) {
            SeekBarView chargingCurrent = new SeekBarView();
            chargingCurrent.setTitle(getString(R.string.charging_current));
            chargingCurrent.setSummary(getString(R.string.charging_current_summary));
            chargingCurrent.setUnit(getString(R.string.ma));
            chargingCurrent.setMax(3000);
            chargingCurrent.setMin(900);
            chargingCurrent.setOffset(10);
            chargingCurrent.setProgress(mBattery.getChargingCurrent() / 10 - 90);
            chargingCurrent.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                @Override
                public void onStop(SeekBarView seekBarView, int position, String value) {
                    mBattery.setChargingCurrent((position + 90) * 10, getActivity());
                }

                @Override
                public void onMove(SeekBarView seekBarView, int position, String value) {
                }
            });
            

            chargeRateCard.addItem(chargingCurrent);
        }
        if (mBattery.hasChargeLimit()) {
            SeekBarView rechargeAt = new SeekBarView();
            SeekBarView chargeLimit = new SeekBarView();
            chargeLimit.setTitle(("Charge Limit"));
            chargeLimit.setSummary(getString(R.string.charge_limit_summary));
            chargeLimit.setUnit("%");
            chargeLimit.setMax(100);
            chargeLimit.setMin(0);
            chargeLimit.setOffset(1);
            chargeLimit.setProgress(mBattery.getChargeLimit());
            chargeLimit.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                @Override
                public void onStop(SeekBarView seekBarView, int position, String value) {
                    mBattery.setChargeLimit(position , getActivity());
                    if(position==0)
                    {
                        mBattery.setRechargeAt(position , getActivity());
                        rechargeAt.setProgress(position);
                    }
                   else if(mBattery.getRechargeAt()>=position)
                    {
                        mBattery.setRechargeAt(mBattery.getChargeLimit()-1 , getActivity());
                        rechargeAt.setProgress(mBattery.getChargeLimit()-1);
                    }
                }

                @Override
                public void onMove(SeekBarView seekBarView, int position, String value) {
                }
            });


            chargeRateCard.addItem(chargeLimit);

        


            rechargeAt.setTitle("Lower Charge Limit");
            rechargeAt.setSummary(getString(R.string.lower_charge_limit_summary));
            rechargeAt.setUnit("%");
            rechargeAt.setMax(100);
            rechargeAt.setMin(0);
            rechargeAt.setOffset(1);
            rechargeAt.setProgress(mBattery.getRechargeAt());
            rechargeAt.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                @Override
                public void onStop(SeekBarView seekBarView, int position, String value) {
                    if(mBattery.getChargeLimit()<position)
                    {
                        mBattery.setRechargeAt(mBattery.getChargeLimit()-1 , getActivity());
                        rechargeAt.setProgress(mBattery.getChargeLimit()-1);
                    }
                    else
                        mBattery.setRechargeAt(position,getActivity());
                }

                @Override
                public void onMove(SeekBarView seekBarView, int position, String value) {
                }
            });


            chargeRateCard.addItem(rechargeAt);
        }
        if (mBattery.hasFullChargeEvery()) {
            SeekBarView fullChargeEvery = new SeekBarView();
            fullChargeEvery.setTitle("Full Charge After");
            fullChargeEvery.setSummary(getString(R.string.recharge_at_summary)+mBattery.getChargeCounter());

            fullChargeEvery.setMax(100);
            fullChargeEvery.setMin(1);
            fullChargeEvery.setOffset(1);
            fullChargeEvery.setProgress(mBattery.getFullChargeEvery()-1);
            fullChargeEvery.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                @Override
                public void onStop(SeekBarView seekBarView, int position, String value) {

                        mBattery.setFullChargeEvery(position+1,getActivity());
                }

                @Override
                public void onMove(SeekBarView seekBarView, int position, String value) {
                }
            });


            chargeRateCard.addItem(fullChargeEvery);
        }



        if (chargeRateCard.size() > 0) {
            items.add(chargeRateCard);
        }
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            mBatteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        }
    };

    @Override
    protected void refresh() {
        super.refresh();
        if (mLevel != null) {
            mLevel.setStat(mBatteryLevel + "%");
        }
        if (mVoltage != null) {
            mVoltage.setStat(mBatteryVoltage + getString(R.string.mv));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(mBatteryReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

}
