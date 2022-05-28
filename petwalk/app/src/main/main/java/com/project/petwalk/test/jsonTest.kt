package com.project.petwalk

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


    fun main()  {
        /*URL*/
        var urlBuilder:StringBuilder = StringBuilder("http://www.pettravel.kr/api/listPart.do")
        /*페이지번호*/
        urlBuilder.append("?" + URLEncoder.encode("page","UTF-8")
                + "=" + URLEncoder.encode("1", "UTF-8"))
        /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("pageBlock","UTF-8")
                + "=" + URLEncoder.encode("10", "UTF-8"));
        /*분야 코드*/
        urlBuilder.append("&" + URLEncoder.encode("partCode","UTF-8")
                +"="+URLEncoder.encode("PC01", "UTF-8"));

        val url = URL(urlBuilder.toString())
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-type", "application/json")

        println("Response code: " + conn.responseCode)

        val rd: BufferedReader = if (conn.responseCode in 200..300) {
            BufferedReader(InputStreamReader(conn.inputStream))
        } else {
            BufferedReader(InputStreamReader(conn.errorStream))
        }
        val sb = java.lang.StringBuilder()
        var line: String?
        while (rd.readLine().also { line = it } != null) {
            sb.append(line)
        }
        rd.close()
        conn.disconnect()
        println(">>>"+sb)
    }
