import React, { useState, useCallback, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { markAttendance, getAttendance, getSubjectStats } from '@/lib/attendance';
import { days, timetableData, dayNames, getTodayDay, type Day } from '@/lib/timetable';

function formatLocalDate(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

const todayStr = formatLocalDate(new Date());

function getWeekDates(): Record<Day, number> {
  const now = new Date();
  const dayOfWeek = now.getDay(); // 0=Sun
  const monday = new Date(now);
  monday.setDate(now.getDate() - ((dayOfWeek === 0 ? 7 : dayOfWeek) - 1));
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
  hidden: { opacity: 0, y: 12 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    transition: { delay: i * 0.04, duration: 0.35, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] },
  }),
  exit: { opacity: 0, y: -6, transition: { duration: 0.12 } },
};

const Schedule: React.FC = () => {
  const today = getTodayDay(); // null on weekends
  const [selectedDay, setSelectedDay] = useState<Day>(today || 'Mon');
  const [, forceUpdate] = useState(0);
  const classes = timetableData[selectedDay];
  const weekDates = useMemo(() => getWeekDates(), []);

  const handleAttendance = useCallback((subject: string, status: 'present' | 'absent') => {
    markAttendance(todayStr, subject, status);
    forceUpdate(n => n + 1);
  }, []);

  return (
    <div className="bg-background min-h-screen flex flex-col pb-20 lg:pb-6">
      {/* Header */}
      <div className="sticky top-0 z-50 liquid-glass-nav px-5 md:px-6 pt-5 pb-4 border-b border-white/20 dark:border-white/5">
        <div className="max-w-3xl mx-auto">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="font-display text-[20px] md:text-[24px] font-bold text-foreground tracking-tight">Schedule</h1>
              <p className="text-muted-foreground text-[11px] mt-0.5">Section B – ECE 6th Sem</p>
            </div>
            <span className="text-[10px] font-medium text-muted-foreground">
              {classes.length} {classes.length === 1 ? 'class' : 'classes'}
            </span>
          </div>

          {/* Day Selector */}
          <div className="flex gap-1">
            {days.map((day, i) => {
              const isSelected = selectedDay === day;
              const isToday = day === today;
              return (
                <motion.button
                  key={day}
                  onClick={() => setSelectedDay(day)}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.04, duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
                  className={`relative flex-1 flex flex-col items-center py-2 rounded-xl transition-colors duration-200 ${
                    isSelected
                      ? 'bg-foreground text-background'
                      : 'text-muted-foreground hover:bg-white/20 dark:hover:bg-white/5'
                  }`}
                >
                  <span className={`text-[9px] font-bold uppercase tracking-wider ${isSelected ? 'text-background/60' : ''}`}>
                    {day}
                  </span>
                  <span className={`font-display text-[17px] font-bold leading-none mt-0.5 ${isSelected ? '' : 'text-foreground'}`}>
                    {weekDates[day]}
                  </span>
                  {isToday && !isSelected && (
                    <span className="absolute bottom-1 size-[3px] rounded-full bg-primary" />
                  )}
                </motion.button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Classes */}
      <main className="flex-1 px-5 md:px-6 py-5">
        <div className="max-w-3xl mx-auto">
          <AnimatePresence mode="wait">
            <motion.div key={selectedDay} initial="hidden" animate="visible" exit="exit">
              {classes.length === 0 ? (
                <motion.div variants={cardVariants} custom={0} className="flex flex-col items-center justify-center py-20 text-center">
                  <p className="font-display text-foreground font-bold text-[17px]">No classes</p>
                  <p className="text-muted-foreground text-[12px] mt-1">Enjoy your {dayNames[selectedDay]} 🎉</p>
                </motion.div>
              ) : (
                <div className="space-y-0">
                  {classes.map((cls, idx) => {
                    const attendance = getAttendance(todayStr, cls.subject);
                    const stats = getSubjectStats(cls.subject);
                    const endTime = cls.time.split('–')[1]?.trim() || '';

                    return (
                      <motion.div key={`${selectedDay}-${idx}`} variants={cardVariants} custom={idx} className="flex gap-3">
                        {/* Time */}
                        <div className="w-[46px] shrink-0 flex flex-col items-center pt-3">
                          <p className="text-[11px] font-bold text-foreground leading-none">{cls.startTime}</p>
                          <div className="flex flex-col items-center flex-1 py-2">
                            <div className="size-[5px] rounded-full bg-border" />
                            <div className="w-px flex-1 bg-border/50 min-h-[40px]" />
                          </div>
                          <p className="text-[10px] text-muted-foreground/50 leading-none mb-3">{endTime}</p>
                        </div>

                        {/* Card */}
                        <div className="flex-1 rounded-2xl liquid-glass-card p-4 mb-2">
                          <div className="flex items-start justify-between">
                            <div className="min-w-0">
                              <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-wider">
                                {cls.type || 'Lecture'}
                              </span>
                              <h3 className="font-display font-bold text-[16px] md:text-[18px] text-foreground leading-tight mt-1">
                                {cls.subject}
                              </h3>
                              <div className="flex items-center gap-3 mt-2">
                                <span className="text-[11px] text-muted-foreground">{cls.faculty || 'TBA'}</span>
                                <span className="text-[11px] text-muted-foreground">{cls.room}</span>
                              </div>
                            </div>
                            {stats.total > 0 && (
                              <span className={`text-[10px] font-bold ${stats.percentage >= 75 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-500'}`}>
                                {stats.percentage}%
                              </span>
                            )}
                          </div>

                          {today && selectedDay === today && (
                            <div className="grid grid-cols-2 gap-2 mt-3">
                              <button
                                onClick={() => handleAttendance(cls.subject, 'present')}
                                className={`text-[11px] font-medium py-2 rounded-xl transition-all active:scale-[0.96] ${
                                  attendance === 'present'
                                    ? 'bg-emerald-500 text-white'
                                    : 'bg-white/30 dark:bg-white/5 text-muted-foreground hover:bg-emerald-500/10 hover:text-emerald-600'
                                }`}
                              >
                                {attendance === 'present' ? 'Present ✓' : 'Present'}
                              </button>
                              <button
                                onClick={() => handleAttendance(cls.subject, 'absent')}
                                className={`text-[11px] font-medium py-2 rounded-xl transition-all active:scale-[0.96] ${
                                  attendance === 'absent'
                                    ? 'bg-rose-500 text-white'
                                    : 'bg-white/30 dark:bg-white/5 text-muted-foreground hover:bg-rose-500/10 hover:text-rose-500'
                                }`}
                              >
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
