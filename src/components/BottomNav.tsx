import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

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
        <div className="flex items-center justify-around h-16 max-w-lg mx-auto">
          {tabs.map(tab => {
            const isActive = location.pathname === tab.path;
            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}
                className={`flex flex-col items-center justify-center gap-0.5 min-w-[64px] py-1.5 transition-all duration-200 ${
                  isActive ? 'text-primary' : 'text-muted-foreground'
                }`}
              >
                <span className={`material-symbols-outlined text-[22px] ${isActive ? 'fill-1' : ''}`}>{tab.icon}</span>
                <span className="text-[10px] font-semibold leading-tight">{tab.label}</span>
              </button>
            );
          })}
        </div>
      </nav>

      {/* Desktop sidebar nav */}
      <nav className="hidden lg:flex fixed left-0 top-0 bottom-0 z-50 w-[220px] flex-col bg-card border-r border-border py-6 px-3">
        <div className="flex items-center gap-3 px-3 mb-8">
          <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary to-blue-600 flex items-center justify-center shadow-md">
            <span className="material-symbols-outlined text-white text-[18px]">school</span>
          </div>
          <div>
            <h2 className="text-foreground text-[14px] font-bold leading-tight">Campus</h2>
            <p className="text-primary text-[10px] font-bold">ECE • Sec B</p>
          </div>
        </div>
        <div className="space-y-1">
          {tabs.map(tab => {
            const isActive = location.pathname === tab.path;
            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}
                className={`flex items-center gap-3 w-full px-3 py-2.5 rounded-xl text-[14px] font-medium transition-all duration-200 ${
                  isActive
                    ? 'bg-primary/10 text-primary font-semibold'
                    : 'text-muted-foreground hover:bg-accent hover:text-foreground'
                }`}
              >
                <span className={`material-symbols-outlined text-[20px] ${isActive ? 'fill-1' : ''}`}>{tab.icon}</span>
                {tab.label}
              </button>
            );
          })}
        </div>
        <div className="mt-auto px-3">
          <p className="text-muted-foreground text-[10px]">Campus Companion v1.0</p>
        </div>
      </nav>
    </>
  );
};

export default BottomNav;
