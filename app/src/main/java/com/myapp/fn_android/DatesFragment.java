package com.myapp.fn_android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 */
public class DatesFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    List<String[]> datesList;
    DatesRecyclerViewAdapter datesAdapter;
    List<String> categoryList;
    DatesRecyclerViewAdapter categoryAdapter;
    List<String> categoriesFiltered = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DatesFragment() {
    }

    @SuppressWarnings("unused")
    public static DatesFragment newInstance(int columnCount) {
        DatesFragment fragment = new DatesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        setHasOptionsMenu(true); // IMPORTANT

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void filter(String text) {
        List<String[]> filteredlist = new ArrayList<>();
        for (String[] arr : datesList) {
            if (arr[0].toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(arr);
            }
        }
        datesAdapter.filterList(filteredlist); // Change list to new filtered list
    }

    private void filterByCategory() {
        List<String[]> filteredlist = new ArrayList<>();
        for (String[] arr : datesList) {
            if (!categoriesFiltered.contains(arr[2])) {
                filteredlist.add(arr);
            }
        }
        datesAdapter.filterList(filteredlist); // Change list to new filtered list
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dates_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView datesRecyclerView = view.findViewById(R.id.datesList);
        RecyclerView catRecyclerView = view.findViewById(R.id.categoriesList);

        if (mColumnCount <= 1) {
            datesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            catRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            datesRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            catRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Setting the date list
            datesList = new ArrayList<>();
            categoryList = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/importantDateGet.php");
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (!pastDate(jsonObject.getString("date"))) {
                            datesList.add(new String[]{jsonObject.getString("title"), convertDate(jsonObject.getString("date"))
                                    , jsonObject.getString("category")});

                            // Setting the category list
                            if (!categoryList.contains(jsonObject.getString("category")))
                                categoryList.add(jsonObject.getString("category"));
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                datesRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                datesAdapter = new DatesRecyclerViewAdapter(datesList,0, this);
                datesRecyclerView.setAdapter(datesAdapter);

                List<String[]> list = new ArrayList<>();
                while (!categoryList.isEmpty()) {
                    list.add(new String[]{categoryList.remove(0),"on",""});
                }
                catRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                categoryAdapter = new DatesRecyclerViewAdapter(list,0,this);
                catRecyclerView.setAdapter(categoryAdapter);
            });
        });

        return view;
    }

    public static String makeServiceCall (String reqUrl) {
        String line;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();
            connection.disconnect();
            in.close();

            StringBuilder str = new StringBuilder("[");
            int brack = line.indexOf("[");
            line = line.substring(brack,line.length()-1);
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                str.append(jsonObject.toString()).append(",");
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
            str.append("]");
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "I died";
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Open the filter page
        ImageButton filterButton = view.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            Boolean open = false;
            final ConstraintLayout filterView = view.findViewById(R.id.FilterLayout);
            @Override
            public void onClick(View v) {
                if (!open) {
                    open = true;
                    filterView.setVisibility(View.VISIBLE);
                }
                else {
                    open = false;
                    filterView.setVisibility(View.INVISIBLE);
                }
            }
        });


//        view.findViewById(R.id.backButton).setOnClickListener(v -> NavHostFragment.findNavController(DatesFragment.this)
//                .navigate(R.id.action_datesFragment_to_homeFragment));
//        view.findViewById(R.id.backText).setOnClickListener(v -> NavHostFragment.findNavController(DatesFragment.this)
//                .navigate(R.id.action_datesFragment_to_homeFragment));
    }

    private String convertDate(String input) {
        String year = input.substring(0,4);
        String month = input.substring(5,7);
        String day = input.substring(8);
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return months[Integer.parseInt(month)-1] + " " + day + ", " + year;
    }

    private boolean pastDate(String date) {
        int iYear = Integer.parseInt(date.substring(0,4));
        int iMonth = Integer.parseInt(date.substring(5,7));
        int iDay = Integer.parseInt(date.substring(8));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String todayDate = sdf.format(Calendar.getInstance().getTime());
        int cYear = Integer.parseInt(todayDate.substring(0,4));
        int cMonth = Integer.parseInt(todayDate.substring(5,7));
        int cDay = Integer.parseInt(todayDate.substring(8));

        if (iYear < cYear) return true; // Past input's year
        if (iYear > cYear) return false; // Before input's year
        // Working within same year now
        if (iMonth < cMonth) return true;  // Past input's month
        if (iMonth > cMonth) return false; // Before input's month
        // Working within same month
        return iDay < cDay; // Past input's day or same day or before input's day
    }

    public void filterFromAdapter(String category) {
        if (categoriesFiltered.contains(category))
            categoriesFiltered.remove(category);
        else
            categoriesFiltered.add(category);
        filterByCategory();
    }

}