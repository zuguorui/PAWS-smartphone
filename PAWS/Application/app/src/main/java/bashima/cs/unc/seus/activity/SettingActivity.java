/*
 * MIT License
 *
 * Copyright (c) 2018, Stephen Xia, Columbia Intelligent and Connected Systems Lab (ICSL), Columbia University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package bashima.cs.unc.seus.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import bashima.cs.unc.seus.constant.Constant;
import seus.bashima.cs.unc.seus.R;

public class SettingActivity extends AppCompatActivity {

    CheckBox cbSound;
    CheckBox cbVibrate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        cbSound = (CheckBox) findViewById(R.id.cb_sound);
        cbVibrate = (CheckBox) findViewById(R.id.cb_vibrate);
        cbSound.setChecked(Constant.sound);
        cbVibrate.setChecked(Constant.vibrate);

        android.support.v7.app.ActionBar menu = getSupportActionBar();
        menu.setDisplayHomeAsUpEnabled(true);
        menu.setDisplayShowHomeEnabled(true);
        menu.setLogo(R.mipmap.ic_launcher);
        menu.setDisplayUseLogoEnabled(true);
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.cb_sound:
                if (checked){
                // Put some meat on the sandwich
                    Constant.sound = true;
                }
                else{
                    Constant.sound = false;
                }
                break;
            case R.id.cb_vibrate:
                if (checked){
                    Constant.vibrate = true;
                }
                else{
                    Constant.vibrate = false;
                }
                break;
            // TODO: Veggie sandwich
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
