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
    val parent:Parent = new Child
    parent.lazyMapSetter
    println("lazyMap" + parent.lazyMap)
    println("<<")
  }
}

trait Parent {
  lazy val lazyVal:Int = 1
  lazy val lazyMap = scala.collection.mutable.Map[String,Int]()
  def printVal:Int = lazyVal
  def lazyMapSetter:Unit
}

class Child extends Parent {
  override lazy val lazyVal = 2
  override def lazyMapSetter:Unit = lazyMap.put("key1",1)
}