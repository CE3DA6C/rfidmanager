package com.example.rfidmanager


import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rfidmanager.databinding.ActivityAddTagBinding
import com.example.rfidmanager.databinding.ActivityEditTagBinding

private lateinit var binding: ActivityEditTagBinding

class EditTag : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private lateinit var binding: ActivityEditTagBinding
    private lateinit var db: MyDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTagBinding.inflate(layoutInflater);
        val view = binding.root;
        setContentView(view);

        db = MyDatabaseHelper(this)
        var tagId = intent.getIntExtra("tag_id", -1)

        if(tagId == -1){
            return
        }
        val tag = db.getTagById(tagId)

        binding.referenceId.setText(tag.reference_id)
        binding.uid.setText(tag.u_id)
        binding.ownerInfo.setText(tag.owner_info)

        binding.saveButton.setOnClickListener {

            val myDatabaseHelper = MyDatabaseHelper(this);
            myDatabaseHelper.updateTag(
                tagId,
                binding.referenceId.text.toString(),
                binding.uid.text.toString(),
                binding.ownerInfo.text.toString()
            )
            finish()

            Toast.makeText(this, "Saved the changes", Toast.LENGTH_SHORT).show()
        }

        binding.deleteButton.setOnClickListener{
            val myDatabaseHelper = MyDatabaseHelper(this);
            myDatabaseHelper.deleteTag(tagId)
            finish()
            Toast.makeText(this, "Tag deleted", Toast.LENGTH_SHORT).show()
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
        val url = "http://192.168.100.118/Serwer/updateTag.php"

        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
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