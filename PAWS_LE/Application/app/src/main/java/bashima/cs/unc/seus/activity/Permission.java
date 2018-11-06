package bashima.cs.unc.seus.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import seus.bashima.cs.unc.seus.R;

import static bashima.cs.unc.seus.constant.Constant.REQUEST_SELECT_DEVICE;

public class Permission extends AppCompatActivity {

    public Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        final Context context = this;
        activity = this;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);


        }
        else {
            Intent i = new Intent(Permission.this, LoadingScreenActivity.class);
            this.startActivity(i);
            finish();
        }
        final TextView text = (TextView) findViewById(R.id.textView5);
        text.setVisibility(View.INVISIBLE);

        Button btnContinue = (Button) findViewById(R.id.bt_continue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    text.setVisibility(View.VISIBLE);
                    Toast.makeText(context, "Can't start the app without permission", Toast.LENGTH_LONG).show();

                }
                else{
                    Intent i = new Intent(Permission.this, LoadingScreenActivity.class);
                    activity.startActivity(i);
                    finish();
                }
            }
        });

    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
