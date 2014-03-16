package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.dev.flashback_v04.Parser;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.SharedPrefs;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.MyThreadsAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-09.
 */
public class MyThreadsFragment extends ListFragment {

    public class GetMyThreadsTask extends AsyncTask<String, HashMap<String, String>, Boolean> {

        Callback mProgressUpdate;
        Callback mCallback;
        Parser mParser;
        Context mContext;

        public GetMyThreadsTask(MainActivity mActivity, Callback threadFetched) {
            mContext = mActivity;
            mCallback = threadFetched;
            mParser = new Parser(mActivity);
            mProgressUpdate = new Callback<HashMap<String, String>>() {
                @Override
                public void onTaskComplete(HashMap<String, String> data) {
                    publishProgress(data);
                }
            };
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            mParser.getMyThreads(strings[0], mProgressUpdate);
            return null;
        }

        @Override
        protected void onProgressUpdate(HashMap<String, String>... values) {
            super.onProgressUpdate(values);
            mCallback.onTaskComplete(values[0]);
        }
    }

    private MainActivity mActivity;
    private MyThreadsAdapter myThreadsAdapter;
    private GetMyThreadsTask getMyThreadsTask;
    private Callback threadFetched;

    private int pageNumber;
    private int numPages;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity)activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("AdapterValues", myThreadsAdapter.getItems());
        outState.putInt("PageNumber", pageNumber);
        outState.putInt("NumPages", numPages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		myThreadsAdapter = new MyThreadsAdapter(mActivity);

        if(savedInstanceState != null) {
            // Restore adapter
            ArrayList<HashMap<String, String>> savedValues = (ArrayList<HashMap<String, String>>) savedInstanceState.get("AdapterValues");
            myThreadsAdapter.setItems(savedValues);
            myThreadsAdapter.notifyDataSetChanged();
        }

        setListAdapter(myThreadsAdapter);

        threadFetched = new Callback<HashMap<String, String>>() {
            @Override
            public void onTaskComplete(HashMap<String, String> data) {
                myThreadsAdapter.addItem(data);
                myThreadsAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_list_pager_layout, container, false);
        TextView header;
        TextView headerRight;
        String numPagesText;

        if(savedInstanceState == null) {
            String userId = Integer.toString(SharedPrefs.getPreference(mActivity, "user", "ID"));

            pageNumber = getArguments().getInt("PageNumber");
            numPages = getArguments().getInt("NumPages");
            getMyThreadsTask = new GetMyThreadsTask(mActivity, threadFetched);
            getMyThreadsTask.execute("https://www.flashback.org/find_threads_by_user.php?userid="+ userId +"&sortorder=DESC&sortby=lastpost&page=" + pageNumber);
        } else {
            pageNumber = savedInstanceState.getInt("PageNumber");
            numPages = savedInstanceState.getInt("NumPages");
        }

        numPagesText = "Sida " + pageNumber + " av " + numPages;

        header = (TextView)view.findViewById(R.id.headerleft);
        headerRight = (TextView)view.findViewById(R.id.headerright);
        header.setText("Mina startade ämnen");
        headerRight.setText(numPagesText);
        return view;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
		String url = myThreadsAdapter.getItems().get(position).get("ThreadLink");
		String threadname = myThreadsAdapter.getItems().get(position).get("ThreadName");
		try {
			mActivity.openThread(url, 0, threadname);
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = mActivity.getMenuInflater();
		inflater.inflate(R.menu.thread_context, menu);
	}
}