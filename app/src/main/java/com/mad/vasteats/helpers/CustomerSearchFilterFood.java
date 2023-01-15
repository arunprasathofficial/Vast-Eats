package com.mad.vasteats.helpers;

import android.widget.Filter;

import com.mad.vasteats.models.Food;
import com.mad.vasteats.presenters.CustomerFoodAdapter;

import java.util.ArrayList;

public class CustomerSearchFilterFood extends Filter {

    private CustomerFoodAdapter customerFoodAdapter;
    private ArrayList<Food> filterList;

    public CustomerSearchFilterFood(CustomerFoodAdapter customerFoodAdapter, ArrayList<Food> filterList) {
        this.customerFoodAdapter = customerFoodAdapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();

        //validate data for search query
        if (charSequence != null && charSequence.length() > 0) {
            //change to upper case to make insensitive
            charSequence = charSequence.toString().toUpperCase();
            //store our filtered list
            ArrayList<Food> foodArrayList = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                //perform search by title or category
                if (filterList.get(i).getFoodName().toUpperCase().contains(charSequence) ||
                        filterList.get(i).getFoodCategory().toUpperCase().contains(charSequence)) {
                    //add filtered data to list
                    foodArrayList.add(filterList.get(i));
                }
            }
            filterResults.count = foodArrayList.size();
            filterResults.values = foodArrayList;
        } else {
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        customerFoodAdapter.foodArrayList = (ArrayList<Food>) filterResults.values;
        //refresh adapter
        customerFoodAdapter.notifyDataSetChanged();
    }
}
