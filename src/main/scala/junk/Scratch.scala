package junk

/**
 * Object to use for temporary exeperiments
 *
 */
object Scratch {

  def main(args: Array[String]) {
    val child = new Child
    println(">>")
    println("child: " + child.lazyVal)
    println("<<")
  }
}

trait Parent {
  lazy val lazyVal:Int = 1
  def printVal:Int = lazyVal
}

class Child extends Parent {
  override lazy val lazyVal = 2

}