package com.example.list.fragments


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.list.data.Couriers
import com.example.list.data.Orders
import com.example.list.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.Date


class OrdersViewModel : ViewModel() {
    var ordersList: MutableLiveData<List<Orders>> = MutableLiveData()

    var _orders: Orders?= null
    var isTvTNTapped: Boolean = false

    val order
        get()=_orders

    lateinit var couriers: Couriers



    fun set_Courier_ByAdress(couriers: Couriers){
        this.couriers = couriers
        AppRepository.getInstance().listOfOrders.observeForever {
            ordersList.postValue(AppRepository.getInstance().getCourierOrdersByAdress(couriers.id))
        }
        AppRepository.getInstance().orders.observeForever {
            _orders = it
        }
    }

    ////////////////////////////////////////////////////////
    fun set_Courier_ByDate(couriers: Couriers){
        this.couriers = couriers
        AppRepository.getInstance().listOfOrders.observeForever {
            ordersList.postValue(AppRepository.getInstance().getCourierOrdersByDate(couriers.id))
        }
        AppRepository.getInstance().orders.observeForever {
            _orders = it
        }
    }
    fun set_Courier_ByTime(couriers: Couriers){
        this.couriers = couriers
        AppRepository.getInstance().listOfOrders.observeForever {
            ordersList.postValue(AppRepository.getInstance().getCourierOrdersByTime(couriers.id))
        }
        AppRepository.getInstance().orders.observeForever {
            _orders = it
        }
    }
    fun set_Courier_ByTimeDelivery(couriers: Couriers){
        this.couriers = couriers
        AppRepository.getInstance().listOfOrders.observeForever {
            ordersList.postValue(AppRepository.getInstance().getCourierOrdersByTimeDelivery(couriers.id))
        }
        AppRepository.getInstance().orders.observeForever {
            _orders = it
        }
    }


    /////////////////////////////////////////////////////////////    /////////////////////////////////////////////////////////////

    fun filterItems(query: String) {
        viewModelScope.launch {
            val filteredList = mutableListOf<Orders>()
            ordersList.value?.map {
                if (it.address.contains(query, true)) filteredList.add(it)
            }
            ordersList.postValue(filteredList)
        }
    }

    /////////////////////////////////////////////////////////////    /////////////////////////////////////////////////////////////

    fun deleteOrder(){

        if (order!=null)
            AppRepository.getInstance().deleteOrder(order!!)
    }


    private val selectedName = MutableLiveData<String>()


    fun setSelectedName(name: String) {
        selectedName.value = name
    }


    fun setCurrentOrder(orders: Orders){
        AppRepository.getInstance().setCurrentStudent(orders)
    }


}


