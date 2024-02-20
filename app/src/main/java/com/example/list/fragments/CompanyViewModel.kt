package com.example.list.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.list.data.Company
import com.example.list.data.ListOfCompany
import com.example.list.myConsts.TAG
import com.example.list.repository.AppRepository

class CompanyViewModel : ViewModel() {

    var companyList : LiveData<List<Company>> = AppRepository.getInstance().listOfCompany
    private var _company: Company? = null
    val company
        get() = _company


    init{
        AppRepository.getInstance().company.observeForever{
            _company=it
        }
    }

    fun deleteCompany(){
        if (company!=null)
            AppRepository.getInstance().deleteCompany(company!!)
    }

    fun appendCompany(facultyName: String){
        val company = Company()
        company.name = facultyName
        AppRepository.getInstance().addCompany(company)
    }

    fun updateCompany(facultyName: String){
        if (_company!=null){
            _company!!.name=facultyName
            AppRepository.getInstance().updateCompany(_company!!)
        }
    }



    fun setCompany(company:Company){
        AppRepository.getInstance().setCurrentCompany(company)

    }
}