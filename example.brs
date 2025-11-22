' BrightScript Example File
' This file demonstrates syntax highlighting

' Main function
function main() as void
    print "Hello, Roku!"

    if x > 0 then
      if y > 0 then
          print "both positive"
      end if
    end if

    '' Variables
    name = "John Doe"
    age = 30
    isActive = true
    balance = 100.50'

    ' Arrays
    numbers = [1, 2, 3, 4, 5]
    fruits = ["apple", "banana", "orange"]

    ' Associative Arrays (Objects)
    person = {
        name: "Jane Smith",
        age: 25,
        email: "jane@example.com"
    }

    ' Conditional statements
    if age >= 18 then
        print "Adult"
    else if age >= 13 then
        print "Teenager"
    else
        print "Child"
    end if

    ' For loop
    for i = 0 to 10
        print i
    end for

    ' For each loop
    for each fruit in fruits
        print fruit
    end for

    ' While loop
    counter = 0
    while counter < 5
        print "Counter: " + counter.toStr()
        counter = counter + 1
    end while

    ' Function call
    result = addNumbers(10, 20)
    print "Result: " + result.toStr()

    ' Object method call
    screen = CreateObject("roScreen")
    screen.show()
end function

' Function with parameters and return value
function addNumbers(a as integer, b as integer) as integer
    return a + b
end function

' Subroutine
sub printMessage(message as string)
    print message
end sub

' REM style comment
REM This is an alternative comment style

' Class definition (BrightScript 2.0)
class Calculator
    public function add(a as integer, b as integer) as integer
        return a + b
    end function

    public function subtract(a as integer, b as integer) as integer
        return a - b
    end function

    public function multiply(a as integer, b as integer) as integer
        return a * b
    end function

    public function divide(a as integer, b as integer) as float
        if b <> 0 then
            return a / b
        else
            return invalid
        end if
    end function
end class

' Namespace
namespace MyApp.Utils
    function formatString(text as string) as string
        return text.trim()
    end function
end namespace

' Error handling
function safeOperation() as void
    try
        ' Some risky operation
        result = 10 / 0
    catch e
        print "Error occurred: " + e.getMessage()
    finally
        print "Cleanup"
    end try
end function

' Operators demonstration
function testOperators() as void
    ' Arithmetic
    sum = 10 + 5
    diff = 10 - 5
    product = 10 * 5
    quotient = 10 / 5
    remainder = 10 mod 3
    power = 2 ^ 3

    ' Comparison
    isEqual = (10 = 10)
    isNotEqual = (10 <> 5)
    isGreater = (10 > 5)
    isLess = (5 < 10)
    isGreaterOrEqual = (10 >= 10)
    isLessOrEqual = (5 <= 10)

    ' Logical
    andResult = true and false
    orResult = true or false
    notResult = not true

    ' Bitwise
    bitwiseAnd = 10 and 5
    bitwiseOr = 10 or 5
    leftShift = 2 << 3
    rightShift = 16 >> 2
end function


if x > 0 then
  if y > 0 then
      print "both positive"
  end if
end if