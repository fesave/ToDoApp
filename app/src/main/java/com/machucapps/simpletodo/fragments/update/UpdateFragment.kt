package com.machucapps.simpletodo.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.machucapps.simpletodo.R
import com.machucapps.simpletodo.data.models.ToDoData
import com.machucapps.simpletodo.data.viewmodel.ToDoViewModel
import com.machucapps.simpletodo.databinding.FragmentUpdateBinding
import com.machucapps.simpletodo.fragments.SharedViewModel

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private val sharedViewModel: SharedViewModel by viewModels()
    private val toDoViewModel: ToDoViewModel by viewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Data binding
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        binding.args = args

        //Set Menu
        setHasOptionsMenu(true)

        //Spinner Item Selected Listener
        binding.currentPrioritiesSpinner.onItemSelectedListener = sharedViewModel.listener

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateItem() {
        val title = binding.currentTitleEt.text.toString()
        val description = binding.currentDescriptionEt.text.toString()
        val priority = binding.currentPrioritiesSpinner.selectedItem.toString()

        val validation = sharedViewModel.verifyDataFromUser(title, description)
        if (validation) {
            val updateItem = ToDoData(
                args.currentItem.id,
                title,
                sharedViewModel.parsePriority(priority),
                description
            )
            toDoViewModel.updateData(updateItem)
            Toast.makeText(
                requireContext(),
                getString(R.string.successfully_updated_text),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.fill_out_all_fields),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //Show AlertDialog to confirm removal
    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setPositiveButton(getString(R.string.yes_button)) { _, _ ->
                toDoViewModel.deleteData(args.currentItem)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.successfully_item_removed),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
            setNegativeButton(getString(R.string.no_button)) { _, _ -> }
            setTitle(getString(R.string.delete_item_title, args.currentItem.title))
            setMessage(getString(R.string.delete_item_message))
            create().show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}