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
package com.actiontech.dble.config.model;

import java.util.Set;

/**
 * @author mycat
 */
public class UserConfig {

    private String name;
    private String password;
    private String encryptPassword;
    private int benchmark = 0;                        // default 0 means not check
    private UserPrivilegesConfig privilegesConfig;    //privileges for tables

    private boolean readOnly = false;
    private boolean manager = false;

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private Set<String> schemas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(int benchmark) {
        this.benchmark = benchmark;
    }

    public Set<String> getSchemas() {
        return schemas;
    }

    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
    }

    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    public UserPrivilegesConfig getPrivilegesConfig() {
        return privilegesConfig;
    }

    public void setPrivilegesConfig(UserPrivilegesConfig privilegesConfig) {
        this.privilegesConfig = privilegesConfig;
    }

    @Override
    public String toString() {
        return "UserConfig [name=" + this.name + ", password=" + this.password + ", encryptPassword=" +
                this.encryptPassword + ", benchmark=" + this.benchmark + ", manager=" + this.manager +
                ", readOnly=" + this.readOnly + ", schemas=" + this.schemas + "]";
    }


}
