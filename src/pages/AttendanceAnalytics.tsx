import React, { useMemo } from 'react';
import { motion } from 'framer-motion';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line, CartesianGrid, Legend } from 'recharts';
import { getSubjectStats, getAll as getAllAttendance } from '@/lib/attendance';
import { timetableData } from '@/lib/timetable';

const fadeUp = {
  hidden: { opacity: 0, y: 18 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.07 } },
};

// Vibrant color palette for subjects
const SUBJECT_COLORS: Record<string, { gradient: string; bg: string; border: string; text: string; hex: string }> = {
  'Computer Org & Arch': { gradient: 'from-orange-400 to-amber-500', bg: 'bg-orange-50 dark:bg-orange-950/40', border: 'border-orange-200/60 dark:border-orange-700/30', text: 'text-orange-700 dark:text-orange-300', hex: 'hsl(30, 90%, 55%)' },
  'VLSI Design': { gradient: 'from-purple-400 to-violet-500', bg: 'bg-purple-50 dark:bg-purple-950/40', border: 'border-purple-200/60 dark:border-purple-700/30', text: 'text-purple-700 dark:text-purple-300', hex: 'hsl(270, 70%, 60%)' },
  'VLSI Design Lab': { gradient: 'from-purple-500 to-fuchsia-500', bg: 'bg-fuchsia-50 dark:bg-fuchsia-950/40', border: 'border-fuchsia-200/60 dark:border-fuchsia-700/30', text: 'text-fuchsia-700 dark:text-fuchsia-300', hex: 'hsl(290, 70%, 60%)' },
  'Digital Signal Processing': { gradient: 'from-emerald-400 to-green-500', bg: 'bg-emerald-50 dark:bg-emerald-950/40', border: 'border-emerald-200/60 dark:border-emerald-700/30', text: 'text-emerald-700 dark:text-emerald-300', hex: 'hsl(150, 60%, 45%)' },
  'DSP Lab': { gradient: 'from-green-400 to-teal-500', bg: 'bg-teal-50 dark:bg-teal-950/40', border: 'border-teal-200/60 dark:border-teal-700/30', text: 'text-teal-700 dark:text-teal-300', hex: 'hsl(170, 60%, 42%)' },
  'Data Comm. & Networking': { gradient: 'from-pink-400 to-rose-500', bg: 'bg-rose-50 dark:bg-rose-950/40', border: 'border-rose-200/60 dark:border-rose-700/30', text: 'text-rose-700 dark:text-rose-300', hex: 'hsl(350, 70%, 58%)' },
  'VLSI Technology (Elective-II)': { gradient: 'from-indigo-400 to-blue-500', bg: 'bg-indigo-50 dark:bg-indigo-950/40', border: 'border-indigo-200/60 dark:border-indigo-700/30', text: 'text-indigo-700 dark:text-indigo-300', hex: 'hsl(238, 70%, 60%)' },
  'Open Elective': { gradient: 'from-cyan-400 to-sky-500', bg: 'bg-cyan-50 dark:bg-cyan-950/40', border: 'border-cyan-200/60 dark:border-cyan-700/30', text: 'text-cyan-700 dark:text-cyan-300', hex: 'hsl(190, 70%, 50%)' },
};

const FALLBACK_COLORS = [
  { gradient: 'from-amber-400 to-yellow-500', bg: 'bg-amber-50 dark:bg-amber-950/40', border: 'border-amber-200/60 dark:border-amber-700/30', text: 'text-amber-700 dark:text-amber-300', hex: 'hsl(40, 90%, 55%)' },
  { gradient: 'from-violet-400 to-purple-500', bg: 'bg-violet-50 dark:bg-violet-950/40', border: 'border-violet-200/60 dark:border-violet-700/30', text: 'text-violet-700 dark:text-violet-300', hex: 'hsl(260, 70%, 60%)' },
];

function getSubjectColor(subject: string, idx: number) {
  return SUBJECT_COLORS[subject] || FALLBACK_COLORS[idx % FALLBACK_COLORS.length];
}

