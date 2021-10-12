package com.helloworld.avarar.lib.cache;


import android.os.SystemClock;

public class CacheEntity<T> {
    private T entity;           //缓存对象
    private long cacheTime;     //缓存时间,单位秒
    private boolean hasCache;   //是否有缓存

    private long expireTime = 60 * 60 * 1;//单位:秒  , 有效时间为1小时

    public CacheEntity(T entity, long cacheTime, boolean hasCache) {
        this.entity = entity;
        this.cacheTime = cacheTime;
        this.hasCache = hasCache;
    }

    /**
     * 缓存是否过期
     *
     * @return
     */
    public boolean isCacheExpired() {
        //开机时间
        long elapsedTime = SystemClock.elapsedRealtime() / 1000; //秒数
        if (elapsedTime - cacheTime >= expireTime) {//大于1小时,过期
            return true;
        } else {
            return false;
        }
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    public boolean hasCache() {
        return hasCache;
    }

    public void setHasCache(boolean hasCache) {
        this.hasCache = hasCache;
    }
}
