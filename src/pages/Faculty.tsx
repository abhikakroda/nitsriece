import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const facultyList = [
  { name: 'Dr. Omkar Singh', position: 'Associate Professor', domain: 'DSP & Signals', cabin: 'ECT-102', email: 'omkar@example.edu.in', img: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=100&q=80' },
  { name: 'Dr. Kajal', position: 'Assistant Professor', domain: 'Computer Architecture', cabin: 'ECT-205', email: 'kajal@example.edu.in', img: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=100&q=80' },
  { name: 'Dr. Arshid', position: 'Head of Department', domain: 'Networks & Comm.', cabin: 'HOD Office', email: 'arshid@example.edu.in', img: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=100&q=80' },
  { name: 'Dr. Brajendra', position: 'Professor', domain: 'VLSI Design', cabin: 'VLSI Lab', email: 'brajendra@example.edu.in', img: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=100&q=80' },
];

const Faculty: React.FC = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');

  const filtered = facultyList.filter(f =>
    f.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    f.domain.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="bg-background min-h-screen flex flex-col pb-safe">
      <header className="sticky top-0 z-50 glass-morphism px-4 py-4 border-b border-border">
        <div className="flex items-center gap-3 mb-4">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-accent rounded-full transition-colors">
            <span className="material-symbols-outlined text-primary">arrow_back</span>
          </button>
          <h1 className="text-xl font-bold tracking-tight text-foreground">Faculty Directory</h1>
        </div>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <span className="material-symbols-outlined text-muted-foreground text-xl">search</span>
          </div>
          <input
            className="block w-full pl-10 pr-4 py-3 bg-card border border-border rounded-xl text-sm placeholder-muted-foreground focus:ring-2 focus:ring-primary/50 shadow-sm outline-none transition-all"
            placeholder="Search by name or domain..."
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </header>

      <main className="flex-1 px-4 py-6 space-y-4">
        {filtered.map((fac, idx) => (
          <div key={idx} className="bg-card p-4 rounded-xl border border-border shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
            <img src={fac.img} alt={fac.name} className="size-16 rounded-full object-cover border-2 border-border" />
            <div className="flex-1 min-w-0">
              <h3 className="font-bold text-foreground text-base">{fac.name}</h3>
              <p className="text-xs font-bold text-primary uppercase tracking-wide mb-1">{fac.position}</p>
              <p className="text-xs text-muted-foreground truncate">{fac.domain}</p>
              <div className="flex items-center gap-4 mt-3">
                <a href={`mailto:${fac.email}`} className="text-muted-foreground hover:text-primary transition-colors flex items-center gap-1 text-xs font-medium">
                  <span className="material-symbols-outlined text-sm">mail</span> Email
                </a>
                <span className="text-muted-foreground flex items-center gap-1 text-xs font-medium">
                  <span className="material-symbols-outlined text-sm">location_on</span> {fac.cabin}
                </span>
              </div>
            </div>
          </div>
        ))}
      </main>
    </div>
  );
};

export default Faculty;
