package com.myapp.fn_android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 */
public class AthleticsFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    List<String[]> todayList;
    List<String[]> tomList;
    List<String[]> weekList;
    List<String[]> resList;
    List<String> sportList;
    AthleticsRecyclerViewAdapter todAdapter;
    AthleticsRecyclerViewAdapter tomAdapter;
    AthleticsRecyclerViewAdapter weekAdapter;
    AthleticsRecyclerViewAdapter resAdapter;
    AthleticsRecyclerViewAdapter sportAdapter;

    List<String> sportsFiltered = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AthleticsFragment() {
    }

    @SuppressWarnings("unused")
    public static AthleticsFragment newInstance(int columnCount) {
        AthleticsFragment fragment = new AthleticsFragment();
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
        setHasOptionsMenu(true);
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
                filter(newText,todayList, 0);
                filter(newText,tomList, 1);
                filter(newText,weekList, 2);
                filter(newText,resList,3);
                return false;
            }
        });
    }

    private void filter(String text, List<String[]> list, int type) {
        List<String[]> filteredlist = new ArrayList<>();
        for (String[] arr : list) {
            if (arr[0].toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(arr);
            }
        }
        if (type == 0)
            todAdapter.filterList(filteredlist); // Change list to new filtered list
        else if (type == 1)
            tomAdapter.filterList(filteredlist);
        else if (type == 2)
            weekAdapter.filterList(filteredlist);
        else
            resAdapter.filterList(filteredlist);
    }

    private void filterBySport(List<String[]> list, int type) {
        List<String[]> filteredlist = new ArrayList<>();
        for (String[] arr : list) {
            if (!sportsFiltered.contains(arr[2])) {
                filteredlist.add(arr);
            }
        }
        if (type == 0)
            todAdapter.filterList(filteredlist); // Change list to new filtered list
        else if (type == 1)
            tomAdapter.filterList(filteredlist);
        else if (type == 2)
            weekAdapter.filterList(filteredlist);
        else
            resAdapter.filterList(filteredlist);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_athletics_list, container, false);

        // Get Dates
        TextView today = view.findViewById(R.id.todayDateText);
        TextView tomorrow = view.findViewById(R.id.tomorrowDateText);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,1);
        String tomDate = sdf.format(cal.getTime());

        // Set the adapter
        Context context = view.getContext();
        RecyclerView todayRecyclerView = view.findViewById(R.id.todayList);
        RecyclerView tomRecyclerView = view.findViewById(R.id.tomorrowList);
        RecyclerView weekRecyclerView = view.findViewById(R.id.thisweekList);
        RecyclerView resRecyclerView = view.findViewById(R.id.resultsList);
        RecyclerView sportRecyclerView = view.findViewById(R.id.sportsList);
        if (mColumnCount <= 1) {
            todayRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            tomRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            weekRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            resRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            sportRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            todayRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            tomRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            weekRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            resRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            sportRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Use this to change content
            todayList = new ArrayList<>();
            tomList = new ArrayList<>();
            weekList = new ArrayList<>();
            resList = new ArrayList<>();
            sportList = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/athleticsGet.php");
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        // Create the event name
                        String name = jsonObject.getString("sportTitle") + " ";
                        if (jsonObject.getString("location_indicator").equals("H"))
                            name += "vs ";
                        else
                            name += "at ";
                        name += jsonObject.getString("opponent");
                        if (!jsonObject.getString("noplayText").equals("")) { // Check if the game is cancelled
                            String can = jsonObject.getString("noplayText");
                            name += " " + can.toUpperCase();
                        }
                        // Check the date
                        // 0 is today, 1 is tomorrow, 2 is this week, -1 is in past or more than a week
                        String date = jsonObject.getString("eventdate");
                        date = date.substring(0,date.indexOf(" "));
                        int check = checkDate(date);
                        cal.set(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,Integer.parseInt(date.substring(8)));
                        date = sdf.format(cal.getTime());
                        if (check == 0) todayList.add(new String[]{name,jsonObject.getString("time"),jsonObject.getString("sportTitle"),""});
                        else if (check == 1) tomList.add(new String[]{name,jsonObject.getString("time"),jsonObject.getString("sportTitle"),""});
                        else if (check == 2) weekList.add(new String[]{name,date + " " + jsonObject.getString("time"),jsonObject.getString("sportTitle"),""});
                        else if (check == -2) {
                            String result = "";
                            if (!jsonObject.isNull("resultStatus")) {
                                result = jsonObject.getString("resultStatus") + " " + jsonObject.getString("resultUs") +
                                        "-" + jsonObject.getString("resultThem");
                                if (!jsonObject.isNull("postscore_info"))
                                    result += " " + jsonObject.getString("postscore_info");
                            }
                            if (!jsonObject.getString("url").equals("null"))
                                resList.add(new String[]{name,result,jsonObject.getString("sportTitle"),jsonObject.getString("url")});
                            else
                                resList.add(new String[]{name,result,jsonObject.getString("sportTitle"),""});
                        }

                        // Create the list of sports
                        if (!sportList.contains(jsonObject.getString("sportTitle")))
                            sportList.add(jsonObject.getString("sportTitle"));
                    }
                }
                else {
                    todayList.add(new String[]{"No events found","","",""});
                    tomList.add(new String[]{"No events found","","",""});
                    weekList.add(new String[]{"No events found","","",""});
                    resList.add(new String[]{"No results found","","",""});
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                todayRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                todAdapter = new AthleticsRecyclerViewAdapter(todayList,0,this, this.getContext());
                todayRecyclerView.setAdapter(todAdapter);
                tomRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                tomAdapter = new AthleticsRecyclerViewAdapter(tomList,0, this, this.getContext());
                tomRecyclerView.setAdapter(tomAdapter);
                weekRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                weekAdapter = new AthleticsRecyclerViewAdapter(weekList,0, this, this.getContext());
                weekRecyclerView.setAdapter(weekAdapter);
                resRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                resAdapter = new AthleticsRecyclerViewAdapter(resList,0, this, this.getContext());
                resRecyclerView.setAdapter(resAdapter);

                List<String[]> list = new ArrayList<>();
                while (!sportList.isEmpty())
                    list.add(new String[] {sportList.remove(0),"on","",""});
                sportRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                sportAdapter = new AthleticsRecyclerViewAdapter(list,0, this, this.getContext());
                sportRecyclerView.setAdapter(sportAdapter);

                today.setText(todayDate);
                tomorrow.setText(tomDate);
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
            if (brack == -1) return "]"; // Empty php file
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
        requireActivity().setTitle(R.string.athletics_text);

        // Navigate to website when clicked
        Button link = view.findViewById(R.id.linkButton);
        link.setOnClickListener(v -> {
            String url = "https://furmanpaladins.com/index.aspx";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

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
                } else {
                    open = false;
                    filterView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private int checkDate(String date) {
        // 0 is today, 1 is tomorrow, 2 is this week, -1 is past or more than a week, -2 is past two days
        SimpleDateFormat sdf = new SimpleDateFormat("DDD yyyy", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());
        int todayDay = Integer.parseInt(todayDate.substring(0,3));
        int todayYear = Integer.parseInt(todayDate.substring(4));

        Calendar calendar = new GregorianCalendar(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,
                Integer.parseInt(date.substring(8))); // Month starts at 0 for some reason?!
        String checkDate = sdf.format(calendar.getTime());
        int checkDay = Integer.parseInt(checkDate.substring(0,3));
        int checkYear = Integer.parseInt(date.substring(0,4));

        if (todayDay == checkDay && todayYear == checkYear) return 0; // Same day
        if (todayDay <= 358) { // Edge cases where adding goes into next year
            if (todayDay+1 == checkDay && todayYear == checkYear) return 1; // Tomorrow
            if (checkDay-todayDay <= 7 && checkDay-todayDay > 0 && todayYear == checkYear) return 2; // Within week
        }
        else { // Will be comparing into next year
            if (todayDay+1%365 == checkDay && (todayYear == checkYear || todayYear == checkYear+1)) return 1; // Tomorrow
            if (checkDay+365-todayDay <= 7 && checkDay+365-todayDay > 0 && (todayYear == checkYear || todayYear == checkYear+1))
                return 2; // Within week
        }
        // Check if in past two days
        if (checkDay <= 363) // Edge cases where adding goes into next year
            if (todayDay-checkDay <= 2 && todayDay-checkDay > 0 && todayYear == checkYear) return -2; // Past two days
        else // Will be comparing into next year
            if (todayDay+365-checkDay <= 2 && todayDay+365-checkDay > 0 && (todayYear == checkYear || todayYear == checkYear+1))
                return -2; // Past two days
        return -1; // Not within the week
    }

    public void filterFromAdapter(String sport) {
        if (sportsFiltered.contains(sport))
            sportsFiltered.remove(sport);
        else
            sportsFiltered.add(sport);
        filterBySport(todayList,0);
        filterBySport(tomList,1);
        filterBySport(weekList,2);
        filterBySport(resList,3);
    }

}