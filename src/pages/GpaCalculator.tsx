import React, { useState, useMemo } from 'react';
import { motion } from 'framer-motion';

const gradePoints: Record<string, number> = {
  'O': 10, 'A+': 9, 'A': 8, 'B+': 7, 'B': 6, 'C': 5, 'F': 0,
};
const gradeOptions = Object.keys(gradePoints);

interface Subject {
  name: string;
  code: string;
  credits: number;
  icon: string;
}

const subjects: Subject[] = [
  { name: 'Computer Org & Arch', code: 'ECT 352', credits: 4, icon: 'memory' },
  { name: 'Digital Signal Processing', code: 'ECT 350', credits: 4, icon: 'graphic_eq' },
  { name: 'VLSI Design', code: 'ECT 351', credits: 4, icon: 'science' },
  { name: 'Data Comm. & Networking', code: 'ECT 353', credits: 4, icon: 'cell_tower' },
  { name: 'VLSI Technology (Elective-II)', code: 'ECT 3XX', credits: 3, icon: 'auto_stories' },
  { name: 'DSP Lab', code: 'ECL 356', credits: 2, icon: 'graphic_eq' },
  { name: 'VLSI Design Lab', code: 'ECL 357', credits: 2, icon: 'science' },
];

const fadeUp = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.05 } },
};

const GpaCalculator: React.FC = () => {
  const [grades, setGrades] = useState<Record<string, string>>({});

  const setGrade = (code: string, grade: string) => {
    setGrades(prev => ({ ...prev, [code]: grade }));
  };

  const { sgpa, totalCredits, earnedPoints, filledCount } = useMemo(() => {
    let total = 0;
    let earned = 0;
    let filled = 0;
    for (const sub of subjects) {
      const g = grades[sub.code];
      if (g && g in gradePoints) {
        total += sub.credits;
        earned += sub.credits * gradePoints[g];
        filled++;
      }
    }
    return {
      sgpa: total > 0 ? (earned / total) : 0,
      totalCredits: total,
      earnedPoints: earned,
      filledCount: filled,
    };
  }, [grades]);

  const resetAll = () => setGrades({});

  return (
    <div className="flex flex-col min-h-screen bg-background pb-20 lg:pb-6">
      {/* Header */}
      <div className="sticky top-0 z-50 glass-morphism px-5 md:px-6 py-3.5 border-b border-border/40">
        <div className="max-w-3xl mx-auto flex items-center justify-between">
          <h1 className="font-display text-[20px] md:text-[24px] font-bold text-foreground tracking-tight">GPA Estimator</h1>
          {filledCount > 0 && (
            <button onClick={resetAll} className="text-[11px] font-bold text-muted-foreground hover:text-destructive transition-colors flex items-center gap-1">
              <span className="material-symbols-outlined text-[14px]">restart_alt</span>
              Reset
            </button>
          )}
        </div>
      </div>

      <motion.div className="max-w-3xl mx-auto w-full px-5 md:px-6 pt-5" variants={stagger} initial="hidden" animate="visible">
        {/* SGPA Card */}
        <motion.div variants={fadeUp} className="mb-5">
          <div className="bg-gradient-to-br from-primary via-blue-600 to-violet-600 animate-gradient rounded-[20px] p-5 md:p-6 text-white shadow-2xl shadow-primary/20 overflow-hidden relative noise-overlay">
            <div className="absolute inset-0 animate-shimmer" />
            <div className="absolute right-[-40px] bottom-[-60px] w-64 h-64 bg-white/5 rounded-full blur-3xl" />
            <div className="relative z-10 flex items-end justify-between">
              <div>
                <p className="text-white/55 text-[10px] md:text-[11px] font-bold uppercase tracking-[0.2em] mb-1.5">Estimated SGPA</p>
                <p className="font-display text-[52px] md:text-[60px] font-bold leading-none tracking-tight">
                  {sgpa.toFixed(2)}
                </p>
              </div>
              <div className="text-right mb-2">
                <p className="text-white/55 text-[11px] font-semibold">{filledCount}/{subjects.length} subjects</p>
                <p className="text-white/40 text-[10px] mt-0.5">{totalCredits} credits • {earnedPoints} pts</p>
              </div>
            </div>
            {/* Progress bar */}
            <div className="relative z-10 mt-5 h-1.5 bg-white/10 rounded-full overflow-hidden">
              <motion.div
                className="h-full bg-white/35 rounded-full"
                initial={{ width: 0 }}
                animate={{ width: `${(sgpa / 10) * 100}%` }}
                transition={{ duration: 0.6, ease: 'easeOut' }}
              />
            </div>
          </div>
        </motion.div>

        {/* Grade scale */}
        <motion.div variants={fadeUp} className="mb-4 flex flex-wrap gap-1.5 px-0.5">
          {gradeOptions.map(g => (
            <span key={g} className="text-[10px] font-bold text-muted-foreground bg-secondary/60 px-2 py-1 rounded-lg border border-border/30">
              {g} = {gradePoints[g]}
            </span>
          ))}
        </motion.div>

        {/* Subject cards */}
        <div className="space-y-2.5 pb-6">
          {subjects.map((sub, idx) => {
            const selected = grades[sub.code];
            return (
              <motion.div key={sub.code} variants={fadeUp} custom={idx}>
                <div className={`bg-card card-elevated rounded-[16px] p-4 border transition-all duration-300 ${
                  selected ? 'border-primary/15 ring-1 ring-primary/5' : 'border-border/50'
                }`}>
                  <div className="flex items-start gap-3 mb-3">
                    <div className="size-10 rounded-[12px] bg-primary/6 text-primary flex items-center justify-center shrink-0">
                      <span className="material-symbols-outlined text-[20px]">{sub.icon}</span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="font-semibold text-foreground text-[13px] md:text-[14px] leading-tight">{sub.name}</h3>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-[10px] text-muted-foreground">{sub.code}</span>
                        <span className="text-[8px] text-muted-foreground/40">•</span>
                        <span className="text-[10px] text-primary font-bold">{sub.credits} cr</span>
                      </div>
                    </div>
                    {selected && (
                      <div className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        gradePoints[selected] >= 8 ? 'bg-green-500/10 text-green-600 dark:text-green-400' :
                        gradePoints[selected] >= 6 ? 'bg-yellow-500/10 text-yellow-600 dark:text-yellow-400' :
                        'bg-red-500/10 text-red-500 dark:text-red-400'
                      }`}>
                        {(sub.credits * gradePoints[selected])} pts
                      </div>
                    )}
                  </div>
                  {/* Grade selector */}
                  <div className="grid grid-cols-7 gap-1.5">
                    {gradeOptions.map(g => (
                      <button
                        key={g}
                        onClick={() => setGrade(sub.code, selected === g ? '' : g)}
                        className={`py-2 md:py-2.5 rounded-[10px] text-[11px] md:text-[12px] font-bold transition-all duration-200 active:scale-[0.92] ${
                          selected === g
                            ? g === 'F'
                              ? 'bg-red-500 text-white shadow-md shadow-red-500/20'
                              : 'bg-primary text-primary-foreground shadow-md shadow-primary/20'
                            : 'bg-secondary/50 text-muted-foreground hover:bg-accent hover:text-accent-foreground border border-transparent hover:border-border/30'
                        }`}
                      >
                        {g}
                      </button>
                    ))}
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      </motion.div>
    </div>
  );
};

export default GpaCalculator;
