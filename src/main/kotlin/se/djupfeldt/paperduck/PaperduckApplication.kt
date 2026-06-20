package se.djupfeldt.paperduck

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaperduckApplication

fun main(args: Array<String>) {
	runApplication<PaperduckApplication>(*args)
}
