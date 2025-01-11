package ru.productstar.translate;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MyTranslationServiceTest_TODO {

    @Mock
    private Translate googleTranslate;

    @Mock
    private Translation translation;

    private MyTranslationService service;

    private static final String SOME_SENTENCE = "Some sentence";
    private static final String SOME_SENTENCE_RU_TRANSLATION = "Какое-то предложение";
    private static final String VALID_TARGET_LANGUAGE = "ru";
    private static final String INVALID_TARGET_LANGUAGE = "es";

    private static final String SOME_SENTENCE_FOR_EXCEPTION = "Some sentence for exception";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        service = new MyTranslationService(googleTranslate);

        var validLanguage = getTranslateOptionForValidLanguage();
        lenient().when(googleTranslate.translate(eq(SOME_SENTENCE), eq(validLanguage))).thenReturn(translation);
        lenient().when(googleTranslate.translate(not(eq(SOME_SENTENCE)), eq(validLanguage))).thenThrow(new RuntimeException());

        lenient().when(translation.getTranslatedText()).thenReturn(SOME_SENTENCE_RU_TRANSLATION);
    }

    private Translate.TranslateOption getTranslateOptionForValidLanguage() {
        return Translate.TranslateOption.targetLanguage(VALID_TARGET_LANGUAGE);
    }

    /**
     * 1. Happy case test.
     * <p>
     * When `MyTranslationService::translateWithGoogle` method is called with any sentence and target language is equal to "ru",
     * `googleTranslate` dependency should be called and `translation.getTranslatedText()` returned.
     * No other interactions with `googleTranslate` dependency should be invoked apart from a single call to `googleTranslate.translate()`.
     */
    @Test
    void translateWithGoogle_anySentenceAndTargetLanguageIsRu_success() {
        var translationResult = service.translateWithGoogle(SOME_SENTENCE, VALID_TARGET_LANGUAGE);
        assertEquals(SOME_SENTENCE_RU_TRANSLATION, translationResult);

        InOrder inOrder = inOrder(googleTranslate, translation);
        inOrder.verify(googleTranslate, times(1))
                .translate(eq(SOME_SENTENCE), eq(getTranslateOptionForValidLanguage()));
        inOrder.verify(translation, times(1)).getTranslatedText();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * 2. Unhappy case test when target language is not supported.
     * <p>
     * When `MyTranslationService::translateWithGoogle` method is called with any sentence and target language is not equal to "ru",
     * `IllegalArgumentException` should be thrown. `googleTranslate` dependency should not be called at all.
     */
    @Test
    void translateWithGoogle_anySentenceAndTargetLanguageIsNotRu_failure() {
        assertThrows(IllegalArgumentException.class,
                () -> service.translateWithGoogle(SOME_SENTENCE, INVALID_TARGET_LANGUAGE));

        verify(googleTranslate, never()).translate(anyString(), any());
        verify(translation, never()).getTranslatedText();
    }

    /**
     * 3. Unhappy case test when Google Translate call throws exception.
     * <p>
     * When `MyTranslationService::translateWithGoogle` method is called with any sentence and target language is equal to "ru",
     * `googleTranslate` dependency should be called. When `googleTranslate` dependency throws exception, it should be
     * wrapped into `MyTranslationServiceException` and the latter should be thrown from our method.
     */
    @Test
    void translateWithGoogle_googleTranslateThrowsException_failure() {
        assertThrows(MyTranslationServiceException.class,
                () -> service.translateWithGoogle(SOME_SENTENCE_FOR_EXCEPTION, VALID_TARGET_LANGUAGE));

        InOrder inOrder = inOrder(googleTranslate, translation);
        inOrder.verify(googleTranslate, times(1))
                .translate( eq(SOME_SENTENCE_FOR_EXCEPTION), eq(getTranslateOptionForValidLanguage()) );
        inOrder.verify(translation, never()).getTranslatedText();
        inOrder.verifyNoMoreInteractions();
    }
}