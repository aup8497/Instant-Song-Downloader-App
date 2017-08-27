package com.example.akshayuprabhu.instantmusic;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends Activity implements View.OnClickListener {

    private DownloadManager downloadManager;
    EditText songname;
    Button search;
    String finalurl="";
    String urlforyimp3="";
    String url="";
    int flag = 0;
    String first_url="";
    String download_url="";
    String songtitle,songt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songname=(EditText)findViewById(R.id.songnameET);
        search=(Button)findViewById(R.id.searchb);

        search.setOnClickListener(this);

    }

    // Async task for downloading
    public class downloader extends AsyncTask<String, Void ,Void> {

        @Override
        protected Void doInBackground(String... params) {

            int flagToDifferenciateUrl=0;
            String sname1=params[0];
            String songname=sname1.trim();
            String sname = songname.replaceAll(" ","+");

            String urlfromyoutube= ("https://m.youtube.com/results?search_query=" + sname );

            // get the link of the video using entered search keyword
            try{
            
                  Document doc = Jsoup.connect(urlfromyoutube)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
                        .timeout(6000)
                        .get();
                  Elements ele = doc.select("ol.item-section");

                  for (Element element : ele.select("li") ) {

                    download_url = element.select("a.yt-uix-sessionlink.spf-link").attr("href");
                    songt=element.select("a.yt-uix-sessionlink.spf-link").attr("title");

                    if (flag == 0) {
                        if (download_url.contains("/watch?")) {
                            first_url = download_url;
                            songtitle=songt;
                            flag = 1;
                        }
                    }

                  }
                
                // try to download the song using youtube in mp3 API.
                    urlforyimp3 = ("http://www.youtubeinmp3.com/fetch/?video=https://www.youtube.com" + first_url);

                    Document sd;
                    sd = Jsoup.connect(urlforyimp3).timeout(6000).get();
                    Elements ele2 = sd.select("div.infoBox");

                    for (Element element2 : ele2.select("p")) {
                        finalurl = "http://www.youtubeinmp3.com" + element2.select("a.button.fullWidth").attr("href");
                    }

                }catch (Exception e) {
                
                    // in case this fails to give direct link, make flag=1 which indicates that it has redirected it to another link for downloading.
                    e.printStackTrace();
                    flagToDifferenciateUrl = 1;
                    finalurl = urlforyimp3 ;

                } finally {

                    // download from the final url link, which provides direct link to the song
                    if ( finalurl!="" && flagToDifferenciateUrl==1) {
                        flagToDifferenciateUrl=0;
                        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        Uri Download_Uri = Uri.parse(finalurl);
                        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                        finalurl = "";
                        request.setTitle(sname1 + ".mp3");
                        request.setDescription("file is being downloaded");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, songtitle + ".mp3");

                        downloadManager.enqueue(request);
                    }
                    else
                    {
                        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(finalurl));
                        startActivity(viewIntent);
                    }

                }
            return null;
        }


    }

     public void onClick(View view) {
         ConnectivityManager cm =
                 (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

         NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
         boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (songname.getText().toString().equals("") ) {
            Toast.makeText(MainActivity.this, "Enter Text", LENGTH_SHORT).show();
        }else if(!isConnected){
            Toast.makeText(MainActivity.this, "No Internet Connectivity!", LENGTH_SHORT).show();
        } else {
            String songName = songname.getText().toString();
            downloader task=new downloader();                
            try{
                task.execute(songName);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }//end of on click method




}
