package enums;

public enum SeckillStatEnum {
    SUCCESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATA_REWRITE(-3,"数据篡改");//设置常量

    private int state;
    private String stateInfo;

    SeckillStatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }
    //写一个静态方法
    public static SeckillStatEnum stateOf(int index){//传进来的状态的值
        for(SeckillStatEnum state:values()){
            if(state.getState() == index)//迭代所有的类型，values()用于拿到所有类型
                return state;
        }
        return null;
    }
}
