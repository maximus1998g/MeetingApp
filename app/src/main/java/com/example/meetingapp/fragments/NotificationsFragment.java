package com.example.meetingapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.meetingapp.R;
import com.example.meetingapp.adapters.NotificationsAdapter;
import com.example.meetingapp.api.RetrofitClient;
import com.example.meetingapp.interfaces.NotificationListener;
import com.example.meetingapp.models.RequestGet;
import com.example.meetingapp.models.RequestSend;
import com.example.meetingapp.services.UserProfileManager;
import com.example.meetingapp.services.WebSocketListenerService;
import com.example.meetingapp.utils.PreferenceUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    @BindView(R.id.recycle_view)
    RecyclerView recycleView;

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private NotificationsAdapter notificationsAdapter;
    private List<RequestGet> eventRequests;
    private BroadcastReceiver broadcastReceiver;

    private NotificationListener listener;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this, view);
        eventRequests = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        recycleView.setLayoutManager(linearLayoutManager);

        recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int userProfileId = UserProfileManager.getInstance().getMyProfile().getId();
                for (int i = 0; i < eventRequests.size(); i++)
                    if (!eventRequests.get(i).isSeen() &&
                            (!eventRequests.get(i).getDecision().equals("NO_ANSWER") && eventRequests.get(i).getFromUser().getId() == userProfileId))
                        seenRequest(i);
//                    else if (eventRequests.get(i).isSeen() || eventRequests.get(i).getToUser().getId() == userProfileId && !eventRequests.get(i).getDecision().equals("NO_ANSWER"))
//                        notificationsAdapter.notifyDataSetChanged();
            }
        });


        loadNotifications();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            eventRequests = new ArrayList<>();
            notificationsAdapter = null;
            loadNotifications();
            swipeRefreshLayout.setRefreshing(false);
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(WebSocketListenerService.EXTRA_REQUEST)) {
                    Gson gson = new Gson();
                    RequestGet eventRequest = gson.fromJson(intent.getStringExtra(
                            WebSocketListenerService.EXTRA_REQUEST), RequestGet.class);

                    if (!exists(eventRequest)) {
                        eventRequests.add(0, eventRequest);
                        notificationsAdapter.notifyItemInserted(0);
                        recycleView.smoothScrollToPosition(0);
                    }

//                    MainActivity instance = MainActivity.instance;
//                    instance.subNotificationBadge(1);

                }
            }
        };

        return view;
    }

    private boolean exists(RequestGet newRequest) {
        for (RequestGet request : eventRequests)
            if (request.getId() == newRequest.getId())
                return true;
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (NotificationListener) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver((broadcastReceiver),
                new IntentFilter(WebSocketListenerService.EXTRA_RESULT)
        );
    }

    @Override
    public void onStop() {

//        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private void loadNotifications() {
        Call<List<RequestGet>> call = RetrofitClient
                .getInstance(PreferenceUtils.getToken(requireContext()))
                .getApi()
                .getRequests();

        call.enqueue(new Callback<List<RequestGet>>() {
            @Override
            public void onResponse(@NonNull Call<List<RequestGet>> call, @NonNull Response<List<RequestGet>> response) {
                eventRequests = response.body();
                if (eventRequests != null) {

                    notificationsAdapter = new NotificationsAdapter(getContext(), eventRequests);
                    recycleView.setAdapter(notificationsAdapter);

//                    listener.addNotificationBadge(345);
//                    ((MainActivity)getActivity()).aaa(6);

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RequestGet>> call, @NonNull Throwable t) {
                Log.d("failure", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void seenRequest(int position) {
        Call<RequestGet> call = RetrofitClient
                .getInstance(PreferenceUtils.getToken(requireContext()))
                .getApi()
                .answerRequest(String.valueOf(eventRequests.get(position).getId()), new RequestSend(true));

        call.enqueue(new Callback<RequestGet>() {
            @Override
            public void onResponse(@NonNull Call<RequestGet> call, @NonNull Response<RequestGet> response) {
                eventRequests.get(position).setSeen(true);
//                notificationsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<RequestGet> call, @NonNull Throwable t) {
                int a = 0;
            }
        });
    }
}