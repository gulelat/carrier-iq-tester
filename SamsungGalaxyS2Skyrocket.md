Some of the information learned about Carrier IQ on this phone.

# Introduction #

iSEC obtained this phone to perform their own analysis of the Carrier IQ app to aid in developing this Carrier IQ Tester app.  This page details some of the things we discovered.

# framework-res.apk AndroidManifest.xml dump #

Android developer forums noted that much of the Carrier IQ functionality could be found in the framework-res.apk.  A dump of the AndroidManifest.xml file for this apk shows the following Carrier IQ items:

```
      E: service (line=1592)
        A: android:label(0x01010001)="IQ Agent Service" (Raw: "IQ Agent Service")
        A: android:name(0x01010003)="com.carrieriq.iqagent.service.IQService" (Raw: "com.carrieriq.iqagent.service.IQService")
        A: android:enabled(0x0101000e)=(type 0x12)0xffffffff
        A: android:exported(0x01010010)=(type 0x12)0xffffffff
        A: android:process(0x01010011)="android.iqd" (Raw: "android.iqd")
        E: intent-filter (line=1598)
          E: action (line=1599)
            A: android:name(0x01010003)="com.carrieriq.iqagent.service.IQService" (Raw: "com.carrieriq.iqagent.service.IQService")
      E: receiver (line=1605)
        A: android:label(0x01010001)="IQ Agent Autostarter" (Raw: "IQ Agent Autostarter")
        A: android:name(0x01010003)="com.carrieriq.iqagent.service.receivers.BootCompletedReceiver" (Raw: "com.carrieriq.iqagent.service.receivers.BootCompletedReceiver")
        A: android:enabled(0x0101000e)=(type 0x12)0xffffffff
        A: android:exported(0x01010010)=(type 0x12)0xffffffff
        A: android:process(0x01010011)="android.iqd" (Raw: "android.iqd")
        E: intent-filter (line=1611)
          E: action (line=1612)
            A: android:name(0x01010003)="android.intent.action.BOOT_COMPLETED" (Raw: "android.intent.action.BOOT_COMPLETED")
      E: activity (line=1616)
        A: android:label(0x01010001)="IQ Agent Settings" (Raw: "IQ Agent Settings")
        A: android:name(0x01010003)="com.carrieriq.iqagent.service.ui.UserPage" (Raw: "com.carrieriq.iqagent.service.ui.UserPage")
        A: android:enabled(0x0101000e)=(type 0x12)0xffffffff
        A: android:exported(0x01010010)=(type 0x12)0xffffffff
        A: android:process(0x01010011)="android.iqd" (Raw: "android.iqd")
        E: intent-filter (line=1622)
          E: action (line=1623)
            A: android:name(0x01010003)="com.carrieriq.iqagent.service.ui.DebugSettings" (Raw: "com.carrieriq.iqagent.service.ui.DebugSettings")
          E: category (line=1624)
            A: android:name(0x01010003)="android.intent.category.DEFAULT" (Raw: "android.intent.category.DEFAULT")
      E: activity (line=1629)
        A: android:label(0x01010001)="IQ Agent Message Dialog" (Raw: "IQ Agent Message Dialog")
        A: android:name(0x01010003)="com.carrieriq.iqagent.service.ui.ShowMessage" (Raw: "com.carrieriq.iqagent.service.ui.ShowMessage")
        A: android:enabled(0x0101000e)=(type 0x12)0xffffffff
        A: android:exported(0x01010010)=(type 0x12)0x0
        A: android:process(0x01010011)="android.iqd" (Raw: "android.iqd")
        A: android:stateNotNeeded(0x01010016)=(type 0x12)0xffffffff
        A: android:excludeFromRecents(0x01010017)=(type 0x12)0xffffffff
      E: activity (line=1640)
        A: android:theme(0x01010000)=@0x103000b
        A: android:name(0x01010003)="com.carrieriq.iqagent.stdmetrics.survey.android.QuestionnaireLaunchActivity" (Raw: "com.carrieriq.iqagent.stdmetrics.survey.android.QuestionnaireLaunchActivity")
        A: android:enabled(0x0101000e)=(type 0x12)0xffffffff
        A: android:exported(0x01010010)=(type 0x12)0x0
        A: android:process(0x01010011)="android.iqd" (Raw: "android.iqd")
        A: android:stateNotNeeded(0x01010016)=(type 0x12)0xffffffff
        A: android:excludeFromRecents(0x01010017)=(type 0x12)0xffffffff
      E: activity (line=1651)
        A: android:name(0x01010003)="com.carrieriq.iqagent.stdmetrics.survey.android.QuestionnaireActivity" (Raw: "com.carrieriq.iqagent.stdmetrics.survey.android.QuestionnaireActivity")
        A: android:enabled(0x0101000e)=(type 0x12)0xffffffff
        A: android:exported(0x01010010)=(type 0x12)0x0
        A: android:process(0x01010011)="anrdoid.iqd" (Raw: "anrdoid.iqd")
        A: android:stateNotNeeded(0x01010016)=(type 0x12)0xffffffff
        A: android:excludeFromRecents(0x01010017)=(type 0x12)0xffffffff
        E: intent-filter (line=1658)
          E: action (line=1659)
            A: android:name(0x01010003)="com.carrieriq.iqagent.survey.android.VIEW_SURVEY" (Raw: "com.carrieriq.iqagent.survey.android.VIEW_SURVEY")
    E: permission-group (line=1672)
      A: android:label(0x01010001)="IQ Agent Client Permissions" (Raw: "IQ Agent Client Permissions")
      A: android:name(0x01010003)="com.carrieriq.iqagent.client" (Raw: "com.carrieriq.iqagent.client")
    E: permission-group (line=1677)
      A: android:label(0x01010001)="IQ Agent Service Permissions" (Raw: "IQ Agent Service Permissions")
      A: android:name(0x01010003)="com.carrieriq.iqagent.service" (Raw: "com.carrieriq.iqagent.service")
    E: permission (line=1682)
      A: android:label(0x01010001)="Submit a metric to the IQ Agent" (Raw: "Submit a metric to the IQ Agent")
      A: android:name(0x01010003)="com.carrieriq.iqagent.client.SUBMIT_METRIC" (Raw: "com.carrieriq.iqagent.client.SUBMIT_METRIC")
      A: android:protectionLevel(0x01010009)=(type 0x10)0x0
      A: android:permissionGroup(0x0101000a)="com.carrieriq.iqagent.client" (Raw: "com.carrieriq.iqagent.client")
    E: permission (line=1690)
      A: android:label(0x01010001)="Use the shared memory interface to the IQ Agent" (Raw: "Use the shared memory interface to the IQ Agent")
      A: android:name(0x01010003)="com.carrieriq.iqagent.client.SHMEM" (Raw: "com.carrieriq.iqagent.client.SHMEM")
      A: android:protectionLevel(0x01010009)=(type 0x10)0x2
      A: android:permissionGroup(0x0101000a)="com.carrieriq.iqagent.client" (Raw: "com.carrieriq.iqagent.client")
    E: permission (line=1698)
      A: android:label(0x01010001)="Submit an SMS to the IQ Agent to be inspected for specia  instructions" (Raw: "Submit an SMS to the IQ Agent to be inspected for specia  instructions")
      A: android:name(0x01010003)="com.carrieriq.iqagent.client.CHECK_SMS" (Raw: "com.carrieriq.iqagent.client.CHECK_SMS")
      A: android:protectionLevel(0x01010009)=(type 0x10)0x3
      A: android:permissionGroup(0x0101000a)="com.carrieriq.iqagent.client" (Raw: "com.carrieriq.iqagent.client")
    E: permission (line=1708)
      A: android:label(0x01010001)="Set the IQ Agent collection profile" (Raw: "Set the IQ Agent collection profile")
      A: android:name(0x01010003)="com.carrieriq.iqagent.client.SET_PROFILE" (Raw: "com.carrieriq.iqagent.client.SET_PROFILE")
      A: android:protectionLevel(0x01010009)=(type 0x10)0x3
      A: android:permissionGroup(0x0101000a)="com.carrieriq.iqagent.client" (Raw: "com.carrieriq.iqagent.client")
    E: permission (line=1716)
      A: android:label(0x01010001)="Start or stop the IQ Agent service" (Raw: "Start or stop the IQ Agent service")
      A: android:name(0x01010003)="com.carrieriq.iqagent.client.STARTSTOP" (Raw: "com.carrieriq.iqagent.client.STARTSTOP")
      A: android:protectionLevel(0x01010009)=(type 0x10)0x3
      A: android:permissionGroup(0x0101000a)="com.carrieriq.iqagent.client" (Raw: "com.carrieriq.iqagent.client")
```

