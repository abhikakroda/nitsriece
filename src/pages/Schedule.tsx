import React, { useState, useCallback, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { markAttendance, getAttendance, getSubjectStats } from '@/lib/attendance';
import { days, timetableData, dayNames, getTodayDay, to12hr, type Day } from '@/lib/timetable';

function formatLocalDate(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

const todayStr = formatLocalDate(new Date());

function getWeekDates(): Record<Day, number> {
  const now = new Date();
  const dayOfWeek = now.getDay();
  const monday = new Date(now);
  if (dayOfWeek === 0) {
    monday.setDate(now.getDate() + 1);
  } else if (dayOfWeek === 6) {
    monday.setDate(now.getDate() + 2);
  } else {
    monday.setDate(now.getDate() - (dayOfWeek - 1));
  }
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

const typeConfig: Record<string, { label: string; color: string; bg: string; border: string }> = {
  Lab: { label: 'Lab', color: 'text-violet-600 dark:text-violet-400', bg: 'bg-violet-500/10', border: 'border-violet-500/20' },
  Tutorial: { label: 'Tutorial', color: 'text-amber-600 dark:text-amber-400', bg: 'bg-amber-500/10', border: 'border-amber-500/20' },
  Lecture: { label: 'Lec', color: 'text-primary/80', bg: 'bg-primary/8', border: 'border-primary/15' },
};

const Schedule: React.FC = () => {
  const today = getTodayDay();
  const [selectedDay, setSelectedDay] = useState<Day>(today || 'Mon');
  const [, forceUpdate] = useState(0);
  const classes = timetableData[selectedDay];
  const weekDates = useMemo(() => getWeekDates(), []);

  const handleAttendance = useCallback((subject: string, status: 'present' | 'absent') => {
    markAttendance(todayStr, subject, status);
    forceUpdate(n => n + 1);
  }, []);

  return (
    <div className="bg-background min-h-screen flex flex-col pb-24 lg:pb-6">
      {/* Sticky Header */}
      <div className="sticky top-0 z-50 liquid-glass-nav border-b border-white/20 dark:border-white/5">
        <div className="max-w-3xl mx-auto px-5 md:px-6 pt-6 pb-4">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="font-display text-[22px] md:text-[26px] font-bold text-foreground tracking-tight">Schedule</h1>
              <p className="text-muted-foreground text-[11px] mt-0.5">Section B – ECE 6th Sem</p>
            </div>
            <div className="rounded-xl px-3 py-1.5 liquid-glass-card">
              <span className="text-[12px] font-semibold text-foreground">{classes.length}</span>
              <span className="text-[10px] text-muted-foreground ml-1">{classes.length === 1 ? 'class' : 'classes'}</span>
            </div>
          </div>

          {/* Day Selector */}
          <div className="flex gap-1.5">
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
                  className={`relative flex-1 flex flex-col items-center py-2.5 rounded-xl transition-all duration-200 ${
                    isSelected
                      ? 'bg-primary text-primary-foreground shadow-sm'
                      : 'text-muted-foreground hover:bg-secondary/60'
                  }`}
                >
                  <span className={`text-[9px] font-bold uppercase tracking-wider ${isSelected ? 'text-primary-foreground/70' : ''}`}>
                    {day}
                  </span>
                  <span className={`font-display text-[17px] font-bold leading-none mt-0.5 ${isSelected ? 'text-primary-foreground' : 'text-foreground'}`}>
                    {weekDates[day]}
                  </span>
                  {isToday && !isSelected && (
                    <span className="absolute bottom-1.5 size-[3px] rounded-full bg-primary" />
                  )}
                </motion.button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Classes List */}
      <main className="flex-1 px-5 md:px-6 py-5">
        <div className="max-w-3xl mx-auto">
          <AnimatePresence mode="wait">
            <motion.div key={selectedDay} initial="hidden" animate="visible" exit="exit">
              {classes.length === 0 ? (
                <motion.div variants={cardVariants} custom={0} className="flex flex-col items-center justify-center py-20 text-center">
                  <span className="material-symbols-outlined text-[40px] text-muted-foreground/20 block mb-3">self_improvement</span>
                  <p className="font-display text-foreground font-bold text-[17px]">No classes</p>
                  <p className="text-muted-foreground text-[12px] mt-1">Enjoy your {dayNames[selectedDay]} 🎉</p>
                </motion.div>
              ) : (
                <div className="space-y-0">
                  {classes.map((cls, idx) => {
                    const attendance = getAttendance(todayStr, cls.subject);
                    const stats = getSubjectStats(cls.subject);
                    const endTime = cls.time.split('–')[1]?.trim() || '';
                    const typeKey = cls.type || 'Lecture';
                    const tc = typeConfig[typeKey] || typeConfig['Lecture'];

                    return (
                      <motion.div key={`${selectedDay}-${idx}`} variants={cardVariants} custom={idx} className="flex gap-3">
                        {/* Timeline */}
                        <div className="w-[46px] shrink-0 flex flex-col items-end pt-3.5">
                          <p className="text-[11px] font-bold text-foreground leading-none">{to12hr(cls.startTime)}</p>
                          <div className="flex flex-col items-center flex-1 py-1.5 w-full">
                            <div className="size-[5px] rounded-full bg-border ml-auto mr-0 mt-1" />
                            <div className="w-px flex-1 bg-border/40 min-h-[32px] ml-auto mr-[2px]" />
                          </div>
                          <p className="text-[9px] text-muted-foreground/50 leading-none mb-3 text-right">{endTime}</p>
                        </div>

                        {/* Card */}
                        <div className="flex-1 rounded-2xl liquid-glass-card mb-3 overflow-hidden">
                          <div className="p-4">
                            <div className="flex items-start justify-between gap-2">
                              <div className="min-w-0 flex-1">
                                <div className="flex items-center gap-2 mb-1">
                                  <span className={`text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md border ${tc.color} ${tc.bg} ${tc.border}`}>
                                    {tc.label}
                                  </span>
                                </div>
                                <h3 className="font-display font-bold text-[16px] md:text-[17px] text-foreground leading-tight">
                                  {cls.subject}
                                </h3>
                                <div className="flex flex-wrap items-center gap-x-3 gap-y-0.5 mt-1.5">
                                  <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                                    <span className="material-symbols-outlined text-[12px]">person</span>
                                    {cls.faculty || 'TBA'}
                                  </span>
                                  <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                                    <span className="material-symbols-outlined text-[12px]">room</span>
                                    {cls.room}
                                  </span>
                                </div>
                              </div>
                              {stats.total > 0 && (
                                <div className="text-right shrink-0">
                                  <span className={`text-[13px] font-bold ${stats.percentage >= 75 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-500'}`}>
                                    {stats.percentage}%
                                  </span>
                                  <p className="text-[9px] text-muted-foreground">{stats.present}/{stats.total}</p>
                                </div>
                              )}
                            </div>

                            {/* Attendance buttons — only for today */}
                            {today && selectedDay === today && (
                              <div className="grid grid-cols-2 gap-2 mt-3">
                                <button
                                  onClick={() => handleAttendance(cls.subject, 'present')}
                                  className={`flex items-center justify-center gap-1.5 text-[11px] font-semibold py-2 rounded-xl transition-all active:scale-[0.96] ${
                                    attendance === 'present'
                                      ? 'bg-emerald-500 text-white'
                                      : 'bg-white/30 dark:bg-white/5 text-muted-foreground hover:bg-emerald-500/10 hover:text-emerald-600 dark:hover:text-emerald-400'
                                  }`}
                                >
                                  <span className="material-symbols-outlined text-[14px]">{attendance === 'present' ? 'check_circle' : 'radio_button_unchecked'}</span>
                                  Present
                                </button>
                                <button
                                  onClick={() => handleAttendance(cls.subject, 'absent')}
                                  className={`flex items-center justify-center gap-1.5 text-[11px] font-semibold py-2 rounded-xl transition-all active:scale-[0.96] ${
                                    attendance === 'absent'
                                      ? 'bg-rose-500 text-white'
                                      : 'bg-white/30 dark:bg-white/5 text-muted-foreground hover:bg-rose-500/10 hover:text-rose-500'
                                  }`}
                                >
                                  <span className="material-symbols-outlined text-[14px]">{attendance === 'absent' ? 'cancel' : 'radio_button_unchecked'}</span>
                                  Absent
                                </button>
                              </div>
                            )}
                          </div>
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
