package exception;

//重复秒杀异常（运行期异常）

public class RepeatKillException extends SeckillException{
    //右键》generate》Constructor，生成构造方法

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
