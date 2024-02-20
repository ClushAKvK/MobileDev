package com.example.list.repository

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager

import com.example.list.ApplicationList2
import com.example.list.api.APPEND_COMPANY
import com.example.list.api.APPEND_COURIER
import com.example.list.api.APPEND_ORDER
import com.example.list.api.DELETE_COMPANY
import com.example.list.api.DELETE_COURIER
import com.example.list.api.DELETE_ORDER
import com.example.list.api.ListAPI
import com.example.list.api.ListConnection
import com.example.list.api.PostCompany
import com.example.list.api.PostResult
import com.example.list.api.PostCouriers
import com.example.list.api.PostOrders
import com.example.list.api.UPDATE_COMPANY
import com.example.list.api.UPDATE_COURIER
import com.example.list.api.UPDATE_ORDER

import com.example.list.data.Company
import com.example.list.data.Companies
import com.example.list.data.Couriers
import com.example.list.data.Couriersed
import com.example.list.data.Orders
import com.example.list.data.Ordered
import com.example.list.data.User
import com.example.list.data.UserLogin
import com.example.list.database.ListDatabase
import com.example.list.myConsts.TAG
import com.example.list1110.R
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AppRepository {
    companion object {

        private var INSTANCE: AppRepository? = null

        fun getInstance(): AppRepository {
            if (INSTANCE == null) {
                INSTANCE = AppRepository()
            }
            return INSTANCE?:
            throw IllegalStateException("Repository is not inisializated")
        }

    }

    var company: MutableLiveData<Company> = MutableLiveData()
    var couriers: MutableLiveData<Couriers> = MutableLiveData()
    var orders: MutableLiveData<Orders> = MutableLiveData()

    fun setCurrentCompany(_company: Company){
        company.postValue(_company)
    }

    fun loadData(){
        fetchCompanies()
    }

    fun setCurrentCourier(_couriers:Couriers){
        couriers.postValue(_couriers)
    }

    val companyCouriers
        get() = listOfCouriers.value?.filter{it.companyID == (company.value?.id?:0)}?.sortedBy { it.name } ?:  listOf()

    fun setCurrentStudent(_orders:Orders){
        orders.postValue( _orders)
    }


    //////////////////////////////////////////////////////////////////
    fun getCourierOrdersByAdress(courierID: Int) =
        (listOfOrders.value?.filter {it.courierID == courierID }?.sortedBy { it.address } ?:listOf())

    fun getCourierOrdersByDate(courierID: Int) =
        listOfOrders.value?.filter {it.courierID == courierID }?.sortedBy { it.date } ?:listOf()

    fun getCourierOrdersByTime(courierID: Int) =
        listOfOrders.value?.filter {it.courierID == courierID }?.sortedBy { it.time } ?:listOf()

    fun getCourierOrdersByTimeDelivery(courierID: Int) =
        listOfOrders.value?.filter {it.courierID == courierID }?.sortedBy { it.timeDelivery } ?:listOf()
    /////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////
    private val listDB by lazy { OfflineDBRepository(ListDatabase.getDatabase(ApplicationList2.context).listDAO()) }

    private val myCoroutineScope = CoroutineScope(Dispatchers.Main)


    val listOfOrders: LiveData<List<Orders>> = listDB.getAllOrders().asLiveData()
    val listOfCompany: LiveData<List<Company>> = listDB.getCompany().asLiveData()
    val listOfCouriers: LiveData<List<Couriers>> = listDB.getAllCouriers().asLiveData()

    private var listAPI = ListConnection.getClient().create(ListAPI::class.java)

    fun fetchCompanies(){
        listAPI.getCompanys().enqueue(object: Callback<Companies> {
            override fun onFailure(call: Call<Companies>, t :Throwable){
                Log.d(TAG,"Ошибка получения списка служеб доставки", t)
            }
            override fun onResponse(
                call : Call<Companies>,
                response: Response<Companies>
            ){
                if (response.code()==200){
                    val companys= response.body()
                    val items =companys?.items?:emptyList()
                    Log.d(TAG,"Получен список служеб доставки $items")
                    myCoroutineScope.launch{
                        listDB.deleteAllCompany()
                        for (f in items){
                            listDB.insertCompany(f)
                        }
                    }
                    fetchCouriers()
                }
            }
        })
    }


    private fun updateCompanies(postCompany: PostCompany){
        listAPI.postCompany(postCompany)
            .enqueue(object : Callback<PostResult> {
                override fun onResponse(call: Call<PostResult>, response: Response<PostResult>){
                    if (response.code()==200) fetchCompanies()
                }
                override fun onFailure(call: Call<PostResult>, t: Throwable){
                    Log.d(TAG,"Ошибка записи службы доставки",t)
                }
            })
    }

    private fun updateCouriers(postCouriers: PostCouriers){
        listAPI.postCourier(postCouriers)
            .enqueue(object : Callback<PostResult>{
                override fun onResponse(call:Call<PostResult>,response: Response<PostResult>){
                    if (response.code()==200) fetchCompanies()
                }
                override fun onFailure(call:Call<PostResult>,t: Throwable){
                    Log.d(TAG,"Ошибка записи службы доставки",t)
                }
            })
    }


    fun addCompany(company: Company){
        updateCompanies(PostCompany(APPEND_COMPANY, company))
    }
    fun updateCompany(company: Company){
        updateCompanies(PostCompany(UPDATE_COMPANY, company))
    }

    fun deleteCompany(company: Company){
        updateCompanies(PostCompany(DELETE_COMPANY, company))
    }

    fun fetchCouriers(){
        listAPI.getCouriers().enqueue(object: Callback<Couriersed> {
            override fun onFailure(call: Call<Couriersed>, t: Throwable) {
                Log.d(TAG, "Ошибка получения списка курьеров", t)
            }

            override fun onResponse(
                call: Call<Couriersed>,
                response: Response<Couriersed>
            ) {
                if (response.code() == 200) {
                    val couriers = response.body()
                    val items = couriers?.items ?: emptyList()
                    Log.d(TAG, "Получен список курьеров $items")
                    myCoroutineScope.launch {
                        listDB.deleteAllCouriers()
                        for (g in items) {
                            listDB.insertCouriers(g)
                        }
                    }
                    fetchOrders()
                }
            }
        })
    }

    fun addCourier(couriers: Couriers){
        updateCouriers(PostCouriers(APPEND_COURIER, couriers))
    }

    fun updateCourier(couriers: Couriers){
        updateCouriers(PostCouriers(UPDATE_COURIER, couriers))
    }

    fun deleteCourier(couriers: Couriers){
        updateCouriers(PostCouriers(DELETE_COURIER, couriers))
    }


    fun fetchOrders(){
        listAPI.getOrders().enqueue(object : Callback<Ordered> {
            override fun onFailure(call: Call<Ordered>, t : Throwable){
                Log.d(TAG,"Ошибка получения списка заказов",t)
            }
            override fun onResponse(
                call: Call<Ordered>,
                response: Response<Ordered>
            ){
                if(response.code()==200){
                    val Orders = response.body()
                    val items = Orders?.items?: emptyList()
                    Log.d(TAG,"Получен список заказов $items")
                    myCoroutineScope.launch {
                        listDB.deleteAllOrders()
                        for (s in items){
                            listDB.insertOrders(s)
                        }
                    }
                }
            }
        })
    }

    private fun updateOrders(postOrders: PostOrders){
        listAPI.postOrders(postOrders)
            .enqueue(object : Callback<PostResult> {
                override fun onResponse(call: Call<PostResult>, response: Response<PostResult>){
                    if (response.code()==200) fetchOrders()
                }
                override fun onFailure(call: Call<PostResult>, t:Throwable){
                    Log.d(TAG,"Ошибка записи заказа", t)
                }
            })
    }

    fun addOrder(orders: Orders){
        updateOrders(PostOrders(APPEND_ORDER, orders))
    }

    fun deleteOrder(orders: Orders){
        updateOrders(PostOrders(DELETE_ORDER,orders))
    }

    fun updateOrder(orders: Orders){
        updateOrders(PostOrders(UPDATE_ORDER,orders))
    }
    /////////////////////////////////////////////////////////////////////
    private fun registrationQuery(user: User) {
        listAPI.registration(user)
            .enqueue(object : Callback<PostResult>{
                override fun onResponse(call:Call<PostResult>,response:Response<PostResult>){
                    if (response.code()==200)
                        ApplicationList2.setIsAdmin(true)
                }
                override fun onFailure(call:Call<PostResult>,t:Throwable){
                    Log.d(TAG,"Ошибка регистрации", t.fillInStackTrace())
                    ApplicationList2.makeToast("Ошибка авторизации")
                    ApplicationList2.setIsAdmin(false)
                }
            })

    }
    private fun loginQuery(user: UserLogin) {
        listAPI.login(user)
            .enqueue(object : Callback<PostResult>{
                override fun onResponse(call:Call<PostResult>,response:Response<PostResult>){
                    if (response.code()==200) {
                        ApplicationList2.setIsAdmin(true)
                        val a = ApplicationList2.isAdmin
                        Log.d(TAG, "Успех входа ${a}")
                    }
                    if (response.code()==401) {
                        ApplicationList2.makeToast("Неверный логин/пароль")
                    }
                }
                override fun onFailure(call:Call<PostResult>,t:Throwable){
                    Log.d(TAG,"Ошибка входа", t.fillInStackTrace())
                    ApplicationList2.setIsAdmin(false)
                }
            })
    }

    fun registration(user: User) {
        registrationQuery(user)
    }

    fun login(usered: User, user: UserLogin)
    {
        registrationQuery(usered)
        loginQuery(user)
    }

    fun logout() {
        ApplicationList2.setIsAdmin(false)
    }

}