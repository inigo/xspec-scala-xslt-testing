package net.surguy.xspec

import xml.NodeSeq

case class XspecResult(scenarios: Seq[Scenario]) {
  def allScenarios: Seq[Scenario] = (for (s <- scenarios) yield s.allScenarios).flatten

  def allTests: Seq[XspecTest] = allScenarios.map(_.result).flatten

  def isSuccessful: Boolean = allTests.forall(_.success)
}

case class Scenario(label:String, childScenarios: Seq[Scenario], result: Option[XspecTest]) {
  def allScenarios: Seq[Scenario] = List(this) ++ (for (s <- childScenarios) yield s.allScenarios).flatten
}

case class XspecTest(label:String,  success: Boolean, expect: NodeSeq, actual: NodeSeq)