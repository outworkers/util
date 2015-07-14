package com.websudos.util.domain


private[util] object Definitions {
  case class EmailAddress(address: String)
  case class FirstName(name: String)
  case class LastName(name: String)
  case class FullName(name: String)
  case class CountryCode(code: String)

  case class Country(country: String)
  case class City(city: String)
  case class ProgrammingLanguage(language: String)

  case class LoremIpsum(word: String)
  case class Url(url: String)
  case class Domain(domain: String)

  case class ShortString(str: String)
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