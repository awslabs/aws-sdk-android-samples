package com.amazonaws.demo.polly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amazonaws.services.polly.model.Voice;

import java.util.List;

public class SpinnerVoiceAdapter extends BaseAdapter {
    private LayoutInflater inflater;

    private List<Voice> voices;

    SpinnerVoiceAdapter(Context ctx, List<Voice> voices) {
        this.inflater = LayoutInflater.from(ctx);
        this.voices = voices;
    }

    public void setVoices(List<Voice> voices) {
        this.voices = voices;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return voices.size();
    }

    @Override
    public Object getItem(int position) {
        return voices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.voice_spinner_row, parent, false);
        }
        Voice voice = voices.get(position);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.voiceName);
        nameTextView.setText(voice.getName());

        TextView languageCodeTextView = (TextView) convertView.findViewById(R.id.voiceLanguageCode);
        languageCodeTextView.setText(voice.getLanguageName() +
                " (" + voice.getLanguageCode() + ")");

        return convertView;
    }
}
