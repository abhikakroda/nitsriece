import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useTheme } from '@/hooks/use-theme';
import { requestNotificationPermission, isNotificationEnabled } from '@/lib/notifications';
import { getAllSubjectStats } from '@/lib/attendance';

const fadeUp = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: [0.25, 0.46, 0.45, 0.94] as [number, number, number, number] } },
};

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.08 } },
};

const Profile: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const [notificationsEnabled, setNotificationsEnabled] = useState(false);
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

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      {/* Header */}
      <div className="glass-morphism flex items-center px-4 md:px-6 py-3.5 border-b border-border/60">
        <h2 className="text-foreground text-[18px] md:text-[20px] font-extrabold tracking-tight flex-1 text-center lg:text-left">Settings</h2>
      </div>

      <motion.div
        className="max-w-3xl mx-auto w-full px-4 md:px-6 space-y-6 pt-6 pb-6"
        variants={stagger}
        initial="hidden"
        animate="visible"
      >
        {/* Attendance Overview */}
        {statEntries.length > 0 && (
          <motion.section variants={fadeUp}>
            <h3 className="text-muted-foreground text-[10px] md:text-[11px] font-bold uppercase tracking-[0.15em] px-1 mb-2.5">Attendance Overview</h3>
            <div className="bg-card card-elevated rounded-2xl overflow-hidden divide-y divide-border/60 border border-border/70">
              {statEntries.map(([subject, stats]) => (
                <div key={subject} className="flex items-center gap-3 px-4 py-3.5">
                  <div className="flex-1 min-w-0">
                    <p className="text-foreground text-[13px] md:text-[14px] font-semibold truncate">{subject}</p>
                    <p className="text-muted-foreground text-[10px] md:text-[11px]">{stats.present}P / {stats.absent}A — {stats.total} classes</p>
                  </div>
                  <div className="flex items-center gap-2.5">
                    <div className="w-16 md:w-24 h-2 bg-secondary rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${stats.percentage >= 75 ? 'bg-gradient-to-r from-green-400 to-green-500' : 'bg-gradient-to-r from-red-400 to-red-500'}`}
                        style={{ width: `${stats.percentage}%` }}
                      />
                    </div>
                    <span className={`text-[11px] md:text-[12px] font-bold min-w-[32px] text-right ${stats.percentage >= 75 ? 'text-green-600' : 'text-red-500'}`}>
                      {stats.percentage}%
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </motion.section>
        )}

        {/* Two-column grid on desktop */}
        <div className="md:grid md:grid-cols-2 md:gap-5 space-y-5 md:space-y-0">
          {/* Notifications */}
          <motion.section variants={fadeUp}>
            <h3 className="text-muted-foreground text-[10px] md:text-[11px] font-bold uppercase tracking-[0.15em] px-1 mb-2.5">Notifications</h3>
            <div className="bg-card card-elevated rounded-2xl overflow-hidden border border-border/70">
              <div className="flex items-center gap-3 px-4 py-4 justify-between">
                <div className="flex items-center gap-3">
                  <div className="text-primary flex items-center justify-center rounded-2xl bg-primary/8 shrink-0 size-11">
                    <span className="material-symbols-outlined text-[22px]">notifications_active</span>
                  </div>
                  <div>
                    <p className="text-foreground text-[14px] font-semibold">Class Reminders</p>
                    <p className="text-muted-foreground text-[10px] md:text-[11px]">20min & 5min before class</p>
                  </div>
                </div>
                {notificationsEnabled ? (
                  <div className="flex items-center gap-1.5 bg-green-500/8 px-3 py-1.5 rounded-full border border-green-500/15">
                    <span className="material-symbols-outlined text-green-500 text-[14px]">check_circle</span>
                    <span className="text-green-600 text-[11px] font-bold">Active</span>
                  </div>
                ) : (
                  <button
                    onClick={handleEnableNotifications}
                    className="bg-primary text-primary-foreground text-[11px] font-bold px-4 py-2 rounded-xl active:scale-95 transition-all shadow-md shadow-primary/15 hover:shadow-lg hover:shadow-primary/20"
                  >
                    Enable
                  </button>
                )}
              </div>
            </div>
          </motion.section>

          {/* Appearance */}
          <motion.section variants={fadeUp}>
            <h3 className="text-muted-foreground text-[10px] md:text-[11px] font-bold uppercase tracking-[0.15em] px-1 mb-2.5">Appearance</h3>
            <div className="bg-card card-elevated rounded-2xl overflow-hidden border border-border/70">
              <div className="flex items-center gap-3 px-4 py-4 justify-between cursor-pointer active:bg-accent/50 transition-colors" onClick={toggleTheme}>
                <div className="flex items-center gap-3">
                  <div className="text-muted-foreground flex items-center justify-center rounded-2xl bg-secondary shrink-0 size-11">
                    <span className="material-symbols-outlined text-[22px]">{theme === 'dark' ? 'light_mode' : 'dark_mode'}</span>
                  </div>
                  <div>
                    <p className="text-foreground text-[14px] font-semibold">Dark Mode</p>
                    <p className="text-muted-foreground text-[11px]">{theme === 'dark' ? 'Currently dark' : 'Currently light'}</p>
                  </div>
                </div>
                <div className={`relative w-12 h-[28px] rounded-full p-0.5 cursor-pointer transition-colors duration-300 ${theme === 'dark' ? 'bg-primary' : 'bg-border'}`}>
                  <motion.div
                    className="h-[24px] w-[24px] rounded-full bg-white shadow-sm"
                    animate={{ x: theme === 'dark' ? 19 : 0 }}
                    transition={{ type: 'spring', stiffness: 500, damping: 30 }}
                  />
                </div>
              </div>
            </div>
          </motion.section>
        </div>
      </motion.div>
    </div>
  );
};

export default Profile;
