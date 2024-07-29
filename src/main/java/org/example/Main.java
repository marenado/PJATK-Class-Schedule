package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        DatabaseHandler.initializeDatabase();

        try {
            processDate("2023-03-21");
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }
    }

    public static Tt processDate(String date) throws IOException, InterruptedException {
        String text = DatabaseHandler.getSchedule(date);
        if (text == null) {
            text = ScheduleHandler.getSchedule(date);
            DatabaseHandler.saveSchedule(date, text);
        }

        ArrayList<Td[]> table = parse(text);

        // For debugging or additional usage, keeping the CSV write method
        writeToCsv(date + ".csv", table);

        Tt tt = createTt(table, date);
        return tt;
    }

    private static ArrayList<Td[]> parse(String text) {
        String[] remain = new String[1];
        String s = text;

        String row1 = getBetween(s, "<tr id=\"BudynkiTableHeaderRow\">", "</tr>", remain);
        if (row1 == null) err();

        s = remain[0];
        String row2 = getBetween(s, "<tr id=\"SaleTableHeaderRow\">", "</tr>", remain);
        if (row2 == null) err();

        s = remain[0];
        String body = getBetween(s, "<tbody>", "</tbody>", remain);
        if (body == null) err();

        ArrayList<Td[]> table = new ArrayList<>();

        table.add(getAllTds(row1));
        table.add(getAllTds(row2));

        while (body.contains("<tr")) {
            row1 = getBetween(body, "<tr", "</tr>", remain);
            if (row1 == null) break;
            table.add(getAllTds(row1));
            body = remain[0];
        }

        return table;
    }

    private static String getCourseName(String arg) {
        if (arg.contains("<br />")) return arg.substring(0, arg.indexOf("<br />"));
        if (arg.contains("<br/>")) return arg.substring(0, arg.indexOf("<br/>"));
        return arg;
    }

    private static Tt createTt(ArrayList<Td[]> table, String date) {
        Tt tt = new Tt();
        tt.day = Integer.parseInt(date.substring(8, 10));
        tt.month = Integer.parseInt(date.substring(5, 7));
        tt.year = Integer.parseInt(date.substring(0, 4));

        Td[] tds = table.get(0);
        int wid = 0;
        for (Td td : tds) wid += td.colspan;

        boolean[][] top = new boolean[table.size()][wid];
        tt.buildings = new String[wid];
        tt.halls = new String[wid];

        for (int i = 0; i < 2; i++) {
            tds = table.get(i);
            int col = 0;
            for (Td td : tds) {
                for (int k = 0; k < td.colspan; k++) {
                    if (i == 0) tt.buildings[col++] = td.value;
                    else tt.halls[col++] = td.value;
                }
            }
        }

        tt.courses = new String[table.size() - 3][wid];
        tt.durations = new int[table.size() - 3][wid];

        for (int i = 3; i < table.size(); i++) {
            tds = table.get(i);
            int col = 0;
            for (Td td : tds) {
                for (int k = 1; k < td.rowspan; k++) top[i + k][col] = true;
                while (top[i][col]) col++;
                for (int k = 0; k < td.colspan; k++) {
                    while (top[i][col]) col++;
                    if (k == 0) {
                        String crs = getCourseName(td.value);
                        tt.courses[i - 3][col] = crs;
                        tt.durations[i - 3][col] = td.rowspan;
                    } else {
                        err();
                    }
                    col++;
                }
            }
        }

        // Adjust courses and durations arrays
        String[][] temp1 = new String[table.size() - 3][wid - 2];
        int[][] temp2 = new int[table.size() - 3][wid - 2];
        for (int i = 0; i < table.size() - 3; i++) {
            for (int j = 2; j < tds.length; j++) {
                temp1[i][j - 2] = tt.courses[i][j];
                temp2[i][j - 2] = tt.durations[i][j];
            }
        }
        tt.courses = temp1;
        tt.durations = temp2;

        return tt;
    }

    private static void writeToCsv(String csvPath, ArrayList<Td[]> table) throws FileNotFoundException {
        StringBuilder ret = new StringBuilder();
        int wid = 1000;
        boolean[][] top = new boolean[table.size()][wid];

        for (int i = 0; i < table.size(); i++) {
            Td[] tds = table.get(i);
            int col = 0;
            for (Td td : tds) {
                for (int k = 1; k < td.rowspan; k++) top[i + k][col] = true;
                while (top[i][col]) {
                    ret.append("**,");
                    col++;
                }
                for (int k = 0; k < td.colspan; k++) {
                    while (top[i][col]) {
                        ret.append("**,");
                        col++;
                    }
                    ret.append(k == 0 ? td.value + "," : "..,");
                    col++;
                }
            }
            ret.append("\n");
        }

        try (PrintWriter out = new PrintWriter(csvPath)) {
            out.println(ret.toString());
        }
    }

    private static String getBetween(String arg, String st, String end, String[] remain) {
        int index = arg.indexOf(st);
        if (index < 0) {
            remain[0] = arg;
            return null;
        }
        int index2 = arg.indexOf(end, index + st.length());
        if (index2 < 0) {
            remain[0] = arg;
            return null;
        }
        remain[0] = arg.substring(index2 + end.length());
        return arg.substring(index + st.length(), index2).trim();
    }

    private static Td[] getAllTds(String arg) {
        String s = arg;
        String[] remain = new String[1];
        ArrayList<Td> tdlist = new ArrayList<>();

        while (s.contains("<td")) {
            String tds = getBetween(s, "<td", "</td>", remain);
            if (tds == null) break;
            s = remain[0];
            int ind1 = tds.indexOf(">");
            String part1 = tds.substring(0, ind1);
            String part2 = tds.substring(ind1 + 1);
            String cls = getBetween(part1, "class=\"", "\"", remain);
            String rowspan = getBetween(part1, "rowspan=\"", "\"", remain);
            String colspan = getBetween(part1, "colspan=\"", "\"", remain);
            int rint = rowspan != null ? Integer.parseInt(rowspan) : 1;
            int cint = colspan != null ? Integer.parseInt(colspan) : 1;
            tdlist.add(new Td(part2, cls, rint, cint));
        }

        return tdlist.toArray(new Td[0]);
    }

    private static void err() {
        System.err.println("An error occurred.");
    }
}
