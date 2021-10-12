package com.helloworld.avarar.lib.cache;

import android.graphics.Bitmap;


/**
 * 缓存管理
 */
public class CacheManager {
    private static volatile CacheManager sCacheManager = null;
    private Cache<String, CacheEntity<Bitmap>> memoryCache;


    private CacheManager(){
        memoryCache = new MemoryCache();
    }

    public static CacheManager getInstance(){
        if(sCacheManager == null){
            synchronized (CacheManager.class){
                if(sCacheManager == null){
                    sCacheManager = new CacheManager();
                }
            }
        }

        return sCacheManager;
    }

    public void save(String key, CacheEntity<Bitmap> value){
        if(key == null || value == null){
            return;
        }
        memoryCache.save(key,value);
    }

    public CacheEntity<Bitmap> get(String key){
        return memoryCache.get(key);
    }

    public CacheEntity<Bitmap> remove(String key) {
        return memoryCache.remove(key);
    }
}
