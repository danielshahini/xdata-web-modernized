import React, { useState, useEffect } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import SchemaManager from './SchemaManager';
import AssignmentManager from './AssignmentManager';
import UserManager from './UserManager';
import AuditLogViewer from './AuditLogViewer';
import DbConnectionManager from './DbConnectionManager';
import AssignmentStats from './AssignmentStats';
import { useAuth } from '../context/AuthContext';
import { 
  ClipboardList, 
  Database, 
  Users, 
  Plug, 
  BarChart3,
  GraduationCap,
  Briefcase,
  Bell,
  Trash2,
  Plus,
  Info,
  ChevronDown
} from 'lucide-react';
import api from '../api';
import { toast } from 'react-hot-toast';
import { Course, Announcement } from '../types';

const InstructorDashboard: React.FC = () => {
  const { isAdmin } = useAuth();
  const [tab, setTab] = useState<'assignments' | 'schemas' | 'users' | 'courses' | 'audit' | 'connections' | 'stats' | 'announcements'>('assignments');
  
  const [newAnnouncement, setNewAnnouncement] = useState({ title: '', content: '', courseId: '' });
  

  const { data: announcementsData, retry: reloadAnnouncements } = useAsyncData<Announcement[]>(
    () => api.get('/announcements').then(res => res.data || []),
    []
  );
  const announcements = announcementsData ?? [];

  const { data: coursesData } = useAsyncData<Course[]>(
    () => api.get('/admin/courses').then(res => res.data || []),
    []
  );
  const courses = coursesData ?? [];

  useEffect(() => {
    if (coursesData && coursesData.length > 0) {
      setNewAnnouncement(prev => prev.courseId ? prev : { ...prev, courseId: coursesData[0].instructorCourseId });
    }
  }, [coursesData]);

  const handleCreateAnnouncement = async () => {
    if (!newAnnouncement.title || !newAnnouncement.content || !newAnnouncement.courseId) {
      toast.error("Bitte alle Felder ausfüllen");
      return;
    }
    try {
      await api.post(`/announcements?courseId=${newAnnouncement.courseId}`, {
        title: newAnnouncement.title,
        content: newAnnouncement.content
      });
      toast.success("Ankündigung erstellt");
      setNewAnnouncement({ ...newAnnouncement, title: '', content: '' });
      reloadAnnouncements();
    } catch (e) {
      toast.error("Fehler beim Erstellen");
    }
  };

  const deleteAnnouncement = async (id: number) => {
    if (window.confirm("Ankündigung löschen?")) {
      try {
        await api.delete(`/announcements/${id}`);
        reloadAnnouncements();
        toast.success("Gelöscht");
      } catch (e) {
        toast.error("Fehler beim Löschen");
      }
    }
  };


  // Everyday actions stay front-and-center; rarely used setup lives under "Erweitert".
  const primaryItems = [
    { id: 'assignments', label: 'Aufgaben', icon: ClipboardList },
    { id: 'users', label: 'Studenten', icon: Users },
    { id: 'announcements', label: 'Ankündigungen', icon: Bell },
    { id: 'stats', label: 'Statistiken', icon: BarChart3 },
  ];
  const advancedItems = [
    { id: 'schemas', label: 'SQL Schemas', icon: Database },
    { id: 'connections', label: 'Datenbanken', icon: Plug },
    { id: 'courses', label: 'Kurse', icon: GraduationCap },
    ...(isAdmin ? [{ id: 'audit', label: 'Audit Logs', icon: Briefcase }] : [])
  ];

  // Keep the advanced group open if the active tab lives inside it.
  const [showAdvanced, setShowAdvanced] = useState(
    ['schemas', 'connections', 'courses', 'audit'].includes(tab)
  );

  const renderNavButton = (item: { id: string; label: string; icon: any }) => {
    const active = tab === item.id;
    return (
      <button
        key={item.id}
        onClick={() => setTab(item.id as any)}
        aria-current={active ? 'page' : undefined}
        className={`group w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 ${
          active
            ? 'bg-brand-600 text-white shadow-sm'
            : 'text-slate-600 dark:text-slate-300 hover:bg-white dark:hover:bg-ink-card hover:shadow-sm border border-transparent hover:border-slate-200 dark:hover:border-ink-border'
        }`}
      >
        <item.icon size={18} className={active ? 'text-white' : 'text-slate-400 group-hover:text-brand-500'} />
        {item.label}
      </button>
    );
  };

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn pb-20">
      <header className="animate-rise">
        <p className="kicker">{isAdmin ? 'Administration' : 'Dozenten-Bereich'}</p>
        <h1 className="mt-1 text-3xl font-display font-extrabold tracking-tight text-slate-900 dark:text-white">Dozenten-Panel</h1>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Kurse, Aufgaben und Studierende an einem Ort.</p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <nav className="lg:col-span-3 space-y-1.5" aria-label="Bereiche">
          {primaryItems.map(item => renderNavButton(item))}

          <button
            onClick={() => setShowAdvanced(v => !v)}
            className="w-full flex items-center justify-between gap-3 px-4 pt-4 pb-1.5 text-[11px] font-bold uppercase tracking-[0.14em] text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
            aria-expanded={showAdvanced}
          >
            Erweitert
            <ChevronDown size={14} className={`transition-transform ${showAdvanced ? 'rotate-180' : ''}`} />
          </button>
          {showAdvanced && advancedItems.map(item => renderNavButton(item))}
        </nav>

        <div className="lg:col-span-9 animate-slideUp">
          {tab === 'assignments' && <AssignmentManager />}
          {tab === 'schemas' && <SchemaManager />}
          {tab === 'users' && <UserManager />}
          {tab === 'audit' && <AuditLogViewer />}
          {tab === 'connections' && <DbConnectionManager />}
          {tab === 'stats' && <AssignmentStats />}

          {tab === 'announcements' && (
            <div className="space-y-6">
              <section className="x-card p-6 sm:p-8">
                <h3 className="section-title flex items-center gap-2"><Plus size={20} className="text-brand-500" /> Neue Ankündigung</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                  <div>
                    <label className="x-label">Titel</label>
                    <input
                      placeholder="z. B. Abgabefrist verlängert"
                      className="x-input"
                      value={newAnnouncement.title}
                      onChange={e => setNewAnnouncement({...newAnnouncement, title: e.target.value})}
                    />
                  </div>
                  <div>
                    <label className="x-label">Kurs</label>
                    <select
                      className="x-select"
                      value={newAnnouncement.courseId}
                      onChange={e => setNewAnnouncement({...newAnnouncement, courseId: e.target.value})}
                    >
                      {courses.map(c => <option key={c.instructorCourseId} value={c.instructorCourseId}>{c.courseName}</option>)}
                    </select>
                  </div>
                </div>
                <div className="mt-4">
                  <label className="x-label">Inhalt</label>
                  <textarea
                    placeholder="Was sollen deine Studierenden wissen?"
                    className="x-input h-32 resize-y"
                    value={newAnnouncement.content}
                    onChange={e => setNewAnnouncement({...newAnnouncement, content: e.target.value})}
                  />
                </div>
                <div className="mt-5 flex justify-end">
                  <button onClick={handleCreateAnnouncement} className="btn-primary"><Bell size={16} /> Veröffentlichen</button>
                </div>
              </section>

              <section className="x-card p-6 sm:p-8">
                <h3 className="section-title mb-5">Veröffentlichte Ankündigungen</h3>
                <div className="space-y-3">
                  {announcements.map(a => (
                    <div key={a.id} className="group flex justify-between items-start gap-4 p-5 rounded-xl border border-slate-200 dark:border-ink-border bg-slate-50/50 dark:bg-ink-soft/40">
                      <div className="min-w-0">
                        <p className="font-semibold text-slate-900 dark:text-white">{a.title}</p>
                        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1 whitespace-pre-line">{a.content}</p>
                        <div className="flex flex-wrap items-center gap-2 mt-3">
                          <span className="badge-brand">{a.course.courseName}</span>
                          <span className="text-xs text-slate-400">{new Date(a.createdAt).toLocaleString('de-DE')}</span>
                        </div>
                      </div>
                      <button onClick={() => deleteAnnouncement(a.id)} className="icon-btn hover:text-hard shrink-0" title="Ankündigung löschen" aria-label="Ankündigung löschen">
                        <Trash2 size={18} />
                      </button>
                    </div>
                  ))}
                  {announcements.length === 0 && (
                    <div className="empty-state">
                      <Bell className="text-slate-300 dark:text-slate-600 mb-3" size={36} />
                      <p className="text-sm font-medium text-slate-500 dark:text-slate-400">Noch keine Ankündigungen. Erstelle oben die erste.</p>
                    </div>
                  )}
                </div>
              </section>
            </div>
          )}

          {tab === 'courses' && (
            <div className="space-y-6">
              <div className="flex items-start gap-3 p-5 rounded-2xl border border-brand-200 dark:border-brand-900/40 bg-brand-50/60 dark:bg-brand-950/30">
                <Info className="text-brand-500 shrink-0 mt-0.5" size={20} />
                <div>
                  <p className="font-semibold text-slate-900 dark:text-white">Deine Kurse</p>
                  <p className="text-sm text-slate-600 dark:text-slate-400">Kurse werden von einer Administratorin angelegt und dir zugewiesen.</p>
                </div>
              </div>

              <section className="x-card overflow-hidden">
                <table className="w-full">
                  <thead>
                    <tr>
                      <th className="x-th">Kursname</th>
                      <th className="x-th">Kurs-ID</th>
                    </tr>
                  </thead>
                  <tbody>
                    {courses.map(c => (
                      <tr key={c.id} className="x-row">
                        <td className="x-td font-semibold text-slate-900 dark:text-white">{c.courseName}</td>
                        <td className="x-td"><span className="font-mono text-brand-600 dark:text-brand-400">{c.instructorCourseId}</span></td>
                      </tr>
                    ))}
                    {courses.length === 0 && (
                      <tr><td className="x-td text-slate-400 italic" colSpan={2}>Dir ist noch kein Kurs zugewiesen.</td></tr>
                    )}
                  </tbody>
                </table>
              </section>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default InstructorDashboard;
