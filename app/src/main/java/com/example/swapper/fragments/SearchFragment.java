package com.example.swapper.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapper.FiltersActivity;
import com.example.swapper.R;
import com.example.swapper.model.HitsList;
import com.example.swapper.model.HitsObject;
import com.example.swapper.model.Post;
import com.example.swapper.util.ElasticSearchAPI;
import com.example.swapper.util.PostListAdapter;
import com.example.swapper.util.RecyclerViewMargin;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String BASE_URL = "http://23.236.56.178//elasticsearch/posts/post/";
    private static final int NUM_GRID_COLUMNS = 3;
    private static final int GRID_ITEM_MARGIN = 5;

    //widgets
    private ImageView mFilters;
    private EditText mSearchText;
    private FrameLayout mFrameLayout;


    //vars
    private String mElasticSearchPassword;
    private String mPrefCity;
    private String mPrefStateProv;
    private String mPrefCountry;
    private ArrayList<Post> mPosts;
    private RecyclerView mRecyclerView;
    private PostListAdapter mAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_search, container, false);

        mFilters = (ImageView) view.findViewById(R.id.ic_search);
        mSearchText = (EditText) view.findViewById(R.id.input_search);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.container);
        init();

        return view;
    }

    private void setupPostsList(){
        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new PostListAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void init() {
        mFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to filters activity.");
                Intent intent = new Intent(getActivity(), FiltersActivity.class);
                startActivity(intent);
            }
        });
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    mPosts = new ArrayList<Post>();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ElasticSearchAPI searchAPI = retrofit.create(ElasticSearchAPI.class);

                    HashMap<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Authorization", Credentials.basic("user", mElasticSearchPassword));

                    String searchString = "";

                    if (!mSearchText.equals("")) {
                        searchString = searchString + mSearchText.getText().toString() + "*";
                    }
                    if (!mPrefCity.equals("")) {
                        searchString = searchString + " city:" + mPrefCity;
                    }
                    if (!mPrefStateProv.equals("")) {
                        searchString = searchString + " state_province:" + mPrefStateProv;
                    }
                    if (!mPrefCountry.equals("")) {
                        searchString = searchString + " country:" + mPrefCountry;
                    }

                    Call<HitsObject> call = searchAPI.search(headerMap, "AND", searchString);

                    call.enqueue(new Callback<HitsObject>() {
                        @Override
                        public void onResponse(Call<HitsObject> call, Response<HitsObject> response) {

                            HitsList hitsList = new HitsList();
                            String jsonResponse = "";
                            try{
                                Log.d(TAG, "onResponse: server response: " + response.toString());

                                if(response.isSuccessful()){
                                    hitsList = response.body().getHits();
                                }else{
                                    jsonResponse = response.errorBody().string();
                                }

                                Log.d(TAG, "onResponse: hits: " + hitsList);

                                for(int i = 0; i < hitsList.getPostIndex().size(); i++){
                                    Log.d(TAG, "onResponse: data: " + hitsList.getPostIndex().get(i).getPost().toString());
                                    mPosts.add(hitsList.getPostIndex().get(i).getPost());
                                }

                                Log.d(TAG, "onResponse: size: " + mPosts.size());
                                //setup the list of posts
                                setupPostsList();

                            }catch (NullPointerException e){
                                Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                            }
                            catch (IndexOutOfBoundsException e){
                                Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                            }
                            catch (IOException e){
                                Log.e(TAG, "onResponse: IOException: " + e.getMessage() );
                            }
                        }

                        @Override
                        public void onFailure(Call<HitsObject> call, Throwable t) {
                            Log.e(TAG, "onFailure: " + t.getMessage() );
                            Toast.makeText(getActivity(), "search failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return false;
            }
        });
    }
    public void viewPost(String postId){
        ViewPostFragment fragment = new ViewPostFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        fragment.setArguments(args);

        transaction.replace(R.id.container, fragment, getString(R.string.fragment_view_post));
        transaction.addToBackStack(getString(R.string.fragment_view_post));
        transaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getFilters();
    }

                private void getElasticSearchPassword () {
                    Log.d(TAG, "getElasticSearchPassword: retrieving elasticsearch password.");

                    Query query = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.node_elasticsearch))
                            .orderByValue();

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                            mElasticSearchPassword = singleSnapshot.getValue().toString();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
        private void getFilters(){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mPrefCity = preferences.getString(getString(R.string.preferences_city), "");
            mPrefStateProv = preferences.getString(getString(R.string.preferences_state_province), "");
            mPrefCountry = preferences.getString(getString(R.string.preferences_country), "");

            Log.d(TAG, "getFilters: got filters: \ncity: " + mPrefCity + "\nState/Prov: " + mPrefStateProv
                    + "\nCountry: " + mPrefCountry);
        }

    }

