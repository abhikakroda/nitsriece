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

export const timetableData: Record<Day, ClassSlot[]> = {
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

export const dayNames: Record<Day, string> = {
  Mon: 'Monday', Tue: 'Tuesday', Wed: 'Wednesday', Thu: 'Thursday', Fri: 'Friday',
};

export function getTodayDay(): Day {
  const d = new Date().getDay();
  const map: Day[] = ['Mon', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  return map[d] || 'Mon';
}

export function getTodayClasses(): ClassSlot[] {
  return timetableData[getTodayDay()] || [];
}

/** Returns 'current', 'upcoming', or 'done' based on current time */
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
