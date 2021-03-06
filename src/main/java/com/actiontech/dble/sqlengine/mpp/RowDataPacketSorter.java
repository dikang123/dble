/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package com.actiontech.dble.sqlengine.mpp;

import com.actiontech.dble.memory.unsafe.utils.BytesTools;
import com.actiontech.dble.net.mysql.RowDataPacket;
import com.actiontech.dble.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RowDataPacketSorter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowDataPacketSorter.class);
    protected final OrderCol[] orderCols;

    private Collection<RowDataPacket> sorted = new ConcurrentLinkedQueue<>();
    private RowDataPacket[] array, resultTemp;
    private int p1, pr, p2;

    public RowDataPacketSorter(OrderCol[] orderCols) {
        super();
        this.orderCols = orderCols;
    }

    public boolean addRow(RowDataPacket row) {
        return this.sorted.add(row);

    }

    public Collection<RowDataPacket> getSortedResult() {
        try {
            this.mergeSort(sorted.toArray(new RowDataPacket[sorted.size()]));
        } catch (Exception e) {
            LOGGER.error("getSortedResultError", e);
        }
        if (array != null) {
            Collections.addAll(this.sorted, array);
        }

        return sorted;
    }

    private RowDataPacket[] mergeSort(RowDataPacket[] result) throws Exception {
        this.sorted.clear();
        array = result;
        if (result == null || result.length < 2 || this.orderCols == null || orderCols.length < 1) {
            return result;
        }
        mergeR(0, result.length - 1);

        return array;
    }

    private void mergeR(int startIndex, int endIndex) {
        if (startIndex < endIndex) {
            int mid = (startIndex + endIndex) / 2;

            mergeR(startIndex, mid);

            mergeR(mid + 1, endIndex);

            merge(startIndex, mid, endIndex);
        }
    }

    private void merge(int startIndex, int midIndex, int endIndex) {
        resultTemp = new RowDataPacket[(endIndex - startIndex + 1)];

        pr = 0;
        p1 = startIndex;
        p2 = midIndex + 1;
        while (p1 <= midIndex || p2 <= endIndex) {
            if (p1 == midIndex + 1) {
                while (p2 <= endIndex) {
                    resultTemp[pr++] = array[p2++];

                }
            } else if (p2 == endIndex + 1) {
                while (p1 <= midIndex) {
                    resultTemp[pr++] = array[p1++];
                }

            } else {
                compare(0);
            }
        }
        for (p1 = startIndex, p2 = 0; p1 <= endIndex; p1++, p2++) {
            array[p1] = resultTemp[p2];

        }
    }

    /**
     * @param byColumnIndex
     */
    private void compare(int byColumnIndex) {

        if (byColumnIndex == this.orderCols.length) {
            if (this.orderCols[byColumnIndex - 1].orderType == OrderCol.COL_ORDER_TYPE_ASC) {

                resultTemp[pr++] = array[p1++];
            } else {
                resultTemp[pr++] = array[p2++];
            }
            return;
        }

        byte[] left = array[p1].fieldValues.get(this.orderCols[byColumnIndex].colMeta.getColIndex());
        byte[] right = array[p2].fieldValues.get(this.orderCols[byColumnIndex].colMeta.getColIndex());

        if (compareObject(left, right, this.orderCols[byColumnIndex]) <= 0) {
            if (compareObject(left, right, this.orderCols[byColumnIndex]) < 0) {
                if (this.orderCols[byColumnIndex].orderType == OrderCol.COL_ORDER_TYPE_ASC) {
                    resultTemp[pr++] = array[p1++];
                } else {
                    resultTemp[pr++] = array[p2++];
                }
            } else { // if this field is equal, try next
                compare(byColumnIndex + 1);

            }

        } else {
            if (this.orderCols[byColumnIndex].orderType == OrderCol.COL_ORDER_TYPE_ASC) {
                resultTemp[pr++] = array[p2++];
            } else {
                resultTemp[pr++] = array[p1++];
            }

        }
    }

    public static int compareObject(Object l, Object r, OrderCol orderCol) {
        return compareObject((byte[]) l, (byte[]) r, orderCol);
    }

    public static int compareObject(byte[] left, byte[] right, OrderCol orderCol) {
        int colType = orderCol.getColMeta().getColType();
        switch (colType) {
            case ColMeta.COL_TYPE_DECIMAL:
            case ColMeta.COL_TYPE_INT:
            case ColMeta.COL_TYPE_SHORT:
            case ColMeta.COL_TYPE_LONG:
            case ColMeta.COL_TYPE_FLOAT:
            case ColMeta.COL_TYPE_DOUBLE:
            case ColMeta.COL_TYPE_LONGLONG:
            case ColMeta.COL_TYPE_INT24:
            case ColMeta.COL_TYPE_NEWDECIMAL:
                // treat date type as number
            case ColMeta.COL_TYPE_DATE:
            case ColMeta.COL_TYPE_TIMSTAMP:
            case ColMeta.COL_TYPE_TIME:
            case ColMeta.COL_TYPE_YEAR:
            case ColMeta.COL_TYPE_DATETIME:
            case ColMeta.COL_TYPE_NEWDATE:
            case ColMeta.COL_TYPE_BIT:
                return ByteUtil.compareNumberByte(left, right);
            case ColMeta.COL_TYPE_VAR_STRING:
            case ColMeta.COL_TYPE_STRING:
                // execute ENUM and SET as string
            case ColMeta.COL_TYPE_ENUM:
            case ColMeta.COL_TYPE_SET:
                return BytesTools.compareTo(left, right);
            // ignore BLOB GEOMETRY
            default:
                break;
        }
        return 0;
    }
}
