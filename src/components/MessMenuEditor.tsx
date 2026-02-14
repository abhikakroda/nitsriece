import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { getMessMenuData, saveMessMenuData, resetMessMenuData, fullDays, fullDayNames, type FullDay, type Meal } from '@/lib/messMenu';

const MessMenuEditor: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const [data, setData] = useState<Record<FullDay, Meal[]>>(getMessMenuData);
  const [activeDay, setActiveDay] = useState<FullDay>('Mon');
  const [editing, setEditing] = useState<{ day: FullDay; mealIdx: number } | null>(null);

  const meals = data[activeDay];

  const updateMealItems = (mealIdx: number, field: 'items' | 'nonVegItems', value: string) => {
    setData(prev => {
      const copy = { ...prev };
      copy[activeDay] = copy[activeDay].map((m, i) => {
        if (i !== mealIdx) return m;
        const items = value.split(',').map(s => s.trim()).filter(Boolean);
        if (field === 'nonVegItems' && items.length === 0) {
          const { nonVegItems, ...rest } = m;
          return rest as Meal;
        }
        return { ...m, [field]: items };
      });
      return copy;
    });
  };

  const handleSave = () => {
    saveMessMenuData(data);
    onClose();
  };

  const handleReset = () => {
    resetMessMenuData();
    setData(getMessMenuData());
    setEditing(null);
  };

  return (
    <div className="space-y-4">
      {/* Day tabs */}
      <div className="flex gap-1">
        {fullDays.map(d => (
          <button
            key={d}
            onClick={() => { setActiveDay(d); setEditing(null); }}
            className={`flex-1 py-2 rounded-xl text-[10px] font-bold transition-colors ${
              activeDay === d ? 'bg-foreground text-background' : 'bg-white/20 dark:bg-white/5 text-muted-foreground'
            }`}
          >
            {d}
          </button>
        ))}
      </div>

      <p className="text-[11px] text-muted-foreground">{fullDayNames[activeDay]} · {meals.length} meals</p>

      {/* Meal list */}
      <div className="space-y-2">
        <AnimatePresence mode="popLayout">
          {meals.map((meal, idx) => {
            const isEditing = editing?.day === activeDay && editing?.mealIdx === idx;
            return (
              <motion.div
                key={`${activeDay}-${idx}`}
                layout
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.95 }}
                className="liquid-glass-card rounded-2xl overflow-hidden"
              >
                <button
                  onClick={() => setEditing(isEditing ? null : { day: activeDay, mealIdx: idx })}
                  className="w-full flex items-center gap-3 px-4 py-3 text-left"
                >
                  <span className="text-[16px]">{meal.emoji}</span>
                  <div className="flex-1 min-w-0">
                    <p className="text-[13px] font-semibold text-foreground">{meal.label}</p>
                    <p className="text-[10px] text-muted-foreground mt-0.5 truncate">{meal.items.slice(0, 3).join(', ')}…</p>
                  </div>
                  <span className={`material-symbols-outlined text-[16px] text-muted-foreground transition-transform ${isEditing ? 'rotate-180' : ''}`}>
                    expand_more
                  </span>
                </button>

                <AnimatePresence>
                  {isEditing && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: 'auto', opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="px-4 pb-4 space-y-3 border-t border-white/10 dark:border-white/5 pt-3">
                        <div>
                          <label className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">🥬 Veg Items (comma separated)</label>
                          <textarea
                            value={meal.items.join(', ')}
                            onChange={e => updateMealItems(idx, 'items', e.target.value)}
                            rows={2}
                            className="w-full mt-1 bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2 text-[11px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30 resize-none"
                          />
                        </div>
                        <div>
                          <label className="text-[9px] font-medium text-muted-foreground uppercase tracking-wider">🍗 Non-Veg Items (comma separated, leave empty for none)</label>
                          <textarea
                            value={meal.nonVegItems?.join(', ') || ''}
                            onChange={e => updateMealItems(idx, 'nonVegItems', e.target.value)}
                            rows={2}
                            className="w-full mt-1 bg-white/20 dark:bg-white/5 rounded-xl px-3 py-2 text-[11px] text-foreground border border-white/15 dark:border-white/5 outline-none focus:border-primary/30 resize-none"
                          />
                        </div>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.div>
            );
          })}
        </AnimatePresence>
      </div>

      {/* Actions */}
      <div className="flex gap-2 pt-2">
        <button
          onClick={handleReset}
          className="flex-1 py-2.5 rounded-2xl bg-white/10 dark:bg-white/5 text-[11px] font-medium text-muted-foreground active:scale-[0.97] transition-transform"
        >
          Reset to default
        </button>
        <button
          onClick={handleSave}
          className="flex-1 py-2.5 rounded-2xl bg-foreground text-background text-[11px] font-bold active:scale-[0.97] transition-transform"
        >
          Save changes
        </button>
      </div>
    </div>
  );
};

export default MessMenuEditor;
