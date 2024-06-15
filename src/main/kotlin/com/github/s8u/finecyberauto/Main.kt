package com.github.s8u.finecyberauto

import org.openqa.selenium.By.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.util.*
import kotlin.system.exitProcess

val WEB_DRIVER_ID = "webdriver.chrome.driver"
val WEB_DRIVER_PATH = "C:/chromedriver.exe"

val WEBSITE_USERNAME = ""
val WEBSITE_PASSWORD = ""

// 학습중인 과정 페이지 URL
//val URL = "https://finecyber.kr/web/html/03_online/view.php?SCode=230515&page=1&keyword=&keyfield="
//val URL = "https://finecyber.kr/web/html/03_online/view.php?SCode=230480&page=1&keyword=&keyfield="
//val URL = "https://finecyber.kr/web/html/03_online/view.php?SCode=230445&page=1&keyword=&keyfield="
val URL = "https://finecyber.kr/web/html/03_online/view.php?SCode=230410&page=1&keyword=&keyfield="

lateinit var driver: ChromeDriver

fun main() {
    // 크롬 켜기
    val process = ProcessBuilder("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe", "--remote-debugging-port=9222", "--user-data-dir=C:\\chrometemp").start()
    println("Chrome start")

    // 드라이버 초기화
    System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH)
    driver = ChromeDriver(ChromeOptions().apply {
        setExperimentalOption("debuggerAddress", "127.0.0.1:9222")
    })
    println("ChromeDriver initialized")


    // 학습 중인 과정 페이지 접속
    driver.get(URL)
    println("page open")
    Thread.sleep(1000L)

    // 로그인 alert 스킵
    driver.switchTo().alert().accept()
    println("skip login alert")
    Thread.sleep(1000L)

    // 로그인
    if (driver.findElement(ById("mem_id")) != null) {
        driver.findElement(ById("mem_id")).sendKeys(WEBSITE_USERNAME)
        driver.findElement(ById("mem_pw")).sendKeys(WEBSITE_PASSWORD)
        driver.findElement(ByCssSelector("#login input[type=submit]")).click()
        println("login")
        Thread.sleep(1000L)

        // 학습 중인 과정 페이지 접속
        driver.get(URL)
        println("page open")
        Thread.sleep(1000L)
    } else {
        println("login skipped")
    }

    // 시청버튼 클릭
    driver.findElement(ByClassName("vod_btn")).click()
    println("vod_btn click")
    Thread.sleep(1000L)

    // 플레이 클릭
    driver.switchTo().frame(driver.findElement(ById("iframe_study")))
    driver.findElement(ById("mediaPlay")).click()
    println("mediaPlay click")
    Thread.sleep(1000L)

    // 초마다 시간 확인
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            val timeText = driver.findElement(ByClassName("timer")).text
            println("timeText: ${timeText}")

            val timeSplit = timeText.split(" / ")
            if (timeSplit[0] != timeSplit[1]) return

            if (driver.findElement(ById("next")).isEnabled) {
                driver.findElement(ById("next")).click()
                println("next click")
            } else {
                notification("finecyber-auto", "end")
                println("end")

                timer.cancel()
                process.destroy()
                exitProcess(0)
            }
        }
    }, 0, 1000L)
}

fun notification(title: String, text: String, messageType: TrayIcon.MessageType = TrayIcon.MessageType.INFO) {
    val trayIcon = TrayIcon(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "finecyber-auto")
    SystemTray.getSystemTray().add(trayIcon)
    trayIcon.displayMessage(title, text, messageType)
    SystemTray.getSystemTray().remove(trayIcon)
}