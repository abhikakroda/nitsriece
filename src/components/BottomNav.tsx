import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const tabs = [
  { path: '/', icon: 'home', label: 'Home' },
  { path: '/schedule', icon: 'calendar_today', label: 'Schedule' },
  { path: '/mess-menu', icon: 'restaurant', label: 'Mess' },
  { path: '/analytics', icon: 'bar_chart', label: 'Analytics' },
  { path: '/profile', icon: 'settings', label: 'Settings' },
];

const BottomNav: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <>
      {/* Mobile floating bar */}
      <nav
        className="fixed bottom-5 left-1/2 -translate-x-1/2 z-50 lg:hidden"
        style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}
      >
        <div className="flex items-center gap-0.5 liquid-glass-float rounded-[24px] px-2 py-1.5">
          {tabs.map(tab => {
            const isActive = location.pathname === tab.path;
            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}
                className="relative flex items-center justify-center rounded-full transition-all duration-200 active:scale-90"
              >
                {isActive && (
                  <motion.div
                    layoutId="floatingNavPill"
                    className="absolute inset-0 rounded-[18px] bg-primary/12"
                    style={{
                      boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.5), inset 0 -0.5px 0 rgba(0,0,0,0.04)',
                    }}
                    transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                  />
                )}
                <div className="relative z-10 flex items-center gap-1.5 px-3.5 py-2">
                  <span className={`material-symbols-outlined transition-all duration-200 ${
                    isActive
                      ? 'text-[20px] text-primary fill-1'
                      : 'text-[20px] text-muted-foreground/55'
                  }`}>
                    {tab.icon}
                  </span>
                  {isActive && (
                    <motion.span
                      initial={{ opacity: 0, width: 0 }}
                      animate={{ opacity: 1, width: 'auto' }}
                      exit={{ opacity: 0, width: 0 }}
                      className="text-[11px] font-bold text-primary overflow-hidden whitespace-nowrap"
                    >
                      {tab.label}
                    </motion.span>
                  )}
                </div>
              </button>
            );
          })}
        </div>
      </nav>

      {/* Desktop sidebar */}
      <nav className="hidden lg:flex fixed left-0 top-0 bottom-0 z-50 w-[220px] flex-col liquid-glass border-r border-white/20 dark:border-white/5 py-6 px-3">
        <div className="flex items-center gap-2.5 px-3 mb-8">
          <div>
            <h2 className="font-display text-foreground text-[14px] font-bold leading-tight">Time Table</h2>
            <p className="text-muted-foreground text-[9px] font-bold tracking-widest uppercase">ECE • Sec B</p>
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
                    : 'text-muted-foreground hover:bg-secondary/50 hover:text-foreground'
                }`}
              >
                {isActive && (
                  <motion.div
                    layoutId="sidebarIndicator"
                    className="absolute inset-0 bg-primary/10 rounded-xl"
                    transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                  />
                )}
                <span className={`material-symbols-outlined text-[20px] relative z-10 ${isActive ? 'fill-1 text-primary' : ''}`}>
                  {tab.icon}
                </span>
                <span className="relative z-10">{tab.label}</span>
              </button>
            );
          })}
        </div>
        <div className="mt-auto px-3 pt-4 border-t border-border/30">
          <p className="text-muted-foreground/40 text-[10px] font-medium">NIT Srinagar • ECE Dept</p>
        </div>
      </nav>
    </>
  );
};

export default BottomNav;
