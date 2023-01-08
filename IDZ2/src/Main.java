import java.io.File;
import java.util.*;

/*
    Класс для точки входа в программу
 */
class Program {
    public static String separator;

    /*
        Метод для приветствия программы и считывания данных от пользователя, повторный запуск после окончания не предусмотрен,
        не вижу смысла в этом просто
     */
    private static void Greetings() {
        String value;
        Scanner in = new Scanner(System.in);
        System.out.println("Здравствуйте!");
        System.out.println("Выберите систему на котором будет запускаться программа:");
        System.out.println("Windows - \\ при обращении к узлу директории");
        System.out.println("MacOS - / при обращении к узлу директории");
        System.out.println("Введите y - для настроек Windows,иначе MacOS.");
        value = in.nextLine();
        separator = Objects.equals(value, "y") ? "\\" : "/";
        System.out.println("Укажите полный путь к корневой директории, например: C:\\User\\directory");
        System.out.println("Если путь будет указан неверно, то программа выбросит ошибку.");
        File file;
        do {
            System.out.println("Ваш путь:");
            value = in.nextLine();
            file = new File(value);
        }
        while (!file.exists());
        Reader reader = new Reader(value);
        reader.readAllFiles();
        reader.computeResultList();
        System.out.println(reader.printFiles());
    }

    /*
        Точка входа в программу
     */
    public static void main(String[] args) {
        Greetings();
    }
}