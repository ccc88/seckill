package service;

/*业务接口：站在“使用者”的角度设计接口
* 三个方面去考虑：
* 方法定义粒度（要明确，比如执行秒杀接口《传入执行秒杀所需参数》）
*参数（简练直接）
* 返回类型（return 类型要友好/异常）
*/

import dto.Exposer;
import dto.SeckillExecution;
import entity.Seckill;
import exception.RepeatKillException;
import exception.SeckillException;

import java.util.List;

public interface SeckillService {
       //查询所有秒杀记录
    List<Seckill> getSeckillList();
    //查询一个秒杀记录
    Seckill getById(long seckillId);

    /*秒杀开启时，输出秒杀接口的地址；否则输出系统时间和秒杀时间
    * 秒杀未开始时，谁也猜不到秒杀接口地址*/
    Exposer exportSeckillUrl(long seckillId);

/*执行秒杀操作
* SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException,RepeatKillException,SeckillException;
*/
    /*执行秒杀操作 by存储过程
    参数md5来自上一接口返回的Exposer的属性
    传进去的md5和内部的md5生成规则比较，若md5变了说明用户的url被篡改，要拒绝执行秒杀
    */
    SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);


}
