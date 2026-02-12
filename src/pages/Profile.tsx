import React from 'react';
import { useNavigate } from 'react-router-dom';

const Profile: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20">
      {/* Header */}
      <div className="flex items-center px-4 py-3 justify-between border-b border-border">
        <button onClick={() => navigate(-1)} className="text-foreground flex size-10 shrink-0 items-center justify-center active:bg-accent rounded-full transition-colors">
          <span className="material-symbols-outlined text-[22px]">arrow_back</span>
        </button>
        <h2 className="text-foreground text-[17px] font-bold tracking-tight">Profile & Settings</h2>
        <div className="w-10" />
      </div>

      {/* Profile */}
      <div className="flex py-6 flex-col items-center">
        <div className="relative">
          <div
            className="bg-center bg-no-repeat bg-cover rounded-full w-28 h-28 border-4 border-primary/20 shadow-lg"
            style={{ backgroundImage: 'url("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80")' }}
          />
          <button className="absolute bottom-0 right-0 bg-primary text-primary-foreground p-1.5 rounded-full shadow-lg border-2 border-background flex items-center justify-center">
            <span className="material-symbols-outlined text-[14px]">edit</span>
          </button>
        </div>
        <div className="mt-3 flex flex-col items-center">
          <p className="text-foreground text-xl font-bold tracking-tight">Student Name</p>
          <div className="flex gap-2 mt-1.5">
            <span className="bg-primary/10 text-primary px-2.5 py-0.5 rounded-full text-[12px] font-semibold">ECE • 6th Sem</span>
            <span className="bg-secondary text-muted-foreground px-2.5 py-0.5 rounded-full text-[12px] font-medium">#ECE-2024-B</span>
          </div>
        </div>
      </div>

      <div className="px-4 space-y-5 pb-6">
        {/* Notifications */}
        <section>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Notification Settings</h3>
          <div className="bg-card rounded-2xl overflow-hidden divide-y divide-border border border-border">
            {[
              { icon: 'notifications_active', label: 'Attendance Alerts' },
              { icon: 'schedule', label: 'Class Reminders' },
            ].map((item, idx) => (
              <div key={idx} className="flex items-center gap-3 px-4 py-3.5 justify-between">
                <div className="flex items-center gap-3">
                  <div className="text-primary flex items-center justify-center rounded-xl bg-primary/10 shrink-0 size-9">
                    <span className="material-symbols-outlined text-[20px]">{item.icon}</span>
                  </div>
                  <p className="text-foreground text-[14px] font-medium">{item.label}</p>
                </div>
                <div className="relative w-11 h-[26px] bg-primary rounded-full p-0.5 cursor-pointer">
                  <div className="absolute right-0.5 top-0.5 h-[22px] w-[22px] rounded-full bg-white shadow-sm transition-transform" />
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Preferences */}
        <section>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Preferences</h3>
          <div className="bg-card rounded-2xl overflow-hidden divide-y divide-border border border-border">
            {[
              { icon: 'dark_mode', label: 'Theme Mode', sub: 'Light Mode' },
              { icon: 'language', label: 'Language', sub: 'English (US)' },
            ].map((item, idx) => (
              <div key={idx} className="flex items-center gap-3 px-4 py-3.5 justify-between cursor-pointer active:bg-accent transition-colors">
                <div className="flex items-center gap-3">
                  <div className="text-muted-foreground flex items-center justify-center rounded-xl bg-secondary shrink-0 size-9">
                    <span className="material-symbols-outlined text-[20px]">{item.icon}</span>
                  </div>
                  <div>
                    <p className="text-foreground text-[14px] font-medium">{item.label}</p>
                    <p className="text-muted-foreground text-[11px]">{item.sub}</p>
                  </div>
                </div>
                <span className="material-symbols-outlined text-muted-foreground text-[20px]">chevron_right</span>
              </div>
            ))}
          </div>
        </section>

        {/* Account */}
        <section>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Account</h3>
          <div className="bg-card rounded-2xl overflow-hidden divide-y divide-border border border-border">
            <div className="flex items-center gap-3 px-4 py-3.5 justify-between cursor-pointer active:bg-accent transition-colors">
              <div className="flex items-center gap-3">
                <div className="text-muted-foreground flex items-center justify-center rounded-xl bg-secondary shrink-0 size-9">
                  <span className="material-symbols-outlined text-[20px]">lock</span>
                </div>
                <p className="text-foreground text-[14px] font-medium">Change Password</p>
              </div>
              <span className="material-symbols-outlined text-muted-foreground text-[20px]">chevron_right</span>
            </div>
            <div className="flex items-center gap-3 px-4 py-3.5 cursor-pointer active:bg-accent transition-colors">
              <div className="flex items-center gap-3">
                <div className="text-red-500 flex items-center justify-center rounded-xl bg-red-50 dark:bg-red-900/20 shrink-0 size-9">
                  <span className="material-symbols-outlined text-[20px]">logout</span>
                </div>
                <p className="text-red-500 text-[14px] font-medium">Logout</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default Profile;
