/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.util.testing

private[testing] trait CommonDataSamplers {
  val Names = List(
    "Aleksandar", "Alexander", "Ali", "Amar", "Andrei", "Aron", "Artem", "Artyom", "Ben", "Bence", "Charlie",
    "Davit ", "Dylan", "Emil", "Filip", "Francesco", "Gabriel", "Georgi ", "Georgios", "Giorgi", "Hugo",
    "Jack", "Jakub", "James", "João", "Jon", "Jonas", "Jónas", "Luca", "Lucas", "Luka", "Lukas", "Luke",
    "Maksym", "Malik", "Marc", "Matas", "Mathéo", "Maxim", "Maxim ", "Mehmet", "Milan", "Mohamed", "Nathan",
    "Nikola", "Noah", "Noah ", "Noel", "Oliver", "Onni", "Raphael", "Rasmus", "Roberts", "Robin", "Sem",
    "William", "Yanis", "Yerasyl ", "Yusif", "Yusuf"
  )

  val Surnames = List(
    "Smith", "Jones", "Taylor", "Brown", "Williams", "Wilson", "Johnson", "Davies", "Robinson", "Wright",
    "Thompson", "Evans", "Walker", "White", "Roberts", "Green", "Hall", "Wood", "Jackson", "Clarke"
  )

  val Environments = List("Linux", "Unix", "FreeBSD", "Windows", "Android", "Mac OS X")

  val Cities = List(
    "Berlin", "Madrid", "Rome", "Paris", "Hamburg", "Budapest", "Vienna", "Warsaw", "Bucharest", "Barcelona",
    "Stockholm", "Munich", "Milan", "Prague", "Sofia", "Copenhagen", "Birmingham", "Cologne", "Brussels",
    "Naples", "Turin", "Marseille", "Valencia", "Amsterdam", "Zagreb", "Kraków", "Riga", "Łódź", "Athens",
    "Seville", "Palermo", "Frankfurt", "Wrocław", "Zaragoza", "Helsinki", "Genoa", "Stuttgart", "Glasgow",
    "Düsseldorf", "Berlin", "Madrid", "Rome", "Paris", "Hamburg", "Budapest", "Vienna", "Warsaw", "Bucharest",
    "Barcelona", "Stockholm", "Munich", "Milan", "Prague", "Sofia", "Copenhagen", "Birmingham", "Cologne",
    "Brussels", "Naples", "Turin", "Marseille", "Valencia", "Amsterdam", "Zagreb", "Kraków", "Riga", "Łódź",
    "Athens", "Seville", "Palermo", "Frankfurt", "Wrocław", "Zaragoza", "Helsinki", "Genoa", "Stuttgart",
    "Glasgow", "Düsseldorf"
  )

  val Countries = List(
    "Albania", "Andorra", "Armenia", "Austria", "Azerbaijan", "Belarus", "Belgium", "Bosnia and Herzegovina",
    "Bulgaria", "Croatia", "Cyprus", "Czech Republic", "Denmark", "Estonia", "Finland", "France", "Georgia",
    "Germany", "Greece", "Hungary", "Iceland", "Ireland", "Italy", "Kazakhstan", "Latvia", "Liechtenstein",
    "Lithuania", "Luxembourg", "Macedonia", "Malta", "Moldova", "Monaco", "Montenegro", "Netherlands",
    "Norway", "Poland", "Portugal", "Romania", "Russia", "San Marino", "Serbia", "Slovakia", "Slovenia",
    "Spain", "Sweden", "Switzerland", "Turkey", "Ukraine", "United Kingdom"
  )

  val LoremIpsum = List(
    "ad", "adipisicing", "aliqua", "aliquip", "amet", "anim", "aute", "cillum", "commodo", "consectetur",
    "consequat", "culpa", "cupidatat", "deserunt", "do", "dolor", "dolore", "duis", "ea", "eiusmod", "elit",
    "enim", "esse", "est", "et", "eu", "ex", "excepteur", "exercitation", "fugiat", "id", "in", "incididunt",
    "ipsum", "irure", "labore", "laboris", "laborum", "lorem", "magna", "minim", "mollit", "nisi", "non",
    "nostrud", "nulla", "occaecat", "officia", "pariatur", "proident", "qui", "quis", "reprehenderit", "sed",
    "sint", "sit", "sunt", "tempor", "ullamco", "ut", "velit", "veniam", "voluptate"
  )

  val PrgrammingLanguages = List(
    "C", "Java", "Objective-C", "C++", "Basic", "C#", "Python", "PHP", "Perl", "JavaScript", "Visual Basic",
    "Visual Basic .NET", "Ruby", "F#", "Pascal", "Transact-SQL", "ActionScript", "Delphi/Object Pascal",
    "Lisp", "PL/SQL", "MATLAB", "SAS", "Swift", "Assembly", "ML", "Logo", "PostScript", "D", "COBOL", "R",
    "OpenEdge ABL", "ABAP", "Ada", "Fortran", "Lua", "C shell", "Scratch", "Go", "Scala", "Haskell",
    "Z shell", "cT", "PL/I", "Scheme", "Erlang", "Prolog", "Tcl", "Hack", "Groovy", "LabVIEW")

  val Libraries = List("JQuery", "Kafka")

  val Tools = List("GitHub", "Git", "Ansible", "Mercurial", "SVN", "Fossil")

  val Paradigms = List("MVC", "TTD", "BDD", "Asynchronous Programming", "OOP")

  val Platforms = List("Linux", "Windows", "IOS")

  val Storage = List("Memcache", "MongoDb", "Cassandra")

}


private[testing] object CommonDataSamplers extends CommonDataSamplers
