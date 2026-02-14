import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { getMessPreference } from '@/lib/messMenu';
import { messMenu, fullDays, fullDayNames, getTodayFullDay, getCurrentMealType, type FullDay, type Meal } from '@/lib/messMenu';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.06 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const mealIcons: Record<string, string> = {
  breakfast: 'bakery_dining',
  lunch: 'lunch_dining',
  dinner: 'dinner_dining',
};

const MealCard: React.FC<{ meal: Meal; isActive: boolean }> = ({ meal, isActive }) => {
  const defaultPref = getMessPreference();
  const [showNonVeg, setShowNonVeg] = useState(defaultPref === 'nonveg' && !!meal.nonVegItems);
  const items = showNonVeg && meal.nonVegItems ? meal.nonVegItems : meal.items;

  return (
    <motion.div variants={fadeUp} className="liquid-glass-card rounded-2xl overflow-hidden">
      <div className="px-4 py-3.5">
        <div className="flex items-center gap-2.5 mb-3">
          <div className="size-8 rounded-xl bg-white/20 dark:bg-white/5 flex items-center justify-center">
            <span className="material-symbols-outlined text-[18px] text-muted-foreground">{mealIcons[meal.type]}</span>
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <p className="text-[13px] font-semibold text-foreground">{meal.label}</p>
              {isActive && (
                <span className="text-[8px] font-bold uppercase tracking-widest text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 px-1.5 py-0.5 rounded-full">Now</span>
              )}
            </div>
          </div>
          {meal.nonVegItems && (
            <button
              onClick={() => setShowNonVeg(!showNonVeg)}
              className={`text-[9px] font-bold uppercase tracking-wider px-2 py-1 rounded-lg transition-colors ${
                showNonVeg
                  ? 'bg-rose-500/15 text-rose-600 dark:text-rose-400'
                  : 'bg-white/20 dark:bg-white/5 text-muted-foreground'
              }`}
            >
              {showNonVeg ? '🍗 Non-Veg' : '🥬 Veg'}
            </button>
          )}
        </div>

        <div className="flex flex-wrap gap-1.5">
          {items.map((item, i) => (
            <span
              key={i}
              className="text-[11px] text-foreground/80 bg-white/15 dark:bg-white/5 px-2.5 py-1.5 rounded-xl"
            >
              {item}
            </span>
          ))}
        </div>
      </div>
    </motion.div>
  );
};

const MessMenu: React.FC = () => {
  const todayDay = getTodayFullDay();
  const [selectedDay, setSelectedDay] = useState<FullDay>(todayDay);
  const currentMeal = getCurrentMealType();
  const meals = messMenu[selectedDay];

  return (
    <div className="bg-background min-h-screen flex flex-col pb-20 lg:pb-6">
      {/* Header */}
      <div className="sticky top-0 z-50 liquid-glass-nav px-5 md:px-6 pt-5 pb-4 border-b border-white/20 dark:border-white/5">
        <div className="max-w-3xl mx-auto">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="font-display text-[20px] md:text-[24px] font-bold text-foreground tracking-tight">Mess Menu</h1>
              <p className="text-muted-foreground text-[11px] mt-0.5">NIT Srinagar Hostel Mess</p>
            </div>
            <span className="text-[10px] font-medium text-muted-foreground">
              {fullDayNames[selectedDay]}
            </span>
          </div>

          {/* Day selector - all 7 days */}
          <div className="flex gap-1">
            {fullDays.map((day, i) => {
              const isSelected = selectedDay === day;
              const isToday = day === todayDay;
              return (
                <motion.button
                  key={day}
                  onClick={() => setSelectedDay(day)}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.03, duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
                  className={`relative flex-1 flex flex-col items-center py-2 rounded-xl transition-colors duration-200 ${
                    isSelected
                      ? 'bg-foreground text-background'
                      : 'text-muted-foreground hover:bg-white/20 dark:hover:bg-white/5'
                  }`}
                >
                  <span className={`text-[9px] font-bold uppercase tracking-wider ${isSelected ? 'text-background/60' : ''}`}>
                    {day}
                  </span>
                  {isToday && !isSelected && (
                    <span className="absolute bottom-1 size-[3px] rounded-full bg-primary" />
                  )}
                </motion.button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Meals */}
      <main className="flex-1 px-5 md:px-6 py-5">
        <div className="max-w-3xl mx-auto">
          <AnimatePresence mode="wait">
            <motion.div key={selectedDay} variants={stagger} initial="hidden" animate="visible" className="space-y-3">
              {meals.map((meal, idx) => (
                <MealCard
                  key={`${selectedDay}-${idx}`}
                  meal={meal}
                  isActive={selectedDay === todayDay && currentMeal === meal.type}
                />
              ))}
            </motion.div>
          </AnimatePresence>
        </div>
      </main>
    </div>
  );
};

export default MessMenu;
