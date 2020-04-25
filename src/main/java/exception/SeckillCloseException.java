package exception;

//秒杀关闭异常（秒杀完了，就不应该再执行秒杀）

public class SeckillCloseException extends SeckillException{
    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
