import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const tabs = [
  { path: '/', icon: 'home', iconFilled: 'home', label: 'Home' },
  { path: '/focus', icon: 'timer', iconFilled: 'timer', label: 'Focus' },
  { path: '/placements', icon: 'trending_up', iconFilled: 'trending_up', label: 'Stats' },
  { path: '/profile', icon: 'person', iconFilled: 'person', label: 'Profile' },
];

const BottomNav: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  if (location.pathname === '/focus') return null;

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 glass-morphism border-t border-border" style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}>
      <div className="flex items-center justify-around h-16 max-w-md mx-auto">
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
  );
};

export default BottomNav;
