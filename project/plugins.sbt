resolvers += Resolver.jcenterRepo

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.19.0")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

// Needed otherwise we get a ClassNotFoundException regarding JAXB when we publish
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"
