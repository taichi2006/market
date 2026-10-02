package com.mycompany.quanlysieuthi.util;

import redis.clients.jedis.Jedis;
import java.net.URI;

public class RedisTest {
    public static void main(String[] args) {
        System.out.println("Connecting to Upstash Redis...");
        
        // Cấu hình kết nối sử dụng URI từ Upstash (dùng rediss:// cho kết nối bảo mật TLS)
        try (Jedis jedis = new Jedis(URI.create("rediss://default:gQAAAAAAA3GyAAIgcDI5NTAzYzc0MGY3N2Q0MzAyYTViNGI5NGMxYTU0YTI4YQ@polished-snapper-225714.upstash.io:6379"))) {
            
            // Kiểm tra kết nối
            String pingResult = jedis.ping();
            System.out.println("Connection successful! Server responded with: " + pingResult);
            
            // Test một số lệnh cơ bản
            jedis.set("foo", "bar");
            String value = jedis.get("foo");
            
            System.out.println("Stored value for 'foo' in redis: " + value);
            
        } catch (Exception e) {
            System.out.println("Failed to connect to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
