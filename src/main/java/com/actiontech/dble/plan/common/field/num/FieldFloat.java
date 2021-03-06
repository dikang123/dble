package com.actiontech.dble.plan.common.field.num;

import com.actiontech.dble.plan.common.item.FieldTypes;

/**
 * bigint(%d) |unsigned |zerofilled
 *
 * @author ActionTech
 */
public class FieldFloat extends FieldReal {

    public FieldFloat(String name, String table, int charsetIndex, int fieldLength, int decimals, long flags) {
        super(name, table, charsetIndex, fieldLength, decimals, flags);
    }

    @Override
    public FieldTypes fieldType() {
        return FieldTypes.MYSQL_TYPE_FLOAT;
    }

}
