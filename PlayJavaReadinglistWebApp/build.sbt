name := """PlayJavaReadingList"""
organization := "com.ratella"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.8"

libraryDependencies += guice

libraryDependencies += ws

libraryDependencies += "com.microsoft.azure" % "azure-cosmosdb" % "2.4.3"



