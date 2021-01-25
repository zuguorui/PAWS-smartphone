package bashima.cs.unc.seus.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import seus.bashima.cs.unc.seus.R;

public class TrainProgressDialog extends DialogFragment {
    private TextView tvTitle;
    private TextView tvContent;
    private ProgressBar pb;

    private String title = "正在训练";
    private String content = "训练中";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root =  LayoutInflater.from(getActivity()).inflate(R.layout.dialog_process, null, false);
        tvTitle = (TextView)root.findViewById(R.id.tv_title);
        tvContent = (TextView)root.findViewById(R.id.tv_content);
        pb = (ProgressBar)root.findViewById(R.id.pb);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root);
        return builder.create();
    }

    public void setContent(String content)
    {
        this.content = content;
        if(tvContent != null)
        {
            tvContent.setText(content);
        }
    }

    public void setTitle(String title)
    {
        this.title = title;
        if(tvTitle != null)
        {
            tvTitle.setText(title);
        }
    }



}
