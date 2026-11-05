import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class ThreadGroupGUI extends JFrame {
    private JTextArea textArea;

    public ThreadGroupGUI() {
        // Настройка главного окна
        setTitle("Структура потоков - Вариант 10[cite: 1]");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Текстовое поле для вывода результатов
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Кнопка запуска
        JButton startButton = new JButton("Создать и запустить потоки");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        startButton.addActionListener(e -> runThreads());

        add(scrollPane, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        // Перенаправляем System.out в наше текстовое поле, чтобы стандартный 
        // вывод (включая метод list() у ThreadGroup) печатался в GUI
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
    }

    private void runThreads() {
        textArea.setText(""); // Очищаем поле перед новым запуском
        System.out.println("--- Инициализация структуры и запуск потоков ---");

        // Создаем корневую группу "Main" явно, чтобы изолировать её от системных потоков Swing
        ThreadGroup mainGroup = new ThreadGroup("Main");

        // --- Создание иерархии групп (согласно формуле) ---
        ThreadGroup g7 = new ThreadGroup(mainGroup, "G7");
        ThreadGroup g3 = new ThreadGroup(g7, "G3");
        ThreadGroup g2 = new ThreadGroup(mainGroup, "G2");

        // Снятие ограничений по приоритету для групп
        mainGroup.setMaxPriority(Thread.MAX_PRIORITY);
        g7.setMaxPriority(Thread.MAX_PRIORITY);
        g3.setMaxPriority(Thread.MAX_PRIORITY);
        g2.setMaxPriority(Thread.MAX_PRIORITY);

        // --- Создание потоков с передачей приоритетов ---
        // G3: Tha(6), Thb(3), Thc(6), Thd(3)
        Thread tha = new CustomThread(g3, "Tha", 6);
        Thread thb = new CustomThread(g3, "Thb", 3);
        Thread thc = new CustomThread(g3, "Thc", 6);
        Thread thd = new CustomThread(g3, "Thd", 3);

        // G7: ThA(7), ThB(6)
        Thread thA_G7 = new CustomThread(g7, "ThA", 7);
        Thread thB_G7 = new CustomThread(g7, "ThB", 6);

        // Main: Th2(3)
        Thread th2_main = new CustomThread(mainGroup, "Th2", 3);

        // G2: Th1(7), Th2(3), Th3(3)
        Thread th1_G2 = new CustomThread(g2, "Th1", 7);
        Thread th2_G2 = new CustomThread(g2, "Th2", 3);
        Thread th3_G2 = new CustomThread(g2, "Th3", 3);

        // --- Запуск потоков ---
        tha.start(); thb.start(); thc.start(); thd.start();
        thA_G7.start(); thB_G7.start(); th2_main.start();
        th1_G2.start(); th2_G2.start(); th3_G2.start();

        // Запускаем вывод дерева структуры с небольшой задержкой (Timer),
        // чтобы потоки успели отработать свои методы run() и вывести текст первыми
        Timer timer = new Timer(300, e -> {
            System.out.println("\n--- Структура дерева потоков (результат вызова list()) ---");
            mainGroup.list();
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        // Запуск графического интерфейса в специальном потоке диспетчеризации событий
        SwingUtilities.invokeLater(() -> {
            new ThreadGroupGUI().setVisible(true);
        });
    }
}

// --- Вспомогательные классы ---

// Пользовательский класс потока
class CustomThread extends Thread {
    public CustomThread(ThreadGroup group, String name, int priority) {
        super(group, name);
        setPriority(priority); // Устанавливаем приоритет при создании
    }

    @Override
    public void run() {
        System.out.println("Поток: " + getName() +
                ", Группа: " + getThreadGroup().getName() +
                ", Приоритет: " + getPriority());
    }
}

// Класс для перехвата консольного вывода (System.out) и направления его в JTextArea
class CustomOutputStream extends OutputStream {
    private JTextArea textArea;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        SwingUtilities.invokeLater(() -> textArea.append(String.valueOf((char) b)));
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String text = new String(b, off, len);
        // Выполняем обновление UI в потоке диспетчеризации событий
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            textArea.setCaretPosition(textArea.getDocument().getLength()); // Прокрутка вниз
        });
    }
}