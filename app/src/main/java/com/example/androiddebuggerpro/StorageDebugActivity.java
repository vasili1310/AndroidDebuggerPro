package com.example.androiddebuggerpro;

import android.app.Activity;
import android.os.Bundle;
import android.os.StatFs;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class StorageDebugActivity extends Activity {
    private final String[] labels = {"App Files", "Cache", "External", "Free Space"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_debug);

        LineChart chart = findViewById(R.id.lineChart);
        TextView infoText = findViewById(R.id.storageInfoText);

        List<Float> sizes = getStorageSizesMB();
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < sizes.size(); i++) {
            entries.add(new Entry(i, sizes.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Storage Usage (MB)");
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(true);

        LineData data = new LineData(dataSet);
        chart.setData(data);

        chart.getXAxis().setValueFormatter(new LabelFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setText("");
        chart.invalidate();

        // Show text version
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.length; i++) {
            sb.append(labels[i]).append(": ").append(String.format("%.2f", sizes.get(i))).append("MB\n");
        }
        infoText.setText(sb.toString());
    }

    private List<Float> getStorageSizesMB() {
        List<Float> list = new ArrayList<>();
        list.add(getFolderSizeMB(getFilesDir()));
        list.add(getFolderSizeMB(getCacheDir()));
        list.add(getFolderSizeMB(getExternalFilesDir(null)));
        StatFs stat = new StatFs(getFilesDir().getAbsolutePath());
        float free = stat.getAvailableBytes() / (1024f * 1024f);
        list.add(free);
        return list;
    }

    private float getFolderSizeMB(File dir) {
        if (dir == null || !dir.exists()) return 0f;
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    size += f.length();
                } else if (f.isDirectory()) {
                    size += (long) getFolderSizeMB(f);
                }
            }
        }
        return size / (1024f * 1024f);
    }

    static class LabelFormatter extends ValueFormatter {
        private final String[] labels;

        LabelFormatter(String[] labels) {
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            return (index >= 0 && index < labels.length) ? labels[index] : "";
        }
    }
}
