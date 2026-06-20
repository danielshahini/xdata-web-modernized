import React, { useState, useEffect, useCallback } from 'react';
import api from '../api';
import UserManager from './UserManager';
import ConfirmationModal from './common/ConfirmationModal';
import {
  Plus,
  Trash2,
  ChevronDown,
  ChevronUp,
  Users
} from 'lucide-react';
import { Course, User } from '../types';
import { toast } from 'react-hot-toast';

const AdminDashboard: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [activeTab, setActiveTab] = useState<'users' | 'courses'>('users');

  const [expandedCourse, setExpandedCourse] = useState<number | null>(null);
  const [courseMembers, setCourseMembers] = useState<Record<number, User[]>>({});
  const [loadingMembers, setLoadingMembers] = useState<Record<number, boolean>>({});

  const [deleteModal, setDeleteModal] = useState<{ isOpen: boolean; courseId: number | null }>({
    isOpen: false,
    courseId: null
  });
  
  const [newCourse, setNewCourse] = useState({ courseName: '', instructorCourseId: '' });

  const loadCourses = useCallback(() => api.get('/admin/courses').then(res => setCourses(res.data)), []);

  const loadCourseMembers = async (courseId: number) => {
    if (courseMembers[courseId]) return;
    setLoadingMembers(prev => ({ ...prev, [courseId]: true }));
    try {
      const res = await api.get(`/admin/courses/${courseId}/members`);
      setCourseMembers(prev => ({ ...prev, [courseId]: res.data }));
    } catch (e) {
      toast.error("Mitglieder konnten nicht geladen werden");
    } finally {
      setLoadingMembers(prev => ({ ...prev, [courseId]: false }));
    }
  };

  const toggleCourse = (courseId: number) => {
    if (expandedCourse === courseId) {
      setExpandedCourse(null);
    } else {
      setExpandedCourse(courseId);
      loadCourseMembers(courseId);
    }
  };

  useEffect(() => {
    loadCourses();
  }, [loadCourses]);

  const handleCreateCourse = async () => {
    try {
      await api.post('/admin/courses', newCourse);
      toast.success('Kurs erstellt');
      loadCourses();
      setNewCourse({ courseName: '', instructorCourseId: '' });
    } catch (err) { toast.error('Fehler beim Erstellen'); }
  };

  const handleDeleteCourse = async () => {
    if (!deleteModal.courseId) return;
    try {
      await api.delete(`/admin/courses/${deleteModal.courseId}`);
      toast.success("Kurs gelöscht");
      loadCourses();
    } catch (e) {
      toast.error("Löschen fehlgeschlagen");
    }
  };

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn pb-20">
      <ConfirmationModal 
        isOpen={deleteModal.isOpen}
        onClose={() => setDeleteModal({ isOpen: false, courseId: null })}
        onConfirm={handleDeleteCourse}
        title="Kurs löschen"
        message="Sind Sie sicher, dass Sie diesen Kurs löschen möchten? Alle zugehörigen Daten gehen verloren."
      />
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 animate-rise">
        <div>
          <p className="kicker">Administration</p>
          <h1 className="mt-1 text-3xl font-display font-extrabold tracking-tight text-slate-900 dark:text-white">Admin-Zentrale</h1>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Systemweite Verwaltung &amp; Monitoring.</p>
        </div>
        <div role="tablist" aria-label="Ansicht" className="flex gap-1 bg-slate-100 dark:bg-ink-soft p-1 rounded-xl border border-slate-200 dark:border-ink-border">
          {([['users','Benutzer'],['courses','Kurse']] as const).map(([id,label]) => (
            <button key={id} role="tab" aria-selected={activeTab === id}
              onClick={() => setActiveTab(id)}
              className={`x-tab ${activeTab === id ? 'x-tab-active' : ''}`}>
              {label}
            </button>
          ))}
        </div>
      </div>

      {activeTab === 'users' && <UserManager />}

      {activeTab === 'courses' && (
        <div className="space-y-6 animate-slideUp">
          <section className="x-card p-6 sm:p-8">
            <h3 className="section-title flex items-center gap-2"><Plus size={20} className="text-brand-500" /> Neuen Kurs anlegen</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
              <div>
                <label className="x-label">Kursname</label>
                <input placeholder="z. B. Datenbanken I" className="x-input"
                  value={newCourse.courseName} onChange={e => setNewCourse({...newCourse, courseName: e.target.value})} />
              </div>
              <div>
                <label className="x-label">Kurs-ID</label>
                <input placeholder="z. B. DB1-2026" className="x-input font-mono"
                  value={newCourse.instructorCourseId} onChange={e => setNewCourse({...newCourse, instructorCourseId: e.target.value})} />
              </div>
            </div>
            <div className="mt-5 flex justify-end">
              <button onClick={handleCreateCourse} className="btn-primary"><Plus size={16} /> Kurs erstellen</button>
            </div>
          </section>

          <section className="x-card overflow-hidden">
            <table className="w-full">
              <thead>
                <tr>
                  <th className="x-th">Name</th>
                  <th className="x-th">Kurs-ID</th>
                  <th className="x-th text-right">Aktion</th>
                </tr>
              </thead>
              <tbody>
                {courses.map(c => (
                  <React.Fragment key={c.id}>
                    <tr onClick={() => toggleCourse(c.id)} className="x-row cursor-pointer">
                      <td className="x-td">
                        <div className="flex items-center gap-2 font-semibold text-slate-900 dark:text-white">
                          {expandedCourse === c.id ? <ChevronUp size={16} className="text-brand-500" /> : <ChevronDown size={16} className="text-slate-400" />}
                          {c.courseName}
                        </div>
                      </td>
                      <td className="x-td"><span className="font-mono text-brand-600 dark:text-brand-400">{c.instructorCourseId}</span></td>
                      <td className="x-td text-right">
                        <button onClick={(e) => { e.stopPropagation(); setDeleteModal({ isOpen: true, courseId: c.id }); }}
                          className="icon-btn hover:text-hard" title="Kurs löschen" aria-label="Kurs löschen">
                          <Trash2 size={18} />
                        </button>
                      </td>
                    </tr>
                    {expandedCourse === c.id && (
                      <tr className="bg-slate-50/60 dark:bg-ink-soft/30">
                        <td colSpan={3} className="px-6 sm:px-10 py-6">
                          <div className="flex items-center justify-between mb-4">
                            <h4 className="kicker flex items-center gap-2"><Users size={14} /> Kursmitglieder</h4>
                            <span className="badge-neutral">{courseMembers[c.id]?.length || 0} Personen</span>
                          </div>
                          {loadingMembers[c.id] ? (
                            <p className="text-sm text-slate-400 animate-pulse">Lade Mitglieder…</p>
                          ) : courseMembers[c.id]?.length === 0 ? (
                            <p className="text-sm text-slate-500 italic">Keine Mitglieder in diesem Kurs.</p>
                          ) : (
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                              {courseMembers[c.id]?.map(member => (
                                <div key={member.loginId} className="flex items-center gap-3 p-3 rounded-xl border border-slate-200 dark:border-ink-border bg-white dark:bg-ink-card">
                                  <div className="h-9 w-9 rounded-lg bg-brand-500/10 flex items-center justify-center shrink-0">
                                    <span className="text-xs font-bold text-brand-600 dark:text-brand-300 uppercase">{(member.username || "??").substring(0, 2)}</span>
                                  </div>
                                  <div className="overflow-hidden">
                                    <p className="text-sm font-semibold text-slate-900 dark:text-white truncate">{member.username || "Unbekannt"}</p>
                                    <p className="text-xs text-slate-400 truncate font-mono">{member.loginId} · {member.role}</p>
                                  </div>
                                </div>
                              ))}
                            </div>
                          )}
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
                {courses.length === 0 && (
                  <tr><td className="x-td text-slate-400 italic" colSpan={3}>Noch keine Kurse angelegt.</td></tr>
                )}
              </tbody>
            </table>
          </section>
        </div>
      )}

    </div>
  );
};

export default AdminDashboard;
