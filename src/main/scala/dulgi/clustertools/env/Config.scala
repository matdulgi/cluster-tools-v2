package dulgi.clustertools.env

final case class Config(
                       nodes: List[Node],
                       curNode: String,
                       groups: Map[String, List[String]],
                       )

final case class Node(
                     name: String,
                     hostname: String,
                     port: String,
                     user: String,
                     )