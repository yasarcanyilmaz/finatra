scala_library(
    sources = rglobs("*.scala"),
    compiler_option_sets = {"fatal_warnings"},
    provides = scala_artifact(
        org = "com.twitter",
        name = "finatra-streams-queryable-thrift",
        repo = artifactory,
    ),
    strict_deps = False,
    dependencies = [
        "finatra/inject/inject-core",
        "finatra/inject/inject-server",
        "finatra/inject/inject-slf4j",
        "finatra/inject/inject-utils",
        "finatra/kafka-streams/kafka-streams-static-partitioning/src/main/scala",
        "finatra/kafka-streams/kafka-streams/src/main/java",
        "finatra/thrift",
    ],
    exports = [
        "finatra/inject/inject-core",
        "finatra/inject/inject-server",
        "finatra/inject/inject-slf4j",
        "finatra/inject/inject-utils",
        "finatra/kafka-streams/kafka-streams/src/main/java",
        "finatra/thrift",
    ],
)
