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
package com.actiontech.dble.manager.response;

import com.actiontech.dble.DbleServer;
import com.actiontech.dble.backend.datasource.PhysicalDBPool;
import com.actiontech.dble.manager.ManagerConnection;
import com.actiontech.dble.net.mysql.OkPacket;
import com.actiontech.dble.route.parser.ManagerParseSwitch;
import com.actiontech.dble.route.parser.util.Pair;

import java.util.Map;

/**
 * SwitchDataSource
 *
 * @author mycat
 */
public final class SwitchDataSource {
    private SwitchDataSource() {
    }

    public static void response(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> pair = ManagerParseSwitch.getPair(stmt);
        Map<String, PhysicalDBPool> dns = DbleServer.getInstance().getConfig().getDataHosts();
        Integer idx = pair.getValue();
        for (String key : pair.getKey()) {
            PhysicalDBPool dn = dns.get(key);
            if (dn != null) {
                int m = dn.getActiveIndex();
                int n = (idx == null) ? dn.next(m) : idx;
                if (dn.switchSource(n, false, "MANAGER")) {
                    ++count;
                }
                //TODO:ELSE?
            }
        }
        OkPacket packet = new OkPacket();
        packet.setPacketId(1);
        packet.setAffectedRows(count);
        packet.setServerStatus(2);
        packet.write(c);
    }

}
