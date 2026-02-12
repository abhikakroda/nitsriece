import React, { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { markAttendance, getAttendance, getSubjectStats } from '@/lib/attendance';
import { days, timetableData, dayNames, getTodayDay, type Day } from '@/lib/timetable';

const todayStr = new Date().toISOString().split('T')[0];

const cardVariants = {
  hidden: { opacity: 0, y: 20, scale: 0.97 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      delay: i * 0.06,
      duration: 0.45,
      ease: [0.22, 1, 0.36, 1] as [number, number, number, number],
    },
  }),
  exit: { opacity: 0, y: -10, scale: 0.98, transition: { duration: 0.2 } },
};

const Schedule: React.FC = () => {
  const [selectedDay, setSelectedDay] = useState<Day>(getTodayDay);
  const [, forceUpdate] = useState(0);
  const classes = timetableData[selectedDay];
  const today = getTodayDay();

  const handleAttendance = useCallback((subject: string, status: 'present' | 'absent') => {
    markAttendance(todayStr, subject, status);
    forceUpdate(n => n + 1);
  }, []);

  return (
    <div className="bg-background min-h-screen flex flex-col pb-20 lg:pb-6">
      {/* Header */}
      <div className="sticky top-0 z-50 glass-morphism px-5 md:px-6 pt-5 pb-3 border-b border-border/40">
        <div className="max-w-3xl mx-auto">
          <h1 className="font-display text-[22px] md:text-[26px] font-bold text-foreground tracking-tight mb-3.5">Weekly Schedule</h1>
          <div className="flex gap-1 bg-secondary/40 p-[3px] rounded-[14px]">
            {days.map(day => (
              <button
                key={day}
                onClick={() => setSelectedDay(day)}
                className="relative flex-1 py-2 md:py-2.5 rounded-[11px] text-[12px] md:text-[13px] font-bold transition-all duration-200"
              >
                {selectedDay === day && (
                  <motion.div
                    layoutId="daySelector"
                    className="absolute inset-0 bg-card shadow-sm shadow-primary/5 rounded-[11px] border border-border/40"
                    transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                  />
                )}
                <span className={`relative z-10 ${
                  selectedDay === day ? 'text-primary font-extrabold' : 'text-muted-foreground'
                }`}>
                  {day}
                </span>
                {day === today && (
                  <span className="absolute top-1 right-1.5 size-[5px] rounded-full bg-primary z-10" />
                )}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Classes */}
      <main className="flex-1 px-5 md:px-6 py-5">
        <div className="max-w-3xl mx-auto">
          <div className="flex items-center justify-between mb-4">
            <p className="text-muted-foreground text-[11px] md:text-[12px] font-semibold uppercase tracking-[0.15em]">{dayNames[selectedDay]}</p>
            <span className="text-[10px] md:text-[11px] font-bold text-primary bg-primary/6 px-2.5 py-1 rounded-full border border-primary/8">
              {classes.length} {classes.length === 1 ? 'Class' : 'Classes'}
            </span>
          </div>

          <AnimatePresence mode="wait">
            <motion.div key={selectedDay} initial="hidden" animate="visible" exit="exit" className="relative">
              {classes.length === 0 ? (
                <motion.div variants={cardVariants} custom={0} className="flex flex-col items-center justify-center py-20 text-center">
                  <div className="w-16 h-16 rounded-[18px] bg-primary/6 flex items-center justify-center mb-4">
                    <span className="material-symbols-outlined text-[32px] text-primary/25">celebration</span>
                  </div>
                  <p className="font-display text-foreground font-bold text-[17px] md:text-[19px]">No classes!</p>
                  <p className="text-muted-foreground text-[13px] mt-1.5">Enjoy your free day 🎉</p>
                </motion.div>
              ) : (
                <>
                  {/* Timeline line */}
                  <div className="absolute left-[19px] top-6 bottom-6 w-[1.5px] bg-gradient-to-b from-primary/15 via-border/60 to-transparent rounded-full" />
                  <div className="space-y-3">
                    {classes.map((cls, idx) => {
                      const attendance = getAttendance(todayStr, cls.subject);
                      const stats = getSubjectStats(cls.subject);

                      return (
                        <motion.div key={`${selectedDay}-${idx}`} variants={cardVariants} custom={idx} className="flex gap-3 relative">
                          {/* Timeline dot */}
                          <div className="relative z-10 mt-5 shrink-0">
                            <div className={`size-[8px] rounded-full border-2 transition-colors ${
                              idx === 0 ? 'border-primary bg-primary shadow-sm shadow-primary/30' : 'border-border bg-background'
                            }`} style={{ marginLeft: '11px' }} />
                          </div>

                          {/* Card */}
                          <div className={`flex-1 bg-card card-elevated rounded-[16px] p-4 md:p-5 border transition-all duration-300 ${
                            idx === 0 ? 'border-primary/12 ring-1 ring-primary/5' : 'border-border/50'
                          }`}>
                            {idx === 0 && (
                              <div className="absolute top-0 left-0 right-0 h-[1.5px] bg-gradient-to-r from-primary via-blue-500 to-transparent rounded-t-[16px]" />
                            )}
                            <div className="flex items-start justify-between">
                              <div className="flex gap-3">
                                <div className={`size-11 md:size-12 rounded-[14px] ${cls.iconBg} ${cls.iconColor} flex items-center justify-center shrink-0`}>
                                  <span className="material-symbols-outlined text-[20px] md:text-[22px]">{cls.icon}</span>
                                </div>
                                <div>
                                  <h3 className="font-bold text-foreground text-[13px] md:text-[14px] leading-tight">{cls.subject}</h3>
                                  <div className="mt-1.5 space-y-0.5">
                                    <p className="text-[10px] md:text-[11px] text-muted-foreground flex items-center gap-1">
                                      <span className="material-symbols-outlined text-[12px]">schedule</span> {cls.time}
                                    </p>
                                    <div className="flex items-center gap-2.5">
                                      <p className="text-[10px] md:text-[11px] text-muted-foreground flex items-center gap-1">
                                        <span className="material-symbols-outlined text-[12px]">location_on</span> {cls.room}
                                      </p>
                                      {cls.faculty && (
                                        <p className="text-[10px] md:text-[11px] text-muted-foreground flex items-center gap-1">
                                          <span className="material-symbols-outlined text-[12px]">person</span> {cls.faculty}
                                        </p>
                                      )}
                                    </div>
                                  </div>
                                </div>
                              </div>
                              <div className="flex flex-col items-end gap-1.5">
                                {cls.type && (
                                  <span className="text-[9px] font-bold py-0.5 px-2 rounded-full bg-purple-500/8 text-purple-600 dark:text-purple-400 uppercase tracking-wide">{cls.type}</span>
                                )}
                                {stats.total > 0 && (
                                  <span className={`text-[9px] font-bold py-0.5 px-2 rounded-full ${stats.percentage >= 75 ? 'bg-green-500/8 text-green-600' : 'bg-red-500/8 text-red-500'}`}>
                                    {stats.percentage}%
                                  </span>
                                )}
                              </div>
                            </div>

                            {/* Attendance buttons */}
                            <div className="grid grid-cols-2 gap-2 mt-3.5">
                              <button
                                onClick={() => handleAttendance(cls.subject, 'present')}
                                className={`flex items-center justify-center gap-1.5 text-[11px] md:text-[12px] font-semibold py-2.5 rounded-[11px] transition-all duration-200 active:scale-[0.96] ${
                                  attendance === 'present'
                                    ? 'bg-green-500 text-white shadow-md shadow-green-500/20'
                                    : 'bg-green-500/6 text-green-600 dark:text-green-400 border border-green-500/10 hover:bg-green-500/12'
                                }`}
                              >
                                <span className="material-symbols-outlined text-[15px]">check_circle</span>
                                {attendance === 'present' ? 'Present ✓' : 'Present'}
                              </button>
                              <button
                                onClick={() => handleAttendance(cls.subject, 'absent')}
                                className={`flex items-center justify-center gap-1.5 text-[11px] md:text-[12px] font-semibold py-2.5 rounded-[11px] transition-all duration-200 active:scale-[0.96] ${
                                  attendance === 'absent'
                                    ? 'bg-red-500 text-white shadow-md shadow-red-500/20'
                                    : 'bg-card border border-border/60 text-muted-foreground hover:bg-red-500/4 hover:border-red-500/15 hover:text-red-500'
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
        </div>
      </main>
    </div>
  );
};

export default Schedule;
