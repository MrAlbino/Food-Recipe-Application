package com.ayberkkose.yemektariflerisqlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListeRecyclerAdapter(val yemekListesi:ArrayList<String>,val yemekIdListesi:ArrayList<Int>):RecyclerView.Adapter<ListeRecyclerAdapter.YemekHolder>() {

    class YemekHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)

        return YemekHolder(view)

    }

    override fun onBindViewHolder(holder: YemekHolder, position: Int) {

        holder.itemView.recyclerRowText.text=yemekListesi.get(position)

        holder.itemView.setOnClickListener {

            val action=YemekListeFragmentDirections.actionYemekListeFragmentToYemekTarifleriFragment("recyclerdangeldim",yemekIdListesi.get(position))

            Navigation.findNavController(it).navigate(action)
        }

    }

    override fun getItemCount(): Int {
        return yemekListesi.size
    }
}