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
    <div className="bg-slate-900 text-white min-h-screen flex flex-col items-center justify-between p-6 relative overflow-hidden transition-colors duration-700">
      <div className={`absolute inset-0 bg-gradient-to-br opacity-20 pointer-events-none transition-all duration-700 ${isActive ? 'from-indigo-600 to-purple-800 scale-110' : 'from-slate-800 to-slate-900 scale-100'}`}></div>

      {/* Header */}
      <div className="w-full flex justify-between items-center z-10">
        <button onClick={() => navigate(-1)} className="p-2 bg-white/10 rounded-full hover:bg-white/20 transition-colors">
          <span className="material-symbols-outlined">arrow_back</span>
        </button>
        <h2 className="font-bold tracking-widest uppercase text-sm opacity-80">Zen Mode</h2>
        <button className="p-2 bg-white/10 rounded-full hover:bg-white/20 transition-colors">
          <span className="material-symbols-outlined">settings</span>
        </button>
      </div>

      {/* Timer Display */}
      <div className="flex-1 flex flex-col items-center justify-center w-full z-10">
        <div className="relative size-72 flex items-center justify-center">
          <svg className="absolute inset-0 w-full h-full -rotate-90" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" strokeWidth="2" className="text-white/10" />
            <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" strokeWidth="2"
              strokeDasharray="283"
              strokeDashoffset={283 - (283 * progress) / 100}
              className={`transition-all duration-1000 ease-linear ${isActive ? 'text-indigo-400' : 'text-white'}`}
              strokeLinecap="round"
            />
          </svg>
          <div className="flex flex-col items-center">
            <div className="text-7xl font-light tabular-nums tracking-tight">
              {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
            </div>
            <p className="text-indigo-200 font-medium mt-2 uppercase tracking-wide text-sm">{isActive ? 'Focusing...' : 'Ready to Start'}</p>
          </div>
        </div>

        <div className="flex items-center gap-6 mt-12">
          <button onClick={resetTimer} className="p-4 rounded-full bg-white/5 text-white/70 hover:bg-white/10 hover:text-white transition-all transform hover:scale-105">
            <span className="material-symbols-outlined text-2xl">restart_alt</span>
          </button>
          <button onClick={toggleTimer} className={`size-20 rounded-full flex items-center justify-center shadow-2xl shadow-indigo-500/20 transition-all transform hover:scale-105 active:scale-95 ${isActive ? 'bg-white text-slate-900' : 'bg-indigo-500 text-white'}`}>
            <span className="material-symbols-outlined text-4xl fill-1">{isActive ? 'pause' : 'play_arrow'}</span>
          </button>
          <button className="p-4 rounded-full bg-white/5 text-white/70 hover:bg-white/10 hover:text-white transition-all transform hover:scale-105">
            <span className="material-symbols-outlined text-2xl">graphic_eq</span>
          </button>
        </div>
      </div>

      {/* Mode Selectors */}
      <div className="bg-white/10 backdrop-blur-md p-1.5 rounded-2xl flex items-center gap-1 z-10 mb-8">
        {(['focus', 'short', 'long'] as const).map(m => (
          <button key={m} onClick={() => setTimerMode(m)}
            className={`px-6 py-2 rounded-xl text-sm font-bold transition-all ${mode === m ? 'bg-white text-slate-900 shadow-lg' : 'text-white/70 hover:text-white'}`}>
            {m === 'focus' ? 'Focus' : m === 'short' ? 'Short Break' : 'Long Break'}
          </button>
        ))}
      </div>

      {/* Task Preview */}
      <div className="w-full bg-white/5 border border-white/10 rounded-xl p-4 flex items-center justify-between z-10">
        <div className="flex items-center gap-3">
          <div className="size-5 rounded-full border-2 border-indigo-400/50"></div>
          <p className="font-medium text-sm">Finish VLSI Assignment</p>
        </div>
        <span className="material-symbols-outlined text-white/40 text-sm">edit</span>
      </div>
    </div>
  );
};

export default Focus;
