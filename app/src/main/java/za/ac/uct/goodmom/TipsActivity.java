package za.ac.uct.goodmom;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TipsActivity extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = TipsActivity.class.getSimpleName();

    /**
     * URL to query the dataset for information
     */
    private static final String DSA_REQUEST_URL = "https://www.diabetessa.org.za/feed/";
    private static final String SLM_REQUEST_URL = "https://sweetlifemag.co.za/feed/";
    private static final String PC_REQUEST_URL = "https://pregnantchicken.com/feed/";
    //private static final String DSA_REQUEST_URL = "http://newsmomsneed.marchofdimes.org/?feed=rss2";

    private RecyclerView mPostRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;

    private PostAdapter mPostAdapter;
    private List<PostData> mPostData = new ArrayList<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    Intent tipsIntent = new Intent(TipsActivity.this, DashboardActivity.class);
                    startActivity(tipsIntent);
                    return true;
                case R.id.navigation_reminders:
                    Intent remindersIntent = new Intent(TipsActivity.this, RemindersActivity.class);
                    startActivity(remindersIntent);
                    return true;
                case R.id.navigation_messenger:
                    Intent messengerIntent = new Intent(TipsActivity.this, MessengerActivity.class);
                    startActivity(messengerIntent);
                    return true;
                case R.id.navigation_tips:
                    // Current activity
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        // Initialize references to views
        mPostRecyclerView = this.findViewById(R.id.post_recycler_view);
        mSwipeLayout = findViewById(R.id.swipe_refresh_layout);

        // Initialise the post adapter
        mPostAdapter = new PostAdapter(mPostData);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mPostRecyclerView.setLayoutManager(mLayoutManager);
        mPostRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPostRecyclerView.addItemDecoration(new CustomDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));

        // set the adapter
        mPostRecyclerView.setAdapter(mPostAdapter);

        // row click listener
        mPostRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mPostRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                PostData post = mPostData.get(position);
                //Toast.makeText(getApplicationContext(), post.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
                Intent postviewIntent = new Intent(TipsActivity.this, WebViewActivity.class);
                postviewIntent.putExtra("postUrl", post.getUrl());
                startActivity(postviewIntent);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        // Swipe listener
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Kick off an {@link AsyncTask} to perform the network request
                HTTPDownloadTask task = new HTTPDownloadTask();
                task.execute();
            }
        });

        // Kick off an {@link AsyncTask} to perform the network request
        HTTPDownloadTask task = new HTTPDownloadTask();
        task.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent dashboardIntent = new Intent(TipsActivity.this, DashboardActivity.class);
        startActivity(dashboardIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_menu:
                Intent profileIntent = new Intent(TipsActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.signout_menu:
                AuthUI.getInstance().signOut(this);
                Intent logoutIntent = new Intent(TipsActivity.this, LandingActivity.class);
                startActivity(logoutIntent);
                return true;
            case R.id.settings_menu:
                Intent settingsIntent = new Intent(TipsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.help_menu:
                Intent helpIntent = new Intent(TipsActivity.this, HelpActivity.class);
                startActivity(helpIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Update the screen to display information from the given {@link PostData}.
     */
    private void updateUi(ArrayList<PostData> posts) {
        Collections.sort(posts);
        for (int i = 0; i < posts.size(); i++) {
            mPostData.add(posts.get(i));
        }
        mPostAdapter.notifyDataSetChanged();
    }

    /**
     * Returns a formatted date and time string for when the earthquake happened.
     */
    private String convertDateString(String srcDate) {
        String destString = null;

        SimpleDateFormat srcFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        SimpleDateFormat destFormat = new SimpleDateFormat("EEE, d MMM yyyy");

        try {
            Date dateObj = srcFormat.parse(srcDate);
            destString = destFormat.format(dateObj);
        } catch (ParseException e) {

            Log.e(LOG_TAG, "Error with pull parse", e);
        }
        return destString;
    }

    /**
     * {@liink AsyncTask } to perform the network request on a bg_main thread, and then
     * update the UI with the response
     */

    private class HTTPDownloadTask extends AsyncTask<String, Integer, ArrayList<PostData>> {

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<PostData> doInBackground(String... strings) {
            // Create URL objects
            URL url1 = createUrl(DSA_REQUEST_URL);
            URL url2 = createUrl(SLM_REQUEST_URL);
            URL url3 = createUrl(PC_REQUEST_URL);

            // Perform HTTP request to the URL and receive an ArrayList of PostData response back
            ArrayList<PostData> postDataList = new ArrayList<>();
            try {
                postDataList = makeHttpRequest(url1);
                postDataList.addAll(makeHttpRequest(url2));
                postDataList.addAll(makeHttpRequest(url3));
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the PostData results");
            }

            return postDataList;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link HTTPDownloadTask}).
         */
        @Override
        protected void onPostExecute(ArrayList<PostData> posts) {
            if (posts == null) {
                return;
            }

            updateUi(posts);
            mSwipeLayout.setRefreshing(false);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error with creating URL", e);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a ArrayList of PostData as the response.
         */
        private ArrayList<PostData> makeHttpRequest(URL url) throws IOException {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            XmlPullParserFactory xmlFactoryObject = null;
            XmlPullParser parser = null;
            ArrayList<PostData> postDataList = new ArrayList<>();

            try {
                // Set up connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.setDoInput(true);
                urlConnection.connect();

                // If request is successful (response code 200), then read input and parse response
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();

                    // Instantiate the parser
                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    parser = xmlFactoryObject.newPullParser();

                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(inputStream, null);

                    // Parse the XML
                    parser.nextTag();
                    int eventType = parser.getEventType();
                    String text = null;
                    PostData post = new PostData();
                    String holdWebsiteIconUrl = null;
                    while (eventType != XmlPullParser.END_DOCUMENT && postDataList.size() <= 10) {
                        String name = parser.getName();

                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                break;

                            case XmlPullParser.TEXT:
                                text = parser.getText();
                                break;

                            case XmlPullParser.END_TAG:

                                if (name.equals("title")) {
                                    post.setTitle(text);
                                } else if (name.equals("pubDate")) {
                                    post.setDate(convertDateString(text));
                                } else if (name.equals("link")) {
                                    post.setUrl(text);
                                } else if (name.equals("url")) {
                                    //post.setThumbUrl(text);
                                    holdWebsiteIconUrl = text;
                                }

                                break;
                        }

                        if (post.getTitle() != null && post.getDate() != null && post.getUrl() != null) {
                            post.setThumbUrl(holdWebsiteIconUrl);
                            postDataList.add(post);
                            text = null;
                            post = new PostData();
                        }

                        eventType = parser.next();
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            } catch (XmlPullParserException e) {
                Log.e(LOG_TAG, "Error with pull parse", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return postDataList;
        }
    }
}
