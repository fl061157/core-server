package cn.v5.trade.mapper;

import cn.v5.trade.bean.PlamwinGoods;
import org.apache.ibatis.annotations.*;

/**
 * Created by fangliang on 16/8/31.
 */
public interface PlamwinGoodsMapper {


    @Select(" Select item_id , app_id , item_type , item_desc , gear  from  plamwin_goods where item_id = #{itemID}")
    @Results(
            value = {
                    @Result(column = "item_id", property = "itemID"),
                    @Result(column = "app_id", property = "appID"),
                    @Result(column = "item_type", property = "itemType"),
                    @Result(column = "item_desc", property = "itemDesc"),
                    @Result(column = "gear", property = "gear")
            }
    )
    PlamwinGoods get(@Param("itemID") String itemID);


    @Insert("Insert into plamwin_goods ( item_id , app_id , item_type , item_desc , gear  ) " +
            " values ( #{goods.itemID} , #{goods.appID} , #{goods.itemType} , #{goods.itemDesc} , #{goods.gear} )  ")
    void create(@Param("goods") PlamwinGoods goods);


}
