package com.androiddevtask.profile

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androiddevtask.R
import com.androiddevtask.database.AppDatabase
import com.androiddevtask.database.Note
import com.androiddevtask.database.NoteDao
import com.androiddevtask.network.ApiClient
import com.androiddevtask.network.ApiInterface
import com.androiddevtask.user.UserResponse
import com.androiddevtask.utils.Constants
import com.google.android.material.textview.MaterialTextView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_userdetails.*
import retrofit2.Retrofit


class UserDetailActivity : AppCompatActivity(),View.OnClickListener {


    var ivBack : ImageView? = null
    var userResponse : UserResponse? = null

    var txTitle : MaterialTextView? = null
    var txFollower : MaterialTextView? = null
    var txFollowing : MaterialTextView? = null
    var txFullName : MaterialTextView? = null
    var txCompany : MaterialTextView? = null
    var txLocation : MaterialTextView? = null
    var txEmail : MaterialTextView? = null
    var etNotes : EditText? = null
    var isNoteAvailable  = false

    lateinit var apiInterface: ApiInterface
    var retrofit: Retrofit? = null
    var compositeDisposable : CompositeDisposable? = null

    private var db: AppDatabase? = null
    private var noteDao: NoteDao? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userdetails)

        db = AppDatabase.getAppDataBase(context = this)
        noteDao = db?.noteDao()


        getIntentData()
        initializations()
        
        loadRemoteData(userResponse?.login)


        Observable.fromCallable {
            noteDao?.getNoteByUser(userResponse!!.login)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it!=null){
                    isNoteAvailable = true
                    etNotes?.setText(it.note)
                }

            },{
                Log.e("Null",it.message.toString())
            })

    }

    private fun loadRemoteData(login: String?) {

        compositeDisposable?.add(
            apiInterface.getUserDetails(login.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ProfileResponse>(){
                    override fun onSuccess(t: ProfileResponse?) {
                        if (t!=null){
                            setUserData(t)
                        }
                    }

                    override fun onError(e: Throwable?) {

                    }

                })

        )
    }

    private fun setUserData(profileResponse: ProfileResponse) {

        txFollowing?.text = "Following : ${profileResponse.following}"
        txFollower?.text = "Followers : ${profileResponse.followers}"



        if (profileResponse.location.isNullOrBlank()){
            txLocation?.text = "Location : Not Available"
        }else{
            txLocation?.text = "Location : ${profileResponse.location}"
        }

        if (profileResponse.name.isNullOrBlank()){
            txFullName?.text = "Name : Not Available"
        }else{
            txFullName?.text = "Name : ${profileResponse.name}"
        }

        if (profileResponse.company.isNullOrBlank()){
            txCompany?.text = "Company : Not Available"
        }else{
            txCompany?.text = "Company : ${profileResponse.company}"
        }
        if (profileResponse.email.isNullOrBlank()){
            txEmail?.text = "Email : Not Available"
        }else{
            txEmail?.text = "Email : ${profileResponse.email}"
        }



    }

    private fun getIntentData() {
        userResponse = intent.getSerializableExtra(Constants.USER_RESPONSE) as UserResponse

    }

    private fun initializations() {
        ivBack = findViewById(R.id.iv_back)
        txTitle = findViewById(R.id.tx_title)
        txFollower = findViewById(R.id.tx_follower)
        txFollowing = findViewById(R.id.tx_following)
        txFullName = findViewById(R.id.tx_fullname)
        txCompany = findViewById(R.id.tx_company)
        txEmail = findViewById(R.id.tx_email)
        txLocation = findViewById(R.id.tx_location)
        etNotes = findViewById(R.id.etNotes)

        etNotes?.setHint("Add Notes for User @${userResponse?.login}")

        etNotes?.imeOptions = EditorInfo.IME_ACTION_DONE
        etNotes?.setRawInputType(InputType.TYPE_CLASS_TEXT)
        txTitle?.text = userResponse?.login

        retrofit = ApiClient.getClient(this)
        if (retrofit!=null){
            apiInterface = retrofit!!.create(ApiInterface::class.java)
        }
        compositeDisposable = CompositeDisposable()
        ivBack?.setOnClickListener(this)
        btSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_back->onBackPressed()
            R.id.btSave->{
                var text = etNotes?.text.toString()
                if (text.isBlank()) text=""
                Observable.fromCallable {
                    if (!isNoteAvailable){
                        noteDao?.insertNote(Note(username = userResponse!!.login,text))
                    }else noteDao?.updateNote(Note(username = userResponse!!.login,text))

                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this,"Notes Saved",Toast.LENGTH_SHORT).show()
                    },{
                        Log.e("Null",it.message.toString())
                    })
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}