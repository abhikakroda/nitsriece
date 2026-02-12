import React, { useState } from 'react';

const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'] as const;

type Day = typeof days[number];

interface ClassSlot {
  subject: string;
  time: string;
  room: string;
  icon: string;
  iconBg: string;
  iconColor: string;
  type?: string;
}

const timetableData: Record<Day, ClassSlot[]> = {
  Mon: [
    { subject: 'Computer Org & Arch', time: '09:50 – 10:40', room: 'ECT 352', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500' },
    { subject: 'VLSI Design Lab (G3)', time: '10:40 – 12:20', room: 'ECL 356', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500', type: 'Lab' },
    { subject: 'DSP Lab-I (G3)', time: '02:00 – 03:40', room: 'ECL 357', icon: 'graphic_eq', iconBg: 'bg-green-500/10', iconColor: 'text-green-500', type: 'Lab' },
    { subject: 'Microprocessors', time: '03:40 – 04:30', room: 'ECT 301', icon: 'developer_board', iconBg: 'bg-blue-500/10', iconColor: 'text-blue-500' },
    { subject: 'Signals & Systems', time: '04:30 – 05:20', room: 'ECT 303', icon: 'show_chart', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500' },
  ],
  Tue: [
    { subject: 'Signals & Systems', time: '09:00 – 09:50', room: 'ECT 303', icon: 'show_chart', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500' },
    { subject: 'Microprocessors', time: '09:50 – 10:40', room: 'ECT 301', icon: 'developer_board', iconBg: 'bg-blue-500/10', iconColor: 'text-blue-500' },
    { subject: 'Computer Org & Arch', time: '10:40 – 11:30', room: 'ECT 352', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500' },
    { subject: 'Engineering Maths', time: '02:00 – 02:50', room: 'ECT 201', icon: 'calculate', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500' },
  ],
  Wed: [
    { subject: 'VLSI Design', time: '09:00 – 09:50', room: 'ECT 355', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500' },
    { subject: 'Engineering Maths', time: '09:50 – 10:40', room: 'ECT 201', icon: 'calculate', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500' },
    { subject: 'Comm. Systems Lab', time: '10:40 – 12:20', room: 'ECL 360', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500', type: 'Lab' },
    { subject: 'Signals & Systems', time: '02:00 – 02:50', room: 'ECT 303', icon: 'show_chart', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500' },
  ],
  Thu: [
    { subject: 'Engineering Maths', time: '09:00 – 09:50', room: 'ECT 201', icon: 'calculate', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500' },
    { subject: 'Computer Org & Arch', time: '09:50 – 10:40', room: 'ECT 352', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500' },
    { subject: 'Microprocessors Lab', time: '10:40 – 12:20', room: 'ECL 358', icon: 'developer_board', iconBg: 'bg-blue-500/10', iconColor: 'text-blue-500', type: 'Lab' },
    { subject: 'VLSI Design', time: '02:00 – 02:50', room: 'ECT 355', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500' },
  ],
  Fri: [
    { subject: 'Signals & Systems', time: '09:00 – 09:50', room: 'ECT 303', icon: 'show_chart', iconBg: 'bg-indigo-500/10', iconColor: 'text-indigo-500' },
    { subject: 'Microprocessors', time: '09:50 – 10:40', room: 'ECT 301', icon: 'developer_board', iconBg: 'bg-blue-500/10', iconColor: 'text-blue-500' },
    { subject: 'Engineering Maths', time: '10:40 – 11:30', room: 'ECT 201', icon: 'calculate', iconBg: 'bg-teal-500/10', iconColor: 'text-teal-500' },
    { subject: 'Computer Org & Arch', time: '02:00 – 02:50', room: 'ECT 352', icon: 'memory', iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500' },
  ],
  Sat: [
    { subject: 'VLSI Design', time: '09:00 – 09:50', room: 'ECT 355', icon: 'science', iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500' },
    { subject: 'Comm. Systems', time: '09:50 – 10:40', room: 'ECT 360', icon: 'cell_tower', iconBg: 'bg-pink-500/10', iconColor: 'text-pink-500' },
  ],
};

const Schedule: React.FC = () => {
  const [selectedDay, setSelectedDay] = useState<Day>('Mon');
  const classes = timetableData[selectedDay];

  return (
    <div className="bg-background min-h-screen flex flex-col pb-20">
      {/* Header */}
      <div className="sticky top-0 z-50 glass-morphism px-4 pt-4 pb-3 border-b border-border">
        <h1 className="text-xl font-black text-foreground tracking-tight mb-3">Weekly Schedule</h1>

        {/* Day Selector */}
        <div className="flex gap-1.5">
          {days.map(day => (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`flex-1 py-2 rounded-xl text-[12px] font-bold transition-all duration-200 ${
                selectedDay === day
                  ? 'bg-primary text-primary-foreground shadow-md shadow-primary/20'
                  : 'bg-secondary text-muted-foreground active:bg-accent'
              }`}
            >
              {day}
            </button>
          ))}
        </div>
      </div>

      {/* Classes */}
      <main className="flex-1 px-4 py-4">
        <div className="flex items-center justify-between mb-3">
          <p className="text-muted-foreground text-[11px] font-bold uppercase tracking-widest">
            {selectedDay === 'Mon' ? 'Monday' : selectedDay === 'Tue' ? 'Tuesday' : selectedDay === 'Wed' ? 'Wednesday' : selectedDay === 'Thu' ? 'Thursday' : selectedDay === 'Fri' ? 'Friday' : 'Saturday'}
          </p>
          <span className="text-[10px] font-bold text-primary bg-primary/10 px-2 py-0.5 rounded-full">
            {classes.length} {classes.length === 1 ? 'Class' : 'Classes'}
          </span>
        </div>

        {classes.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <span className="material-symbols-outlined text-[48px] text-muted-foreground/30 mb-3">event_available</span>
            <p className="text-muted-foreground font-medium text-[14px]">No classes today!</p>
            <p className="text-muted-foreground/60 text-[12px] mt-1">Enjoy your free day 🎉</p>
          </div>
        ) : (
          <div className="relative">
            {/* Timeline line */}
            <div className="absolute left-[19px] top-4 bottom-4 w-[2px] bg-border rounded-full" />

            <div className="space-y-3">
              {classes.map((cls, idx) => (
                <div key={idx} className="flex gap-3 relative">
                  {/* Timeline dot */}
                  <div className="relative z-10 mt-4 shrink-0">
                    <div className={`size-[10px] rounded-full border-2 ${idx === 0 ? 'border-primary bg-primary' : 'border-border bg-card'}`} style={{ marginLeft: '10px' }} />
                  </div>

                  {/* Card */}
                  <div className={`flex-1 bg-card rounded-2xl p-3.5 border ${idx === 0 ? 'border-primary/20 shadow-sm' : 'border-border'} transition-all active:scale-[0.98]`}>
                    <div className="flex items-start justify-between">
                      <div className="flex gap-3">
                        <div className={`size-10 rounded-xl ${cls.iconBg} ${cls.iconColor} flex items-center justify-center shrink-0`}>
                          <span className="material-symbols-outlined text-[20px]">{cls.icon}</span>
                        </div>
                        <div>
                          <h3 className="font-bold text-foreground text-[14px] leading-tight">{cls.subject}</h3>
                          <p className="text-[11px] text-muted-foreground mt-1 flex items-center gap-1">
                            <span className="material-symbols-outlined text-[13px]">schedule</span> {cls.time}
                          </p>
                          <p className="text-[11px] text-muted-foreground flex items-center gap-1">
                            <span className="material-symbols-outlined text-[13px]">location_on</span> {cls.room}
                          </p>
                        </div>
                      </div>
                      {cls.type && (
                        <span className="text-[9px] font-bold py-0.5 px-2 rounded-full bg-purple-500/10 text-purple-600 uppercase">{cls.type}</span>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default Schedule;
