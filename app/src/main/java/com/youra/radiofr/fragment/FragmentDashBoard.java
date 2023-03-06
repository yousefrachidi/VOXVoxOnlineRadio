package com.youra.radiofr.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.youra.radiofr.R;
import com.youra.radiofr.activity.MainActivity;

public class FragmentDashBoard extends Fragment {

    private FragmentManager fm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        fm = getFragmentManager();

        FragmentMost f1 = new FragmentMost();
        loadFrag(f1, getString(R.string.most));

        setHasOptionsMenu(true);
        return rootView;
    }

    public void loadFrag(Fragment f1, String name) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (name.equals(getString(R.string.search))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment_dash, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment_dash, f1, name);
        }
        ft.commit();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
    }
}