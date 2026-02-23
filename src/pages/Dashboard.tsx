import React, { useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { requestNotificationPermission, startClassNotifications, stopClassNotifications } from '@/lib/notifications';
import { getTodayClasses, getClassStatus, getTodayDay, timetableData, getExamDate, to12hr } from '@/lib/timetable';
import { getAllSubjectStats } from '@/lib/attendance';
import { messMenu, getTodayFullDay, getMessPreference, type Meal } from '@/lib/messMenu';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.055 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.42, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const todayClasses = getTodayClasses();
  const todayDay = getTodayDay();
  const hour = new Date().getHours();
  const minutes = new Date().getMinutes();
  const timeInMinutes = hour * 60 + minutes;

  const greeting = hour < 5 ? 'Good night' : hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
  const greetingEmoji = hour < 5 ? '🌙' : hour < 12 ? '☀️' : hour < 17 ? '🌤️' : '🌙';

  const todayFullDay = getTodayFullDay();
  const todayMeals = messMenu[todayFullDay];
  const messPref = getMessPreference();

  const activeMeal: Meal | null = (() => {
    if (timeInMinutes >= 450 && timeInMinutes < 540) return todayMeals.find(m => m.type === 'breakfast') || null;
    if (timeInMinutes >= 720 && timeInMinutes < 780) return todayMeals.find(m => m.type === 'lunch') || null;
    if (timeInMinutes >= 1080 && timeInMinutes < 1200) return todayMeals.find(m => m.type === 'dinner') || null;
    return null;
  })();

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

  const attendanceColor = totalClasses === 0 ? 'text-muted-foreground' :
    overallPercent >= 75 ? 'text-emerald-600 dark:text-emerald-400' :
    overallPercent >= 60 ? 'text-amber-600 dark:text-amber-400' :
    'text-rose-600 dark:text-rose-400';

  const typeColors: Record<string, string> = {
    'Lab': 'text-violet-600 dark:text-violet-400 bg-violet-500/10',
    'Tutorial': 'text-amber-600 dark:text-amber-400 bg-amber-500/10',
  };

  return (
    <div className="flex flex-col min-h-screen bg-background pb-28 lg:pb-8 relative overflow-x-hidden">
      <motion.div className="max-w-3xl mx-auto w-full relative z-10" variants={stagger} initial="hidden" animate="visible">

        {/* Greeting Header */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pt-10 pb-3">
          <p className="text-muted-foreground text-[12px] font-medium tracking-wide">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
          </p>
          <h1 className="font-display text-[28px] md:text-[34px] font-bold text-foreground tracking-tight leading-[1.15] mt-1">
            {greeting} <span>{greetingEmoji}</span>
          </h1>
        </motion.div>

        {/* Current / Next class highlight */}
        {(currentClass || nextClass) && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pt-1 pb-2">
            {currentClass ? (
              <div className="rounded-2xl overflow-hidden relative" style={{
                background: 'linear-gradient(135deg, rgba(16,185,129,0.13) 0%, rgba(5,150,105,0.07) 100%)',
                border: '1px solid rgba(16,185,129,0.22)',
                boxShadow: '0 4px 24px -4px rgba(16,185,129,0.15), inset 0 1px 0 rgba(255,255,255,0.25)'
              }}>
                <div className="absolute left-0 top-0 bottom-0 w-[3px] bg-emerald-500 rounded-r-full" />
                <div className="p-4 pl-5">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
                    <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400 uppercase tracking-widest">In Progress</span>
                  </div>
                  <h3 className="font-display font-bold text-[20px] text-foreground leading-tight">{currentClass.subject}</h3>
                  <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2">
                    <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                      <span className="material-symbols-outlined text-[13px]">schedule</span>
                      {currentClass.time}
                    </span>
                    <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                      <span className="material-symbols-outlined text-[13px]">room</span>
                      {currentClass.room}
                    </span>
                    {currentClass.faculty && (
                      <span className="text-[11px] text-muted-foreground">{currentClass.faculty}</span>
                    )}
                  </div>
                </div>
              </div>
            ) : nextClass ? (
              <div className="rounded-2xl overflow-hidden relative" style={{
                background: 'linear-gradient(135deg, rgba(99,102,241,0.1) 0%, rgba(79,70,229,0.05) 100%)',
                border: '1px solid rgba(99,102,241,0.18)',
                boxShadow: '0 4px 24px -4px rgba(99,102,241,0.1), inset 0 1px 0 rgba(255,255,255,0.25)'
              }}>
                <div className="absolute left-0 top-0 bottom-0 w-[3px] bg-primary rounded-r-full" />
                <div className="p-4 pl-5">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-[10px] font-bold text-primary/80 uppercase tracking-widest">Up Next</span>
                  </div>
                  <h3 className="font-display font-bold text-[20px] text-foreground leading-tight">{nextClass.subject}</h3>
                  <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2">
                    <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                      <span className="material-symbols-outlined text-[13px]">schedule</span>
                      {nextClass.time}
                    </span>
                    <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                      <span className="material-symbols-outlined text-[13px]">room</span>
                      {nextClass.room}
                    </span>
                    {nextClass.faculty && (
                      <span className="text-[11px] text-muted-foreground">{nextClass.faculty}</span>
                    )}
                  </div>
                </div>
              </div>
            ) : null}
          </motion.div>
        )}

        {/* Stats Row */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 py-3">
          <div className="grid grid-cols-4 gap-2">
            {[
              { label: 'Today', value: String(todayClasses.length), icon: 'calendar_today', color: 'text-primary', bg: 'rgba(99,102,241,0.08)' },
              { label: 'Done', value: String(doneCount), icon: 'check_circle', color: 'text-emerald-600 dark:text-emerald-400', bg: 'rgba(16,185,129,0.08)' },
              { label: 'Weekly', value: String(weeklyTotal), icon: 'view_week', color: 'text-violet-600 dark:text-violet-400', bg: 'rgba(139,92,246,0.08)' },
              {
                label: 'Attend.',
                value: totalClasses > 0 ? `${overallPercent}%` : '—',
                icon: 'bar_chart',
                color: attendanceColor,
                bg: totalClasses === 0 ? 'rgba(0,0,0,0.04)' :
                  overallPercent >= 75 ? 'rgba(16,185,129,0.08)' :
                  overallPercent >= 60 ? 'rgba(245,158,11,0.08)' : 'rgba(244,63,94,0.08)'
              },
            ].map((stat, i) => (
              <div key={i} className="rounded-2xl liquid-glass-card p-3 text-center">
                <div className="size-7 rounded-xl flex items-center justify-center mx-auto mb-1.5" style={{ background: stat.bg }}>
                  <span className={`material-symbols-outlined text-[15px] ${stat.color}`}>{stat.icon}</span>
                </div>
                <p className="font-display text-[18px] md:text-[20px] font-bold text-foreground leading-none">{stat.value}</p>
                <p className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider mt-0.5">{stat.label}</p>
              </div>
            ))}
          </div>
        </motion.div>

        {/* Now Serving - Mess Meal Card */}
        {activeMeal && (() => {
          const mealGradients: Record<string, string> = {
            breakfast: 'from-amber-500/15 via-orange-500/10 to-yellow-500/5 dark:from-amber-500/10 dark:via-orange-500/5 dark:to-transparent',
            lunch: 'from-emerald-500/15 via-green-500/10 to-teal-500/5 dark:from-emerald-500/10 dark:via-green-500/5 dark:to-transparent',
            dinner: 'from-indigo-500/15 via-purple-500/10 to-violet-500/5 dark:from-indigo-500/10 dark:via-purple-500/5 dark:to-transparent',
          };
          const mealAccents: Record<string, string> = {
            breakfast: 'text-amber-600 dark:text-amber-400',
            lunch: 'text-emerald-600 dark:text-emerald-400',
            dinner: 'text-indigo-600 dark:text-indigo-400',
          };
          const mealIcons: Record<string, string> = {
            breakfast: 'bakery_dining', lunch: 'lunch_dining', dinner: 'dinner_dining',
          };
          const items = messPref === 'nonveg' && activeMeal.nonVegItems ? activeMeal.nonVegItems : activeMeal.items;
          return (
            <motion.div variants={fadeUp} className="px-5 md:px-6 pb-2">
              <button
                onClick={() => navigate('/mess-menu')}
                className={`w-full rounded-2xl bg-gradient-to-br ${mealGradients[activeMeal.type]} border border-white/20 dark:border-white/5 p-4 text-left active:scale-[0.98] transition-transform`}
                style={{ boxShadow: '0 2px 20px -4px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.4)' }}
              >
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2.5">
                    <div className="size-9 rounded-xl bg-white/40 dark:bg-white/10 flex items-center justify-center"
                      style={{ boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.6)' }}
                    >
                      <span className={`material-symbols-outlined text-[18px] ${mealAccents[activeMeal.type]}`}>{mealIcons[activeMeal.type]}</span>
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <p className="text-[13px] font-bold text-foreground">{activeMeal.label}</p>
                        <span className={`text-[8px] font-bold uppercase tracking-widest ${mealAccents[activeMeal.type]} bg-white/40 dark:bg-white/10 px-2 py-0.5 rounded-full`}>
                          Now Serving
                        </span>
                      </div>
                      <p className="text-[10px] text-muted-foreground mt-0.5">Tap to view full menu</p>
                    </div>
                  </div>
                  <span className="material-symbols-outlined text-[16px] text-muted-foreground/40">chevron_right</span>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {items.map((item, i) => (
                    <span
                      key={i}
                      className="text-[11px] text-foreground font-medium bg-white/50 dark:bg-white/8 px-2.5 py-1.5 rounded-xl border border-white/30 dark:border-white/5"
                      style={{ boxShadow: '0 1px 3px -1px rgba(0,0,0,0.04)' }}
                    >
                      {item}
                    </span>
                  ))}
                </div>
              </button>
            </motion.div>
          );
        })()}

        {/* Low Attendance Warning */}
        {atRisk.length > 0 && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-2">
            <div className="rounded-2xl p-4" style={{
              background: 'linear-gradient(135deg, rgba(244,63,94,0.09) 0%, rgba(244,63,94,0.04) 100%)',
              border: '1px solid rgba(244,63,94,0.18)'
            }}>
              <div className="flex items-center gap-2 mb-2.5">
                <span className="material-symbols-outlined text-rose-500 text-[16px]">warning</span>
                <span className="text-[10px] font-bold text-rose-600 dark:text-rose-400 uppercase tracking-widest">Low Attendance Alert</span>
              </div>
              <div className="space-y-2">
                {atRisk.map(([name, s]) => (
                  <div key={name} className="flex items-center justify-between">
                    <span className="text-[12px] text-foreground/80 truncate flex-1 mr-3">{name}</span>
                    <span className="text-[10px] font-bold text-rose-500 bg-rose-500/12 px-2.5 py-0.5 rounded-full shrink-0">{s.percentage}%</span>
                  </div>
                ))}
              </div>
            </div>
          </motion.div>
        )}

        {/* Exam Countdown */}
        {localStorage.getItem('hideExamCountdown') === 'false' && (() => {
          const examDateStr = getExamDate();
          const examDateObj = new Date(examDateStr + 'T00:00:00');
          const now = new Date();
          const diffMs = examDateObj.getTime() - now.getTime();
          const totalDays = Math.max(0, Math.ceil(diffMs / (1000 * 60 * 60 * 24)));
          const weeks = Math.floor(totalDays / 7);
          const remainingDays = totalDays % 7;
          const dateLabel = examDateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
          const urgencyColor = totalDays <= 7 ? 'text-rose-600 dark:text-rose-400' :
            totalDays <= 14 ? 'text-amber-600 dark:text-amber-400' : 'text-foreground';
          const iconBg = totalDays <= 7 ? 'rgba(244,63,94,0.1)' : totalDays <= 14 ? 'rgba(245,158,11,0.1)' : 'rgba(99,102,241,0.1)';
          return (
            <motion.div variants={fadeUp} className="px-5 md:px-6 pb-2">
              <div className="rounded-2xl liquid-glass-card p-4 flex items-center justify-between">
                <div>
                  <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-widest">Sessionals</p>
                  <p className={`font-display text-[18px] font-bold mt-0.5 ${urgencyColor}`}>
                    {totalDays > 0 ? `${totalDays} days left` : 'Started!'}
                  </p>
                  <p className="text-muted-foreground text-[10px] mt-0.5">
                    {totalDays > 0 ? `${weeks}w ${remainingDays}d remaining` : 'Good luck!'} • {dateLabel}
                  </p>
                </div>
                <div className="size-11 rounded-2xl flex items-center justify-center" style={{ background: iconBg }}>
                  <span className="material-symbols-outlined text-muted-foreground text-[22px]">event</span>
                </div>
              </div>
            </motion.div>
          );
        })()}

        {/* Today's Schedule */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pt-4 pb-2">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2">
              <h2 className="text-[13px] font-bold text-foreground">Today's Schedule</h2>
              {todayClasses.length > 0 && (
                <span className="text-[10px] font-semibold text-muted-foreground bg-secondary px-2 py-0.5 rounded-full">
                  {todayClasses.length}
                </span>
              )}
            </div>
            <button onClick={() => navigate('/schedule')} className="text-primary text-[11px] font-semibold flex items-center gap-0.5">
              Full week
              <span className="material-symbols-outlined text-[14px]">chevron_right</span>
            </button>
          </div>
        </motion.div>

        {todayClasses.length === 0 ? (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-6">
            <div className="rounded-2xl liquid-glass-card p-8 text-center">
              <span className="material-symbols-outlined text-[36px] text-muted-foreground/25 block mb-2">weekend</span>
              <p className="text-foreground text-[14px] font-semibold">No classes today</p>
              <p className="text-muted-foreground text-[12px] mt-1">Enjoy your day! 🎉</p>
            </div>
          </motion.div>
        ) : (
          <motion.div variants={fadeUp} className="px-5 md:px-6 pb-4">
            <div className="rounded-2xl liquid-glass divide-y divide-white/10 dark:divide-white/5 overflow-hidden">
              {todayClasses.map((cls, idx) => {
                const status = getClassStatus(cls.startTime);
                const typeLabel = cls.type || 'Lec';
                const typeStyle = typeColors[typeLabel] || 'text-primary/80 bg-primary/8';
                return (
                  <div
                    key={idx}
                    className={`px-4 py-3.5 flex items-center gap-3 transition-opacity ${status === 'done' ? 'opacity-40' : ''}`}
                  >
                    <div className="w-[44px] shrink-0 text-right">
                      <p className="text-[11px] font-bold text-foreground leading-none">{to12hr(cls.startTime)}</p>
                    </div>
                    <div className="shrink-0 flex items-center justify-center w-5">
                      {status === 'current' ? (
                        <span className="size-2.5 rounded-full bg-emerald-500 block animate-pulse" />
                      ) : status === 'done' ? (
                        <span className="material-symbols-outlined text-[15px] text-muted-foreground/40">check_circle</span>
                      ) : (
                        <span className="size-2 rounded-full border-2 border-border/60 block" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-[13px] font-semibold text-foreground leading-tight truncate">{cls.subject}</p>
                      <p className="text-[10px] text-muted-foreground mt-0.5">{cls.faculty || 'TBA'} · {cls.room}</p>
                    </div>
                    <span className={`text-[9px] font-bold px-2 py-0.5 rounded-md uppercase tracking-wide shrink-0 ${typeStyle}`}>
                      {typeLabel}
                    </span>
                  </div>
                );
              })}
            </div>
          </motion.div>
        )}

        {/* Quick Links */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pb-8">
          <h2 className="text-[13px] font-bold text-foreground mb-3">Quick Links</h2>
          <div className="grid grid-cols-2 gap-2">
            {[
              { path: '/schedule', icon: 'calendar_today', label: 'Schedule', sub: 'Weekly timetable', bg: 'rgba(99,102,241,0.1)', color: 'text-primary' },
              { path: '/mess-menu', icon: 'restaurant', label: 'Mess Menu', sub: "Today's meals", bg: 'rgba(16,185,129,0.1)', color: 'text-emerald-600 dark:text-emerald-400' },
              { path: '/analytics', icon: 'bar_chart', label: 'Analytics', sub: 'Attendance stats', bg: 'rgba(245,158,11,0.1)', color: 'text-amber-600 dark:text-amber-400' },
              { path: '/profile', icon: 'settings', label: 'Settings', sub: 'Preferences & edit', bg: 'rgba(244,63,94,0.08)', color: 'text-rose-500' },
            ].map(link => (
              <button
                key={link.path}
                onClick={() => navigate(link.path)}
                className="rounded-2xl liquid-glass-card p-4 text-left active:scale-[0.97] transition-transform"
              >
                <div className="size-9 rounded-xl flex items-center justify-center mb-3" style={{ background: link.bg }}>
                  <span className={`material-symbols-outlined text-[20px] ${link.color}`}>{link.icon}</span>
                </div>
                <p className="font-display font-bold text-foreground text-[13px]">{link.label}</p>
                <p className="text-[10px] text-muted-foreground mt-0.5">{link.sub}</p>
              </button>
            ))}
          </div>
        </motion.div>

      </motion.div>
    </div>
  );
};

export default Dashboard;
