package com.actiontech.dble.route.function;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PartitionByFileMapTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test1() {
        PartitionByFileMap partition = new PartitionByFileMap();
        partition.setMapFile("partition-hash-int.txt");
        partition.init();
        String idVal = "10000";
        Assert.assertEquals(true, 0 == partition.calculate(idVal));
        idVal = "10010";
        Assert.assertEquals(true, 1 == partition.calculate(idVal));
        idVal = "10020";
        Assert.assertEquals(true, null == partition.calculate(idVal));
    }

    @Test
    public void test2() {
        PartitionByFileMap partition = new PartitionByFileMap();
        partition.setMapFile("partition-hash-int.txt");
        partition.setDefaultNode(1);
        partition.init();
        String idVal = "10020";
        Assert.assertEquals(true, 1 == partition.calculate(idVal));
    }

    @Test
    public void test3() {
        PartitionByFileMap partition = new PartitionByFileMap();
        partition.setMapFile("partition-hash-int.txt");
        partition.setDefaultNode(1);
        partition.init();
        String idVal = "xx";
        thrown.expect(IllegalArgumentException.class);
        partition.calculate(idVal);
    }

    @Test
    public void test4() {
        PartitionByFileMap partition = new PartitionByFileMap();
        partition.setMapFile("partition-hash-int2.txt");
        partition.setType(-1);
        partition.init();
        String idVal = "A";
        Assert.assertEquals(true, 0 == partition.calculate(idVal));
        idVal = "B";
        Assert.assertEquals(true, 1 == partition.calculate(idVal));
        idVal = "C";
        Assert.assertEquals(true, null == partition.calculate(idVal));
    }

    @Test
    public void test5() {
        PartitionByFileMap partition = new PartitionByFileMap();
        partition.setMapFile("partition-hash-int2.txt");
        partition.setDefaultNode(1);
        partition.setType(-1);
        partition.init();
        String idVal = "C";
        Assert.assertEquals(true, 1 == partition.calculate(idVal));
    }

    @Test
    public void test6() {
        PartitionByFileMap partition = new PartitionByFileMap();
        partition.setMapFile("partition-hash-int2.txt");
        partition.setDefaultNode(1);
        partition.setType(-1);
        partition.init();
        String idVal = "1000";
        Assert.assertEquals(true, 1 == partition.calculate(idVal));
    }
}
