package com.example.realfrontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.data.Index;
import com.example.api.StackBackendDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.util.Log.d;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    MenuItem downloadedMenuItem;
    Spinner fakeNameSpinner;
    Spinner keySpinner;
    ArrayAdapter<String> keyAdapter;
    Button downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        final StackBackendDao stackBackendDao = ViewModelProviders.of(this).get(StackBackendDao.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.textView);

        // Download the index
        if (viewModel.getIndex() == Index.EMPTY) {
            d("log", "download_index");
            stackBackendDao.getIndex().enqueue(new Callback<List<Index.Book>>() {
                @Override
                public void onResponse(Call<List<Index.Book>> call, Response<List<Index.Book>> response) {
                    d("log", "getIndexOnResponse");
                    viewModel.setIndex(new Index(response.body()));
                    updateDownloadMenuItem(viewModel);
                    updateFakeNameSpinner(viewModel);
                }

                @Override
                public void onFailure(Call<List<Index.Book>> call, Throwable t) {
                    d("log", t.getMessage());
                    d("log", "getIndexFailure", t);
                }
            });
        }

        // Set up fake selection
        fakeNameSpinner = findViewById(R.id.fake_name_spinner);
        fakeNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                d("log", "fakeNameSpinner#onItemSelected");
                String seletedFakeName = (String) parent.getItemAtPosition(position);
                // Set options for key spinner
                keyAdapter.clear();
                keyAdapter.addAll(viewModel.getIndex().getKeysForFake(seletedFakeName));
                // Record selected fake
                viewModel.setSelectedFakeName(seletedFakeName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                d("log", "fakeNameSpinner#onNothingSelected");
            }
        });

        keySpinner = findViewById(R.id.key_spinner);
        keySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                d("log", "keySpinner#onNothingSelected");
                String selectedKey = (String) parent.getItemAtPosition(position);
                viewModel.setSelectedKey(selectedKey);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                d("log", "keySpinner#onNothingSelected");
            }
        });
        keyAdapter = new ArrayAdapter<String>(
                this, R.layout.support_simple_spinner_dropdown_item, viewModel.getIndex().getKeysForFake(""));
        keySpinner.setAdapter(keyAdapter);
        updateFakeNameSpinner(viewModel);

        // Set up download button
        downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Index.Book, Index.Fake> selectedBookAndFake = viewModel.getSelectedBookAndFake();
                stackBackendDao.getFake(selectedBookAndFake.first.getBookCode(), selectedBookAndFake.second.getFileName()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        d("pdf", "onResponse");
                        writeAndRenderPDF(response);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        d("pdf", t.getMessage());
                        d("pdf", "onFailure");
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        downloadedMenuItem = menu.getItem(0);

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if (viewModel.getIndex() == null) {
            downloadedMenuItem.setTitle("Downloading Index");
        } else {
            updateDownloadMenuItem(viewModel);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void writeAndRenderPDF(Response<ResponseBody> response) {
        File file = new File(getExternalFilesDir(null) + File.separator + "fake.pdf");
        boolean writtenToDisk = writeResponseBodyToDisk(response.body(), file);
        Intent pdfViewIntent = new Intent(Intent.ACTION_VIEW);
        Uri pdfUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        pdfViewIntent.setDataAndType(pdfUri,"application/pdf");
        pdfViewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pdfViewIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        Intent intent = Intent.createChooser(pdfViewIntent, "Open File");
        startActivity(intent);
    }

    private void updateText(int value) {
        textView.setText(String.valueOf(value));
    }

    private void updateDownloadMenuItem(MainViewModel viewModel) {
        downloadedMenuItem.setTitle("Fakes Available: " + viewModel.getIndex().getSize());
    }

    private void updateFakeNameSpinner(MainViewModel viewModel) {
        ArrayAdapter<String> fakeNameAdapter = new ArrayAdapter<String>(
                this, R.layout.support_simple_spinner_dropdown_item, viewModel.getIndex().getFakeDisplayNames());
        fakeNameSpinner.setAdapter(fakeNameAdapter);
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, File file) {
        try {
            // todo change the file location/name according to your needs

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    d("pdf", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
