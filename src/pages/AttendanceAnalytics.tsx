import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, CartesianGrid } from 'recharts';
import { getSubjectStats, getAll as getAllAttendance } from '@/lib/attendance';
import { timetableData } from '@/lib/timetable';

const SUBJECT_COLORS: Record<string, { bg: string; border: string; text: string; hex: string }> = {
  'Computer Org & Arch': { bg: 'bg-orange-50 dark:bg-orange-950/30', border: 'border-orange-200/50 dark:border-orange-800/30', text: 'text-orange-700 dark:text-orange-300', hex: '#f97316' },
  'VLSI Design': { bg: 'bg-purple-50 dark:bg-purple-950/30', border: 'border-purple-200/50 dark:border-purple-800/30', text: 'text-purple-700 dark:text-purple-300', hex: '#a855f7' },
  'VLSI Design Lab': { bg: 'bg-fuchsia-50 dark:bg-fuchsia-950/30', border: 'border-fuchsia-200/50 dark:border-fuchsia-800/30', text: 'text-fuchsia-700 dark:text-fuchsia-300', hex: '#d946ef' },
  'Digital Signal Processing': { bg: 'bg-emerald-50 dark:bg-emerald-950/30', border: 'border-emerald-200/50 dark:border-emerald-800/30', text: 'text-emerald-700 dark:text-emerald-300', hex: '#10b981' },
  'DSP Lab': { bg: 'bg-teal-50 dark:bg-teal-950/30', border: 'border-teal-200/50 dark:border-teal-800/30', text: 'text-teal-700 dark:text-teal-300', hex: '#14b8a6' },
  'Data Comm. & Networking': { bg: 'bg-rose-50 dark:bg-rose-950/30', border: 'border-rose-200/50 dark:border-rose-800/30', text: 'text-rose-700 dark:text-rose-300', hex: '#f43f5e' },
  'VLSI Technology (Elective-II)': { bg: 'bg-indigo-50 dark:bg-indigo-950/30', border: 'border-indigo-200/50 dark:border-indigo-800/30', text: 'text-indigo-700 dark:text-indigo-300', hex: '#6366f1' },
  'Open Elective': { bg: 'bg-cyan-50 dark:bg-cyan-950/30', border: 'border-cyan-200/50 dark:border-cyan-800/30', text: 'text-cyan-700 dark:text-cyan-300', hex: '#06b6d4' },
};

const FALLBACK_HEX = ['#eab308', '#8b5cf6', '#ec4899', '#0ea5e9'];

function getColor(subject: string, idx: number) {
  return SUBJECT_COLORS[subject] || {
    bg: 'bg-slate-50 dark:bg-slate-950/30',
    border: 'border-slate-200/50 dark:border-slate-800/30',
    text: 'text-slate-700 dark:text-slate-300',
    hex: FALLBACK_HEX[idx % FALLBACK_HEX.length],
  };
}

const tooltipStyle = {
  background: 'hsl(var(--card))',
  border: '1px solid hsl(var(--border))',
  borderRadius: '10px',
  fontSize: '11px',
  color: 'hsl(var(--foreground))',
};

