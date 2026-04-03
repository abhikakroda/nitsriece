import { useEffect, useState } from 'react';

export function useMinimalistMode() {
  const [isMinimalist, setIsMinimalist] = useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('minimalist-mode') === 'true';
    }
    return false;
  });

  useEffect(() => {
    const root = document.documentElement;
    if (isMinimalist) {
      root.classList.add('minimalist');
    } else {
      root.classList.remove('minimalist');
    }
    localStorage.setItem('minimalist-mode', String(isMinimalist));
  }, [isMinimalist]);

  const toggleMinimalistMode = () => setIsMinimalist(prev => !prev);

  return { isMinimalist, setIsMinimalist, toggleMinimalistMode };
}
