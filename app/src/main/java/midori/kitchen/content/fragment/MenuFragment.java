package midori.kitchen.content.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import midori.kitchen.R;
import midori.kitchen.content.adapter.MenuAdapter;
import midori.kitchen.content.adapter.ResepAdapter;
import midori.kitchen.content.model.MenuModel;
import midori.kitchen.manager.AppData;
import midori.kitchen.manager.JSONControl;

/**
 * Created by M. Asrof Bayhaqqi on 3/11/2017.
 */

public class MenuFragment extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;


    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<MenuModel> menuItems = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitleBar = (TextView) getActivity().findViewById(R.id.tv_title_bar);
        tvTitleBar.setText(R.string.midori_kitchen);

        initRecyclerView();
        getDataMenu();
    }

    private void initRecyclerView() {
        try {
            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            adapter = new MenuAdapter(menuItems, getActivity());
            recyclerView.setAdapter(adapter);
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getDataMenu();
                }
            });
        }catch (Exception e){}
    }

    private void getDataMenu() {
        new GetProduk(getActivity()).execute();
    }

    private class GetProduk extends AsyncTask<String, Void, String> {
        private Activity activity;
        private Context context;
        private Resources resources;

        public GetProduk(Activity activity) {
            super();
            this.activity = activity;
            this.context = activity.getApplicationContext();
            this.resources = activity.getResources();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefresh.setRefreshing(true);
            menuItems.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONControl jsControl = new JSONControl();
                JSONObject response = jsControl.getAllMenus(activity);

                if (!response.toString().contains("error")) {
                    JSONArray allmenus = response.getJSONArray("allmenus");
                    for(int i =0; i< allmenus.length();i++){
                        MenuModel menuModel = new MenuModel();
                        JSONObject menuObject = allmenus.getJSONObject(i);
                        menuModel.setId(menuObject.getString("menuId"));
                        menuModel.setMenu(menuObject.getString("menuNama"));
                        menuModel.setPrice_menu(menuObject.getInt("menuHarga"));
                        menuModel.setDescription(menuObject.getString("menuDeskripsi"));
                        menuModel.setStok(menuObject.getInt("menuStok"));
                        menuModel.setDeliveryDate(menuObject.getString("menuJadwal"));
                        menuModel.setPhoto(menuObject.getString("menuImage"));
                        menuModel.setIbuNama(menuObject.getString("ibuNama"));
                        menuModel.setIbuId(menuObject.getString("ibuId"));
                        menuItems.add(menuModel);

                    }

                    return "OK";
                } else {
                    return "FAIL";
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            switch (result) {
                case "FAIL":
                    Log.d("Menu", "fail");
                    break;
                case "OK":
                    adapter = new MenuAdapter(menuItems, activity);
                    recyclerView.setAdapter(adapter);
                    swipeRefresh.setRefreshing(false);
                    swipeRefresh.setEnabled(true);
                    break;
            }
        }
    }

    private class GetProdukBukalapak extends AsyncTask<String, Void, String> {
        private Activity activity;
        private Context context;
        private Resources resources;

        public GetProdukBukalapak(Activity activity) {
            super();
            this.activity = activity;
            this.context = activity.getApplicationContext();
            this.resources = activity.getResources();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefresh.setRefreshing(true);
            menuItems.clear();
            adapter.notifyDataSetChanged();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                String ibuName, menuNama, desc, deliveryDate;

                JSONControl jsControl = new JSONControl();
                JSONObject response = jsControl.bukalapakMidoriLapak(AppData.Base64KeyMidoriKitchen);
                    JSONArray allmenus = response.getJSONArray("products");
                    for(int i =0; i< allmenus.length();i++){
                        MenuModel menuModel = new MenuModel();
                        JSONObject menuObject = allmenus.getJSONObject(i);
                        menuModel.setId(menuObject.getString("id"));
                        String words[] = menuObject.getString("name").split("-");
                        String words2[] = menuObject.getString("desc").split("-");
                        try{
                            ibuName = words[0];
                        }
                        catch (Exception e){
                            ibuName = "Midori Kitchen";
                        }
                        try{
                            menuNama = words[1];
                        }
                        catch (Exception e){
                            menuNama = "Menu Midori Kitchen";
                        }
                        try{
                            desc = words2[0];}
                        catch (Exception e){
                            desc = "-";
                        }
                        try{
                            deliveryDate = words2[1];}
                        catch (Exception e){
                            deliveryDate = "Now";
                        }
                        menuModel.setMenu(menuNama);
                        menuModel.setPrice_menu(menuObject.getInt("price"));
                        menuModel.setDescription(desc);
                        menuModel.setStok(menuObject.getInt("stock"));
                        menuModel.setDeliveryDate(deliveryDate);
                        JSONArray images = menuObject.getJSONArray("images");
                        menuModel.setPhoto(images.getString(0));
                        menuModel.setIbuNama(ibuName);
                        JSONObject responseIbu = jsControl.postIbuProfile(ibuName);
                        menuModel.setIbuId(responseIbu.getString("id"));
                        menuItems.add(menuModel);

                    }

                    return "OK";



            } catch (Exception e) {
                e.printStackTrace();
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            switch (result) {
                case "FAIL":
                    break;
                case "OK":
                    adapter = new MenuAdapter(menuItems, activity);
                    recyclerView.setAdapter(adapter);

                    swipeRefresh.setRefreshing(false);
                    swipeRefresh.setEnabled(true);
                    break;
            }
        }
    }
}
