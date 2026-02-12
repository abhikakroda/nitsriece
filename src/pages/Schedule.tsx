import React, { useState, useCallback, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { markAttendance, getAttendance, getSubjectStats } from '@/lib/attendance';
import { days, timetableData, dayNames, getTodayDay, type Day } from '@/lib/timetable';

const todayStr = new Date().toISOString().split('T')[0];

// Get actual dates for the current week
function getWeekDates(): Record<Day, number> {
  const now = new Date();
  const dayOfWeek = now.getDay(); // 0=Sun
  const monday = new Date(now);
  monday.setDate(now.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
  const result: Record<string, number> = {};
  const dayList: Day[] = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  dayList.forEach((d, i) => {
    const date = new Date(monday);
    date.setDate(monday.getDate() + i);
    result[d] = date.getDate();
  });
  return result as Record<Day, number>;
}

const cardVariants = {
  hidden: { opacity: 0, y: 16, scale: 0.98 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      delay: i * 0.05,
      duration: 0.4,
      ease: [0.22, 1, 0.36, 1] as [number, number, number, number],
    },
  }),
  exit: { opacity: 0, y: -8, scale: 0.98, transition: { duration: 0.15 } },
};

// Color palette for subject cards - vibrant in dark mode
const subjectColors = [
  { bg: 'bg-amber-50 dark:bg-amber-950/40', border: 'border-amber-200/60 dark:border-amber-700/30', text: 'text-amber-800 dark:text-amber-200', badge: 'border-amber-300 dark:border-amber-600/40 text-amber-700 dark:text-amber-300' },
  { bg: 'bg-purple-50 dark:bg-purple-950/40', border: 'border-purple-200/60 dark:border-purple-700/30', text: 'text-purple-800 dark:text-purple-200', badge: 'border-purple-300 dark:border-purple-600/40 text-purple-700 dark:text-purple-300' },
  { bg: 'bg-emerald-50 dark:bg-emerald-950/40', border: 'border-emerald-200/60 dark:border-emerald-700/30', text: 'text-emerald-800 dark:text-emerald-200', badge: 'border-emerald-300 dark:border-emerald-600/40 text-emerald-700 dark:text-emerald-300' },
  { bg: 'bg-blue-50 dark:bg-blue-950/40', border: 'border-blue-200/60 dark:border-blue-700/30', text: 'text-blue-800 dark:text-blue-200', badge: 'border-blue-300 dark:border-blue-600/40 text-blue-700 dark:text-blue-300' },
  { bg: 'bg-rose-50 dark:bg-rose-950/40', border: 'border-rose-200/60 dark:border-rose-700/30', text: 'text-rose-800 dark:text-rose-200', badge: 'border-rose-300 dark:border-rose-600/40 text-rose-700 dark:text-rose-300' },
];

