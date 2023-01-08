import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Класс для чтения файлов по переданной пользователем директории и дальнейшей обработки.
 */
public class Reader {
    private final String root;
    private final List<Edge> list;
    private List<String> resultList;

    Reader(String root) {
        this.root = root;
        this.list = new ArrayList<>();
    }

    protected void readAllFiles() {
        processFilesFromFolder(new File(root));
    }

    /*
        Рекурсивный метод для обхода всех директорий по заданному корню,
        Если попавшийся файл не является папкой, то считываем и добавляем зависимости.
     */
    private void processFilesFromFolder(File folder) {
        File[] folderEntries = folder.listFiles();
        if (folderEntries == null) {
            return;
        }
        for (File entry : folderEntries) {
            if (entry.isDirectory()) {
                processFilesFromFolder(entry);
            } else {
                readFile(entry);
            }
        }
    }

    /*
        Метод для считывания файла по указанному пути
     */
    private void readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            boolean flag = false;
            String line = reader.readLine();
            Pattern pattern = Pattern.compile("require '.+'");
            Matcher matcher;
            do {
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    flag = true;
                    String str = matcher.group();
                    list.add(new Edge(file.getPath(), root + Program.separator + str.substring(9, str.length() - 1)));
                }
                line = reader.readLine();
            }
            while (line != null);
            if (!flag) {
                list.add(new Edge(file.getPath(), null));
            }
        } catch (Exception e) {
            System.out.println("Произошла ошибка при чтении файла, возможно он занят другим ресурсом.\n");
        }
    }

    /*
        Уже на готовом массиве зависимостей строится корректная последовательность списка
     */
    public void computeResultList() {
        resultList = new ArrayList<>();
        for (var i : list) {
            if (i.src == null && i.dest == null) {
                continue;
            }
            if (i.dest == null) {
                resultList.add(i.src);
                deleteAllNodes(i);
                i.dest = null;
                i.src = null;
            }
        }
        list.removeIf(i -> i.dest == null && i.src == null);
        try {
            while (!list.isEmpty()) {
                checkItem();
            }
        } catch (HaveALoopException e) {
            System.out.println(e.getMessage());
        }

    }

    /*
        Вспомогательный метод для поиска циклов, в противном случае удаляется связка и добавляется в итоговый список, так пока не массив входных данных
        не станет пустым
     */
    private void checkItem() throws HaveALoopException {
        boolean globalFlag = false;
        if (list.size() == 1) {
            if (list.get(0).src.equals(list.get(0).dest)) {
                throw new HaveALoopException("Найдена циклическая зависимость, невозможно построить итоговый файл.");
            } else {
                resultList.add(list.get(0).dest);
                resultList.add(list.get(0).src);
            }
            list.clear();
            return;
        }
        for (var i : list) {
            if (isOutgoing(i.dest)) {
                resultList.add(i.dest);
                deleteAllNodes(i);
                globalFlag = true;
                break;
            }
        }
        list.removeIf(i -> i.dest == null && i.src == null);
        if (!globalFlag) {
            throw new HaveALoopException("Найдена циклическая зависимость, невозможно построить итоговый файл.");
        }
    }

    /*
        Вспомогательный метод для удаления всех зависимостей вершины в ориентированном графе
     */
    private void deleteAllNodes(Edge edge) {
        String current;
        if (edge.dest == null) {
            current = edge.src;
        } else {
            current = edge.dest;
        }
        for (var i : list) {
            if (i.src == null && i.dest == null) {
                continue;
            }
            if (i.dest != null && i.dest.equals(current)) {
                if (count(i.src) == 1) {
                    resultList.add(i.src);
                }
                i.dest = null;
                i.src = null;
            }
        }
    }

    /*
        Метод для проверки выходит ли значение к другой вершине из указанной, если нет, то по алгоритму она удаляется,
        потому что пока не найден цикл(либо его вообще нет)
     */
    private boolean isOutgoing(String value) {
        for (var i : list) {
            if (i.src.equals(value)) {
                return false;
            }
        }
        return true;
    }

    /*
        Вспомогательный метод для рассчета количества входящих и выходящих связей из вершины
     */
    private int count(String value) {
        int counter = 0;
        for (var i : list) {
            if (i.dest != null) {
                if (i.dest.equals(value) || i.src.equals(value)) {
                    ++counter;
                }
            }
        }
        return counter;
    }

    /*
        Обход по итоговому списку с отсортированными путями файлов для формирования итоговой строки
     */
    public String printFiles() {
        StringBuilder string = new StringBuilder();
        for (var i : resultList) {
            try (BufferedReader reader = new BufferedReader(new FileReader(i))) {
                String line;
                do {
                    line = reader.readLine();
                    if (line != null) {
                        string.append(line);
                        string.append("\n");
                    }
                }
                while (line != null);
            } catch (Exception e) {
                System.out.println("При попытке считать данные из файла: " + i);
                System.out.println("Возможно файл занят другим ресурсом.");
            }
        }
        return string.toString();
    }
}