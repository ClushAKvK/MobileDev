package com.example.list.fragments

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.list.MainActivity
import com.example.list.NamesOfFragment
import com.example.list.data.Company
import com.example.list.interfaces.MainActivityInterface
import com.example.list1110.R
import com.example.list1110.databinding.FragmentCompanyBinding
import java.lang.Exception



class CompanyFragment : Fragment(), MainActivity.Edit {

    companion object {
        private var INSTANCE : CompanyFragment? =null
        fun getInstance():CompanyFragment{
            if(INSTANCE==null) INSTANCE = CompanyFragment()
            return INSTANCE ?: throw Exception("CompanyFragment не создан")
        }
    }

    private lateinit var viewModel: CompanyViewModel
    private var _binding : FragmentCompanyBinding?= null
    val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCompanyBinding.inflate(inflater, container, false)
        binding.rvCompany.layoutManager=LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivityInterface).updateTitle("Службы доставки")
        viewModel = ViewModelProvider(this).get(CompanyViewModel::class.java)
        viewModel.companyList.observe(viewLifecycleOwner){
            binding.rvCompany.adapter=CompanyListAdapter(it)

        }
    }

    private inner class CompanyListAdapter(private val items: List<Company>)
        : RecyclerView.Adapter<CompanyListAdapter.ItemHolder>(){
        private var lastView: View?=null
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ItemHolder {
            val view = layoutInflater.inflate(R.layout.element_company_list,parent,false)
            return ItemHolder(view)
        }

        override fun getItemCount(): Int=  items.size
        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(items[position])
        }

        private fun updateCurrentView(view:View){
            lastView?.findViewById<ConstraintLayout>(R.id.clCompany)?.setBackgroundColor(
                ContextCompat.getColor(requireContext(),R.color.white))
            view.findViewById<ConstraintLayout>(R.id.clCompany).setBackgroundColor(
                ContextCompat.getColor(requireContext(),R.color.my_blue))
            lastView=view
        }

        private inner class ItemHolder(view:View)
            :RecyclerView.ViewHolder(view){
                private lateinit var company: Company
                fun bind(company: Company){
                    this.company= company
                    if (company==viewModel.company){
                        updateCurrentView(itemView)
                    }

                    val tv = itemView.findViewById<TextView>(R.id.tvCompany)
                    tv.text= company.name
                    tv.setOnClickListener{
                        viewModel.setCompany(company)
                        updateCurrentView(itemView)
                    }

                    tv.setOnLongClickListener{
                        tv.callOnClick()
                        (requireActivity() as MainActivityInterface).showFragment(NamesOfFragment.COURIERS)
                        true
                    }
                }

            }

        }

    override fun append() {
        editCompany()
    }

    override fun update() {
        editCompany(viewModel.company?.name?:"")
    }

    override fun delete() {
        deleteDialog()
    }

    private fun deleteDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление")
            .setMessage("Вы действительно хоитите удалить службу доставки ${viewModel.company?.name?: ""}?")
            .setPositiveButton("Да"){_,_ ->
                viewModel.deleteCompany()
            }
            .setNegativeButton("Нет",null)
            .setCancelable(true)
            .create()
            .show()
    }

    private fun editCompany(companyName:String=""){
        val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input_name,null)
        val messageText = mDialogView.findViewById<TextView>(R.id.tvInfo)
        val inputString= mDialogView.findViewById<EditText>(R.id.etInput)
        inputString.setText(companyName)
        messageText.text="Укажите наименование службы доставки"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Изменение данных")
            .setView(mDialogView)
            .setPositiveButton("Подтверждаю"){_,_ ->
               if (inputString.text.isNotBlank()){
                   if (companyName.isBlank())
                       viewModel.appendCompany(inputString.text.toString())
                   else
                       viewModel.updateCompany(inputString.text.toString())
               }
            }
            .setNegativeButton("Отмена",null)
            .setCancelable(true)
            .create()
            .show()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as MainActivityInterface).updateTitle("Службы доставки")
    }
}