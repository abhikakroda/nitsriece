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
    { subject: 'Computer Org & Arch', time: '09:00 – 09:50', startTime: '09:00', room: 'L-15', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500', faculty: 'Dr. Kajal' },
    { subject: 'VLSI Design Lab (G3)', time: '09:50 – 10:40', startTime: '09:50', room: 'Lab', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500', type: 'Lab', faculty: 'Dr. Brajendra & Dr. Aamir' },
    { subject: 'DSP Lab-I (G3)', time: '11:30 – 12:20', startTime: '11:30', room: 'Lab', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500', type: 'Lab', faculty: 'Dr. Mahroosh Banday' },
    { subject: 'Elective-II', time: '12:20 – 01:10', startTime: '12:20', room: 'L-15', icon: 'auto_stories', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500' },
    { subject: 'IE-2', time: '01:10 – 02:00', startTime: '13:10', room: 'L-15', icon: 'school', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500', faculty: 'Dr. Arshid' },
  ],
  Tue: [
    { subject: 'Data Comm. & Networking', time: '09:00 – 09:50', startTime: '09:00', room: 'L-15', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500', faculty: 'Dr. Yusra Banday' },
    { subject: 'VLSI Design', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500', faculty: 'Dr. S.A. Ahsan' },
  ],
  Wed: [
    { subject: 'Digital Signal Processing', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500', faculty: 'Dr. Omkar Singh' },
  ],
  Thu: [
    { subject: 'Computer Org & Arch', time: '09:00 – 09:50', startTime: '09:00', room: 'L-15', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500', faculty: 'Dr. Kajal' },
    { subject: 'Data Comm. & Networking', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500', faculty: 'Dr. Yusra Banday' },
  ],
  Fri: [
    { subject: 'RF Design', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'settings_input_antenna', iconBg: 'bg-blue-500/10', iconColor: 'text-blue-500', faculty: 'Prof. Najeeb-ud-din' },
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
