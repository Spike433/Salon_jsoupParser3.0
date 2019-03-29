package com.example.jura.salon;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// *****************************JURA&&&&&&&&&&&&&&&&&&&&&&&&&&&&************//

public class MainActivity extends AppCompatActivity implements  View.OnClickListener{

    public int pocetakRadnogVremena=8,krajRadnogVremena=16;

    private WebView webView;

    private Button buttonSalji;
    Button btnDatePicker, btnTimePicker;
    TextView txtDate,txtTime;
    ListView listView;
    EditText editTextIme; // 1.

    public int mYear, mMonth, mDay, mHour, mMinute,mDanMjeseca;
    boolean vri=false,dat=false; // vrijeme i datum
    boolean internet=false;
    boolean SlanjePrviPut=false,SlanjePrviPutParsanje=false;
    boolean jeLiTerminZauzet=false;
    public int MinuteIzmeduSisanja=30;

    public StringBuilder sDate=new StringBuilder("");
    public StringBuilder sTime=new StringBuilder("");
    public StringBuilder link=new StringBuilder("");


    ArrayList<String> arrayList=new ArrayList<>();
    List<Calendar> kalendar=new ArrayList<>();




    String words;


    public class doit extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                org.jsoup.nodes.Document doc =Jsoup.connect("http://touristgps.azurewebsites.net/speed").get();
                words=doc.text();

            String[] parts = words.split("&");

            kalendar.clear();

            for (int i=0;i< parts.length;i++){

                String[]  dateAndTime=parts[i].split("\\.");  // 14.06.2012.06.12&
                                                                    // 0  1   2  3  4
                Calendar tren= Calendar.getInstance();
                tren.set(Integer.parseInt(dateAndTime[2]), Integer.parseInt(dateAndTime[1])-1,Integer.parseInt(dateAndTime[0]),Integer.parseInt(dateAndTime[3]),Integer.parseInt(dateAndTime[4]));

                kalendar.add((Calendar) tren.clone());



            }

