# Conference Session Signup refactoring exercise

_From mutable beans, to immutable values, to unrepresentable illegal states_

## Scenario

The code we are working on implements sign-up for conference sessions.

```plantuml
:Attendee: as a
:Presenter: as p
:Admin: as b

component "Attendee's Phone" as aPhone {
    component [Conference App] as aApp
}
component "Presenter's Phone" as pPhone {
    component [Conference App] as pApp
}
component "Web Browser" as bBrowser
component "Conference Web Service" as webService

a -right-> aApp : "sign-up\ncancel sign-up"
aApp -down-> webService : HTTP

p -left-> pApp : "list attendees"
pApp -down-> webService : HTTP

b -right-> bBrowser : "add sessions\nlist attendees"
bBrowser -right-> webService : HTTP
```

An admin user creates sign-up sheets for sessions in an admin app (not covered in this example). 
Sessions have limited capacity, set when the sign-up sheet is created.
Attendees sign up for sessions via mobile conference app.  
Admins can also sign attendees up for sessions via the admin app.
The session presenter starts the session via the mobile conference app.  After that, the sign-up sheet cannot be changed.

## Simplifications

The code is simplified for the sake of brevity and clarity:

* It doesn't cover some edge cases.  The techniques we will use in the exercise apply equally well to those too.
* It doesn't include authentication, authorisation, monitoring, tracing, etc., to focus on the topic of the exercise.


## Review the Kotlin code in the conf.signup.server package

Classes:

* `SignupSheet` manages the sign-up for a single conference session  

* A `SignupBook` is a collection of signup sheets for all the sessions in the conference 
  * Hexagonal architecture: the `SignupBook` interface is defined in the application domain model and hides the choice of technical persistence layer
  * Sheets are added to the signup book out-of-band by an admin app, which is not shown in the example code.

* `SignupApp` implements the HTTP API by which a front-end controls the SignupSheet.
  * Routes on request path and method
  * Supports attendee sign up and cancellation, closing signup to a session, and listing who is signed up.
  * Translates exceptions from the SignupSheet into HTTP error responses
  * SignupApp is a typical Http4k handler... although Http4k implements the "server as a function" pattern, the handler is not a pure function. It reads and writes to the SignupSheet, which is our abstraction for the database.

* SessionSignupAppTests: behaviour is tested at the HTTP API, with fast, in-memory storage.  
  * The InMemorySignupBook and InMemoryTransactor emulate the semantics of the persistence layer

* SignupServer:
  * Example of a real HTTP server storing signups in memory.  You can run this and play with the server using IntelliJ's HTTP requests plugin.


NOTE: The name of the SignupApp function follows Http4k conventions. An "App" includes the HTTP routing and business logic. A "Server" runs the App in an HTTP server, with an HTTP "Stack" that provides monitoring, authentication flows, etc.


## Getting started

Run the tests to confirm that they pass.

Use "F2" to navigate through the IntelliJ warnings.  Use Alt-Enter to automatically address any issues found.

Now... Let's refactor this to *idiomatic* Kotlin.


## Refactoring task

* Address code smells
* Refactor to a _functional_ domain model
* Introduce type safety

Our strategy is to start by working in the domain model and work outwards towards the HTTP layer.


### What code smells?

The class is a Kotlin implementation of Java style "bean" code.

* Essential behaviour implemented by mutation (signups)
* Also _inappropriate_ mutation (e.g. sessionId, capacity)
* Throws exceptions if client code uses the object incorrectly.
* Exceptions are caught at the HTTP layer and translated to HTTP status codes

Wouldn't it be better if the client code could NOT use the object incorrectly?

We can make that happen by refactoring to represent different states at the type level, and only define operations on those types that are applicable for the states they represent.

However, we cannot do that while the code uses mutable state... Kotlin cannot represent _dynamic_ aspects of mutable state in its _static_ type system. To introduce type safety, we must first remove mutation.  That gives us our refactoring strategy:

1. Convert the mutable SignupSheet bean to an immutable data class.
2. Convert the immutable data class into a sealed class hierarchy in which subclasses represent different states, and operations are only defined on states for which they are applicable.


