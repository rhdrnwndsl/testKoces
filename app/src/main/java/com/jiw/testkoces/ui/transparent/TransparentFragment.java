package com.jiw.testkoces.ui.transparent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jiw.testkoces.databinding.FragmentTransparentBinding;

public class TransparentFragment extends Fragment {

    private FragmentTransparentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TransparentViewModel transparentViewModel =
                new ViewModelProvider(this).get(TransparentViewModel.class);

        binding = FragmentTransparentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textTransparent;
        transparentViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}