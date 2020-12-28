package com.machucapps.simpletodo.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.google.android.material.snackbar.Snackbar
import com.machucapps.simpletodo.R
import com.machucapps.simpletodo.data.models.ToDoData
import com.machucapps.simpletodo.data.viewmodel.ToDoViewModel
import com.machucapps.simpletodo.databinding.FragmentListBinding
import com.machucapps.simpletodo.fragments.SharedViewModel
import com.machucapps.simpletodo.fragments.list.adapter.ListAdapter
import com.machucapps.simpletodo.utils.hideKeyBoard
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val toDoViewModel: ToDoViewModel by viewModels()
    private val shareViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.sharedViewModel = shareViewModel

        //Setup recycler view
        setUpRecyclerView()

        //Observe LiveData
        toDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            shareViewModel.checkIfDatabaseIsEmpty(data)
            adapter.setData(data)
        })

        //Set Menu
        setHasOptionsMenu(true)

        //Hide soft keyboard
        hideKeyBoard(requireActivity())

        return binding.root
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = SlideInUpAnimator().apply { addDuration = 300 }

        // Swipe to delete
        swipeToDelete(recyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]

                //Delete Item
                toDoViewModel.deleteData(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)

                //Restore Deleted Item
                restoreDeletedData(viewHolder.itemView, deletedItem)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedData(view: View, deleteItem: ToDoData) {
        val snackBar = Snackbar.make(
            view,
            getString(R.string.item_has_been_deleted),
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction(getString(R.string.undo)) {
            toDoViewModel.insetData(deleteItem)
        }

        snackBar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> toDoViewModel.sortByHighPriority.observe(this, Observer {
                adapter.setData(it)
            })
            R.id.menu_priority_low -> toDoViewModel.sortByLowPriority.observe(this, Observer {
                adapter.setData(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        toDoViewModel.searchDatabase(searchQuery).observe(this, Observer { list ->
            list?.let {
                adapter.setData(it)
            }
        })
    }

    //Show AlertDialog to confirm removal all items
    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setPositiveButton(getString(R.string.yes_button)) { _, _ ->
                toDoViewModel.deleteAll()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.successfully_item_removed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            setNegativeButton(getString(R.string.no_button)) { _, _ -> }
            setTitle(getString(R.string.delete_items_title))
            setMessage(getString(R.string.delete_items_message))
            create().show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}