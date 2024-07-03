package info.kgeorgiy.ja.shchetinin.i18n;

import java.util.ResourceBundle;

/**
 * A StringBuilder-like class that supports appending strings and their translations.
 * This class provides methods to append strings directly or to append their translations
 * based on a provided {@link ResourceBundle}.
 */
public class TranslateStringBuilder {
    private final StringBuilder builder;
    private final ResourceBundle bundle;

    /**
     * Constructs a new TranslateStringBuilder with the specified ResourceBundle.
     *
     * @param bundle the ResourceBundle used for translations
     */
    public TranslateStringBuilder(ResourceBundle bundle) {
        builder = new StringBuilder();
        this.bundle = bundle;
    }

    /**
     * Appends the specified string to this TranslateStringBuilder.
     *
     * @param s the string to append
     * @return this TranslateStringBuilder instance
     */
    public TranslateStringBuilder append(String s) {
        builder.append(s);
        return this;
    }

    /**
     * Appends the translation of the specified string to this TranslateStringBuilder.
     * If the string is not found in the ResourceBundle, the original string is appended.
     *
     * @param s the string to translate and append
     * @return this TranslateStringBuilder instance
     */
    public TranslateStringBuilder appendTranslate(String s) {
        if (!bundle.containsKey(s)) {
            builder.append(s);
        } else {
            builder.append(bundle.getString(s));
        }
        return this;
    }

    /**
     * Appends the string representation of the specified object to this TranslateStringBuilder.
     *
     * @param s the object whose string representation to append
     * @return this TranslateStringBuilder instance
     */
    public TranslateStringBuilder append(Object s) {
        builder.append(s.toString());
        return this;
    }

    /**
     * Returns a string representation of the TranslateStringBuilder.
     *
     * @return a string representation of the TranslateStringBuilder
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}
