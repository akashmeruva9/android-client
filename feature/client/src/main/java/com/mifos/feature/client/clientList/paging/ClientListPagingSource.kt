@file:OptIn(ExperimentalCoroutinesApi::class)

package com.mifos.feature.client.clientList.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mifos.core.common.utils.Page
import com.mifos.core.data.model.client.Client
import com.mifos.core.model.ClientDb
import com.mifos.core.network.datamanger.DataManagerClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by Aditya Gupta on 21/02/24.
 */

class ClientListPagingSource(private val dataManagerClient: DataManagerClient) :
    PagingSource<Int, Client>() {

    override fun getRefreshKey(state: PagingState<Int, Client>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(10) ?: state.closestPageToPosition(
                position
            )?.nextKey?.minus(10)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Client> {
        val position = params.key ?: 0
        return try {
            val getClients = getClientList(position)
            val clientList = getClients.first
            val totalClients = getClients.second
            val clientDbList = getClientDbList()
            val clientListWithSync = getClientListWithSync(clientList, clientDbList)
            LoadResult.Page(
                data = clientListWithSync,
                prevKey = if (position <= 0) null else position - 10,
                nextKey = if (position >= totalClients) null else position + 10
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getClientList(position: Int): Pair<List<Client>, Int> {
        return suspendCancellableCoroutine { continuation ->
            dataManagerClient.getAllClients(offset = position, 10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<Page<Client>>() {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        continuation.resumeWithException(e)
                    }

                    override fun onNext(t: Page<Client>) {
                        continuation.resume(Pair(t.pageItems, t.totalFilteredRecords))
                    }
                })
        }
    }

    private suspend fun getClientDbList(): List<ClientDb> {
        return dataManagerClient.allDatabaseClients().first().pageItems
    }

    private fun getClientListWithSync(
        clientList: List<Client>,
        clientDbList: List<ClientDb>
    ): List<Client> {
        if (clientDbList.isEmpty()) {
            clientList.forEach { client ->
                clientDbList.forEach { clientDb ->
                    if (client.id == clientDb._id) {
                        client.sync = true
                    }
                }
            }
        }
        return clientList
    }
}