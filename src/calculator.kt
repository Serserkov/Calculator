package calculator

import java.math.BigInteger
import java.util.Scanner
//checking for wrong input
fun check(splitedArr: MutableList<String>, variablesMap: MutableMap<String, String>):Boolean {
    var out = true
    for(i in 0 until splitedArr.lastIndex)
        when {
            Regex(pattern = """\w+""").matches(splitedArr[i]) && Regex(pattern = """\w+""").matches(splitedArr[i + 1]) -> out = false
            Regex(pattern = """\w+""").matches(splitedArr[i]) && Regex(pattern = "\\(").matches(splitedArr[i + 1]) -> out = false
            Regex(pattern = "[*/]").matches(splitedArr[i]) && Regex(pattern = "[*/]").matches(splitedArr[i + 1]) -> out = false
            Regex(pattern = "[*/+\\-]").matches(splitedArr[i]) && Regex(pattern = "\\)").matches(splitedArr[i + 1]) -> out = false
            Regex(pattern = "\\(").matches(splitedArr[i]) && Regex(pattern = "[*/+\\-]").matches(splitedArr[i + 1]) -> out = false
            Regex(pattern = "[*/]").matches(splitedArr[i]) && Regex(pattern = "[+\\-]").matches(splitedArr[i + 1]) -> out = false
            Regex(pattern = "[+\\-]").matches(splitedArr[i]) && Regex(pattern = "[*/]").matches(splitedArr[i + 1]) -> out = false
            Regex(pattern = "[a-zA-Z]").containsMatchIn(splitedArr[i]) && !variablesMap.containsKey(splitedArr[i]) -> out = false
        }
    return out
}

//transform +- in a row into single sign
fun plusMines(splitedArr: MutableList<String>): MutableList<String> {
    for (i in  0 until splitedArr.lastIndex) {
        if (splitedArr[i] == "-" && splitedArr[i + 1] == "-") {
            splitedArr[i] = " "
            splitedArr[i + 1] = "+"
        }
    }
    splitedArr.removeAll { it == " " }
    for(i in 0 until splitedArr.lastIndex) {
        if (splitedArr[i] == "-" && splitedArr[i + 1] == "+") splitedArr[i + 1] = " "
        if (splitedArr[i] == "+" && Regex(pattern = "[+\\-]").matches(splitedArr[i + 1])) splitedArr[i] = " "
    }
    splitedArr.removeAll { it == " " }
    if (splitedArr[0] == "-") {
        splitedArr[1] = "-" + splitedArr[1]
        splitedArr.removeAt(0)
    }
    return splitedArr
}

//transform infix to postfix
fun toPostfix(splitedArr: MutableList<String>, operand: Map<String, Int>):MutableList<String> {
    val stak = mutableListOf<String>()
    val postfix = mutableListOf<String>()
    for (i in splitedArr) {
        when {
            Regex("""\d""").containsMatchIn(i) -> postfix.add(i)
            //Regex(pattern = "[a-zA-Z]").containsMatchIn(i) -> postfix.add(variablesMap.getValue(i))
            i == "(" -> stak.add(i)
            i == ")" -> {
                while (stak.last() != "(") {
                    postfix.add(stak.last())
                    stak.removeAt(stak.lastIndex)
                    if (stak.isEmpty()) return stak
                }
                stak.removeAt(stak.lastIndex)
            }
            stak.isEmpty() || stak.last() == "(" -> stak.add(i)

            operand[i] ?: error("") > operand[stak.last()] ?: error("") -> stak.add(i)
            //Regex(pattern = """\\/|\\*""").matches(i) && Regex(pattern = """\\+|-""").matches(stak.last()) -> stak.add(i)
            else -> {
                while (stak.isNotEmpty() && stak.last() != "(" && operand[i]!! <= operand[stak.last()]!! ) {
                    postfix.add(stak.last())
                    stak.removeAt(stak.lastIndex)
                }
                stak.add(i)
            }
        }
    }
    if (stak.contains("(")) {
        stak.clear()
        return stak
    }
    while (stak.isNotEmpty()) {
        postfix.add(stak[stak.lastIndex])
        stak.removeAt(stak.lastIndex)
    }
    return postfix
}

