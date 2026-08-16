# Process boundaries

- Accessibility service runs in an isolated process (`:serviceProcess`).
- Do not rely on shared statics across processes.
- Treat reflection / private APIs as unstable; always null-safe fallback.
- Notification listener and widget host run in the main app process.
- Log process/service boundary failures with structured messages; never log PII.
