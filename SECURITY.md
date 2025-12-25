# Security Policy

## Supported Versions

We release patches for security vulnerabilities. Which versions are eligible for receiving such patches depends on the CVSS v3.0 Rating:

| Version | Supported          |
| ------- | ------------------ |
| Latest  | ✅ Yes             |
| Previous major | ✅ Yes       |
| < Previous major | ❌ No      |

## Reporting a Vulnerability

The AutoReply team takes security bugs seriously. We appreciate your efforts to responsibly disclose your findings, and will make every effort to acknowledge your contributions.

### How to Report

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report them via email to:

📧 **Email**: [prasoonkumar008@gmail.com](mailto:prasoonkumar008@gmail.com)

**Subject Line**: `[SECURITY] Brief description of vulnerability`

### What to Include

Please include the following information along with your report:

- Type of issue (e.g., buffer overflow, SQL injection, cross-site scripting, etc.)
- Full paths of source file(s) related to the manifestation of the issue
- The location of the affected source code (tag/branch/commit or direct URL)
- Any special configuration required to reproduce the issue
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit the issue

This information will help us triage your report more quickly.

### Response Timeline

- **Initial Response**: We aim to respond to security reports within 48 hours
- **Updates**: We will send you regular updates about our progress at least every 7 days
- **Resolution**: We target resolving critical vulnerabilities within 30 days
- **Disclosure**: Once fixed, we will work with you on coordinated disclosure

### Safe Harbor

We support safe harbor for security researchers who:

- Make a good faith effort to avoid privacy violations, destruction of data, and interruption or degradation of our services
- Only interact with accounts you own or with explicit permission of the account holder
- Do not access a system beyond what is necessary to demonstrate a vulnerability
- Report vulnerabilities as soon as you discover them
- Do not violate any other applicable laws or regulations

## Security Considerations

### For Users

- **Keep the app updated**: Always use the latest version from Google Play Store
- **Review permissions**: The app only requires notification access to function
- **API Keys**: Keep your AI API keys secure and never share them
- **Message Privacy**: Your messages are processed locally and not transmitted unless AI features are enabled

### For Developers

- **Input Validation**: Always validate and sanitize user inputs
- **Secure Storage**: Use Android Keystore for sensitive data
- **Network Security**: Use HTTPS for all API communications
- **Permissions**: Request only necessary permissions
- **Code Review**: All code changes require review before merging

## Known Security Features

### Privacy Protection
- **Local Processing**: Custom replies work entirely offline
- **No Data Collection**: We don't collect or store personal conversations
- **API Key Security**: AI API keys are stored securely using Android Keystore
- **Minimal Permissions**: Only requires notification listener permission

### AI Safety
- **Anti-Scam Protection**: Built-in protection against money scams and threats
- **Content Filtering**: AI responses are filtered for inappropriate content
- **Rate Limiting**: API calls are rate-limited to prevent abuse
- **Context Isolation**: Conversation contexts are isolated per user

## Security Updates

Security updates will be released as patch versions and announced through:

- GitHub Releases with security advisories
- Google Play Store app updates
- Project README updates

## Vulnerability Disclosure Process

1. **Report received**: We acknowledge receipt within 48 hours
2. **Initial assessment**: We perform initial validation (1-7 days)
3. **Investigation**: We investigate and develop a fix (7-30 days)
4. **Fix deployment**: We deploy the fix and notify you
5. **Public disclosure**: We coordinate public disclosure with you

## Security Hall of Fame

We recognize security researchers who help improve AutoReply's security:

<!-- Future contributors will be listed here -->

Thank you for helping keep AutoReply and our users safe! 🔒

## Contact

For security-related questions or concerns:

- **Email**: [prasoonkumar008@gmail.com](mailto:prasoonkumar008@gmail.com)
- **Subject**: `[SECURITY] Your question here`

For general questions, please use GitHub Issues or regular support channels.

---

**Last Updated**: December 2024