## Part 1: Converting the bean to an immutable data class

The SignupSheet is used in the SignupApp. If we are going to make the SignupSheet immutable, we'll need to change this HTTP handler to work with immutable values, rather than mutable beans.

A general strategy for converting classes from mutable to immutable is to push mutation from the inside outwards.  E.g. converting a val that holds a mutable object into a var that holds an immutable object.  And continuing this strategy to push mutation into external resources (databases, for example).

### Replacing mutability with immutability

We'll demonstrate this strategy "in the small", by making the `SignupSheet#signups` set immutable. We will replace the immutable reference to a MutableSet with a _mutable_ reference to an immutable Set:

* Find usages of the `signups` property.  There are several references in this class that mutate the Set by calling its methods.  All these methods are operator methods, and so the method calls can be replaced by operators that Kotlin desugars to mutation of a mutable set or to transformation of an immutable set held in a mutable variable. Using the operators will let us push mutation outwards without breaking the app. You can use Alt-Enter in IntelliJ to refactor between method call and operator.
  * Replace the call to the add method with `signups += attendeeId`
  * Replace the call to the remove method with `signups -= attendeeId`. (For some reason, IntelliJ does not have offer this when you hit Alt-Enter. You have to do this by a manual edit.)
  * For consistency, you can also replace the call to the contains method with the `in` operator.

Run the tests. They pass. COMMIT!

* Change the declaration of `signups` to: `var signups = emptySet<AttendeeId>()`.

Run the tests. They pass.

**Review**: We pushed mutation one level outwards. We did so without breaking the application by making the application use operators that are the same whether mutating a mutable object held in a `val` or transforming an immutable object held in a `var`.

Demonstrate this by using Alt-Enter and "Replace with ordinary assignment" to expand the `+=` to `+` and `-=` to `-`. 

Use Alt-Enter to toggle back and forth between the `+=` and ordinary assignment operator. Leave the code using the ordinary assignment operator. It will come in handy later.

Run the tests. They pass. COMMIT!

### Now for the bean itself

We can apply the same strategy of using an API that has the same syntax for both mutable and immutable operations to let us convert the SignupSheet to an immutable data class without breaking lots of code.  However, we can't use Kotlin's operators to do so.  We'll have to create that API ourselves:

1. Change the SignupSheet so have a so-called "fluent" API.
2. Change clients to use the fluent API as if the SignupSheet were immutable.
3. Make the SignupSheet immutable.

#### Step 1: Change the SignupSheet so have a "fluent" API.

* Turn the mutator methods into a "fluent" API by adding `return this` at the end of the `signUp` and `cancelSignUp` methods and using Alt-Enter to add the return type to the method signature.

Run the tests. They pass. COMMIT!

#### Step 2: Change clients to use the fluent API as if the SignupSheet were immutable

In SignupApp, replace sequential statements that mutate and then save with a single statement passes the result of the mutator to the `save` method, like:

~~~
book.save(sheet.close())
~~~

We can make IntelliJ do this automatically by extracting the call to the chainable mutator into a local variable called `sheet`.  IntelliJ will warn about shadowing. That's OK: inline the local `sheet` variable, and the call to the chainable mutator will be inlined as a parameter of `book.save`.

Run the tests. They pass. COMMIT!

#### Step 3: Make the SignupSheet immutable

In SignupServer, replace the mutation of the sheet with a call to the secondary constructor and inline the `sheet` variable.

We don't have a test for the server — it itself is test code — but COMMIT! anyway.  The use of domain-specific value types means that the type checker gives us good confidence that this refactor is correct.

We can now delete the no-arg constructor.  There's no easy way to do this automatically in IntelliJ.  You'll have to do so with a manual edit: delete the `()` after the class name, and the call to `this()` in the secondary constructor declaration.

ASIDE: Like a lot of real-world Java code, this example uses Java Bean naming conventions but not actual Java Beans.

Run the tests. They pass. COMMIT!

Convert the secondary constructor to a primary constructor via Alt-Enter on the declaration.

Run the tests. They pass. COMMIT!

Make sessionId a non-nullable val.

