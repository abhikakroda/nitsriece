import React, { useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { requestNotificationPermission } from '@/lib/notifications';
import { getTodayClasses, getClassStatus, dayNames, getTodayDay, timetableData } from '@/lib/timetable';
import { getAllSubjectStats } from '@/lib/attendance';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.06 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 18 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const Dashboard: React.FC = () => {
  const navigate = useNavigate();

  useEffect(() => {
    requestNotificationPermission();
  }, []);

  const todayClasses = getTodayClasses();
  const todayDay = getTodayDay();
  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';

  const allStats = useMemo(() => getAllSubjectStats(), []);
  const statEntries = Object.entries(allStats);
  const totalPresent = statEntries.reduce((a, [, s]) => a + s.present, 0);
  const totalAbsent = statEntries.reduce((a, [, s]) => a + s.absent, 0);
  const totalClasses = totalPresent + totalAbsent;
  const overallPercent = totalClasses > 0 ? Math.round((totalPresent / totalClasses) * 100) : 0;
  const isAbove75 = overallPercent >= 75 || totalClasses === 0;

  // Count weekly classes from timetable
  const weeklyTotal = Object.values(timetableData).reduce((a, d) => a + d.length, 0);

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6 relative overflow-x-hidden">
      {/* Ambient blobs */}
      <div className="liquid-blob top-[-6%] right-[-15%] w-[300px] h-[300px] bg-primary/8 dark:bg-primary/6 rounded-full filter blur-[100px] animate-blob will-change-transform pointer-events-none" />
      <div className="liquid-blob bottom-[10%] left-[-10%] w-[250px] h-[250px] bg-violet-400/8 dark:bg-violet-500/5 rounded-full filter blur-[100px] animate-blob animation-delay-4000 will-change-transform pointer-events-none" />

      <motion.div className="max-w-3xl mx-auto w-full relative z-10" variants={stagger} initial="hidden" animate="visible">
        {/* Greeting */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pt-7 pb-1">
          <h1 className="font-display text-[28px] md:text-[34px] font-bold text-foreground tracking-tight leading-[1.1]">
            {greeting} 👋
          </h1>
          <p className="text-muted-foreground text-[13px] md:text-[14px] mt-1.5">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}
          </p>
        </motion.div>

        {/* Stat Cards Row */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 py-4">
          <div className="grid grid-cols-3 gap-2.5">
            {[
              { label: 'Today', value: String(todayClasses.length), sub: todayClasses.length === 1 ? 'Class' : 'Classes' },
              { label: 'Weekly', value: String(weeklyTotal), sub: 'Total' },
              { label: 'Attendance', value: totalClasses > 0 ? `${overallPercent}%` : 'N/A', sub: totalClasses > 0 ? 'Overall' : 'No data' },
            ].map((stat, i) => (
              <div key={i} className="rounded-[14px] border border-border/60 dark:border-border/40 bg-card/50 dark:bg-card/30 p-3.5 md:p-4">
                <p className="text-[10px] md:text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">{stat.label}</p>
                <p className="font-display text-[24px] md:text-[28px] font-bold text-foreground leading-none mt-1">{stat.value}</p>
                <p className="text-[10px] md:text-[11px] text-muted-foreground mt-0.5">{stat.sub}</p>
              </div>
            ))}
          </div>
        </motion.div>

        {/* Smart Reminder / Exam Countdown */}
        {localStorage.getItem('hideExamCountdown') !== 'true' && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-4">
            {(() => {
              const examDate = new Date('2025-04-01T00:00:00');
              const now = new Date();
              const diffMs = examDate.getTime() - now.getTime();
              const totalDays = Math.max(0, Math.ceil(diffMs / (1000 * 60 * 60 * 24)));
              const weeks = Math.floor(totalDays / 7);
              const remainingDays = totalDays % 7;
              return (
                <div className="bg-gradient-to-br from-orange-400 via-rose-400 to-pink-500 rounded-[18px] p-5 md:p-6 text-white shadow-lg shadow-orange-500/15 dark:shadow-orange-500/10 overflow-hidden relative noise-overlay">
                  <div className="absolute inset-0 animate-shimmer" />
                  <div className="relative z-10">
                    <p className="text-white/70 text-[11px] font-bold uppercase tracking-[0.15em] mb-1">Sessional Exams</p>
                    <h3 className="font-display text-[20px] md:text-[22px] font-bold leading-tight">
                      {totalDays > 0 ? `${totalDays} days to go` : 'Exams have started!'}
                    </h3>
                    <p className="text-white/60 text-[12px] mt-1.5">
                      {totalDays > 0 ? `${weeks} weeks and ${remainingDays} days remaining` : 'Good luck!'} • April 1, 2025
                    </p>
                  </div>
                  <div className="absolute right-[-20px] bottom-[-30px] w-40 h-40 bg-white/8 rounded-full blur-2xl" />
                  <div className="absolute right-4 top-4 bg-white/15 p-2 rounded-xl backdrop-blur-sm border border-white/10 z-10">
                    <span className="material-symbols-outlined text-[20px]">timer</span>
                  </div>
                </div>
              );
            })()}
          </motion.div>
        )}

        {/* Attendance Status Banner */}
        {totalClasses > 0 && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-4">
            <div className={`rounded-[14px] p-4 border ${
              isAbove75 
                ? 'bg-green-500/6 dark:bg-green-500/8 border-green-500/15' 
                : 'bg-red-500/6 dark:bg-red-500/8 border-red-500/15'
            }`}>
              <div className="flex items-center gap-2 mb-1">
                <span className={`material-symbols-outlined text-[18px] ${isAbove75 ? 'text-green-500' : 'text-red-500'}`}>
                  {isAbove75 ? 'verified' : 'warning'}
                </span>
                <p className={`text-[13px] font-bold ${isAbove75 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}`}>
                  {isAbove75 ? 'All Clear' : 'Attendance Low'}
                </p>
              </div>
              <p className="text-muted-foreground text-[12px]">
                {isAbove75 
                  ? 'You are above the required attendance. Keep the streak going.' 
                  : 'Your attendance is below 75%. Try not to miss more classes.'}
              </p>
            </div>
          </motion.div>
        )}

        {/* Today's Classes */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pb-2">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h2 className="font-display text-[18px] md:text-[20px] font-bold text-foreground">Today's Classes</h2>
              <p className="text-muted-foreground text-[12px] mt-0.5">{dayNames[todayDay]}</p>
            </div>
            <button onClick={() => navigate('/schedule')} className="text-primary text-[12px] font-semibold hover:underline underline-offset-2 active:opacity-70 flex items-center gap-0.5">
              Full Week
              <span className="material-symbols-outlined text-[14px]">arrow_forward</span>
            </button>
          </div>
        </motion.div>

        {todayClasses.length === 0 ? (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-5">
            <div className="bg-gradient-to-br from-orange-400 via-rose-400 to-pink-500 rounded-[18px] p-5 text-white overflow-hidden relative noise-overlay">
              <div className="relative z-10">
                <p className="text-white/70 text-[11px] font-bold uppercase tracking-wider mb-1">Smart Reminder</p>
                <h3 className="font-display text-[18px] font-bold">No classes scheduled today</h3>
                <p className="text-white/60 text-[12px] mt-1">Enjoy your free {dayNames[todayDay]} 🎉</p>
              </div>
              <div className="absolute right-[-20px] bottom-[-30px] w-36 h-36 bg-white/8 rounded-full blur-2xl" />
            </div>
          </motion.div>
        ) : (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-5 space-y-2.5">
            {todayClasses.map((cls, idx) => {
              const status = getClassStatus(cls.startTime);
              const colors = [
                'bg-amber-50 dark:bg-amber-500/8 border-amber-200/60 dark:border-amber-500/15',
                'bg-purple-50 dark:bg-purple-500/8 border-purple-200/60 dark:border-purple-500/15',
                'bg-green-50 dark:bg-green-500/8 border-green-200/60 dark:border-green-500/15',
                'bg-blue-50 dark:bg-blue-500/8 border-blue-200/60 dark:border-blue-500/15',
                'bg-rose-50 dark:bg-rose-500/8 border-rose-200/60 dark:border-rose-500/15',
              ];
              const textColors = [
                'text-amber-800 dark:text-amber-300',
                'text-purple-800 dark:text-purple-300',
                'text-green-800 dark:text-green-300',
                'text-blue-800 dark:text-blue-300',
                'text-rose-800 dark:text-rose-300',
              ];
              const colorIdx = idx % colors.length;

              return (
                <div
                  key={idx}
                  className={`rounded-[16px] p-4 border transition-all duration-300 ${colors[colorIdx]} ${
                    status === 'done' ? 'opacity-40' : ''
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      {cls.type && (
                        <span className={`text-[9px] font-bold py-0.5 px-2 rounded-full border uppercase tracking-wider mb-2 inline-block ${
                          cls.type === 'Lab' ? 'border-purple-300 dark:border-purple-500/30 text-purple-600 dark:text-purple-400' : 'border-amber-300 dark:border-amber-500/30 text-amber-700 dark:text-amber-400'
                        }`}>
                          {cls.type}
                        </span>
                      )}
                      {!cls.type && (
                        <span className="text-[9px] font-bold py-0.5 px-2 rounded-full border border-amber-300 dark:border-amber-500/30 text-amber-700 dark:text-amber-400 uppercase tracking-wider mb-2 inline-block">
                          Lecture
                        </span>
                      )}
                      <h3 className={`font-display font-bold text-[16px] md:text-[18px] leading-tight mt-1 ${textColors[colorIdx]}`}>{cls.subject}</h3>
                      <div className="flex items-center gap-3 mt-2">
                        <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                          <span className="material-symbols-outlined text-[14px]">person</span> {cls.faculty || 'TBA'}
                        </p>
                        <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                          <span className="material-symbols-outlined text-[14px]">location_on</span> {cls.room}
                        </p>
                      </div>
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-[11px] font-semibold text-muted-foreground">{cls.time}</p>
                      <span className={`text-[9px] font-bold py-0.5 px-2 rounded-full uppercase tracking-wider mt-1.5 inline-block ${
                        status === 'current' ? 'bg-primary/10 text-primary' 
                        : status === 'done' ? 'bg-secondary text-muted-foreground'
                        : 'bg-secondary text-muted-foreground'
                      }`}>
                        {status === 'current' ? '● Live' : status === 'done' ? 'Done' : 'Up Next'}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </motion.div>
        )}

        {/* Quick Access */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pb-6">
          <div className="grid grid-cols-2 gap-3">
            <div
              onClick={() => navigate('/schedule')}
              className="relative bg-foreground dark:bg-card p-4 rounded-[16px] flex flex-col justify-between cursor-pointer h-[110px] overflow-hidden group active:scale-[0.97] transition-all duration-200 shadow-lg shadow-foreground/5 dark:shadow-none border border-transparent dark:border-border/40"
            >
              <div className="size-9 rounded-[10px] bg-white/10 dark:bg-primary/10 flex items-center justify-center relative z-10">
                <span className="material-symbols-outlined text-white dark:text-primary text-[18px]">calendar_today</span>
              </div>
              <div className="relative z-10">
                <h3 className="font-display font-bold text-white dark:text-foreground text-[14px]">Schedule</h3>
                <p className="text-[10px] text-white/40 dark:text-muted-foreground font-medium">Weekly Timetable</p>
              </div>
            </div>
            <div
              onClick={() => navigate('/gpa')}
              className="relative bg-gradient-to-br from-primary/6 to-violet-500/6 dark:from-primary/8 dark:to-violet-500/8 p-4 rounded-[16px] flex flex-col justify-between cursor-pointer border border-primary/8 h-[110px] active:scale-[0.97] transition-all duration-200 group"
            >
              <div className="size-9 rounded-[10px] bg-primary text-white flex items-center justify-center shadow-sm shadow-primary/20">
                <span className="material-symbols-outlined text-[18px]">calculate</span>
              </div>
              <div>
                <h3 className="font-display font-bold text-foreground text-[14px]">GPA Calc</h3>
                <p className="text-[10px] text-muted-foreground font-medium">Estimate SGPA</p>
              </div>
            </div>
          </div>
        </motion.div>
      </motion.div>
    </div>
  );
};

export default Dashboard;
