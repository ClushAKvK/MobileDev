package com.example.list.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.collection.emptyLongSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.list.ApplicationList2
import com.example.list.NamesOfFragment
import com.example.list.api.ListAPI
import com.example.list.api.ListConnection
import com.example.list1110.R
import com.example.list.data.Couriers
import com.example.list.data.Orders
import com.example.list.data.Ordered
import com.example.list.database.ListDatabase
import com.example.list.interfaces.MainActivityInterface
import com.example.list.myConsts
import com.example.list.repository.OfflineDBRepository
import com.example.list1110.databinding.FragmentOrdersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

class OrderedFragment : Fragment() {

    companion object {
        private lateinit var couriers: Couriers
        fun newInstance(couriers: Couriers) : OrderedFragment {
            this.couriers = couriers
            return OrderedFragment()
        }
    }



    private lateinit var viewModel: OrdersViewModel

    private lateinit var _binding : FragmentOrdersBinding
    val binding
        get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        binding .rvOrders.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(OrdersViewModel::class.java)
        viewModel.set_Courier_ByAdress(couriers)
        viewModel.ordersList.observe(viewLifecycleOwner){
            binding.rvOrders.adapter = StudentAdapter(it)
        }

        if (!ApplicationList2.isAdmin)
            binding.fabNewOrder.isVisible = false


        binding.fabNewOrder.setOnClickListener{
            editOrder(Orders().apply { courierID = viewModel.couriers.id })
        }
    }
    
    
    private fun deleteDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление")
            .setMessage("Вы действительно хотите удалить накладную?")
            .setPositiveButton("Да"){_, _ ->
                viewModel.deleteOrder()
            }
            .setNegativeButton("Нет", null)
            .setCancelable(true)
            .create()
            .show()
    }

    private fun editOrder(orders: Orders){
        (requireActivity() as MainActivityInterface).showFragment(NamesOfFragment.ORDERS, orders)
        (requireActivity() as MainActivityInterface).updateTitle("Курьер ${viewModel.couriers.name}")
    }

    private inner class StudentAdapter(private val items: List<Orders>)
        : RecyclerView.Adapter<StudentAdapter.ItemHolder>(){

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): StudentAdapter.ItemHolder {
            val view = layoutInflater.inflate(R.layout.element_order_list, parent, false)
            return ItemHolder(view)
        }

        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: StudentAdapter.ItemHolder, position: Int) {
            holder.bind(viewModel.ordersList.value!![position])
        }
        private var lastView : View? = null
        private fun updateCurrentView(view: View){
            val ll = lastView?.findViewById<LinearLayout>(R.id.clOrderButtons)
            ll?.visibility = View.INVISIBLE
            ll?.layoutParams = ll?.layoutParams.apply { this?.width = 1 }
            val ib = lastView?.findViewById<ImageButton>(R.id.ibPhone)
            ib?.visibility = View.INVISIBLE
            ib?.layoutParams = ib?.layoutParams.apply{ this?.width = 1}

            lastView?.findViewById<ConstraintLayout>(R.id.clCompany)?.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white))
            view.findViewById<ConstraintLayout>(R.id.clCompany)?.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.my_green))
            lastView = view
        }


        
        private inner class ItemHolder(view: View)
            : RecyclerView.ViewHolder(view){

                private lateinit var orders: Orders

                fun bind(orders: Orders){
                    this.orders = orders
                    if (orders == viewModel.order)
                        updateCurrentView(itemView)
                    val tvTN = itemView.findViewById<TextView>(R.id.tvAddress)
                    tvTN.text = orders.address

                    val tvD = itemView.findViewById<TextView>(R.id.tvTime)
                    tvD.text = orders.time

                    val tvHC = itemView.findViewById<TextView>(R.id.tvTimeDelivery)
                    val timeDeliveryArray = resources.getStringArray(R.array.TIME_DELIVERY)
                    tvHC.text = timeDeliveryArray.get(orders.timeDelivery)

                    val tvP = itemView.findViewById<TextView>(R.id.tvDate)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                    tvP.text = dateFormat.format(orders.date)

                    val cl = itemView.findViewById<ConstraintLayout>(R.id.clOrder)

                    binding.svSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                        override fun onQueryTextChange(text: String?): Boolean {
                                    text?.let { viewModel.filterItems(it) }
                            return true
                        }

                        override fun onQueryTextSubmit(query: String?): Boolean {
                            query?.let { viewModel.filterItems(it) }
                            return true
                        }
                    })

                    
                    tvTN.setOnLongClickListener{
                        viewModel.set_Courier_ByAdress(couriers)

                        true
                    }
                    tvHC.setOnLongClickListener{
                        viewModel.set_Courier_ByTimeDelivery(couriers)

                        true
                    }
                    tvD.setOnLongClickListener{
                        viewModel.set_Courier_ByTime(couriers)

                        true
                    }
                    tvP.setOnLongClickListener{
                        viewModel.set_Courier_ByDate(couriers)

                        true
                    }
