package com.wedo8020.app.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Pair
import androidx.annotation.WorkerThread
import androidx.media3.common.C
import androidx.media3.common.PriorityTaskManager
import androidx.media3.common.util.Assertions
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheKeyFactory
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.ContentMetadata

import java.io.EOFException
import java.io.IOException
import java.util.NavigableSet
import java.util.concurrent.atomic.AtomicBoolean


object CacheUtil {
    /** Default buffer size to be used while caching.  */
    const val DEFAULT_BUFFER_SIZE_BYTES = 128 * 1024

    /** Default [CacheKeyFactory].  */
    @SuppressLint("UnsafeOptInUsageError")
    val DEFAULT_CACHE_KEY_FACTORY: CacheKeyFactory = CacheKeyFactory { dataSpec ->
        if (dataSpec.key != null) dataSpec.key!! else generateKey(
            dataSpec.uri
        )
    }

    /**
     * Generates a cache key out of the given [Uri].
     *
     * @param uri Uri of a content which the requested key is for.
     */
    fun generateKey(uri: Uri): String {
        return uri.toString()
    }

    /**
     * Queries the cache to obtain the request length and the number of bytes already cached for a
     * given [DataSpec].
     *
     * @param dataSpec Defines the data to be checked.
     * @param cache A [Cache] which has the data.
     * @param cacheKeyFactory An optional factory for cache keys.
     * @return A pair containing the request length and the number of bytes that are already cached.
     */
    @SuppressLint("UnsafeOptInUsageError")
    fun getCached(
        dataSpec: DataSpec, cache: Cache, cacheKeyFactory: CacheKeyFactory?
    ): Pair<Long, Long> {
        val key = buildCacheKey(dataSpec, cacheKeyFactory)
        var position: Long = dataSpec.absoluteStreamPosition
        val requestLength = getRequestLength(dataSpec, cache, key)
        var bytesAlreadyCached: Long = 0
        var bytesLeft = requestLength
        while (bytesLeft != 0L) {
            var blockLength: Long = cache.getCachedLength(
                key, position, if (bytesLeft != C.LENGTH_UNSET.toLong()) bytesLeft else Long.MAX_VALUE
            )
            if (blockLength > 0) {
                bytesAlreadyCached += blockLength
            } else {
                blockLength = -blockLength
                if (blockLength == Long.MAX_VALUE) {
                    break
                }
            }
            position += blockLength
            bytesLeft -= if (bytesLeft == C.LENGTH_UNSET.toLong()) 0 else blockLength
        }
        return Pair.create(requestLength, bytesAlreadyCached)
    }

    /**
     * Caches the data defined by `dataSpec`, skipping already cached data. Caching stops early
     * if the end of the input is reached.
     *
     *
     * This method may be slow and shouldn't normally be called on the main thread.
     *
     * @param dataSpec Defines the data to be cached.
     * @param cache A [Cache] to store the data.
     * @param upstream A [DataSource] for reading data not in the cache.
     * @param progressListener A listener to receive progress updates, or `null`.
     * @param isCanceled An optional flag that will interrupt caching if set to true.
     * @throws IOException If an error occurs reading from the source.
     * @throws InterruptedException If the thread was interrupted directly or via `isCanceled`.
     */
    @SuppressLint("UnsafeOptInUsageError")
    @WorkerThread
    @Throws(IOException::class, InterruptedException::class)
    fun cache(
        dataSpec: DataSpec,
        cache: Cache,
        upstream: DataSource?,
        progressListener: ProgressListener?,
        isCanceled: AtomicBoolean?
    ) {
        cache(
            dataSpec,
            cache,  /* cacheKeyFactory= */
            null,
            CacheDataSource(cache, upstream),
            ByteArray(DEFAULT_BUFFER_SIZE_BYTES),  /* priorityTaskManager= */
            null,  /* priority= */
            0,
            progressListener,
            isCanceled,  /* enableEOFException= */
            false
        )
    }

