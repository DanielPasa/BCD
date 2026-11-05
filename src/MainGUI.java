import javax.swing.*;
import java.awt.*;
import java.util.Random;

// Пункт 3: Создание логики потока через реализацию интерфейса Runnable
class OddPositionsSumTask implements Runnable {
    private int[] mas;
    private int start, end, step;
    private JTextArea outputArea;
    private String threadName;

    public OddPositionsSumTask(String threadName, int[] mas, int start, int end, int step, JTextArea outputArea) {
        this.threadName = threadName;
        this.mas = mas;
        this.start = start;
        this.end = end;
        this.step = step;
        this.outputArea = outputArea;
    }

    @Override
    public void run() {
        int pos1 = -1;

        for (int i = start; i != end; i += step) {
            if (mas[i] % 2 != 0) { // Проверка на нечетность
                if (pos1 == -1) {
                    pos1 = i;
                } else {
                    int pos2 = i;
                    int sum = pos1 + pos2;

                    String result = threadName + ": Позиции " + pos1 + " и " + pos2 +
                            " (числа " + mas[pos1] + " и " + mas[pos2] +
                            "). Сумма позиций = " + sum + "\n";

                    // Безопасный вывод данных в графический интерфейс
                    SwingUtilities.invokeLater(() -> outputArea.append(result));

                    pos1 = -1; // Сброс для поиска следующей пары
                }
            }
        }
    }
}

// Пункт 4: Создание графического интерфейса (на базе Java Swing)
public class MainGUI extends JFrame {
    private JTextArea textArea;
    private JLabel studentLabel;
    private JButton startButton;

    public MainGUI() {
        setTitle("Лабораторная работа (Условие 1)");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Текстовая область для вывода массива и работы потока
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Метка для бегущей строки с данными студента
        studentLabel = new JLabel(" ");
        studentLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        studentLabel.setForeground(Color.BLUE);
        studentLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(studentLabel, BorderLayout.SOUTH);

        // Кнопка запуска
        startButton = new JButton("Сгенерировать массив и запустить Th1");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(startButton, BorderLayout.NORTH);

        // Обработчик нажатия на кнопку
        startButton.addActionListener(e -> startMultithreadingProcess());
    }

    private void startMultithreadingProcess() {
        startButton.setEnabled(false); // Отключаем кнопку на время работы
        textArea.setText("");
        studentLabel.setText(" ");

        // Генерация массива 1..100
        int[] mas = new int[100];
        Random rand = new Random();
        StringBuilder arrayStr = new StringBuilder("Сгенерированный массив:\n");
        for (int i = 0; i < mas.length; i++) {
            mas[i] = rand.nextInt(100) + 1;
            arrayStr.append(mas[i]).append(" ");
        }
        arrayStr.append("\n\nРезультаты работы потока:\n");
        textArea.append(arrayStr.toString());

        // Пункт 2: Создание и инициализация потоков (через Runnable)
        OddPositionsSumTask task1 = new OddPositionsSumTask("Th1", mas, 0, 100, 1, textArea);
        Thread th1 = new Thread(task1);

        // Фоновый поток, чтобы метод join() и sleep() не заморозили графический интерфейс
        Thread supervisorThread = new Thread(() -> {
            th1.start(); // Запуск потока Th1 (Условие 1)

            try {
                th1.join(); // Ждем завершения работы потока Th1

                // Анимация вывода информации о студенте с интервалом 100 мс
                String studentInfo = "Студент: Pasa Daniel, Группа: CR-243";
                StringBuilder currentText = new StringBuilder();

                for (char c : studentInfo.toCharArray()) {
                    currentText.append(c);
                    String textToShow = currentText.toString();

                    // Обновляем UI в главном потоке отрисовки
                    SwingUtilities.invokeLater(() -> studentLabel.setText(textToShow));
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            // Включаем кнопку обратно после завершения всех процессов
            SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
        });

        supervisorThread.start();
    }

    public static void main(String[] args) {
        // Запуск графического интерфейса
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setLocationRelativeTo(null); // Окно по центру экрана
            gui.setVisible(true);
        });
    }
}