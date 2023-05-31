package com.glass.payroll;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.InputFilter;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class Utils {

    static Settlement calculate(@NonNull Settlement x) {
        x.setGross(0);
        x.setFuelCost(0);
        x.setDefCost(0);
        x.setPayoutCost(0);
        x.setMaintenanceCost(0);
        x.setMiscCost(0);
        x.setFixedCost(0);
        x.setDefGallons(0);
        x.setDieselGallons(0);
        x.setEmptyMiles(0);
        x.setLoadedMiles(0);
        for (Load load : x.getLoads()) {
            x.setGross(x.getGross() + load.getRate());
            x.setEmptyMiles(x.getEmptyMiles() + load.getEmpty());
            x.setLoadedMiles(x.getLoadedMiles() + load.getLoaded());
        }
        if (x.getPayout().getPPercent() != 0) {
            x.setPayoutCost(x.getGross() * ((double) x.getPayout().getPPercent() / 100));
        }
        if (x.getPayout().getMPercent() != 0) {
            x.setMaintenanceCost(x.getGross() * ((double) x.getPayout().getMPercent() / 100));
        }
        if (x.getPayout().getPCpm() != 0) {
            x.setPayoutCost(x.getPayoutCost() + ((double) (x.getEmptyMiles() + x.getLoadedMiles()) * x.getPayout().getMCpm()) / 100);
        }
        if (x.getPayout().getMCpm() != 0) {
            x.setMaintenanceCost(x.getMaintenanceCost() + ((double) (x.getEmptyMiles() + x.getLoadedMiles()) * x.getPayout().getMCpm()) / 100);
        }

        for (Fuel fuel : x.getFuel()) {
            if (!fuel.getDef()) {
                x.setFuelCost(x.getFuelCost() + fuel.getCost());
                x.setDieselGallons(x.getDieselGallons() + fuel.getGallons());
            } else {
                x.setDefCost(x.getDefCost() + fuel.getCost());
                x.setDefGallons(x.getDefGallons() + fuel.getGallons());
            }
        }
        for (Cost cost : x.getFixed()) {
            x.setFixedCost(x.getFixedCost() + cost.getCost());
        }
        for (Cost cost : x.getMiscellaneous()) {
            x.setMiscCost(x.getMiscCost() + cost.getCost());
        }
        x.setBalance(x.getGross() - (x.getPayoutCost() + x.getMaintenanceCost() + x.getFixedCost() + x.getMiscCost() + x.getFuelCost() + x.getDefCost()));
        x.setBalance(formatDouble(x.getBalance()));
        x.setFuelCost(formatDouble(x.getFuelCost()));
        x.setDefCost(formatDouble(x.getDefCost()));
        x.setMaintenanceCost(formatDouble(x.getMaintenanceCost()));
        x.setPayoutCost(formatDouble(x.getPayoutCost()));
        return x;
    }

    static void gotoPlayStore(@NonNull Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    static double avg(@NonNull double[] array) {
        double total = 0.0;
        for (double i : array) {
            total += i;
        }
        return total / array.length;
    }

    static int avg(@NonNull int[] array) {
        int total = 0;
        for (int i : array) {
            total += i;
        }
        return total / array.length;
    }

    static void showKeyboard(@NonNull Context context, @NonNull final EditText ettext) {
        InputMethodManager methodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        ettext.requestFocus();
        ettext.postDelayed(() -> methodManager.showSoftInput(ettext, 0), 200);
    }

    static int getVersion(@NonNull Context context) {
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

    static Settlement sortFuel(@NonNull Settlement settlement, boolean order, boolean sort) {
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

    static Settlement sortLoads(@NonNull Settlement settlement, boolean order, boolean sort) {
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

    static Settlement sortFixed(@NonNull Settlement settlement, boolean order, boolean sort) {
        settlement.getFixed().sort((one, two) -> {
            if (!sort) {
                if (!order) return Long.compare(one.getStamp(), two.getStamp());
                else return Long.compare(two.getStamp(), one.getStamp());
            } else {
                if (!order) return Long.compare(one.getCost(), two.getCost());
                else return Long.compare(two.getCost(), one.getCost());
            }
        });
        return settlement;
    }

    static Settlement sortMiscellaneous(@NonNull Settlement settlement, boolean order, boolean sort) {
        settlement.getMiscellaneous().sort((one, two) -> {
            if (!sort) {
                if (!order) return Long.compare(one.getStamp(), two.getStamp());
                else return Long.compare(two.getStamp(), one.getStamp());
            } else {
                if (!order) return Long.compare(one.getCost(), two.getCost());
                else return Long.compare(two.getCost(), one.getCost());
            }
        });
        return settlement;
    }

    static boolean getOrder(@NonNull Context context, String key) {
        return context.getSharedPreferences("order", Context.MODE_PRIVATE).getBoolean(key, false);
    }

    static void setOrder(@NonNull Context context, String key, boolean checked) {
        context.getSharedPreferences("order", Context.MODE_PRIVATE).edit().putBoolean(key, checked).apply();
    }

    static boolean getSort(@NonNull Context context, String key) {
        return context.getSharedPreferences("sort", Context.MODE_PRIVATE).getBoolean(key, false);
    }

    static void setSort(@NonNull Context context, String key, boolean checked) {
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

    static double formatDouble(double value) {
        return Double.parseDouble(new DecimalFormat("#.00").format(value));

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

}
