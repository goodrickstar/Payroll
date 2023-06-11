package com.glass.payroll;

import android.view.MenuItem;
import android.view.View;

interface MI {

    void handleMenuNavigation(MenuItem menuItem, boolean close, boolean physical);

    boolean locationPermission();

    void handleGrouping();

    String returnLocation();

    void showSnack(String message, int length);

    void vibrate(View view);

    void newSettlement(Settlement settlement, boolean transfer);

    void newFuel(Fuel fuel, int index);

    void newLoad(Load load, int index);

    void newMisc(Cost cost, int index);

    void newFixed(Cost cost, int index);

    void navigate(int i);

    void group();

    void hideKeyboard(View v);
}
