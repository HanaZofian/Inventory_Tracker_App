package com.bignerdranch.android.inventoryIntent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ItemListFragment"

class ItemListFragment : Fragment() {
    /**
     * Required interface for hosting activities
     *
     */
    interface Callbacks {
        fun onItemSelected(itemId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var searchView: SearchView

    private val itemListViewModel: ItemListViewModel by lazy {
        ViewModelProviders.of(this).get(ItemListViewModel::class.java)
    }

    private lateinit var itemRecyclerView: RecyclerView

    private var adapter: itemAdapter? = itemAdapter(emptyList())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        itemRecyclerView = view.findViewById(R.id.item_recycler_view) as RecyclerView
        itemRecyclerView.layoutManager = LinearLayoutManager(context)
        itemRecyclerView.adapter = adapter


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemListViewModel.itemListLiveData.observe(
            viewLifecycleOwner,
            Observer { items ->
                items?.let { Log.i(TAG, "Got items ${items.size}") }
                updateUI(items)
            })
    }



    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_item_list, menu)
        val searchItem = menu.findItem(R.id.search_item)
        var searchView = searchItem?.actionView as? SearchView


        // Set up the search listener
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform search when the user submits the query (e.g., presses search button on keyboard)
                searchView?.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform search as the user types
                newText?.let { query ->
                    if (query.isNotEmpty()) {
                        searchItems(query)}

                    else {
                        // If the search query is empty, show all items (clear search results)
                        updateUI(itemListViewModel.getItems().value ?: emptyList())
                    }
                }
                return true
            }
        })
    }



    private fun searchItems(query: String) {
        val searchQuery = "%$query%"
        itemListViewModel.searchItemsByTitle(searchQuery).observe(
            viewLifecycleOwner,
            Observer { items ->
                items?.let { Log.i(TAG, "Got items ${items.size}") }
                updateUI(items)
            }
        )



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_item -> {
                val item = Item()
                itemListViewModel.addItem(item)
                callbacks?.onItemSelected(item.id)
                true
            }


            android.R.id.home -> {
                searchView.setQuery("", false)
                searchView.clearFocus()
                updateUI(itemListViewModel.getItems().value ?: emptyList())
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(items: List<Item>) {
        adapter = itemAdapter(items)
        itemRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): ItemListFragment {
            return ItemListFragment()
        }
    }


    private inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private val titleTextView: TextView = itemView.findViewById(R.id.item_model) as TextView
        private val dateTextView: TextView = itemView.findViewById(R.id.item_date) as TextView
        private val solvedImageView: ImageView =
            itemView.findViewById(R.id.item_shipment) as ImageView
        private lateinit var item: Item

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Item) {
            this.item = item
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            titleTextView.text = item.model
            val addedDateText = getString(R.string.added_date, dateFormat.format(item.date))
            dateTextView.text = addedDateText

            solvedImageView.visibility = if (item.inShipment) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        override fun onClick(v: View?) {
            callbacks?.onItemSelected(item.id)
        }


    }

    private inner class itemAdapter(var items: List<Item>) :
        RecyclerView.Adapter<ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val view = layoutInflater.inflate(R.layout.list_item_inventory, parent, false)
            return ItemHolder(view)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int = items.size

    } }