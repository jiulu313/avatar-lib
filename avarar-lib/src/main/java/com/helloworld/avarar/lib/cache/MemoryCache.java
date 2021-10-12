package com.helloworld.avarar.lib.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * 内存缓存
 */
public class MemoryCache implements Cache<String, CacheEntity<Bitmap>>{
    LruCache<String,CacheEntity<Bitmap>> mLruCache;

    public MemoryCache(){
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 8);
        mLruCache = new LruCache<String, CacheEntity<Bitmap>>(cacheSize){
            @Override
            protected int sizeOf(String key, CacheEntity<Bitmap> entity) {
                if(entity != null && entity.hasCache() && entity.getEntity() != null){
                    return entity.getEntity().getByteCount();
                }else {
                    return 0;
                }
            }
        };
    }

    @Override
    public void save(String key, CacheEntity<Bitmap> value) {
        if(key == null || value == null){
            return;
        }

        mLruCache.put(key,value);
    }

    @Override
    public CacheEntity<Bitmap> get(String key){
        return mLruCache.get(key);
    }

    @Override
    public CacheEntity<Bitmap> remove(String key) {
        return mLruCache.remove(key);
    }
}
