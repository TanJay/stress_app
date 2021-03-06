package com.tanushaj.element.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tanushaj.element.LevelActivity;
import com.tanushaj.element.R;
import com.tanushaj.element.SessionViewAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements SessionViewAdapter.ItemClickListener, View.OnClickListener {

    SessionViewAdapter adapter;
        List<String> beats;
    List<String> links = new ArrayList<>();


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button liveFeedButton;

    Button recom1;
    Button recom2;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        beats = new ArrayList<>();
        beats.add("Study");
        beats.add("Focus");
        beats.add("Calm");

        links.add("https://elementapp.s3-ap-southeast-1.amazonaws.com/2298596870.wav");
        links.add("https://elementapp.s3-ap-southeast-1.amazonaws.com/2822761320.wav");
        links.add("https://elementapp.s3-ap-southeast-1.amazonaws.com/4215351455.wav");


        recom1 = view.findViewById(R.id.recommend1);
        recom2 = view.findViewById(R.id.recommend2);
        recom1.setOnClickListener(this);
        recom2.setOnClickListener(this);

        // set up the RecyclerView
//        RecyclerView recyclerView = view.findViewById(R.id.session_recycle);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new SessionViewAdapter(getContext(), animalNames);
//        adapter.setClickListener(this);
//        recyclerView.setAdapter(adapter);

        RecyclerView recyclerView = view.findViewById(R.id.session_recycle);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 2);
        adapter = new SessionViewAdapter(getContext(), beats);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(View view, int position) {
//        String item = beats.get(position);
//        Toast.makeText(getContext(), item, Toast.LENGTH_LONG).show();
//        startActivity(new Intent(getContext(), LevelActivity.class));
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(getContext(), LevelActivity.class));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
