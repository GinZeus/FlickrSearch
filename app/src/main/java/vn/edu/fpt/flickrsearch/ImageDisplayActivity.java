package vn.edu.fpt.flickrsearch;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class ImageDisplayActivity extends AppCompatActivity {
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private ArrayList<String> imageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        gridView = findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter(this, imageUrls);
        gridView.setAdapter(imageAdapter);

        String searchUrl = getIntent().getStringExtra("searchUrl");
        fetchImages(searchUrl );
    }

    private void fetchImages(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject photos = response.getJSONObject("photos");
                            JSONArray photoArray = photos.getJSONArray("photo");

                            for (int i = 0; i < photoArray.length(); i++) {
                                JSONObject photo = photoArray.getJSONObject(i);
                                String farmId = String.valueOf(photo.getInt("farm"));
                                String serverId = photo.getString("server");
                                String id = photo.getString("id");
                                String secret = photo.getString("secret");

                                String imageUrl = "https://farm" + farmId + ".staticflickr.com/"
                                        + serverId + "/" + id + "_" + secret + ".jpg";
                                imageUrls.add(imageUrl);
                            }
                            imageAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ImageDisplayActivity.this,
                                    "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ImageDisplayActivity.this,
                                "Error fetching images", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonObjectRequest);
    }
}