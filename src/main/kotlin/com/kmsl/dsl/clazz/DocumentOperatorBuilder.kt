package com.kmsl.dsl.clazz

import com.kmsl.dsl.clazz.DocumentOperator.AND
import com.kmsl.dsl.clazz.DocumentOperator.EXPRESSION
import com.kmsl.dsl.clazz.DocumentOperator.NOR
import com.kmsl.dsl.clazz.DocumentOperator.OR
import org.bson.Document
import org.springframework.data.mapping.toDotPath
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class DocumentOperatorBuilder(
    var isEmbeddedDocument: Boolean = false,
) {
    val documents = mutableListOf<Document>()

    fun and(
        block: DocumentOperatorBuilder.() -> Unit,
    ) {
        val andDocumentOperatorBuilder =
            DocumentOperatorBuilder(this.isEmbeddedDocument)
        andDocumentOperatorBuilder.block()
        val andDocuments = andDocumentOperatorBuilder.documents
        if (andDocuments.isNotEmpty()) documents.add(Document(AND, andDocuments))
    }

    fun or(
        block: DocumentOperatorBuilder.() -> Unit,
    ) {
        val orDocumentOperatorBuilder =
            DocumentOperatorBuilder(this.isEmbeddedDocument)
        orDocumentOperatorBuilder.block()
        val orDocuments = orDocumentOperatorBuilder.documents
        if (orDocuments.isNotEmpty()) documents.add(Document(OR, orDocuments))
    }

    fun nor(
        block: DocumentOperatorBuilder.() -> Unit,
    ) {
        val norDocumentOperatorBuilder =
            DocumentOperatorBuilder(this.isEmbeddedDocument)
        norDocumentOperatorBuilder.block()
        val norDocuments = norDocumentOperatorBuilder.documents
        if (norDocuments.isNotEmpty()) documents.add(Document(NOR, norDocuments))
    }

    fun <T, R> embeddedDocument(
        property: KProperty1<T, R>,
    ) = EmbeddedDocument.of(property)

    inline infix fun <T, reified R> EmbeddedDocument<T, R?>.where(
        noinline block: DocumentOperatorBuilder.() -> Unit,
    ) {
        val embeddedDocumentOperatorBuilder =
            DocumentOperatorBuilder(this@DocumentOperatorBuilder.isEmbeddedDocument)
        embeddedDocumentOperatorBuilder.isEmbeddedDocument = true
        embeddedDocumentOperatorBuilder.block()
        embeddedDocumentOperatorBuilder.isEmbeddedDocument = false
        val whereDocuments = embeddedDocumentOperatorBuilder.documents
        if (whereDocuments.isNotEmpty()) documents.add(Document(AND, whereDocuments))
    }

    fun <T, R> embeddedDocument(
        property: KProperty1<T, List<R>>,
    ) = EmbeddedDocuments.of(property)

    inline infix fun <T, reified R> EmbeddedDocuments<T, R>.elemMatch(
        noinline block: DocumentOperatorBuilder.() -> Unit,
    ) {
        val embeddedDocumentsOperatorBuilder =
            DocumentOperatorBuilder(this@DocumentOperatorBuilder.isEmbeddedDocument)
        embeddedDocumentsOperatorBuilder.block()
        val elemMatchDocuments = embeddedDocumentsOperatorBuilder.documents
        if (elemMatchDocuments.isNotEmpty()) {
            documents.add(
                Document(
                    this.name,
                    Document(
                        DocumentOperator.ELEM_MATCH, Document(AND, elemMatchDocuments)
                    )
                )
            )
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.eq(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(key, value.convertIfId()).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.ne(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.NOT_EQUAL, value.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    infix fun <T, R, C: Any> Field<T, R>.type(
        type: KClass<C>,
    ): AggregationTypeCaster<T, R, C> {
        val castValue = MongoTypeCaster.cast(type)

        return AggregationTypeCaster(castValue, this.key, documents, type)
    }

    inline infix fun <reified T : Any, R> Field<T, R>.lt(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.LESS_THAN, value.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.lte(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.LESS_THAN_EQUAL, value.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.gt(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.GREATER_THAN, value.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.gte(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.GREATER_THAN_EQUAL, value.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.between(
        values: Pair<R?, R?>,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return when {
            values.first == null && values.second == null -> {
                Document()
            }

            values.first == null -> {
                Document(key, Document(DocumentOperator.LESS_THAN, values.second?.convertIfId())).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            values.second == null -> {
                Document(
                    key,
                    Document(DocumentOperator.GREATER_THAN, values.first?.convertIfId())
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            else -> {
                Document(
                    key,
                    Document(DocumentOperator.GREATER_THAN, values.first?.convertIfId()).append(
                        DocumentOperator.LESS_THAN, values.second?.convertIfId()
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.notBetween(
        values: Pair<R?, R?>,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return when {
            values.first == null && values.second == null -> {
                Document()
            }

            values.first == null -> {
                Document(
                    key,
                    Document(
                        DocumentOperator.NOT,
                        Document(DocumentOperator.LESS_THAN, values.second?.convertIfId())
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            values.second == null -> {
                Document(
                    key,
                    Document(
                        DocumentOperator.NOT,
                        Document(DocumentOperator.GREATER_THAN, values.first?.convertIfId())
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            else -> {
                Document(
                    key,
                    Document(
                        DocumentOperator.NOT,
                        Document(DocumentOperator.GREATER_THAN, values.first?.convertIfId()).append(
                            DocumentOperator.LESS_THAN, values.second?.convertIfId()
                        )
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.betweenInclusive(
        values: Pair<R?, R?>,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return when {
            values.first == null && values.second == null -> {
                Document()
            }

            values.first == null -> {
                Document(
                    key,
                    Document(DocumentOperator.LESS_THAN_EQUAL, values.second?.convertIfId())
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            values.second == null -> {
                Document(
                    key,
                    Document(DocumentOperator.GREATER_THAN_EQUAL, values.first?.convertIfId())
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            else -> {
                Document(
                    key,
                    Document(DocumentOperator.GREATER_THAN_EQUAL, values.first?.convertIfId()).append(
                        DocumentOperator.LESS_THAN_EQUAL, values.second?.convertIfId()
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.notBetweenInclusive(
        values: Pair<R?, R?>,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return when {
            values.first == null && values.second == null -> {
                Document()
            }

            values.first == null -> {
                Document(
                    key,
                    Document(
                        DocumentOperator.NOT,
                        Document(DocumentOperator.LESS_THAN_EQUAL, values.second?.convertIfId())
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            values.second == null -> {
                Document(
                    key,
                    Document(
                        DocumentOperator.NOT,
                        Document(DocumentOperator.GREATER_THAN_EQUAL, values.first?.convertIfId())
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }

            else -> {
                Document(
                    key,
                    Document(
                        DocumentOperator.NOT,
                        Document(DocumentOperator.GREATER_THAN_EQUAL, values.first?.convertIfId()).append(
                            DocumentOperator.LESS_THAN_EQUAL, values.second?.convertIfId()
                        )
                    )
                ).apply {
                    if (isNotEmpty()) documents.add(this)
                }
            }
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.`in`(
        values: Iterable<R>,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.IN, values.map { it.convertIfId() }),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.`in`(
        values: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.IN, values.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.notIn(
        values: Iterable<R>,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.NOT_IN, values.map { it.convertIfId() }),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.notIn(
        values: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.NOT_IN, values.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.contains(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.REGEX, value.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.containsIgnoreCase(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.REGEX, value.convertIfId()).append(
                DocumentOperator.OPTIONS,
                DocumentOperator.CASE_INSENSITIVE
            )
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.containsNot(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.NOT, Document(DocumentOperator.REGEX, value.convertIfId()))
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.containsNotIgnoreCase(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(
                DocumentOperator.NOT, Document(DocumentOperator.REGEX, value.convertIfId()).append(
                    DocumentOperator.OPTIONS,
                    DocumentOperator.CASE_INSENSITIVE
                )
            )
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.startsWith(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.REGEX, "^${value.convertIfId()}"),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.endsWith(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.REGEX, "${value.convertIfId()}$"),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.match(
        value: R,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.MATCH, value.convertIfId()),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.all(
        value: Iterable<R>,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.ALL, value.map { it.convertIfId() }),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.size(
        value: Int,
    ): Document {
        val key =
            if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
            else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.SIZE, value),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    inline infix fun <reified T : Any, R> Field<T, R>.exists(
        value: Boolean,
    ): Document {
        val key = if (isEmbeddedDocument) "${this.getClassName(T::class)}.${key.fieldName}"
        else key.fieldName

        return Document(
            key,
            Document(DocumentOperator.EXISTS, value),
        ).apply {
            if (isNotEmpty()) documents.add(this)
        }
    }

    class AggregationTypeCaster<T, R, V: Any>(
        private val castValue: String,
        private val key: KProperty1<T, R>,
        private val documents: MutableList<Document>,
        private val type: KClass<V>,
    ) {
        infix fun lt(
            value: V,
        ) {
            val document = Document(
                EXPRESSION,
                Document(
                    DocumentOperator.LESS_THAN,
                    listOf(
                        Document(castValue, "$"+key.toDotPath()),
                        if (value is Enum<*>) value.name else value
                    )
                )
            )

            if (document.isNotEmpty()) documents.add(document)
        }

        infix fun lte(
            value: V,
        ) {
            val document = Document(
                EXPRESSION,
                Document(
                    DocumentOperator.LESS_THAN_EQUAL,
                    listOf(
                        Document(castValue, "$"+key.toDotPath()),
                        if (value is Enum<*>) value.name else value
                    )
                )
            )

            if (document.isNotEmpty()) documents.add(document)
        }

        infix fun gt(
            value: V,
        ) {
            val document = Document(
                EXPRESSION,
                Document(
                    DocumentOperator.GREATER_THAN,
                    listOf(
                        Document(castValue, "$"+key.toDotPath()),
                        if (value is Enum<*>) value.name else value
                    )
                )
            )

            if (document.isNotEmpty()) documents.add(document)
        }

        infix fun gte(
            value: V,
        ) {
            val document = Document(
                EXPRESSION,
                Document(
                    DocumentOperator.GREATER_THAN_EQUAL,
                    listOf(
                        Document(castValue, "$"+key.toDotPath()),
                        if (value is Enum<*>) value.name else value
                    )
                )
            )

            if (document.isNotEmpty()) documents.add(document)
        }
    }
}