Make capacity a val. Delete the entire var property including the checks. Those are now enforced by the type system.  IntelliJ highlights the declaration in the class body as redundant.  Use Option-Enter to move the val declaration to the primary constructor.

Run the tests. They pass. COMMIT!

Move the declaration of `signups` to primary constructor, initialised to `emptySet()`

Try running the tests...  The mutators do not compile.  Change them so that, instead of mutating a property, they return a new copy of the object that the property changed.  It's easiest to declare the class as a `data class` and call the `copy` method.

Run the tests... they fail!  

We also have to update our in-memory simulation of persistence, the InMemorySignupBook. The code to return a copy of the stored SignupSheet is now unnecessary because SignupSheet is immutable. Delete it all, and return the value obtained from the map

Run the tests. They pass. COMMIT!

### Tidying up

We can turn some more methods into expression form.

* We cannot do this for signUp and cancelSignup because of those checks.  We'll come back to those shortly...

ASIDE: I prefer to use block form for functions with side effects and expression for pure functions.

Run the tests. They pass. COMMIT!


The data class does allow us to make the state of a signup sheet inconsistent, by passing in more signups than the capacity.

Add a check in the init block:

    ~~~
    init {
        check(signups.size <= capacity) {
            "cannot have more sign-ups than capacity"
        }
    }
    ~~~
  
Now, if you have a reference to a SignupSheet, it's guaranteed to have a consistent state. 

This makes the `isFull` check in `signUp` redundant, so delete it.


## Part 2. Converting the immutable data class into a sealed class hierarchy

Now, those checks... it would be better to prevent client code from using the SignupSheet incorrectly than to throw an exception after they have used it incorrectly.  In FP circles, this is sometimes referred to as "making illegal states unrepresentable".

The SignupSheet class implements a state machine:


~~~plantuml

