import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Focus: React.FC = () => {
  const navigate = useNavigate();
  const [isActive, setIsActive] = useState(false);
  const [minutes, setMinutes] = useState(25);
  const [seconds, setSeconds] = useState(0);
  const [mode, setMode] = useState<'focus' | 'short' | 'long'>('focus');

  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | null = null;
    if (isActive) {
      interval = setInterval(() => {
        if (seconds === 0) {
          if (minutes === 0) {
            setIsActive(false);
          } else {
            setMinutes(m => m - 1);
            setSeconds(59);
          }
        } else {
          setSeconds(s => s - 1);
        }
      }, 1000);
    }
    return () => { if (interval) clearInterval(interval); };
  }, [isActive, minutes, seconds]);

  const toggleTimer = () => setIsActive(!isActive);

  const resetTimer = () => {
    setIsActive(false);
    setMinutes(mode === 'focus' ? 25 : mode === 'short' ? 5 : 15);
    setSeconds(0);
  };

  const setTimerMode = (newMode: 'focus' | 'short' | 'long') => {
    setMode(newMode);
    setIsActive(false);
    setMinutes(newMode === 'focus' ? 25 : newMode === 'short' ? 5 : 15);
    setSeconds(0);
  };

  const totalSeconds = (mode === 'focus' ? 25 : mode === 'short' ? 5 : 15) * 60;
  const progress = (totalSeconds - (minutes * 60 + seconds)) / totalSeconds * 100;

  return (
    <div className="bg-slate-900 text-white min-h-screen flex flex-col items-center justify-between p-5 relative overflow-hidden">
      <div className={`absolute inset-0 bg-gradient-to-br opacity-20 pointer-events-none transition-all duration-700 ${isActive ? 'from-indigo-600 to-purple-800 scale-110' : 'from-slate-800 to-slate-900 scale-100'}`} />

      {/* Header */}
      <div className="w-full flex justify-between items-center z-10">
        <button onClick={() => navigate('/')} className="p-2 bg-white/10 rounded-full active:bg-white/20 transition-colors">
          <span className="material-symbols-outlined text-[22px]">arrow_back</span>
        </button>
        <h2 className="font-bold tracking-widest uppercase text-[12px] opacity-80">Zen Mode</h2>
        <button className="p-2 bg-white/10 rounded-full active:bg-white/20 transition-colors">
          <span className="material-symbols-outlined text-[22px]">settings</span>
        </button>
      </div>

      {/* Timer */}
      <div className="flex-1 flex flex-col items-center justify-center w-full z-10">
        <div className="relative w-64 h-64 flex items-center justify-center">
          <svg className="absolute inset-0 w-full h-full -rotate-90" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" strokeWidth="2" className="text-white/10" />
            <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" strokeWidth="2.5"
              strokeDasharray="283"
              strokeDashoffset={283 - (283 * progress) / 100}
              className={`transition-all duration-1000 ease-linear ${isActive ? 'text-indigo-400' : 'text-white/60'}`}
              strokeLinecap="round"
            />
          </svg>
          <div className="flex flex-col items-center">
            <div className="text-6xl font-light tabular-nums tracking-tight">
              {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
            </div>
            <p className="text-indigo-200 font-medium mt-2 uppercase tracking-wide text-[12px]">{isActive ? 'Focusing...' : 'Ready to Start'}</p>
          </div>
        </div>

        <div className="flex items-center gap-6 mt-10">
          <button onClick={resetTimer} className="p-3.5 rounded-full bg-white/5 text-white/70 active:bg-white/10 transition-all">
            <span className="material-symbols-outlined text-[24px]">restart_alt</span>
          </button>
          <button onClick={toggleTimer} className={`size-[72px] rounded-full flex items-center justify-center shadow-2xl shadow-indigo-500/20 transition-all active:scale-95 ${isActive ? 'bg-white text-slate-900' : 'bg-indigo-500 text-white'}`}>
            <span className="material-symbols-outlined text-[36px] fill-1">{isActive ? 'pause' : 'play_arrow'}</span>
          </button>
          <button className="p-3.5 rounded-full bg-white/5 text-white/70 active:bg-white/10 transition-all">
            <span className="material-symbols-outlined text-[24px]">graphic_eq</span>
          </button>
        </div>
      </div>

      {/* Modes */}
      <div className="bg-white/10 backdrop-blur-md p-1 rounded-2xl flex items-center gap-0.5 z-10 mb-6">
        {(['focus', 'short', 'long'] as const).map(m => (
          <button key={m} onClick={() => setTimerMode(m)}
            className={`px-4 py-2 rounded-xl text-[12px] font-bold transition-all ${mode === m ? 'bg-white text-slate-900 shadow-lg' : 'text-white/60 active:text-white'}`}>
            {m === 'focus' ? 'Focus' : m === 'short' ? 'Short' : 'Long'}
          </button>
        ))}
      </div>

      {/* Task */}
      <div className="w-full bg-white/5 border border-white/10 rounded-xl p-3.5 flex items-center justify-between z-10">
        <div className="flex items-center gap-3">
          <div className="size-4 rounded-full border-2 border-indigo-400/50" />
          <p className="font-medium text-[13px]">Finish VLSI Assignment</p>
        </div>
        <span className="material-symbols-outlined text-white/40 text-[16px]">edit</span>
      </div>
    </div>
  );
};

export default Focus;
