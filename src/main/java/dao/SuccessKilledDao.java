package dao;

import entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

public interface SuccessKilledDao {
    //购买订单

    /*插入购买明细,可过滤重复（记录用户秒杀购买明细）
    * 返回值为更新的行数*/
    int insertSuccessKilled(@Param("seckillId") Long seckillId,@Param("userPhone") Long userPhone);

    //根据id查询SuccessKilled并携带秒杀产品对象实体Seckill
    SuccessKilled queryByIdWithSeckill(@Param("seckillId")Long seckillId,@Param("userPhone") Long userPhone);

}
