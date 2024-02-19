package com.example.list.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.list.data.Orders
import com.example.list.myConsts
import com.example.list.repository.AppRepository
import com.example.list1110.R
import com.example.list1110.databinding.FragmentOrderBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat


private const val ARG_PARAM1 = "order_param"


class OrdersFragment : Fragment() {
    private lateinit var orders: Orders
    private lateinit var _binding: FragmentOrderBinding

    val binding
        get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val param1 = it.getString(ARG_PARAM1)
            if (param1 == null)
                orders = Orders()
            else {
                val paramType = object : TypeToken<Orders>(){}.type
                orders = Gson().fromJson<Orders>(param1, paramType)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderBinding.inflate(inflater, container,false)

        val timeDeliveryArray = resources.getStringArray(R.array.TIME_DELIVERY)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeDeliveryArray)
        binding.spTimeDelivery.adapter = adapter
        binding.spTimeDelivery.setSelection(orders.timeDelivery)
        binding.spTimeDelivery.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                orders.timeDelivery = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }


        }
        binding.cvDate.setOnDateChangeListener{
            view, year, month, dayOfMonth ->
            orders.date.time =
                SimpleDateFormat("yyyy.MM.dd").parse("$year.${month+1}.$dayOfMonth")?.time?: orders.date.time
        }
        var timeLen = orders.time.length
        var lowInd = orders.time.indexOf(':')
        if (lowInd > -1) {
            binding.edHour.setText(orders.time.substring(0, lowInd))
            binding.edMinute.setText(orders.time.substring(lowInd + 1, timeLen))
        }
        binding.edAdress.setText(orders.address)
        binding.edOrderDetails.setText(orders.orderDetails)
        binding.cvDate.date = orders.date.time
        binding.EdPhoneNumber.setText(orders.phone)
        binding.edPrice.setText(orders.price)
        binding.edComment.setText(orders.comment)
        binding.btnCancel.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }



        binding.btnSave.setOnClickListener {
            var flag: Boolean = true

            if (binding.edAdress.text.toString().equals("")) {
                flag = false
                binding.edAdress.error = "Адресс должен быть указан"
            }
            if (binding.edHour.text.toString().equals("") || 0 > binding.edHour.text.toString().toInt() || binding.edHour.text.toString().toInt() > 23) {
                flag = false
                binding.edHour.error = "Часы указанны некорректно"
            }
            if (binding.edMinute.text.toString().equals("") || 0 > binding.edMinute.text.toString().toInt() || binding.edMinute.text.toString().toInt() > 59) {
                flag = false
                binding.edMinute.error = "Минуты указанны некорректно"
            }

            orders.address = binding.edAdress.text.toString()
            orders.orderDetails = binding.edOrderDetails.text.toString()

//            Log.d(myConsts.TAG, "!!!!!!VALUE  ${binding.edHour.text.toString().toInt()} and ${binding.edMinute.text.toString().toInt()}")

            orders.time = binding.edHour.text.toString() + ':' + binding.edMinute.text.toString()
            //date чуть выше
            binding.spTimeDelivery.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    orders.timeDelivery = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
            orders.phone = binding.EdPhoneNumber.text.toString()
            orders.price = binding.edPrice.text.toString()
            orders.comment = binding.edComment.text.toString()

            if (flag) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                AppRepository.getInstance().addOrder(orders)
            }
        }

        binding.edButton.setOnClickListener {
            orders.address = binding.edAdress.text.toString()
            orders.orderDetails = binding.edOrderDetails.text.toString()
            orders.time = binding.edHour.text.toString() + ':' + binding.edMinute.text.toString()
            //date чуть выше
            binding.spTimeDelivery.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    orders.timeDelivery = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
            orders.phone = binding.EdPhoneNumber.text.toString()
            orders.price = binding.edPrice.text.toString()
            orders.comment = binding.edComment.text.toString()
            AppRepository.getInstance().updateOrder(orders)
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }
        return binding.root


    }


    companion object {
        @JvmStatic
        fun newInstance(orders: Orders) =
            OrdersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, Gson().toJson(orders))
                }
            }
    }
}