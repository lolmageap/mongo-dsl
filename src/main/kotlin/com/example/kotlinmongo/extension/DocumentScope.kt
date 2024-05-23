package com.example.kotlinmongo.extension

import com.example.kotlinmongo.clazz.AndOperatorBuilder
import com.example.kotlinmongo.clazz.Field
import com.example.kotlinmongo.clazz.OrOperatorBuilder
import org.bson.Document
import org.springframework.data.mongodb.core.query.BasicQuery
import kotlin.reflect.KProperty1

fun document(
    function: Document.() -> Document,
): BasicQuery {
    val document = Document()
    val json = document.function().toJson()
    return BasicQuery(json)
}

fun orOperator(
    function: OrOperatorBuilder.() -> Unit,
): BasicQuery {
    val document = Document()
    val orOperatorBuilder = OrOperatorBuilder(document, function).run()
    return BasicQuery(
        orOperatorBuilder.toJson()
    )
}

fun andOperator(
    function: AndOperatorBuilder.() -> Unit,
): BasicQuery {
    val document = Document()
    val orOperatorBuilder = AndOperatorBuilder(document, function).run()
    return BasicQuery(
        orOperatorBuilder.toJson()
    )
}

fun <T, R> Document.field(
    key: KProperty1<T, R>,
): Field<T, R> {
    return Field(key, this)
}

fun Document.copy(): Document {
    return Document(this)
}