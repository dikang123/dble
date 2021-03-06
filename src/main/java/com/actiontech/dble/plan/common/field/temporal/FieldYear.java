package com.actiontech.dble.plan.common.field.temporal;

import com.actiontech.dble.plan.common.field.num.FieldTiny;
import com.actiontech.dble.plan.common.item.FieldTypes;

/**
 * @author ActionTech
 */
public class FieldYear extends FieldTiny {

    public FieldYear(String name, String table, int charsetIndex, int fieldLength, int decimals, long flags) {
        super(name, table, charsetIndex, fieldLength, decimals, flags);
    }

    @Override
    public FieldTypes fieldType() {
        return FieldTypes.MYSQL_TYPE_YEAR;
    }

}
