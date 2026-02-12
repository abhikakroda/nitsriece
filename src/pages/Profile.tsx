import React, { useState, useEffect } from 'react';
import { useTheme } from '@/hooks/use-theme';
import { requestNotificationPermission, isNotificationEnabled } from '@/lib/notifications';
import { getAllSubjectStats } from '@/lib/attendance';

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
    <div className="flex flex-col min-h-screen bg-background pb-20">
      {/* Header */}
      <div className="flex items-center px-4 py-3 border-b border-border">
        <h2 className="text-foreground text-[17px] font-bold tracking-tight flex-1 text-center">Settings</h2>
      </div>

      <div className="px-4 space-y-5 pt-5 pb-6">

        {/* Attendance Overview */}
        {statEntries.length > 0 && (
          <section>
            <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Attendance Overview</h3>
            <div className="bg-card rounded-2xl overflow-hidden divide-y divide-border border border-border">
              {statEntries.map(([subject, stats]) => (
                <div key={subject} className="flex items-center gap-3 px-4 py-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-foreground text-[13px] font-medium truncate">{subject}</p>
                    <p className="text-muted-foreground text-[10px]">{stats.present}P / {stats.absent}A — {stats.total} classes</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-16 h-1.5 bg-secondary rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all ${stats.percentage >= 75 ? 'bg-green-500' : 'bg-red-500'}`}
                        style={{ width: `${stats.percentage}%` }}
                      />
                    </div>
                    <span className={`text-[11px] font-bold min-w-[32px] text-right ${stats.percentage >= 75 ? 'text-green-600' : 'text-red-500'}`}>
                      {stats.percentage}%
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Notifications */}
        <section>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Notifications</h3>
          <div className="bg-card rounded-2xl overflow-hidden divide-y divide-border border border-border">
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between">
              <div className="flex items-center gap-3">
                <div className="text-primary flex items-center justify-center rounded-xl bg-primary/10 shrink-0 size-9">
                  <span className="material-symbols-outlined text-[20px]">notifications_active</span>
                </div>
                <div>
                  <p className="text-foreground text-[14px] font-medium">Class Reminders</p>
                  <p className="text-muted-foreground text-[10px]">Get notified 20min, 5min before class</p>
                </div>
              </div>
              {notificationsEnabled ? (
                <div className="flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-green-500 text-[16px]">check_circle</span>
                  <span className="text-green-600 text-[11px] font-bold">On</span>
                </div>
              ) : (
                <button
                  onClick={handleEnableNotifications}
                  className="bg-primary text-primary-foreground text-[11px] font-bold px-3 py-1.5 rounded-lg active:scale-95 transition-transform"
                >
                  Enable
                </button>
              )}
            </div>
          </div>
        </section>

        {/* Appearance */}
        <section>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Appearance</h3>
          <div className="bg-card rounded-2xl overflow-hidden divide-y divide-border border border-border">
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between cursor-pointer active:bg-accent transition-colors" onClick={toggleTheme}>
              <div className="flex items-center gap-3">
                <div className="text-muted-foreground flex items-center justify-center rounded-xl bg-secondary shrink-0 size-9">
                  <span className="material-symbols-outlined text-[20px]">{theme === 'dark' ? 'light_mode' : 'dark_mode'}</span>
                </div>
                <div>
                  <p className="text-foreground text-[14px] font-medium">Dark Mode</p>
                  <p className="text-muted-foreground text-[11px]">{theme === 'dark' ? 'Currently dark' : 'Currently light'}</p>
                </div>
              </div>
              <div className={`relative w-11 h-[26px] rounded-full p-0.5 cursor-pointer transition-colors ${theme === 'dark' ? 'bg-primary' : 'bg-border'}`}>
                <div className={`h-[22px] w-[22px] rounded-full bg-white shadow-sm transition-transform ${theme === 'dark' ? 'translate-x-[18px]' : 'translate-x-0'}`} />
              </div>
            </div>
          </div>
        </section>

        {/* Preferences */}
        <section>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Preferences</h3>
          <div className="bg-card rounded-2xl overflow-hidden divide-y divide-border border border-border">
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between cursor-pointer active:bg-accent transition-colors">
              <div className="flex items-center gap-3">
                <div className="text-muted-foreground flex items-center justify-center rounded-xl bg-secondary shrink-0 size-9">
                  <span className="material-symbols-outlined text-[20px]">language</span>
                </div>
                <div>
                  <p className="text-foreground text-[14px] font-medium">Language</p>
                  <p className="text-muted-foreground text-[11px]">English (US)</p>
                </div>
              </div>
              <span className="material-symbols-outlined text-muted-foreground text-[20px]">chevron_right</span>
            </div>
          </div>
        </section>

        {/* About */}
        <section>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">About</h3>
          <div className="bg-card rounded-2xl overflow-hidden border border-border px-4 py-3.5">
            <div className="flex items-center gap-3">
              <div className="size-10 rounded-xl bg-gradient-to-br from-primary to-blue-600 flex items-center justify-center shadow-sm">
                <span className="material-symbols-outlined text-white text-[20px]">school</span>
              </div>
              <div>
                <p className="text-foreground text-[14px] font-bold">Campus Companion</p>
                <p className="text-muted-foreground text-[11px]">Version 1.0 • ECE Sec B</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default Profile;
