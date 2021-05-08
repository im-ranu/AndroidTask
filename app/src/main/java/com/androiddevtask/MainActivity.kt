package com.androiddevtask

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevtask.database.AppDatabase
import com.androiddevtask.database.NoteDao
import com.androiddevtask.network.ApiClient
import com.androiddevtask.network.ApiInterface
import com.androiddevtask.profile.UserDetailActivity
import com.androiddevtask.user.UserListAdapter
import com.androiddevtask.user.UserResponse
import com.androiddevtask.utils.Constants
import com.androiddevtask.utils.ItemClickListener
import com.google.android.material.textview.MaterialTextView
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit


class MainActivity : AppCompatActivity(),View.OnClickListener {


    var recyclerView : RecyclerView? =  null
    var userList = ArrayList<UserResponse>()
    var userListAdapter : UserListAdapter? = null
     var compositeDisposable: CompositeDisposable? = null
    var progressBar : ProgressBar? = null
    var loadMoreProgress : ProgressBar? = null
    lateinit var apiInterface: ApiInterface
    var retrofit: Retrofit? = null
    var index = 0
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem = 0
    private  var totalItemCount:Int = 0
    private var layoutManager: LinearLayoutManager? = null
    var clickListener : ItemClickListener? = null
    var loading = false
    var txSearch : MaterialTextView? = null
    var etSearch : EditText? = null
    private var db: AppDatabase? = null
    private var noteDao: NoteDao? = null
    var isSearchActive = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getAppDataBase(context = this)
        noteDao = db?.noteDao()


        clickListener = object : ItemClickListener{
            override fun onClick(userResponse: UserResponse, position: Int) {

                val intent = Intent(this@MainActivity, UserDetailActivity::class.java)
                intent.putExtra(Constants.USER_RESPONSE, userResponse)
                startActivity(intent)
            }

        }
        initialization()

        loadRemoteData()

        onScrollData()

        etSearch?.textChanges()?.subscribe{

             userListAdapter?.filter?.filter(it)

        }



    }


    private fun onScrollData() {
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                totalItemCount = layoutManager!!.getItemCount()
                lastVisibleItem = layoutManager!!.findLastVisibleItemPosition();
                if (!loading
                    && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)
                ) {
                    loadMoreProgress?.visibility = View.VISIBLE
                    loading = true
                    loadRemoteData()
                }
            }

        })
    }

    private fun loadRemoteData() {

        compositeDisposable?.add(
            apiInterface.getUserList(index)
                .doOnSuccess {
                    index = it[it.size - 1].id
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<UserResponse>>() {
                    override fun onSuccess(t: List<UserResponse>?) {
                        if (t != null) {
                            Log.e("id", t[0].id.toString())
                            userList.addAll(t)
                            progressBar?.visibility = View.GONE
                            userListAdapter?.notifyDataSetChanged()
                            loading = false
                            if (loadMoreProgress!!.isVisible) {
                                loadMoreProgress?.visibility = View.GONE
                            }
                        }
                    }

                    override fun onError(e: Throwable?) {
                        Toast.makeText(this@MainActivity, e?.message, Toast.LENGTH_SHORT).show()
                        if (loadMoreProgress!!.isVisible) {
                            loadMoreProgress?.visibility = View.GONE
                        }
                    }

                })
        )



    }

    private fun initialization() {

        retrofit = ApiClient.getClient(this)
        if (retrofit!=null){
            apiInterface = retrofit!!.create(ApiInterface::class.java)
        }

        progressBar = findViewById(R.id.progressBar)
        loadMoreProgress = findViewById(R.id.hz_progressbar)

        txSearch = findViewById(R.id.txSearch)
        txSearch?.setOnClickListener(this)
        etSearch = findViewById(R.id.etSearchText)

        etSearch?.imeOptions  = EditorInfo.IME_ACTION_DONE




        loadMoreProgress?.visibility = View.GONE
        recyclerView = findViewById(R.id.rv_user)
        userListAdapter = UserListAdapter(this, userList, clickListener!!,noteDao)
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        if (recyclerView!=null){
            recyclerView?.layoutManager = layoutManager
            recyclerView?.adapter = userListAdapter
        }

        compositeDisposable = CompositeDisposable()

    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.txSearch -> {
                etSearch?.requestFocus()
                txSearch?.visibility = View.GONE
                etSearch?.visibility = View.VISIBLE
                isSearchActive = true
            }
        }
    }


    override fun onBackPressed() {
        if (isSearchActive)
        {
            etSearch?.getText()?.clear();
            txSearch?.visibility = View.VISIBLE
            etSearch?.visibility = View.GONE
            isSearchActive = false
        }else{
            super.onBackPressed()
        }


    }

    override fun onResume() {
        if (userListAdapter!=null){
            userListAdapter?.notifyDataSetChanged()
        }
        super.onResume()
    }


}