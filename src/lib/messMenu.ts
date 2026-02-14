export type MealType = 'breakfast' | 'lunch' | 'dinner';

export interface Meal {
  type: MealType;
  label: string;
  emoji: string;
  items: string[];
  nonVegItems?: string[];
}

export type FullDay = 'Mon' | 'Tue' | 'Wed' | 'Thu' | 'Fri' | 'Sat' | 'Sun';

export const fullDays: FullDay[] = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

export const fullDayNames: Record<FullDay, string> = {
  Mon: 'Monday', Tue: 'Tuesday', Wed: 'Wednesday', Thu: 'Thursday',
  Fri: 'Friday', Sat: 'Saturday', Sun: 'Sunday',
};

const defaultMessMenu: Record<FullDay, Meal[]> = {
  Mon: [
    { type: 'breakfast', label: 'Breakfast', emoji: '☀️', items: ['Tea ☕', 'Bread Pakoda 🥪', 'Malai 🧈', 'Sauce 🥫', 'Green Chutney 🌿', 'Kashmiri Roti 🫓 with Butter 🧈'] },
    { type: 'lunch', label: 'Lunch', emoji: '🌤️', items: ['Rice 🍚', 'Roti 🫓', 'Baingan Bharta 🍆', 'Chana Wash Dal 🥣', 'Curd 🍶'] },
    { type: 'dinner', label: 'Dinner', emoji: '🌙', items: ['Rice 🍚', 'Roti 🫓', 'White Chana Dal 🥣', 'Matter Paneer 🧀', 'Seveiyan 🍜'], nonVegItems: ['Rice 🍚', 'Roti 🫓', 'Tomato Chicken 🍗🍅', 'Seveiyan 🍜'] },
  ],
  Tue: [
    { type: 'breakfast', label: 'Breakfast', emoji: '☀️', items: ['Tea ☕', 'Bread 🍞', 'Butter 🧈', 'Jam 🍓', 'Milk 🥛', 'Banana 🍌'] },
    { type: 'lunch', label: 'Lunch', emoji: '🌤️', items: ['Rice 🍚', 'Roti 🫓', 'Aloo Capsicum 🥔🫑', 'Rajma Dal 🫘', 'Curd 🍶'] },
    { type: 'dinner', label: 'Dinner', emoji: '🌙', items: ['Rice 🍚', 'Roti 🫓', 'Mixed Vegetables 🥕🥦', 'Kabuli Chana 🫘'] },
  ],
  Wed: [
    { type: 'breakfast', label: 'Breakfast', emoji: '☀️', items: ['Tea ☕', 'Brown Bread 🍞', 'Peanut Butter 🥜'] },
    { type: 'lunch', label: 'Lunch', emoji: '🌤️', items: ['Rice 🍚', 'Roti 🫓', 'Matter Paneer 🧀', 'Black Chana 🫘', 'Salad 🥗'] },
    { type: 'dinner', label: 'Dinner', emoji: '🌙', items: ['Rice 🍚', 'Roti 🫓', 'Paneer Butter Masala 🧀', 'Kabuli Chana 🫘', 'Gulab Jamun 🍮'], nonVegItems: ['Rice 🍚', 'Roti 🫓', 'Arhar Dal 🥣 / Chicken 🍗', 'Gulab Jamun 🍮', 'Amul Milk 🥛'] },
  ],
  Thu: [
    { type: 'breakfast', label: 'Breakfast', emoji: '☀️', items: ['Tea ☕', 'Pyaz Paratha 🫓🧅', 'Sauce 🥫', 'Kashmiri Roti 🫓 with Butter 🧈'] },
    { type: 'lunch', label: 'Lunch', emoji: '🌤️', items: ['Rice 🍚', 'Roti 🫓', 'Mixed Vegetable 🥕🥦', 'Sambar Dal 🥣', 'Salad 🥗'] },
    { type: 'dinner', label: 'Dinner', emoji: '🌙', items: ['Rice 🍚', 'Roti 🫓', 'Aloo Palak 🥔🌿', 'Rajma Dal 🫘'] },
  ],
  Fri: [
    { type: 'breakfast', label: 'Breakfast', emoji: '☀️', items: ['Tea ☕', 'Pav Bhaji 🍞🥔', 'Kashmiri Roti 🫓'] },
    { type: 'lunch', label: 'Lunch', emoji: '🌤️', items: ['Rice 🍚', 'Roti 🫓', 'Tomato Matter 🍅', 'Rajma Dal 🫘', 'Curd 🍶'] },
    { type: 'dinner', label: 'Dinner', emoji: '🌙', items: ['Rice 🍚', 'Roti 🫓', 'Paneer Do Pyaza 🧀🧅', 'Moong Dal 🥣', 'Kheer 🍮'], nonVegItems: ['Rice 🍚', 'Roti 🫓', 'Tomato Chicken 🍗🍅'] },
  ],
  Sat: [
    { type: 'breakfast', label: 'Breakfast', emoji: '☀️', items: ['Tea ☕', 'Chola Samosa 🥟', 'Curd 🍶'] },
    { type: 'lunch', label: 'Lunch', emoji: '🌤️', items: ['Rice 🍚', 'Roti 🫓', 'Aloo Gobhi Fry 🥔🥦', 'Moong Dal 🥣', 'Curd 🍶'] },
    { type: 'dinner', label: 'Dinner', emoji: '🌙', items: ['Rice 🍚', 'Roti 🫓', 'Mixed Vegetables 🥕🥦', 'Rajma Dal 🫘'] },
  ],
  Sun: [
    { type: 'breakfast', label: 'Breakfast', emoji: '☀️', items: ['Tea ☕', 'Aloo Paratha 🫓🥔', 'Butter 🧈', 'Sauce 🥫'] },
    { type: 'lunch', label: 'Lunch', emoji: '🌤️', items: ['Vegetable Biryani 🍛', 'Rajma Dal 🫘', 'Vegetable Raita 🍶🥒'] },
    { type: 'dinner', label: 'Dinner', emoji: '🌙', items: ['Rice 🍚', 'Roti 🫓', 'Paneer Bhurji 🧀', 'Chana Wash Dal 🥣', 'Gulab Jamun 🍮'], nonVegItems: ['Rice 🍚', 'Roti 🫓', 'Egg Curry 🥚🍛', 'Gulab Jamun 🍮'] },
  ],
};

