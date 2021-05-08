package com.androiddevtask.user

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.androiddevtask.R
import com.androiddevtask.database.NoteDao
import com.androiddevtask.utils.ItemClickListener
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.*
import io.reactivex.rxjava3.schedulers.Schedulers


class UserListAdapter(
    val mContext: Context,
    var mUserList: ArrayList<UserResponse>,
    var clickListener: ItemClickListener,
    val noteDao: NoteDao?
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>(),Filterable {


    var filterList: ArrayList<UserResponse> = ArrayList()
    var userFilterList = ArrayList<UserResponse>()
    val TAG = UserListAdapter::class.simpleName

    init {
        userFilterList = mUserList
    }
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var txUsername = itemView.findViewById<MaterialTextView>(R.id.tx_username)
        var txID = itemView.findViewById<MaterialTextView>(R.id.tx_id)
        var ivUserAvatar = itemView.findViewById<ImageView>(R.id.iv_user_avatar)
        var itemLayout = itemView.findViewById<ConstraintLayout>(R.id.itemLayout)
        var ivNote = itemView.findViewById<ImageView>(R.id.iv_note)


    }

    fun addItems(items: List<UserResponse>) {
        userFilterList.addAll(items)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_user_list,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var userResponse = userFilterList[position]
        holder.txUsername.setText(userResponse.login)
        holder.txID.setText("User ID : ${userResponse.id}")
        Picasso.get().load(userResponse.avatarUrl).into(holder.ivUserAvatar)

        holder.itemLayout.setOnClickListener {
            clickListener.onClick(userResponse, position)
        }

        holder.ivNote.visibility = View.GONE


        fromCallable {
            noteDao?.getNoteByUser(userFilterList[position].login)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                    if (it?.note!!.isNotBlank()){
                        holder.ivNote.visibility = View.VISIBLE
                    }
            },{
                try {
                    it.printStackTrace()
                }catch (e:Exception){

                }
            })




    }

    override fun getItemCount(): Int {
        return userFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filterList = mUserList
                }else {
                    val resultList = ArrayList<UserResponse>()
                    for (row in mUserList) {
                        if (row.login.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            Log.e("add",constraint.toString() +"----"+row.login)
                            resultList.add(row)
                        }
                    }
                    filterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults

            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userFilterList = results?.values as ArrayList<UserResponse>
                notifyDataSetChanged()
            }

        }
    }






}