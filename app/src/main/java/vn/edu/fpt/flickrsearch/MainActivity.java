package vn.edu.fpt.flickrsearch;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends ListActivity {
    private static final String SEARCHES = "searches";
    private static final String FLICKR_API_URL = "https://www.flickr.com/services/rest/?method=flickr.photos.search&api_key=0222df10f044edf766558ca7ea8b9cbb&text=";

    private EditText queryEditText;
    private EditText tagEditText;
    private SharedPreferences savedSearches;
    private ArrayList<String> tags;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);

        savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);
        tags = new ArrayList<String>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, tags);
        setListAdapter(adapter);

        ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);

        getListView().setOnItemClickListener(itemClickListener);
        getListView().setOnItemLongClickListener(itemLongClickListener);
    }

    public View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0) {
                addTaggedSearch(queryEditText.getText().toString(), tagEditText.getText().toString());
                queryEditText.setText("");
                tagEditText.setText("");

                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.missingMessage);
                builder.setPositiveButton(R.string.OK, null);
                AlertDialog errorDialog = builder.create();
                errorDialog.show();
            }
        }
    };

    private void addTaggedSearch(String query, String tag) {
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query);
        preferencesEditor.apply();

        if (!tags.contains(tag)) {
            tags.add(tag);
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged();
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String tag = ((TextView) view).getText().toString();
            String query = savedSearches.getString(tag, "");
            String urlString = FLICKR_API_URL + Uri.encode(query, "UTF-8") + "&format=json&nojsoncallback=1";

            Intent intent = new Intent(MainActivity.this, ImageDisplayActivity.class);
            intent.putExtra("searchUrl", urlString);
            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final String tag = ((TextView) view).getText().toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.shareEditDelete));
            builder.setItems(new CharSequence[]{getString(R.string.share), getString(R.string.edit), getString(R.string.delete)},
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    shareSearch(tag);
                                    break;
                                case 1:
                                    tagEditText.setText(tag);
                                    queryEditText.setText(savedSearches.getString(tag, ""));
                                    break;
                                case 2:
                                    deleteSearch(tag);
                                    break;
                            }
                        }
                    });
            builder.setNegativeButton(getString(R.string.cancel), null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        }
    };

    private void shareSearch(String tag) {
        String urlString = FLICKR_API_URL + Uri.encode(savedSearches.getString(tag, ""), "UTF-8") + "&format=json&nojsoncallback=1";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, urlString);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }

    private void deleteSearch(final String tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.deleteConfirmation));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor preferencesEditor = savedSearches.edit();
                preferencesEditor.remove(tag);
                preferencesEditor.apply();

                tags.remove(tag);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}