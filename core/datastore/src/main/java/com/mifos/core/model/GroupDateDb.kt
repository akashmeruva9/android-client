package com.mifos.core.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class GroupDateDb : RealmObject {

    @PrimaryKey
    var groupId: Long = 0

    var chargeId: Long = 0

    var day: Int = 0

    var month: Int = 0

    var year: Int = 0

}