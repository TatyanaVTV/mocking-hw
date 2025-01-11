package ru.productstar.translate;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;

public class MyTranslationService {
    private final Translate googleTranslate;

    public MyTranslationService(Translate googleTranslate) {
        this.googleTranslate = googleTranslate;
    }

    public String translateWithGoogle(String sentence, String targetLanguage) {
        if (!targetLanguage.equals("ru")) {
            throw new IllegalArgumentException("only translation to Russian is currently supported!");
        }

        try {
            Translation translation = googleTranslate.translate(sentence, Translate.TranslateOption.targetLanguage(targetLanguage));
            return translation.getTranslatedText();
        } catch (Exception ex) {
            throw new MyTranslationServiceException("Exception while calling Google Translate API", ex);
        }
    }
}