const AttendanceAnalytics: React.FC = () => {
  const records = useMemo(() => getAllAttendance(), []);

  const allSubjects = useMemo(() => {
    const set = new Set<string>();
    for (const classes of Object.values(timetableData)) {
      for (const cls of classes) set.add(cls.subject);
    }
    return Array.from(set);
  }, []);

  const stats = useMemo(() => {
    const map: Record<string, ReturnType<typeof getSubjectStats>> = {};
    for (const s of allSubjects) map[s] = getSubjectStats(s);
    return map;
  }, [allSubjects]);

  const totalPresent = allSubjects.reduce((a, s) => a + stats[s].present, 0);
  const totalAbsent = allSubjects.reduce((a, s) => a + stats[s].absent, 0);
  const total = totalPresent + totalAbsent;
  const pct = total > 0 ? Math.round((totalPresent / total) * 100) : 0;

  const barData = allSubjects.map((s, i) => ({
    name: s.length > 12 ? s.slice(0, 12) + '…' : s,
    Present: stats[s].present,
    Absent: stats[s].absent,
    fill: getColor(s, i).hex,
  }));

  const pieData = total > 0
    ? [{ name: 'Present', value: totalPresent }, { name: 'Absent', value: totalAbsent }]
    : [{ name: 'No Data', value: 1 }];

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      {/* Header */}
      <div className="glass-morphism flex items-center px-5 py-3.5 border-b border-border/40">
        <h2 className="font-display text-foreground text-[18px] md:text-[20px] font-bold tracking-tight flex-1 text-center lg:text-left">
          Attendance Analytics
        </h2>
      </div>

      <div className="max-w-3xl mx-auto w-full px-5 space-y-4 pt-5 pb-6">
        {/* Hero */}
        <div className="bg-gradient-to-br from-indigo-500 via-purple-500 to-violet-600 rounded-2xl p-5 text-white shadow-lg overflow-hidden relative">
          <div className="absolute right-[-20px] bottom-[-20px] w-32 h-32 bg-white/10 rounded-full blur-2xl" />
          <p className="text-white/60 text-[10px] font-bold uppercase tracking-widest mb-0.5">Overall</p>
          <div className="flex items-end gap-3">
            <span className="font-display text-[40px] font-bold leading-none">{total > 0 ? `${pct}%` : '—'}</span>
            <span className="text-white/70 text-[12px] mb-1">{totalPresent}/{total} classes</span>
          </div>
        </div>

        {/* Quick stats */}
        <div className="grid grid-cols-3 gap-2">
          {[
            { label: 'Present', val: totalPresent, c: 'emerald' },
            { label: 'Absent', val: totalAbsent, c: 'rose' },
            { label: 'Subjects', val: allSubjects.length, c: 'blue' },
          ].map(s => (
            <div key={s.label} className={`rounded-xl border border-${s.c}-200/50 dark:border-${s.c}-800/30 bg-${s.c}-50 dark:bg-${s.c}-950/30 p-3 text-center`}>
              <p className={`text-[10px] font-semibold text-${s.c}-600 dark:text-${s.c}-400 uppercase tracking-wider`}>{s.label}</p>
              <p className={`font-display text-[22px] font-bold text-${s.c}-700 dark:text-${s.c}-300 leading-none mt-0.5`}>{s.val}</p>
            </div>
          ))}
        </div>

        {/* Pie + Bar side by side on larger screens */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div className="bg-card rounded-xl border border-border/50 p-3">
            <p className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest mb-2">Split</p>
            <ResponsiveContainer width="100%" height={150}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={40} outerRadius={60} paddingAngle={3} dataKey="value" strokeWidth={0}>
                  <Cell fill="#10b981" />
                  <Cell fill="#f43f5e" />
                </Pie>
                <Tooltip contentStyle={tooltipStyle} />
              </PieChart>
            </ResponsiveContainer>
            <div className="flex justify-center gap-4 mt-1">
              <span className="flex items-center gap-1 text-[10px] text-muted-foreground"><span className="w-2 h-2 rounded-full bg-emerald-500" />Present</span>
              <span className="flex items-center gap-1 text-[10px] text-muted-foreground"><span className="w-2 h-2 rounded-full bg-rose-500" />Absent</span>
            </div>
          </div>

          {total > 0 && (
            <div className="bg-card rounded-xl border border-border/50 p-3">
              <p className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest mb-2">By Subject</p>
              <ResponsiveContainer width="100%" height={150}>
                <BarChart data={barData} barGap={1} barSize={10}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                  <XAxis dataKey="name" tick={{ fontSize: 7 }} axisLine={false} tickLine={false} interval={0} angle={-15} textAnchor="end" height={40} />
                  <YAxis tick={{ fontSize: 9 }} axisLine={false} tickLine={false} allowDecimals={false} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Bar dataKey="Present" fill="#10b981" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Absent" fill="#f43f5e" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Subject list — compact */}
        <div>
          <p className="text-muted-foreground text-[10px] font-bold uppercase tracking-widest px-1 mb-2">Subjects</p>
          <div className="space-y-1.5">
            {allSubjects.map((subject, idx) => {
              const s = stats[subject];
              const color = getColor(subject, idx);
              const hasData = s.total > 0;
              return (
                <div key={subject} className={`rounded-xl border p-3 ${color.bg} ${color.border} flex items-center gap-3`}>
                  <div className="w-7 h-7 rounded-lg flex items-center justify-center shrink-0" style={{ background: color.hex }}>
                    <span className="material-symbols-outlined text-white text-[14px]">school</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className={`text-[12px] font-bold truncate ${color.text}`}>{subject}</p>
                    <div className="w-full h-1.5 bg-black/5 dark:bg-white/5 rounded-full overflow-hidden mt-1">
                      <div className="h-full rounded-full" style={{ width: hasData ? `${s.percentage}%` : '0%', background: color.hex, transition: 'width 0.3s' }} />
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <span className={`text-[13px] font-bold ${!hasData ? 'text-muted-foreground' : s.percentage >= 75 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-500'}`}>
                      {hasData ? `${s.percentage}%` : '—'}
                    </span>
                    <p className="text-[9px] text-muted-foreground">{s.present}/{s.total}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AttendanceAnalytics;
