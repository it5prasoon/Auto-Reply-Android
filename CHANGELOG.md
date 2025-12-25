# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Implement Dependency Injection (Dagger/Hilt)
- UI/UX improvements and Material Design 3
- MVVM architecture refactoring
- Multi-language support expansion
- Advanced scheduling features
- Support for additional messaging platforms (Telegram, Slack, Discord)
- Contact-specific rules and VIP contacts

## [2.1.0] - 2024-12-01

### Added
- 🧠 **Conversational Context (AI Memory)** - AI now remembers conversation history for multi-turn chats
  - Per-user conversation isolation
  - Automatic session expiry after 30 minutes of inactivity
  - Active contexts indicator on dashboard
- ⏱️ **Configurable Reply Delay** - Set custom delay (1-10 seconds) for natural response timing
  - Default 3-second delay for human-like responses
  - Prevents platform spam detection
  - Configurable from main dashboard
- 💼 **WhatsApp Business Support** - Full auto-reply functionality for WhatsApp Business
  - Separate app support alongside regular WhatsApp
  - Business-specific message log filtering
  - Professional use case optimization
- 🛡️ **Anti-Scam Safety Protection** - Optional protection against fraudulent messages
  - Money scam detection and deflection
  - Threat and violence message protection
  - OTP/password information safety
  - User-controlled enable/disable option

### Improved
- Enhanced AI response coherence with conversation context
- Better message log organization with app-specific filtering
- Improved user experience with natural response timing
- Strengthened security against social engineering attacks

## [2.0.0] - 2024-10-01

### Added
- 🤖 **AI-Powered Smart Replies** with Groq & OpenAI integration
- ✨ **AI Prompt Templates System** - 10 pre-made personality styles
  - Custom Prompt (blank slate)
  - Friendly & Casual
  - Professional
  - Busy Person
  - Humorous & Fun
  - Supportive & Caring
  - Minimal Responder
  - Enthusiastic
  - Academic/Student
  - Entrepreneur/Hustler
- 🎯 **AI Prompt Generator** - Use AI to refine prompts with natural language
- 📊 **Analytics Dashboard** - Track daily/total replies and usage metrics
  - Today's replies counter
  - Total replies all-time
  - AI vs Custom usage breakdown
  - Per-app tracking (WhatsApp, Instagram, Messenger)
- 🔥 **Firebase Analytics** integration for detailed usage insights
- 📱 **Instagram Direct Messages** support
- 📁 **Enhanced Message Logs** with better organization and filtering

### Changed
- Complete UI overhaul with modern Material Design
- Improved AI response quality and context understanding
- Better error handling and user feedback
- Enhanced privacy controls and settings

### Security
- Secure API key storage using Android Keystore
- Local message processing with optional AI enhancement
- No personal data collection or tracking

## [1.5.0] - 2024-07-01

### Added
- **Facebook Messenger** auto-reply support
- **Custom Reply Editor** with rich text formatting
- **Message frequency control** to prevent spam
- **Dark mode** support system-wide
- **Export/Import settings** functionality

### Improved
- Better notification parsing for various messaging apps
- Improved reliability and crash handling
- Enhanced user interface with better navigation
- Optimized battery usage

### Fixed
- Notification listener service stability issues
- Memory leaks in background processing
- Crash on Android 14+ devices

## [1.0.0] - 2024-04-01

### Added
- ✅ **Initial Release** - AutoReply for WhatsApp
- 💬 **Custom Auto Replies** - Set personalized automatic responses
- 📱 **WhatsApp Integration** - Full support for WhatsApp personal accounts
- 🔔 **Notification Listener** - Core message detection functionality
- ⚙️ **Settings Dashboard** - Easy configuration and management
- 📊 **Basic Message Logs** - Track sent auto-replies
- 🎛️ **Service Toggle** - Easy enable/disable functionality
- 🔒 **Privacy First** - Local processing, no data collection
- 🆓 **Completely Free** - No ads, no premium features
- 📖 **Open Source** - MIT licensed

### Features
- Works without root access
- Offline functionality for custom replies
- Simple and intuitive user interface
- Minimal permissions required
- Battery optimized background service

---

## Release Notes

### Version Numbering
- **Major.Minor.Patch** (e.g., 2.1.0)
- **Major**: Breaking changes or significant feature additions
- **Minor**: New features, backward compatible
- **Patch**: Bug fixes and small improvements

### Download
- **Google Play Store**: [AutoReply](https://play.google.com/store/apps/details?id=com.matrix.autoreply)
- **GitHub Releases**: [Releases Page](https://github.com/it5prasoon/Auto-Reply-Android/releases)

### Support
For support and bug reports, please see our [SUPPORT.md](SUPPORT.md) file or contact us at prasoonkumar008@gmail.com.

---

**Legend:**
- 🆕 New Feature
- 🔧 Improvement  
- 🐛 Bug Fix
- 🔒 Security
- 🗑️ Deprecated
- ❌ Removed
