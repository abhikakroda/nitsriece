import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const tabs = [
  { path: '/', icon: 'home', label: 'Home' },
  { path: '/focus', icon: 'timer', label: 'Focus' },
  { path: '/placements', icon: 'trending_up', label: 'Stats' },
  { path: '/profile', icon: 'person', label: 'Profile' },
];

const BottomNav: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Hide on focus mode for immersive experience
  if (location.pathname === '/focus') return null;

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 glass-morphism border-t border-border">
      <div className="flex items-center justify-around h-16 max-w-lg mx-auto" style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}>
        {tabs.map(tab => {
          const isActive = location.pathname === tab.path;
          return (
            <button
              key={tab.path}
              onClick={() => navigate(tab.path)}
              className={`flex flex-col items-center justify-center gap-0.5 w-16 py-1 transition-all ${
                isActive ? 'text-primary scale-105' : 'text-muted-foreground'
              }`}
            >
              <span className={`material-symbols-outlined text-2xl ${isActive ? 'fill-1' : ''}`}>{tab.icon}</span>
              <span className="text-[10px] font-bold">{tab.label}</span>
              {isActive && <div className="w-1 h-1 rounded-full bg-primary mt-0.5"></div>}
            </button>
          );
        })}
      </div>
    </nav>
  );
};

export default BottomNav;
