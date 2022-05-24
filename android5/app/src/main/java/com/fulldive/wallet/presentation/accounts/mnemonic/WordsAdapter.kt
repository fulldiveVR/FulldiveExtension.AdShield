package com.fulldive.wallet.presentation.accounts.mnemonic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fulldive.wallet.extensions.or
import org.adshield.R

class WordsAdapter(
    private val onSuggestionClicked: (String) -> Unit
) : RecyclerView.Adapter<WordsAdapter.MnemonicHolder>(), Filterable {
    var items = emptyList<String>()
    private var filteredItems = listOf<String>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MnemonicHolder {
        return MnemonicHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_suggest_menmonic, viewGroup, false)
        )
    }

    override fun onBindViewHolder(holder: MnemonicHolder, position: Int) {
        val item = filteredItems[position]
        holder.titleTextView.text = item
        holder.itemView.setOnClickListener {
            onSuggestionClicked(item)
        }
    }

    override fun getItemCount(): Int {
        return filteredItems.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString().lowercase()
                return FilterResults().apply {
                    values = charString
                        .takeIf(String::isNotEmpty)
                        ?.let { text ->
                            items.filter { word ->
                                word.startsWith(text) && word != text
                            }
                        }
                        .or(emptyList())
                }
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredItems = results.values as List<String>
                notifyDataSetChanged()
            }
        }
    }

    class MnemonicHolder(
        itemView: View,
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    ) : RecyclerView.ViewHolder(itemView)
}