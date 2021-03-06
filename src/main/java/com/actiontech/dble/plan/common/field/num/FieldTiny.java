package com.actiontech.dble.plan.common.field.num;

import com.actiontech.dble.plan.common.item.FieldTypes;
import com.actiontech.dble.plan.common.item.Item;

/**
 * tinyint(%d) |unsigned | zerofilled
 *
 * @author ActionTech
 */
public class FieldTiny extends FieldNum {

    public FieldTiny(String name, String table, int charsetIndex, int fieldLength, int decimals, long flags) {
        super(name, table, charsetIndex, fieldLength, decimals, flags);
    }

    @Override
    public Item.ItemResult resultType() {
        return Item.ItemResult.INT_RESULT;
    }

    @Override
    public FieldTypes fieldType() {
        return FieldTypes.MYSQL_TYPE_TINY;
    }
}
