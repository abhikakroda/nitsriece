import React, { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { markAttendance, getAttendance, getSubjectStats } from '@/lib/attendance';

const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'] as const;
type Day = typeof days[number];

interface ClassSlot {
  subject: string;
  time: string;
  startTime: string;
  room: string;
  icon: string;
  iconBg: string;
  iconColor: string;
  type?: string;
}

const timetableData: Record<Day, ClassSlot[]> = {
  Mon: [
    { subject: 'Computer Org & Arch', time: '09:00 – 09:50', startTime: '09:00', room: 'L-15', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500' },
    { subject: 'VLSI Design Lab (G3)', time: '09:50 – 11:30', startTime: '09:50', room: 'Lab', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500', type: 'Lab' },
    { subject: 'DSP Lab-I (G3)', time: '11:30 – 12:20', startTime: '11:30', room: 'Lab', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500', type: 'Lab' },
    { subject: 'Elective-II', time: '12:20 – 01:10', startTime: '12:20', room: 'L-15', icon: 'auto_stories', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500' },
    { subject: 'IE-2', time: '01:10 – 02:00', startTime: '13:10', room: 'L-15', icon: 'school', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500' },
  ],
  Tue: [
    { subject: 'Data Comm. & Networking', time: '09:00 – 09:50', startTime: '09:00', room: 'L-15', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500' },
    { subject: 'VLSI Design', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500' },
  ],
  Wed: [
    { subject: 'Digital Signal Processing', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500' },
  ],
  Thu: [
    { subject: 'Computer Org & Arch', time: '09:00 – 09:50', startTime: '09:00', room: 'L-15', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500' },
    { subject: 'Data Comm. & Networking', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500' },
  ],
  Fri: [
    { subject: 'RF Design', time: '09:50 – 10:40', startTime: '09:50', room: 'L-15', icon: 'settings_input_antenna', iconBg: 'bg-blue-500/10', iconColor: 'text-blue-500' },
  ],
};

const dayNames: Record<Day, string> = {
  Mon: 'Monday', Tue: 'Tuesday', Wed: 'Wednesday', Thu: 'Thursday', Fri: 'Friday',
};

const todayStr = new Date().toISOString().split('T')[0];

const cardVariants = {
  hidden: { opacity: 0, y: 20, scale: 0.97 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      delay: i * 0.07,
      duration: 0.35,
      ease: [0.25, 0.46, 0.45, 0.94] as [number, number, number, number],
    },
  }),
  exit: { opacity: 0, y: -10, transition: { duration: 0.15 } },
};

const Schedule: React.FC = () => {
  const [selectedDay, setSelectedDay] = useState<Day>(() => {
    const d = new Date().getDay();
    const map: Day[] = ['Mon', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri']; // Sun defaults to Mon
    return map[d] || 'Mon';
  });
  const [, forceUpdate] = useState(0);
  const classes = timetableData[selectedDay];

  const handleAttendance = useCallback((subject: string, status: 'present' | 'absent') => {
    markAttendance(todayStr, subject, status);
    forceUpdate(n => n + 1);
  }, []);

  return (
    <div className="bg-background min-h-screen flex flex-col pb-20">
      {/* Header */}
      <div className="sticky top-0 z-50 glass-morphism px-4 pt-4 pb-3 border-b border-border">
        <h1 className="text-xl font-black text-foreground tracking-tight mb-3">Weekly Schedule</h1>
        <div className="flex gap-1.5">
          {days.map(day => (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`flex-1 py-2 rounded-xl text-[12px] font-bold transition-all duration-200 ${
                selectedDay === day
                  ? 'bg-primary text-primary-foreground shadow-md shadow-primary/20'
                  : 'bg-secondary text-muted-foreground active:bg-accent'
              }`}
            >
              {day}
            </button>
          ))}
        </div>
      </div>

      {/* Classes */}
      <main className="flex-1 px-4 py-4">
        <div className="flex items-center justify-between mb-3">
          <p className="text-muted-foreground text-[11px] font-bold uppercase tracking-widest">{dayNames[selectedDay]}</p>
          <span className="text-[10px] font-bold text-primary bg-primary/10 px-2 py-0.5 rounded-full">
            {classes.length} {classes.length === 1 ? 'Class' : 'Classes'}
          </span>
        </div>

        <AnimatePresence mode="wait">
          <motion.div key={selectedDay} initial="hidden" animate="visible" exit="exit" className="relative">
            {classes.length === 0 ? (
              <motion.div variants={cardVariants} custom={0} className="flex flex-col items-center justify-center py-20 text-center">
                <span className="material-symbols-outlined text-[48px] text-muted-foreground/30 mb-3">event_available</span>
                <p className="text-muted-foreground font-medium text-[14px]">No classes today!</p>
                <p className="text-muted-foreground/60 text-[12px] mt-1">Enjoy your free day 🎉</p>
              </motion.div>
            ) : (
              <>
                <div className="absolute left-[19px] top-4 bottom-4 w-[2px] bg-border rounded-full" />
                <div className="space-y-3">
                  {classes.map((cls, idx) => {
                    const attendance = getAttendance(todayStr, cls.subject);
                    const stats = getSubjectStats(cls.subject);

                    return (
                      <motion.div key={`${selectedDay}-${idx}`} variants={cardVariants} custom={idx} className="flex gap-3 relative">
                        {/* Timeline dot */}
                        <div className="relative z-10 mt-4 shrink-0">
                          <div className={`size-[10px] rounded-full border-2 ${idx === 0 ? 'border-primary bg-primary' : 'border-border bg-card'}`} style={{ marginLeft: '10px' }} />
                        </div>

                        {/* Card */}
                        <div className={`flex-1 bg-card rounded-2xl p-3.5 border ${idx === 0 ? 'border-primary/20 shadow-sm' : 'border-border'}`}>
                          <div className="flex items-start justify-between">
                            <div className="flex gap-3">
                              <div className={`size-10 rounded-xl ${cls.iconBg} ${cls.iconColor} flex items-center justify-center shrink-0`}>
                                <span className="material-symbols-outlined text-[20px]">{cls.icon}</span>
                              </div>
                              <div>
                                <h3 className="font-bold text-foreground text-[14px] leading-tight">{cls.subject}</h3>
                                <p className="text-[11px] text-muted-foreground mt-1 flex items-center gap-1">
                                  <span className="material-symbols-outlined text-[13px]">schedule</span> {cls.time}
                                </p>
                                <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                                  <span className="material-symbols-outlined text-[13px]">location_on</span> {cls.room}
                                </p>
                              </div>
                            </div>
                            <div className="flex flex-col items-end gap-1">
                              {cls.type && (
                                <span className="text-[9px] font-bold py-0.5 px-2 rounded-full bg-purple-500/10 text-purple-600 dark:text-purple-400 uppercase">{cls.type}</span>
                              )}
                              {stats.total > 0 && (
                                <span className={`text-[9px] font-bold py-0.5 px-2 rounded-full ${stats.percentage >= 75 ? 'bg-green-500/10 text-green-600' : 'bg-red-500/10 text-red-500'}`}>
                                  {stats.percentage}%
                                </span>
                              )}
                            </div>
                          </div>

                          {/* Attendance buttons */}
                          <div className="grid grid-cols-2 gap-2 mt-3">
                            <button
                              onClick={() => handleAttendance(cls.subject, 'present')}
                              className={`flex items-center justify-center gap-1 text-[12px] font-semibold py-2 rounded-xl transition-all active:scale-[0.97] ${
                                attendance === 'present'
                                  ? 'bg-green-500 text-white shadow-md shadow-green-500/20'
                                  : 'bg-green-500/10 text-green-600 dark:text-green-400'
                              }`}
                            >
                              <span className="material-symbols-outlined text-[15px]">check_circle</span>
                              {attendance === 'present' ? 'Present ✓' : 'Present'}
                            </button>
                            <button
                              onClick={() => handleAttendance(cls.subject, 'absent')}
                              className={`flex items-center justify-center gap-1 text-[12px] font-semibold py-2 rounded-xl transition-all active:scale-[0.97] ${
                                attendance === 'absent'
                                  ? 'bg-red-500 text-white shadow-md shadow-red-500/20'
                                  : 'bg-card border border-border text-muted-foreground'
                              }`}
                            >
                              <span className="material-symbols-outlined text-[15px]">cancel</span>
                              {attendance === 'absent' ? 'Absent ✓' : 'Absent'}
                            </button>
                          </div>
                        </div>
                      </motion.div>
                    );
                  })}
                </div>
              </>
            )}
          </motion.div>
        </AnimatePresence>
      </main>
    </div>
  );
};

export default Schedule;
