TODO: Run your tests with Firebase Test Lab

https://mobiletestingblog.com/2017/01/29/introduction-to-android-espresso-testing-and-spoon/


EXE ALL TEST:
=============

test
----

./gradlew test


androidTest
-----------

./gradlew connectedAndroidTest  ==> Desinstala la app con cada test ?!

OR

adb shell am instrument -w com.cesoft.encuentrame3.test/android.support.test.runner.AndroidJUnitRunner



