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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    static void gotoPlayStore(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    static double avg(double[] array) {
        double total = 0.0;
        for (double i : array) {
            total += i;
        }
        return total / array.length;
    }

    static int avg(int[] array) {
        int total = 0;
        for (int i : array) {
            total += i;
        }
        return total / array.length;
    }

    public static double getMaxDouble(Number[] numbers) {
        double maxValue = numbers[0].doubleValue();
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i].doubleValue() > maxValue) {
                maxValue = numbers[i].doubleValue();
            }
        }
        return maxValue;
    }

    public static double getMinDouble(Number[] numbers) {
        double minValue = numbers[0].doubleValue();
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i].doubleValue() < minValue) {
                minValue = numbers[i].doubleValue();
            }
        }
        return minValue;
    }

    public static int getMaxInt(Number[] numbers) {
        int maxValue = numbers[0].intValue();
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i].intValue() > maxValue) {
                maxValue = numbers[i].intValue();
            }
        }
        return maxValue;
    }

    public static int getMinInt(Number[] numbers) {
        int minValue = numbers[0].intValue();
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i].intValue() < minValue) {
                minValue = numbers[i].intValue();
            }
        }
        return minValue;
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
                if (!order) return Long.compare(one.getCost(), two.getCost());
                else return Long.compare(two.getCost(), one.getCost());
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
                if (!order) return Long.compare(one.getCost(), two.getCost());
                else return Long.compare(two.getCost(), one.getCost());
            }
        });
        return settlement;
    }

    static int getHelp(Context context) {
        return context.getSharedPreferences("system", Context.MODE_PRIVATE).getInt("swipe", 0);
    }

    static void setHelp(Context context, int help) {
        context.getSharedPreferences("system", Context.MODE_PRIVATE).edit().putInt("swipe", help).apply();
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

    static int calculateBalance(Settlement settlement) {
        int x = 0;
        for (Load load : settlement.getLoads()) {
            x += load.getRate();
        }
        if (settlement.getPayout() != null) {
            if (settlement.getPayout().getPPercent() != 0) {
                x -= x - (x * (100 - settlement.getPayout().getPPercent())) / 100;
            }
            if (settlement.getPayout().getMCpm() != 0) {
                x -= ((settlement.getEmptyMiles() + settlement.getLoadedMiles()) * settlement.getPayout().getMCpm()) / 100;
            }
        }
        for (Fuel fuel : settlement.getFuel()) {
            x -= fuel.getCost();
        }
        for (Cost cost : settlement.getFixed()) {
            x -= cost.getCost();
        }
        for (Cost cost : settlement.getMiscellaneous()) {
            x -= cost.getCost();
        }
        return x;
    }

    static String formatInt(int count) {
        return NumberFormat.getNumberInstance(Locale.US).format(count);
    }

    static String formatInt(int count, int decimals) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMaximumFractionDigits(decimals);
        return format.format(count);
    }

    static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    static double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    static String formatValueToCurrency(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(value);
    }

    static String formatValueToCurrency(int value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(value);
    }

    static String formatValueToCurrency(int value, int decimals) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        formatter.setMaximumFractionDigits(decimals);
        return formatter.format(value);
    }

    static String formatValueToCurrency(double value, int decimals) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        formatter.setMaximumFractionDigits(decimals);
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

    static String toShortDate(long value) {
        DateFormat df2 = new SimpleDateFormat("MM/dd", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    static String toShortDate(Date date) {
        DateFormat df2 = new SimpleDateFormat("MM/dd", Locale.getDefault());
        return df2.format(date);
    }

    static String toShortDateSpelled(long value) {
        DateFormat df2 = new SimpleDateFormat("E, MMM dd", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    static String toShortDateSpelledWithTime(long value) {
        DateFormat df2 = new SimpleDateFormat("E, MMM dd - HH:mm", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    static String withLocation(String location, long value) {
        DateFormat df2 = new SimpleDateFormat("E, MMM dd", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime())) + " " + location;
    }

    static String range(long value1, long value2) {
        DateFormat df1 = new SimpleDateFormat("MMM dd", Locale.getDefault());
        DateFormat df2 = new SimpleDateFormat(" - MMM dd", Locale.getDefault());
        return df1.format(new Date(new Timestamp(value1).getTime())) + df2.format(new Date(new Timestamp(value2).getTime()));
    }

    static String toShortDateSpelled(Date date) {
        DateFormat df2 = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
        return df2.format(date);
    }

    static String toDate(long value) {
        DateFormat df2 = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    static String toDate(Date date) {
        DateFormat df2 = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        return df2.format(date);
    }

    private static String toTime(long value) {
        DateFormat df2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    static String toTime(Date date) {
        DateFormat df2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df2.format(date);
    }

    static String toDateAndTime(long value, boolean seconds) {
        DateFormat df2;
        if (seconds) df2 = new SimpleDateFormat("MM/dd/yy HH:mm:ss:SSS", Locale.getDefault());
        else df2 = new SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault());
        return df2.format(new Date(new Timestamp(value).getTime()));
    }

    static String toDateAndTime(Date date) {
        DateFormat df2 = new SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault());
        return df2.format(date);
    }

    private static String formatDiff(Duration duration) {
        String response = "just now";
        if (duration.toDays() == 1) response = "Yesterday " + toTime(duration.toMillis());
        else if (duration.toDays() > 1) response = duration.toDays() + " days ago";
        else {
            if (duration.toHours() == 1) response = duration.toHours() + " hour ago";
            else if (duration.toHours() > 1) response = duration.toHours() + " hours ago";
            else {
                if (duration.toMinutes() == 1) response = duration.toMinutes() + "  min ago";
                else if (duration.toMinutes() > 1) response = duration.toMinutes() + "  mins ago";
            }
        }
        return response;
    }

    private static Duration timeDifferance(long then) {
        return Duration.between(Instant.ofEpochSecond(then), Instant.now());
    }

    static String showElapsed(long stamp) {
        return formatDiff(timeDifferance(stamp));
    }

    static ArrayList<Settlement> returnSettlementArray(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Settlement>>() {
        }.getType());
    }

}
