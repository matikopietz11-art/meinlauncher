package com.example.myhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    static class Entry {
        String label;
        Drawable icon;
        Intent intent;
    }

    private final List<Entry> entries = new ArrayList<>();
    private BaseAdapter adapter;

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#101114"));
        root.setPadding(dp(48), dp(32), dp(48), 0);

        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm");
        clock.setFormat12Hour("HH:mm");
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(36);
        root.addView(clock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        GridView grid = new GridView(this);
        grid.setNumColumns(6);
        grid.setVerticalSpacing(dp(16));
        grid.setHorizontalSpacing(dp(16));
        grid.setClipToPadding(false);
        grid.setPadding(0, dp(24), 0, dp(24));
        GradientDrawable sel = new GradientDrawable();
        sel.setColor(0x33FFFFFF);
        sel.setCornerRadius(dp(16));
        grid.setSelector(sel);

        adapter = new BaseAdapter() {
            @Override public int getCount() { return entries.size(); }
            @Override public Object getItem(int i) { return entries.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                Entry e = entries.get(pos);
                LinearLayout item = new LinearLayout(MainActivity.this);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setGravity(Gravity.CENTER_HORIZONTAL);
                item.setPadding(dp(8), dp(16), dp(8), dp(16));

                ImageView icon = new ImageView(MainActivity.this);
                icon.setImageDrawable(e.icon);
                item.addView(icon, new LinearLayout.LayoutParams(dp(88), dp(88)));

                TextView label = new TextView(MainActivity.this);
                label.setText(e.label);
                label.setTextColor(Color.WHITE);
                label.setTextSize(14);
                label.setGravity(Gravity.CENTER);
                label.setSingleLine(true);
                label.setEllipsize(TextUtils.TruncateAt.END);
                item.addView(label, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                return item;
            }
        };
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((p, v, pos, id) -> launch(entries.get(pos)));

        root.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    public void onBackPressed() {
        // Ein Launcher bleibt beim Zurück-Knopf auf dem Home-Bildschirm.
    }

    private void load() {
        PackageManager pm = getPackageManager();
        entries.clear();

        Entry s = new Entry();
        s.label = "Einstellungen";
        Drawable d = getDrawable(android.R.drawable.ic_menu_preferences).mutate();
        d.setTint(Color.WHITE);
        s.icon = d;
        s.intent = new Intent(Settings.ACTION_SETTINGS);
        entries.add(s);

        Set<String> seen = new HashSet<>();
        seen.add(getPackageName());
        List<Entry> apps = new ArrayList<>();
        String[] cats = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
        for (String cat : cats) {
            Intent q = new Intent(Intent.ACTION_MAIN).addCategory(cat);
            for (ResolveInfo ri : pm.queryIntentActivities(q, 0)) {
                String pkg = ri.activityInfo.packageName;
                if (!seen.add(pkg)) continue;
                Entry e = new Entry();
                e.label = ri.loadLabel(pm).toString();
                e.icon = ri.loadIcon(pm);
                e.intent = new Intent(Intent.ACTION_MAIN)
                        .addCategory(cat)
                        .setClassName(pkg, ri.activityInfo.name);
                apps.add(e);
            }
        }
        Collections.sort(apps, (a, b) -> a.label.compareToIgnoreCase(b.label));
        entries.addAll(apps);
        adapter.notifyDataSetChanged();
    }

    private void launch(Entry e) {
        try {
            Intent i = new Intent(e.intent);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
        } catch (Exception ex) {
            Toast.makeText(this, "Konnte nicht starten", Toast.LENGTH_SHORT).show();
        }
    }
}
