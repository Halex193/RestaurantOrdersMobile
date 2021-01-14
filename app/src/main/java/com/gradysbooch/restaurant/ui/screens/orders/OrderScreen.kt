package com.gradysbooch.restaurant.ui.screens.orders

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.gradysbooch.restaurant.model.Table
import com.gradysbooch.restaurant.ui.values.*
import com.gradysbooch.restaurant.viewmodel.OrderViewModel
import java.lang.Exception

class OrderScreen(
        private val orderViewModel: OrderViewModel,
        private val screenNavController: NavHostController,
        private val tableId: String?,
) {
    private val backupColor : String = "#000"

    private lateinit var selectedColor: MutableState<String>
    private lateinit var locked: MutableState<Boolean>
    private lateinit var orderNavController: NavHostController

    @Composable
    fun Show() {
        if (tableId.isNullOrEmpty()) {
            screenNavController.navigate("tables")
            return
        }
        orderViewModel.setTable(tableId)

        selectedColor = remember { mutableStateOf(backupColor) }
        locked = remember { mutableStateOf(false) }

        orderNavController = rememberNavController()

        Scaffold(topBar = { AppBar() }, bodyContent = {

            NavHost(navController = orderNavController, startDestination = "all") {

                composable("all") {
                    selectedColor.value = backupColor
                    locked.value = false
                    AllOrderScreen(orderViewModel).Show()
                    // AllScreen()
                }

                composable("one/{selectedColor}/{locked}",
                        arguments = listOf(
                                navArgument("selectedColor") { type = NavType.StringType },
                                navArgument("locked") { type = NavType.BoolType }
                        )
                ) {
                    selectedColor.value = it.arguments?.getString("selectedColor")!!
                    locked.value = it.arguments?.getBoolean("locked")!!
                    OneOrderScreen(orderViewModel, selectedColor, locked).Show()
                    // OneScreen()
                }
            }
        })
    }


    //  --------------------------------    APP BAR     --------------------------------


    @Composable
    private fun AppBar() {
        val selectedTable by orderViewModel.table
                .collectAsState(initial = Table("-1", "name", 0, false))

        TopAppBar(
                backgroundColor = getColorOr(selectedColor.value, MaterialTheme.colors.primaryVariant),
                modifier = Modifier.height(120.dp),
                title = {
                    Column{
                        OrderScreenTopRow(selectedTable)
                        CustomerNavigationRow(selectedTable)
                    }
                })
    }

    @Composable
    private fun OrderScreenTopRow(selectedTable: Table) {
        val isChecked by orderViewModel.requiresAttention
                .collectAsState(initial = false)

        RoundedRowCard (
                color = Color.Transparent,
                modifier = Modifier.padding(8.dp, 0.dp).fillMaxWidth()
        ){
            // Go Back
            RoundedIconButton(
                    color = Color.Transparent,
                    tint = MaterialTheme.colors.secondary,
                    asset = Icons.Filled.ArrowBack,
                    onClick = {
                        // orderViewModel.setTable("-1")
                        screenNavController.navigate("tables")
                    })
            // Table name & code
            Text(
                    modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp),
                    text = "${selectedTable.name} (#${selectedTable.code})"
            )
            // Check Table
            RoundedIconButton(
                    color = Color.Transparent,
                    tint = MaterialTheme.colors.secondary,
                    asset = if (isChecked) Icons.Filled.CheckCircle else Icons.Filled.Check,
                    onClick = {
                        orderViewModel.clearAttention()
                    })
        }
    }

    @Composable
    private fun CustomerNavigationRow(selectedTable: Table) {
        val bullets by orderViewModel.bulletList
                .collectAsState(initial = emptyList())

        Surface(
                modifier = Modifier.padding(8.dp, 0.dp).fillMaxWidth(),
                shape = RoundedCornerShape(20)
        ) {
            Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
            ) {
                // Go to All Orders Screen
                RoundedIconButton(
                        modifier = Modifier.padding(4.dp, 0.dp),
                        asset = Icons.Filled.Check,
                        onClick = {
                            // orderViewModel.selectAllScreen()
                            orderNavController.navigate("all")
                        })
                // Add new Bullet
                RoundedIconButton(
                        modifier = Modifier.padding(4.dp, 0.dp),
                        asset = Icons.Filled.Add,
                        onClick = {
                            try { orderViewModel.addBullet()
                            } catch (e : Exception) { Log.d("ERROR", e.stackTraceToString()) }
                        })
                // Navigation Bullets
                LazyRowFor(items = bullets) { bullet -> RoundedIconButton(
                        modifier = Modifier.padding(4.dp, 0.dp)
                                .longPressGestureFilter {
                                    if (bullet.locked) { orderViewModel.unlockOrder(selectedTable.tableUID, bullet.color)
                                    } else { orderViewModel.lockOrder(selectedTable.tableUID, bullet.color) }
                                },
                        color = getColorOr(bullet.color),
                        tint = MaterialTheme.colors.primary,
                        asset = if (bullet.locked) Icons.Filled.Lock else Icons.Filled.Clear,
                        onClick = {
                            // orderViewModel.selectColor(bullet.color)
                            orderNavController.navigate("one/${bullet.color}/${bullet.locked}")
                        })
                }
            }
        }
    }
}