## Carrier IQ related files ##
These files have names that show they have something to do with Carrier IQ.

```
/system/bin/iqmsd
/system/lib/libiq_client.so
/system/lib/libiq_service.so
/system/xbin/uniq
/sys/devices/platform/SDIO_CIQ
/sys/devices/virtual/tty/sdio_tty_ciq_00
/sys/bus/platform/devices/SDIO_CIQ
/sys/bus/platform/drivers/SDIO_CIQ_TEST
/sys/bus/platform/drivers/SDIO_CIQ
/sys/bus/platform/drivers/SDIO_CIQ/SDIO_CIQ
/sys/class/tty/sdio_tty_ciq_00
/data/property/persist.iq.logging.enabled
/data/system/app_iq_archive
/dev/sdio_tty_ciq_00
```

## com.carrieriq.iqagent.service.ui.UserPage ##
If you run "am start -n" on this activity from a rooted adb shell on the phone, you are presented with a GUI for managing CarrierIQ.  Screenshots of the default settings for the Skyrocket are shown next.

![http://wiki.carrier-iq-tester.googlecode.com/git/images/device-2011-12-07-113217.png](http://wiki.carrier-iq-tester.googlecode.com/git/images/device-2011-12-07-113217.png)

The above image is the main settings page.

![http://wiki.carrier-iq-tester.googlecode.com/git/images/device-2011-12-07-113318.png](http://wiki.carrier-iq-tester.googlecode.com/git/images/device-2011-12-07-113318.png)

The above images shows the options when you select the CADeT connectivity option.