package lp.template.wizard

object Ascii {
  def classToSnakeCase(name: String) = {
    name match {
      case "" => name
      case _ =>
        name
          .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
          .replaceAll("([a-z\\d])([A-Z])", "$1_$2")
          .toLowerCase
    }
  }

  def classToMinusSnakeCase(name: String) = {
    name match {
      case "" => name
      case _ => classToSnakeCase(name).replaceAll("_", "-")
    }
  }

  def classToSnakeUpperCase(name: String) = {
    name match {
      case "" => name
      case _ =>
        name
          .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
          .replaceAll("([a-z\\d])([A-Z])", "$1_$2")
          .toUpperCase()
    }
  }

  //  def snakeToClassCase(name: String): String = {
  //    def loop(x: List[Char]): List[Char] = {
  //      (x: @unchecked) match {
  //        case '_' :: '_' :: rest => loop('_' :: rest)
  //        case '_' :: c :: rest => Character.toUpperCase(c) :: loop(rest)
  //        case '_' :: Nil => Nil
  //        case c :: rest => c :: loop(rest)
  //        case Nil => Nil
  //      }
  //    }
  //
  //    name match {
  //      case " => name
  //      case _ =>
  //        loop('_' :: name.toList).mkString
  //    }
  //  }

  def classToMethodCase(name: String): String = {
    name match {
      case "" => name
      case _ => name.substring(0, 1).toLowerCase + name.substring(1)
    }
  }
}

