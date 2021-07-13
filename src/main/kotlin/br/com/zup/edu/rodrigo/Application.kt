package br.com.zup.edu.rodrigo

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.edu.rodrigo")
		.start()
}

