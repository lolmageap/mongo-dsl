package com.example.kotlinmongo

import com.example.kotlinmongo.collection.Author
import com.example.kotlinmongo.extension.document
import com.example.kotlinmongo.extension.field
import com.example.kotlinmongo.extension.orderBy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.BasicQuery

class OrderByTest: StringSpec({
    "단일 orderBy 정렬 테스트" {
        val result = document {
            and(
                { field(Author::name) eq "John" },
                { field(Author::age) eq 18 },
            )
        }.orderBy(Author::name).desc()

        result shouldBe BasicQuery("{ \"\$and\" : [{ \"name\" : \"John\"}, { \"age\" : 18}]}")
            .with(Sort.by(Sort.Direction.DESC, "name"))
    }

    "다중 orderBy 정렬 테스트" {
        val result = document {
            and(
                { field(Author::name) eq "John" },
                { field(Author::age) eq 18 },
            )
        }.orderBy(Author::name).desc()
            .orderBy(Author::age).asc()

        result shouldBe BasicQuery("{ \"\$and\" : [{ \"name\" : \"John\"}, { \"age\" : 18}]}")
            .with(Sort.by(Sort.Direction.DESC, "name").and(Sort.by(Sort.Direction.ASC, "age")))
    }
})