package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeTableApp extends JFrame implements ActionListener {
    private JLabel labelQuestion;
    private JTextField startDate;
    private JTextField endDate;
    private JButton buttonLoad;
    private JButton buttonShowTable;
    private JButton display;
    JPanel panel1;
    JScrollPane scrollpane1;
    JPanel panel2;
    JScrollPane scrollpane2;
    Tt[] tts;

    private final ArrayList<JCheckBox> checkBoxes = new ArrayList<>();

    public TimeTableApp() {
        super("Time Table");

        initComponents();

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        labelQuestion = new JLabel("Enter start and end dates (inclusive)");
        startDate = new JTextField(10);
        endDate = new JTextField(10);
        buttonLoad = new JButton("Load Time Table");
        buttonShowTable = new JButton("Show Time Table");

        setLayout(null);
        add(labelQuestion);
        add(startDate);
        add(endDate);
        add(buttonLoad);
        add(buttonShowTable);

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayString = today.format(formatter);

        startDate.setText(todayString);
        endDate.setText(todayString);

        labelQuestion.setBounds(50, 20, 300, 30);
        startDate.setBounds(350, 20, 100, 30);
        endDate.setBounds(470, 20, 100, 30);
        buttonLoad.setBounds(650, 20, 150, 30);
        buttonShowTable.setBounds(650, 230, 150, 30);

        panel1 = new JPanel();
        panel1.setBounds(50, 120, 700, 100);
        scrollpane1 = new JScrollPane(panel1);
        scrollpane1.setBounds(50, 120, 700, 100);
        scrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollpane1);

        panel2 = new JPanel();
        panel2.setBounds(50, 420, 700, 300);
        add(panel2);

        buttonLoad.addActionListener(this);
        buttonShowTable.addActionListener(this);
    }

    private static int[] getDMY(String date) {
        int day = Integer.parseInt(date.substring(8, 10));
        int month = Integer.parseInt(date.substring(5, 7));
        int year = Integer.parseInt(date.substring(0, 4));
        return new int[]{day, month, year};
    }

    private String prefixZero(int val, int count) {
        String ret = val + "";
        while (ret.length() < count)
            ret = "0" + ret;
        return ret;
    }

    private String dateToStr(LocalDate d) {
        return prefixZero(d.getYear(), 4) + "-" + prefixZero(d.getMonthValue(), 2) + "-" + prefixZero(d.getDayOfMonth(), 2);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == buttonLoad) {
            handleLoadButtonPress();
        }
        if (event.getSource() == buttonShowTable) {
            handleShowTable();
        }
    }

    private void handleShowTable() {
        HashSet<String> crs = new HashSet<>();
        for (JCheckBox chk : this.checkBoxes) {
            if (chk.isSelected())
                crs.add(chk.getText());
        }
        int colcnt = 0;
        List<String> headers = new ArrayList<>();
        for (Tt t : tts) {
            boolean[] yesno = new boolean[t.courses[0].length];
            for (int i = 0; i < t.courses.length; i++) {
                for (int j = 0; j < t.courses[i].length; j++)
                    if (crs.contains(t.courses[i][j]))
                        yesno[j] = true;
            }
            for (int i = 0; i < yesno.length; i++) {
                if (yesno[i]) {
                    colcnt++;
                    headers.add(t.halls[i + 2] + " (" + t.buildings[i + 2] + ")" + t.year + "-" + t.month + "-" + t.day);
                }
            }
        }
        int rowcnt = tts[0].courses.length;
        String[] header = new String[headers.size() + 1];
        int cnt = 1;
        header[0] = "";
        for (String s : headers)
            header[cnt++] = s;

        String[][] rec = new String[rowcnt][colcnt];
        int currcol = 0;
        for (Tt t : tts) {
            boolean[] yesno = new boolean[t.courses[0].length];
            for (int i = 0; i < t.courses.length; i++) {
                for (int j = 0; j < t.courses[i].length; j++)
                    if (crs.contains(t.courses[i][j]))
                        yesno[j] = true;
            }
            for (int i = 0; i < yesno.length; i++) {
                if (!yesno[i])
                    continue;

                for (int j = 0; j < rowcnt; j++) {
                    String cr = t.courses[j][i];
                    if (cr == null || cr.length() == 0)
                        continue;
                    if (!crs.contains(cr))
                        continue;
                    rec[j][currcol] = cr + "(" + (t.durations[j][i] * 15) + " min)";
                }
                currcol++;
            }
        }

        String[][] temp = new String[rowcnt + 1][colcnt + 1];
        for (int i = 0; i < rowcnt + 1; i++) {
            for (int j = 0; j < colcnt + 1; j++) {
                if (i == 0) {
                    if (j == 0)
                        temp[i][j] = "";
                    else
                        temp[i][j] = header[j];
                } else {
                    if (j == 0) {
                        temp[i][j] = getTime(i - 1);
                    } else {
                        temp[i][j] = rec[i - 1][j - 1];
                    }
                }
            }
        }

        rec = temp;

        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Time Table", TitledBorder.CENTER, TitledBorder.TOP));

        JTable table = new JTable(rec, header);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(Color.orange);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        scrollpane2 = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollpane2.setBounds(10, 230, 600, 300);

        panel2.removeAll();
        panel2.add(scrollpane2);
        panel2.revalidate();
        panel2.repaint();
    }

    private String getTime(int i) {
        int h = 7 + (i / 4);
        int m = (i % 4) * 15;
        return prefixZero(h, 2) + ":" + prefixZero(m, 2);
    }

    private void handleLoadButtonPress() {
        String d1 = startDate.getText();
        String d2 = endDate.getText();

        LocalDate start = LocalDate.parse(d1);
        LocalDate end = LocalDate.parse(d2);

        long days = TimeUnit.DAYS.convert(end.toEpochDay() - start.toEpochDay(), TimeUnit.DAYS);

        if (days > 7) {
            JOptionPane.showMessageDialog(this, "Please select two dates within 7 days range.");
            return;
        }

        tts = new Tt[(int) days + 1];
        for (int i = 0; i <= days; i++) {
            LocalDate date = start.plusDays(i);
            String datestr = dateToStr(date);
            try {
                tts[i] = Main.processDate(datestr);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        HashSet<String> courses = new HashSet<>();
        for (Tt tt : tts) {
            String[][] crs = tt.courses;
            for (String[] row : crs) {
                for (String s : row) {
                    if (s == null || s.length() == 0)
                        continue;
                    courses.add(s);
                }
            }
        }

        String[] carr = courses.toArray(new String[0]);
        Arrays.sort(carr);

        int chksize = this.checkBoxes.size();
        for (int i = 0; i < carr.length - chksize; i++)
            this.checkBoxes.add(new JCheckBox());

        for (int i = 0; i < carr.length; i++) {
            this.checkBoxes.get(i).setText(carr[i]);
        }

        panel1.removeAll();
        for (int i = 0; i < this.checkBoxes.size(); i++) {
            JCheckBox chk = this.checkBoxes.get(i);
            this.panel1.add(chk);
            chk.setBounds((i % 7) * 100 + 50, (i / 7) * 50 + 100, 100, 30);
        }
        panel1.revalidate();
        panel1.repaint();

        JOptionPane.showMessageDialog(this, "Please select courses to view the time table.");
    }

    public static void main(String[] args) {
        new TimeTableApp().setVisible(true);
    }
}
