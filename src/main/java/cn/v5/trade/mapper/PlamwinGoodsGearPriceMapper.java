package cn.v5.trade.mapper;

import cn.v5.trade.bean.PlamwinGoodsGearPrice;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by fangliang on 16/8/31.
 */

public interface PlamwinGoodsGearPriceMapper {


    @Select(" select item_id , trade_platform , fee_type , gear , price , app_id from  plamwin_goods_gear_price " +
            " where item_id = #{itemID} and trade_platform = #{tradePlatform} and fee_type = #{feeType} ")

    @Results(
            value = {
                    @Result(column = "item_id", property = "itemID"),
                    @Result(column = "trade_platform", property = "tradePlatform"),
                    @Result(column = "fee_type", property = "feeType"),
                    @Result(column = "gear", property = "gear"),
                    @Result(column = "appID", property = "appID"),
                    @Result(column = "price", property = "price")
            }
    )
    PlamwinGoodsGearPrice get(@Param("itemID") String itemID, @Param("tradePlatform") int tradePlatform, @Param("feeType") int feeType);


    @Insert(" insert into plamwin_goods_gear_price ( item_id , trade_platform , fee_type , gear , price , app_id  ) " +
            " values (  #{pggp.itemID} ,   #{pggp.tradePlatform} ,  #{pggp.feeType} ,  #{pggp.gear} ,  #{pggp.price} ,  #{pggp.appID})  ")
    void create(@Param("pggp") PlamwinGoodsGearPrice plamwinGoodsGearPrice);



    @Select("<script> select item_id , trade_platform , fee_type , gear , price , app_id from  plamwin_goods_gear_price " +
            " where trade_platform = #{tradePlatform} and fee_type = #{feeType} and item_id in <foreach collection='itemList' item='itemID' open='(' close=')' separator=','>  #{itemID} </foreach> </script> ")
    @Results(
            value = {
                    @Result(column = "item_id", property = "itemID"),
                    @Result(column = "trade_platform", property = "tradePlatform"),
                    @Result(column = "fee_type", property = "feeType"),
                    @Result(column = "gear", property = "gear"),
                    @Result(column = "app_id", property = "appID"),
                    @Result(column = "price", property = "price")
            }
    )
    List<PlamwinGoodsGearPrice> find(@Param("tradePlatform") int tradePlatform, @Param("feeType") int feeType, @Param("itemList") List<String> itemList);


}
