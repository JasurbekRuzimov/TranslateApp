package uz.jasurbekruzimov.translatorapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdit;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translatedTV;
    String[] fromLang = {"From", "English", "Russian", "Turkish", "Korean", "Japanese", "German", "French", "Italian", "Spanish"};
    String[] toLang = {"To", "English", "Russian", "Turkish", "Korean", "Japanese", "German", "French", "Italian", "Spanish"};

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    int languageCode, fromLanguageCode, toLanguageCode = 0;


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdit = findViewById(R.id.idEditSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTVTranslatedTV);

        fromSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLang[position]);
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLang);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLang[position]);
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLang);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(v -> {
            translatedTV.setText("Translated Text");
            if (Objects.requireNonNull(sourceEdit.getText()).toString().isEmpty()) {
                sourceEdit.setError("Please enter text to translate");
                return;
            } else if (fromLanguageCode == 0) {
                sourceEdit.setError("Please select source language");
                return;
            } else if (toLanguageCode == 0) {
                sourceEdit.setError("Please select target language");
                return;
            } else {
                translateText(fromLanguageCode, toLanguageCode, sourceEdit.getText().toString());
            }
        });

        micIV.setOnClickListener(v -> {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // RecognizerIntent.ACTION_RECOGNIZE_SPEECH is a constant
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); // en-US is a constant
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to translate");
            try {
                startActivityForResult(i, REQUEST_CODE_SPEECH_INPUT);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

//        @Override
//        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
//            super.onActivityResult(requestCode, resultCode, data);
//            if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
//                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                sourceEdit.setText(Objects.requireNonNull(result).get(0));
//            }
//        }

    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String sourceText) {
        translatedTV.setText("Loading...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FireNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(unused ->
                        translator.translate(sourceText)
                                .addOnSuccessListener(s ->
                                        translatedTV.setText(s))
                                .addOnFailureListener(e ->
                                        translatedTV.setText(e.getMessage())))
                .addOnFailureListener(e ->
                        translatedTV.setText(e.getMessage()));
    }

    private int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "Turkish":
                languageCode = FirebaseTranslateLanguage.TR;
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            default:
                languageCode = 0;
        }
        return languageCode;
    }
}