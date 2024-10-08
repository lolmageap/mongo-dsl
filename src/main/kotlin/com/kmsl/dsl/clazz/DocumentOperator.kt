package com.kmsl.dsl.clazz

object DocumentOperator {
    const val AND = "\$and"
    const val OR = "\$or"
    const val NOR = "\$nor"
    const val NOT = "\$not"
    const val EQUAL = "\$eq"
    const val NOT_EQUAL = "\$ne"
    const val GREATER_THAN = "\$gt"
    const val GREATER_THAN_EQUAL = "\$gte"
    const val LESS_THAN = "\$lt"
    const val LESS_THAN_EQUAL = "\$lte"
    const val IN = "\$in"
    const val NOT_IN = "\$nin"
    const val EXISTS = "\$exists"
    const val REGEX = "\$regex"
    const val ELEM_MATCH = "\$elemMatch"
    const val OPTIONS = "\$options"
    const val CASE_INSENSITIVE = "i"
    const val MATCH = "\$match"
    const val SIZE = "\$size"
    const val ALL = "\$all"
    const val PROJECT = "\$project"
    const val EXPRESSION = "\$expr"
    const val JOIN = "\$lookup"
    const val UNWIND = "\$unwind"
    const val FROM = "from"
    const val LOCAL_FIELD = "localField"
    const val FOREIGN_FIELD = "foreignField"
    const val AS = "as"
}