///////////////////////////////////////////////////////////////////////////////////////////////////////

                    cl.setOnClickListener{
                        viewModel.setCurrentOrder(orders)
                        updateCurrentView(itemView)
                    }
                    itemView.findViewById<ImageButton>(R.id.ibEditOrder).setOnClickListener {
                        editOrder(orders)
                    }
                    itemView.findViewById<ImageButton>(R.id.ibDeleteOrder).setOnClickListener {
                        deleteDialog()
                    }

                    val llb = itemView.findViewById<LinearLayout>(R.id.clOrderButtons)
                    llb.visibility = View.INVISIBLE
                    llb?.layoutParams = llb?.layoutParams.apply { this?.width = 1 }
                    val ib = itemView.findViewById<ImageButton>(R.id.ibPhone)
                    ib.visibility = View.INVISIBLE
                    val a = ApplicationList2.isAdmin
                    if (a == true) {
                        cl.setOnLongClickListener {
                            cl.callOnClick()
                            llb.visibility = View.VISIBLE
                            if (orders.phone.isNotBlank())
                                ib.visibility = View.VISIBLE
                            MainScope().launch {
                                val lp = llb?.layoutParams
                                lp?.width = 1
                                val ip = ib.layoutParams
                                ip.width = 1
                                while (lp?.width!! < 350) {
                                    lp?.width = lp?.width!! + 35
                                    llb?.layoutParams = lp
                                    ip.width = ip.width + 10
                                    if (ib.visibility == View.VISIBLE)
                                        ib.layoutParams = ip
                                    delay(50)
                                }
                            }
                            true

                        }

                    }
                    else
                        cl.setOnLongClickListener {
                            cl.callOnClick()
                            llb.visibility = View.INVISIBLE
                            if (orders.phone.isNotBlank())
                                ib.visibility = View.VISIBLE
                            MainScope().launch {
                                val lp = llb?.layoutParams
                                lp?.width = 1
                                val ip = ib.layoutParams
                                ip.width = 1
                                while (lp?.width!! < 350) {
                                    lp?.width = lp?.width!! + 35
                                    llb?.layoutParams = lp
                                    ip.width = ip.width + 10
                                    if (ib.visibility == View.VISIBLE)
                                        ib.layoutParams = ip
                                    delay(50)
                                }
                            }
                            true

                        }

                    itemView.findViewById<ImageButton>(R.id.ibPhone).setOnClickListener {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel: ${orders.phone}"))
                            startActivity(intent)
                        } else {
                            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 2)
                        }
                    }
                    binding.svSearch.setOnCloseListener {
                        // Обработчик закрытия SearchView
                        binding.svSearch.setQuery("", false)
                        binding.svSearch.clearFocus()
                        binding.svSearch.onActionViewCollapsed()
                        var listAPI = ListConnection.getClient().create(ListAPI::class.java)
                        val listDB by lazy { OfflineDBRepository(
                            ListDatabase.getDatabase(
                                ApplicationList2.context).listDAO()) }

                        val myCoroutineScope = CoroutineScope(Dispatchers.Main)
                        fun fetchOrders(){
                            listAPI.getOrders().enqueue(object : Callback<Ordered> {
                                override fun onFailure(call: Call<Ordered>, t : Throwable){
                                    Log.d(myConsts.TAG,"Ошибка получения списка заказов",t)
                                }
                                override fun onResponse(
                                    call: Call<Ordered>,
                                    response: Response<Ordered>
                                ){
                                    if(response.code()==200){
                                        val orders = response.body()
                                        val items = orders?.items?: emptyList()
                                        Log.d(myConsts.TAG,"Получен список заказов $items")
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
                        fetchOrders()
                        true
                    }
                }

            }
        }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OrdersViewModel::class.java)
        // TODO: Use the ViewModel
    }

}