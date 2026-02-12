import React from 'react';
import { useNavigate } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 relative overflow-x-hidden">

      {/* Liquid Blobs */}
      <div className="liquid-blob top-[-10%] left-[-10%] w-72 h-72 bg-purple-400/30 dark:bg-purple-900/30 rounded-full mix-blend-multiply filter blur-3xl animate-blob" />
      <div className="liquid-blob top-[20%] right-[-10%] w-64 h-64 bg-cyan-300/30 dark:bg-cyan-900/30 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-2000" />
      <div className="liquid-blob bottom-[-10%] left-[20%] w-80 h-80 bg-pink-300/30 dark:bg-pink-900/30 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-4000" />

      {/* Header */}
      <div className="sticky top-0 z-50 glass-morphism px-4 py-3 flex items-center justify-between border-b border-border">
        <div className="flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-full bg-cover bg-center border-2 border-primary/20 cursor-pointer shadow-md active:scale-95 transition-transform"
            style={{ backgroundImage: 'url("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80")' }}
            onClick={() => navigate('/profile')}
          />
          <div>
            <h2 className="text-foreground text-[15px] font-bold leading-tight">Welcome, Student</h2>
            <p className="text-primary text-[11px] font-bold tracking-wide">ECE • 6th Sem - Sec B</p>
          </div>
        </div>
        <button className="flex items-center justify-center rounded-xl h-10 w-10 bg-primary/5 text-primary border border-primary/10 active:scale-95 transition-transform">
          <span className="material-symbols-outlined text-[22px]">notifications</span>
        </button>
      </div>

      <div className="px-4 pt-4 pb-1 relative z-10">
        <p className="text-muted-foreground text-[11px] font-bold uppercase tracking-widest">Monday, Oct 23</p>
        <h1 className="text-2xl font-black text-foreground tracking-tight mt-0.5">Daily Schedule</h1>
      </div>

      {/* Notices Card */}
      <div className="px-4 pb-5 relative z-10">
        <div className="bg-gradient-to-br from-primary to-blue-600 rounded-2xl p-5 text-white shadow-lg shadow-primary/20 overflow-hidden relative cursor-pointer active:scale-[0.98] transition-transform group">
          <div className="relative z-10 flex justify-between items-start mb-4">
            <div>
              <p className="text-blue-100 text-[10px] font-bold uppercase tracking-widest mb-1">Latest Update</p>
              <h3 className="text-xl font-bold leading-tight">Sessional Exams</h3>
            </div>
            <div className="bg-white/20 p-2 rounded-xl backdrop-blur-md border border-white/10 animate-pulse-glow">
              <span className="material-symbols-outlined text-[20px]">campaign</span>
            </div>
          </div>
          <div className="relative z-10 space-y-2">
            <div className="bg-white/10 rounded-xl p-2.5 flex items-center gap-2.5 border border-white/5 backdrop-blur-sm">
              <span className="material-symbols-outlined text-[18px]">event</span>
              <p className="text-[13px] font-semibold">Date Sheet Released for Jan 2026</p>
            </div>
            <p className="text-[11px] text-blue-50 font-medium ml-1">Check resources for PDF.</p>
          </div>
          <div className="absolute right-[-20px] bottom-[-40px] w-48 h-48 bg-white/10 rounded-full blur-3xl opacity-50" />
        </div>
      </div>

      {/* Timetable */}
      <div className="flex-1 px-4 space-y-3">
        <div className="flex items-center justify-between mb-1">
          <h2 className="text-base font-bold text-foreground">Today's Timetable</h2>
          <span className="text-[10px] font-bold text-primary bg-primary/10 px-2 py-0.5 rounded-full uppercase tracking-tight">Mon • 5 Classes</span>
        </div>

        {/* Current Class */}
        <div className="bg-card rounded-2xl p-4 border border-orange-200/60 dark:border-border shadow-sm relative overflow-hidden">
          <div className="absolute top-2 right-2 opacity-[0.04]">
            <span className="material-symbols-outlined text-[80px]">memory</span>
          </div>
          <div className="flex items-start justify-between relative z-10">
            <div className="flex gap-3">
              <div className="size-10 rounded-xl bg-orange-500/10 text-orange-500 flex items-center justify-center shrink-0">
                <span className="material-symbols-outlined text-[20px]">memory</span>
              </div>
              <div>
                <h3 className="font-bold text-foreground text-[14px] leading-tight">Computer Org & Arch</h3>
                <p className="text-[11px] text-muted-foreground mt-1 flex items-center gap-1">
                  <span className="material-symbols-outlined text-[13px]">schedule</span> 09:50 – 10:40 AM
                </p>
                <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                  <span className="material-symbols-outlined text-[13px]">location_on</span> ECT 352
                </p>
              </div>
            </div>
            <span className="text-[9px] font-bold py-0.5 px-2 rounded-full bg-green-500/10 text-green-600 uppercase border border-green-500/20 animate-pulse">Current</span>
          </div>
          <div className="grid grid-cols-2 gap-2.5 mt-3.5 relative z-10">
            <button className="flex items-center justify-center gap-1.5 bg-primary text-primary-foreground text-[13px] font-semibold py-2.5 rounded-xl active:scale-[0.97] transition-transform shadow-md shadow-primary/20">
              <span className="material-symbols-outlined text-[16px]">check_circle</span> Present
            </button>
            <button className="flex items-center justify-center gap-1.5 bg-card border border-border text-muted-foreground text-[13px] font-semibold py-2.5 rounded-xl active:scale-[0.97] transition-transform">
              <span className="material-symbols-outlined text-[16px]">cancel</span> Absent
            </button>
          </div>
        </div>

        {/* Upcoming */}
        {[
          { name: 'VLSI Design Lab (G3)', time: '10:40 – 12:20 PM', room: 'ECL 356', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500' },
          { name: 'DSP Lab-I (G3)', time: '02:00 – 03:40 PM', room: 'ECL 357', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500' },
        ].map((cls, idx) => (
          <div key={idx} className="bg-card rounded-2xl p-4 border border-border opacity-70 active:opacity-100 transition-opacity">
            <div className="flex items-start justify-between">
              <div className="flex gap-3">
                <div className={`size-10 rounded-xl ${cls.iconBg} ${cls.iconColor} flex items-center justify-center shrink-0`}>
                  <span className="material-symbols-outlined text-[20px]">{cls.icon}</span>
                </div>
                <div>
                  <h3 className="font-bold text-foreground text-[14px] leading-tight">{cls.name}</h3>
                  <p className="text-[11px] text-muted-foreground mt-1 flex items-center gap-1">
                    <span className="material-symbols-outlined text-[13px]">schedule</span> {cls.time}
                  </p>
                  <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                    <span className="material-symbols-outlined text-[13px]">location_on</span> {cls.room}
                  </p>
                </div>
              </div>
              <span className="text-[9px] font-bold py-0.5 px-2 rounded-full bg-secondary text-muted-foreground uppercase">Upcoming</span>
            </div>
          </div>
        ))}
      </div>

      {/* Resources */}
      <div className="px-4 py-5">
        <div className="flex items-center justify-between mb-2.5">
          <h3 className="text-[13px] font-bold text-foreground">Recent Resources</h3>
          <button className="text-primary text-[11px] font-bold">View All</button>
        </div>
        <div className="bg-card rounded-2xl border border-border divide-y divide-border overflow-hidden">
          {[
            { icon: 'picture_as_pdf', color: 'text-red-500 bg-red-50 dark:bg-red-900/20', title: 'VLSI Unit 2 Notes', sub: 'Uploaded yesterday' },
            { icon: 'description', color: 'text-blue-500 bg-blue-50 dark:bg-blue-900/20', title: 'DSP Assignment 3', sub: 'Due in 2 days' },
          ].map((r, idx) => (
            <div key={idx} className="flex items-center gap-3 p-3.5 active:bg-accent transition-colors cursor-pointer">
              <div className={`size-9 rounded-lg flex items-center justify-center shrink-0 ${r.color}`}>
                <span className="material-symbols-outlined text-[18px]">{r.icon}</span>
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="text-[13px] font-bold text-foreground">{r.title}</h4>
                <p className="text-[11px] text-muted-foreground">{r.sub}</p>
              </div>
              <span className="material-symbols-outlined text-muted-foreground text-[20px]">download</span>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Access */}
      <div className="px-4 pb-6 grid grid-cols-2 gap-3">
        {[
          { path: '/focus', icon: 'timer', label: 'Focus Mode', sub: 'Start Pomodoro', dark: true },
          { path: '/placements', icon: 'business_center', label: 'Placement Cell', sub: 'Drives & Prep', bg: 'bg-blue-50 dark:bg-blue-900/10', border: 'border-blue-100 dark:border-blue-800', iconBg: 'bg-blue-500', textColor: 'text-blue-900 dark:text-blue-100', subColor: 'text-blue-600 dark:text-blue-300' },
          { path: '/faculty', icon: 'school', label: 'Faculty', sub: 'Contact Professors', bg: 'bg-amber-50 dark:bg-amber-900/10', border: 'border-amber-100 dark:border-amber-800', iconBg: 'bg-amber-500', textColor: 'text-amber-900 dark:text-amber-100', subColor: 'text-amber-600 dark:text-amber-300' },
          { icon: 'calculate', label: 'GPA Estimator', sub: 'Calculate SGPA', bg: 'bg-teal-50 dark:bg-teal-900/10', border: 'border-teal-100 dark:border-teal-800', iconBg: 'bg-teal-500', textColor: 'text-teal-900 dark:text-teal-100', subColor: 'text-teal-600 dark:text-teal-300' },
          { icon: 'group', label: 'Study Groups', sub: 'Find Buddies', bg: 'bg-indigo-50 dark:bg-indigo-900/10', border: 'border-indigo-100 dark:border-indigo-800', iconBg: 'bg-indigo-500', textColor: 'text-indigo-900 dark:text-indigo-100', subColor: 'text-indigo-600 dark:text-indigo-300' },
          { icon: 'checklist_rtl', label: 'Syllabus Tracker', sub: 'Track Progress', bg: 'bg-rose-50 dark:bg-rose-900/10', border: 'border-rose-100 dark:border-rose-800', iconBg: 'bg-rose-500', textColor: 'text-rose-900 dark:text-rose-100', subColor: 'text-rose-600 dark:text-rose-300' },
        ].map((tile, idx) => (
          tile.dark ? (
            <div key={idx} onClick={() => tile.path && navigate(tile.path)} className="bg-slate-900 text-white p-4 rounded-2xl flex flex-col justify-between cursor-pointer border border-slate-700 h-[120px] relative overflow-hidden group active:scale-95 transition-transform">
              <div className="absolute inset-0 bg-gradient-to-br from-purple-600/20 to-blue-600/20 opacity-0 group-active:opacity-100 transition-opacity" />
              <div className="size-9 rounded-xl bg-white/10 flex items-center justify-center relative z-10">
                <span className="material-symbols-outlined text-[20px]">{tile.icon}</span>
              </div>
              <div className="relative z-10">
                <h3 className="font-bold text-white text-[13px] leading-tight">{tile.label}</h3>
                <p className="text-[10px] text-slate-300 mt-0.5 font-medium">{tile.sub}</p>
              </div>
            </div>
          ) : (
            <div key={idx} onClick={() => tile.path && navigate(tile.path)} className={`${tile.bg} p-4 rounded-2xl flex flex-col justify-between cursor-pointer border ${tile.border} h-[120px] active:scale-95 transition-transform`}>
              <div className={`size-9 rounded-xl ${tile.iconBg} text-white flex items-center justify-center shadow-sm`}>
                <span className="material-symbols-outlined text-[20px]">{tile.icon}</span>
              </div>
              <div>
                <h3 className={`font-bold ${tile.textColor} text-[13px] leading-tight`}>{tile.label}</h3>
                <p className={`text-[10px] ${tile.subColor} mt-0.5 font-medium`}>{tile.sub}</p>
              </div>
            </div>
          )
        ))}
      </div>
    </div>
  );
};

export default Dashboard;
