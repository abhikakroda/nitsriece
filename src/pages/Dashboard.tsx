import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { requestNotificationPermission } from '@/lib/notifications';
import { getTodayClasses, getClassStatus, dayNames, getTodayDay } from '@/lib/timetable';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.08 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: [0.25, 0.46, 0.45, 0.94] as [number, number, number, number] } },
};

const Dashboard: React.FC = () => {
  const navigate = useNavigate();

  useEffect(() => {
    requestNotificationPermission();
  }, []);

  const todayClasses = getTodayClasses();
  const todayDay = getTodayDay();

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6 relative overflow-x-hidden">
      {/* Liquid Blobs */}
      <div className="liquid-blob top-[-8%] left-[-8%] w-80 h-80 bg-primary/15 dark:bg-primary/10 rounded-full mix-blend-multiply dark:mix-blend-screen filter blur-[80px] animate-blob" />
      <div className="liquid-blob top-[25%] right-[-8%] w-72 h-72 bg-blue-400/15 dark:bg-blue-500/10 rounded-full mix-blend-multiply dark:mix-blend-screen filter blur-[80px] animate-blob animation-delay-2000" />
      <div className="liquid-blob bottom-[-5%] left-[25%] w-96 h-96 bg-violet-300/12 dark:bg-violet-500/8 rounded-full mix-blend-multiply dark:mix-blend-screen filter blur-[80px] animate-blob animation-delay-4000" />


      <motion.div className="max-w-3xl mx-auto w-full" variants={stagger} initial="hidden" animate="visible">
        <motion.div variants={fadeUp} className="px-4 md:px-6 pt-5 pb-2 relative z-10">
          <p className="text-muted-foreground text-[11px] md:text-[12px] font-bold uppercase tracking-[0.15em]">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
          </p>
          <h1 className="text-[26px] md:text-[32px] font-black text-foreground tracking-tight mt-0.5 leading-tight">
            Good {new Date().getHours() < 12 ? 'Morning' : new Date().getHours() < 17 ? 'Afternoon' : 'Evening'} 👋
          </h1>
        </motion.div>

        {/* Hero Notice Card */}
        {localStorage.getItem('hideExamCountdown') !== 'true' && (
          <motion.div variants={fadeUp} className="px-4 md:px-6 pb-5 relative z-10">
            {(() => {
              const examDate = new Date('2025-04-01T00:00:00');
              const now = new Date();
              const diffMs = examDate.getTime() - now.getTime();
              const totalDays = Math.max(0, Math.ceil(diffMs / (1000 * 60 * 60 * 24)));
              const weeks = Math.floor(totalDays / 7);
              const days = totalDays % 7;
              return (
                <div className="bg-gradient-to-br from-primary via-blue-600 to-violet-600 rounded-3xl p-5 md:p-6 text-white shadow-xl shadow-primary/15 overflow-hidden relative cursor-pointer active:scale-[0.98] transition-transform group">
                  <div className="absolute inset-0 animate-shimmer" />
                  <div className="relative z-10 flex justify-between items-start mb-4">
                    <div>
                      <p className="text-white/70 text-[10px] md:text-[11px] font-bold uppercase tracking-[0.2em] mb-1.5">Sessional Exams</p>
                      <h3 className="text-[22px] md:text-[24px] font-extrabold leading-tight">April 1, 2025</h3>
                    </div>
                    <div className="bg-white/15 p-2.5 rounded-2xl backdrop-blur-md border border-white/10 animate-float">
                      <span className="material-symbols-outlined text-[22px]">timer</span>
                    </div>
                  </div>
                  <div className="relative z-10 flex gap-2.5 mb-3">
                    {[
                      { value: totalDays, label: 'Days' },
                      { value: weeks, label: 'Weeks' },
                      { value: days, label: 'Days Left' },
                    ].map((item, i) => (
                      <div key={i} className="flex-1 bg-white/12 rounded-2xl p-3 text-center border border-white/8 backdrop-blur-sm">
                        <p className="text-[22px] md:text-[26px] font-black leading-none">{item.value}</p>
                        <p className="text-[9px] md:text-[10px] font-semibold text-white/60 uppercase tracking-wider mt-1">{item.label}</p>
                      </div>
                    ))}
                  </div>
                  <div className="relative z-10">
                    <p className="text-[11px] text-white/60 font-medium ml-1">Date sheet released — check resources for PDF.</p>
                  </div>
                  <div className="absolute right-[-30px] bottom-[-50px] w-56 h-56 bg-white/8 rounded-full blur-3xl" />
                  <div className="absolute left-[-20px] top-[-30px] w-40 h-40 bg-violet-400/15 rounded-full blur-3xl" />
                </div>
              );
            })()}
          </motion.div>
        )}

        {/* Today's Timetable + Resources grid */}
        <div className="md:grid md:grid-cols-2 md:gap-5 px-4 md:px-6">
          {/* Today's Timetable */}
          <motion.div variants={fadeUp} className="space-y-3 mb-5 md:mb-0">
            <div className="flex items-center justify-between mb-1">
              <h2 className="text-[15px] md:text-[17px] font-extrabold text-foreground tracking-tight">Today's Classes</h2>
              <button onClick={() => navigate('/schedule')} className="text-primary text-[11px] md:text-[12px] font-bold active:opacity-70 hover:underline underline-offset-2">
                Full Week →
              </button>
            </div>

            {todayClasses.length === 0 ? (
              <div className="bg-card card-elevated rounded-3xl p-8 border border-border text-center">
                <div className="w-14 h-14 rounded-2xl bg-primary/8 flex items-center justify-center mx-auto mb-3">
                  <span className="material-symbols-outlined text-[28px] text-primary/40">event_available</span>
                </div>
                <p className="text-foreground font-bold text-[15px]">No classes today!</p>
                <p className="text-muted-foreground text-[12px] mt-1">Enjoy your {dayNames[todayDay]} 🎉</p>
              </div>
            ) : (
              todayClasses.map((cls, idx) => {
                const status = getClassStatus(cls.startTime);
                return (
                  <motion.div
                    key={idx}
                    variants={fadeUp}
                    className={`bg-card card-elevated rounded-2xl p-4 border relative overflow-hidden transition-all duration-300 ${
                      status === 'current'
                        ? 'border-primary/25 ring-1 ring-primary/10 shadow-lg shadow-primary/5'
                        : status === 'done'
                        ? 'border-border opacity-45'
                        : 'border-border/80'
                    }`}
                  >
                    {status === 'current' && (
                      <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-primary via-blue-500 to-violet-500" />
                    )}
                    <div className="flex items-start justify-between relative z-10">
                      <div className="flex gap-3">
                        <div className={`size-11 rounded-2xl ${cls.iconBg} ${cls.iconColor} flex items-center justify-center shrink-0 ${status === 'current' ? 'shadow-sm' : ''}`}>
                          <span className="material-symbols-outlined text-[20px]">{cls.icon}</span>
                        </div>
                        <div>
                          <h3 className="font-bold text-foreground text-[14px] leading-tight">{cls.subject}</h3>
                          <div className="flex items-center gap-3 mt-1.5">
                            <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                              <span className="material-symbols-outlined text-[12px]">schedule</span> {cls.time}
                            </p>
                            <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                              <span className="material-symbols-outlined text-[12px]">location_on</span> {cls.room}
                            </p>
                          </div>
                        </div>
                      </div>
                      <div className="flex flex-col items-end gap-1">
                        {cls.type && (
                          <span className="text-[9px] font-bold py-0.5 px-2 rounded-full bg-purple-500/10 text-purple-600 dark:text-purple-400 uppercase">{cls.type}</span>
                        )}
                        <span className={`text-[9px] font-bold py-1 px-2.5 rounded-full uppercase tracking-wide ${
                          status === 'current' ? 'bg-primary/10 text-primary animate-pulse'
                          : status === 'done' ? 'bg-secondary text-muted-foreground'
                          : 'bg-secondary text-muted-foreground'
                        }`}>
                          {status === 'current' ? '● Live' : status === 'done' ? 'Done' : 'Next'}
                        </span>
                      </div>
                    </div>
                  </motion.div>
                );
              })
            )}
          </motion.div>

          {/* Resources + Quick Access */}
          <motion.div variants={fadeUp}>
            <div className="mb-5">
              <div className="flex items-center justify-between mb-2.5">
                <h3 className="text-[14px] md:text-[15px] font-extrabold text-foreground tracking-tight">Resources</h3>
                <button className="text-primary text-[11px] font-bold hover:underline underline-offset-2">View All</button>
              </div>
              <div className="bg-card card-elevated rounded-2xl border border-border/80 divide-y divide-border/60 overflow-hidden">
                {[
                  { icon: 'picture_as_pdf', color: 'text-red-500 bg-red-500/10', title: 'VLSI Unit 2 Notes', sub: 'Uploaded yesterday' },
                  { icon: 'description', color: 'text-blue-500 bg-blue-500/10', title: 'DSP Assignment 3', sub: 'Due in 2 days' },
                ].map((r, idx) => (
                  <div key={idx} className="flex items-center gap-3 p-3.5 active:bg-accent/50 hover:bg-accent/30 transition-colors cursor-pointer">
                    <div className={`size-10 rounded-xl flex items-center justify-center shrink-0 ${r.color}`}>
                      <span className="material-symbols-outlined text-[18px]">{r.icon}</span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <h4 className="text-[13px] font-bold text-foreground">{r.title}</h4>
                      <p className="text-[11px] text-muted-foreground">{r.sub}</p>
                    </div>
                    <span className="material-symbols-outlined text-muted-foreground/50 text-[18px]">chevron_right</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Quick Access */}
            <div className="grid grid-cols-2 gap-3 pb-6">
              <div
                onClick={() => navigate('/schedule')}
                className="bg-gradient-to-br from-slate-900 to-slate-800 text-white p-4 rounded-2xl flex flex-col justify-between cursor-pointer h-[130px] relative overflow-hidden group active:scale-[0.96] transition-all duration-200 shadow-lg shadow-slate-900/10"
              >
                <div className="absolute inset-0 bg-gradient-to-br from-primary/15 to-violet-500/15 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                <div className="size-10 rounded-xl bg-white/10 flex items-center justify-center relative z-10 backdrop-blur-sm">
                  <span className="material-symbols-outlined text-[20px]">calendar_today</span>
                </div>
                <div className="relative z-10">
                  <h3 className="font-bold text-white text-[14px] leading-tight">Schedule</h3>
                  <p className="text-[10px] text-white/50 mt-0.5 font-medium">Weekly Timetable</p>
                </div>
              </div>
              <div
                onClick={() => navigate('/gpa')}
                className="bg-gradient-to-br from-primary/8 to-violet-500/8 dark:from-primary/12 dark:to-violet-500/12 p-4 rounded-2xl flex flex-col justify-between cursor-pointer border border-primary/10 h-[130px] active:scale-[0.96] transition-all duration-200"
              >
                <div className="size-10 rounded-xl bg-primary text-white flex items-center justify-center shadow-md shadow-primary/20">
                  <span className="material-symbols-outlined text-[20px]">calculate</span>
                </div>
                <div>
                  <h3 className="font-bold text-foreground text-[14px] leading-tight">GPA Calc</h3>
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
