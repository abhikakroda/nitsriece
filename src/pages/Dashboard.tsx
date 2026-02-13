import React, { useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { requestNotificationPermission, startClassNotifications, stopClassNotifications } from '@/lib/notifications';
import { getTodayClasses, getClassStatus, dayNames, getTodayDay, timetableData } from '@/lib/timetable';
import { getAllSubjectStats } from '@/lib/attendance';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.06 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.45, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const todayClasses = getTodayClasses();
  const todayDay = getTodayDay();
  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';

  useEffect(() => {
    requestNotificationPermission();
  }, []);

  useEffect(() => {
    if (todayClasses.length > 0) {
      startClassNotifications(todayClasses.map(cls => ({
        subject: cls.subject,
        startTime: cls.startTime,
        room: cls.room,
      })));
    }
    return () => stopClassNotifications();
  }, []);

  const allStats = useMemo(() => getAllSubjectStats(), []);
  const statEntries = Object.entries(allStats);
  const totalPresent = statEntries.reduce((a, [, s]) => a + s.present, 0);
  const totalAbsent = statEntries.reduce((a, [, s]) => a + s.absent, 0);
  const totalClasses = totalPresent + totalAbsent;
  const overallPercent = totalClasses > 0 ? Math.round((totalPresent / totalClasses) * 100) : 0;
  const weeklyTotal = Object.values(timetableData).reduce((a, d) => a + d.length, 0);

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6 relative overflow-x-hidden">
      <motion.div className="max-w-3xl mx-auto w-full relative z-10" variants={stagger} initial="hidden" animate="visible">
        {/* Greeting */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pt-8 pb-2">
          <p className="text-muted-foreground text-[12px] font-medium">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
          </p>
          <h1 className="font-display text-[26px] md:text-[32px] font-bold text-foreground tracking-tight leading-[1.15] mt-0.5">
            {greeting}
          </h1>
        </motion.div>

        {/* Stat Cards */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 py-4">
          <div className="grid grid-cols-3 gap-2">
            {[
              { label: 'Today', value: String(todayClasses.length), sub: todayClasses.length === 1 ? 'class' : 'classes' },
              { label: 'Weekly', value: String(weeklyTotal), sub: 'total' },
              { label: 'Attendance', value: totalClasses > 0 ? `${overallPercent}%` : '—', sub: totalClasses > 0 ? 'overall' : 'no data' },
            ].map((stat, i) => (
              <div key={i} className="rounded-2xl bg-card border border-border/40 p-3.5">
                <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">{stat.label}</p>
                <p className="font-display text-[22px] md:text-[26px] font-bold text-foreground leading-none mt-1">{stat.value}</p>
                <p className="text-[10px] text-muted-foreground/70 mt-0.5">{stat.sub}</p>
              </div>
            ))}
          </div>
        </motion.div>

        {/* Exam Countdown */}
        {localStorage.getItem('hideExamCountdown') === 'false' && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-4">
            {(() => {
              const examDate = new Date('2026-04-01T00:00:00');
              const now = new Date();
              const diffMs = examDate.getTime() - now.getTime();
              const totalDays = Math.max(0, Math.ceil(diffMs / (1000 * 60 * 60 * 24)));
              const weeks = Math.floor(totalDays / 7);
              const remainingDays = totalDays % 7;
              return (
                <div className="bg-gradient-to-r from-orange-500 to-rose-500 rounded-2xl p-4 text-white overflow-hidden relative">
                  <div className="relative z-10 flex items-center justify-between">
                    <div>
                      <p className="text-white/60 text-[10px] font-bold uppercase tracking-widest">Sessional Exams</p>
                      <p className="font-display text-[18px] font-bold mt-0.5">
                        {totalDays > 0 ? `${totalDays} days to go` : 'Exams have started!'}
                      </p>
                      <p className="text-white/50 text-[11px] mt-0.5">
                        {totalDays > 0 ? `${weeks}w ${remainingDays}d` : 'Good luck!'} • Apr 1
                      </p>
                    </div>
                    <span className="material-symbols-outlined text-white/20 text-[36px]">timer</span>
                  </div>
                </div>
              );
            })()}
          </motion.div>
        )}

        {/* Today's Classes */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pb-2">
          <div className="flex items-baseline justify-between mb-3">
            <h2 className="font-display text-[16px] md:text-[18px] font-bold text-foreground">Today's Classes</h2>
            <button onClick={() => navigate('/schedule')} className="text-primary text-[11px] font-semibold flex items-center gap-0.5">
              Full week
              <span className="material-symbols-outlined text-[14px]">chevron_right</span>
            </button>
          </div>
        </motion.div>

        {todayClasses.length === 0 ? (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-6">
            <div className="rounded-2xl border border-border/40 bg-card p-6 text-center">
              <p className="text-muted-foreground text-[13px]">No classes today 🎉</p>
              <p className="text-muted-foreground/50 text-[11px] mt-1">Enjoy your {dayNames[todayDay]}</p>
            </div>
          </motion.div>
        ) : (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-6 space-y-2">
            {todayClasses.map((cls, idx) => {
              const status = getClassStatus(cls.startTime);
              return (
                <div
                  key={idx}
                  className={`rounded-2xl bg-card border border-border/40 p-4 transition-opacity ${
                    status === 'done' ? 'opacity-40' : ''
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-wider">
                          {cls.type || 'Lecture'}
                        </span>
                        {status === 'current' && (
                          <span className="text-[9px] font-bold text-primary bg-primary/8 px-1.5 py-0.5 rounded-full">● Live</span>
                        )}
                      </div>
                      <h3 className="font-display font-bold text-[15px] md:text-[17px] text-foreground leading-tight">{cls.subject}</h3>
                      <div className="flex items-center gap-3 mt-1.5">
                        <span className="text-[11px] text-muted-foreground">{cls.faculty || 'TBA'}</span>
                        <span className="text-[11px] text-muted-foreground">{cls.room}</span>
                      </div>
                    </div>
                    <span className="text-[11px] font-medium text-muted-foreground shrink-0">{cls.time}</span>
                  </div>
                </div>
              );
            })}
          </motion.div>
        )}

        {/* Quick Links */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pb-6">
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => navigate('/schedule')}
              className="rounded-2xl bg-card border border-border/40 p-4 text-left active:scale-[0.97] transition-transform"
            >
              <span className="material-symbols-outlined text-muted-foreground text-[20px]">calendar_today</span>
              <p className="font-display font-bold text-foreground text-[13px] mt-2">Schedule</p>
              <p className="text-[10px] text-muted-foreground">Weekly timetable</p>
            </button>
            <button
              onClick={() => navigate('/analytics')}
              className="rounded-2xl bg-card border border-border/40 p-4 text-left active:scale-[0.97] transition-transform"
            >
              <span className="material-symbols-outlined text-muted-foreground text-[20px]">monitoring</span>
              <p className="font-display font-bold text-foreground text-[13px] mt-2">Analytics</p>
              <p className="text-[10px] text-muted-foreground">Attendance stats</p>
            </button>
          </div>
        </motion.div>
      </motion.div>
    </div>
  );
};

export default Dashboard;