state Open {
    state choice <<choice>>
    open -down-> Available
    Available -down-> Available : cancelSignUp(a)
    Available -right-> choice : signUp(a)
    choice -right-> Full : [#signups = capacity]
    choice -up-> Available : [#signups < capacity]
    Full -left-> Available : cancelSignUp(a)
}

[*] -down-> Available
~~~


We can express this in Kotlin with a _sealed type hierarchy_...

~~~plantuml
hide empty members
hide circle

class SignupSheet <<sealed>>

class Available <<sealed>> extends SignupSheet {
    cancelSignUp(a): Available
    signUp(a): Open
}

class Full extends Open {
    cancelSignUp(a): Available
}
~~~

We'll introduce this state by state, replacing predicates over dynamic state with subtype relationships.

Unfortunately, IntelliJ doesn't have any automated refactorings to split a class into a sealed hierarchy, so we'll have to do it the old-fashioned way.

* Extract a sealed base class from SignupSheet
  * NOTE: IntelliJ seems to have lost the ability to rename a class and extract a supertype with the original name.  So, we'll have to extract the base class with a temporary name and then rename class and interface to what we want.
  * call it anything, we're about to rename it.  "SignupSheetBase", for example, or "Banana". 
  
Pull up sessionId, capacity & signups as abstract members. 

* This refactoring doesn't work 100% for Kotlin, so fix the errors in the interface by hand.

Pull up isSignedUp and isFull as a concrete members.

Change the name of the subclass by hand (not a rename refactor) to Available, and then use a rename refactoring to rename the base class to SignupSheet.

Repeatedly run all the tests to locate all the compilation errors...

In SignupApp, there are calls to methods of the Available class that are not defined on the SignupSheet class.

* wrap the logic for each HTTP method in `when(sheet) { is Available -> ... }` to get things compiling again. E.g.

    ~~~
    when (sheet) {
        is Available ->
            try {
                book.save(sheet.signUp(attendeeId))
                sendResponse(exchange, OK, "subscribed")
            } catch (e: IllegalStateException) {
                sendResponse(exchange, CONFLICT, e.message)
            }
        }
    }
    ~~~

  * In SessionSignupHttpTests and SignupServer we need to create Available instead of SessionSignup. IntelliJ doesn't yet have a "Replace constructor with factory method" refactoring for Kotlin classes.  But we can do the same thing manually... define a function to create new signup sheets in SignupSheet.kt, called `SignupSheet:
    
    ~~~
    fun SignupSheet(sessionId: SessionId, capacity: Int) =
        Available(sessionId, capacity)
    ~~~

Run the tests. They pass. COMMIT!

Now we can add the Full subclass:

NOTE: do not use the "Implement sealed class" action... it does not give the option to create the class in the same file. Instead...

Define a new `data class Full : SignupSheet()` in the same file. 

The new class is highlighted with an error underline. Option-Enter on the highlighted error, choose "Implement as constructor parameters", ensure sessionId and signups are selected in the pop-up (default behaviour), and perform the action.

The class will still be underlined with a red error highlight because `capacity` has not been implemented yet.  Alt-Enter on the error, select "Implement members", and implement `capacity` to evaluate to `signups.size`.

* The end result should therefore be:

    ~~~kotlin
    data class Full(
        override val sessionId: SessionId,
        override val signups: Set<AttendeeId>
    ) : Open() {
        override val capacity: Int
            get() = signups.size
    }
    ~~~

We've broken the SignupApp, so before we use the Full class to implement our state machine, get it compiling again: add when branches for Full that just call TODO(), by Alt-Enter-ing on the errors and selecting "Add remaining branches". This is safe, because we are not instantiating the Full class yet.

Run the tests to verify that we have not broken anything.

Now make the `Available#signUp` method return a Full instance when the session is full:

~~~kotlin
fun signUp(attendeeId: AttendeeId): SignupSheet {
    val newSignups = signups + attendeeId
    if (newSignups.size == capacity) {
        return Full(sessionId, newSignups)
    } else {
        return copy(signups = newSignups)
    }
}
~~~

Run the tests: there are failures because of the TODO() calls. Make them pass by implementing the `when` branches for the Full case:
* for the POST handler, return a CONFLICT status with a helpful message as the body of the response
* for the DELETE handler, remember copy/paste/refactor? We'll use that here.  Duplicate the logic for Available, but without the try/catch because Full#cancelSignup does not throw.

Run the tests. They pass. COMMIT!


Now to refactor away that duplication. 

* Available#cancelSignup does not throw either.  Remove the try/catch from the Available branch, to make the two branches identical.
* Pull the Full#cancelSignup method up to the SignupSheet class, and delete the implementation in the Available class.
* Remove the unnecessary conditional in the DELETE handler.

Run the tests. They pass. COMMIT!

Review the signUp method. The check in the Available constructor 
Review the subclasses of SignupSheet.  The classes no longer check that methods are called in the right state. The only remaining check, in the init block, defines a class invariant that the internal implementation maintains. It will only fail if someone introduces a bug. We don't want that reported as a CONFLICT response, and can let it propagate to the HTTP server layer where it will be reported as an INTERNAL_SERVER_ERROR status. Therefore, remove the try/catch in the POST handler.

Run the tests. They pass. COMMIT!

We can remove the isFull property.  It is only now only true for the `Full` type.
 * Find uses of isFull: there is only one use, in the SignupApp.
 * Replace the property access `sheet.isFull` with `sheet is Full`   
 * Safe-delete the isFull property

Run all the tests. They pass. COMMIT!


## Converting the methods to extensions

If we have time, convert methods to extensions (Option-Enter on the methods).

Change the result types to the most specific possible.

Gather the types and functions into two separate groups.

Fold away the function bodies. Ta-da!  The function signatures describe the state machine!


## Wrap up

Review the code of SignupSheet and SignupApp

What have we done?

* Refactored a mutable, java-bean-style domain model to an immutable, algebraic data type and operations on the data type.
  * Pushed mutation outward, to the edge of our system
* Replaced runtime tests throwing exceptions for invalid method calls, with type safety: it is impossible to call methods in the wrong state because those operations do not exist in those states
  * Pushed error checking outwards, to the edge of the system, where the system has the most context to handle or report the error
* Used copy/paste/refactor to discover the right abstraction
* Used a fluent interface to deliver the refactoring via expand/contract.  In a large system we could gradually migrate code that depends on the SignupSheet class to a functional style, to avoid disrupting people's work.
