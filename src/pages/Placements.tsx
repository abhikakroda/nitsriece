import React from 'react';
import { useNavigate } from 'react-router-dom';

const companies = [
  { name: 'Texas Instruments', role: 'Analog Design Intern', date: 'Oct 28', ctc: '35 LPA', icon: 'memory', iconBg: 'bg-red-50 dark:bg-red-900/20', iconColor: 'text-red-600' },
  { name: 'Qualcomm', role: 'Hardware Eng. Intern', date: 'Nov 02', ctc: '32 LPA', icon: 'developer_board', iconBg: 'bg-blue-50 dark:bg-blue-900/20', iconColor: 'text-blue-600' },
  { name: 'NVIDIA', role: 'ASIC Engineer', date: 'Nov 10', ctc: '40 LPA', icon: 'videogame_asset', iconBg: 'bg-green-50 dark:bg-green-900/20', iconColor: 'text-green-600' },
  { name: 'Samsung Semi', role: 'R&D Engineer', date: 'Nov 15', ctc: '22 LPA', icon: 'smartphone', iconBg: 'bg-indigo-50 dark:bg-indigo-900/20', iconColor: 'text-indigo-600' },
];

const Placements: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-background min-h-screen flex flex-col pb-20">
      <header className="sticky top-0 z-50 glass-morphism px-4 py-3 border-b border-border">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate(-1)} className="p-2 active:bg-accent rounded-full transition-colors">
            <span className="material-symbols-outlined text-primary text-[22px]">arrow_back</span>
          </button>
          <h1 className="text-[17px] font-bold tracking-tight text-foreground">Placement Cell</h1>
        </div>
      </header>

      <main className="flex-1 px-4 py-4 space-y-5">
        {/* Hero */}
        <div className="bg-slate-900 rounded-2xl p-5 text-white shadow-lg relative overflow-hidden">
          <div className="relative z-10">
            <h2 className="text-xl font-bold mb-1">Internship Season 2026</h2>
            <p className="text-slate-400 text-[12px] mb-3">Preparation is key. 12 companies visiting this month.</p>
            <button className="bg-primary text-primary-foreground px-4 py-2 rounded-xl text-[13px] font-bold flex items-center gap-2 active:scale-95 transition-transform">
              <span className="material-symbols-outlined text-[16px]">upload_file</span> Update Resume
            </button>
          </div>
          <div className="absolute right-[-15px] bottom-[-15px] opacity-10">
            <span className="material-symbols-outlined text-[100px]">business_center</span>
          </div>
        </div>

        {/* Drives */}
        <section>
          <div className="flex items-center justify-between mb-2.5">
            <h3 className="text-[14px] font-bold text-foreground">Upcoming Drives</h3>
            <span className="text-primary text-[11px] font-bold uppercase cursor-pointer">View Calendar</span>
          </div>
          <div className="space-y-2.5">
            {companies.map((c, idx) => (
              <div key={idx} className="bg-card p-3.5 rounded-2xl border border-border flex items-center gap-3 active:scale-[0.99] transition-transform cursor-pointer">
                <div className={`size-11 rounded-xl flex items-center justify-center shrink-0 ${c.iconBg} ${c.iconColor}`}>
                  <span className="material-symbols-outlined text-[20px]">{c.icon}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <h4 className="font-bold text-foreground text-[14px] leading-tight">{c.name}</h4>
                  <p className="text-[11px] text-muted-foreground font-medium truncate">{c.role}</p>
                  <div className="flex items-center gap-1.5 mt-1">
                    <span className="bg-secondary px-1.5 py-0.5 rounded text-[9px] font-bold text-secondary-foreground">{c.ctc}</span>
                    <span className="bg-red-50 dark:bg-red-900/20 px-1.5 py-0.5 rounded text-[9px] font-bold text-red-600 dark:text-red-400">CGPA {'>'} 8.0</span>
                  </div>
                </div>
                <div className="flex flex-col items-end shrink-0">
                  <span className="text-[14px] font-bold text-foreground">{c.date.split(' ')[1]}</span>
                  <span className="text-[9px] uppercase font-bold text-muted-foreground">{c.date.split(' ')[0]}</span>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Prep */}
        <section>
          <h3 className="text-[14px] font-bold text-foreground mb-2.5">Quick Prep</h3>
          <div className="grid grid-cols-2 gap-2.5">
            {[
              { icon: 'psychology', label: 'Aptitude', sub: 'Quant & Logical', bg: 'bg-purple-50 dark:bg-purple-900/10', border: 'border-purple-100 dark:border-purple-800', color: 'text-purple-600', titleColor: 'text-purple-900 dark:text-purple-100', subColor: 'text-purple-600 dark:text-purple-300' },
              { icon: 'code', label: 'Coding', sub: 'DSA Sheets', bg: 'bg-orange-50 dark:bg-orange-900/10', border: 'border-orange-100 dark:border-orange-800', color: 'text-orange-600', titleColor: 'text-orange-900 dark:text-orange-100', subColor: 'text-orange-600 dark:text-orange-300' },
              { icon: 'memory', label: 'Core ECE', sub: 'Digital & Analog', bg: 'bg-cyan-50 dark:bg-cyan-900/10', border: 'border-cyan-100 dark:border-cyan-800', color: 'text-cyan-600', titleColor: 'text-cyan-900 dark:text-cyan-100', subColor: 'text-cyan-600 dark:text-cyan-300' },
              { icon: 'forum', label: 'Interview', sub: 'HR Questions', bg: 'bg-pink-50 dark:bg-pink-900/10', border: 'border-pink-100 dark:border-pink-800', color: 'text-pink-600', titleColor: 'text-pink-900 dark:text-pink-100', subColor: 'text-pink-600 dark:text-pink-300' },
            ].map((item, idx) => (
              <div key={idx} className={`${item.bg} p-3.5 rounded-2xl border ${item.border} flex flex-col items-center text-center gap-2 cursor-pointer active:scale-95 transition-transform`}>
                <span className={`material-symbols-outlined ${item.color} text-[28px]`}>{item.icon}</span>
                <div>
                  <h4 className={`font-bold text-[13px] ${item.titleColor}`}>{item.label}</h4>
                  <p className={`text-[10px] ${item.subColor}`}>{item.sub}</p>
                </div>
              </div>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
};

export default Placements;
