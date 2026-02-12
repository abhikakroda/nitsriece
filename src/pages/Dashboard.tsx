import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { requestNotificationPermission } from '@/lib/notifications';
import { getTodayClasses, getClassStatus, dayNames, getTodayDay } from '@/lib/timetable';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.07 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 20 },
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
  const greeting = hour < 12 ? 'Good Morning' : hour < 17 ? 'Good Afternoon' : 'Good Evening';

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6 relative overflow-x-hidden">
      {/* Ambient background */}
      <div className="liquid-blob top-[-10%] left-[-12%] w-[320px] h-[320px] bg-primary/12 dark:bg-primary/8 rounded-full filter blur-[100px] animate-blob will-change-transform pointer-events-none" />
      <div className="liquid-blob top-[20%] right-[-10%] w-[280px] h-[280px] bg-blue-400/10 dark:bg-blue-500/6 rounded-full filter blur-[100px] animate-blob animation-delay-2000 will-change-transform pointer-events-none" />
      <div className="liquid-blob bottom-[-8%] left-[20%] w-[360px] h-[360px] bg-violet-300/8 dark:bg-violet-500/5 rounded-full filter blur-[100px] animate-blob animation-delay-4000 will-change-transform pointer-events-none" />

      <motion.div className="max-w-3xl mx-auto w-full relative z-10" variants={stagger} initial="hidden" animate="visible">
        {/* Greeting */}
        <motion.div variants={fadeUp} className="px-5 md:px-6 pt-6 pb-1">
          <p className="text-muted-foreground text-[11px] md:text-[12px] font-semibold uppercase tracking-[0.18em]">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
          </p>
          <h1 className="font-display text-[28px] md:text-[34px] font-bold text-foreground tracking-tight mt-1 leading-[1.1]">
            {greeting} 👋
          </h1>
        </motion.div>

        {/* Exam Countdown */}
        {localStorage.getItem('hideExamCountdown') !== 'true' && (
          <motion.div variants={fadeUp} className="px-5 md:px-6 py-4 relative z-10">
            {(() => {
              const examDate = new Date('2025-04-01T00:00:00');
              const now = new Date();
              const diffMs = examDate.getTime() - now.getTime();
              const totalDays = Math.max(0, Math.ceil(diffMs / (1000 * 60 * 60 * 24)));
              const weeks = Math.floor(totalDays / 7);
              const remainingDays = totalDays % 7;
              return (
                <div className="bg-gradient-to-br from-primary via-blue-600 to-violet-600 animate-gradient rounded-[20px] p-5 md:p-6 text-white shadow-2xl shadow-primary/20 overflow-hidden relative noise-overlay">
                  <div className="absolute inset-0 animate-shimmer" />
                  <div className="relative z-10 flex justify-between items-start mb-5">
                    <div>
                      <p className="text-white/60 text-[10px] md:text-[11px] font-bold uppercase tracking-[0.2em] mb-1">Sessional Exams</p>
                      <h3 className="font-display text-[24px] md:text-[28px] font-bold leading-none tracking-tight">April 1, 2025</h3>
                    </div>
                    <div className="bg-white/10 p-2.5 rounded-2xl backdrop-blur-sm border border-white/10 animate-float">
                      <span className="material-symbols-outlined text-[24px]">timer</span>
                    </div>
                  </div>
                  <div className="relative z-10 grid grid-cols-3 gap-2 mb-3.5">
                    {[
                      { value: totalDays, label: 'Total Days' },
                      { value: weeks, label: 'Weeks' },
                      { value: remainingDays, label: 'Days' },
                    ].map((item, i) => (
                      <div key={i} className="bg-white/10 rounded-2xl p-3 md:p-3.5 text-center border border-white/5">
                        <p className="font-display text-[26px] md:text-[30px] font-bold leading-none">{item.value}</p>
                        <p className="text-[9px] md:text-[10px] font-medium text-white/50 uppercase tracking-widest mt-1.5">{item.label}</p>
                      </div>
                    ))}
                  </div>
                  <p className="relative z-10 text-[11px] text-white/45 font-medium">Date sheet released — check resources for PDF.</p>
                  <div className="absolute right-[-40px] bottom-[-60px] w-64 h-64 bg-white/5 rounded-full blur-3xl" />
                  <div className="absolute left-[-20px] top-[-40px] w-44 h-44 bg-violet-400/10 rounded-full blur-3xl" />
                </div>
              );
            })()}
          </motion.div>
        )}

        {/* Today's Timetable + Resources */}
        <div className="md:grid md:grid-cols-2 md:gap-5 px-5 md:px-6">
          {/* Today's Classes */}
          <motion.div variants={fadeUp} className="mb-5 md:mb-0">
            <div className="flex items-center justify-between mb-3">
              <h2 className="font-display text-[16px] md:text-[18px] font-bold text-foreground">Today's Classes</h2>
              <button onClick={() => navigate('/schedule')} className="text-primary text-[11px] font-bold hover:underline underline-offset-2 active:opacity-70 flex items-center gap-0.5">
                Full Week
                <span className="material-symbols-outlined text-[14px]">arrow_forward</span>
              </button>
            </div>

            {todayClasses.length === 0 ? (
              <div className="bg-card card-elevated rounded-[20px] p-10 border border-border/60 text-center">
                <div className="w-16 h-16 rounded-[18px] bg-primary/6 flex items-center justify-center mx-auto mb-4">
                  <span className="material-symbols-outlined text-[32px] text-primary/30">celebration</span>
                </div>
                <p className="font-display text-foreground font-bold text-[17px]">No classes today!</p>
                <p className="text-muted-foreground text-[13px] mt-1.5">Enjoy your {dayNames[todayDay]} 🎉</p>
              </div>
            ) : (
              <div className="space-y-2.5">
                {todayClasses.map((cls, idx) => {
                  const status = getClassStatus(cls.startTime);
                  return (
                    <motion.div
                      key={idx}
                      variants={fadeUp}
                      className={`bg-card card-elevated rounded-[16px] p-3.5 md:p-4 border relative overflow-hidden transition-all duration-300 ${
                        status === 'current'
                          ? 'border-primary/20 ring-1 ring-primary/8 shadow-lg shadow-primary/8'
                          : status === 'done'
                          ? 'border-border/50 opacity-40'
                          : 'border-border/60'
                      }`}
                    >
                      {status === 'current' && (
                        <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-primary via-blue-500 to-violet-500 animate-gradient" />
                      )}
                      <div className="flex items-center gap-3 relative z-10">
                        <div className={`size-11 md:size-12 rounded-[14px] ${cls.iconBg} ${cls.iconColor} flex items-center justify-center shrink-0 ${status === 'current' ? 'shadow-sm' : ''}`}>
                          <span className="material-symbols-outlined text-[20px] md:text-[22px]">{cls.icon}</span>
                        </div>
                        <div className="flex-1 min-w-0">
                          <h3 className="font-bold text-foreground text-[13px] md:text-[14px] leading-tight truncate">{cls.subject}</h3>
                          <div className="flex items-center gap-2.5 mt-1">
                            <p className="text-[10px] md:text-[11px] text-muted-foreground flex items-center gap-0.5">
                              <span className="material-symbols-outlined text-[12px]">schedule</span> {cls.time}
                            </p>
                            <p className="text-[10px] md:text-[11px] text-muted-foreground flex items-center gap-0.5">
                              <span className="material-symbols-outlined text-[12px]">location_on</span> {cls.room}
                            </p>
                          </div>
                        </div>
                        <div className="flex flex-col items-end gap-1 shrink-0">
                          {cls.type && (
                            <span className="text-[9px] font-bold py-0.5 px-2 rounded-full bg-purple-500/10 text-purple-600 dark:text-purple-400 uppercase">{cls.type}</span>
                          )}
                          <span className={`text-[9px] font-bold py-0.5 px-2 rounded-full uppercase tracking-wider ${
                            status === 'current' ? 'bg-primary/10 text-primary' 
                            : status === 'done' ? 'bg-secondary text-muted-foreground'
                            : 'bg-secondary text-muted-foreground'
                          }`}>
                            {status === 'current' ? '● Live' : status === 'done' ? 'Done' : 'Up Next'}
                          </span>
                        </div>
                      </div>
                    </motion.div>
                  );
                })}
              </div>
            )}
          </motion.div>

          {/* Resources + Quick Access */}
          <motion.div variants={fadeUp}>
            <div className="mb-5">
              <div className="flex items-center justify-between mb-3">
                <h3 className="font-display text-[15px] md:text-[17px] font-bold text-foreground">Resources</h3>
                <button className="text-primary text-[11px] font-bold hover:underline underline-offset-2">View All</button>
              </div>
              <div className="bg-card card-elevated rounded-[16px] border border-border/60 divide-y divide-border/50 overflow-hidden">
                {[
                  { icon: 'picture_as_pdf', color: 'text-red-500 bg-red-500/8', title: 'VLSI Unit 2 Notes', sub: 'Uploaded yesterday' },
                  { icon: 'description', color: 'text-blue-500 bg-blue-500/8', title: 'DSP Assignment 3', sub: 'Due in 2 days' },
                ].map((r, idx) => (
                  <div key={idx} className="flex items-center gap-3 p-3.5 active:bg-accent/50 hover:bg-accent/30 transition-colors cursor-pointer group">
                    <div className={`size-10 rounded-[12px] flex items-center justify-center shrink-0 ${r.color}`}>
                      <span className="material-symbols-outlined text-[18px]">{r.icon}</span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <h4 className="text-[13px] font-semibold text-foreground">{r.title}</h4>
                      <p className="text-[11px] text-muted-foreground">{r.sub}</p>
                    </div>
                    <span className="material-symbols-outlined text-muted-foreground/40 text-[16px] group-hover:translate-x-0.5 transition-transform">chevron_right</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Quick Access */}
            <div className="grid grid-cols-2 gap-3 pb-6">
              <div
                onClick={() => navigate('/schedule')}
                className="relative bg-foreground dark:bg-card p-4 rounded-[16px] flex flex-col justify-between cursor-pointer h-[120px] overflow-hidden group active:scale-[0.97] transition-all duration-200 shadow-lg shadow-foreground/5 dark:shadow-none border border-transparent dark:border-border/60"
              >
                <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-violet-500/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                <div className="size-10 rounded-[12px] bg-white/10 dark:bg-primary/10 flex items-center justify-center relative z-10">
                  <span className="material-symbols-outlined text-white dark:text-primary text-[20px]">calendar_today</span>
                </div>
                <div className="relative z-10">
                  <h3 className="font-display font-bold text-white dark:text-foreground text-[14px] leading-tight">Schedule</h3>
                  <p className="text-[10px] text-white/45 dark:text-muted-foreground mt-0.5 font-medium">Weekly Timetable</p>
                </div>
              </div>
              <div
                onClick={() => navigate('/gpa')}
                className="relative bg-gradient-to-br from-primary/6 to-violet-500/6 dark:from-primary/10 dark:to-violet-500/10 p-4 rounded-[16px] flex flex-col justify-between cursor-pointer border border-primary/8 h-[120px] active:scale-[0.97] transition-all duration-200 group"
              >
                <div className="size-10 rounded-[12px] bg-primary text-white flex items-center justify-center shadow-md shadow-primary/20 group-hover:shadow-lg group-hover:shadow-primary/25 transition-shadow">
                  <span className="material-symbols-outlined text-[20px]">calculate</span>
                </div>
                <div>
                  <h3 className="font-display font-bold text-foreground text-[14px] leading-tight">GPA Calc</h3>
                  <p className="text-[10px] text-muted-foreground mt-0.5 font-medium">Estimate SGPA</p>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </motion.div>
    </div>
  );
};

export default Dashboard;
