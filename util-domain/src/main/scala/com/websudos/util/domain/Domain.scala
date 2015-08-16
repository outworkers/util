package com.websudos.util.domain


private[util] object Definitions {

  abstract class GeneratedValue[T](val value: T)

  case class EmailAddress(address: String) extends GeneratedValue(address)
  case class FirstName(name: String) extends GeneratedValue(name)
  case class LastName(name: String) extends GeneratedValue(name)
  case class FullName(name: String) extends GeneratedValue(name)
  case class CountryCode(code: String) extends GeneratedValue(code)

  case class Country(country: String) extends GeneratedValue(country)
  case class City(city: String) extends GeneratedValue(city)
  case class ProgrammingLanguage(language: String) extends GeneratedValue(language)

  case class LoremIpsum(word: String) extends GeneratedValue(word)
  case class Url(url: String) extends GeneratedValue(url)
  case class Domain(domain: String) extends GeneratedValue(domain)

  case class ShortString(str: String) extends GeneratedValue(str)
}

private[util] trait GenerationDomain {
  type EmailAddress = Definitions.EmailAddress
  val EmailAddress = Definitions.EmailAddress

  type FirstName = Definitions.FirstName
  val FirstName = Definitions.FirstName

  type FullName = Definitions.FullName
  val FullName = Definitions.FullName

  type LastName = Definitions.LastName
  val LastName = Definitions.LastName

  type CountryCode = Definitions.CountryCode
  val CountryCode = Definitions.CountryCode

  type Country = Definitions.Country
  val Country = Definitions.Country

  type City = Definitions.City
  val City = Definitions.City

  type ProgrammingLanguage = Definitions.ProgrammingLanguage
  val ProgrammingLanguage = Definitions.ProgrammingLanguage

  type LoremIpsum = Definitions.LoremIpsum
  val LoremIpsum = Definitions.LoremIpsum

  type Url = Definitions.Url
  val Url = Definitions.Url

  type Domain = Definitions.Domain
  val Domain = Definitions.Domain

  type ShortString = Definitions.ShortString
  val ShortString = Definitions.ShortString
}