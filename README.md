<div align="center">
  <h1>🤖 AutoReply</h1>
  <p><strong>Smart Automatic Messaging for Android</strong></p>
  
  <p>
    <a href="https://play.google.com/store/apps/details?id=com.matrix.autoreply">
      <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80">
    </a>
  </p>
  
  <p>
    <img alt="Android" src="https://img.shields.io/badge/Platform-Android-green.svg">
    <img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg">
    <img alt="Kotlin" src="https://img.shields.io/badge/Language-Kotlin-blue.svg">
    <img alt="License" src="https://img.shields.io/badge/License-MIT-yellow.svg">
  </p>
</div>

## ✨ Overview

AutoReply is a powerful Android application that automatically responds to messages on WhatsApp and Facebook Messenger. Stay connected with your contacts even when you're busy, sleeping, or away from your phone.

### 🎯 Key Benefits
- **Never miss a message** - Automatic responses keep conversations flowing
- **Customizable replies** - Set personalized messages for different situations  
- **Smart frequency control** - Avoid spam with intelligent reply timing
- **Privacy focused** - No data collection or tracking
- **Completely free** - Open source with no ads or premium features

## 📱 Screenshots

<div align="center">
  <table>
    <tr>
      <td><img src="screenshots/screenshots/screenshot_1.png" width="200"/></td>
      <td><img src="screenshots/screenshots/screenshot_2.png" width="200"/></td>
      <td><img src="screenshots/screenshots/screenshot_3.png" width="200"/></td>
      <td><img src="screenshots/screenshots/screenshot_4.png" width="200"/></td>
    </tr>
    <tr>
      <td align="center"><strong>Main Interface</strong></td>
      <td align="center"><strong>Custom Replies</strong></td>
      <td align="center"><strong>Message Logs</strong></td>
      <td align="center"><strong>Settings</strong></td>
    </tr>
  </table>
</div>

## 💰 Support the Project

<div align="center">
  <a href="https://buymeacoffee.com/prasoonk187">
    <img src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black" alt="Buy Me A Coffee">
  </a>
</div>



## 🎆 Features

<div align="center">
  <table>
    <tr>
      <td align="center">
        <h3>💬 Auto Responses</h3>
        <p>Automated replies for individual and group chats</p>
      </td>
      <td align="center">
        <h3>🔒 Privacy First</h3>
        <p>No data collection or tracking of your activities</p>
      </td>
    </tr>
    <tr>
      <td align="center">
        <h3>⏱️ Smart Timing</h3>
        <p>Control reply frequency to avoid spam</p>
      </td>
      <td align="center">
        <h3>📁 Message Logs</h3>
        <p>Keep records even if messages are deleted</p>
      </td>
    </tr>
    <tr>
      <td align="center">
        <h3>🆓 Completely Free</h3>
        <p>No ads, no premium features, open source</p>
      </td>
      <td align="center">
        <h3>🔄 Auto Updates</h3>
        <p>In-app updates keep you current</p>
      </td>
    </tr>
  </table>
</div>

## 🛠️ Tech Stack

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
      </td>
      <td align="center">
        <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
      </td>
      <td align="center">
        <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">
      </td>
    </tr>
    <tr>
      <td align="center">
        <img src="https://img.shields.io/badge/Room-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Room">
      </td>
      <td align="center">
        <img src="https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white" alt="Retrofit">
      </td>
      <td align="center">
        <img src="https://img.shields.io/badge/Coroutines-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Coroutines">
      </td>
    </tr>
  </table>
</div>

### 📚 Libraries & Components
- **Kotlin** - Modern programming language
- **Firebase** - Backend services and analytics
- **Room Database** - Local data storage
- **Retrofit** - Network communication
- **Coroutines** - Asynchronous programming
- **Notification Listener** - Core messaging functionality

## 📋 Roadmap

- [ ] Implement Dependency Injection (Dagger/Hilt)
- [ ] UI/UX improvements and Material Design 3
- [ ] MVVM architecture refactoring
- [ ] Multi-language support expansion
- [ ] Advanced scheduling features

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 23+
- Kotlin 1.9+

### 🔧 Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/it5prasoon/Auto-Reply-Android.git
   cd Auto-Reply-Android
   ```

2. **Configure Firebase**
   - Add your `google-services.json` file to `app/src/main/`
   - Get it from [Firebase Console](https://console.firebase.google.com/)

3. **Configure AdMob (Optional)**
   - Create `app/src/main/res/values/ad_mob_config.xml`:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="admob_app_id">your_app_id</string>
       <string name="msg_logs_banner">your_banner_id</string>
       <string name="main_banner">your_banner_id</string>
       <string name="save_custom_reply_interstitial">your_interstitial_id</string>
   </resources>
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### 📜 Contribution Guidelines
- Follow Kotlin coding conventions
- Write clear commit messages
- Test your changes thoroughly
- Update documentation if needed

## ⚠️ Important Notes

- **Reply Timing**: The app includes a 10-second delay between replies to prevent spam (configurable)
- **Privacy**: This app is not affiliated with WhatsApp, Facebook, or any messaging platform
- **Permissions**: Notification access is required for the app to function properly

## 📞 Support & Contact

<div align="center">
  <p>Need help or have questions?</p>
  
  <a href="mailto:prasoonk187@gmail.com">
    <img src="https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white" alt="Email">
  </a>
  <a href="https://github.com/it5prasoon/Auto-Reply-Android/issues">
    <img src="https://img.shields.io/badge/Issues-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Issues">
  </a>
</div>

## 📜 License

```
MIT License

Copyright (c) 2024 AutoReply

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<div align="center">
  <p><strong>Made with ❤️ by the AutoReply Community</strong></p>
  <p>If this project helped you, please consider giving it a ⭐️</p>
</div>
