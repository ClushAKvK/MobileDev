package com.example.list

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import com.example.list.data.Orders
import com.example.list.fragments.CompanyFragment
import com.example.list.fragments.CouriersFragment
import com.example.list.fragments.OrdersFragment
import com.example.list.fragments.OrdersViewModel
import com.example.list.interfaces.MainActivityInterface
import com.example.list.repository.AppRepository
import com.example.list1110.R
import com.example.lsit.fragments.LoginFragment

class MainActivity : AppCompatActivity(), MainActivityInterface {
    interface Edit {
        fun append()
        fun update()
        fun delete()
    }

    private lateinit var viewModel: OrdersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onBackPressedDispatcher.addCallback(this ) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                when (activeFragment) {
                    NamesOfFragment.COMPANY -> {
                        finish()
                    }
                    NamesOfFragment.COURIERS -> {
                        activeFragment=NamesOfFragment.COMPANY
                    }
                    NamesOfFragment.ORDERS -> {
                        activeFragment = NamesOfFragment.COURIERS
                    }
                    NamesOfFragment.LOGIN-> {
                        activeFragment=NamesOfFragment.COMPANY
//                        updateMenu(activeFragment)
                    }
                    else -> {}
                }
                updateMenu(activeFragment)
            }
            else {
//                updateMenu(NamesOfFragment.COMPANY)
                finish()
            }
        }
        showFragment(activeFragment, null)
    }

    private var _miAppendCompany:  MenuItem? =null
    private var _miUpdateCompany:  MenuItem? =null
    private var _miDeleteCompany:  MenuItem? =null
    private var _miAppendCourier:  MenuItem? =null
    private var _miUpdateCourier:  MenuItem? =null
    private var _miDeleteCourier:  MenuItem? =null
    private var _miSignup: MenuItem ?= null
    private var _miLogin: MenuItem ?= null
    private var _miExit: MenuItem ?= null


    override fun onCreateOptionsMenu(menu: Menu?):Boolean{
     menuInflater.inflate(R.menu.main_menu, menu)
        _miAppendCompany = menu?.findItem(R.id.miNewCompany)
        _miUpdateCompany = menu?.findItem(R.id.miUpdateCompany)
        _miDeleteCompany = menu?.findItem(R.id.miDeleteCompany)
        _miAppendCourier = menu?.findItem(R.id.miAppendCourier)
        _miUpdateCourier = menu?.findItem(R.id.miUpdateCourier)
        _miDeleteCourier = menu?.findItem(R.id.miDeleteCourier)
        _miLogin = menu?.findItem(R.id.miLogin)
        _miExit = menu?.findItem(R.id.miExit)

        updateMenu(activeFragment)
        return true
    }

    var activeFragment : NamesOfFragment = NamesOfFragment.COMPANY
    private fun updateMenu(fragmentType: NamesOfFragment){
        _miAppendCompany?.isVisible = (
                fragmentType == NamesOfFragment.COMPANY && ApplicationList2.isAdmin)
        _miUpdateCompany?.isVisible = (
                fragmentType == NamesOfFragment.COMPANY && ApplicationList2.isAdmin)
        _miDeleteCompany?.isVisible = (
                fragmentType == NamesOfFragment.COMPANY && ApplicationList2.isAdmin)
        _miAppendCourier?.isVisible = (
                fragmentType == NamesOfFragment.COURIERS && ApplicationList2.isAdmin)
        _miUpdateCourier?.isVisible = (
                fragmentType == NamesOfFragment.COURIERS && ApplicationList2.isAdmin)
        _miDeleteCourier?.isVisible = (
                fragmentType == NamesOfFragment.COURIERS && ApplicationList2.isAdmin)

        _miLogin?.isVisible = (
                !ApplicationList2.isAdmin)
        _miExit?.isVisible = ApplicationList2.isAdmin
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{

        return when (item.itemId){
            R.id.miNewCompany->{
                val fedit:Edit = CompanyFragment.getInstance()
                fedit.append()
                true
            }

            R.id.miUpdateCompany  ->{
                val fedit:Edit = CompanyFragment.getInstance()
                fedit.update()
                true
            }

            R.id.miDeleteCompany->{
                val fedit:Edit = CompanyFragment.getInstance()
                fedit.delete()
                true
            }
            R.id.miAppendCourier->{
                val fedit:Edit = CouriersFragment.getInstance()
                fedit.append()
                true
            }

            R.id.miUpdateCourier  ->{
                val fedit:Edit = CouriersFragment.getInstance()
                fedit.update()
                true
            }

            R.id.miDeleteCourier  ->{
                val fedit:Edit = CouriersFragment.getInstance()
                fedit.delete()
                true
            }

            R.id.miLogin -> {
                showFragment(NamesOfFragment.LOGIN)
                true
            }
            R.id.miExit -> {
                AppRepository.getInstance().logout()
                true
            }
            else-> super.onOptionsItemSelected(item)

        }
    }


    override fun showFragment(fragmentType: NamesOfFragment, orders: Orders?) {
        when (fragmentType){
            NamesOfFragment.COMPANY -> {
                supportFragmentManager
                    .beginTransaction()
//                    .replace(R.id.fcMain, CompanyFragment.getInstance())
                    .replace(R.id.fcMain, CompanyFragment.getInstance())
                    .addToBackStack(null)
                    .commit()
            }
            NamesOfFragment.COURIERS -> {
                supportFragmentManager
                    .beginTransaction()
//                    .replace(R.id.fcMain, CouriersFragment.getInstance())
                    .replace(R.id.fcMain, CouriersFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
            }
            NamesOfFragment.ORDERS -> {
                if (orders != null)
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fcMain, OrdersFragment.newInstance(orders))
                        .addToBackStack(null)
                        .commit()

            }
            NamesOfFragment.LOGIN->{
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fcMain, LoginFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
            }
        }
        activeFragment = fragmentType
        updateMenu(fragmentType)
    }





    override fun updateTitle(newTitle: String) {
        title= newTitle
    }

    override fun onStop(){
//        AppRepository.getInstance().saveData()
        super.onStop()
    }

}