const Schedule: React.FC = () => {
  const [selectedDay, setSelectedDay] = useState<Day>(getTodayDay);
  const [, forceUpdate] = useState(0);
  const classes = timetableData[selectedDay];
  const today = getTodayDay();
  const weekDates = useMemo(() => getWeekDates(), []);

  const handleAttendance = useCallback((subject: string, status: 'present' | 'absent') => {
    markAttendance(todayStr, subject, status);
    forceUpdate(n => n + 1);
  }, []);

  return (
    <div className="bg-background min-h-screen flex flex-col pb-20 lg:pb-6">
      {/* Header */}
      <div className="sticky top-0 z-50 liquid-glass-nav px-5 md:px-6 pt-5 pb-4 border-b border-border/20">
        <div className="max-w-3xl mx-auto">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="font-display text-[20px] md:text-[24px] font-bold text-foreground tracking-tight">Schedule</h1>
              <p className="text-muted-foreground text-[12px] mt-0.5">Section B – ECE 6th Sem</p>
            </div>
            <span className="text-[10px] font-bold text-primary bg-primary/6 px-2.5 py-1 rounded-full border border-primary/8">
              {classes.length} {classes.length === 1 ? 'Class' : 'Classes'}
            </span>
          </div>

          {/* Calendar Day Selector */}
          <div className="flex gap-1.5 md:gap-2">
            {days.map(day => {
              const isSelected = selectedDay === day;
              const isToday = day === today;
              return (
                <button
                  key={day}
                  onClick={() => setSelectedDay(day)}
                  className={`relative flex-1 flex flex-col items-center py-2 md:py-2.5 rounded-[14px] transition-all duration-200 ${
                    isSelected 
                      ? 'bg-primary text-white shadow-md shadow-primary/20' 
                      : 'text-muted-foreground hover:bg-secondary/50'
                  }`}
                >
                  <span className={`text-[10px] md:text-[11px] font-bold uppercase tracking-wider ${isSelected ? 'text-white/70' : ''}`}>
                    {day}
                  </span>
                  <span className={`font-display text-[18px] md:text-[20px] font-bold leading-none mt-0.5 ${isSelected ? 'text-white' : 'text-foreground'}`}>
                    {weekDates[day]}
                  </span>
                  {isToday && !isSelected && (
                    <span className="absolute bottom-1 size-[4px] rounded-full bg-primary" />
                  )}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Classes Timeline */}
      <main className="flex-1 px-5 md:px-6 py-5">
        <div className="max-w-3xl mx-auto">
          <AnimatePresence mode="wait">
            <motion.div key={selectedDay} initial="hidden" animate="visible" exit="exit">
              {classes.length === 0 ? (
                <motion.div variants={cardVariants} custom={0} className="flex flex-col items-center justify-center py-20 text-center">
                  <div className="w-16 h-16 rounded-[18px] bg-primary/6 flex items-center justify-center mb-4">
                    <span className="material-symbols-outlined text-[32px] text-primary/25">celebration</span>
                  </div>
                  <p className="font-display text-foreground font-bold text-[18px]">No classes!</p>
                  <p className="text-muted-foreground text-[13px] mt-1.5">Enjoy your free {dayNames[selectedDay]} 🎉</p>
                </motion.div>
              ) : (
                <div className="space-y-0">
                  {classes.map((cls, idx) => {
                    const attendance = getAttendance(todayStr, cls.subject);
                    const stats = getSubjectStats(cls.subject);
                    const color = subjectColors[idx % subjectColors.length];
                    const endTime = cls.time.split('–')[1]?.trim() || '';

                    return (
                      <motion.div key={`${selectedDay}-${idx}`} variants={cardVariants} custom={idx} className="flex gap-3 md:gap-4">
                        {/* Time Column */}
                        <div className="w-[48px] md:w-[56px] shrink-0 flex flex-col items-center pt-3">
                          <p className="text-[12px] md:text-[13px] font-bold text-foreground leading-none">{cls.startTime}</p>
                          <div className="flex flex-col items-center flex-1 py-2">
                            <div className="size-[7px] rounded-full bg-muted-foreground/25" />
                            <div className="w-[1.5px] flex-1 bg-border/50 min-h-[40px]" />
                          </div>
                          <p className="text-[11px] text-muted-foreground/60 leading-none mb-3">{endTime}</p>
                        </div>

                        {/* Card */}
                        <div className={`flex-1 rounded-[16px] p-4 md:p-5 border mb-3 ${color.bg} ${color.border}`}>
                          <div className="flex items-start justify-between">
                            <div className="flex-1 min-w-0">
                              <span className={`text-[9px] font-bold py-[3px] px-2 rounded-full border uppercase tracking-wider inline-block ${color.badge}`}>
                                {cls.type || 'Lecture'}
                              </span>
                              <h3 className={`font-display font-bold text-[17px] md:text-[19px] leading-tight mt-2 ${color.text}`}>
                                {cls.subject}
                              </h3>
                              <div className="flex items-center gap-3 mt-2.5">
                                <p className="text-[11px] md:text-[12px] text-muted-foreground flex items-center gap-1">
                                  <span className="material-symbols-outlined text-[14px]">person</span> {cls.faculty || 'TBA'}
                                </p>
                                <p className="text-[11px] md:text-[12px] text-muted-foreground flex items-center gap-1">
                                  <span className="material-symbols-outlined text-[14px]">location_on</span> {cls.room}
                                </p>
                              </div>
                            </div>
                            <div className="flex flex-col items-end gap-1.5 shrink-0 ml-2">
                              {stats.total > 0 && (
                                <span className={`text-[9px] font-bold py-0.5 px-2 rounded-full ${stats.percentage >= 75 ? 'bg-green-500/10 text-green-600 dark:text-green-400' : 'bg-red-500/10 text-red-500'}`}>
                                  {stats.percentage}%
                                </span>
                              )}
                            </div>
                          </div>

                          {/* Attendance buttons */}
                          {selectedDay === today && (
                            <div className="grid grid-cols-2 gap-2 mt-3.5">
                              <button
                                onClick={() => handleAttendance(cls.subject, 'present')}
                                className={`flex items-center justify-center gap-1 text-[11px] font-semibold py-2 rounded-[10px] transition-all duration-200 active:scale-[0.96] ${
                                  attendance === 'present'
                                    ? 'bg-green-500 text-white shadow-sm shadow-green-500/20'
                                    : 'bg-white/60 dark:bg-white/5 text-green-600 dark:text-green-400 border border-green-500/15 hover:bg-green-500/10'
                                }`}
                              >
                                <span className="material-symbols-outlined text-[14px]">check_circle</span>
                                {attendance === 'present' ? 'Present ✓' : 'Present'}
                              </button>
                              <button
                                onClick={() => handleAttendance(cls.subject, 'absent')}
                                className={`flex items-center justify-center gap-1 text-[11px] font-semibold py-2 rounded-[10px] transition-all duration-200 active:scale-[0.96] ${
                                  attendance === 'absent'
                                    ? 'bg-red-500 text-white shadow-sm shadow-red-500/20'
                                    : 'bg-white/60 dark:bg-white/5 border border-border/40 text-muted-foreground hover:bg-red-500/5 hover:border-red-500/15 hover:text-red-500'
                                }`}
                              >
                                <span className="material-symbols-outlined text-[14px]">cancel</span>
                                {attendance === 'absent' ? 'Absent ✓' : 'Absent'}
                              </button>
                            </div>
                          )}
                        </div>
                      </motion.div>
                    );
                  })}
                </div>
              )}
            </motion.div>
          </AnimatePresence>
        </div>
      </main>
    </div>
  );
};

export default Schedule;
