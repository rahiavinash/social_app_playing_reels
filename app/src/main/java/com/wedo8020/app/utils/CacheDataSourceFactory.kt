package com.wedo8020.app.utils

import android.annotation.SuppressLint
import android.content.Context

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import com.wedo8020.app.MainApplication
import java.lang.ref.WeakReference


class CachedDataSourceFactory(
    private val context: WeakReference<Context>,
    private val userAgent: String,
    private val cache: SimpleCache
) : DataSource.Factory {

    @SuppressLint("UnsafeOptInUsageError")
    override fun createDataSource(): DataSource {
        val httpDataSourceFactory: HttpDataSource.Factory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
        val upstreamFactory: DataSource.Factory = DefaultDataSource.Factory(context.get()?:MainApplication.get().applicationContext, httpDataSourceFactory)

        val cacheDataSinkFactory = CacheDataSink.Factory()
            .setCache(cache)
            .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)
        val dataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(cacheDataSinkFactory)

        return dataSourceFactory.createDataSource()
    }
}