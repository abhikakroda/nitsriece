

# 📱 College Student Companion — PWA App

A mobile-first Progressive Web App for ECE engineering students to manage their daily academic life — installable from the browser, works offline, no app store needed.

---

## Pages & Features

### 1. **Dashboard (Home)**
- Student greeting header with profile photo and notification bell
- Date display with "Daily Schedule" heading
- **Notices card** — gradient hero card showing latest announcements (e.g., exam date sheets)
- **Today's Timetable** — class cards showing subject, time, room, and current/upcoming status with Present/Absent attendance buttons
- **Recent Resources** — quick access to uploaded PDFs and assignments
- **Quick Access Grid** — 6 colorful shortcut tiles: Study Groups, Focus Mode, GPA Estimator, Syllabus Tracker, Placement Cell, Faculty Directory
- Glassmorphism header with animated liquid blob backgrounds

### 2. **Focus Mode (Pomodoro Timer)**
- Full-screen dark-themed zen mode with animated SVG progress ring
- Focus / Short Break / Long Break modes (25min / 5min / 15min)
- Play/Pause/Reset controls with current task display
- Dynamic background that shifts when timer is active

### 3. **Faculty Directory**
- Searchable list of professors with photo, name, position, domain, cabin location, and email
- Sticky header with search bar

### 4. **Placement Cell**
- Hero card for current placement season
- List of upcoming company drives with role, CTC, date, and eligibility
- Quick Prep grid — Aptitude, Coding, Core ECE, Interview prep categories

### 5. **Profile & Settings**
- Profile photo with edit button, name, department, and ID badge
- Notification toggles (Attendance Alerts, Class Reminders)
- Preferences (Theme Mode, Language)
- Account management (Change Password, Logout)

### 6. **Bottom Navigation Bar**
- Persistent bottom nav with Home, Schedule, Stats, and Profile tabs (as shown in the screenshot)

---

## PWA Setup
- Install `vite-plugin-pwa` for service worker, offline caching, and install prompt
- App manifest with proper icons, theme color, and splash screens
- Mobile-optimized meta tags for iOS and Android
- Dedicated install prompt page

## Design Style
- Mobile-first (optimized for phone screens)
- Glassmorphism + gradient cards + animated liquid blob backgrounds
- Google Material Symbols icons throughout
- Light/dark mode support
- Smooth micro-interactions and transitions

## Data Approach
- All data is hardcoded/static for now (timetable, faculty, placements)
- No backend needed initially — can be added later if you want real auth, dynamic data, etc.

