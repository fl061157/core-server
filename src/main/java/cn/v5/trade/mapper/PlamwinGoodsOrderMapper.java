package cn.v5.trade.mapper;

import cn.v5.trade.bean.PlamwinGoodsOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by fangliang on 16/8/31.
 */
public interface PlamwinGoodsOrderMapper {


    @Insert("Insert into  plamwin_goods_order ( trade_no , user_id , item_id , " +
            "item_size , item_price ,  trade_status , trade_time_start , trade_time_expire , create_ip , device_type , app_id , fee_type , trade_platform ) " +
            "values ( #{order.tradeNo} , #{order.userID} , #{order.itemID} , #{order.itemSize} , #{order.itemPrice} ,  #{order.tradeStatus} , " +
            "#{order.tradeTimeStart} , #{order.tradeTimeExpire} , #{order.createIP} , #{order.deviceType} , #{order.appID} , #{order.feeType} , #{order.tradePlatform}  )  ")
    void create(@Param("order") PlamwinGoodsOrder order);


    @Update("Update plamwin_goods_order set trade_status = #{ts} , trade_time_expire = #{tte}  where  trade_no = #{tn}")
    void update(@Param("tn") String tradeNo, @Param("ts") int tradeStatus, @Param("tte") long tradeTimeExpire);


    @Select("Select trade_no , user_id , item_id , item_size , item_price ,  trade_status , trade_time_start , trade_time_expire , create_ip , device_type , app_id , fee_type , trade_platform  " +
            "from plamwin_goods_order where trade_no = #{tradeNo}  ")
    @Results(
            value = {
                    @Result(column = "trade_no", property = "tradeNo"),
                    @Result(column = "user_id", property = "userID"),
                    @Result(column = "item_id", property = "itemID"),
                    @Result(column = "item_size", property = "itemSize"),
                    @Result(column = "item_price", property = "itemPrice"),
                    @Result(column = "trade_status", property = "tradeStatus"),
                    @Result(column = "trade_time_start", property = "tradeTimeStart"),
                    @Result(column = "trade_time_expire", property = "tradeTimeExpire"),
                    @Result(column = "create_ip", property = "createIP"),
                    @Result(column = "device_type", property = "deviceType"),
                    @Result(column = "app_id", property = "appID"),
                    @Result(column = "fee_type", property = "feeType"),
                    @Result(column = "trade_platform", property = "tradePlatform")
            }
    )
    PlamwinGoodsOrder get(@Param("tradeNo") String tradeNo);


    @Select("<script>Select trade_no , user_id , item_id , item_size , item_price ,  trade_status , trade_time_start , trade_time_expire , create_ip , device_type , app_id , fee_type , trade_platform  " +
            "        from plamwin_goods_order  where user_id = #{userID} and item_id in <foreach collection='itemList' item='itemID' open='(' close=')' separator=','>  " +
            "        #{itemID} </foreach>  and trade_status = #{tradeStatus}   </script>")
    @Results(
            value = {
                    @Result(column = "trade_no", property = "tradeNo"),
                    @Result(column = "user_id", property = "userID"),
                    @Result(column = "item_id", property = "itemID"),
                    @Result(column = "item_size", property = "itemSize"),
                    @Result(column = "item_price", property = "itemPrice"),
                    @Result(column = "trade_status", property = "tradeStatus"),
                    @Result(column = "trade_time_start", property = "tradeTimeStart"),
                    @Result(column = "trade_time_expire", property = "tradeTimeExpire"),
                    @Result(column = "create_ip", property = "createIP"),
                    @Result(column = "device_type", property = "deviceType"),
                    @Result(column = "app_id", property = "appID"),
                    @Result(column = "fee_type", property = "feeType"),
                    @Result(column = "trade_platform", property = "tradePlatform" )
            }
    )
    List<PlamwinGoodsOrder> find(@Param("userID") String userID, @Param("itemList") List<String> itemList, @Param("tradeStatus") int tradeStatus);


}
