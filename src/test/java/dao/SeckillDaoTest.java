package dao;

import entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
/*配置spring和junit整合，使得junit启动时加载springIOC容器
* spring-test做spring测试的依赖，junit依赖
*/
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件的位置，加载springIOC容器时应用spring-dao.xml，用于验证mybatis、spring整合以及数据库连接池是否OK
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    //注入dao实现类依赖
    @Resource
    //看到@Resource时会在spring容器中查找SeckillDao这个类型的实现类注入到单元测试类里，当我们运行时这个类可以直接使用
    private SeckillDao seckilledDao;

    @Test
    public void reduceNumber() {
        Date killTime = new Date();
        int updateCount = seckilledDao.reduceNumber(1000L,killTime);
        System.out.println("updateCount="+updateCount);
    }

    @Test
    public void queryById() {
        long id = 1000;
        Seckill seckill = seckilledDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void queryAll() {
        //java没有保存形参的记录， List<Seckill> queryAll(int offset, int limit)->queryAll(arg0, arg1)
         List<Seckill> seckills = seckilledDao.queryAll(0,100);
         for(Seckill seckill:seckills){
             System.out.println(seckill);
         }
    }
}