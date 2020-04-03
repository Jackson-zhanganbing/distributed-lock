package com.zab.distributedlock.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ZkDistributedLock implements Lock, Watcher {

    private ZooKeeper zk = null;
    private String ROOT_LOCK = "/locks";
    private String WAIT_LOCK;
    private String CURRENT_LOCK;
    private CountDownLatch countDownLatch;

    public ZkDistributedLock() {
        try {
            zk = new ZooKeeper("localhost:2181", 4000, this);
            Stat stat = zk.exists(ROOT_LOCK, false);
            if (stat == null) {
                zk.create(ROOT_LOCK, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lock() {
        if (this.tryLock()) {
            System.out.println(Thread.currentThread().getName() + "->" + CURRENT_LOCK + "获得锁");
            return;
        }
        waitForLock(WAIT_LOCK);
    }

    private boolean waitForLock(String prev) {
        try {
            Stat stat = zk.exists(prev, true);
            if (stat != null) {
                System.out.println(Thread.currentThread().getName() + "等待锁" + prev + "释放");
                countDownLatch = new CountDownLatch(1);
                countDownLatch.await();
                System.out.println(Thread.currentThread().getName() + "获得锁");
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            //创建临时有序结点
            CURRENT_LOCK = zk.create(ROOT_LOCK + "/", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName() + "-->" + CURRENT_LOCK + "尝试竞争锁");
            List<String> children = zk.getChildren(ROOT_LOCK, false);
            SortedSet<String> treeSet = new TreeSet<>();
            for (String child : children) {
                treeSet.add(ROOT_LOCK + "/" + child);
            }
            String firstNode = treeSet.first();
            SortedSet<String> lessThanMe = ((TreeSet<String>) treeSet).headSet(CURRENT_LOCK);
            if (CURRENT_LOCK.equals(firstNode)) {
                return true;
            }
            if (!lessThanMe.isEmpty()) {
                WAIT_LOCK = lessThanMe.last();
            }


        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        try {
            zk.delete(CURRENT_LOCK, -1);
            CURRENT_LOCK = null;
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void process(WatchedEvent event) {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }
}
