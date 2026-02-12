import React, { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { markAttendance, getAttendance, getSubjectStats } from '@/lib/attendance';
import { days, timetableData, dayNames, getTodayDay, type Day } from '@/lib/timetable';

const todayStr = new Date().toISOString().split('T')[0];

const cardVariants = {
  hidden: { opacity: 0, y: 24, scale: 0.96 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      delay: i * 0.08,
      duration: 0.4,
      ease: [0.25, 0.46, 0.45, 0.94] as [number, number, number, number],
    },
  }),
  exit: { opacity: 0, y: -12, scale: 0.98, transition: { duration: 0.2 } },
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
      <div className="sticky top-0 z-50 glass-morphism px-4 md:px-6 pt-4 pb-3.5 border-b border-border/60">
        <div className="max-w-3xl mx-auto">
          <h1 className="text-[22px] md:text-[26px] font-black text-foreground tracking-tight mb-3.5">Weekly Schedule</h1>
          <div className="flex gap-1.5 md:gap-2 bg-secondary/50 p-1 rounded-2xl">
            {days.map(day => (
              <button
                key={day}
                onClick={() => setSelectedDay(day)}
                className="relative flex-1 py-2.5 md:py-3 rounded-xl text-[12px] md:text-[13px] font-bold transition-all duration-200"
              >
                {selectedDay === day && (
                  <motion.div
                    layoutId="daySelector"
                    className="absolute inset-0 bg-card shadow-md shadow-primary/5 rounded-xl border border-border/50"
                    transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                  />
                )}
                <span className={`relative z-10 ${
                  selectedDay === day ? 'text-primary font-extrabold' : 'text-muted-foreground'
                }`}>
                  {day}
                </span>
                {day === today && (
                  <span className="absolute top-1.5 right-1.5 size-1.5 rounded-full bg-primary z-10" />
                )}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Classes */}
      <main className="flex-1 px-4 md:px-6 py-5">
        <div className="max-w-3xl mx-auto">
          <div className="flex items-center justify-between mb-4">
            <p className="text-muted-foreground text-[11px] md:text-[12px] font-bold uppercase tracking-[0.15em]">{dayNames[selectedDay]}</p>
            <span className="text-[10px] md:text-[11px] font-bold text-primary bg-primary/8 px-2.5 py-1 rounded-full border border-primary/10">
              {classes.length} {classes.length === 1 ? 'Class' : 'Classes'}
            </span>
          </div>

          <AnimatePresence mode="wait">
            <motion.div key={selectedDay} initial="hidden" animate="visible" exit="exit" className="relative">
              {classes.length === 0 ? (
                <motion.div variants={cardVariants} custom={0} className="flex flex-col items-center justify-center py-20 text-center">
                  <div className="w-16 h-16 rounded-3xl bg-primary/8 flex items-center justify-center mb-4">
                    <span className="material-symbols-outlined text-[32px] text-primary/30">event_available</span>
                  </div>
                  <p className="text-foreground font-bold text-[16px] md:text-[18px]">No classes today!</p>
                  <p className="text-muted-foreground text-[13px] mt-1">Enjoy your free day 🎉</p>
                </motion.div>
              ) : (
                <>
                  {/* Timeline line */}
                  <div className="absolute left-[19px] top-6 bottom-6 w-[2px] bg-gradient-to-b from-primary/20 via-border to-border rounded-full" />
                  <div className="space-y-3.5 md:space-y-4">
                    {classes.map((cls, idx) => {
                      const attendance = getAttendance(todayStr, cls.subject);
                      const stats = getSubjectStats(cls.subject);

                      return (
                        <motion.div key={`${selectedDay}-${idx}`} variants={cardVariants} custom={idx} className="flex gap-3 relative">
                          {/* Timeline dot */}
                          <div className="relative z-10 mt-5 shrink-0">
                            <div className={`size-[10px] rounded-full border-2 transition-colors ${
                              idx === 0 ? 'border-primary bg-primary shadow-sm shadow-primary/30' : 'border-border bg-card'
                            }`} style={{ marginLeft: '10px' }} />
                          </div>

                          {/* Card */}
                          <div className={`flex-1 bg-card card-elevated rounded-2xl p-4 md:p-5 border transition-all duration-300 ${
                            idx === 0 ? 'border-primary/15 ring-1 ring-primary/5' : 'border-border/70'
                          }`}>
                            {idx === 0 && (
                              <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-primary via-blue-500 to-transparent rounded-t-2xl" />
                            )}
                            <div className="flex items-start justify-between">
                              <div className="flex gap-3">
                                <div className={`size-11 md:size-12 rounded-2xl ${cls.iconBg} ${cls.iconColor} flex items-center justify-center shrink-0`}>
                                  <span className="material-symbols-outlined text-[22px] md:text-[24px]">{cls.icon}</span>
                                </div>
                                <div>
                                  <h3 className="font-bold text-foreground text-[14px] md:text-[15px] leading-tight">{cls.subject}</h3>
                                  <div className="mt-1.5 space-y-0.5">
                                    <p className="text-[11px] md:text-[12px] text-muted-foreground flex items-center gap-1">
                                      <span className="material-symbols-outlined text-[13px]">schedule</span> {cls.time}
                                    </p>
                                    <div className="flex items-center gap-3">
                                      <p className="text-[11px] md:text-[12px] text-muted-foreground flex items-center gap-1">
                                        <span className="material-symbols-outlined text-[13px]">location_on</span> {cls.room}
                                      </p>
                                      {cls.faculty && (
                                        <p className="text-[11px] md:text-[12px] text-muted-foreground flex items-center gap-1">
                                          <span className="material-symbols-outlined text-[13px]">person</span> {cls.faculty}
                                        </p>
                                      )}
                                    </div>
                                  </div>
                                </div>
                              </div>
                              <div className="flex flex-col items-end gap-1.5">
                                {cls.type && (
                                  <span className="text-[9px] md:text-[10px] font-bold py-0.5 px-2.5 rounded-full bg-purple-500/10 text-purple-600 dark:text-purple-400 uppercase tracking-wide">{cls.type}</span>
                                )}
                                {stats.total > 0 && (
                                  <span className={`text-[9px] md:text-[10px] font-bold py-0.5 px-2.5 rounded-full ${stats.percentage >= 75 ? 'bg-green-500/10 text-green-600' : 'bg-red-500/10 text-red-500'}`}>
                                    {stats.percentage}%
                                  </span>
                                )}
                              </div>
                            </div>

                            {/* Attendance buttons */}
                            <div className="grid grid-cols-2 gap-2.5 mt-4">
                              <button
                                onClick={() => handleAttendance(cls.subject, 'present')}
                                className={`flex items-center justify-center gap-1.5 text-[12px] md:text-[13px] font-semibold py-2.5 md:py-3 rounded-xl transition-all duration-200 active:scale-[0.96] ${
                                  attendance === 'present'
                                    ? 'bg-green-500 text-white shadow-md shadow-green-500/20'
                                    : 'bg-green-500/8 text-green-600 dark:text-green-400 border border-green-500/15 hover:bg-green-500/15'
                                }`}
                              >
                                <span className="material-symbols-outlined text-[16px]">check_circle</span>
                                {attendance === 'present' ? 'Present ✓' : 'Present'}
                              </button>
                              <button
                                onClick={() => handleAttendance(cls.subject, 'absent')}
                                className={`flex items-center justify-center gap-1.5 text-[12px] md:text-[13px] font-semibold py-2.5 md:py-3 rounded-xl transition-all duration-200 active:scale-[0.96] ${
                                  attendance === 'absent'
                                    ? 'bg-red-500 text-white shadow-md shadow-red-500/20'
                                    : 'bg-card border border-border text-muted-foreground hover:bg-red-500/5 hover:border-red-500/20 hover:text-red-500'
                                }`}
                              >
                                <span className="material-symbols-outlined text-[16px]">cancel</span>
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
