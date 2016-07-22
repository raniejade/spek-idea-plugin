package org.jetbrains.spek.idea

import org.jetbrains.kotlin.psi.KtClassOrObject
import kotlin.properties.Delegates

/**
 * @author Ranie Jade Ramiso
 */
class SpekModel {
    var spec by Delegates.observable<KtClassOrObject?>(null, { prop, old, new ->
        if (new != null && old != new) {

        }
    })
}
