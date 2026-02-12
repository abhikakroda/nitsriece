import React, { useMemo } from 'react';
import { motion } from 'framer-motion';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line, CartesianGrid, Legend } from 'recharts';
import { getAllSubjectStats, getAll as getAllAttendance } from '@/lib/attendance';

const fadeUp = {
  hidden: { opacity: 0, y: 18 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.07 } },
};

const COLORS = ['hsl(238, 85%, 72%)', 'hsl(280, 65%, 60%)', 'hsl(150, 60%, 45%)', 'hsl(30, 90%, 55%)', 'hsl(350, 65%, 55%)'];

const AttendanceAnalytics: React.FC = () => {
  const allStats = useMemo(() => getAllSubjectStats(), []);
  const records = useMemo(() => getAllAttendance(), []);

  const statEntries = Object.entries(allStats);
  const totalPresent = statEntries.reduce((a, [, s]) => a + s.present, 0);
  const totalAbsent = statEntries.reduce((a, [, s]) => a + s.absent, 0);
  const totalClasses = totalPresent + totalAbsent;
  const overallPercent = totalClasses > 0 ? Math.round((totalPresent / totalClasses) * 100) : 0;

  // Per-subject bar chart data
  const subjectBarData = statEntries.map(([subject, stats]) => ({
    name: subject.length > 12 ? subject.slice(0, 12) + '…' : subject,
    fullName: subject,
    Present: stats.present,
    Absent: stats.absent,
    percentage: stats.percentage,
  }));

  // Pie chart data
  const pieData = [
    { name: 'Present', value: totalPresent },
    { name: 'Absent', value: totalAbsent },
  ];
  const pieColors = ['hsl(150, 60%, 45%)', 'hsl(350, 65%, 55%)'];

  // Weekly trend — group by date
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
      .slice(-14) // last 14 days
      .map(([date, data]) => ({
        date: new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
        Present: data.present,
        Absent: data.absent,
        Total: data.present + data.absent,
      }));
  }, [records]);

  // Most skipped subject
  const mostSkipped = statEntries.length > 0
    ? statEntries.reduce((worst, [, stats], i, arr) => {
        const [worstName, worstStats] = worst;
        return stats.absent > worstStats.absent ? [arr[i][0], stats] as const : worst;
      }, [statEntries[0][0], statEntries[0][1]] as const)
    : null;

  const hasData = totalClasses > 0;

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      {/* Header */}
      <div className="glass-morphism flex items-center px-5 md:px-6 py-3.5 border-b border-border/40">
        <h2 className="font-display text-foreground text-[18px] md:text-[20px] font-bold tracking-tight flex-1 text-center lg:text-left">
          Attendance Analytics
        </h2>
      </div>

      <motion.div
        className="max-w-3xl mx-auto w-full px-5 md:px-6 space-y-5 pt-6 pb-6"
        variants={stagger}
        initial="hidden"
        animate="visible"
      >
        {!hasData ? (
          <motion.div variants={fadeUp} className="text-center py-16">
            <span className="material-symbols-outlined text-[48px] text-muted-foreground/30 mb-3 block">monitoring</span>
            <h3 className="font-display text-[18px] font-bold text-foreground mb-1">No attendance data yet</h3>
            <p className="text-muted-foreground text-[13px]">
              Mark attendance on the Schedule page to see your analytics here.
            </p>
          </motion.div>
        ) : (
          <>
            {/* Overview Cards */}
            <motion.div variants={fadeUp} className="grid grid-cols-3 gap-2.5">
              <div className="rounded-[14px] border border-border/50 bg-card/60 dark:bg-card/40 p-3.5">
                <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Total</p>
                <p className="font-display text-[24px] font-bold text-foreground leading-none mt-1">{totalClasses}</p>
                <p className="text-[10px] text-muted-foreground mt-0.5">Classes</p>
              </div>
              <div className="rounded-[14px] border border-border/50 bg-card/60 dark:bg-card/40 p-3.5">
                <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Present</p>
                <p className="font-display text-[24px] font-bold text-green-600 dark:text-green-400 leading-none mt-1">{totalPresent}</p>
                <p className="text-[10px] text-muted-foreground mt-0.5">Classes</p>
              </div>
              <div className="rounded-[14px] border border-border/50 bg-card/60 dark:bg-card/40 p-3.5">
                <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Overall</p>
                <p className={`font-display text-[24px] font-bold leading-none mt-1 ${overallPercent >= 75 ? 'text-green-600 dark:text-green-400' : 'text-red-500 dark:text-red-400'}`}>{overallPercent}%</p>
                <p className="text-[10px] text-muted-foreground mt-0.5">{overallPercent >= 75 ? 'On track' : 'Low'}</p>
              </div>
            </motion.div>

            {/* Alert card */}
            {mostSkipped && mostSkipped[1].absent > 0 && (
              <motion.div variants={fadeUp}>
                <div className="rounded-[14px] p-4 border bg-amber-50 dark:bg-amber-950/30 border-amber-200/60 dark:border-amber-700/30">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="material-symbols-outlined text-[18px] text-amber-600 dark:text-amber-400">warning</span>
                    <p className="text-[13px] font-bold text-amber-700 dark:text-amber-300">Most Skipped Subject</p>
                  </div>
                  <p className="text-muted-foreground text-[12px]">
                    <span className="font-semibold text-foreground">{mostSkipped[0]}</span> — {mostSkipped[1].absent} classes missed ({mostSkipped[1].percentage}% attendance)
                  </p>
                </div>
              </motion.div>
            )}

            {/* Overall Pie Chart */}
            <motion.div variants={fadeUp}>
              <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Overall Split</h3>
              <div className="bg-card card-elevated rounded-[16px] border border-border/50 p-4">
                <ResponsiveContainer width="100%" height={200}>
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={55}
                      outerRadius={80}
                      paddingAngle={4}
                      dataKey="value"
                      strokeWidth={0}
                    >
                      {pieData.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={pieColors[index]} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        background: 'hsl(var(--card))',
                        border: '1px solid hsl(var(--border))',
                        borderRadius: '12px',
                        fontSize: '12px',
                        color: 'hsl(var(--foreground))',
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
                <div className="flex justify-center gap-6 mt-2">
                  <div className="flex items-center gap-1.5">
                    <div className="w-2.5 h-2.5 rounded-full" style={{ background: pieColors[0] }} />
                    <span className="text-[11px] text-muted-foreground font-medium">Present ({totalPresent})</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <div className="w-2.5 h-2.5 rounded-full" style={{ background: pieColors[1] }} />
                    <span className="text-[11px] text-muted-foreground font-medium">Absent ({totalAbsent})</span>
                  </div>
                </div>
              </div>
            </motion.div>

            {/* Per-Subject Bar Chart */}
            <motion.div variants={fadeUp}>
              <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">By Subject</h3>
              <div className="bg-card card-elevated rounded-[16px] border border-border/50 p-4">
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={subjectBarData} barGap={2} barSize={14}>
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                    <XAxis
                      dataKey="name"
                      tick={{ fontSize: 9, fill: 'hsl(var(--muted-foreground))' }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <YAxis
                      tick={{ fontSize: 10, fill: 'hsl(var(--muted-foreground))' }}
                      axisLine={false}
                      tickLine={false}
                      allowDecimals={false}
                    />
                    <Tooltip
                      contentStyle={{
                        background: 'hsl(var(--card))',
                        border: '1px solid hsl(var(--border))',
                        borderRadius: '12px',
                        fontSize: '12px',
                        color: 'hsl(var(--foreground))',
                      }}
                    />
                    <Bar dataKey="Present" fill="hsl(150, 60%, 45%)" radius={[6, 6, 0, 0]} />
                    <Bar dataKey="Absent" fill="hsl(350, 65%, 55%)" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </motion.div>

            {/* Daily Trend Line Chart */}
            {weeklyTrend.length > 1 && (
              <motion.div variants={fadeUp}>
                <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Daily Trend</h3>
                <div className="bg-card card-elevated rounded-[16px] border border-border/50 p-4">
                  <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={weeklyTrend}>
                      <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                      <XAxis
                        dataKey="date"
                        tick={{ fontSize: 9, fill: 'hsl(var(--muted-foreground))' }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <YAxis
                        tick={{ fontSize: 10, fill: 'hsl(var(--muted-foreground))' }}
                        axisLine={false}
                        tickLine={false}
                        allowDecimals={false}
                      />
                      <Tooltip
                        contentStyle={{
                          background: 'hsl(var(--card))',
                          border: '1px solid hsl(var(--border))',
                          borderRadius: '12px',
                          fontSize: '12px',
                          color: 'hsl(var(--foreground))',
                        }}
                      />
                      <Legend
                        wrapperStyle={{ fontSize: '10px' }}
                      />
                      <Line type="monotone" dataKey="Present" stroke="hsl(150, 60%, 45%)" strokeWidth={2} dot={{ r: 3 }} />
                      <Line type="monotone" dataKey="Absent" stroke="hsl(350, 65%, 55%)" strokeWidth={2} dot={{ r: 3 }} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </motion.div>
            )}

            {/* Subject Breakdown Cards */}
            <motion.div variants={fadeUp}>
              <h3 className="text-muted-foreground text-[10px] font-bold uppercase tracking-[0.18em] px-1 mb-2">Subject Breakdown</h3>
              <div className="space-y-2">
                {statEntries.map(([subject, stats], idx) => {
                  const color = COLORS[idx % COLORS.length];
                  return (
                    <div key={subject} className="bg-card card-elevated rounded-[14px] border border-border/50 p-4">
                      <div className="flex items-center justify-between mb-2">
                        <h4 className="text-foreground text-[13px] font-bold truncate flex-1 mr-3">{subject}</h4>
                        <span className={`text-[13px] font-bold ${stats.percentage >= 75 ? 'text-green-600 dark:text-green-400' : 'text-red-500 dark:text-red-400'}`}>
                          {stats.percentage}%
                        </span>
                      </div>
                      <div className="w-full h-2 bg-secondary rounded-full overflow-hidden mb-2">
                        <div
                          className="h-full rounded-full transition-all duration-700"
                          style={{ width: `${stats.percentage}%`, background: color }}
                        />
                      </div>
                      <div className="flex items-center gap-4 text-[10px] text-muted-foreground">
                        <span>{stats.present} present</span>
                        <span>{stats.absent} absent</span>
                        <span>{stats.total} total</span>
                        {stats.percentage < 75 && (
                          <span className="text-amber-600 dark:text-amber-400 font-semibold ml-auto">⚠ Below 75%</span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </motion.div>
          </>
        )}
      </motion.div>
    </div>
  );
};

export default AttendanceAnalytics;
