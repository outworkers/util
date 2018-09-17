/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.util.domain


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
