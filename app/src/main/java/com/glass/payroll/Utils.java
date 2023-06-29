package com.glass.payroll;

import static java.lang.Math.round;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class Utils {

    static Settlement calculate(Settlement settlement) {
        settlement.setGross(0);
        settlement.setFuelCost(0);
        settlement.setDefCost(0);
        settlement.setPayoutCost(0);
        settlement.setMaintenanceCost(0);
        settlement.setMiscCost(0);
        settlement.setFixedCost(0);
        settlement.setDefGallons(0);
        settlement.setDieselGallons(0);
        settlement.setEmptyMiles(0);
        settlement.setLoadedMiles(0);
        for (Load load : settlement.getLoads()) {
            settlement.setGross(settlement.getGross() + load.getRate());
            settlement.setEmptyMiles(settlement.getEmptyMiles() + load.getEmpty());
            settlement.setLoadedMiles(settlement.getLoadedMiles() + load.getLoaded());
        }
        if (settlement.getPayout().getPPercent() != 0) {
            settlement.setPayoutCost(settlement.getGross() * ((double) settlement.getPayout().getPPercent() / 100));
        }
        if (settlement.getPayout().getMPercent() != 0) {
            settlement.setMaintenanceCost(settlement.getGross() * ((double) settlement.getPayout().getMPercent() / 100));
        }
        if (settlement.getPayout().getPCpm() != 0) {
            settlement.setPayoutCost(settlement.getPayoutCost() + ((double) (settlement.getEmptyMiles() + settlement.getLoadedMiles()) * settlement.getPayout().getMCpm()) / 100);
        }
        if (settlement.getPayout().getMCpm() != 0) {
            settlement.setMaintenanceCost(settlement.getMaintenanceCost() + ((double) (settlement.getEmptyMiles() + settlement.getLoadedMiles()) * settlement.getPayout().getMCpm()) / 100);
        }

        for (Fuel fuel : settlement.getFuel()) {
            if (!fuel.getDef()) {
                settlement.setFuelCost(settlement.getFuelCost() + fuel.getCost());
                settlement.setDieselGallons(settlement.getDieselGallons() + fuel.getGallons());
            } else {
                settlement.setDefCost(settlement.getDefCost() + fuel.getCost());
                settlement.setDefGallons(settlement.getDefGallons() + fuel.getGallons());
            }
        }
        for (Cost cost : settlement.getFixed()) {
            settlement.setFixedCost(settlement.getFixedCost() + cost.getCost());
        }
        for (Cost cost : settlement.getMiscellaneous()) {
            settlement.setMiscCost(settlement.getMiscCost() + cost.getCost());
        }
        settlement.setBalance(settlement.getGross() - (settlement.getPayoutCost() + settlement.getMaintenanceCost() + settlement.getFixedCost() + settlement.getMiscCost() + settlement.getFuelCost() + settlement.getDefCost()));
        settlement.setBalance(formatDouble(settlement.getBalance(), 2));
        settlement.setFuelCost(formatDouble(settlement.getFuelCost(), 2));
        settlement.setDefCost(formatDouble(settlement.getDefCost(), 2));
        settlement.setMaintenanceCost(formatDouble(settlement.getMaintenanceCost(), 2));
        settlement.setPayoutCost(formatDouble(settlement.getPayoutCost(), 2));
        return settlement;
    }

    static Settlement setQuarters(Settlement settlement) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(settlement.getStop());
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int quarter = getQuarter(month);
        int year = calendar.get(Calendar.YEAR);
        settlement.setWeek(week);
        settlement.setMonth(month);
        settlement.setQuarter(quarter);
        settlement.setYear(year);
        return settlement;
    }

    static int getQuarter(int month) {
        switch (month) {
            case 1:
            case 2:
            case 3:
                return 1;
            case 4:
            case 5:
            case 6:
                return 2;
            case 7:
            case 8:
            case 9:
                return 3;
            default:
                return 4;
        }
    }

    static int miles(Settlement settlement) {
        return settlement.getEmptyMiles() + settlement.getLoadedMiles();
    }

    static int miles(Load load) {
        return load.getEmpty() + load.getLoaded();
    }


    static int parseInt(Editable editable) {
        try {
            return Integer.parseInt(editable.toString().trim());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    static double parseDouble(Editable editable) {
        try {
            return Double.parseDouble(editable.toString().trim());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    static void gotoPlayStore(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    static double avgDouble(ArrayList<Double> array) {
        if (array.isEmpty()) return 0.0;
        return sumDouble(array) / array.size();
    }

    static double sumDouble(ArrayList<Double> array){
        double total = 0.0;
        for (Double i : array) {
            total += i;
        }
        return total;
    }

    static double avgInt(ArrayList<Integer> array) {
        if (array.isEmpty()) return 0.0;
        return sumInt(array) / array.size();
    }

    static double sumInt(ArrayList<Integer> array){
        double total = 0.0;
        for (Integer i : array) {
            total += i;
        }
        return total;
    }

    static void showKeyboard(Context context, final EditText ettext) {
        InputMethodManager methodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        ettext.requestFocus();
        ettext.postDelayed(() -> methodManager.showSoftInput(ettext, 0), 200);
    }

    static int getVersion(Context context) {
        int version = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.glass.payroll", PackageManager.GET_META_DATA);
            version = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("getVersion()", e.getMessage());
        }
        return version;
    }

    @NonNull
    static String returnMonthName(int month) {
        switch (month) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            default:
                return "December";
        }
    }

    static InputFilter[] inputFilter() {
        return new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }};
    }

    static Settlement sortFuel(Settlement settlement, boolean order, boolean sort) {
        settlement.getFuel().sort((one, two) -> {
            if (!sort) {
                if (!order) return Long.compare(one.getStamp(), two.getStamp());
                else return Long.compare(two.getStamp(), one.getStamp());
            } else {
                if (!order) return Double.compare(one.getCost(), two.getCost());
                else return Double.compare(two.getCost(), one.getCost());
            }
        });
        return settlement;
    }

    static Settlement sortLoads(Settlement settlement, boolean order, boolean sort) {
        settlement.getLoads().sort((one, two) -> {
            if (!sort) {
                if (!order) return Long.compare(one.getStart(), two.getStart());
                else return Long.compare(two.getStart(), one.getStart());
            } else {
                if (!order) return Long.compare(one.getRate(), two.getRate());
                else return Long.compare(two.getRate(), one.getRate());
            }
        });
        return settlement;
    }

    static Settlement sortFixed(Settlement settlement, boolean order, boolean sort) {
        settlement.getFixed().sort((one, two) -> {
            if (!sort) {
                if (!order) return Long.compare(one.getStamp(), two.getStamp());
                else return Long.compare(two.getStamp(), one.getStamp());
            } else {
                if (!order) return Double.compare(one.getCost(), two.getCost());
                else return Double.compare(two.getCost(), one.getCost());
            }
        });
        return settlement;
    }

    static Settlement sortMiscellaneous(Settlement settlement, boolean order, boolean sort) {
        settlement.getMiscellaneous().sort((one, two) -> {
            if (!sort) {
                if (!order) return Long.compare(one.getStamp(), two.getStamp());
                else return Long.compare(two.getStamp(), one.getStamp());
            } else {
                if (!order) return Double.compare(one.getCost(), two.getCost());
                else return Double.compare(two.getCost(), one.getCost());
            }
        });
        return settlement;
    }

    static boolean getOrder(Context context, String key) {
        return context.getSharedPreferences("order", Context.MODE_PRIVATE).getBoolean(key, false);
    }

    static void setOrder(Context context, String key, boolean checked) {
        context.getSharedPreferences("order", Context.MODE_PRIVATE).edit().putBoolean(key, checked).apply();
    }

    static boolean getSort(Context context, String key) {
        return context.getSharedPreferences("sort", Context.MODE_PRIVATE).getBoolean(key, false);
    }

    static void setSort(Context context, String key, boolean checked) {
        context.getSharedPreferences("sort", Context.MODE_PRIVATE).edit().putBoolean(key, checked).apply();
    }

    @NonNull
    static String formatInt(int count) {
        return NumberFormat.getNumberInstance(Locale.US).format(count);
    }

    @NonNull
    static String formatValueToCurrency(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(value);
    }

    static String formatValueToCurrency(double value, boolean dollarSign) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        if (!dollarSign) return formatter.format(value).replace("$", "");
        else return formatter.format(value);
    }

    static String formatValueToCurrencyWhole(double value) {
        return "$" + formatInt((int) round(value));
    }

    static String formatDoubleWhole(double value) {
        return formatInt((int) round(value));
    }

    static double formatDouble(double value, int digits) {
        switch (digits){
            case 0:
                return Double.parseDouble(new DecimalFormat("#").format(value));
            case 1:
                return Double.parseDouble(new DecimalFormat("#.#").format(value));
            case 2:
                return Double.parseDouble(new DecimalFormat("#.##").format(value));
            case 3:
                return Double.parseDouble(new DecimalFormat("#.###").format(value));
        }
        return 0.0;
    }

    static int calculateDifference(long then, long now) {
        long diff = then - now;
        if (diff < 0) diff = -diff;
        switch (0) {
            case 1:
                return (int) (diff / 1000);
            case 2:
                return (int) (diff / (60 * 1000));
            case 3:
                return (int) (diff / (60 * 60 * 1000));
            default:
                return (int) (diff / (24 * 60 * 60 * 1000));
        }
    }

    @NonNull
    static String toShortDateSpelled(long value) {
        DateFormat df2 = new SimpleDateFormat("E, MMM dd", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    @NonNull
    static String toShortDateSpelledWithTime(long value) {
        DateFormat df2 = new SimpleDateFormat("E, MMM dd - HH:mm", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    @NonNull
    static String range(long value1, long value2) {
        DateFormat df1 = new SimpleDateFormat("MMM dd", Locale.getDefault());
        DateFormat df2 = new SimpleDateFormat(" - MMM dd", Locale.getDefault());
        return df1.format(new Date(new Timestamp(value1).getTime())) + df2.format(new Date(new Timestamp(value2).getTime()));
    }

    @NonNull
    private static String toTime(long value) {
        DateFormat df2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    static ArrayList<Settlement> returnSettlementArray(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Settlement>>() {
        }.getType());
    }

    static ArrayList<Truck> returnTruckArray(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Truck>>() {
        }.getType());
    }

    static ArrayList<Trailer> returnTrailerArray(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Trailer>>() {
        }.getType());
    }

    static void vibrate(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
    }

    static String addLine(TextView textView){
        return textView.getText() + "\n";
    }

    public static double loadedRate(Load load) {
        if (load.getLoaded() == 0) return 0;
        else return load.getRate()/ load.getLoaded();
    }
}
