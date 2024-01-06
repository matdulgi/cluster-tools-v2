package dulgi.clustertools.env

final case class Config(
                       nodes: List[Node],
                       curNode: String,
                       groups: Map[String, List[String]],
                       app: App,
                       )

final case class Node(
                     name: String,
                     hostname: String,
                     port: Int,
                     user: String,
                     )

final case class App(
                    replaceHome: Boolean
                    )