const MESS_MENU_KEY = 'campus_mess_menu';
const MESS_PREF_KEY = 'campus_mess_preference'; // 'veg' | 'nonveg'

// Listeners
type Listener = () => void;
const messListeners: Set<Listener> = new Set();
export function subscribeMessMenu(fn: Listener) {
  messListeners.add(fn);
  return () => messListeners.delete(fn);
}
function notifyMess() {
  messListeners.forEach(fn => fn());
}

export function getMessMenuData(): Record<FullDay, Meal[]> {
  try {
    const stored = localStorage.getItem(MESS_MENU_KEY);
    if (stored) return JSON.parse(stored);
  } catch {}
  return JSON.parse(JSON.stringify(defaultMessMenu));
}

export function saveMessMenuData(data: Record<FullDay, Meal[]>) {
  localStorage.setItem(MESS_MENU_KEY, JSON.stringify(data));
  Object.assign(messMenu, data);
  notifyMess();
}

export function resetMessMenuData() {
  localStorage.removeItem(MESS_MENU_KEY);
  Object.assign(messMenu, JSON.parse(JSON.stringify(defaultMessMenu)));
  notifyMess();
}

export function getMessPreference(): 'veg' | 'nonveg' {
  return (localStorage.getItem(MESS_PREF_KEY) as 'veg' | 'nonveg') || 'veg';
}

export function setMessPreference(pref: 'veg' | 'nonveg') {
  localStorage.setItem(MESS_PREF_KEY, pref);
  notifyMess();
}

// Mutable export
export const messMenu: Record<FullDay, Meal[]> = getMessMenuData();

export function getTodayFullDay(): FullDay {
  const d = new Date().getDay();
  const map: FullDay[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  return map[d];
}

export function getCurrentMealType(): MealType {
  const h = new Date().getHours();
  if (h < 11) return 'breakfast';
  if (h < 16) return 'lunch';
  return 'dinner';
}
