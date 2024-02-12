/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

/**
 * Ensure string is not null or empty
 *
 * @receiver String object
 * @return boolean, true if string is not null or empty
 */
fun String?.isPresent(): Boolean = !this.isNullOrEmpty()

/**
 * Ensure string is not null or empty
 *
 * @receiver String object
 * @return boolean, true if string is not null or empty
 */
fun String?.isNotEmptyIfDeclared(): Boolean = this?.isNotEmpty() ?: true
