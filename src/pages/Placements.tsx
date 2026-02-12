import React from 'react';
import { useNavigate } from 'react-router-dom';

const companies = [
  { name: 'Texas Instruments', role: 'Analog Design Intern', date: 'Oct 28', ctc: '35 LPA', icon: 'memory', color: 'bg-red-50 dark:bg-red-900/10 text-red-600' },
  { name: 'Qualcomm', role: 'Hardware Eng. Intern', date: 'Nov 02', ctc: '32 LPA', icon: 'developer_board', color: 'bg-blue-50 dark:bg-blue-900/10 text-blue-600' },
  { name: 'NVIDIA', role: 'ASIC Engineer', date: 'Nov 10', ctc: '40 LPA', icon: 'videogame_asset', color: 'bg-green-50 dark:bg-green-900/10 text-green-600' },
  { name: 'Samsung Semi', role: 'R&D Engineer', date: 'Nov 15', ctc: '22 LPA', icon: 'smartphone', color: 'bg-indigo-50 dark:bg-indigo-900/10 text-indigo-600' },
];

const Placements: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-background min-h-screen flex flex-col pb-safe">
      <header className="sticky top-0 z-50 glass-morphism px-4 py-4 border-b border-border">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-accent rounded-full transition-colors">
            <span className="material-symbols-outlined text-primary">arrow_back</span>
          </button>
          <h1 className="text-xl font-bold tracking-tight text-foreground">Placement Cell</h1>
        </div>
      </header>

      <main className="flex-1 px-4 py-6 space-y-6">
        {/* Hero Card */}
        <div className="bg-slate-900 rounded-2xl p-6 text-white shadow-xl relative overflow-hidden">
          <div className="relative z-10">
            <h2 className="text-2xl font-bold mb-1">Internship Season 2026</h2>
            <p className="text-slate-400 text-sm mb-4">Preparation is key. 12 companies visiting this month.</p>
            <button className="bg-primary text-primary-foreground px-4 py-2 rounded-lg text-sm font-bold flex items-center gap-2">
              <span className="material-symbols-outlined text-sm">upload_file</span> Update Resume
            </button>
          </div>
          <div className="absolute right-[-20px] bottom-[-20px] opacity-10">
            <span className="material-symbols-outlined text-9xl">business_center</span>
          </div>
        </div>

        {/* Upcoming Drives */}
        <section>
          <div className="flex items-center justify-between mb-3 px-1">
            <h3 className="text-base font-bold text-foreground">Upcoming Drives</h3>
            <span className="text-primary text-xs font-bold uppercase cursor-pointer">View Calendar</span>
          </div>
          <div className="space-y-3">
            {companies.map((company, idx) => (
              <div key={idx} className="bg-card p-4 rounded-xl border border-border flex items-center gap-4 shadow-sm hover:border-primary/20 transition-colors cursor-pointer active:scale-[0.99]">
                <div className={`size-12 rounded-lg flex items-center justify-center shrink-0 ${company.color}`}>
                  <span className="material-symbols-outlined">{company.icon}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <h4 className="font-bold text-foreground leading-tight">{company.name}</h4>
                  <p className="text-xs text-muted-foreground font-medium truncate">{company.role}</p>
                  <div className="flex items-center gap-2 mt-1.5">
                    <span className="bg-secondary px-2 py-0.5 rounded text-[10px] font-bold text-secondary-foreground">{company.ctc}</span>
                    <span className="bg-red-50 dark:bg-red-900/20 px-2 py-0.5 rounded text-[10px] font-bold text-red-600 dark:text-red-400">CGPA {'>'} 8.0</span>
                  </div>
                </div>
                <div className="flex flex-col items-end shrink-0">
                  <span className="text-sm font-bold text-foreground">{company.date.split(' ')[1]}</span>
                  <span className="text-[10px] uppercase font-bold text-muted-foreground">{company.date.split(' ')[0]}</span>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Quick Prep */}
        <section>
          <h3 className="text-base font-bold text-foreground mb-3 px-1">Quick Prep</h3>
          <div className="grid grid-cols-2 gap-3">
            {[
              { icon: 'psychology', label: 'Aptitude', sub: 'Quant & Logical', color: 'purple' },
              { icon: 'code', label: 'Coding', sub: 'DSA Sheets', color: 'orange' },
              { icon: 'memory', label: 'Core ECE', sub: 'Digital & Analog', color: 'cyan' },
              { icon: 'forum', label: 'Interview', sub: 'HR Questions', color: 'pink' },
            ].map((item, idx) => (
              <div key={idx} className={`bg-${item.color}-50 dark:bg-${item.color}-900/10 p-4 rounded-xl border border-${item.color}-100 dark:border-${item.color}-800 flex flex-col items-center text-center gap-2 cursor-pointer hover:bg-${item.color}-100 dark:hover:bg-${item.color}-900/20 transition-colors`}>
                <span className={`material-symbols-outlined text-${item.color}-600 text-3xl`}>{item.icon}</span>
                <div>
                  <h4 className={`font-bold text-sm text-${item.color}-900 dark:text-${item.color}-100`}>{item.label}</h4>
                  <p className={`text-[10px] text-${item.color}-600 dark:text-${item.color}-300`}>{item.sub}</p>
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
