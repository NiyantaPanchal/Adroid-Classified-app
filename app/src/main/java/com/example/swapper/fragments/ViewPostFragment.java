package com.example.swapper.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.swapper.R;
import com.example.swapper.SearchActivity;
import com.example.swapper.model.Post;
import com.example.swapper.util.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ViewPostFragment extends Fragment {
    private static final String TAG = "ViewPostFragment";
    //widgets
    private TextView mContactSeller, mTitle, mDescription, mPrice, mLocation, mSavePost;
    private ImageView mClose, mWatchList, mPostImage;

    //vars
    private String mPostId;
    private Post mPost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostId = (String) getArguments().get(getString(R.string.arg_post_id));
        Log.d(TAG, "onCreate: got the post id: " + mPostId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_view_post, container, false);
        mContactSeller = (TextView) view.findViewById(R.id.post_contact);
        mTitle = (TextView) view.findViewById(R.id.post_title);
        mDescription = (TextView) view.findViewById(R.id.post_description);
        mPrice = (TextView) view.findViewById(R.id.post_price);
        mLocation = (TextView) view.findViewById(R.id.post_location);
        mClose = (ImageView) view.findViewById(R.id.post_close);
        mWatchList = (ImageView) view.findViewById(R.id.add_watch_list);
        mPostImage = (ImageView) view.findViewById(R.id.post_image);
        mSavePost = (TextView) view.findViewById(R.id.save_post);

        init();

        hideSoftKeyboard();

        return view;
    }

    private void init(){
        getPostInfo();

        //view the post in more detail
        Fragment fragment = (Fragment)((SearchActivity)getActivity()).getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                        ((SearchActivity)getActivity()).mViewPager.getCurrentItem());
        if(fragment != null){
            //SearchFragment (AKA #0)
            if(fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":0")){
                Log.d(TAG, "onClick: switching to: " + getActivity().getString(R.string.fragment_view_post));

                mSavePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addItemToWatchList();
                    }
                });

                mWatchList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addItemToWatchList();
                    }
                });

            }
            //WatchList Fragment (AKA #1)
            else if(fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":1")){
                Log.d(TAG, "onClick: switching to: " + getActivity().getString(R.string.fragment_watch_list));

                mSavePost.setText("remove post");
                mSavePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItemFromWatchList();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });

                mWatchList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItemFromWatchList();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        }

        mContactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {mPost.getContact_email()});
                getActivity().startActivity(emailIntent);
            }
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing post.");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mSavePost.setShadowLayer(5, 0 , 0, Color.BLUE);
        mWatchList.setImageBitmap(createOutline(BitmapFactory.decodeResource(getResources(), R.drawable.ic_save_white)));
        mWatchList.setColorFilter(Color.BLUE);
        mClose.setImageBitmap(createOutline(BitmapFactory.decodeResource(getResources(), R.drawable.ic_x_white)));
        mClose.setColorFilter(Color.BLUE);
    }

    private void addItemToWatchList(){
        Log.d(TAG, "addItemToWatchList: adding item to watch list.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.node_watch_list))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .child(getString(R.string.field_post_id))
                .setValue(mPostId);

        Toast.makeText(getActivity(), "Added to watch list", Toast.LENGTH_SHORT).show();
    }

    private void removeItemFromWatchList(){
        Log.d(TAG, "removeItemFromWatchList: removing item from watch list.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.node_watch_list))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .removeValue();

        Toast.makeText(getActivity(), "Removed from watch list", Toast.LENGTH_SHORT).show();
    }

    private void getPostInfo(){
        Log.d(TAG, "getPostInfo: getting the post information.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_posts))
                .orderByKey()
                .equalTo(mPostId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                if(singleSnapshot != null){
                    mPost = singleSnapshot.getValue(Post.class);
                    Log.d(TAG, "onDataChange: found the post: " + mPost.getTitle());

                    mTitle.setText(mPost.getTitle());
                    mDescription.setText(mPost.getDescription());

                    String price = "FREE";
                    if(mPost.getPrice() != null){
                        price = "$" + mPost.getPrice();
                    }
                    mPrice.setText(price);
                    String location = mPost.getCity() + ", " + mPost.getState_province() + ", " +
                            mPost.getCountry();
                    mLocation.setText(location);
                    UniversalImageLoader.setImage(mPost.getImage(), mPostImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void hideSoftKeyboard(){
        final Activity activity = getActivity();
        final InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private Bitmap createOutline(Bitmap src){
        Paint p = new Paint();
        p.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.OUTER));
        return src.extractAlpha(p, null);
    }
}