            for (int i = 0; i < kalendar.size(); i++) {

            // ova jebena java krecu mjeseci od 0, uvijek moras za 1 manje spremat, (cast) .clone(da se ne dodijeli svemu

                Calendar t=(Calendar) kalendar.get(i).clone();

                Calendar krajSisanja=(Calendar) kalendar.get(i).clone();

                krajSisanja.add(Calendar.MINUTE,30);


                arrayList.add("Dan         :   "+t.get(Calendar.DAY_OF_MONTH)+'\n'+"Mjesec   :   "+(t.get(Calendar.MONTH)+1) +'\n'+"Godina   :   "+t.get(Calendar.YEAR)+'\n'+"Vrijeme  :   "+t.get(Calendar.HOUR_OF_DAY)+":"+t.get(Calendar.MINUTE) +" - " +krajSisanja.get(Calendar.HOUR_OF_DAY)+":"+krajSisanja.get(Calendar.MINUTE));

            }
            } catch (Exception e ) {
                e.printStackTrace();
            }

            return null;
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=(ListView)findViewById(R.id.listview);



        if (AppStatus.getInstance(getApplicationContext()).isOnline()) {

            internet=true;
            //Create Adapter
            new doit().execute();

            ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayList);

            //assign adapter to listview

            listView.setAdapter(arrayAdapter);

            listView.setVisibility(View.INVISIBLE);





        } else {

            Toast.makeText(getApplicationContext(), "Nema interneta!", Toast.LENGTH_SHORT).show();

        }

        buttonSalji=(Button)findViewById(R.id.button);
        btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnTimePicker=(Button)findViewById(R.id.btn_time);
        txtDate=(TextView) findViewById(R.id.in_date);
        txtTime=(TextView)findViewById(R.id.in_time); //333
        editTextIme=(EditText)findViewById(R.id.ime); //2

        buttonSalji.setOnClickListener(this);
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        // new doit().execute();          // ovo mora bit kad ima interneta

        if (v == btnDatePicker) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            Date today = Calendar.getInstance().getTime();

                            Calendar datum = Calendar.getInstance();
                            datum.set(year, monthOfYear, dayOfMonth,0,0);


                            mDanMjeseca=dayOfMonth;

                            if(datum.getTime().after(today) || datum.getTime().equals(today)) {
                                txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                txtDate.setError(null);
                                dat=true;
                                // 2008 06 14 T 131236

                                sDate.append(year);
                                if(monthOfYear<10)sDate.append('0');
                                sDate.append((monthOfYear+1));
                                if(dayOfMonth<10)sDate.append('0');
                                sDate.append(dayOfMonth);
                                sDate.append('T');
                            }

                            else{
                                txtDate.setText(null);
                              //  txtDate.getText().clear();
                                txtDate.setError("Datum mora biti veći od današnjeg");
                                dat=false;
                            }

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
       }

        if (v == btnTimePicker) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            if (dat == false)
                            {
                                txtDate.setText(null);
                                // txtTime.getText().clear();     111
                                txtDate.setError("Prvo namijestite datum");
                            }
                            else{

                            if(hourOfDay>=pocetakRadnogVremena && hourOfDay <=krajRadnogVremena) {

                                for (int i=0;i<kalendar.size();i++) {

                                    Calendar trenutnoVrijeme = (Calendar) kalendar.get(i).clone();

                                    if (trenutnoVrijeme.get(Calendar.YEAR) == mYear && trenutnoVrijeme.get(Calendar.MONTH) == mMonth && trenutnoVrijeme.get(Calendar.DAY_OF_MONTH) ==mDanMjeseca ) {
                                     //   jeLiTerminZauzet = true;

                                        Calendar krajVrijeme= (Calendar) (Calendar) kalendar.get(i).clone();

                                        krajVrijeme.add(Calendar.MINUTE, MinuteIzmeduSisanja);

                                        Calendar ovajTren= Calendar.getInstance();
                                        ovajTren.set(mYear,mMonth,mDanMjeseca,hourOfDay,minute);

                                        if((ovajTren.after(trenutnoVrijeme)|| ovajTren.equals(trenutnoVrijeme)) && (ovajTren.before(krajVrijeme))){

                                            jeLiTerminZauzet=true;
                                            break;

                                        }else{

                                            jeLiTerminZauzet=false;
                                            listView.setVisibility(View.INVISIBLE);

                                        }
                                    }
                                }

                                        if (!jeLiTerminZauzet) {

                                        txtTime.setText(hourOfDay + ":" + minute);
                                        txtTime.setError(null);
                                        vri = true;                     // 2008 06 14 T 13 12 36

                                        if (hourOfDay < 10) sTime.append('0');
                                        sTime.append(hourOfDay);
                                        if (minute < 10) sTime.append('0');
                                        sTime.append(minute);
                                        sTime.append("00");

                                        }else {

                                            txtTime.setText(null);
                                            txtTime.setError("Termin je zauzet, pogledajte u talici za slobodne");

                                            listView.setVisibility(View.VISIBLE);


                                        }

                            }else{
                                txtTime.setText(null);
                                txtTime.setError("Vrijeme mora biti unutar radnog vremena");
                                vri=false;
                            }
                            }

                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }

        if (AppStatus.getInstance(getApplicationContext()).isOnline()) {

            internet=true;

        }
        else{
            internet=false;
        }

        if(!internet){

                Toast.makeText(getApplicationContext(), "Nema interneta!", Toast.LENGTH_SHORT).show();

        }

        else if (v==buttonSalji ) {

            if(TextUtils.isEmpty(editTextIme.getText().toString())){
                editTextIme.setError("Unesite ime");
            }
            else{

                if (vri == true && dat == true && !SlanjePrviPut) {

                    SlanjePrviPut = true;
                    vri = false;
                    dat = false;


                    webView = (WebView) findViewById(R.id.webView1);
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    // param1=20190614T041236&&param2=car&&api_key=M6N015C8W6ALJ

                    link.append("http://touristgps.azurewebsites.net/update?param1=");
                    link.append(sDate);
                    link.append(sTime);
                    link.append("&&param2=");
                    link.append(editTextIme.getText());
                    link.append("&&api_key=M6N015C8W6ALJ");

                    webView.loadUrl(link.toString());

                    webView.setWebViewClient(new WebViewClient() {

                        @Override
                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                            handler.proceed();

                        }
                    });

                    Toast.makeText(getApplicationContext(), "Zahtijev je poslan!", Toast.LENGTH_SHORT).show();


                } else if (SlanjePrviPut) {

                    Toast.makeText(getApplicationContext(), "Iz sigurnosnih razloga ugasite pa upalite aplikaciju za ponovno slanje", Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(getApplicationContext(), "Vrijeme ili datum nisu postavljeni!", Toast.LENGTH_SHORT).show();

                }
            }
        }


        }
    }











