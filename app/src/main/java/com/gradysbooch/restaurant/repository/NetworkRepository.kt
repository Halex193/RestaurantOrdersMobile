package com.gradysbooch.restaurant.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.exception.ApolloException
import com.gradysbooch.restaurant.GetMenuItemsQuery
import com.gradysbooch.restaurant.SubscribeToOrderItemsSubscription
import com.gradysbooch.restaurant.SubscribeToOrdersSubscription
import com.gradysbooch.restaurant.SubscribeToTablesSubscription
import com.gradysbooch.restaurant.model.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import kotlin.math.roundToInt

val apolloClient = ApolloClient.builder()
        .serverUrl("https://restaurant.playgroundev.com/graphql/")
        .build()

class NetworkRepository(context: Context? = null) : NetworkRepositoryInterface {
    @OptIn(ExperimentalCoroutinesApi::class)
    val internalOnlineStatus = MutableStateFlow(false)

    override val onlineStatus: Flow<Boolean> = internalOnlineStatus

    /**
     * This function does an apollo call and checks for apollo failure and null return.
     * @param T
     * The return type, must be Operation.Data. It is up to the caller to make sure that the query returns the propper return type
     * @param GQLQuery
     *  Instance of the query to be run
     */
    private suspend inline fun <reified T : Operation.Data> runQuerySafely(GQLQuery: Query<*, *, *>): T {
        try {
            val result = (apolloClient.query(GQLQuery).await().data as? T
                    ?: throw IOException("ApolloFailure: menu items returned null."))

            internalOnlineStatus.emit(true)
            return result

        } catch (e: ApolloException) {
            Log.d("NetworkError", e.stackTraceToString())

            internalOnlineStatus.emit(false)
            throw IOException("ApolloFailure: failed to get menu items.", e)
        }
    }

    override suspend fun getMenuItems(): Set<MenuItem> {
        val list = runQuerySafely<GetMenuItemsQuery.Data>(GetMenuItemsQuery()).menuItems?.data
                ?: throw IOException("ApolloFailure: menu items returned null.")

        return list.filterNotNull().map {
            MenuItem(it.id
                    ?: error("Id null."),
                    it.internalName,
                    it.price.roundToInt())
        }.toSet()
    }

    @ExperimentalCoroutinesApi
    override fun getTables(): Flow<Set<Table>> {
//        return apolloClient.subscribe(SubscribeToTablesSubscription())
//                .toFlow()
//                .map { value ->
//                    value.data?.servings?.data?.map { it ->
//                        it ?: error("Item null");
//                        Table(
//                                it.userId ?: error("UserId null"),
//                                "PLACEHOLDER",
//                                it.code?.toInt(),
//                                it.called ?: false
//                        )
//                    }?.toSet() ?: error("Set null")
//                }
//
        return flowOf(
                setOf(
                    Table("table1", "name1", 1, false),
                    Table("table2", "name2", 2, false),
            )
        )
    }

    @ExperimentalCoroutinesApi
    override fun clientOrders(): Flow<List<Order>> {
        /*return apolloClient.subscribe(SubscribeToOrdersSubscription())
                .toFlow()
                .map { value ->
                    value.data?.orders?.data?.map { it ->
                        it ?: error("Item null");
                        Order(
                                it.id ?: error("Id null"),
                                "PLACEHOLDER",
                                it.note ?: error("Note null")
                        )
                    }?.toList() ?: error("List null")
                }*/
        return flowOf(listOf(Order("table1", "green", "")))
    }

    @ExperimentalCoroutinesApi
    override fun orderItems(): Flow<List<OrderItem>> {
        return apolloClient.subscribe(SubscribeToOrderItemsSubscription())
                .toFlow()
                .map { value ->
                    value.data?.orderMenuItems?.data?.map { it ->
                        it ?: error("Item null")
                        OrderItem(
                                it.color ?: error("Color null"),
                                it.servingId ?: error("ServingId null"),
                                it.menuItemId ?: error("MenuItemId null"),
                                it.quantity ?: error("Quality null")
                        )
                    }?.toList() ?: error("List null")
                }
    }

    override suspend fun clearCall(tableUID: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateOrder(orderWithMenuItems: OrderWithMenuItems) {
        TODO("Not yet implemented")
    }

    override suspend fun unlock(order: Order) {
        TODO("Not yet implemented")
    }
}