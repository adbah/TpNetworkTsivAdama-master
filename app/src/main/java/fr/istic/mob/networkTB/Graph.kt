package fr.istic.mob.networkTB

data class Graph (
    val nodes : MutableSet<Node> = mutableSetOf(),
    val connexions: MutableSet<Connexion> = mutableSetOf()
)