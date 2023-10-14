package com.example.rfidmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rfidmanager.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var myDatabaseHelper: MyDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        val view = binding.root;
        setContentView(view);

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        // getting the recyclerview by its id

        binding.recyclerView.setLayoutManager(layoutManager)


        myDatabaseHelper = MyDatabaseHelper(this);
        loadTags(myDatabaseHelper)
        val adapter = CustomAdapter(myDatabaseHelper.all);

        // Setting the Adapter with the recyclerview
        binding.recyclerView.adapter = adapter;


        binding.addButton.setOnClickListener{
            val intent = Intent(this, AddTag::class.java);
            startActivity(intent);
        }

    }

    override fun onResume() {
        super.onResume()
        binding = ActivityMainBinding.inflate(layoutInflater);
        val view = binding.root;
        setContentView(view);

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.recyclerView.setLayoutManager(layoutManager)

        val adapter = CustomAdapter(myDatabaseHelper.all)
        binding.recyclerView.adapter = adapter;

        binding.addButton.setOnClickListener{
            val intent = Intent(this, AddTag::class.java);
            startActivity(intent);
        }
    }

    private fun loadTags(localDB : MyDatabaseHelper) {
        val queue = Volley.newRequestQueue(this)

//        var exists = localDB.checkTag("awbafwasdf")
//        exists.toString()
        val stringRequest = StringRequest(Request.Method.GET,
            "http://192.168.100.118/Serwer/getTags.php",
            Response.Listener<String> { s ->
                try {
                    val obj = JSONObject(s)
                    if (!obj.getBoolean("error")) {
                        val array = obj.getJSONArray("tags")

                        for (i in 0..array.length() - 1) {
                            val TagJSON = array.getJSONObject(i)
                            if(!localDB.checkTag(TagJSON.getString("reference_id"))) {
                                localDB.addNew(
                                    TagJSON.getString("reference_id"),
                                    TagJSON.getString("u_id"),
                                    TagJSON.getString("owner_info")
                                )

                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { volleyError -> Toast.makeText(applicationContext, volleyError.message, Toast.LENGTH_LONG).show() })

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add<String>(stringRequest)
    }
}
