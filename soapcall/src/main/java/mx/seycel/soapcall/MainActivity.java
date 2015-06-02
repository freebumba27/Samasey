package mx.seycel.soapcall;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


public class MainActivity extends Activity {

    private static final String SOAP_ACTION = "urn:recargas#saldo";
    private static final String METHOD_NAME = "saldo";
    private static final String NAMESPACE = "urn:recargas";
    private static final String URL = "http://seycel.com.mx/ws/res2.php?wsdl";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new UserRegistrationTask().execute();
    }

    private class UserRegistrationTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... values) {

            String result = "Nothing";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("usuario", "12121212125");// Parameter for Method
            request.addProperty("palabra", "0439267236");// Parameter for Method
            request.addProperty("sms", "saldo");// Parameter for Method
            request.addProperty("fecha", "15-05-30 20:52:20");// Parameter for Method

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            try {
                androidHttpTransport.call(SOAP_ACTION, envelope);

                //SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
                // SoapPrimitive  resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                String Result = resultsRequestSOAP.getProperty(0).toString();


                Log.d("TAG", "Response::" + Result.toString());
                result = Result.toString();

            } catch (Exception e) {
                result = "Error"+e;
                System.out.println("Error"+e);
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Log.d("TAG", "value: " + result);
            ((TextView)findViewById(R.id.helloWorld)).setText(result);
        }
    }
}
