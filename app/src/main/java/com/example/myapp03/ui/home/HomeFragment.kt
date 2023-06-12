package com.example.myapp03.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapp03.R
import com.example.myapp03.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val client = OkHttpClient()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 取得元件的參考
        val stockCodeEditText = view.findViewById<EditText>(R.id.stockCodeEditText)
        val averagePriceEditText = view.findViewById<EditText>(R.id.averagePriceEditText)
        val numberOfSharesEditText = view.findViewById<EditText>(R.id.numberOfSharesEditText)
        val additionalSharesEditText = view.findViewById<EditText>(R.id.additionalSharesEditText)
        val calculateButton = view.findViewById<Button>(R.id.calculateButton)
        val newAveragePriceTextView = view.findViewById<TextView>(R.id.newAveragePriceTextView)
        val currentPriceTextView = view.findViewById<TextView>(R.id.currentPriceTextView)

        // 設置按鈕點擊事件
        calculateButton.setOnClickListener {
            val stockCode = stockCodeEditText.text.toString()
            val averagePriceText = averagePriceEditText.text.toString()
            val numberOfSharesText = numberOfSharesEditText.text.toString()
            val additionalSharesText = additionalSharesEditText.text.toString()

            if (averagePriceText.isEmpty() || numberOfSharesText.isEmpty() || additionalSharesText.isEmpty()) {
                Snackbar.make(view, "請輸入數值", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getCurrentStockPrice(stockCode) { currentPrice ->
                val averagePrice = averagePriceText.toFloat()
                val numberOfShares = numberOfSharesText.toInt()
                val additionalShares = additionalSharesText.toInt()

                activity?.runOnUiThread {
                    // 在主線程中更新 UI 元件
                    val newAveragePrice =
                        ((averagePrice * numberOfShares) + (currentPrice * additionalShares)) /
                                (numberOfShares + additionalShares)

                    newAveragePriceTextView.text = String.format("%.2f", newAveragePrice)
                    currentPriceTextView.text = "最新成交價：$currentPrice"
                }
            }
        }

    }


    private fun getCurrentStockPrice(stockCode: String, callback: (Float) -> Unit) {
        val url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?json=1&delay=0&ex_ch=tse_$stockCode.tw"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 網路請求失敗處理
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                val jsonObject = JSONObject(responseData)
                val msgArray = jsonObject.getJSONArray("msgArray")
                if (msgArray.length() > 0) {
                    val stockData = msgArray.getJSONObject(0)
                    val zValue = stockData.getString("z")
                    val currentPrice = if (zValue.isNotEmpty()) zValue.toFloat() else 0.0f
                    if (responseData != null) {
                        Log.d("Response Data", responseData)
                    }
                    Log.d("Msg Array Length", msgArray.length().toString())

                    // 在這裡可以執行你希望在獲取到股價後進行的操作
                    // 例如，更新 UI 顯示股價等
                    callback(currentPrice)
                }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}