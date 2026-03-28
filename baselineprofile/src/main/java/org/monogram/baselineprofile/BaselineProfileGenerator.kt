package org.monogram.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "org.monogram",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()

        waitForDesc("ChatList", 10_000)
        scrollByDesc("ChatList")

        clickByDesc("ChatTitle")
        waitForDesc("ChatContent")
        scrollByDesc("ChatMessages")

        clickByDesc("ChatHeaderButton")
        waitForDesc("ProfileContent")
        scrollByDesc("ProfileContent")
        pressBackAndWait()

        pressBackAndWait()
        waitForDesc("ChatList")

        clickByDesc("Settings")
        waitForDesc("SettingsContent")
        scrollByDesc("SettingsList")

        openSettingsSubscreenAndBack(
            itemDesc = "SettingsChatSettings",
            screenDesc = "ChatSettingsContent"
        )
        openSettingsSubscreenAndBack(
            itemDesc = "SettingsPrivacy",
            screenDesc = "PrivacyContent"
        )
        openSettingsSubscreenAndBack(
            itemDesc = "SettingsNotifications",
            screenDesc = "NotificationsContent"
        )
        openSettingsSubscreenAndBack(
            itemDesc = "SettingsDataStorage",
            screenDesc = "DataStorageContent"
        )
        openSettingsSubscreenAndBack(
            itemDesc = "SettingsFolders",
            screenDesc = "FoldersContent"
        )
        openSettingsSubscreenAndBack(
            itemDesc = "SettingsEditProfile",
            screenDesc = "EditProfileContent"
        )
        openSettingsSubscreenAndBack(
            itemDesc = "SettingsPremium",
            screenDesc = "PremiumContent"
        )

        pressBackAndWait()
        waitForDesc("ChatList")
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.waitForDesc(desc: String, timeout: Long = 5_000) {
        device.wait(Until.hasObject(By.desc(desc)), timeout)
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.clickByDesc(desc: String) {
        device.findObject(By.desc(desc))?.click()
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.scrollByDesc(desc: String) {
        val node = device.findObject(By.desc(desc))
        node?.fling(Direction.DOWN)
        device.waitForIdle()
        node?.fling(Direction.UP)
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.openSettingsSubscreenAndBack(
        itemDesc: String,
        screenDesc: String
    ) {
        clickByDesc(itemDesc)
        waitForDesc(screenDesc)

        val scrollable: UiObject2? = device.findObject(By.desc(screenDesc))
            ?: device.findObject(By.scrollable(true))
        scrollable?.fling(Direction.DOWN)
        device.waitForIdle()
        scrollable?.fling(Direction.UP)
        device.waitForIdle()

        pressBackAndWait()
        waitForDesc("SettingsContent")
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.pressBackAndWait() {
        device.pressBack()
        device.waitForIdle()
    }
}