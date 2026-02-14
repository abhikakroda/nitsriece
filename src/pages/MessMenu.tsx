import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { getMessPreference } from '@/lib/messMenu';
import { messMenu, fullDays, fullDayNames, getTodayFullDay, getCurrentMealType, type FullDay, type Meal } from '@/lib/messMenu';

const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.08 } },
};

const fadeUp = {
  hidden: { opacity: 0, y: 18 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.22, 1, 0.36, 1] as [number, number, number, number] } },
};

const mealConfig: Record<string, { icon: string; gradient: string; accent: string; time: string }> = {
  breakfast: {
    icon: 'bakery_dining',
    gradient: 'from-amber-500/15 via-orange-500/10 to-yellow-500/5 dark:from-amber-500/10 dark:via-orange-500/5 dark:to-transparent',
    accent: 'text-amber-600 dark:text-amber-400',
    time: '7:30 – 9:00 AM',
  },
  lunch: {
    icon: 'lunch_dining',
    gradient: 'from-emerald-500/15 via-green-500/10 to-teal-500/5 dark:from-emerald-500/10 dark:via-green-500/5 dark:to-transparent',
    accent: 'text-emerald-600 dark:text-emerald-400',
    time: '12:00 – 1:00 PM',
  },
  dinner: {
    icon: 'dinner_dining',
    gradient: 'from-indigo-500/15 via-purple-500/10 to-violet-500/5 dark:from-indigo-500/10 dark:via-purple-500/5 dark:to-transparent',
    accent: 'text-indigo-600 dark:text-indigo-400',
    time: '6:00 – 8:00 PM',
  },
};

const MealCard: React.FC<{ meal: Meal; isActive: boolean }> = ({ meal, isActive }) => {
  const defaultPref = getMessPreference();
  const [showNonVeg, setShowNonVeg] = useState(defaultPref === 'nonveg' && !!meal.nonVegItems);
  const items = showNonVeg && meal.nonVegItems ? meal.nonVegItems : meal.items;
  const config = mealConfig[meal.type];

  return (
    <motion.div
      variants={fadeUp}
      className={`rounded-2xl overflow-hidden border border-white/20 dark:border-white/5 bg-gradient-to-br ${config.gradient} backdrop-blur-sm`}
      style={{ boxShadow: '0 2px 20px -4px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.5)' }}
    >
      <div className="p-4 pb-5">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className={`size-10 rounded-2xl bg-white/40 dark:bg-white/10 flex items-center justify-center backdrop-blur-sm`}
              style={{ boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.6), 0 2px 8px -2px rgba(0,0,0,0.08)' }}
            >
              <span className={`material-symbols-outlined text-[22px] ${config.accent}`}>{config.icon}</span>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-[15px] font-bold text-foreground">{meal.label}</h3>
                {isActive && (
                  <motion.span
                    initial={{ scale: 0.8, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    className={`text-[8px] font-bold uppercase tracking-widest ${config.accent} bg-white/50 dark:bg-white/10 px-2 py-0.5 rounded-full`}
                  >
                    Now
                  </motion.span>
                )}
              </div>
              <p className="text-[10px] text-muted-foreground mt-0.5">{config.time}</p>
            </div>
          </div>
          {meal.nonVegItems && (
            <button
              onClick={() => setShowNonVeg(!showNonVeg)}
              className={`text-[10px] font-bold px-3 py-1.5 rounded-xl transition-all active:scale-95 ${
                showNonVeg
                  ? 'bg-rose-500/20 text-rose-600 dark:text-rose-400 border border-rose-500/20'
                  : 'bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20'
              }`}
            >
              {showNonVeg ? '🍗 Non-Veg' : '🥬 Veg'}
            </button>
          )}
        </div>

        {/* Items */}
        <div className="flex flex-wrap gap-2">
          {items.map((item, i) => (
            <motion.span
              key={`${showNonVeg}-${i}`}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: i * 0.03 }}
              className="text-[12px] text-foreground bg-white/50 dark:bg-white/8 px-3 py-2 rounded-xl font-medium border border-white/30 dark:border-white/5"
              style={{ boxShadow: '0 1px 3px -1px rgba(0,0,0,0.04)' }}
            >
              {item}
            </motion.span>
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
            <div className="text-right">
              <p className="text-[12px] font-semibold text-foreground">{fullDayNames[selectedDay]}</p>
              <p className="text-[9px] text-muted-foreground">{meals.length} meals</p>
            </div>
          </div>

          {/* Day selector */}
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
                  className={`relative flex-1 flex flex-col items-center py-2.5 rounded-xl transition-all duration-200 ${
                    isSelected
                      ? 'bg-foreground text-background shadow-lg'
                      : 'text-muted-foreground hover:bg-white/20 dark:hover:bg-white/5'
                  }`}
                >
                  <span className={`text-[10px] font-bold uppercase tracking-wider ${isSelected ? 'text-background/70' : ''}`}>
                    {day}
                  </span>
                  {isToday && !isSelected && (
                    <span className="absolute bottom-1 size-[4px] rounded-full bg-primary" />
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
            <motion.div key={selectedDay} variants={stagger} initial="hidden" animate="visible" className="space-y-4">
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
