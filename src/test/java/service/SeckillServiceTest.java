package service;

import dto.Exposer;
import dto.SeckillExecution;
import entity.Seckill;
import exception.RepeatKillException;
import exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;
    @Test
    public void getSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() {
        Long id = 1000L;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void exportSeckillUrl() {
        long id = 1000L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer="+exposer);
    }

    /*@Test
    public void executeSeckill() {
        long id = 1000L;
        long phone = 13502222222L;
        String md5 = "2586cc81f4d90d4ace425955d5ad92ef";
        try {
            SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
            logger.info("result = {}", execution);
        }catch(RepeatKillException e){
            logger.error(e.getMessage());
        }catch(SeckillCloseException e){
            logger.error(e.getMessage());
        }
    }*/
    @Test
    public void executeSeckillProcedure(){
        long seckillId = 1000L;
        long phone = 13502222222L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId,phone,md5);
            logger.info(execution.getStateInfo());
        }


    }

    //测试代码完整逻辑，注意可重复执行
    @Test
    public void testSeckillLogic(){
        long id = 1001L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            //如果秒杀已经开始
            logger.info("exposer="+exposer);
            long phone = 13502222222L;
            String md5 = exposer.getMd5();
            try {
                //SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
                //logger.info("result = {}", execution);
            }catch(RepeatKillException e){
                logger.error(e.getMessage());
            }catch(SeckillCloseException e){
                logger.error(e.getMessage());
            }
        }else{
            //秒杀未开启
            logger.warn("exposer={}",exposer);
        }

    }
}