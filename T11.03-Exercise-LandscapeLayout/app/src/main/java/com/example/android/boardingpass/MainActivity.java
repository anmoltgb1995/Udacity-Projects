package com.example.android.boardingpass;

/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.android.boardingpass.databinding.ActivityMainBinding;
import com.example.android.boardingpass.utilities.FakeDataUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*
         * DataBindUtil.setContentView replaces our normal call of setContent view.
         * DataBindingUtil also created our ActivityMainBinding that we will eventually use to
         * display all of our data.
         */
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        BoardingPassInfo fakeBoardingInfo = FakeDataUtils.generateFakeBoardingPassInfo();
        displayBoardingPassInfo(fakeBoardingInfo);
    }

    private void displayBoardingPassInfo(BoardingPassInfo info) {

        mBinding.textViewPassengerName.setText(info.passengerName);
        // TODO (7) Use the flightInfor attribute in mBinding below to get the appropiate text Views
        TextView textViewOriginAirport = (TextView) mBinding.flightInfo.findViewById(R.id.textViewOriginAirport);
        TextView textViewFlightCode = (TextView) mBinding.flightInfo.findViewById(R.id.textViewFlightCode);
        TextView textViewDestinationAirport = (TextView) mBinding.flightInfo.findViewById(R.id.textViewDestinationAirport);
        textViewOriginAirport.setText(info.originCode);
        textViewFlightCode.setText(info.flightCode);
        textViewDestinationAirport.setText(info.destCode);

        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault());
        String boardingTime = formatter.format(info.boardingTime);
        String departureTime = formatter.format(info.departureTime);
        String arrivalTime = formatter.format(info.arrivalTime);

        mBinding.textViewBoardingTime.setText(boardingTime);
        mBinding.textViewDepartureTime.setText(departureTime);
        mBinding.textViewArrivalTime.setText(arrivalTime);

        long totalMinutesUntilBoarding = info.getMinutesUntilBoarding();
        long hoursUntilBoarding = TimeUnit.MINUTES.toHours(totalMinutesUntilBoarding);
        long minutesLessHoursUntilBoarding =
                totalMinutesUntilBoarding - TimeUnit.HOURS.toMinutes(hoursUntilBoarding);

        String hoursAndMinutesUntilBoarding = getString(R.string.countDownFormat,
                hoursUntilBoarding,
                minutesLessHoursUntilBoarding);

        mBinding.textViewBoardingInCountdown.setText(hoursAndMinutesUntilBoarding);
        // TODO (8) Use the boardingInfo attribute in mBinding below to get the appropriate text Views
        TextView textViewTerminal = (TextView) mBinding.boardingInfoTable.findViewById(R.id.textViewTerminal);
        TextView textViewGate = (TextView) mBinding.boardingInfoTable.findViewById(R.id.textViewGate);
        TextView textViewSeat = (TextView) mBinding.boardingInfoTable.findViewById(R.id.textViewSeat);
        textViewTerminal.setText(info.departureTerminal);
        textViewGate.setText(info.departureGate);
        textViewSeat.setText(info.seatNumber);
    }
}

