export interface ClassSlot {
  subject: string;
  time: string;
  startTime: string; // 24h "HH:MM"
  room: string;
  icon: string;
  iconBg: string;
  iconColor: string;
  type?: string;
  faculty?: string;
}

export const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'] as const;
export type Day = typeof days[number];

const defaultTimetable: Record<Day, ClassSlot[]> = {
  Mon: [
    { subject: 'Computer Org & Arch', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500', faculty: 'Dr. Kajal' },
    { subject: 'VLSI Design Lab', time: '10:40 – 12:20', startTime: '10:40', room: 'Lab', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500', type: 'Lab', faculty: 'Dr. S.A. Ahsan' },
    { subject: 'DSP Lab', time: '02:00 – 03:40', startTime: '14:00', room: 'Lab', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500', type: 'Lab', faculty: 'Dr. Omkar Singh' },
    { subject: 'VLSI Technology (Elective-II)', time: '03:40 – 04:30', startTime: '15:40', room: 'L-15', icon: 'auto_stories', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500', faculty: 'Dr. Bisma Bilal' },
    { subject: 'Open Elective', time: '04:30 – 05:20', startTime: '16:30', room: 'L-15', icon: 'menu_book', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500', faculty: 'TBA' },
  ],
  Tue: [
    { subject: 'VLSI Design', time: '09:50 – 11:30', startTime: '09:50', room: 'L-15', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500', faculty: 'Dr. S.A. Ahsan' },
    { subject: 'Digital Signal Processing', time: '11:30 – 01:10', startTime: '11:30', room: 'L-15', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500', faculty: 'Dr. Omkar Singh' },
    { subject: 'Open Elective', time: '04:30 – 05:20', startTime: '16:30', room: 'L-15', icon: 'menu_book', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500', faculty: 'TBA' },
  ],
  Wed: [
    { subject: 'Data Comm. & Networking', time: '09:00 – 10:40', startTime: '09:00', room: 'L-15', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500', faculty: 'Dr. Yusra Banday' },
    { subject: 'Digital Signal Processing', time: '10:40 – 11:30', startTime: '10:40', room: 'L-15', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500', faculty: 'Dr. Omkar Singh' },
    { subject: 'VLSI Design', time: '11:30 – 12:20', startTime: '11:30', room: 'L-15', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500', faculty: 'Dr. S.A. Ahsan' },
    { subject: 'VLSI Technology (Elective-II)', time: '03:40 – 04:30', startTime: '15:40', room: 'L-15', icon: 'auto_stories', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500', faculty: 'Dr. Bisma Bilal' },
    { subject: 'Open Elective', time: '04:30 – 05:20', startTime: '16:30', room: 'L-15', icon: 'menu_book', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500', faculty: 'TBA' },
  ],
  Thu: [
    { subject: 'Computer Org & Arch', time: '09:50 – 11:30', startTime: '09:50', room: 'L-15', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500', faculty: 'Dr. Kajal' },
    { subject: 'Data Comm. & Networking', time: '11:30 – 12:20', startTime: '11:30', room: 'L-15', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500', faculty: 'Dr. Yusra Banday' },
  ],
  Fri: [
    { subject: 'VLSI Technology (Elective-II)', time: '03:40 – 04:30', startTime: '15:40', room: 'L-15', icon: 'auto_stories', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500', faculty: 'Dr. Bisma Bilal' },
  ],
};

const TIMETABLE_KEY = 'campus_timetable';
const EXAM_DATE_KEY = 'campus_exam_date';
const DEFAULT_EXAM_DATE = '2026-04-01';

// Listeners for reactivity
type Listener = () => void;
const listeners: Set<Listener> = new Set();

export function subscribe(fn: Listener) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

function notify() {
  listeners.forEach(fn => fn());
}

export function getTimetableData(): Record<Day, ClassSlot[]> {
  try {
    const stored = localStorage.getItem(TIMETABLE_KEY);
    if (stored) return JSON.parse(stored);
  } catch {}
  return defaultTimetable;
}

export function saveTimetableData(data: Record<Day, ClassSlot[]>) {
  localStorage.setItem(TIMETABLE_KEY, JSON.stringify(data));
  // Update the mutable export
  Object.assign(timetableData, data);
  notify();
}

export function resetTimetableData() {
  localStorage.removeItem(TIMETABLE_KEY);
  Object.assign(timetableData, defaultTimetable);
  notify();
}

export function getExamDate(): string {
  return localStorage.getItem(EXAM_DATE_KEY) || DEFAULT_EXAM_DATE;
}

export function setExamDate(date: string) {
  localStorage.setItem(EXAM_DATE_KEY, date);
  notify();
}

// Mutable timetable that other modules import
export const timetableData: Record<Day, ClassSlot[]> = getTimetableData();

export const dayNames: Record<Day, string> = {
  Mon: 'Monday', Tue: 'Tuesday', Wed: 'Wednesday', Thu: 'Thursday', Fri: 'Friday',
};

export function getTodayDay(): Day | null {
  const d = new Date().getDay();
  // 0=Sun, 6=Sat → no classes
  if (d === 0 || d === 6) return null;
  const map: Day[] = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  return map[d - 1];
}

export function getTodayClasses(): ClassSlot[] {
  const day = getTodayDay();
  if (!day) return [];
  return timetableData[day] || [];
}

export function getClassStatus(startTime: string, durationMin = 50): 'current' | 'upcoming' | 'done' {
  const now = new Date();
  const currentMin = now.getHours() * 60 + now.getMinutes();
  const [h, m] = startTime.split(':').map(Number);
  const classStart = h * 60 + m;
  const classEnd = classStart + durationMin;

  if (currentMin >= classStart && currentMin < classEnd) return 'current';
  if (currentMin < classStart) return 'upcoming';
  return 'done';
}
