package com.epicure.chronos.epicure;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.epicure.chronos.epicure.db.FavoriteDbHelper;
import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private static Intent newIntent = null;
    private OkHttpClient httpClient;
    private static final String TAG = "MainActivity", STD_URL = "https://api.imgur.com/3/gallery/user/rising/0.json";
    private static boolean searchUrl = false, isConnected = false;
    OAuth2AccessToken ACCESS_TOKEN = null;
    EpicureAdapter mAdapter = new EpicureAdapter();

    private String CODE_PIN = "";

    private FavoriteDbHelper favoriteDbHelper;

    InputStream in;
    String resultToDisplay;

    //CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(mAdapter.getClient_id(), mAdapter.getClient_secret());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        favoriteDbHelper = new FavoriteDbHelper(this);
        SQLiteDatabase db = favoriteDbHelper.getReadableDatabase();
        db.close();
        if (!searchUrl)
            fetchData(STD_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return (true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.button_connect:
                try {
                    mAdapter.connect();
                    String AuthUrl = mAdapter.getAuthorizationUrl();
                    if (!isConnected) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AuthUrl));

                        //System.out.println("Got it : " + AuthUrl);
                        final OAuth20Service service = new ServiceBuilder()
                                .apiKey(mAdapter.getClient_id())
                                .apiSecret(mAdapter.getClient_secret())
                                .build(ImgurApi.instance());

                       startActivity(intent); //aaaaaaaaaaaaa delete

                        final EditText editText = new EditText(this);
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle("PIN Access")
                                .setMessage("Enter your PIN access: ")
                                .setView(editText)
                                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String pinCode = String.valueOf(editText.getText());
                                        CODE_PIN = pinCode;

                                        if (Build.VERSION.SDK_INT >= 19)
                                        {
                                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                            StrictMode.setThreadPolicy(policy);
                                        }

                                        final OAuth20Service service = new ServiceBuilder()
                                                .apiKey(mAdapter.getClient_id())
                                                .apiSecret(mAdapter.getClient_secret())
                                                .build(ImgurApi.instance());

                                        OAuth2AccessToken accessToken = null;
                                        try {
                                            accessToken = service.getAccessToken(pinCode);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (accessToken != null) {
                                            System.out.println("Got the Access Token!");
                                            System.out.println("(if your curious it looks like this: " + accessToken
                                                    + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
                                            System.out.println();
                                            isConnected = true;
                                        }

                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create();
                        dialog.show();
                    }

                    else {
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle("Already Connected")
                                .setMessage("You are already connected")
                                .setNegativeButton("Ok", null)
                                .create();
                        dialog.show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.Favoris:
                System.out.println("Add to favoris guys !!! ");
                Intent intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
            case R.id.Reload:
                if (!searchUrl)
                fetchData(STD_URL);

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private static class Photo {
        String id;
        String title;
        String url;
    }

    private void fetchData(String urlRequest) {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url(urlRequest)
                .header("Authorization", "Client-ID " + mAdapter.getClient_id())
                .header("User-Agent", "My Little App")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "An error occured !");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    JSONArray items = data.getJSONArray("data");
                    final List<Photo> photos  = new ArrayList<Photo>();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        Photo photo = new Photo();
                        if (item.getBoolean("is_album")) {
                            photo.id = item.getString("cover");
                        }
                        else {
                            photo.id = item.getString("id");
                        }
                        photo.title = item.getString("title");
                        photo.url = item.getString("link");
                        photos.add(photo);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                render(photos);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title;
        TextView urlText;

        public PhotoVH(View itemView) {
            super(itemView);
        }
    }

    private void render(final List<Photo> photos) {
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_of_photos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.item, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                vh.urlText = (TextView) vh.itemView.findViewById(R.id.url_link);
                return vh;
            }

            @Override
            public void onBindViewHolder(PhotoVH holder, int position) {
                Picasso.with(MainActivity.this).load("https://i.imgur.com/" +
                        photos.get(position).id + ".jpg").into(holder.photo); //load l'objet selectionné.
                holder.title.setText(photos.get(position).title); // permet de selectionner l'objet qu'on veut !!!
                //holder.urlText.setText(photos.get(position).id);
                holder.urlText.setText("https://i.imgur.com/" + photos.get(position).id + ".jpg");


            }

            @Override
            public int getItemCount() {
                return photos.size();
            }
        };
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 1;
            }
        });
        rv.setAdapter(adapter);
    }

    p   ublic void addToFavorite(View view){
        View parent = (View) view.getParent();
        TextView titleImg = (TextView)findViewById(R.id.title);
        TextView urlImg = (TextView)findViewById(R.id.url_link);
        //ImageView img = (ImageView)findViewById(R.id.photo);

        String urlLoad = urlImg.getText().toString();
        String textTitleImg = titleImg.getText().toString();


        if (isConnected == true) {
            if (Build.VERSION.SDK_INT >= 19) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            String imgId = urlLoad.replaceAll("https://i.imgur.com/", "");
            String lastImgId = imgId.replaceAll(".jpg", "");

            System.out.println(lastImgId);
            String urlString = "https://api.imgur.com/3/image/" + lastImgId + "/favorite"; // URL to call
            String resultToDisplay = "";
            InputStream in = null;
            try {

                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try {
                resultToDisplay = IOUtils.toString(in, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("add This to favorite" + resultToDisplay);
        }
        else {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Not Connected")
                    .setMessage("You are not connected")
                    .setNegativeButton("Ok", null)
                    .create();
            dialog.show();
        }
        return ;
    }

    public void deleteToFavorite(View view){
        View parent = (View) view.getParent();
        TextView titleImg = (TextView)findViewById(R.id.title);
        TextView urlImg = (TextView)findViewById(R.id.url_link);
        //ImageView img = (ImageView)findViewById(R.id.photo);

        String urlLoad = urlImg.getText().toString();
        String textTitleImg = titleImg.getText().toString();

        //editing databases
        /*SQLiteDatabase db = favoriteDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoriteContract.FavoriteEntry.COL_FAV_URL, urlLoad);
        db.insertWithOnConflict(FavoriteContract.FavoriteEntry.TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        System.out.println("add This to favorite");*/
    }
}