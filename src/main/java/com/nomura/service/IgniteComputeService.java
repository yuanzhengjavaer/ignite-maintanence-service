package com.nomura.service;

import com.nomura.common.Constants;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgniteRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


import java.util.concurrent.ExecutorService;

@Service
public class IgniteComputeService  {

    @Autowired
    private Ignite ignite;

    /**
     * 必ずignite package中のfunctional interface　を使う。
     */
    public void roundRobinCompute() {
        //broadcast to remote nodes , exclude local node
        ClusterGroup clusterGroup = ignite.cluster().forRemotes();
        // executorService は各nodeの間で、round robin で実行される
        ExecutorService executorService = ignite.executorService(clusterGroup);
        executorService.execute(new IgniteRunnable() {
            @Override
            public void run() {

                System.out.println("ignite compute 実行！！！！！！！！！！！！！");

            }
        });
    }

    /**
     * broadcast けど、local nodeは含まれてない
     */
    public void broadCastCompute() {
        ClusterGroup clusterGroup = ignite.cluster().forAttribute(Constants.SERVICE_NODE_TAG, true);
        IgniteCompute compute = ignite.compute(clusterGroup);
        compute.run(new IgniteRunnable() {
            @Override
            public void run() {
                System.out.println("attributeでnode を選択、ignite compute 実行！！！！！！！！！！！！！");
            }
        });

        ClusterGroup remotes = ignite.cluster().forRemotes();
        ignite.compute(remotes).run(new IgniteRunnable() {
            @Override
            public void run() {

                System.out.println("cluster(他の役のnodeを含め)、ignite compute 実行！！！！！！！！！！！！！");

            }
        });
    }
}
