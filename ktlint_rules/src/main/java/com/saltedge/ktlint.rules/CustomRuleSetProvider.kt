/*
 * Copyright (c) 2019 Salt Edge Inc.
 */

package com.saltedge.ktlint.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class CustomRuleSetProvider : RuleSetProvider {

    override fun get(): RuleSet = RuleSet(
        "custom",
        CommentSpacingRule()
    )
}
