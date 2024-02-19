package com.example.list.fragments

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.list.MainActivity
import com.example.list.data.Couriers
import com.example.list.interfaces.MainActivityInterface
import com.example.list1110.R
import com.example.list1110.databinding.FragmentCourierBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CouriersFragment : Fragment(), MainActivity.Edit {

    companion object {
        private var INSTANCE : CouriersFragment? = null
        fun getInstance(): CouriersFragment{
            if (INSTANCE == null) INSTANCE = CouriersFragment()
            return INSTANCE ?: throw Exception("CourierFragment не создан")
        }
        fun newInstance(): CouriersFragment{
            INSTANCE = CouriersFragment()
            return INSTANCE!!
        }
    }

    private lateinit var viewModel: CouriersViewModel
    private var tabPosition : Int = 0
    private lateinit var _binding: FragmentCourierBinding
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCourierBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CouriersViewModel::class.java)
        val ma = (requireActivity() as MainActivityInterface)
        ma.updateTitle("Служба доставки \"${viewModel.company?.name}\"")

        viewModel.couriersList.observe(viewLifecycleOwner){
            createUI(it)
        }
    }

    private inner class GroipPageAdapter(fa: FragmentActivity, private val makers: List<Couriers>?): FragmentStateAdapter(fa){
        override fun getItemCount(): Int {
            return (makers?.size?: 0)
        }

        override fun createFragment(position: Int): Fragment {
            return OrderedFragment.newInstance(makers!![position])
        }
    }

    private fun createUI(couriersList: List<Couriers>){
        binding.tlCourier.clearOnTabSelectedListeners()
        binding.tlCourier.removeAllTabs()

        for (i in 0 until (couriersList.size)){
            binding.tlCourier.addTab(binding.tlCourier.newTab().apply {
                text = couriersList.get(i).name
            })
        }
        val adapter = GroipPageAdapter(requireActivity(), couriersList)
        binding.vpCourier.adapter=adapter
        TabLayoutMediator(binding.tlCourier, binding.vpCourier, true, true){
                tab, pos ->
            tab.text = couriersList.get(pos).name
        }.attach()
        tabPosition = 0
        if (viewModel.courier != null)
            tabPosition = if (viewModel.getCurrentListPosition>=0)
                viewModel.getCurrentListPosition
            else
                0
        viewModel.setCurrentCourier(tabPosition)
        binding.tlCourier.selectTab(binding.tlCourier.getTabAt(tabPosition), true)

        binding.tlCourier.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabPosition = tab?.position!!
                viewModel.setCurrentCourier(couriersList[tabPosition])
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun append() {
        editGroup()
    }

    override fun update() {
        editGroup(viewModel.courier?.name?: "")
    }

    override fun delete(){
        deleteDialog()
    }

    private fun deleteDialog(){
        if (viewModel.courier == null) return
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление")
            .setMessage("Вы действительно хотите удалить курьера ${viewModel.courier?.name?:""}?")
            .setPositiveButton("ДА"){ _, _ ->
                viewModel.deleteCourier()
            }
            .setNegativeButton("НЕТ", null)
            .setCancelable(true)
            .create()
            .show()
    }

    private fun editGroup(groupName: String = ""){
        val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input_name, null)
        val messageText = mDialogView.findViewById<TextView>(R.id.tvInfo)
        val inputString = mDialogView.findViewById<TextView>(R.id.etInput)
        inputString.setText(groupName)
        messageText.text = "Укажите наименование курьера"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("ИЗМЕНЕНИЕ ДАННЫХ")
            .setView(mDialogView)
            .setPositiveButton("Подтверждаю"){ _, _ ->
                if (inputString.text.isNotBlank()){
                    if (groupName.isBlank())
                        viewModel.appendCourier(inputString.text.toString())
                    else
                        viewModel.updateCourier(inputString.text.toString())
                }

            }
            .setNegativeButton("Отмена", null)
            .setCancelable(true)
            .create()
            .show()

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CouriersViewModel::class.java)
        // TODO: Use the ViewModel
    }

}