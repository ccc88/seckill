package service.impl;

import dao.SeckillDao;
import dao.SuccessKilledDao;
import dao.cache.RedisDao;
import dto.Exposer;
import dto.SeckillExecution;
import entity.Seckill;
import entity.SuccessKilled;
import enums.SeckillStatEnum;
import exception.RepeatKillException;
import exception.SeckillCloseException;
import exception.SeckillException;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import service.SeckillService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component代表所有的组件，当你不知道一个类它是一个service或者dao或者controller时使用该注解
//@Service，当你知道一个类是service时，给它一个@Service注解；@Dao、@Controller同理
@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //在spring容器中获取该实例，注入到service中来(注入service依赖）
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5盐值字符串，用于混淆md5
    private final String salt = "jhfasgfkbcsdkfhkjslxb23hzkJKASG";//用于避免用户猜到结果(内容随便设，越复杂越好）

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
       /* 需要优化的点是这步的数据库操作，这一步的数据库访问通过主键访问，速度很快。
        但由于所有秒杀单都要调用暴露接口这个方法，所以要通过Redis缓存起来，降低数据库访问的压力
        优化点：缓存优化,超时的基础上维护一致性*/
        Seckill seckill = redisDao.getSeckill(seckillId);
        //1.访问Redis
        if(seckill == null){//缓存中没有
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null)//如果数据库也没有
                return new Exposer(false,seckillId);
            else {//如果数据库中存在
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if(nowTime.getTime() <startTime.getTime() || nowTime.getTime() > endTime.getTime())
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        //秒杀成功
        String md5 = getMd5(seckillId);//md5将任意字符串转换成特定长度的编码，不可逆
        return new Exposer(true,md5,seckillId);
    }

    //不希望被外部人员访问到
    private String getMd5(long seckillId){
        String base = seckillId+"/"+salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());//用spring的工具类来生成md5的二进制
        return md5;
    }

    @Transactional
/*
* 使用注解控制事务方法的优点：
* 1、开发团队达成一致约定：明确标注事务方法的编程风格
* 2.保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，如RPC/HTTP请求,只有操作数据库
* （或者将网络操作剥离到事务方法外部）
* 3、不是所有的方法都需要事务，如只有一条修改操作、只读操作
* */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillException {
        if(md5 == null || !md5.equals(getMd5(seckillId)))
            throw new SeckillException("Seckill data rewrite.");
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime = new Date();
        try{
            //减库存成功，记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一：seckillId，userPhone
            if(insertCount <= 0)
                //重复秒杀
                throw new RepeatKillException("seckill repeat");
            else {
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
                if(updateCount <= 0)
                    //没有更新到记录 rollback
                    throw new SeckillException("seckill is closed");
                else {
                    //秒杀成功 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch(SeckillCloseException e1) {
            throw e1;
        } catch(RepeatKillException e2) {
            throw e2;
        }catch (Exception e){//执行过程中，插入时可能会超时或数据库连接断了，这样的异常统一用总体的异常表示
            logger.error(e.getMessage(),e);
            //所有编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error"+e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5)  {
        if(md5 == null || !md5.equals(getMd5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        //执行完存储过程后，result被赋值
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map,"result",-2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,sk);
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            //出现异常
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }

    }
}
