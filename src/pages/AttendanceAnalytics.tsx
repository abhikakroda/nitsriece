import React, { useMemo } from 'react';
import { motion } from 'framer-motion';
import { getSubjectStats, getAll as getAllAttendance } from '@/lib/attendance';
import { timetableData } from '@/lib/timetable';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.05 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 12 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.38, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

function getAttendanceColor(pct: number, hasData: boolean): string {
  if (!hasData) return 'text-muted-foreground';
  if (pct >= 75) return 'text-emerald-600 dark:text-emerald-400';
  if (pct >= 60) return 'text-amber-600 dark:text-amber-400';
  return 'text-rose-500';
}

function getBarColor(pct: number): string {
  if (pct >= 75) return 'bg-emerald-500';
  if (pct >= 60) return 'bg-amber-500';
  return 'bg-rose-500';
}

const AttendanceAnalytics: React.FC = () => {
  getAllAttendance();

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

  const atRiskCount = allSubjects.filter(s => stats[s].total > 0 && stats[s].percentage < 75).length;
  const sortedSubjects = [...allSubjects].sort((a, b) => {
    const aHas = stats[a].total > 0;
    const bHas = stats[b].total > 0;
    if (!aHas && bHas) return 1;
    if (aHas && !bHas) return -1;
    return stats[a].percentage - stats[b].percentage;
  });

  const overallColor = total === 0 ? 'text-muted-foreground' : getAttendanceColor(pct, true);
  const overallBarColor = total > 0 ? getBarColor(pct) : 'bg-muted';

  return (
    <div className="flex flex-col min-h-screen bg-background pb-24 lg:pb-6">
      {/* Header */}
      <div className="liquid-glass-nav border-b border-white/20 dark:border-white/5 px-5 md:px-6 pt-6 pb-4">
        <div className="max-w-3xl mx-auto">
          <h1 className="font-display text-[22px] md:text-[26px] font-bold text-foreground tracking-tight">Analytics</h1>
          <p className="text-muted-foreground text-[11px] mt-0.5">Attendance overview</p>
        </div>
      </div>

      <motion.div
        className="max-w-3xl mx-auto w-full px-5 md:px-6 space-y-4 pt-5 pb-8"
        variants={stagger}
        initial="hidden"
        animate="visible"
      >
        {/* Overall */}
        <motion.div variants={fadeUp} className="rounded-2xl liquid-glass p-5">
          <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-widest">Overall Attendance</p>
          <div className="flex items-end gap-3 mt-2">
            <span className={`font-display text-[42px] font-bold leading-none ${overallColor}`}>
              {total > 0 ? `${pct}%` : '—'}
            </span>
            <span className="text-muted-foreground text-[12px] mb-1.5">{totalPresent}/{total} classes</span>
          </div>
          {total > 0 && (
            <>
              <div className="w-full h-2 bg-white/15 dark:bg-white/5 rounded-full mt-3 overflow-hidden">
                <motion.div
                  className={`h-full rounded-full ${overallBarColor}`}
                  initial={{ width: 0 }}
                  animate={{ width: `${pct}%` }}
                  transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
                />
              </div>
              {pct < 75 && (
                <p className="text-[10px] text-rose-500 mt-2 flex items-center gap-1">
                  <span className="material-symbols-outlined text-[12px]">warning</span>
                  Below required 75% threshold
                </p>
              )}
            </>
          )}
        </motion.div>

        {/* Quick Stats */}
        <motion.div variants={fadeUp} className="grid grid-cols-3 gap-2">
          {[
            { label: 'Present', val: totalPresent, icon: 'check_circle', color: 'text-emerald-600 dark:text-emerald-400', bg: 'rgba(16,185,129,0.1)' },
            { label: 'Absent', val: totalAbsent, icon: 'cancel', color: 'text-rose-500', bg: 'rgba(244,63,94,0.1)' },
            { label: 'Subjects', val: allSubjects.length, icon: 'menu_book', color: 'text-primary', bg: 'rgba(99,102,241,0.1)' },
          ].map(s => (
            <div key={s.label} className="rounded-2xl liquid-glass-card p-3 text-center">
              <div className="size-7 rounded-xl flex items-center justify-center mx-auto mb-1.5" style={{ background: s.bg }}>
                <span className={`material-symbols-outlined text-[15px] ${s.color}`}>{s.icon}</span>
              </div>
              <p className="font-display text-[20px] font-bold text-foreground leading-none">{s.val}</p>
              <p className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider mt-0.5">{s.label}</p>
            </div>
          ))}
        </motion.div>

        {/* At-Risk Banner */}
        {atRiskCount > 0 && (
          <motion.div variants={fadeUp} className="rounded-2xl p-3.5 flex items-center gap-3" style={{
            background: 'linear-gradient(135deg, rgba(244,63,94,0.09) 0%, rgba(244,63,94,0.04) 100%)',
            border: '1px solid rgba(244,63,94,0.18)'
          }}>
            <span className="material-symbols-outlined text-rose-500 text-[20px] shrink-0">warning</span>
            <div>
              <p className="text-[12px] font-bold text-rose-600 dark:text-rose-400">
                {atRiskCount} subject{atRiskCount > 1 ? 's' : ''} below 75%
              </p>
              <p className="text-[10px] text-muted-foreground mt-0.5">Mark more attendance to meet the requirement</p>
            </div>
          </motion.div>
        )}

        {/* Subject List */}
        <motion.div variants={fadeUp}>
          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-1 mb-2.5">Subjects</p>
          <div className="space-y-2">
            {sortedSubjects.map(subject => {
              const s = stats[subject];
              const hasData = s.total > 0;
              const color = getAttendanceColor(s.percentage, hasData);
              const barColor = hasData ? getBarColor(s.percentage) : 'bg-muted';

              return (
                <div key={subject} className="rounded-2xl liquid-glass-card p-3.5 flex items-center gap-3">
                  {hasData && s.percentage < 75 && (
                    <span className="material-symbols-outlined text-rose-500 text-[16px] shrink-0">warning</span>
                  )}
                  {hasData && s.percentage >= 75 && (
                    <span className="material-symbols-outlined text-emerald-500 text-[16px] shrink-0">check_circle</span>
                  )}
                  {!hasData && (
                    <span className="material-symbols-outlined text-muted-foreground/30 text-[16px] shrink-0">radio_button_unchecked</span>
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="text-[12px] font-semibold text-foreground truncate">{subject}</p>
                    <div className="w-full h-1.5 bg-white/10 dark:bg-white/5 rounded-full overflow-hidden mt-1.5">
                      <motion.div
                        className={`h-full rounded-full ${barColor}`}
                        initial={{ width: 0 }}
                        animate={{ width: hasData ? `${s.percentage}%` : '0%' }}
                        transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
                      />
                    </div>
                  </div>
                  <div className="text-right shrink-0 ml-1">
                    <span className={`text-[13px] font-bold ${color}`}>
                      {hasData ? `${s.percentage}%` : '—'}
                    </span>
                    <p className="text-[9px] text-muted-foreground">{s.present}/{s.total}</p>
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
