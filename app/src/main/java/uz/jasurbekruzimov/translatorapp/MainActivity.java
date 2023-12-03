package uz.jasurbekruzimov.translatorapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText sourceEdit;
    private TextView translatedTV;
    String[] fromLang = {"Russian", "English"};
    String[] toLang = {"English", "Russian"};
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    int fromLanguageCode, toLanguageCode = 0;
    Spinner fromSpinner, toSpinner;
    ImageView swipeLang;
    MaterialSwitch autoTranslate;
    Button translateBtn;

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdit = findViewById(R.id.idEditSource);
        ImageView micIV = findViewById(R.id.idIVMic);
        swipeLang = findViewById(R.id.idSwapLangs);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTVTranslatedTV);
        translatedTV.setTextIsSelectable(true);
        autoTranslate = findViewById(R.id.autoTranslate);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLang[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLang);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLang[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLang);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(v -> {
            translatedTV.setText("");
            if (Objects.requireNonNull(sourceEdit.getText()).toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
            } else if (fromLanguageCode == 0) {
                Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
            } else if (toLanguageCode == 0) {
                Toast.makeText(MainActivity.this, "Please select target language", Toast.LENGTH_SHORT).show();
            } else {
                translateText(fromLanguageCode, toLanguageCode, sourceEdit.getText().toString());
            }
        });

        micIV.setOnClickListener(v -> startSpeechToText());

        swipeLang.setOnClickListener(v -> {
            performRotateAnimation();
        });

        // Set the initial visibility of the translate button
        translateBtn.setVisibility(autoTranslate.isChecked() ? View.GONE : View.VISIBLE);

        // Add a TextWatcher to the sourceEdit
        sourceEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Auto-translate whenever the text changes if auto-translate is enabled
                if (charSequence.length() > 0 && autoTranslate.isChecked()) {
                    translateText(fromLanguageCode, toLanguageCode, charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Set a listener for autoTranslate switch changes
        autoTranslate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle the visibility of the translate button based on the auto-translate state
            translateBtn.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        // ... (your existing code)
    }

    private void startSpeechToText() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to translate");
        try {
            startActivityForResult(i, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String spokenText = Objects.requireNonNull(result).get(0);
                sourceEdit.setText(spokenText);
                // Auto-translate only if the auto-translate switch is on
                if (autoTranslate.isChecked()) {
                    translateText(fromLanguageCode, toLanguageCode, spokenText);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void translateText(int fromLanguageCode, int toLanguageCode, String sourceText) {
        translatedTV.setText("Loading...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(unused -> {
            translatedTV.setText("Translating...");
            translator.translate(sourceText).addOnSuccessListener(s -> translatedTV.setText(s)).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Fail to translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to download language Modal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
        }
        return languageCode;
    }

    private void performRotateAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(
                0,
                180,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateAnimation.setDuration(300);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateUIAfterAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        swipeLang.startAnimation(rotateAnimation);
    }

    private void updateUIAfterAnimation() {
        int tempPosition = fromSpinner.getSelectedItemPosition();
        fromSpinner.setSelection(toSpinner.getSelectedItemPosition());
        toSpinner.setSelection(tempPosition);

        int tempLanguageCode = fromLanguageCode;
        fromLanguageCode = toLanguageCode;
        toLanguageCode = tempLanguageCode;

        String tempLang = fromLang[fromSpinner.getSelectedItemPosition()];
        fromLang[fromSpinner.getSelectedItemPosition()] = toLang[toSpinner.getSelectedItemPosition()];
        toLang[toSpinner.getSelectedItemPosition()] = tempLang;

        sourceEdit.setText(translatedTV.getText());
        translatedTV.setText("");

        updateSpinnerAdapters();
    }

    private void updateSpinnerAdapters() {
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLang);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLang);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
    }
}
