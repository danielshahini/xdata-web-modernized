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
  Info
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


  const menuItems = [
    { id: 'assignments', label: 'Aufgaben', icon: ClipboardList },
    { id: 'stats', label: 'Statistiken', icon: BarChart3 },
    { id: 'announcements', label: 'Ankündigungen', icon: Bell },
    { id: 'schemas', label: 'SQL Schemas', icon: Database },
    { id: 'users', label: 'Studenten', icon: Users },
    { id: 'courses', label: 'Kurse', icon: GraduationCap },
    { id: 'connections', label: 'Datenbanken', icon: Plug },
    ...(isAdmin ? [{ id: 'audit', label: 'Audit Logs', icon: Briefcase }] : [])
  ];

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn pb-20">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border border-gray-100 dark:border-gray-700 gap-4 transition-colors">
        <div>
          <h1 className="text-3xl font-black dark:text-white tracking-tight">Dozenten-Panel</h1>
          <p className="text-gray-400 font-bold uppercase tracking-widest text-xs mt-1">Verwalten Sie Ihre Kurse und Studenten</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <div className="lg:col-span-3 space-y-2">
          {menuItems.map(item => (
            <button
              key={item.id}
              onClick={() => setTab(item.id as any)}
              className={`w-full flex items-center px-6 py-4 rounded-2xl text-sm font-black transition-all ${
                tab === item.id 
                ? 'bg-blue-600 text-white shadow-xl dark:shadow-none' 
                : 'text-gray-500 dark:text-gray-400 hover:bg-white dark:hover:bg-gray-800 hover:shadow-sm'
              }`}
            >
              <item.icon size={20} className="mr-4" />
              {item.label}
            </button>
          ))}
        </div>

        <div className="lg:col-span-9 animate-slideUp">
          {tab === 'assignments' && <AssignmentManager />}
          {tab === 'schemas' && <SchemaManager />}
          {tab === 'users' && <UserManager />}
          {tab === 'audit' && <AuditLogViewer />}
          {tab === 'connections' && <DbConnectionManager />}
          {tab === 'stats' && <AssignmentStats />}

          {tab === 'announcements' && (
            <div className="space-y-6">
              <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm dark:shadow-none border border-gray-100 dark:border-gray-700 transition-colors">
                <h3 className="text-xl font-black dark:text-white mb-6 flex items-center">
                  <Plus className="mr-2 text-blue-500" /> Neue Ankündigung erstellen
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <input 
                    placeholder="Titel" 
                    className="w-full px-5 py-3 rounded-2xl border dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-bold dark:text-white"
                    value={newAnnouncement.title}
                    onChange={e => setNewAnnouncement({...newAnnouncement, title: e.target.value})}
                  />
                  <select 
                    className="w-full px-5 py-3 rounded-2xl border dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-bold dark:text-white"
                    value={newAnnouncement.courseId}
                    onChange={e => setNewAnnouncement({...newAnnouncement, courseId: e.target.value})}
                  >
                    {courses.map(c => <option key={c.instructorCourseId} value={c.instructorCourseId}>{c.courseName}</option>)}
                  </select>
                </div>
                <textarea 
                  placeholder="Inhalt der Ankündigung..." 
                  className="w-full px-5 py-3 rounded-2xl border dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-bold dark:text-white mb-4 h-32"
                  value={newAnnouncement.content}
                  onChange={e => setNewAnnouncement({...newAnnouncement, content: e.target.value})}
                />
                <button 
                  onClick={handleCreateAnnouncement}
                  className="bg-blue-600 text-white px-8 py-3 rounded-2xl font-black hover:bg-blue-700 transition-all shadow-lg"
                >
                  Veröffentlichen
                </button>
              </div>

              <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm dark:shadow-none border border-gray-100 dark:border-gray-700 transition-colors">
                <h3 className="text-xl font-black dark:text-white mb-6">Bestehende Ankündigungen</h3>
                <div className="space-y-4">
                  {announcements.map(a => (
                    <div key={a.id} className="p-6 rounded-2xl border dark:border-gray-700 flex justify-between items-start">
                      <div>
                        <p className="font-black text-lg dark:text-white">{a.title}</p>
                        <p className="text-gray-500 dark:text-gray-400 mt-2">{a.content}</p>
                        <p className="text-[10px] font-black uppercase text-blue-500 mt-4 tracking-widest">
                          Kurs: {a.course.courseName} | Datum: {new Date(a.createdAt).toLocaleString()}
                        </p>
                      </div>
                      <button onClick={() => deleteAnnouncement(a.id)} className="text-gray-300 hover:text-red-500 transition-colors">
                        <Trash2 size={20} />
                      </button>
                    </div>
                  ))}
                  {announcements.length === 0 && <p className="text-center text-gray-400 italic py-10">Keine Ankündigungen vorhanden.</p>}
                </div>
              </div>
            </div>
          )}

          {tab === 'courses' && (
            <div className="space-y-8">
              <div className="bg-blue-50/60 dark:bg-blue-900/10 p-6 rounded-3xl border border-blue-100 dark:border-blue-900/30 flex items-start gap-3">
                <Info className="text-blue-500 shrink-0 mt-0.5" size={20} />
                <div>
                  <h3 className="text-sm font-black dark:text-white uppercase tracking-widest mb-1">Ihre Kurse</h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Neue Kurse werden von einem Administrator angelegt und Ihnen zugewiesen. Hier sehen Sie Ihre zugewiesenen Kurse.</p>
                </div>
              </div>

              <div className="bg-white dark:bg-gray-800 rounded-3xl overflow-hidden shadow-sm border dark:border-gray-700">
                <table className="w-full">
                  <thead className="bg-gray-50 dark:bg-gray-900/50">
                    <tr>
                      <th className="px-6 py-4 text-left text-[10px] font-black text-gray-400 uppercase tracking-widest">Kursname</th>
                      <th className="px-6 py-4 text-left text-[10px] font-black text-gray-400 uppercase tracking-widest">ID</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                    {courses.map(c => (
                      <tr key={c.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
                        <td className="px-6 py-4 font-bold dark:text-white">{c.courseName}</td>
                        <td className="px-6 py-4 font-mono text-sm text-blue-500">{c.instructorCourseId}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default InstructorDashboard;
