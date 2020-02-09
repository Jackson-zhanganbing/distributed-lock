package com.zab.distributedlock.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class ZookeeperTest {
    public static void main(String[] args) throws Exception{
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 4000, null);

        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (Event.KeeperState.SyncConnected==event.getState()) {
                    latch.countDown();
                }

                try {
                    zooKeeper.exists(event.getPath(),true);
                }catch (Exception e){

                }

                if(Event.EventType.NodeCreated.getIntValue()==event.getState().getIntValue()){
                    System.out.println("有结点创建！");
                }
            }
        };
        zooKeeper.register(watcher);

        latch.await();

        System.out.println(zooKeeper.getState());

        Stat stat = new Stat();
        zooKeeper.delete("/ca",stat.getVersion());
        zooKeeper.create("/ca","666".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        Thread.sleep(1000);

        byte[] data = zooKeeper.getData("/ca", null, stat);
        System.out.println(new String(data));
        System.in.read();
    }
}
