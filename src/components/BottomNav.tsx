import React, { forwardRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const tabs = [
  { path: '/', icon: 'home', label: 'Home' },
  { path: '/schedule', icon: 'calendar_today', label: 'Schedule' },
  { path: '/gpa', icon: 'calculate', label: 'GPA' },
  { path: '/profile', icon: 'settings', label: 'Settings' },
];

const BottomNav = forwardRef<HTMLDivElement>((_, ref) => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <div ref={ref}>
      {/* Mobile bottom nav */}
      <nav className="fixed bottom-0 left-0 right-0 z-50 glass-morphism border-t border-border/50 lg:hidden" style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}>
        <div className="flex items-center justify-around h-[56px] max-w-lg mx-auto">
          {tabs.map(tab => {
            const isActive = location.pathname === tab.path;
            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}
                className="relative flex flex-col items-center justify-center gap-[2px] min-w-[56px] py-1 transition-all duration-200"
              >
                {isActive && (
                  <motion.div
                    layoutId="bottomNavIndicator"
                    className="absolute -top-[1px] w-6 h-[2.5px] rounded-full bg-primary"
                    transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                  />
                )}
                <span className={`material-symbols-outlined text-[22px] transition-all duration-200 ${isActive ? 'text-primary fill-1 scale-105' : 'text-muted-foreground'}`}>{tab.icon}</span>
                <span className={`text-[9px] font-semibold leading-tight transition-colors duration-200 ${isActive ? 'text-primary' : 'text-muted-foreground/70'}`}>{tab.label}</span>
              </button>
            );
          })}
        </div>
      </nav>

      {/* Desktop sidebar */}
      <nav className="hidden lg:flex fixed left-0 top-0 bottom-0 z-50 w-[220px] flex-col bg-card/60 backdrop-blur-2xl border-r border-border/50 py-6 px-3">
        <div className="flex items-center gap-2.5 px-3 mb-10">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-primary to-blue-500 flex items-center justify-center shadow-md shadow-primary/20">
            <span className="material-symbols-outlined text-white text-[18px]">school</span>
          </div>
          <div>
            <h2 className="font-display text-foreground text-[14px] font-bold leading-tight">Campus</h2>
            <p className="text-primary text-[9px] font-bold tracking-widest uppercase">ECE • Sec B</p>
          </div>
        </div>
        <div className="space-y-0.5">
          {tabs.map(tab => {
            const isActive = location.pathname === tab.path;
            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}
                className={`relative flex items-center gap-2.5 w-full px-3 py-2.5 rounded-xl text-[13px] font-medium transition-all duration-200 ${
                  isActive
                    ? 'text-primary font-semibold'
                    : 'text-muted-foreground hover:bg-accent/50 hover:text-foreground'
                }`}
              >
                {isActive && (
                  <motion.div
                    layoutId="sidebarIndicator"
                    className="absolute inset-0 bg-primary/8 border border-primary/12 rounded-xl"
                    transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                  />
                )}
                <span className={`material-symbols-outlined text-[20px] relative z-10 ${isActive ? 'fill-1' : ''}`}>{tab.icon}</span>
                <span className="relative z-10">{tab.label}</span>
              </button>
            );
          })}
        </div>
        <div className="mt-auto px-3 pt-4 border-t border-border/50">
          <p className="text-muted-foreground/50 text-[10px] font-medium">Campus Companion v1.0</p>
          <p className="text-muted-foreground/30 text-[9px] mt-0.5">NIT Srinagar • ECE Dept</p>
        </div>
      </nav>
    </div>
  );
});

BottomNav.displayName = 'BottomNav';

export default BottomNav;
