package com.example.rfidmanager

import android.app.PendingIntent
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rfidmanager.databinding.ActivityAddTagBinding
import org.json.JSONException
import org.json.JSONObject
import java.net.InetAddress
import java.nio.charset.Charset


private lateinit var binding: ActivityAddTagBinding

class AddTag : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private lateinit var pendingIntent: PendingIntent

    private lateinit var intentFiltersArray: Array<IntentFilter>
    private lateinit var techListsArray: Array<Array<String>>
    private var nfcAdapter: NfcAdapter? = null
    private var tag: Tag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTagBinding.inflate(layoutInflater);
        val view = binding.root;
        setContentView(view);

        binding.addButton.setOnClickListener {

            syncDb()
            val myDatabaseHelper = MyDatabaseHelper(this);
            if(binding.referenceId.text.toString().isNotEmpty())

                myDatabaseHelper.addNew(
                    binding.referenceId.text.toString(),
                    binding.uid.text.toString(),
                    binding.ownerInfo.text.toString()
                )


        }
    }

    override fun onResume() {
        super.onResume()
        if (NfcAdapter.getDefaultAdapter(this) != null) {
            NfcAdapter.getDefaultAdapter(this).enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                null
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (NfcAdapter.getDefaultAdapter(this) != null) {
            NfcAdapter.getDefaultAdapter(this).disableReaderMode(this)
        }
    }

    override fun onTagDiscovered(tag: Tag) {
        // Seperate thread
        binding.uid.setText(bytes2Hex(tag.getId()));
    }

    fun bytes2Hex(bytes: ByteArray?): String? {
        val ret = StringBuilder()
        if (bytes != null) {
            for (b in bytes) {
                ret.append(String.format("%02X", b.toInt() and 0xFF))
            }
        }
        return ret.toString()
    }

    private fun syncDb(){

        val queue = Volley.newRequestQueue(this)
        val url = "http://192.168.100.118/Serwer/saveTag.php"

        val stringReq : StringRequest =
            object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.toString()
                    Log.d("API", strResp)
                },
                Response.ErrorListener { error ->
                    Log.d("API", "error => $error")
                }
            ){
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("reference_id", binding.referenceId.text.toString(),)
                    params.put("u_id", binding.uid.text.toString(),)
                    params.put("owner_info", binding.ownerInfo.text.toString(),)
                    return params
                }
            }
        queue.add(stringReq)
    }


}