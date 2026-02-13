import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useTheme } from '@/hooks/use-theme';
import { requestNotificationPermission, isNotificationEnabled } from '@/lib/notifications';

const fadeUp = {
  hidden: { opacity: 0, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.06 } },
};

const Profile: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const [notificationsEnabled, setNotificationsEnabled] = useState(false);
  const [hideExamCountdown, setHideExamCountdown] = useState(() => localStorage.getItem('hideExamCountdown') !== 'false');

  useEffect(() => {
    setNotificationsEnabled(isNotificationEnabled());
  }, []);

  const handleEnableNotifications = async () => {
    await requestNotificationPermission();
    setNotificationsEnabled(isNotificationEnabled());
  };

  const handleToggleExamCountdown = () => {
    const current = localStorage.getItem('hideExamCountdown') === 'true';
    localStorage.setItem('hideExamCountdown', String(!current));
    setHideExamCountdown(!current);
  };

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      <div className="liquid-glass-nav flex items-center px-5 py-3.5 border-b border-white/20 dark:border-white/5">
        <h2 className="font-display text-foreground text-[18px] md:text-[20px] font-bold tracking-tight flex-1 text-center lg:text-left">Settings</h2>
      </div>

      <motion.div className="max-w-3xl mx-auto w-full px-5 md:px-6 space-y-5 pt-6 pb-6" variants={stagger} initial="hidden" animate="visible">
        <motion.section variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest px-1 mb-2">Preferences</h3>
          <div className="liquid-glass rounded-2xl divide-y divide-white/15 dark:divide-white/5">
            {/* Dark Mode */}
            <button className="flex items-center w-full gap-3 px-4 py-3.5 justify-between" onClick={toggleTheme}>
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-muted-foreground text-[20px]">{theme === 'dark' ? 'light_mode' : 'dark_mode'}</span>
                <div className="text-left">
                  <p className="text-foreground text-[13px] font-medium">Dark Mode</p>
                  <p className="text-muted-foreground text-[10px]">{theme === 'dark' ? 'On' : 'Off'}</p>
                </div>
              </div>
              <div className={`w-[38px] h-[22px] rounded-full p-0.5 transition-colors ${theme === 'dark' ? 'bg-foreground' : 'bg-white/30 dark:bg-white/10'}`}>
                <motion.div className="h-[18px] w-[18px] rounded-full bg-background shadow-sm" animate={{ x: theme === 'dark' ? 16 : 0 }} transition={{ type: 'spring', stiffness: 500, damping: 35 }} />
              </div>
            </button>

            {/* Exam Countdown */}
            <button className="flex items-center w-full gap-3 px-4 py-3.5 justify-between" onClick={handleToggleExamCountdown}>
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-muted-foreground text-[20px]">timer</span>
                <div className="text-left">
                  <p className="text-foreground text-[13px] font-medium">Exam Countdown</p>
                  <p className="text-muted-foreground text-[10px]">Show on dashboard</p>
                </div>
              </div>
              <div className={`w-[38px] h-[22px] rounded-full p-0.5 transition-colors ${!hideExamCountdown ? 'bg-foreground' : 'bg-white/30 dark:bg-white/10'}`}>
                <motion.div className="h-[18px] w-[18px] rounded-full bg-background shadow-sm" animate={{ x: !hideExamCountdown ? 16 : 0 }} transition={{ type: 'spring', stiffness: 500, damping: 35 }} />
              </div>
            </button>

            {/* Notifications */}
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-muted-foreground text-[20px]">notifications</span>
                <div>
                  <p className="text-foreground text-[13px] font-medium">Reminders</p>
                  <p className="text-muted-foreground text-[10px]">Before each class</p>
                </div>
              </div>
              {notificationsEnabled ? (
                <span className="text-[10px] font-medium text-muted-foreground">Active</span>
              ) : (
                <button onClick={handleEnableNotifications} className="text-[11px] font-medium text-primary">
                  Enable
                </button>
              )}
            </div>
          </div>
        </motion.section>

        <motion.section variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest px-1 mb-2">About</h3>
          <div className="liquid-glass-card rounded-2xl px-4 py-4 flex items-center gap-3.5">
            <img src="/pwa-192x192.png" alt="Time Table" className="size-10 rounded-xl" />
            <div>
              <p className="text-foreground text-[13px] font-medium">Time Table</p>
              <p className="text-muted-foreground text-[10px] mt-0.5">ECE Section B • NIT Srinagar</p>
            </div>
          </div>
        </motion.section>
      </motion.div>
    </div>
  );
};

export default Profile;
