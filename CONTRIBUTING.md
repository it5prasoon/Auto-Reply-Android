# Contributing to AutoReply

We love your input! We want to make contributing to AutoReply as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Process

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

## Pull Requests

Pull requests are the best way to propose changes to the codebase. We actively welcome your pull requests:

1. **Fork the repository** and create your branch from `main`.
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/yourusername/Auto-Reply-Android.git
   cd Auto-Reply-Android
   ```
3. **Set up the development environment**:
   - Install Android Studio Arctic Fox or newer
   - Configure Firebase by adding `google-services.json` to `app/src/main/`
   - Optional: Configure AdMob by creating `ad_mob_config.xml`
4. **Create a feature branch**:
   ```bash
   git checkout -b feature/amazing-feature
   ```
5. **Make your changes** and ensure they follow our coding standards
6. **Test thoroughly** on different Android versions and devices
7. **Commit your changes** with clear, descriptive messages:
   ```bash
   git commit -m 'Add amazing feature: brief description'
   ```
8. **Push to your fork**:
   ```bash
   git push origin feature/amazing-feature
   ```
9. **Create a Pull Request** from your branch to our `main` branch

## Code Style

### Kotlin Guidelines
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused
- Use proper indentation (4 spaces)

### Android Guidelines
- Follow [Android Architecture Guidelines](https://developer.android.com/guide/architecture)
- Use proper resource naming conventions
- Optimize for different screen sizes and densities
- Follow Material Design principles

### Example Code Style
```kotlin
class MessageProcessor {
    companion object {
        private const val MAX_RETRY_COUNT = 3
    }
    
    /**
     * Processes incoming notification and generates appropriate response
     * @param notification The notification listener event
     * @return ProcessResult indicating success or failure
     */
    fun processNotification(notification: StatusBarNotification): ProcessResult {
        val messageText = extractMessageText(notification)
            ?: return ProcessResult.Error("Failed to extract message")
        
        return when (val response = generateResponse(messageText)) {
            is ResponseResult.Success -> {
                sendReply(response.message)
                ProcessResult.Success
            }
            is ResponseResult.Error -> ProcessResult.Error(response.message)
        }
    }
}
```

## Testing

- Write unit tests for new functionality
- Test on multiple Android versions (API 23+)
- Test on different devices and screen sizes
- Verify functionality on WhatsApp, Instagram, and Messenger
- Test both AI and custom reply modes

### Running Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Reporting Bugs

We use GitHub Issues to track bugs. Report a bug by [opening a new issue](https://github.com/it5prasoon/Auto-Reply-Android/issues/new?template=bug_report.md).

**Great Bug Reports** tend to have:

- A quick summary and/or background
- Steps to reproduce
  - Be specific!
  - Give sample code if you can
- What you expected would happen
- What actually happens
- Device information (Android version, device model)
- App version
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

### Bug Report Template
```markdown
**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Device Information:**
 - Device: [e.g. Samsung Galaxy S21]
 - OS: [e.g. Android 12]
 - App Version: [e.g. v2.1.0]

**Additional context**
Add any other context about the problem here.
```

## Feature Requests

We also use GitHub Issues for feature requests. You can [request a feature](https://github.com/it5prasoon/Auto-Reply-Android/issues/new?template=feature_request.md).

**Great Feature Requests** include:

- A clear and concise description of what the problem is
- A clear and concise description of what you want to happen
- Any additional context or screenshots about the feature request
- Consider if this feature aligns with the project's goals

## Security Issues

Please do not report security vulnerabilities through public GitHub issues. Instead, please refer to our [SECURITY.md](SECURITY.md) file for instructions on responsible disclosure.

## Development Setup

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- Android SDK 23+ (API level 23)
- Kotlin 1.9+
- Git

### Local Development
1. Clone the repository
2. Open in Android Studio
3. Add your `google-services.json` for Firebase (required)
4. Configure AdMob (optional, see README.md)
5. Build and run on device/emulator

### Project Structure
```
app/
├── src/main/java/com/matrix/autoreply/
│   ├── constants/          # App constants
│   ├── model/             # Data models
│   ├── network/           # API services
│   ├── ui/                # Activities, Fragments, Dialogs
│   ├── utils/             # Utility classes
│   └── AutoReplyApp.kt    # Application class
├── src/main/res/          # Resources
└── src/main/python/       # Python scripts (if any)
```

## Commit Message Guidelines

Write clear and meaningful commit messages:

### Format
```
type(scope): brief description

Detailed description if needed

Fixes #123
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (no functionality change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

### Examples
```bash
feat(ai): add support for Claude AI provider
fix(notifications): resolve crash when processing Instagram messages
docs(readme): update setup instructions for Firebase
refactor(database): migrate to Room database from SQLite
```

## Code Review Process

All submissions require review before merging:

1. **Automated checks** must pass (build, tests, linting)
2. **Manual review** by maintainers focuses on:
   - Code quality and style
   - Architecture and design decisions
   - Security considerations
   - Performance impact
   - User experience
   - Documentation completeness

## Community

- Be respectful and inclusive
- Help others learn and grow
- Follow our [Code of Conduct](CODE_OF_CONDUCT.md)
- Join discussions in issues and pull requests

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes for significant contributions
- GitHub contributors page

## Questions?

Feel free to:
- Open a discussion issue
- Contact us at prasoonkumar008@gmail.com
- Review existing issues and documentation

Thank you for contributing to AutoReply! 🤖✨
