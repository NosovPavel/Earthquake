package example.earthquake;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by Pavel on 24/02/15.
 */
public class EarthQuakeDialog extends DialogFragment {
    private static String DIALOG_STRING = "DIALOG_STRING";

    public static EarthQuakeDialog newInstance(Context context,Quake quake){

        //Создаем новый экземпляр фрагмента
        EarthQuakeDialog fragment = new EarthQuakeDialog();
        Bundle args = new Bundle();

        SimpleDateFormat sdf  =  new  SimpleDateFormat
                ("dd/MM/yyyy  HH:mm:ss");
        String  dateString  =  sdf.format(quake.getDate());
        String  quakeText  = dateString  +  "\n"  +  "Magnitude"  +  quake.getMagnitude()  + "\n"  +  quake.getDetails()  +  "\n" +
        quake.getLink();
        args.putString(DIALOG_STRING,  quakeText);
        fragment.setArguments(args);
        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quake_details,container,false);

        String title = getArguments().getString(DIALOG_STRING);

        TextView tv = (TextView) view.findViewById(R.id.quakeDetailsTextView);

        tv.setText(title);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Earthquake details.");
        return dialog;
    }
}
