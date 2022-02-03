package com.example.declarativeclientpublisher

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("com.example.declarativeclientpublisher")
		.start()
}

