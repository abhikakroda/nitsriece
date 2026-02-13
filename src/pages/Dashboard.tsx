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

  const nextClass = todayClasses.find(cls => getClassStatus(cls.startTime) === 'upcoming');
  const currentClass = todayClasses.find(cls => getClassStatus(cls.startTime) === 'current');
  const doneCount = todayClasses.filter(cls => getClassStatus(cls.startTime) === 'done').length;
  const atRisk = statEntries.filter(([, s]) => s.total > 0 && s.percentage < 75);

  return (
    <div className="flex flex-col min-h-screen bg-background pb-24 lg:pb-6 relative overflow-x-hidden">
      <motion.div className="max-w-3xl mx-auto w-full relative z-10" variants={stagger} initial="hidden" animate="visible">
        {/* Greeting */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pt-8 pb-1">
          <p className="text-muted-foreground text-[12px] font-medium">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
          </p>
          <h1 className="font-display text-[26px] md:text-[32px] font-bold text-foreground tracking-tight leading-[1.15] mt-0.5">
            {greeting}
          </h1>
        </motion.div>

        {/* Now / Next class highlight */}
        {(currentClass || nextClass) && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pt-4 pb-2">
            {currentClass ? (
              <div className="rounded-2xl liquid-glass p-4">
                <div className="flex items-center gap-2 mb-2">
                  <span className="size-2 rounded-full bg-emerald-400 animate-pulse" />
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Now</span>
                </div>
                <h3 className="font-display font-bold text-[18px] md:text-[20px] text-foreground leading-tight">{currentClass.subject}</h3>
                <div className="flex items-center gap-3 mt-1.5">
                  <span className="text-[11px] text-muted-foreground">{currentClass.time}</span>
                  <span className="text-[11px] text-muted-foreground">{currentClass.room}</span>
                  <span className="text-[11px] text-muted-foreground">{currentClass.faculty || 'TBA'}</span>
                </div>
              </div>
            ) : nextClass ? (
              <div className="rounded-2xl liquid-glass-card p-4">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Up Next</span>
                </div>
                <h3 className="font-display font-bold text-[18px] md:text-[20px] text-foreground leading-tight">{nextClass.subject}</h3>
                <div className="flex items-center gap-3 mt-1.5">
                  <span className="text-[11px] text-muted-foreground">{nextClass.time}</span>
                  <span className="text-[11px] text-muted-foreground">{nextClass.room}</span>
                  <span className="text-[11px] text-muted-foreground">{nextClass.faculty || 'TBA'}</span>
                </div>
              </div>
            ) : null}
          </motion.div>
        )}

        {/* Stats Row */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 py-3">
          <div className="grid grid-cols-4 gap-2">
            {[
              { label: 'Today', value: String(todayClasses.length) },
              { label: 'Done', value: String(doneCount) },
              { label: 'Weekly', value: String(weeklyTotal) },
              { label: 'Attend.', value: totalClasses > 0 ? `${overallPercent}%` : '—' },
            ].map((stat, i) => (
              <div key={i} className="rounded-2xl liquid-glass-card p-3 text-center">
                <p className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">{stat.label}</p>
                <p className="font-display text-[20px] md:text-[22px] font-bold text-foreground leading-none mt-1">{stat.value}</p>
              </div>
            ))}
          </div>
        </motion.div>

        {/* Attendance Alert */}
        {atRisk.length > 0 && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-2">
            <div className="rounded-2xl liquid-glass-card p-3.5" style={{ borderColor: 'rgba(244,63,94,0.15)' }}>
              <div className="flex items-center gap-2 mb-1.5">
                <span className="material-symbols-outlined text-rose-500 text-[16px]">warning</span>
                <span className="text-[10px] font-bold text-rose-600 dark:text-rose-400 uppercase tracking-widest">Low Attendance</span>
              </div>
              <div className="space-y-1">
                {atRisk.map(([name, s]) => (
                  <div key={name} className="flex items-center justify-between">
                    <span className="text-[11px] text-foreground/80 truncate flex-1 mr-2">{name}</span>
                    <span className="text-[11px] font-bold text-rose-500">{s.percentage}%</span>
                  </div>
                ))}
              </div>
            </div>
          </motion.div>
        )}

        {/* Exam Countdown */}
        {localStorage.getItem('hideExamCountdown') === 'false' && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-2">
            {(() => {
              const examDate = new Date('2026-04-01T00:00:00');
              const now = new Date();
              const diffMs = examDate.getTime() - now.getTime();
              const totalDays = Math.max(0, Math.ceil(diffMs / (1000 * 60 * 60 * 24)));
              const weeks = Math.floor(totalDays / 7);
              const remainingDays = totalDays % 7;
              return (
                <div className="rounded-2xl liquid-glass-card p-3.5 flex items-center justify-between">
                  <div>
                    <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-widest">Sessionals</p>
                    <p className="font-display text-[16px] font-bold text-foreground mt-0.5">
                      {totalDays > 0 ? `${totalDays} days` : 'Started'}
                    </p>
                    <p className="text-muted-foreground text-[10px] mt-0.5">
                      {totalDays > 0 ? `${weeks}w ${remainingDays}d remaining` : 'Good luck!'} • Apr 1
                    </p>
                  </div>
                  <span className="material-symbols-outlined text-muted-foreground/20 text-[28px]">event</span>
                </div>
              );
            })()}
          </motion.div>
        )}

        {/* Today's Schedule */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pt-2 pb-2">
          <div className="flex items-baseline justify-between mb-2">
            <h2 className="text-[12px] font-bold text-muted-foreground uppercase tracking-widest">Today's Schedule</h2>
            <button onClick={() => navigate('/schedule')} className="text-primary text-[11px] font-semibold flex items-center gap-0.5">
              Full week
              <span className="material-symbols-outlined text-[14px]">chevron_right</span>
            </button>
          </div>
        </motion.div>

        {todayClasses.length === 0 ? (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-6">
            <div className="rounded-2xl liquid-glass-card p-6 text-center">
              <p className="text-muted-foreground text-[13px]">No classes today</p>
              <p className="text-muted-foreground/50 text-[11px] mt-1">Enjoy your {dayNames[todayDay]}</p>
            </div>
          </motion.div>
        ) : (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-4">
            <div className="rounded-2xl liquid-glass divide-y divide-white/10 dark:divide-white/5">
              {todayClasses.map((cls, idx) => {
                const status = getClassStatus(cls.startTime);
                return (
                  <div
                    key={idx}
                    className={`px-4 py-3 flex items-center gap-3 transition-opacity ${
                      status === 'done' ? 'opacity-35' : ''
                    }`}
                  >
                    <div className="w-[42px] shrink-0 text-center">
                      <p className="text-[11px] font-bold text-foreground leading-none">{cls.startTime}</p>
                    </div>
                    <div className="shrink-0">
                      {status === 'current' ? (
                        <span className="size-2 rounded-full bg-emerald-500 block animate-pulse" />
                      ) : status === 'done' ? (
                        <span className="material-symbols-outlined text-[14px] text-muted-foreground/40">check_circle</span>
                      ) : (
                        <span className="size-2 rounded-full border-2 border-white/30 dark:border-white/10 block" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-[13px] font-semibold text-foreground leading-tight truncate">{cls.subject}</p>
                      <p className="text-[10px] text-muted-foreground mt-0.5">{cls.faculty || 'TBA'} · {cls.room}</p>
                    </div>
                    <span className="text-[9px] font-medium text-muted-foreground/60 uppercase tracking-wider shrink-0">
                      {cls.type || 'Lec'}
                    </span>
                  </div>
                );
              })}
            </div>
          </motion.div>
        )}

        {/* Quick Links */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pb-6">
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => navigate('/schedule')}
              className="rounded-2xl liquid-glass-card p-4 text-left active:scale-[0.97] transition-transform"
            >
              <span className="material-symbols-outlined text-muted-foreground text-[20px]">calendar_today</span>
              <p className="font-display font-bold text-foreground text-[13px] mt-2">Schedule</p>
              <p className="text-[10px] text-muted-foreground">Weekly timetable</p>
            </button>
            <button
              onClick={() => navigate('/analytics')}
              className="rounded-2xl liquid-glass-card p-4 text-left active:scale-[0.97] transition-transform"
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
