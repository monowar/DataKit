package org.md2k.datakit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.md2k.datakit.configuration.Configuration;
import org.md2k.datakit.configuration.ConfigurationManager;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;

import java.util.ArrayList;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class PrefsFragmentSettingsUpload extends PreferenceFragment {

    private static final String TAG = PrefsFragmentSettingsUpload.class.getSimpleName();
    Configuration configuration;
    String[] network_type_show={
        "Any", "WiFi", "Cellular Data", "None"
    };
    String[] network_type_value={
            "ANY", "WIFI", "CELLULAR_DATA", "NONE"
    };

    private String getFreqShowFromValue(String value){
        if(value==null) return network_type_show[0];
        for(int i=0;i<network_type_value.length;i++)
            if(network_type_value[i].equals(value))
                return network_type_show[i];
        return network_type_show[0];
    }
    private String getFreqValueFromShow(String show){
        if(show==null) return network_type_value[0];
        for(int i=0;i<network_type_value.length;i++)
            if(network_type_show[i].equals(show))
                return network_type_value[i];
        return network_type_value[0];
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration=ConfigurationManager.getInstance(getActivity()).configuration;
        getPreferenceManager().getSharedPreferences().edit().clear().apply();
        getPreferenceManager().getSharedPreferences().edit().putBoolean("key_enabled",configuration.upload.enabled).apply();
        getPreferenceManager().getSharedPreferences().edit().putString("key_url",configuration.upload.url).apply();
        getPreferenceManager().getSharedPreferences().edit().putString("key_interval",String.valueOf(configuration.upload.interval)).apply();
        getPreferenceManager().getSharedPreferences().edit().putString("key_network_high_freq",configuration.upload.network_high_frequency).apply();
        getPreferenceManager().getSharedPreferences().edit().putString("key_network_low_freq",configuration.upload.network_low_frequency).apply();
        if(configuration.upload.restricted_datasource==null || configuration.upload.restricted_datasource.size()==0)
            getPreferenceManager().getSharedPreferences().edit().putBoolean("key_restrict_location",false).apply();
        else
            getPreferenceManager().getSharedPreferences().edit().putBoolean("key_restrict_location",true).apply();

        addPreferencesFromResource(R.xml.pref_settings_upload);
        setBackButton();
        setSaveButton();
    }
    @Override
    public void onResume(){
        setupPreferences();
        super.onResume();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=super.onCreateView(inflater, container,savedInstanceState);
        assert v != null;
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);

        return v;
    }
    private void setBackButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_1);
        button.setText("Close");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }
    void setupPreferences(){
        setupEnabled();
        setupInterval();
        setupNetworkHighFrequency();
        setupNetworkLowFrequency();
        setupURL();
        setupRestrictedDataSource();
    }
    void setupRestrictedDataSource(){
        final CheckBoxPreference checkBoxPreference= (CheckBoxPreference) findPreference("key_restrict_location");
        boolean enabled = getPreferenceManager().getSharedPreferences().getBoolean("key_restrict_location", false);
        checkBoxPreference.setChecked(enabled);
        checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = (Boolean)newValue;
                getPreferenceManager().getSharedPreferences().edit().putBoolean("key_restrict_location",enabled).apply();
                setupPreferences();
                return false;
            }
        });
    }
    void setupEnabled(){
        SwitchPreference switchPreference= (SwitchPreference) findPreference("key_enabled");
        boolean enabled = getPreferenceManager().getSharedPreferences().getBoolean("key_enabled", configuration.upload.enabled);
        switchPreference.setChecked(enabled);
    }
    void setupInterval() {
        ListPreference preference = (ListPreference) findPreference("key_interval");
        String interval = getPreferenceManager().getSharedPreferences().getString("key_interval", String.valueOf(configuration.upload.interval));
        preference.setValue(interval);
        preference.setSummary(findString(getResources().getStringArray(R.array.upload_interval_values), getResources().getStringArray(R.array.upload_interval_text), interval));
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getPreferenceManager().getSharedPreferences().edit().putString("key_interval", newValue.toString()).apply();
                setupPreferences();
                return false;
            }
        });
    }
    void setupNetworkHighFrequency() {
        ListPreference preference = (ListPreference) findPreference("key_network_high_freq");
        String network = getPreferenceManager().getSharedPreferences().getString("key_network_high_freq", network_type_value[0]);
        preference.setValue(network);
        preference.setEntries(network_type_show);
        preference.setEntryValues(network_type_value);
        preference.setSummary(getFreqShowFromValue(network));
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getPreferenceManager().getSharedPreferences().edit().putString("key_network_high_freq", newValue.toString()).apply();
                setupPreferences();
                return false;
            }
        });
    }
    void setupNetworkLowFrequency() {
        ListPreference preference = (ListPreference) findPreference("key_network_low_freq");
        String network = getPreferenceManager().getSharedPreferences().getString("key_network_low_freq", network_type_value[0]);
        preference.setValue(network);
        preference.setEntries(network_type_show);
        preference.setEntryValues(network_type_value);
        preference.setSummary(getFreqShowFromValue(network));
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getPreferenceManager().getSharedPreferences().edit().putString("key_network_low_freq", newValue.toString()).apply();
                setupPreferences();
                return false;
            }
        });
    }

    private String findString(String[] values, String[] strings, String value) {
        for (int i = 0; i < values.length; i++)
            if (values[i].equals(value))
                return strings[i];
        return ("(not selected)");
    }

    private void setSaveButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_2);
        button.setText("Save");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                configuration.upload.enabled = sharedPreferences.getBoolean("key_enabled", configuration.upload.enabled);
                configuration.upload.url = sharedPreferences.getString("key_url", configuration.upload.url);
                configuration.upload.interval = Long.parseLong(sharedPreferences.getString("key_interval", String.valueOf(configuration.upload.interval)));
                configuration.upload.network_high_frequency = sharedPreferences.getString("key_network_high_freq", configuration.upload.network_high_frequency);
                configuration.upload.network_low_frequency = sharedPreferences.getString("key_network_low_freq", configuration.upload.network_high_frequency);

                if (configuration.upload.enabled && (configuration.upload.url == null || configuration.upload.interval == 0)) {
                    Toast.makeText(getActivity(), "Not Saved...not all values are set properly", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!sharedPreferences.getBoolean("key_restrict_location", false))
                    configuration.upload.restricted_datasource.clear();
                else{
                    DataSource dataSource=new DataSourceBuilder().setType(DataSourceType.LOCATION).build();
                    configuration.upload.restricted_datasource= new ArrayList<>();
                    configuration.upload.restricted_datasource.add(dataSource);
                }
                ConfigurationManager.getInstance(getActivity()).write();
                Toast.makeText(getActivity(),"Saved...",Toast.LENGTH_LONG).show();

            }
        });
    }
    void setupURL() {
        EditTextPreference preference = (EditTextPreference) findPreference("key_url");
        String url = getPreferenceManager().getSharedPreferences().getString("key_url", configuration.upload.url);
        preference.setText(url);
        preference.setSummary(url);
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getPreferenceManager().getSharedPreferences().edit().putString("key_url", newValue.toString()).apply();
                setupPreferences();
                return false;
            }
        });
    }

}
