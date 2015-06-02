package mx.seycel.samasey;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {

    EditText editTextMobileNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMobileNo = (EditText)findViewById(R.id.editTextMobileNo);

        if(!ReusableClass.getFromPreference("Mobile_No",MainActivity.this).equalsIgnoreCase(""))
        {
            editTextMobileNo.setText(ReusableClass.getFromPreference("Mobile_No", MainActivity.this));
        }
    }

    public void savingMobileNo(View view) {
        ReusableClass.saveInPreference("Mobile_No", editTextMobileNo.getText().toString(), MainActivity.this);
        Toast.makeText(this, "Thanks for saving Mobile No !!",Toast.LENGTH_LONG).show();
        finish();
    }
}
