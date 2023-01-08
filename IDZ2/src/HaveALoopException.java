/*
    Exception для отлавливания цикла в ориентированном графе
 */
public class HaveALoopException extends Exception {
    public HaveALoopException(String errorMessage) {
        super(errorMessage);
    }
}