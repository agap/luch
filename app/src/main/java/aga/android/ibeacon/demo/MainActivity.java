package aga.android.ibeacon.demo;

import android.os.Bundle;

import aga.android.ibeacon.BeaconScanner;
import aga.android.ibeacon.IScanner;
import aga.android.ibeacon.R;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final IScanner scanner = new BeaconScanner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        scanner.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanner.stop();
    }
}
