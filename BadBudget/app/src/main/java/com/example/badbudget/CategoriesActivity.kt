package com.example.badbudget

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.badbudget.models.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriesActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        // Back arrow
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()  // or navigate to wherever makes sense
        }

        // RecyclerView + adapter
        adapter = CategoryAdapter { category ->
            // delete by Firestore document ID
            FirestoreService.deleteCategory(category.id) { success ->
                if (success) {
                    Toast.makeText(this, "Category removed", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<RecyclerView>(R.id.recyclerViewCategories).apply {
            layoutManager = LinearLayoutManager(this@CategoriesActivity)
            adapter = this@CategoriesActivity.adapter
        }

        // FAB to add new
        findViewById<FloatingActionButton>(R.id.fabAddCategory)
            .setOnClickListener { showAddDialog() }

        // initial load
        loadCategories()
    }

    private fun loadCategories() {
        FirestoreService.getCategories(UserSession.id(this)) { list ->
            adapter.submitList(list)
        }
    }

    private fun showAddDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val newCat = Category(
                        id = "",               // empty → Firestore will generate
                        name = name,
                        userId = UserSession.id(this)
                    )
                    FirestoreService.addCategory(newCat) { success ->
                        if (success) {
                            Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                            loadCategories()
                        } else {
                            Toast.makeText(this, "Add failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
