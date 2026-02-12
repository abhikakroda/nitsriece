import React from 'react';
import { useNavigate } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col min-h-screen bg-background pb-safe relative overflow-x-hidden transition-colors duration-500">

      {/* Liquid Blobs Background */}
      <div className="liquid-blob top-[-10%] left-[-10%] w-72 h-72 bg-purple-400/30 dark:bg-purple-900/30 rounded-full mix-blend-multiply filter blur-3xl animate-blob"></div>
      <div className="liquid-blob top-[20%] right-[-10%] w-64 h-64 bg-cyan-300/30 dark:bg-cyan-900/30 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-2000"></div>
      <div className="liquid-blob bottom-[-10%] left-[20%] w-80 h-80 bg-pink-300/30 dark:bg-pink-900/30 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-4000"></div>

      {/* Header */}
      <div className="sticky top-0 z-50 glass-morphism p-4 pb-2 flex items-center justify-between border-b border-border shadow-sm transition-all duration-300">
        <div className="flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-full bg-cover bg-center border-2 border-border cursor-pointer shadow-lg hover:scale-105 transition-transform"
            style={{ backgroundImage: 'url("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80")' }}
            onClick={() => navigate('/profile')}
          ></div>
          <div>
            <h2 className="text-foreground text-base font-bold leading-tight">Welcome, Student</h2>
            <p className="text-primary text-xs font-bold tracking-wide">ECE • 6th Sem - Sec B</p>
          </div>
        </div>
        <button className="flex items-center justify-center rounded-xl h-10 w-10 bg-card/50 text-primary backdrop-blur-sm border border-border shadow-sm hover:shadow-md transition-all active:scale-95">
          <span className="material-symbols-outlined text-xl">notifications</span>
        </button>
      </div>

      <div className="p-5 flex flex-col gap-1 relative z-10">
        <p className="text-muted-foreground text-sm font-bold uppercase tracking-wider opacity-80">Monday, Oct 23</p>
        <h1 className="text-3xl font-black text-foreground tracking-tight">Daily Schedule</h1>
      </div>

      {/* Recent Notices Card */}
      <div className="px-5 pb-6 relative z-10">
        <div className="bg-gradient-to-br from-primary to-blue-600 rounded-2xl p-6 text-primary-foreground shadow-xl shadow-blue-500/20 flex flex-col justify-between overflow-hidden relative cursor-pointer hover:-translate-y-1 transition-transform duration-300 group">
          <div className="relative z-10 flex justify-between items-start mb-6">
            <div>
              <p className="text-blue-100 text-xs font-bold uppercase tracking-widest mb-1">Latest Update</p>
              <h3 className="text-2xl font-bold leading-tight group-hover:underline decoration-2 underline-offset-4 decoration-white/30">Sessional Exams</h3>
            </div>
            <div className="bg-white/20 p-2.5 rounded-xl backdrop-blur-md shadow-inner border border-white/10 animate-pulse-glow">
              <span className="material-symbols-outlined">campaign</span>
            </div>
          </div>
          <div className="relative z-10 space-y-3">
            <div className="bg-white/10 rounded-xl p-3 flex items-center gap-3 border border-white/5 backdrop-blur-sm">
              <span className="material-symbols-outlined text-lg">event</span>
              <p className="text-sm font-semibold">Date Sheet Released for Jan 2026</p>
            </div>
            <p className="text-xs text-blue-50 font-medium ml-1">Check resources for PDF.</p>
          </div>
          <div className="absolute right-[-20px] bottom-[-40px] size-48 bg-white/10 rounded-full blur-3xl opacity-50 group-hover:scale-125 transition-transform duration-700"></div>
          <div className="absolute top-[-20px] left-[-20px] size-32 bg-blue-400/20 rounded-full blur-2xl"></div>
        </div>
      </div>

      {/* Timetable */}
      <div className="flex-1 px-5 space-y-4">
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-lg font-bold text-foreground">Today's Timetable</h2>
          <span className="text-xs font-bold text-primary bg-primary/10 px-2 py-1 rounded-full uppercase tracking-tighter">Mon • 5 Classes</span>
        </div>

        {/* Current Class */}
        <div className="bg-gradient-to-br from-card to-orange-50 dark:from-card dark:to-card/80 rounded-xl p-4 border border-orange-100 dark:border-border shadow-md shadow-orange-500/5 relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-5 transform group-hover:scale-110 transition-transform duration-500">
            <span className="material-symbols-outlined text-[100px]">memory</span>
          </div>
          <div className="flex items-start justify-between relative z-10">
            <div className="flex gap-3">
              <div className="size-10 rounded-lg bg-orange-500/10 text-orange-500 flex items-center justify-center shrink-0">
                <span className="material-symbols-outlined">memory</span>
              </div>
              <div>
                <h3 className="font-bold text-foreground leading-tight">Computer Org & Arch</h3>
                <p className="text-xs text-muted-foreground mt-1 flex items-center gap-1">
                  <span className="material-symbols-outlined text-[14px]">schedule</span> 09:50 AM - 10:40 AM
                </p>
                <p className="text-xs text-muted-foreground flex items-center gap-1">
                  <span className="material-symbols-outlined text-[14px]">location_on</span> ECT 352
                </p>
              </div>
            </div>
            <span className="text-[10px] font-bold py-1 px-2 rounded bg-green-500/10 text-green-600 uppercase border border-green-500/20 animate-pulse">Current</span>
          </div>
          <div className="grid grid-cols-2 gap-3 mt-4 relative z-10">
            <button className="flex items-center justify-center gap-1 bg-primary text-primary-foreground text-sm font-semibold py-2.5 rounded-lg hover:bg-primary/90 transition-all shadow-lg shadow-primary/20 active:scale-[0.98]">
              <span className="material-symbols-outlined text-sm">check_circle</span> Present
            </button>
            <button className="flex items-center justify-center gap-1 bg-card border border-border text-muted-foreground text-sm font-semibold py-2.5 rounded-lg hover:bg-accent transition-colors">
              <span className="material-symbols-outlined text-sm">cancel</span> Absent
            </button>
          </div>
        </div>

        {/* Upcoming Classes */}
        {[
          { name: 'VLSI Design Lab (G3)', time: '10:40 AM - 12:20 PM', room: 'ECL 356', icon: 'science', color: 'purple' },
          { name: 'DSP Lab-I (G3)', time: '02:00 PM - 03:40 PM', room: 'ECL 357', icon: 'graphic_eq', color: 'green' },
        ].map((cls, idx) => (
          <div key={idx} className="bg-card rounded-xl p-4 border border-border flex flex-col gap-4 opacity-75 hover:opacity-100 transition-opacity">
            <div className="flex items-start justify-between">
              <div className="flex gap-3">
                <div className={`size-10 rounded-lg bg-${cls.color}-500/10 text-${cls.color}-500 flex items-center justify-center shrink-0`}>
                  <span className="material-symbols-outlined">{cls.icon}</span>
                </div>
                <div>
                  <h3 className="font-bold text-foreground leading-tight">{cls.name}</h3>
                  <p className="text-xs text-muted-foreground mt-1 flex items-center gap-1">
                    <span className="material-symbols-outlined text-[14px]">schedule</span> {cls.time}
                  </p>
                  <p className="text-xs text-muted-foreground flex items-center gap-1">
                    <span className="material-symbols-outlined text-[14px]">location_on</span> {cls.room}
                  </p>
                </div>
              </div>
              <span className="text-[10px] font-bold py-1 px-2 rounded bg-secondary text-muted-foreground uppercase">Upcoming</span>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Resources */}
      <div className="p-5 mt-4">
        <div className="flex items-center justify-between mb-3 px-1">
          <h3 className="text-sm font-bold text-foreground">Recent Resources</h3>
          <button className="text-primary text-xs font-bold">View All</button>
        </div>
        <div className="bg-secondary/50 rounded-xl border border-primary/5 divide-y divide-border">
          <div className="flex items-center gap-4 p-4 hover:bg-card transition-colors cursor-pointer">
            <div className="size-10 bg-red-100 dark:bg-red-900/20 rounded-lg flex items-center justify-center text-red-500">
              <span className="material-symbols-outlined">picture_as_pdf</span>
            </div>
            <div className="flex-1">
              <h4 className="text-sm font-bold text-foreground">VLSI Unit 2 Notes</h4>
              <p className="text-xs text-muted-foreground">Uploaded yesterday</p>
            </div>
            <span className="material-symbols-outlined text-muted-foreground">download</span>
          </div>
          <div className="flex items-center gap-4 p-4 hover:bg-card transition-colors cursor-pointer">
            <div className="size-10 bg-blue-100 dark:bg-blue-900/20 rounded-lg flex items-center justify-center text-blue-500">
              <span className="material-symbols-outlined">description</span>
            </div>
            <div className="flex-1">
              <h4 className="text-sm font-bold text-foreground">DSP Assignment 3</h4>
              <p className="text-xs text-muted-foreground">Due in 2 days</p>
            </div>
            <span className="material-symbols-outlined text-muted-foreground">download</span>
          </div>
        </div>
      </div>

      {/* Quick Access Grid */}
      <div className="p-5 pt-0 grid grid-cols-2 gap-4">
        <div onClick={() => navigate('/focus')} className="bg-slate-900 text-white p-4 rounded-xl flex flex-col justify-between cursor-pointer border border-slate-700 h-32 relative overflow-hidden group active:scale-95 transition-transform">
          <div className="absolute inset-0 bg-gradient-to-br from-purple-600/20 to-blue-600/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
          <div className="size-10 rounded-lg bg-white/10 flex items-center justify-center backdrop-blur-sm relative z-10">
            <span className="material-symbols-outlined">timer</span>
          </div>
          <div className="relative z-10">
            <h3 className="font-bold text-white leading-tight">Focus Mode</h3>
            <p className="text-[10px] text-slate-300 mt-1 font-medium">Start Pomodoro</p>
          </div>
        </div>

        <div onClick={() => navigate('/placements')} className="bg-blue-50 dark:bg-blue-900/10 p-4 rounded-xl flex flex-col justify-between cursor-pointer border border-blue-100 dark:border-blue-800 h-32 active:scale-95 transition-transform">
          <div className="size-10 rounded-lg bg-blue-500 text-white flex items-center justify-center shadow-lg shadow-blue-500/20">
            <span className="material-symbols-outlined">business_center</span>
          </div>
          <div>
            <h3 className="font-bold text-blue-900 dark:text-blue-100 leading-tight">Placement Cell</h3>
            <p className="text-[10px] text-blue-600 dark:text-blue-300 mt-1 font-medium">Drives & Prep</p>
          </div>
        </div>

        <div onClick={() => navigate('/faculty')} className="bg-amber-50 dark:bg-amber-900/10 p-4 rounded-xl flex flex-col justify-between cursor-pointer border border-amber-100 dark:border-amber-800 h-32 active:scale-95 transition-transform">
          <div className="size-10 rounded-lg bg-amber-500 text-white flex items-center justify-center shadow-lg shadow-amber-500/20">
            <span className="material-symbols-outlined">school</span>
          </div>
          <div>
            <h3 className="font-bold text-amber-900 dark:text-amber-100 leading-tight">Faculty Directory</h3>
            <p className="text-[10px] text-amber-600 dark:text-amber-300 mt-1 font-medium">Contact Professors</p>
          </div>
        </div>

        <div className="bg-teal-50 dark:bg-teal-900/10 p-4 rounded-xl flex flex-col justify-between cursor-pointer border border-teal-100 dark:border-teal-800 h-32 active:scale-95 transition-transform">
          <div className="size-10 rounded-lg bg-teal-500 text-white flex items-center justify-center shadow-lg shadow-teal-500/20">
            <span className="material-symbols-outlined">calculate</span>
          </div>
          <div>
            <h3 className="font-bold text-teal-900 dark:text-teal-100 leading-tight">GPA Estimator</h3>
            <p className="text-[10px] text-teal-600 dark:text-teal-300 mt-1 font-medium">Calculate SGPA/CGPA</p>
          </div>
        </div>

        <div className="bg-indigo-50 dark:bg-indigo-900/10 p-4 rounded-xl flex flex-col justify-between cursor-pointer border border-indigo-100 dark:border-indigo-800 h-32 active:scale-95 transition-transform">
          <div className="size-10 rounded-lg bg-indigo-500 text-white flex items-center justify-center shadow-lg shadow-indigo-500/20">
            <span className="material-symbols-outlined">group</span>
          </div>
          <div>
            <h3 className="font-bold text-indigo-900 dark:text-indigo-100 leading-tight">Study Groups</h3>
            <p className="text-[10px] text-indigo-600 dark:text-indigo-300 mt-1 font-medium">Find Study Buddies</p>
          </div>
        </div>

        <div className="bg-rose-50 dark:bg-rose-900/10 p-4 rounded-xl flex flex-col justify-between cursor-pointer border border-rose-100 dark:border-rose-800 h-32 active:scale-95 transition-transform">
          <div className="size-10 rounded-lg bg-rose-500 text-white flex items-center justify-center shadow-lg shadow-rose-500/20">
            <span className="material-symbols-outlined">checklist_rtl</span>
          </div>
          <div>
            <h3 className="font-bold text-rose-900 dark:text-rose-100 leading-tight">Syllabus Tracker</h3>
            <p className="text-[10px] text-rose-600 dark:text-rose-300 mt-1 font-medium">Track Progress</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
