package com.example.text.data.network

import com.example.text.data.network.dto.CourseSuggestionDto
import com.example.text.data.network.dto.SemesterInfoDto
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val query = request.url.queryParameter("q") ?: ""

        val json = when {
            path.contains("api/courses/suggestions") -> getSuggestions(query)
            path.contains("api/semester/info") -> getSemesterInfo()
            path.contains("api/courses") -> getAllCourses()
            else -> """[]"""
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun getSuggestions(q: String): String {
        val allCourses = listOf(
            Triple("CS101", "数据结构与算法", "张老师"),
            Triple("CS102", "操作系统原理", "李老师"),
            Triple("CS201", "计算机网络", "王老师"),
            Triple("CS202", "编译原理", "赵老师"),
            Triple("CS301", "人工智能基础", "陈老师"),
            Triple("CS302", "机器学习", "刘老师"),
            Triple("MATH201", "高等数学", "周老师"),
            Triple("MATH202", "线性代数", "吴老师"),
            Triple("PHYS101", "大学物理", "杨老师"),
            Triple("ENG101", "大学英语", "黄老师"),
            Triple("CS401", "软件工程", "孙老师"),
            Triple("CS402", "数据库系统", "钱老师"),
            Triple("CS403", "Web开发技术", "何老师"),
            Triple("CS404", "移动应用开发", "林老师"),
            Triple("CS405", "计算机图形学", "郭老师")
        )
        val filtered = if (q.isEmpty()) allCourses else allCourses.filter {
            it.first.contains(q, ignoreCase = true) || it.second.contains(q, ignoreCase = true)
        }
        val list = filtered.map {
            """{"id":"${it.first}","name":"${it.second}","teacher":"${it.third}","credits":3,"description":"${it.second}课程"}"""
        }.joinToString(",")
        return "[$list]"
    }

    private fun getSemesterInfo(): String {
        return """{"semester":"2025-2026-2","startDate":"2026-02-23","endDate":"2026-07-05","weekCount":19,"currentWeek":18}"""
    }

    private fun getAllCourses(): String {
        return """[{"id":"1","name":"数据结构与算法","teacher":"张老师","credits":3,"semester":"2025-2026-2"}]"""
    }
}
