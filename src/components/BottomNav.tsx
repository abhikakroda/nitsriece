import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const tabs = [
  { path: '/', icon: 'home', label: 'Home' },
  { path: '/schedule', icon: 'calendar_today', label: 'Schedule' },
  { path: '/profile', icon: 'settings', label: 'Settings' },
];

const BottomNav: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <>
      {/* Mobile/Tablet bottom nav */}
      <nav className="fixed bottom-0 left-0 right-0 z-50 glass-morphism border-t border-border lg:hidden" style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}>
        <div className="flex items-center justify-around h-[60px] max-w-lg mx-auto">
          {tabs.map(tab => {
            const isActive = location.pathname === tab.path;
            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}
                className="relative flex flex-col items-center justify-center gap-0.5 min-w-[64px] py-1.5 transition-all duration-300"
              >
                {isActive && (
                  <motion.div
                    layoutId="bottomNavIndicator"
                    className="absolute -top-[1px] w-8 h-[3px] rounded-full bg-primary"
                    transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                  />
                )}
                <span className={`material-symbols-outlined text-[22px] transition-colors duration-200 ${isActive ? 'text-primary fill-1' : 'text-muted-foreground'}`}>{tab.icon}</span>
                <span className={`text-[10px] font-semibold leading-tight transition-colors duration-200 ${isActive ? 'text-primary' : 'text-muted-foreground'}`}>{tab.label}</span>
              </button>
            );
          })}
        </div>
      </nav>

      {/* Desktop sidebar nav */}
      <nav className="hidden lg:flex fixed left-0 top-0 bottom-0 z-50 w-[240px] flex-col bg-card/80 backdrop-blur-xl border-r border-border py-6 px-4">
        <div className="flex items-center gap-3 px-2 mb-10">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-primary via-primary to-blue-500 flex items-center justify-center shadow-lg shadow-primary/20">
            <span className="material-symbols-outlined text-white text-[20px]">school</span>
          </div>
          <div>
            <h2 className="text-foreground text-[15px] font-extrabold leading-tight tracking-tight">Campus</h2>
            <p className="text-primary text-[10px] font-bold tracking-wider uppercase">ECE • Sec B</p>
          </div>
        </div>
        <div className="space-y-1">
          {tabs.map(tab => {
            const isActive = location.pathname === tab.path;
            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}
                className={`relative flex items-center gap-3 w-full px-3 py-3 rounded-xl text-[14px] font-medium transition-all duration-200 ${
                  isActive
                    ? 'text-primary font-semibold'
                    : 'text-muted-foreground hover:bg-accent hover:text-foreground'
                }`}
              >
                {isActive && (
                  <motion.div
                    layoutId="sidebarIndicator"
                    className="absolute inset-0 bg-primary/8 border border-primary/15 rounded-xl"
                    transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                  />
                )}
                <span className={`material-symbols-outlined text-[20px] relative z-10 ${isActive ? 'fill-1' : ''}`}>{tab.icon}</span>
                <span className="relative z-10">{tab.label}</span>
              </button>
            );
          })}
        </div>
        <div className="mt-auto px-2 pt-4 border-t border-border">
          <p className="text-muted-foreground/60 text-[10px] font-medium">Campus Companion v1.0</p>
          <p className="text-muted-foreground/40 text-[9px] mt-0.5">NIT Srinagar • ECE Dept</p>
        </div>
      </nav>
    </>
  );
};

export default BottomNav;