const AttendanceAnalytics: React.FC = () => {
  const records = useMemo(() => getAllAttendance(), []);

  // Extract unique subjects from timetable
  const allSubjects = useMemo(() => {
    const subjectSet = new Set<string>();
    for (const dayClasses of Object.values(timetableData)) {
      for (const cls of dayClasses) {
        subjectSet.add(cls.subject);
      }
    }
    return Array.from(subjectSet);
  }, []);

  // Get stats for each timetable subject
  const subjectStatsMap = useMemo(() => {
    const map: Record<string, { present: number; absent: number; total: number; percentage: number }> = {};
    for (const sub of allSubjects) {
      map[sub] = getSubjectStats(sub);
    }
    return map;
  }, [allSubjects]);

  const totalPresent = allSubjects.reduce((a, s) => a + subjectStatsMap[s].present, 0);
  const totalAbsent = allSubjects.reduce((a, s) => a + subjectStatsMap[s].absent, 0);
  const totalClasses = totalPresent + totalAbsent;
  const overallPercent = totalClasses > 0 ? Math.round((totalPresent / totalClasses) * 100) : 0;

  // Per-subject bar chart data
  const subjectBarData = allSubjects.map((subject, idx) => {
    const stats = subjectStatsMap[subject];
    return {
      name: subject.length > 10 ? subject.slice(0, 10) + '…' : subject,
      fullName: subject,
      Present: stats.present,
      Absent: stats.absent,
      percentage: stats.percentage,
      fill: getSubjectColor(subject, idx).hex,
    };
  });

  // Pie chart data
  const pieData = totalClasses > 0
    ? [{ name: 'Present', value: totalPresent }, { name: 'Absent', value: totalAbsent }]
    : [{ name: 'No Data', value: 1 }];
  const pieColors = totalClasses > 0
    ? ['hsl(150, 65%, 48%)', 'hsl(350, 70%, 55%)']
    : ['hsl(var(--muted))'];

  // Daily trend
  const weeklyTrend = useMemo(() => {
    const dateMap: Record<string, { present: number; absent: number }> = {};
    for (const [key, val] of Object.entries(records)) {
      const datePart = key.split('_')[0];
      if (!dateMap[datePart]) dateMap[datePart] = { present: 0, absent: 0 };
      if (val === 'present') dateMap[datePart].present++;
      else dateMap[datePart].absent++;
    }
    return Object.entries(dateMap)
      .sort(([a], [b]) => a.localeCompare(b))
      .slice(-14)
      .map(([date, data]) => ({
        date: new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
        Present: data.present,
        Absent: data.absent,
      }));
  }, [records]);

  // Most skipped subject (only those with absences)
  const mostSkipped = useMemo(() => {
    let worst: [string, typeof subjectStatsMap[string]] | null = null;
    for (const sub of allSubjects) {
      const s = subjectStatsMap[sub];
      if (s.absent > 0 && (!worst || s.absent > worst[1].absent)) {
        worst = [sub, s];
      }
    }
    return worst;
  }, [allSubjects, subjectStatsMap]);

  const tooltipStyle = {
    background: 'hsl(var(--card))',
    border: '1px solid hsl(var(--border))',
    borderRadius: '12px',
    fontSize: '12px',
    color: 'hsl(var(--foreground))',
  };

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6 relative overflow-x-hidden">
      {/* Ambient blobs */}
      <div className="fixed top-[-8%] right-[-12%] w-[280px] h-[280px] bg-violet-500/10 dark:bg-violet-500/6 rounded-full filter blur-[100px] pointer-events-none" />
      <div className="fixed bottom-[15%] left-[-10%] w-[220px] h-[220px] bg-emerald-400/10 dark:bg-emerald-400/5 rounded-full filter blur-[100px] pointer-events-none" />

      {/* Header */}
      <div className="glass-morphism flex items-center px-5 md:px-6 py-3.5 border-b border-border/40 relative z-10">
        <h2 className="font-display text-foreground text-[18px] md:text-[20px] font-bold tracking-tight flex-1 text-center lg:text-left">
          Attendance Analytics
        </h2>
      </div>

      <motion.div
        className="max-w-3xl mx-auto w-full px-5 md:px-6 space-y-5 pt-6 pb-6 relative z-10"
        variants={stagger}
        initial="hidden"
        animate="visible"
      >
        {/* Hero Stats */}
        <motion.div variants={fadeUp}>
          <div className="bg-gradient-to-br from-indigo-500 via-purple-500 to-violet-600 rounded-[18px] p-5 md:p-6 text-white shadow-lg shadow-purple-500/15 dark:shadow-purple-500/10 overflow-hidden relative noise-overlay">
            <div className="absolute inset-0 animate-shimmer" />
            <div className="absolute right-[-20px] bottom-[-30px] w-40 h-40 bg-white/8 rounded-full blur-2xl" />
            <div className="relative z-10">
              <p className="text-white/60 text-[11px] font-bold uppercase tracking-[0.15em] mb-1">Overall Attendance</p>
              <div className="flex items-end gap-3">
                <h3 className="font-display text-[42px] md:text-[48px] font-bold leading-none">
                  {totalClasses > 0 ? `${overallPercent}%` : '—'}
                </h3>
                <div className="mb-1.5">
                  <p className="text-white/80 text-[13px] font-semibold">{totalPresent} present</p>
                  <p className="text-white/50 text-[11px]">{totalClasses} total classes tracked</p>
                </div>
              </div>
            </div>
            <div className="absolute right-4 top-4 bg-white/15 p-2 rounded-xl backdrop-blur-sm border border-white/10 z-10">
              <span className="material-symbols-outlined text-[20px]">monitoring</span>
            </div>
          </div>
        </motion.div>

        {/* Quick Stats Grid */}
        <motion.div variants={fadeUp} className="grid grid-cols-3 gap-2.5">
          <div className="rounded-[14px] border border-emerald-200/60 dark:border-emerald-700/30 bg-emerald-50 dark:bg-emerald-950/40 p-3.5">
            <p className="text-[10px] font-semibold text-emerald-600 dark:text-emerald-400 uppercase tracking-wider">Present</p>
            <p className="font-display text-[24px] font-bold text-emerald-700 dark:text-emerald-300 leading-none mt-1">{totalPresent}</p>
            <p className="text-[10px] text-emerald-600/60 dark:text-emerald-400/60 mt-0.5">Classes</p>
          </div>
          <div className="rounded-[14px] border border-rose-200/60 dark:border-rose-700/30 bg-rose-50 dark:bg-rose-950/40 p-3.5">
            <p className="text-[10px] font-semibold text-rose-600 dark:text-rose-400 uppercase tracking-wider">Absent</p>
            <p className="font-display text-[24px] font-bold text-rose-700 dark:text-rose-300 leading-none mt-1">{totalAbsent}</p>
            <p className="text-[10px] text-rose-600/60 dark:text-rose-400/60 mt-0.5">Classes</p>
          </div>
          <div className="rounded-[14px] border border-blue-200/60 dark:border-blue-700/30 bg-blue-50 dark:bg-blue-950/40 p-3.5">
            <p className="text-[10px] font-semibold text-blue-600 dark:text-blue-400 uppercase tracking-wider">Subjects</p>
            <p className="font-display text-[24px] font-bold text-blue-700 dark:text-blue-300 leading-none mt-1">{allSubjects.length}</p>
            <p className="text-[10px] text-blue-600/60 dark:text-blue-400/60 mt-0.5">Tracked</p>
          </div>
        </motion.div>

        {/* Alert card */}
        {mostSkipped && (
          <motion.div variants={fadeUp}>
            <div className="rounded-[14px] p-4 border bg-amber-50 dark:bg-amber-950/30 border-amber-200/60 dark:border-amber-700/30">
              <div className="flex items-center gap-2 mb-1">
                <span className="material-symbols-outlined text-[18px] text-amber-600 dark:text-amber-400">warning</span>
                <p className="text-[13px] font-bold text-amber-700 dark:text-amber-300">Most Skipped</p>
              </div>
              <p className="text-muted-foreground text-[12px]">
                <span className="font-semibold text-foreground">{mostSkipped[0]}</span> — {mostSkipped[1].absent} missed ({mostSkipped[1].percentage}%)
              </p>
            </div>
          </motion.div>
        )}

        {/* Pie Chart */}
        <motion.div variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Overall Split</h3>
          <div className="bg-card card-elevated rounded-[16px] border border-border/50 p-4">
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={55} outerRadius={80} paddingAngle={4} dataKey="value" strokeWidth={0}>
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={pieColors[index]} />
                  ))}
                </Pie>
                <Tooltip contentStyle={tooltipStyle} />
              </PieChart>
            </ResponsiveContainer>
            <div className="flex justify-center gap-6 mt-2">
              <div className="flex items-center gap-1.5">
                <div className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                <span className="text-[11px] text-muted-foreground font-medium">Present ({totalPresent})</span>
              </div>
              <div className="flex items-center gap-1.5">
                <div className="w-2.5 h-2.5 rounded-full bg-rose-500" />
                <span className="text-[11px] text-muted-foreground font-medium">Absent ({totalAbsent})</span>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Per-Subject Bar Chart */}
        {totalClasses > 0 && (
          <motion.div variants={fadeUp}>
            <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">By Subject</h3>
            <div className="bg-card card-elevated rounded-[16px] border border-border/50 p-4">
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={subjectBarData} barGap={2} barSize={12}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                  <XAxis dataKey="name" tick={{ fontSize: 8, fill: 'hsl(var(--muted-foreground))' }} axisLine={false} tickLine={false} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis tick={{ fontSize: 10, fill: 'hsl(var(--muted-foreground))' }} axisLine={false} tickLine={false} allowDecimals={false} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Bar dataKey="Present" fill="hsl(150, 65%, 48%)" radius={[6, 6, 0, 0]} />
                  <Bar dataKey="Absent" fill="hsl(350, 70%, 55%)" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </motion.div>
        )}

        {/* Daily Trend */}
        {weeklyTrend.length > 1 && (
          <motion.div variants={fadeUp}>
            <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Daily Trend</h3>
            <div className="bg-card card-elevated rounded-[16px] border border-border/50 p-4">
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={weeklyTrend}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                  <XAxis dataKey="date" tick={{ fontSize: 9, fill: 'hsl(var(--muted-foreground))' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 10, fill: 'hsl(var(--muted-foreground))' }} axisLine={false} tickLine={false} allowDecimals={false} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Legend wrapperStyle={{ fontSize: '10px' }} />
                  <Line type="monotone" dataKey="Present" stroke="hsl(150, 65%, 48%)" strokeWidth={2.5} dot={{ r: 3, fill: 'hsl(150, 65%, 48%)' }} />
                  <Line type="monotone" dataKey="Absent" stroke="hsl(350, 70%, 55%)" strokeWidth={2.5} dot={{ r: 3, fill: 'hsl(350, 70%, 55%)' }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </motion.div>
        )}

        {/* Subject Breakdown Cards */}
        <motion.div variants={fadeUp}>
          <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Subject Breakdown</h3>
          <div className="space-y-2.5">
            {allSubjects.map((subject, idx) => {
              const stats = subjectStatsMap[subject];
              const color = getSubjectColor(subject, idx);
              const hasData = stats.total > 0;
              return (
                <div key={subject} className={`rounded-[16px] border p-4 ${color.bg} ${color.border}`}>
                  <div className="flex items-center justify-between mb-2.5">
                    <div className="flex items-center gap-2.5 flex-1 min-w-0">
                      <div className={`w-8 h-8 rounded-[10px] bg-gradient-to-br ${color.gradient} flex items-center justify-center shrink-0`}>
                        <span className="material-symbols-outlined text-white text-[16px]">school</span>
                      </div>
                      <h4 className={`text-[13px] font-bold truncate ${color.text}`}>{subject}</h4>
                    </div>
                    <span className={`text-[15px] font-bold ${
                      !hasData ? 'text-muted-foreground' :
                      stats.percentage >= 75 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-500 dark:text-rose-400'
                    }`}>
                      {hasData ? `${stats.percentage}%` : '—'}
                    </span>
                  </div>
                  <div className="w-full h-2 bg-black/5 dark:bg-white/5 rounded-full overflow-hidden mb-2">
                    <div
                      className={`h-full rounded-full transition-all duration-700 bg-gradient-to-r ${color.gradient}`}
                      style={{ width: hasData ? `${stats.percentage}%` : '0%' }}
                    />
                  </div>
                  <div className="flex items-center gap-4 text-[10px] text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                      {stats.present} present
                    </span>
                    <span className="flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-rose-500" />
                      {stats.absent} absent
                    </span>
                    <span>{stats.total} total</span>
                    {hasData && stats.percentage < 75 && (
                      <span className="text-amber-600 dark:text-amber-400 font-semibold ml-auto">⚠ Below 75%</span>
                    )}
                    {!hasData && (
                      <span className="text-muted-foreground/50 font-medium ml-auto">No data yet</span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </motion.div>
      </motion.div>
    </div>
  );
};

export default AttendanceAnalytics;
