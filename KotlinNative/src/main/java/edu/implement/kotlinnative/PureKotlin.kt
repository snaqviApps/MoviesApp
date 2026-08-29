package edu.implement.kotlinnative

data class Foo(val i: Int)

fun main() {
    val inventory = listOf(
        Product(id = 2, name = "Mouse", category = "Tech", price = 100),
        Product(id = 3, name = "Desk", category = "Tech", price = 250),
        Product(id = 1, name = "Laptop", category = "Tech", price = 500),
        Product(id = 4, name = "Chair", category = "Furniture", price = 150),
        Product(id = 5, name = "Table", category = "Furniture", price = 200),
        Product(id = 6, name = "Sofa", category = "Furniture", price = 500)
    )

    val mapFoo = mapOf(
        -5 to Foo(-5),
        1 to Foo(1),
        2 to Foo(2),
        3 to Foo(3)
    )

    println(mapFoo[-5])
    println("${mapFoo[1] is Foo}")      // "is" checks the runtime type
    println("${mapFoo[0] is Foo}")


//   1. Sort by category
    val affordableTech = inventory
        .filter { it.category == "Tech" && it.price <= 350 }
        .map { it.name }

//   Sort by Price
    val expensiveFurniture = inventory.sortedByDescending { it.price }

//   Group by Category
    val groupByCcat: Grouping<Product, String> = inventory.groupingBy { it.category }.also {
        println("category by name: ${it.eachCount()}")
    }

    println("name: $groupByCcat")

}