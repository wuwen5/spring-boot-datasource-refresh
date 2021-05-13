package com.example.demo;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.jdbc.DataSourcePoolMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.jdbc.core.JdbcTemplate;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
    RefreshScope refreshScope;

	@Test
	void contextLoads() throws IOException, AttachNotSupportedException {

	    IntStream.range(0, 3).forEach(i -> {
            String ret = jdbcTemplate.queryForObject("select 1", String.class);

            System.out.println(ret);
            System.out.println("DataSource:" + jdbcTemplate.getDataSource());

            refreshScope.refreshAll();

        });


        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();


        List<String> dump = dump(runtimeMXBean.getName().split("@")[0], s -> s.contains("com.zaxxer.hikari.HikariDataSource"));

        System.out.println(dump);
        //TODO Assertions.assertTrue(dump1.isEmpty());

        //If try to clear the cache, the instance has been released.
        try {
            Class<?>[] declaredClasses = DataSourcePoolMetrics.class.getDeclaredClasses();
            Field cache = declaredClasses[0].getDeclaredField("cache");

            cache.setAccessible(true);
            Map map = (Map) cache.get(declaredClasses[0]);

            map.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> dump1 = dump(runtimeMXBean.getName().split("@")[0], s -> s.contains("com.zaxxer.hikari.HikariDataSource"));

        Assertions.assertTrue(dump1.isEmpty());
    }

    static List<String> dump(String pid, Predicate<String> filter) throws IOException, AttachNotSupportedException {
        VirtualMachine var2 = VirtualMachine.attach(pid);
        InputStream var3 = ((HotSpotVirtualMachine)var2).heapHisto("-live");
        return drain(var2, var3, filter);
    }

    private static List<String> drain(VirtualMachine var0, InputStream var1, Predicate<String> filter) throws IOException {
        byte[] var2 = new byte[256];

        List<String> list = new ArrayList<>();
        int var3;
        do {
            var3 = var1.read(var2);
            if (var3 > 0) {
                String var4 = new String(var2, 0, var3, "UTF-8");

                if (filter.test(var4)) {
                    list.add(var4);
                }
            }
        } while(var3 > 0);

        var1.close();
        var0.detach();

        return list;
    }

}
