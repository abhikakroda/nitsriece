import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from '@/hooks/use-theme';
import { requestNotificationPermission, isNotificationEnabled } from '@/lib/notifications';
import { getTimetableData, saveTimetableData, resetTimetableData, getExamDate, setExamDate as saveExamDate, days, type Day, type ClassSlot } from '@/lib/timetable';

const fadeUp = {
  hidden: { opacity: 0, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.06 } },
};

const dayLabels: Record<Day, string> = { Mon: 'Monday', Tue: 'Tuesday', Wed: 'Wednesday', Thu: 'Thursday', Fri: 'Friday' };

// ─── Timetable Editor ───
const TimetableEditor: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const [data, setData] = useState<Record<Day, ClassSlot[]>>(getTimetableData);
  const [activeDay, setActiveDay] = useState<Day>('Mon');
  const [editing, setEditing] = useState<{ day: Day; idx: number } | null>(null);

  const classes = data[activeDay];

  const updateSlot = (idx: number, patch: Partial<ClassSlot>) => {
    setData(prev => {
      const copy = { ...prev };
      copy[activeDay] = [...copy[activeDay]];
      copy[activeDay][idx] = { ...copy[activeDay][idx], ...patch };
      return copy;
    });
  };

  const addSlot = () => {
    setData(prev => {
      const copy = { ...prev };
      copy[activeDay] = [...copy[activeDay], {
        subject: 'New Subject',
        time: '09:00 – 09:50',
        startTime: '09:00',
        room: 'TBA',
        icon: 'school',
        iconBg: '',
        iconColor: '',
        faculty: 'TBA',
      }];
      return copy;
    });
    setEditing({ day: activeDay, idx: classes.length });
  };

  const removeSlot = (idx: number) => {
    setData(prev => {
      const copy = { ...prev };
      copy[activeDay] = copy[activeDay].filter((_, i) => i !== idx);
      return copy;
    });
    setEditing(null);
  };

  const handleSave = () => {
    saveTimetableData(data);
    onClose();
  };

  const handleReset = () => {
    resetTimetableData();
    setData(getTimetableData());
    setEditing(null);
  };

  return (
    <div className="space-y-4">
      {/* Day tabs */}
      <div className="flex gap-1">
        {days.map(d => (
          <button
            key={d}
            onClick={() => { setActiveDay(d); setEditing(null); }}
            className={`flex-1 py-2 rounded-xl text-[11px] font-bold transition-colors ${
              activeDay === d ? 'bg-foreground text-background' : 'bg-white/20 dark:bg-white/5 text-muted-foreground'
            }`}
          >
            {d}
          </button>
        ))}
      </div>

      <p className="text-[11px] text-muted-foreground">{dayLabels[activeDay]} · {classes.length} {classes.length === 1 ? 'class' : 'classes'}</p>

      {/* Class list */}
      <div className="space-y-2">
        <AnimatePresence mode="popLayout">
          {classes.map((cls, idx) => {
            const isEditing = editing?.day === activeDay && editing?.idx === idx;
            return (
              <motion.div
                key={`${activeDay}-${idx}`}
                layout
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.95 }}
                className="liquid-glass-card rounded-2xl overflow-hidden"
              >
                {/* Summary row */}
                <button
                  onClick={() => setEditing(isEditing ? null : { day: activeDay, idx })}
                  className="w-full flex items-center gap-3 px-4 py-3 text-left"
                >
                  <div className="flex-1 min-w-0">
                    <p className="text-[13px] font-semibold text-foreground truncate">{cls.subject}</p>
                    <p className="text-[10px] text-muted-foreground mt-0.5">{cls.time} · {cls.room}</p>
                  </div>
                  <span className={`material-symbols-outlined text-[16px] text-muted-foreground transition-transform ${isEditing ? 'rotate-180' : ''}`}>
                    expand_more
                  </span>
                </button>

                {/* Edit form */}
                <AnimatePresence>
                  {isEditing && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: 'auto', opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="px-4 pb-4 space-y-3 border-t border-white/10 dark:border-white/5 pt-3">
                        <div className="grid grid-cols-2 gap-2">
                          <div>
                            <label className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">Subject</label>
                            <input
                              value={cls.subject}
                              onChange={e => updateSlot(idx, { subject: e.target.value })}
                              className="w-full mt-1 bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2 text-[12px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30"
                            />
                          </div>
                          <div>
                            <label className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">Faculty</label>
                            <input
                              value={cls.faculty || ''}
                              onChange={e => updateSlot(idx, { faculty: e.target.value })}
                              className="w-full mt-1 bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2 text-[12px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30"
                            />
                          </div>
                        </div>
                        <div className="grid grid-cols-3 gap-2">
                          <div>
                            <label className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">Start Time</label>
                            <input
                              type="time"
                              value={cls.startTime}
                              onChange={e => {
                                const start = e.target.value;
                                // Auto-compute display time
                                const [h, m] = start.split(':').map(Number);
                                const endMin = h * 60 + m + 50;
                                const endH = Math.floor(endMin / 60);
                                const endM = endMin % 60;
                                const endStr = `${String(endH % 12 || 12).padStart(2, '0')}:${String(endM).padStart(2, '0')}`;
                                const startStr = `${String(h % 12 || 12).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
                                updateSlot(idx, { startTime: start, time: `${startStr} – ${endStr}` });
                              }}
                              className="w-full mt-1 bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2 text-[12px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30"
                            />
                          </div>
                          <div>
                            <label className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">Room</label>
                            <input
                              value={cls.room}
                              onChange={e => updateSlot(idx, { room: e.target.value })}
                              className="w-full mt-1 bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2 text-[12px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30"
                            />
                          </div>
                          <div>
                            <label className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">Type</label>
                            <select
                              value={cls.type || 'Lecture'}
                              onChange={e => updateSlot(idx, { type: e.target.value === 'Lecture' ? undefined : e.target.value })}
                              className="w-full mt-1 bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2 text-[12px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30"
                            >
                              <option value="Lecture">Lecture</option>
                              <option value="Lab">Lab</option>
                              <option value="Tutorial">Tutorial</option>
                            </select>
                          </div>
                        </div>
                        <button
                          onClick={() => removeSlot(idx)}
                          className="text-[11px] font-medium text-rose-500 flex items-center gap-1 pt-1"
                        >
                          <span className="material-symbols-outlined text-[14px]">delete</span>
                          Remove class
                        </button>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.div>
            );
          })}
        </AnimatePresence>

        <button
          onClick={addSlot}
          className="w-full py-2.5 rounded-2xl border border-dashed border-white/20 dark:border-white/8 text-[11px] font-medium text-muted-foreground flex items-center justify-center gap-1 active:scale-[0.98] transition-transform"
        >
          <span className="material-symbols-outlined text-[16px]">add</span>
          Add class
        </button>
      </div>

      {/* Actions */}
      <div className="flex gap-2 pt-2">
        <button
          onClick={handleReset}
          className="flex-1 py-2.5 rounded-2xl bg-white/10 dark:bg-white/5 text-[11px] font-medium text-muted-foreground active:scale-[0.97] transition-transform"
        >
          Reset to default
        </button>
        <button
          onClick={handleSave}
          className="flex-1 py-2.5 rounded-2xl bg-foreground text-background text-[11px] font-bold active:scale-[0.97] transition-transform"
        >
          Save changes
        </button>
      </div>
    </div>
  );
};

// ─── Main Profile ───
const Profile: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const [notificationsEnabled, setNotificationsEnabled] = useState(false);
  const [hideExamCountdown, setHideExamCountdown] = useState(() => localStorage.getItem('hideExamCountdown') !== 'false');
  const [showTimetableEditor, setShowTimetableEditor] = useState(false);
  const [examDate, setExamDateState] = useState(getExamDate);
  const [showExamDatePicker, setShowExamDatePicker] = useState(false);

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

  const handleExamDateChange = (date: string) => {
    saveExamDate(date);
    setExamDateState(date);
    setShowExamDatePicker(false);
  };

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      <div className="liquid-glass-nav flex items-center px-5 py-3.5 border-b border-white/20 dark:border-white/5">
        <h2 className="font-display text-foreground text-[18px] md:text-[20px] font-bold tracking-tight flex-1 text-center lg:text-left">Settings</h2>
      </div>

      <motion.div className="max-w-3xl mx-auto w-full px-5 md:px-6 space-y-5 pt-6 pb-6" variants={stagger} initial="hidden" animate="visible">
        {/* Preferences */}
        <motion.section variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest px-1 mb-2">Preferences</h3>
          <div className="liquid-glass rounded-2xl divide-y divide-white/15 dark:divide-white/5">
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
                <button onClick={handleEnableNotifications} className="text-[11px] font-medium text-primary">Enable</button>
              )}
            </div>
          </div>
        </motion.section>

        {/* Exam Date */}
        <motion.section variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest px-1 mb-2">Exam Date</h3>
          <div className="liquid-glass rounded-2xl px-4 py-3.5">
            <button className="w-full flex items-center justify-between" onClick={() => setShowExamDatePicker(!showExamDatePicker)}>
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-muted-foreground text-[20px]">event</span>
                <div className="text-left">
                  <p className="text-foreground text-[13px] font-medium">Sessional Exam Date</p>
                  <p className="text-muted-foreground text-[10px]">{new Date(examDate + 'T00:00:00').toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</p>
                </div>
              </div>
              <span className="material-symbols-outlined text-muted-foreground text-[16px]">edit</span>
            </button>
            <AnimatePresence>
              {showExamDatePicker && (
                <motion.div
                  initial={{ height: 0, opacity: 0 }}
                  animate={{ height: 'auto', opacity: 1 }}
                  exit={{ height: 0, opacity: 0 }}
                  className="overflow-hidden"
                >
                  <div className="pt-3 mt-3 border-t border-white/10 dark:border-white/5">
                    <input
                      type="date"
                      value={examDate}
                      onChange={e => handleExamDateChange(e.target.value)}
                      className="w-full bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2.5 text-[12px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30"
                    />
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </motion.section>

        {/* Timetable Editor */}
        <motion.section variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest px-1 mb-2">Timetable</h3>
          {!showTimetableEditor ? (
            <button
              onClick={() => setShowTimetableEditor(true)}
              className="liquid-glass rounded-2xl px-4 py-3.5 w-full flex items-center justify-between"
            >
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-muted-foreground text-[20px]">edit_calendar</span>
                <div className="text-left">
                  <p className="text-foreground text-[13px] font-medium">Edit Timetable</p>
                  <p className="text-muted-foreground text-[10px]">Change subjects, times, rooms</p>
                </div>
              </div>
              <span className="material-symbols-outlined text-muted-foreground text-[16px]">chevron_right</span>
            </button>
          ) : (
            <div className="liquid-glass rounded-2xl p-4">
              <div className="flex items-center justify-between mb-4">
                <p className="text-foreground text-[13px] font-semibold">Edit Timetable</p>
                <button onClick={() => setShowTimetableEditor(false)} className="text-muted-foreground">
                  <span className="material-symbols-outlined text-[18px]">close</span>
                </button>
              </div>
              <TimetableEditor onClose={() => setShowTimetableEditor(false)} />
            </div>
          )}
        </motion.section>

        {/* About */}
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
