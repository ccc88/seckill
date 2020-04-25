package dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {
    private  final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;//类似于数据库连接池的connectPool
    //拿到对象的字节码的属性方法，做一个模式，创建对象时schema根据这种模式赋予相应的值（序列化的本质）
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    //构造方法
    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);//jedisPool初始化
    }

    //通过Redis拿到Seckill对象
    public Seckill getSeckill(long seckillId){
        //Redis操作逻辑
        try{
            Jedis jedis = jedisPool.getResource();//拿到jedis对象
            try{
                String key = "seckill:"+seckillId;//Redis的命名
                //并没有实现内部序列化（很多人认为是Seckill implements Serializable，这个用的是jdk自己的序列化机制），用array Object altherputString把key写成二进制数就可以序列化
                //get到的是一个二进制数组byte[],因此要通过反序列化得到一个Seckill类型的Object
                //采用自定义序列化方式（用protostuff把pojo类型的对象（不能是String等类型）转化成字节数组）
                byte[] bytes = jedis.get(key.getBytes());
                if(bytes != null){//从缓存中获取到了
                    Seckill seckill = schema.newMessage();//空对象
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);//把数组传到空对象中
                    //seckill被反序列化
                    return seckill;
                    //这种序列化方法比原生的序列化方法更省空间，速度更快
                }
            }finally{
                jedis.close();//关闭连接池
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);

        }
        return null;
    }

    //Redis缓存没有Seckill时
    public String putSeckill(Seckill seckill){
        //set方法 拿到Object（seckill) 转化成byte[],发送给Redis（即序列化的过程）
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeout = 60*60;
                String result = jedis.setex(key.getBytes(),timeout,bytes);///超时缓存,缓存一小时
                return result;
            }finally{
                jedis.close();//关闭连接池
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);

        }
        return null;
    }

}
