
# Base Utils

In this sub-project we have:
 * **Clock** wraps time operations making time mocking more easy to do;
 * **ConfigExtensions** extends the operations around [Config](https://github.com/typesafehub/config);
 * **Settings** abstraction that allow us to define in a standard way Server/Client/Repository settings.
  
##Clock
This object wraps time operations in order to make it more easy to UnitTest. 

In the next example, the *Product* depends heavily on the current time making it more hard to test.

```scala
case class Product(name:String, lastUpdated:Long, lastCreated:Long)

class RepoProduct {
  def createJson:String = {
    val product = Product( 
      name = "sample", 
      lastUpdated = LocalDateTime.now().toEpochSecond, 
      lastCreated = LocalDateTime.now().toEpochSecond
    )
    productToJson(product)
  }
  
  def productToJson(product:Product):String = {
    /*Missing Code*/  
  }
}
```

The **Clock** object allow us to write the code in a more Testable way:
```scala
case class Product(name:String, lastUpdated:Long, lastCreated:Long)

class RepoProduct {
  def createJson(implicit clock = DefaultSystemClock):String = {
    val product = Product( 
      name = "sample", 
      lastUpdated = clock.now.toEpochSecond, 
      lastCreated = clock.now.toEpochSecond
    )
    productToJson(product)
  }
  
  def productToJson(product:Product):String = {
    /*Missing Code*/  
  }
}
``` 

and we can easily test the class like 
```scala
class RepoProductSpec extends FreeSpec with Matchers {
  /*Missing code*/
}

object RepoProductSpec{
  val DateTimeExample = LocalDateTime.now()
  val ZonedDateTimeExample = ZonedDateTime.now()
  val MockedClock = new Clock{
    def now         = DateTimeExample
    def nowWithZone = ZonedDateTimeExample
  }
}
```

### Clock.now*
Returns a LocalDateTime instance.

### Clock.nowWithZone
Returns a ZonedDateTime instance.

## ConfigExtensions
This wrapper allow us to extend Config functionalities.
  
### ConfigExtensions.getFiniteDuration
Gets a FiniteDuration from *config* by mapping the *Java Duration* into *Scala Duration*.

### ConfigExtensions.getMap
Gets a value as Map[String, String]. Throws an exception if it does not exist

### ConfigExtensions.tryGetConfig
Try to get a value as Config. If no configuration is found, returns an empty one.

### ConfigExtensions.getOptionString
Gets a value as Option[String]. If no value is found, returns None.

### ConfigExtensions.getUrl
Gets a value as an Url object.

### ConfigExtensions.prefixKeysWith
This operation applied a prefix to all Config Keys.

#On Going
Enhance the wrapper to support generic types instead of specifying the type for every field.
