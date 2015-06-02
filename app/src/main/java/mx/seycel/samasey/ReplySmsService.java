package mx.seycel.samasey;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ReplySmsService extends Service {

    private static final String SOAP_ACTION = "urn:recargas#saldo";
    private static final String METHOD_NAME = "saldo";
    private static final String NAMESPACE = "urn:recargas";
    private static final String URL = "http://seycel.com.mx/ws/res2.php?wsdl";


    public static final String ACTION_SMS_SENT = "com.mycompany.myapp.SMS_SENT";
    public static final String ACTION_SMS_DELIVERED = "com.mycompany.myapp.SMS_DELIVERED";

    public ReplySmsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String senderNum        = intent.getStringExtra("senderNum");
        String message          = intent.getStringExtra("message");

        SimpleDateFormat sdf    = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String currentDateTime  = sdf.format(new Date());
        String deviceMobileNo   = ReusableClass.getFromPreference("Mobile_No", ReplySmsService.this) ;

        new GetServerResponseTask().execute(senderNum, deviceMobileNo, message, currentDateTime);

        return super.onStartCommand(intent, flags, startId);
    }

    private class GetServerResponseTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... values) {

            String result = "Nothing";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("usuario", values[0]);  // Parameter for sender No
            request.addProperty("palabra", values[1]);  // Parameter for Device No
            request.addProperty("sms", values[2]);      // Parameter for Sms Body
            request.addProperty("fecha", values[3]);    // Parameter for current Date and time

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
            return result + "@@#@@" + values[0];
        }

        protected void onPostExecute(String result) {
            Log.d("TAG", "value: " + result);

            String[] mobileNoAndResult = result.split("@@#@@");
            String serverResponse = mobileNoAndResult[0];
            String mobileNo       = mobileNoAndResult[1];

            Log.d("TAG", "serverResponse: " + serverResponse + " ::: mobileNo: " + mobileNo);

            sendSMS(mobileNo, serverResponse);
        }
    }

    private void sendSMS(String number, String msg)
    {
        SmsManager sm = SmsManager.getDefault();
        ArrayList<String> parts = sm.divideMessage(msg);

        Intent iSent = new Intent(ACTION_SMS_SENT);
        PendingIntent piSent = PendingIntent.getBroadcast(this, 0, iSent, 0);
        Intent iDel = new Intent(ACTION_SMS_DELIVERED);
        PendingIntent piDel = PendingIntent.getBroadcast(this, 0, iDel, 0);

        if (parts.size() == 1)
        {
            msg = parts.get(0);
            sm.sendTextMessage(number, null, msg, piSent, piDel);
        }
        else
        {
            ArrayList<PendingIntent> sentPis = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> delPis = new ArrayList<PendingIntent>();

            int ct = parts.size();
            for (int i = 0; i < ct; i++)
            {
                sentPis.add(i, piSent);
                delPis.add(i, piDel);
            }

            sm.sendMultipartTextMessage(number, null, parts, sentPis, delPis);
        }
        stopSelf();
    }

    public class SmsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action.equals(ACTION_SMS_SENT))
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            else if (action.equals(ACTION_SMS_DELIVERED))
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }
}
