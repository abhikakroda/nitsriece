import React from 'react';
import { useNavigate } from 'react-router-dom';

const Profile: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col min-h-screen bg-background pb-safe">
      {/* Header */}
      <div className="flex items-center p-4 justify-between border-b border-border">
        <div onClick={() => navigate(-1)} className="text-foreground flex size-10 shrink-0 items-center justify-center cursor-pointer hover:bg-accent rounded-full">
          <span className="material-symbols-outlined">arrow_back</span>
        </div>
        <h2 className="text-foreground text-lg font-bold leading-tight tracking-tight flex-1 text-center pr-10">Profile & Settings</h2>
      </div>

      {/* Profile Section */}
      <div className="flex p-6 flex-col items-center">
        <div className="relative group cursor-pointer">
          <div
            className="bg-center bg-no-repeat aspect-square bg-cover rounded-full min-h-32 w-32 border-4 border-primary/20"
            style={{ backgroundImage: 'url("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80")' }}
          ></div>
          <button className="absolute bottom-0 right-0 bg-primary text-primary-foreground p-2 rounded-full shadow-lg border-2 border-background flex items-center justify-center">
            <span className="material-symbols-outlined text-sm">edit</span>
          </button>
        </div>
        <div className="mt-4 flex flex-col items-center justify-center">
          <p className="text-foreground text-2xl font-bold leading-tight tracking-tight text-center">Student Name</p>
          <div className="flex gap-2 mt-1">
            <span className="bg-primary/10 text-primary px-3 py-0.5 rounded-full text-sm font-medium">ECE • 6th Sem</span>
            <span className="bg-secondary text-muted-foreground px-3 py-0.5 rounded-full text-sm font-medium">#ECE-2024-B</span>
          </div>
        </div>
      </div>

      <div className="px-4 space-y-6">
        {/* Notifications */}
        <section>
          <h3 className="text-muted-foreground text-xs font-bold uppercase tracking-wider px-2 mb-2">Notification Settings</h3>
          <div className="bg-secondary/50 rounded-xl overflow-hidden divide-y divide-border">
            {[
              { icon: 'notifications_active', label: 'Attendance Alerts' },
              { icon: 'schedule', label: 'Class Reminders' },
            ].map((item, idx) => (
              <div key={idx} className="flex items-center gap-4 px-4 py-4 justify-between">
                <div className="flex items-center gap-4">
                  <div className="text-primary flex items-center justify-center rounded-lg bg-primary/10 shrink-0 size-10">
                    <span className="material-symbols-outlined">{item.icon}</span>
                  </div>
                  <p className="text-foreground text-base font-medium">{item.label}</p>
                </div>
                <label className="relative flex h-[28px] w-[48px] cursor-pointer items-center rounded-full border-none bg-primary p-0.5 justify-end transition-all">
                  <div className="h-full w-[24px] rounded-full bg-white shadow-md"></div>
                  <input defaultChecked className="hidden" type="checkbox" />
                </label>
              </div>
            ))}
          </div>
        </section>

        {/* Preferences */}
        <section>
          <h3 className="text-muted-foreground text-xs font-bold uppercase tracking-wider px-2 mb-2">Preferences</h3>
          <div className="bg-secondary/50 rounded-xl overflow-hidden divide-y divide-border">
            {[
              { icon: 'dark_mode', label: 'Theme Mode', sub: 'Light Mode' },
              { icon: 'language', label: 'Language', sub: 'English (US)' },
            ].map((item, idx) => (
              <div key={idx} className="flex items-center gap-4 px-4 py-4 justify-between cursor-pointer active:bg-accent">
                <div className="flex items-center gap-4">
                  <div className="text-muted-foreground flex items-center justify-center rounded-lg bg-secondary shrink-0 size-10">
                    <span className="material-symbols-outlined">{item.icon}</span>
                  </div>
                  <div className="flex flex-col">
                    <p className="text-foreground text-base font-medium">{item.label}</p>
                    <p className="text-muted-foreground text-xs font-normal">{item.sub}</p>
                  </div>
                </div>
                <span className="material-symbols-outlined text-muted-foreground">chevron_right</span>
              </div>
            ))}
          </div>
        </section>

        {/* Account */}
        <section>
          <h3 className="text-muted-foreground text-xs font-bold uppercase tracking-wider px-2 mb-2">Account Management</h3>
          <div className="bg-secondary/50 rounded-xl overflow-hidden divide-y divide-border">
            <div className="flex items-center gap-4 px-4 py-4 justify-between cursor-pointer active:bg-accent">
              <div className="flex items-center gap-4">
                <div className="text-muted-foreground flex items-center justify-center rounded-lg bg-secondary shrink-0 size-10">
                  <span className="material-symbols-outlined">lock</span>
                </div>
                <p className="text-foreground text-base font-medium">Change Password</p>
              </div>
              <span className="material-symbols-outlined text-muted-foreground">chevron_right</span>
            </div>
            <div className="flex items-center gap-4 px-4 py-4 justify-between cursor-pointer active:bg-accent">
              <div className="flex items-center gap-4">
                <div className="text-red-500 flex items-center justify-center rounded-lg bg-red-50 dark:bg-red-900/20 shrink-0 size-10">
                  <span className="material-symbols-outlined">logout</span>
                </div>
                <p className="text-red-500 text-base font-medium">Logout</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default Profile;
