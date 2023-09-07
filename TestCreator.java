import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class TestCreator {

    private ArrayList<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    public static void main(String[] args) {
        new TestCreator().displayMainMenu();
    }

    private void displayMainMenu() {
        JFrame mainFrame = createFrame("Test Creator", 300, 200, JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.add(createButton("Create Test", e -> createTest()));
        mainPanel.add(createButton("Run Test", e -> runTest()));
        mainPanel.add(createButton("Modify Test", e -> selectTestToModify()));

        mainFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    private void createTest() {
        JFrame createTestFrame = createFrame("Create Test", 450, 300, JFrame.DISPOSE_ON_CLOSE);

        JPanel createTestPanel = new JPanel();
        createTestPanel.setLayout(new BoxLayout(createTestPanel, BoxLayout.Y_AXIS));

        JTextField testNameField = addLabelAndTextField(createTestPanel, "Test Name:");
        JTextField questionTextField = addLabelAndTextField(createTestPanel, "Question Text:");

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        createTestPanel.add(optionsPanel);

        ArrayList<JTextField> optionFields = new ArrayList<>();
        createTestPanel.add(createButton("Add Option", e -> addOptionField(createTestFrame, optionsPanel, optionFields)));

        JTextField correctOptionIndexField = addLabelAndTextField(createTestPanel, "Correct Option Index (e.g., 1 for the first option):");

        createTestPanel.add(createButton("Add Question", e -> addQuestion(createTestFrame, testNameField, questionTextField, optionFields, correctOptionIndexField)));
        createTestPanel.add(createButton("Save Test", e -> saveTest(createTestFrame, testNameField)));

        createTestFrame.getContentPane().add(createTestPanel, BorderLayout.CENTER);
        createTestFrame.setVisible(true);
    }

    private void addOptionField(JFrame createTestFrame, JPanel optionsPanel, ArrayList<JTextField> optionFields) {
        Dimension currentSize = createTestFrame.getSize();

        JPanel optionPanel = new JPanel(new BorderLayout());
        JLabel optionLabel = new JLabel("Option " + (optionFields.size() + 1) + ": ");
        JTextField newOptionField = new JTextField();
        optionFields.add(newOptionField);

        optionPanel.add(optionLabel, BorderLayout.WEST);
        optionPanel.add(newOptionField, BorderLayout.CENTER);

        optionsPanel.add(optionPanel);
        createTestFrame.pack();
        createTestFrame.setSize(currentSize);
    }

    private void addQuestion(JFrame createTestFrame, JTextField testNameField, JTextField questionTextField, ArrayList<JTextField> optionFields, JTextField correctOptionIndexField) {
        try {
            ArrayList<String> options = new ArrayList<>();
            for (JTextField optionField : optionFields) {
                options.add(optionField.getText());
            }

            int correctOptionIndex = Integer.parseInt(correctOptionIndexField.getText()) - 1;
            questions.add(new Question(questionTextField.getText(), options, correctOptionIndex));

            questionTextField.setText("");
            correctOptionIndexField.setText("");
            for (JTextField optionField : optionFields) {
                optionField.setText("");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(createTestFrame, "Invalid input. Please ensure correct option index is a number.");
        }
    }

    private void saveTest(JFrame createTestFrame, JTextField testNameField) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(testNameField.getText() + ".ser"))) {
            out.writeObject(questions);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(createTestFrame, "Failed to save the test.");
        }

        questions.clear();
        JOptionPane.showMessageDialog(createTestFrame, "Test saved successfully.");
        createTestFrame.dispose();
    }

    private void runTest() {
        String testName = selectTest("Run Test");
        if (testName != null) {
            runSelectedTest(testName);
        }
    }

    private void runSelectedTest(String testName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(testName + ".ser"))) {
            questions = (ArrayList<Question>) in.readObject();
            currentQuestionIndex = 0;
            score = 0;
            showNextQuestion();
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Failed to load the test.");
        }
    }

    private void showNextQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            JFrame questionFrame = createFrame("Question " + (currentQuestionIndex + 1), 400, 300, JFrame.EXIT_ON_CLOSE);
            JPanel questionPanel = new JPanel();
            questionPanel.setLayout(new BorderLayout());

            JLabel questionLabel = new JLabel(currentQuestion.getText(), SwingConstants.CENTER);
            questionPanel.add(questionLabel, BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel();
            optionsPanel.setLayout(new GridLayout(currentQuestion.getOptions().size(), 1));
            for (int i = 0; i < currentQuestion.getOptions().size(); i++) {
                int optionIndex = i;
                JButton optionButton = createButton(currentQuestion.getOptions().get(i), e -> {
                    if (optionIndex == currentQuestion.getCorrectOptionIndex()) {
                        score++;
                    }
                    currentQuestionIndex++;
                    questionFrame.dispose();
                    showNextQuestion();
                });
                optionsPanel.add(optionButton);
            }

            questionPanel.add(optionsPanel, BorderLayout.CENTER);
            questionFrame.getContentPane().add(questionPanel);
            questionFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Test completed. Your score is: " + score);
        }
    }

    private void selectTestToModify() {
        String testName = selectTest("Modify Test");
        if (testName != null) {
            listQuestionsToModify(testName);
        }
    }

    private String selectTest(String title) {
        File folder = new File(".");
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".ser"));
        ArrayList<String> testNames = new ArrayList<>();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                testNames.add(file.getName().replace(".ser", ""));
            }
        }
        String[] testArray = new String[testNames.size()];
        testNames.toArray(testArray);
        return (String) JOptionPane.showInputDialog(
                null,
                "Select a test",
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                testArray,
                testArray[0]
        );
    }


    private void listQuestionsToModify(String testName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(testName + ".ser"))) {
            ArrayList<Question> testQuestions = (ArrayList<Question>) in.readObject();
            JFrame modifyTestFrame = createFrame("Modify Test - " + testName, 400, 300, JFrame.DISPOSE_ON_CLOSE);

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BorderLayout());

            DefaultListModel<String> listModel = new DefaultListModel<>();
            for (Question question : testQuestions) {
                listModel.addElement(question.getText());
            }

            JList<String> questionsList = new JList<>(listModel);
            listPanel.add(new JScrollPane(questionsList), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            listPanel.add(buttonPanel, BorderLayout.SOUTH);

            JButton editButton = new JButton("Edit Question");
            editButton.addActionListener(e -> editQuestion(modifyTestFrame, testName, testQuestions, questionsList.getSelectedIndex()));
            buttonPanel.add(editButton);

            JButton deleteButton = new JButton("Delete Question");
            deleteButton.addActionListener(e -> deleteQuestion(testName, testQuestions, questionsList.getSelectedIndex()));
            buttonPanel.add(deleteButton);

            modifyTestFrame.getContentPane().add(listPanel, BorderLayout.CENTER);
            modifyTestFrame.setVisible(true);
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Failed to load the test.");
        }
    }

    private void editQuestion(JFrame parentFrame, String testName, ArrayList<Question> testQuestions, int selectedIndex) {
        if (selectedIndex >= 0) {
            Question questionToEdit = testQuestions.get(selectedIndex);

            JFrame editFrame = createFrame("Edit Question", 450, 300, JFrame.DISPOSE_ON_CLOSE);
            JPanel editPanel = new JPanel();
            editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
            JTextField questionTextField = addLabelAndTextField(editPanel, "Question Text:");
            questionTextField.setText(questionToEdit.getText());

            JPanel optionsPanel = new JPanel();
            optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
            ArrayList<JTextField> optionFields = new ArrayList<>();
            for (int i = 0; i < questionToEdit.getOptions().size(); i++) {
                JTextField optionField = addLabelAndTextField(optionsPanel, "Option " + (i + 1) + ": ");
                optionField.setText(questionToEdit.getOptions().get(i));
                optionFields.add(optionField);
            }
            editPanel.add(optionsPanel);

            JTextField correctOptionIndexField = addLabelAndTextField(editPanel, "Correct Option Index (e.g., 1 for the first option):");
            correctOptionIndexField.setText(String.valueOf(questionToEdit.getCorrectOptionIndex() + 1));

            editPanel.add(createButton("Save Changes", e -> {
                try {
                    ArrayList<String> options = new ArrayList<>();
                    for (JTextField optionField : optionFields) {
                        options.add(optionField.getText());
                    }

                    int correctOptionIndex = Integer.parseInt(correctOptionIndexField.getText()) - 1;
                    testQuestions.set(selectedIndex, new Question(questionTextField.getText(), options, correctOptionIndex));
                    saveModifiedTest(testName, testQuestions);
                    JOptionPane.showMessageDialog(editFrame, "Question updated successfully.");
                    editFrame.dispose();
                    parentFrame.dispose();
                    listQuestionsToModify(testName);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editFrame, "Invalid input. Please ensure correct option index is a number.");
                }
            }));

            editFrame.getContentPane().add(editPanel, BorderLayout.CENTER);
            editFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(parentFrame, "No question selected.");
        }
    }

    private void deleteQuestion(String testName, ArrayList<Question> testQuestions, int selectedIndex) {
        if (selectedIndex >= 0) {
            testQuestions.remove(selectedIndex);
            saveModifiedTest(testName, testQuestions);
            JOptionPane.showMessageDialog(null, "Question deleted successfully.");
            listQuestionsToModify(testName);
        } else {
            JOptionPane.showMessageDialog(null, "No question selected.");
        }
    }

    private void saveModifiedTest(String testName, ArrayList<Question> testQuestions) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(testName + ".ser"))) {
            out.writeObject(testQuestions);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to save the modified test.");
        }
    }


    private JFrame createFrame(String title, int width, int height, int closeOperation) {
        JFrame frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(closeOperation);
        return frame;
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    private JTextField addLabelAndTextField(JPanel panel, String labelText) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(new JLabel(labelText), BorderLayout.WEST);
        JTextField textField = new JTextField();
        fieldPanel.add(textField, BorderLayout.CENTER);
        panel.add(fieldPanel);
        return textField;
    }
}



