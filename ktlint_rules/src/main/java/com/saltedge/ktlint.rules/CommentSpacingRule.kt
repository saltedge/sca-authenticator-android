/*
Copyright 2019 Pinterest, Inc.
Copyright 2016-2019 Stanley Shyiko

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.saltedge.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.prevLeaf
import com.pinterest.ktlint.core.ast.upsertWhitespaceBeforeMe
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * @see https://github.com/pinterest/ktlint/blob/master/ktlint-ruleset-standard/src/main/kotlin/com/pinterest/ktlint/ruleset/standard/CommentSpacingRule.kt
 */
class CommentSpacingRule : Rule("comment-spacing") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node is PsiComment && node is LeafPsiElement && node.getText().startsWith("//")) {
            val prevLeaf = node.prevLeaf()
            if (prevLeaf !is PsiWhiteSpace && prevLeaf is LeafPsiElement) {
                emit(node.startOffset, "Missing space before //", true)
                if (autoCorrect) {
                    node.upsertWhitespaceBeforeMe(" ")
                }
            }
            val text = node.getText()
            if (text.length != 2 &&
                !text.startsWith("// ") &&
                !text.startsWith("//noinspection") &&
                !text.startsWith("//region") &&
                !text.startsWith("//endregion") &&
                !text.startsWith("//language=")
            ) {
                emit(node.startOffset, "Missing space after //", true)
                if (autoCorrect) {
                    node.rawReplaceWithText("// " + text.removePrefix("//"))
                }
            }
        }
    }
}
