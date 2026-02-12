import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useTheme } from '@/hooks/use-theme';
import { requestNotificationPermission, isNotificationEnabled } from '@/lib/notifications';
import { getAllSubjectStats } from '@/lib/attendance';

const fadeUp = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.07 } },
};

const Profile: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const [notificationsEnabled, setNotificationsEnabled] = useState(false);
  const [hideExamCountdown, setHideExamCountdown] = useState(() => localStorage.getItem('hideExamCountdown') === 'true');
  const [attendanceStats, setAttendanceStats] = useState<Record<string, { present: number; absent: number; total: number; percentage: number }>>({});

  useEffect(() => {
    setNotificationsEnabled(isNotificationEnabled());
    setAttendanceStats(getAllSubjectStats());
  }, []);

  const handleEnableNotifications = async () => {
    await requestNotificationPermission();
    setNotificationsEnabled(isNotificationEnabled());
  };

  const statEntries = Object.entries(attendanceStats);

  const ToggleSwitch = ({ active, onClick }: { active: boolean; onClick: () => void }) => (
    <div
      onClick={onClick}
      className={`relative w-[44px] h-[26px] rounded-full p-0.5 cursor-pointer transition-colors duration-300 ${active ? 'bg-primary' : 'bg-border'}`}
    >
      <motion.div
        className="h-[22px] w-[22px] rounded-full bg-white shadow-sm"
        animate={{ x: active ? 18 : 0 }}
        transition={{ type: 'spring', stiffness: 500, damping: 35 }}
      />
    </div>
  );

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      {/* Header */}
      <div className="glass-morphism flex items-center px-5 md:px-6 py-3.5 border-b border-border/40">
        <h2 className="font-display text-foreground text-[18px] md:text-[20px] font-bold tracking-tight flex-1 text-center lg:text-left">Settings</h2>
      </div>

      <motion.div
        className="max-w-3xl mx-auto w-full px-5 md:px-6 space-y-5 pt-6 pb-6"
        variants={stagger}
        initial="hidden"
        animate="visible"
      >
        {/* Attendance Overview */}
        {statEntries.length > 0 && (
          <motion.section variants={fadeUp}>
            <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Attendance Overview</h3>
            <div className="bg-card card-elevated rounded-[16px] overflow-hidden divide-y divide-border/40 border border-border/50">
              {statEntries.map(([subject, stats]) => (
                <div key={subject} className="flex items-center gap-3 px-4 py-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-foreground text-[13px] font-semibold truncate">{subject}</p>
                    <p className="text-muted-foreground text-[10px]">{stats.present}P / {stats.absent}A — {stats.total} classes</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-16 md:w-20 h-1.5 bg-secondary rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${stats.percentage >= 75 ? 'bg-gradient-to-r from-green-400 to-green-500' : 'bg-gradient-to-r from-red-400 to-red-500'}`}
                        style={{ width: `${stats.percentage}%` }}
                      />
                    </div>
                    <span className={`text-[11px] font-bold min-w-[28px] text-right ${stats.percentage >= 75 ? 'text-green-600 dark:text-green-400' : 'text-red-500 dark:text-red-400'}`}>
                      {stats.percentage}%
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </motion.section>
        )}

        {/* Preferences */}
        <motion.section variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Preferences</h3>
          <div className="bg-card card-elevated rounded-[16px] overflow-hidden border border-border/50 divide-y divide-border/40">
            {/* Dark Mode */}
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between cursor-pointer active:bg-accent/30 transition-colors" onClick={toggleTheme}>
              <div className="flex items-center gap-3">
                <div className="text-muted-foreground flex items-center justify-center rounded-[12px] bg-secondary/60 shrink-0 size-10">
                  <span className="material-symbols-outlined text-[20px]">{theme === 'dark' ? 'light_mode' : 'dark_mode'}</span>
                </div>
                <div>
                  <p className="text-foreground text-[13px] font-semibold">Dark Mode</p>
                  <p className="text-muted-foreground text-[10px]">{theme === 'dark' ? 'Currently dark' : 'Currently light'}</p>
                </div>
              </div>
              <ToggleSwitch active={theme === 'dark'} onClick={toggleTheme} />
            </div>

            {/* Exam Countdown */}
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between cursor-pointer active:bg-accent/30 transition-colors" onClick={() => {
              const current = localStorage.getItem('hideExamCountdown') === 'true';
              localStorage.setItem('hideExamCountdown', String(!current));
              setHideExamCountdown(!current);
            }}>
              <div className="flex items-center gap-3">
                <div className="text-muted-foreground flex items-center justify-center rounded-[12px] bg-secondary/60 shrink-0 size-10">
                  <span className="material-symbols-outlined text-[20px]">timer</span>
                </div>
                <div>
                  <p className="text-foreground text-[13px] font-semibold">Exam Countdown</p>
                  <p className="text-muted-foreground text-[10px]">Show on dashboard</p>
                </div>
              </div>
              <ToggleSwitch active={!hideExamCountdown} onClick={() => {}} />
            </div>

            {/* Notifications */}
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between">
              <div className="flex items-center gap-3">
                <div className="text-primary flex items-center justify-center rounded-[12px] bg-primary/6 shrink-0 size-10">
                  <span className="material-symbols-outlined text-[20px]">notifications_active</span>
                </div>
                <div>
                  <p className="text-foreground text-[13px] font-semibold">Class Reminders</p>
                  <p className="text-muted-foreground text-[10px]">20min & 5min before class</p>
                </div>
              </div>
              {notificationsEnabled ? (
                <div className="flex items-center gap-1 bg-green-500/6 px-2.5 py-1 rounded-full border border-green-500/10">
                  <span className="material-symbols-outlined text-green-500 text-[13px]">check_circle</span>
                  <span className="text-green-600 text-[10px] font-bold">Active</span>
                </div>
              ) : (
                <button
                  onClick={handleEnableNotifications}
                  className="bg-primary text-primary-foreground text-[11px] font-bold px-3.5 py-1.5 rounded-[10px] active:scale-95 transition-all shadow-sm shadow-primary/15"
                >
                  Enable
                </button>
              )}
            </div>
          </div>
        </motion.section>

        {/* About */}
        <motion.section variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">About</h3>
          <div className="bg-card card-elevated rounded-[16px] overflow-hidden border border-border/50 px-4 py-4">
            <p className="text-foreground text-[13px] font-semibold">Campus Companion</p>
            <p className="text-muted-foreground text-[11px] mt-0.5">ECE Section B • 6th Semester</p>
            <p className="text-muted-foreground/50 text-[10px] mt-2">NIT Srinagar • v1.0</p>
          </div>
        </motion.section>
      </motion.div>
    </div>
  );
};

export default Profile;
