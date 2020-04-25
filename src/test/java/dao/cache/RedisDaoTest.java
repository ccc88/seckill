package dao.cache;

import dao.SeckillDao;
import entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    Long id=1001L;//秒杀id
    @Autowired
    private RedisDao redisDao;//注入RedisDao
    @Autowired
    private SeckillDao seckillDao;//通过SeckillDao拿到秒杀对象，再把对象放到redis缓存中
    @Test
    public void testSeckill() throws Exception {
        //测试get put方法
        Seckill seckill = redisDao.getSeckill(id);//一开始是空对象
        if(seckill == null) {
            seckill = seckillDao.queryById(id);//对象是空的时候，从数据库取
            if(seckill != null){
                String result = redisDao.putSeckill(seckill);
                System.out.println(result);
                seckill = redisDao.getSeckill(id);//拿seckill对象
                System.out.println(seckill);
            }

        }
    }

}