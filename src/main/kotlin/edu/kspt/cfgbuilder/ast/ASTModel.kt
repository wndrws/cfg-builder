package edu.kspt.cfgbuilder.ast

import java.util.*

interface Component

interface Statement : Component

typealias Statements = List<Statement>

data class IfStatement(val testToBranch: LinkedHashMap<String, Statements>,
                       val elseBranch: Statements = emptyList()) : Statement

data class LoopStatement(val condition: String,
                         val body: Statements,
                         val elseBranch: Statements = emptyList()) : Statement

data class ReturnStatement(val returnVariation: String, val returnValue: String) : Statement

data class SimpleStatement(val text: String) : Statement

class BreakStatement : Statement { override fun toString() = "BreakStatement()" }

class ContinueStatement : Statement { override fun toString() = "ContinueStatement()" }

data class Function(val definition: String, val body: Statements) : Component

data class Program(val name: String, val body: List<Component>)
