package com.mad.vasteats.helpers;

import android.widget.Filter;

import com.mad.vasteats.models.Merchant;
import com.mad.vasteats.presenters.CustomerMerchantAdapter;

import java.util.ArrayList;

public class SearchFilterMerchant extends Filter {

    private CustomerMerchantAdapter customerMerchantAdapter;
    private ArrayList<Merchant> filterList;

    public SearchFilterMerchant(CustomerMerchantAdapter customerMerchantAdapter, ArrayList<Merchant> filterList) {
        this.customerMerchantAdapter = customerMerchantAdapter;
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
            ArrayList<Merchant> filteredMerchant = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                //perform search by merchant name
                if (filterList.get(i).getMerchantName().toUpperCase().contains(charSequence)) {
                    //add filtered data to list
                    filteredMerchant.add(filterList.get(i));
                }
            }
            filterResults.count = filteredMerchant.size();
            filterResults.values = filteredMerchant;
        } else {
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        customerMerchantAdapter.merchantArrayList = (ArrayList<Merchant>) filterResults.values;
        //refresh adapter
        customerMerchantAdapter.notifyDataSetChanged();
    }
}
