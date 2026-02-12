// Attendance stored per subject per date: { "2026-02-12_Computer Org & Arch": "present" | "absent" }

const ATTENDANCE_KEY = 'campus_attendance';

export interface AttendanceRecord {
  [key: string]: 'present' | 'absent';
}

export function getAll(): AttendanceRecord {
  try {
    return JSON.parse(localStorage.getItem(ATTENDANCE_KEY) || '{}');
  } catch {
    return {};
  }
}

function makeKey(date: string, subject: string) {
  return `${date}_${subject}`;
}

export function markAttendance(date: string, subject: string, status: 'present' | 'absent') {
  const records = getAll();
  records[makeKey(date, subject)] = status;
  localStorage.setItem(ATTENDANCE_KEY, JSON.stringify(records));
}

export function getAttendance(date: string, subject: string): 'present' | 'absent' | null {
  const records = getAll();
  return records[makeKey(date, subject)] || null;
}

export function getSubjectStats(subject: string): { present: number; absent: number; total: number; percentage: number } {
  const records = getAll();
  let present = 0;
  let absent = 0;
  for (const [key, val] of Object.entries(records)) {
    if (key.endsWith(`_${subject}`)) {
      if (val === 'present') present++;
      else absent++;
    }
  }
  const total = present + absent;
  return { present, absent, total, percentage: total > 0 ? Math.round((present / total) * 100) : 0 };
}

export function getAllSubjectStats(): Record<string, { present: number; absent: number; total: number; percentage: number }> {
  const records = getAll();
  const subjects = new Set<string>();
  for (const key of Object.keys(records)) {
    const parts = key.split('_');
    parts.shift(); // remove date
    subjects.add(parts.join('_'));
  }
  const stats: Record<string, { present: number; absent: number; total: number; percentage: number }> = {};
  for (const sub of subjects) {
    stats[sub] = getSubjectStats(sub);
  }
  return stats;
}
