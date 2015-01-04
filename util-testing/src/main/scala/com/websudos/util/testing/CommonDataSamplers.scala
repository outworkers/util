package com.websudos.util.testing

trait CommonDataSamplers {
  private[this] val Names = List(
    "Aleksandar", "Alexander", "Ali", "Amar", "Andrei", "Aron", "Artem", "Artyom", "Ben", "Bence", "Charlie",
    "Davit ", "Dylan", "Emil", "Filip", "Francesco", "Gabriel", "Georgi ", "Georgios", "Giorgi", "Hugo",
    "Jack", "Jakub", "James", "João", "Jon", "Jonas", "Jónas", "Luca", "Lucas", "Luka", "Lukas", "Luke",
    "Maksym", "Malik", "Marc", "Matas", "Mathéo", "Maxim", "Maxim ", "Mehmet", "Milan", "Mohamed", "Nathan",
    "Nikola", "Noah", "Noah ", "Noel", "Oliver", "Onni", "Raphael", "Rasmus", "Roberts", "Robin", "Sem",
    "William", "Yanis", "Yerasyl ", "Yusif", "Yusuf"
  )

  private[this] val Surnames = List(
    "Smith", "Jones", "Taylor", "Brown", "Williams", "Wilson", "Johnson", "Davies", "Robinson", "Wright",
    "Thompson", "Evans", "Walker", "White", "Roberts", "Green", "Hall", "Wood", "Jackson", "Clarke"
  )

  private[this] val Environments = List("Linux", "Unix", "FreeBSD", "Windows", "Android", "Mac OS X")

  private[this] val Cities = List(
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

  private[this] val Languages = List(
    "C", "Java", "Objective-C", "C++", "Basic", "C#", "Python", "PHP", "Perl", "JavaScript", "Visual Basic",
    "Visual Basic .NET", "Ruby", "F#", "Pascal", "Transact-SQL", "ActionScript", "Delphi/Object Pascal",
    "Lisp", "PL/SQL", "MATLAB", "SAS", "Swift", "Assembly", "ML", "Logo", "PostScript", "D", "COBOL", "R",
    "OpenEdge ABL", "ABAP", "Ada", "Fortran", "Lua", "C shell", "Scratch", "Go", "Scala", "Haskell",
    "Z shell", "cT", "PL/I", "Scheme", "Erlang", "Prolog", "Tcl", "Hack", "Groovy", "LabVIEW")

  private[this] val Libraries = List("JQuery", "Kafka")

  private[this] val Tools = List("GitHub", "Git", "Ansible", "Mercurial", "SVN", "Fossil")

  private[this] val Paradigms = List("MVC", "TTD", "BDD", "Asynchronous Programming", "OOP")

  private[this] val Platforms = List("Linux", "Windows", "IOS")

  private[this] val Storage = List("Memcache", "MongoDb", "Cassandra")

  /*
  val date: Gen[DateTime] = for {
    year <- choose(2010, 2014)
    month <- oneOf(1, 12)
    day <- oneOf(1, 28)
  } yield new DateTime(year, month, day)

  def sentence(words: Int = 5): Gen[String] = for {
    ws <- listOfN(words, oneOf(LoremIpsum))
  } yield {
    val s = ws.mkString("")
    s.head.toUpper + s.tail + "."
  }
    
  def sentences(n: Int = 5, words: Int = 5): Gen[String] = for {
    s <- listOfN(n, sentence(words))
  } yield s.mkString(" ")
  */


  /*
    val developerWithAttribute: Gen[(Developer, Attribute)] = for {
      name        <- oneOf(Names)
      surname     <- oneOf(Surnames)
      active      <- arbitrary[Boolean]
      startTime   <- date
      city        <- oneOf(Cities)
      country     <- oneOf(Countries)
      description <- sentences(5)
      availability <- sentences(3)
      preferedEnvironment <- oneOf(Environments)
      amazing <- sentences(1)
    } yield {
      val fullName = name + " " + surname
      val uid = MockUid(fullName)
  
      val developer = Developer(uid, active, name, fullName, -1)
      val atribute = Attribute(
        id = -1L,
        photo = s"http://example.com/photos/$uid.png",
        startTime = startTime,
        city = city,
        country = country,
        description = description,
        availability = availability,
        preferedEnvironment = preferedEnvironment,
        amazing = amazing,
        devUid = "")
  
      (developer, atribute)
    }
    */
}