//calculating result
fun result (input: MutableList<String>):BigInteger {
    val stak = mutableListOf<BigInteger>()
    var a:BigInteger
    var b:BigInteger
    for (i in input) {
        when {
            Regex("""\d+""").containsMatchIn(i) -> stak.add(i.toBigInteger())
            i == "+" -> {
                a = stak.last()
                stak.removeAt(stak.lastIndex)
                b = stak.last()
                stak.removeAt(stak.lastIndex)
                stak.add(a + b)
            }
            i == "-" -> {
                a = stak.last()
                stak.removeAt(stak.lastIndex)
                b = stak.last()
                stak.removeAt(stak.lastIndex)
                stak.add(b - a)
            }
            i == "*" -> {
                a = stak.last()
                stak.removeAt(stak.lastIndex)
                b = stak.last()
                stak.removeAt(stak.lastIndex)
                stak.add(b * a)
            }
            i == "/" -> {
                a = stak.last()
                stak.removeAt(stak.lastIndex)
                b = stak.last()
                stak.removeAt(stak.lastIndex)
                stak.add(b / a)
            }
        }
    }
    return stak[0]
}

fun main() {
    val scanner = Scanner(System.`in`)
    val operand = mapOf("+" to 0, "-" to 0, "*" to 1, "/" to 1)
    val variablesMap = mutableMapOf<String, String>()
    val splitedStr = StringBuilder()
    var splitedArr = mutableListOf<String>()
    var exit = false
    loop@ while (!exit) {
        splitedArr.clear()
        splitedStr.clear()
        val input = scanner.nextLine()
        //split input string into separately parts: numbers, variables, brackets, symbols of mathematical operations
        for (i in Regex(pattern = "\\+|-|\\w+|\\*|/|\\(|\\)").findAll(input)) {
            splitedStr.append(i.value + " ")
        }
        splitedArr = splitedStr.trim().split(" ").toMutableList()
        when {
            input.isEmpty() -> continue@loop
            splitedArr[0] == "/" -> {
                when {
                    splitedArr.size == 1 -> variablesMap.forEach { (s, i) ->
                        println("$s $i")
                    }
                    splitedArr[1] == "exit" -> {
                        println("Bye!")
                        exit = true
                        continue@loop
                    }
                    splitedArr[1] ==  "help" -> println("The program calculates the sum of numbers")
                    else -> println("Unknown command")
                }
            } //command execution
            input.contains("=") -> {
                when {
                    Regex("\\d").containsMatchIn(splitedArr[0]) -> {
                        println("Invalid identifier")
                        continue@loop
                    }
                    splitedArr.size == 2 -> {
                        when {
                            variablesMap.containsKey(splitedArr[1]) -> {
                                variablesMap[splitedArr[0]] = variablesMap.getValue(splitedArr[1])
                            }
                            Regex(pattern = """\d+""").matches(splitedArr[1]) -> variablesMap[splitedArr[0]] = splitedArr[1]

                            else -> {
                                println("Invalid assignment")
                                continue@loop
                            }
                        }
                    }
                    splitedArr.size == 3 && splitedArr[1] == "-" -> {
                        when {
                            variablesMap.containsKey(splitedArr[2]) -> {
                                variablesMap[splitedArr[0]] = ("-" + variablesMap.getValue(splitedArr[2]))
                            }
                            Regex(pattern = """\d+""").matches(splitedArr[2]) -> variablesMap[splitedArr[0]] = ("-" + splitedArr[2])

                            else -> {
                                println("Invalid assignment")
                                continue@loop
                            }
                        }
                    }
                    else -> {
                        println("Invalid assignment")
                        continue@loop
                    }
                }
            } //assigning a value to a variable
            else -> {
                var change = true
                for(i in splitedArr.indices) {
                    if (Regex(pattern = "[a-zA-Z]").containsMatchIn(splitedArr[i])) {
                        if (variablesMap.containsKey(splitedArr[i])) splitedArr[i] = variablesMap[splitedArr[i]]!! else change = false
                    }
                }//replace variables with their values
                if (!change) {
                    println("Invalid expression")
                    continue@loop
                }
                //than checking for wrong input, transfom infix to postfix and calculating result
                if(check(splitedArr, variablesMap)) {
                    val a = plusMines(splitedArr)
                    val b = toPostfix(a, operand)
                    if(b.isEmpty()) {
                        println("Invalid expression")
                        continue@loop
                    }
                    val c = result(b)
                    println(c)
                } else {
                    println("Invalid expression")
                    continue@loop
                }
            }
        }
    }
}