    /**
     * Caches the data defined by `dataSpec`, skipping already cached data. Caching stops early
     * if end of input is reached and `enableEOFException` is false.
     *
     *
     * If a [PriorityTaskManager] is provided, it's used to pause and resume caching
     * depending on `priority` and the priority of other tasks registered to the
     * PriorityTaskManager. Please note that it's the responsibility of the calling code to call
     * [PriorityTaskManager.add] to register with the manager before calling this method, and to
     * call [PriorityTaskManager.remove] afterwards to unregister.
     *
     *
     * This method may be slow and shouldn't normally be called on the main thread.
     *
     * @param dataSpec Defines the data to be cached.
     * @param cache A [Cache] to store the data.
     * @param cacheKeyFactory An optional factory for cache keys.
     * @param dataSource A [CacheDataSource] that works on the `cache`.
     * @param buffer The buffer to be used while caching.
     * @param priorityTaskManager If not null it's used to check whether it is allowed to proceed with
     * caching.
     * @param priority The priority of this task. Used with `priorityTaskManager`.
     * @param progressListener A listener to receive progress updates, or `null`.
     * @param isCanceled An optional flag that will interrupt caching if set to true.
     * @param enableEOFException Whether to throw an [EOFException] if end of input has been
     * reached unexpectedly.
     * @throws IOException If an error occurs reading from the source.
     * @throws InterruptedException If the thread was interrupted directly or via `isCanceled`.
     */
    @SuppressLint("UnsafeOptInUsageError")
    @WorkerThread
    @Throws(IOException::class, InterruptedException::class)
    fun cache(
        dataSpec: DataSpec,
        cache: Cache,
        cacheKeyFactory: CacheKeyFactory?,
        dataSource: CacheDataSource,
        buffer: ByteArray,
        priorityTaskManager: PriorityTaskManager?,
        priority: Int,
        progressListener: ProgressListener?,
        isCanceled: AtomicBoolean?,
        enableEOFException: Boolean
    ) {
        Assertions.checkNotNull(dataSource)
        Assertions.checkNotNull(buffer)
        val key = buildCacheKey(dataSpec, cacheKeyFactory)
        var bytesLeft: Long
        var progressNotifier: ProgressNotifier? = null
        if (progressListener != null) {
            progressNotifier = ProgressNotifier(progressListener)
            val lengthAndBytesAlreadyCached = getCached(dataSpec, cache, cacheKeyFactory)
            progressNotifier.init(
                lengthAndBytesAlreadyCached.first,
                lengthAndBytesAlreadyCached.second
            )
            bytesLeft = lengthAndBytesAlreadyCached.first
        } else {
            bytesLeft = getRequestLength(dataSpec, cache, key)
        }
        var position: Long = dataSpec.absoluteStreamPosition
        val lengthUnset = bytesLeft == C.LENGTH_UNSET.toLong()
        while (bytesLeft != 0L) {
            throwExceptionIfInterruptedOrCancelled(isCanceled)
            var blockLength: Long =
                cache.getCachedLength(key, position, if (lengthUnset) Long.MAX_VALUE else bytesLeft)
            if (blockLength > 0) {
                // Skip already cached data.
            } else {
                // There is a hole in the cache which is at least "-blockLength" long.
                blockLength = -blockLength
                val length = if (blockLength == Long.MAX_VALUE) C.LENGTH_UNSET else blockLength
                val isLastBlock = length == bytesLeft
                val read = readAndDiscard(
                    dataSpec,
                    position,
                    length.toLong(),
                    dataSource,
                    buffer,
                    priorityTaskManager,
                    priority,
                    progressNotifier,
                    isLastBlock,
                    isCanceled
                )
                if (read < blockLength) {
                    // Reached to the end of the data.
                    if (enableEOFException && !lengthUnset) {
                        throw EOFException()
                    }
                    break
                }
            }
            position += blockLength
            if (!lengthUnset) {
                bytesLeft -= blockLength
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun getRequestLength(dataSpec: DataSpec, cache: Cache, key: String): Long {
        return if (dataSpec.length !== C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else ({
            val contentLength: Long =
                ContentMetadata.getContentLength(cache.getContentMetadata(key))
            if (contentLength == C.LENGTH_UNSET.toLong()) C.LENGTH_UNSET else contentLength - dataSpec.absoluteStreamPosition
        }) as Long
    }

    /**
     * Reads and discards all data specified by the `dataSpec`.
     *
     * @param dataSpec Defines the data to be read. `absoluteStreamPosition` and `length`
     * fields are overwritten by the following parameters.
     * @param absoluteStreamPosition The absolute position of the data to be read.
     * @param length Length of the data to be read, or [C.LENGTH_UNSET] if it is unknown.
     * @param dataSource The [DataSource] to read the data from.
     * @param buffer The buffer to be used while downloading.
     * @param priorityTaskManager If not null it's used to check whether it is allowed to proceed with
     * caching.
     * @param priority The priority of this task.
     * @param progressNotifier A notifier through which to report progress updates, or `null`.
     * @param isLastBlock Whether this read block is the last block of the content.
     * @param isCanceled An optional flag that will interrupt caching if set to true.
     * @return Number of read bytes, or 0 if no data is available because the end of the opened range
     * has been reached.
     */
    @SuppressLint("UnsafeOptInUsageError")
    @Throws(IOException::class, InterruptedException::class)
    private fun readAndDiscard(
        dataSpec: DataSpec,
        absoluteStreamPosition: Long,
        length: Long,
        dataSource: DataSource,
        buffer: ByteArray,
        priorityTaskManager: PriorityTaskManager?,
        priority: Int,
        progressNotifier: ProgressNotifier?,
        isLastBlock: Boolean,
        isCanceled: AtomicBoolean?
    ): Long {
        var positionOffset: Long = absoluteStreamPosition - dataSpec.absoluteStreamPosition
        val initialPositionOffset = positionOffset
        val endOffset = if (length != C.LENGTH_UNSET.toLong()) positionOffset + length else C.POSITION_UNSET
        while (true) {
            if (priorityTaskManager != null) {
                // Wait for any other thread with higher priority to finish its job.
                priorityTaskManager.proceed(priority)
            }
            throwExceptionIfInterruptedOrCancelled(isCanceled)
            try {
                var resolvedLength: Long = C.LENGTH_UNSET.toLong()
                var isDataSourceOpen = false
                if (endOffset != C.POSITION_UNSET) {
                    // If a specific length is given, first try to open the data source for that length to
                    // avoid more data then required to be requested. If the given length exceeds the end of
                    // input we will get a "position out of range" error. In that case try to open the source
                    // again with unset length.
                    try {
                        resolvedLength = dataSource.open(
                            dataSpec.subrange(
                                positionOffset,
                                endOffset.toLong() - positionOffset
                            )
                        )
                        isDataSourceOpen = true
                    } catch (exception: IOException) {
                        if (!isLastBlock || !isCausedByPositionOutOfRange(exception)) {
                            throw exception
                        }
//                        Util.closeQuietly(dataSource)
                    }
                }
                if (!isDataSourceOpen) {
                    resolvedLength =
                        dataSource.open(dataSpec.subrange(positionOffset, C.LENGTH_UNSET.toLong()))
                }
                if (isLastBlock && progressNotifier != null && resolvedLength != C.LENGTH_UNSET.toLong()) {
                    progressNotifier.onRequestLengthResolved(positionOffset + resolvedLength)
                }
                while (positionOffset != endOffset) {
                    throwExceptionIfInterruptedOrCancelled(isCanceled)
                    val bytesRead: Int = dataSource.read(
                        buffer,
                        0,
                        if (endOffset != C.POSITION_UNSET) Math.min(
                            buffer.size.toLong(),
                            endOffset.toLong() - positionOffset
                        ).toInt() else buffer.size
                    )
                    if (bytesRead == C.RESULT_END_OF_INPUT) {
                        progressNotifier?.onRequestLengthResolved(positionOffset)
                        break
                    }
                    positionOffset += bytesRead.toLong()
                    progressNotifier?.onBytesCached(bytesRead.toLong())
                }
                return positionOffset - initialPositionOffset
            } catch (exception: PriorityTaskManager.PriorityTooLowException) {
                // catch and try again
            } finally {
//                Util.closeQuietly(dataSource)
            }
        }
    }

    /**
     * Removes all of the data specified by the `dataSpec`.
     *
     *
     * This methods blocks until the operation is complete.
     *
     * @param dataSpec Defines the data to be removed.
     * @param cache A [Cache] to store the data.
     * @param cacheKeyFactory An optional factory for cache keys.
     */
    @WorkerThread
    fun remove(
        dataSpec: DataSpec, cache: Cache, cacheKeyFactory: CacheKeyFactory?
    ) {
        remove(cache, buildCacheKey(dataSpec, cacheKeyFactory))
    }

    /**
     * Removes all of the data specified by the `key`.
     *
     *
     * This methods blocks until the operation is complete.
     *
     * @param cache A [Cache] to store the data.
     * @param key The key whose data should be removed.
     */
    @SuppressLint("UnsafeOptInUsageError")
    @WorkerThread
    fun remove(cache: Cache, key: String?) {
        val cachedSpans: NavigableSet<CacheSpan> = cache.getCachedSpans(key!!)
        for (cachedSpan in cachedSpans) {
            cache.removeSpan(cachedSpan)
        }
    }

    /* package */
    @SuppressLint("UnsafeOptInUsageError")
    fun isCausedByPositionOutOfRange(e: IOException?): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            if (cause is DataSourceException) {
                val reason: Int = (cause as DataSourceException).reason
                if (reason == DataSourceException.POSITION_OUT_OF_RANGE) {
                    return true
                }
            }
            cause = cause.cause
        }
        return false
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun buildCacheKey(
        dataSpec: DataSpec, cacheKeyFactory: CacheKeyFactory?
    ): String {
        return (if (cacheKeyFactory != null) cacheKeyFactory else DEFAULT_CACHE_KEY_FACTORY)
            .buildCacheKey(dataSpec)
    }

    @Throws(InterruptedException::class)
    private fun throwExceptionIfInterruptedOrCancelled(isCanceled: AtomicBoolean?) {
        if (Thread.interrupted() || isCanceled!! != null && isCanceled.get()) {
            throw InterruptedException()
        }
    }

    /** Receives progress updates during cache operations.  */
    interface ProgressListener {
        /**
         * Called when progress is made during a cache operation.
         *
         * @param requestLength The length of the content being cached in bytes, or [     ][C.LENGTH_UNSET] if unknown.
         * @param bytesCached The number of bytes that are cached.
         * @param newBytesCached The number of bytes that have been newly cached since the last progress
         * update.
         */
        fun onProgress(requestLength: Long, bytesCached: Long, newBytesCached: Long)
    }

    private class ProgressNotifier(
        /** The listener to notify when progress is made.  */
        private val listener: ProgressListener
    ) {
        /** The length of the content being cached in bytes, or [C.LENGTH_UNSET] if unknown.  */
        private var requestLength: Long = 0

        /** The number of bytes that are cached.  */
        private var bytesCached: Long = 0
        fun init(requestLength: Long, bytesCached: Long) {
            this.requestLength = requestLength
            this.bytesCached = bytesCached
            listener.onProgress(requestLength, bytesCached,  /* newBytesCached= */0)
        }

        fun onRequestLengthResolved(requestLength: Long) {
            if (this.requestLength == C.LENGTH_UNSET.toLong() && requestLength != C.LENGTH_UNSET.toLong()) {
                this.requestLength = requestLength
                listener.onProgress(requestLength, bytesCached,  /* newBytesCached= */0)
            }
        }

        fun onBytesCached(newBytesCached: Long) {
            bytesCached += newBytesCached
            listener.onProgress(requestLength, bytesCached, newBytesCached)
        }
    }
}

