# Support

Welcome to AutoReply support! We're here to help you get the most out of your automatic messaging experience.

## 📚 Documentation

### Quick Start
1. **Download** the app from [Google Play Store](https://play.google.com/store/apps/details?id=com.matrix.autoreply)
2. **Grant permissions** - Enable notification access when prompted
3. **Configure replies** - Set up custom messages or enable AI features
4. **Enable service** - Toggle the main switch to start auto-replying

### User Guides

#### Basic Setup
- [Installation Guide](README.md#-getting-started)
- [Permission Setup](README.md#%EF%B8%8F-important-notes)
- [Basic Configuration](README.md#-features)

#### Advanced Features
- **AI Smart Replies**: Enable in settings → AI Settings → Choose provider (Groq/OpenAI)
- **Custom Prompts**: Customize AI personality with system messages
- **Reply Delay**: Configure 1-10 second delay for natural responses
- **Message Logs**: View and manage conversation history

#### Supported Platforms
- ✅ WhatsApp (Personal & Business)
- ✅ Instagram Direct Messages
- ✅ Facebook Messenger

## 🆘 Getting Help

### Before Asking for Help

Please check these common solutions first:

#### App Not Working
1. ✅ **Check permissions** - Ensure notification access is enabled
2. ✅ **Restart the app** - Force close and reopen
3. ✅ **Toggle service** - Turn off and on the main switch
4. ✅ **Update app** - Make sure you have the latest version
5. ✅ **Restart device** - Sometimes a device restart helps

#### AI Features Not Working
1. ✅ **Valid API Key** - Check your API key is correct
2. ✅ **Internet connection** - AI features require internet
3. ✅ **Provider status** - Check if Groq/OpenAI services are online
4. ✅ **Enable AI** - Make sure AI replies are turned on in settings

#### Messages Not Auto-Replying
1. ✅ **Service enabled** - Main switch should be ON
2. ✅ **App permissions** - Notification listener access granted
3. ✅ **Battery optimization** - Disable for AutoReply app
4. ✅ **Do Not Disturb** - Check if DND mode is blocking notifications
5. ✅ **Custom replies set** - Ensure you have replies configured

## 💬 Support Channels

### Primary Support
📧 **Email**: [prasoonkumar008@gmail.com](mailto:prasoonkumar008@gmail.com)
- **Response time**: 24-48 hours
- **Best for**: Bug reports, feature requests, account issues

### Community Support
🐛 **GitHub Issues**: [Report Issues](https://github.com/it5prasoon/Auto-Reply-Android/issues)
- **Best for**: Bug reports, feature discussions
- **Public**: Help others with similar issues

### Social Media
📺 **YouTube Demo**: [AutoReply Tutorial](https://youtube.com/shorts/_Dk9octBRbk?si=5lFMLGUn3D-O7VPB)

## 🐛 Reporting Bugs

When reporting a bug, please include:

### Essential Information
- **App version**: Found in Settings → About
- **Android version**: e.g., Android 13
- **Device model**: e.g., Samsung Galaxy S21
- **Steps to reproduce**: Detailed step-by-step instructions
- **Expected vs actual behavior**: What should happen vs what happens
- **Screenshots**: If applicable

### Bug Report Template
```markdown
**Bug Description**
Brief description of the issue

**Steps to Reproduce**
1. Go to...
2. Tap on...
3. See error

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Device Info**
- Device: [e.g., Pixel 6]
- Android: [e.g., 13]
- App Version: [e.g., 2.1.0]

**Additional Context**
Any other relevant information
```

## 🚀 Feature Requests

We welcome feature suggestions! When requesting a feature:

1. **Check existing requests** - Search GitHub issues first
2. **Describe the problem** - What need does this solve?
3. **Propose a solution** - How should it work?
4. **Consider scope** - How does it fit with existing features?

## 🔧 Troubleshooting

### Common Issues & Solutions

#### ❌ "Notification access not granted"
**Solution**: Go to Settings → Apps → Special app access → Notification access → Enable AutoReply

#### ❌ Auto-replies not sending
**Solutions**:
- Check if main service is enabled
- Verify app has notification permissions
- Disable battery optimization for AutoReply
- Check if messaging app notifications are enabled

#### ❌ AI replies not working
**Solutions**:
- Verify API key is correctly entered
- Check internet connection
- Ensure AI provider service is online
- Restart the app

#### ❌ App crashes or freezes
**Solutions**:
- Update to latest version
- Clear app cache: Settings → Apps → AutoReply → Storage → Clear Cache
- Restart device
- Reinstall app if persistent

#### ❌ Battery drain
**Solutions**:
- Update to latest version (optimized for battery life)
- Check other apps aren't causing conflicts
- Restart device to clear background processes

### Advanced Troubleshooting

#### Logs and Debugging
1. Enable developer options on your device
2. Use `adb logcat` to capture system logs
3. Filter for AutoReply package: `adb logcat | grep com.matrix.autoreply`
4. Include relevant logs in bug reports

#### Permission Issues
1. Check notification listener: Settings → Apps → Special access → Notification access
2. Verify app permissions: Settings → Apps → AutoReply → Permissions
3. Battery optimization: Settings → Battery → Battery optimization → AutoReply → Don't optimize

## 📖 FAQ

### General Questions

**Q: Is AutoReply really free?**
A: Yes! Completely free, no ads, no premium features. Open source under MIT license.

**Q: Does AutoReply read my personal messages?**
A: No. The app only processes message notifications to generate replies. Your actual conversation content stays private.

**Q: Why does AutoReply need notification access?**
A: This permission allows the app to detect incoming messages and send automatic replies.

**Q: Can I use AutoReply without internet?**
A: Yes! Custom replies work completely offline. AI features require internet connection.

### Technical Questions

**Q: Which Android versions are supported?**
A: Android 6.0 (API 23) and above.

**Q: Does AutoReply work on tablets?**
A: Yes! AutoReply is optimized for both phones and tablets.

**Q: Can I backup my settings?**
A: Currently not available, but planned for future releases.

**Q: How secure are my API keys?**
A: API keys are stored securely using Android Keystore encryption.

### Feature Questions

**Q: Can I set different replies for different contacts?**
A: Not yet, but contact-specific rules are planned for future releases.

**Q: Can I schedule auto-replies?**
A: Advanced scheduling is planned for future updates.

**Q: Why is there a delay before replies are sent?**
A: The 3-second delay makes responses appear more natural and helps avoid platform spam detection.

## 🤝 Community Guidelines

When seeking or providing support:
- ✅ Be respectful and patient
- ✅ Search before asking
- ✅ Provide detailed information
- ✅ Follow our [Code of Conduct](CODE_OF_CONDUCT.md)
- ✅ Help others when possible

## 📱 System Requirements

- **Android**: 6.0+ (API level 23)
- **RAM**: 2GB+ recommended
- **Storage**: 50MB free space
- **Internet**: Required for AI features only
- **Permissions**: Notification listener access

## 🎯 Feature Roadmap

See our [CHANGELOG.md](CHANGELOG.md) for:
- Recent updates and new features
- Planned improvements
- Version history

## 💝 Support the Project

If AutoReply has helped you, consider:
- ⭐ **Star the repository** on GitHub
- 📝 **Write a review** on Google Play Store
- ☕ **Buy us a coffee**: [Support Link](https://buymeacoffee.com/prasoonk187)
- 🤝 **Contribute**: See [CONTRIBUTING.md](CONTRIBUTING.md)

## 📞 Contact Information

- **Developer**: it5prasoon
- **Email**: [prasoonkumar008@gmail.com](mailto:prasoonkumar008@gmail.com)
- **GitHub**: [@it5prasoon](https://github.com/it5prasoon)
- **Project**: [AutoReply-Android](https://github.com/it5prasoon/Auto-Reply-Android)

---

**Last Updated**: December 2024

Thank you for using AutoReply! We're committed to making your messaging experience better. 🤖✨
