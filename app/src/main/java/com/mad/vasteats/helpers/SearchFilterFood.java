package com.mad.vasteats.helpers;

import android.widget.Filter;

import com.mad.vasteats.models.Food;
import com.mad.vasteats.presenters.MerchantFoodAdapter;

import java.util.ArrayList;

public class SearchFilterFood extends Filter {

    private MerchantFoodAdapter merchantFoodAdapter;
    private ArrayList<Food> filterList;

    public SearchFilterFood(MerchantFoodAdapter merchantFoodAdapter, ArrayList<Food> filterList) {
        this.merchantFoodAdapter = merchantFoodAdapter;
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
            ArrayList<Food> filteredFood = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                //perform search by title or category
                if (filterList.get(i).getFoodName().toUpperCase().contains(charSequence) ||
                        filterList.get(i).getFoodCategory().toUpperCase().contains(charSequence)) {
                    //add filtered data to list
                    filteredFood.add(filterList.get(i));
                }
            }
            filterResults.count = filteredFood.size();
            filterResults.values = filteredFood;
        } else {
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        merchantFoodAdapter.foodArrayList = (ArrayList<Food>) filterResults.values;
        //refresh adapter
        merchantFoodAdapter.notifyDataSetChanged();
    }
}
