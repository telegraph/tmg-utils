/* Add Here Additional Plugins */

addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver"   % "0.14.0")
addSbtPlugin("net.virtual-void"   % "sbt-dependency-graph" % "0.9.0")


resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.12")
