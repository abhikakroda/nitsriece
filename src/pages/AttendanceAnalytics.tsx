import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { getSubjectStats, getAll as getAllAttendance } from '@/lib/attendance';
import { timetableData } from '@/lib/timetable';

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

  const barData = allSubjects.map(s => ({
    name: s.length > 10 ? s.slice(0, 10) + '…' : s,
    Present: stats[s].present,
    Absent: stats[s].absent,
  }));

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      {/* Header */}
      <div className="bg-background/80 backdrop-blur-xl flex items-center px-5 py-3.5 border-b border-border/30">
        <h2 className="font-display text-foreground text-[18px] md:text-[20px] font-bold tracking-tight flex-1 text-center lg:text-left">
          Analytics
        </h2>
      </div>

      <div className="max-w-3xl mx-auto w-full px-5 space-y-4 pt-5 pb-6">
        {/* Overall */}
        <div className="rounded-2xl bg-card border border-border/40 p-5">
          <p className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest">Overall Attendance</p>
          <div className="flex items-end gap-3 mt-1">
            <span className="font-display text-[38px] font-bold text-foreground leading-none">{total > 0 ? `${pct}%` : '—'}</span>
            <span className="text-muted-foreground text-[12px] mb-1">{totalPresent}/{total} classes</span>
          </div>
          {total > 0 && (
            <div className="w-full h-1.5 bg-secondary rounded-full mt-3 overflow-hidden">
              <div className="h-full bg-foreground rounded-full transition-all" style={{ width: `${pct}%` }} />
            </div>
          )}
        </div>

        {/* Quick stats */}
        <div className="grid grid-cols-3 gap-2">
          {[
            { label: 'Present', val: totalPresent },
            { label: 'Absent', val: totalAbsent },
            { label: 'Subjects', val: allSubjects.length },
          ].map(s => (
            <div key={s.label} className="rounded-2xl bg-card border border-border/40 p-3 text-center">
              <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">{s.label}</p>
              <p className="font-display text-[20px] font-bold text-foreground leading-none mt-1">{s.val}</p>
            </div>
          ))}
        </div>

        {/* Chart */}
        {total > 0 && (
          <div className="rounded-2xl bg-card border border-border/40 p-4">
            <p className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest mb-3">By Subject</p>
            <ResponsiveContainer width="100%" height={160}>
              <BarChart data={barData} barGap={2} barSize={8}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 8 }} axisLine={false} tickLine={false} interval={0} angle={-15} textAnchor="end" height={40} />
                <YAxis tick={{ fontSize: 9 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip contentStyle={tooltipStyle} />
                <Bar dataKey="Present" fill="hsl(var(--foreground))" radius={[3, 3, 0, 0]} />
                <Bar dataKey="Absent" fill="hsl(var(--border))" radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Subject list */}
        <div>
          <p className="text-muted-foreground text-[10px] font-medium uppercase tracking-widest px-1 mb-2">Subjects</p>
          <div className="space-y-1.5">
            {allSubjects.map(subject => {
              const s = stats[subject];
              const hasData = s.total > 0;
              return (
                <div key={subject} className="rounded-2xl bg-card border border-border/40 p-3 flex items-center gap-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-[12px] font-semibold text-foreground truncate">{subject}</p>
                    <div className="w-full h-1 bg-secondary rounded-full overflow-hidden mt-1.5">
                      <div
                        className="h-full rounded-full bg-foreground transition-all"
                        style={{ width: hasData ? `${s.percentage}%` : '0%' }}
                      />
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <span className={`text-[13px] font-bold ${!hasData ? 'text-muted-foreground' : s.percentage >= 75 ? 'text-foreground' : 'text-rose-500'}`}>
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
