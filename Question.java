import java.io.Serializable;
import java.util.ArrayList;

class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private String text;
    private ArrayList<String> options;
    private int correctOptionIndex;

    Question(String text, ArrayList<String> options, int correctOptionIndex) {
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getText() {
        return text;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }
}
