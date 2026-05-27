import React, { useState, useEffect, useCallback } from 'react';
import api from '../api';
import UserManager from './UserManager';
import ConfirmationModal from './common/ConfirmationModal';
import { 
  Server, 
  Database, 
  Cpu, 
  HardDrive, 
  Activity, 
  Clock, 
  CheckCircle, 
  XCircle,
  RefreshCw,
  Plus,
  Trash2,
  ChevronDown,
  ChevronUp,
  Users
} from 'lucide-react';
import { Course, SystemStatus, User } from '../types';
import { toast } from 'react-hot-toast';

const AdminDashboard: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [activeTab, setActiveTab] = useState<'users' | 'courses' | 'system'>('users');
  const [dbStatus, setDbStatus] = useState<string | null>(null);
  const [testingDb, setTestingDb] = useState(false);
  const [systemStatus, setSystemStatus] = useState<SystemStatus | null>(null);
  
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

  const loadSystemStatus = useCallback(async () => {
    try {
      const res = await api.get('/system/status');
      setSystemStatus(res.data);
    } catch (e) {
      console.error("System-Status konnte nicht geladen werden");
    }
  }, []);

  useEffect(() => {
    loadCourses();
    if (activeTab === 'system') {
      loadSystemStatus();
    }
  }, [loadCourses, activeTab, loadSystemStatus]);

  const testConnection = async () => {
    setTestingDb(true);
    try {
      const res = await api.get('/db-connections/test');
      setDbStatus(res.data);
      toast.success("Verbindungstest abgeschlossen");
    } catch (e: any) {
      setDbStatus("Fehler: " + (e.response?.data || e.message));
      toast.error("Verbindungstest fehlgeschlagen");
    } finally {
      setTestingDb(false);
    }
  };

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
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border border-gray-100 dark:border-gray-700 gap-4 transition-colors">
        <div>
          <h1 className="text-3xl font-black dark:text-white tracking-tight">Admin-Zentrale</h1>
          <p className="text-gray-400 font-bold uppercase tracking-widest text-xs mt-1">Systemweite Verwaltung & Monitoring</p>
        </div>
        <div className="flex gap-2 bg-gray-50 dark:bg-gray-900 p-1.5 rounded-2xl border dark:border-gray-700">
          <button
            onClick={() => setActiveTab('users')}
            className={`px-6 py-2.5 rounded-xl text-xs font-black transition-all ${activeTab === 'users' ? 'bg-blue-600 text-white shadow-lg' : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-200'}`}
          >
            Benutzer
          </button>
          <button
            onClick={() => setActiveTab('courses')}
            className={`px-6 py-2.5 rounded-xl text-xs font-black transition-all ${activeTab === 'courses' ? 'bg-blue-600 text-white shadow-lg' : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-200'}`}
          >
            Kurse
          </button>
          <button
            onClick={() => setActiveTab('system')}
            className={`px-6 py-2.5 rounded-xl text-xs font-black transition-all ${activeTab === 'system' ? 'bg-blue-600 text-white shadow-lg' : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-200'}`}
          >
            System
          </button>
        </div>
      </div>

      {activeTab === 'users' && <UserManager />}

      {activeTab === 'courses' && (
        <div className="space-y-8">
          <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border dark:border-gray-700">
            <h3 className="text-xl font-black dark:text-white mb-6 flex items-center">
               <Plus className="mr-2 text-blue-500" /> Neuen Kurs anlegen
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <input 
                placeholder="Kursname" 
                className="w-full px-5 py-3 rounded-2xl border dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-bold dark:text-white"
                value={newCourse.courseName}
                onChange={e => setNewCourse({...newCourse, courseName: e.target.value})}
              />
              <input 
                placeholder="Instructor Course ID" 
                className="w-full px-5 py-3 rounded-2xl border dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-bold dark:text-white"
                value={newCourse.instructorCourseId}
                onChange={e => setNewCourse({...newCourse, instructorCourseId: e.target.value})}
              />
            </div>
            <button onClick={handleCreateCourse} className="mt-6 bg-blue-600 text-white px-8 py-3 rounded-2xl font-black hover:bg-blue-700 transition-all shadow-lg">
              Kurs erstellen
            </button>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-3xl overflow-hidden shadow-sm border dark:border-gray-700">
            <table className="w-full">
              <thead className="bg-gray-50 dark:bg-gray-900/50">
                <tr>
                  <th className="px-6 py-4 text-left text-[10px] font-black text-gray-400 uppercase tracking-widest">Name</th>
                  <th className="px-6 py-4 text-left text-[10px] font-black text-gray-400 uppercase tracking-widest">ID</th>
                  <th className="px-6 py-4 text-right text-[10px] font-black text-gray-400 uppercase tracking-widest">Aktion</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {courses.map(c => (
                  <React.Fragment key={c.id}>
                    <tr 
                      onClick={() => toggleCourse(c.id)}
                      className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors cursor-pointer"
                    >
                      <td className="px-6 py-4">
                        <div className="flex items-center">
                          {expandedCourse === c.id ? <ChevronUp size={16} className="mr-2 text-blue-500" /> : <ChevronDown size={16} className="mr-2 text-gray-400" />}
                          <span className="font-bold dark:text-white">{c.courseName}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 font-mono text-sm text-blue-500">{c.instructorCourseId}</td>
                      <td className="px-6 py-4 text-right">
                        <button 
                          onClick={(e) => { e.stopPropagation(); setDeleteModal({ isOpen: true, courseId: c.id }); }} 
                          className="text-gray-300 hover:text-red-500 transition-colors"
                        >
                          <Trash2 size={18} />
                        </button>
                      </td>
                    </tr>
                    {expandedCourse === c.id && (
                      <tr className="bg-gray-50/50 dark:bg-gray-900/30">
                        <td colSpan={3} className="px-12 py-6">
                          <div className="space-y-4">
                            <div className="flex items-center justify-between">
                              <h4 className="text-xs font-black text-gray-400 uppercase tracking-widest flex items-center">
                                <Users size={14} className="mr-2" /> Kursmitglieder
                              </h4>
                              <span className="text-[10px] font-black bg-blue-100 text-blue-600 px-2 py-0.5 rounded-full">
                                {courseMembers[c.id]?.length || 0} Personen
                              </span>
                            </div>
                            
                            {loadingMembers[c.id] ? (
                              <div className="flex items-center space-x-2 animate-pulse">
                                <div className="h-2 w-2 bg-blue-500 rounded-full"></div>
                                <div className="text-[10px] font-bold text-gray-400">Lade Mitglieder...</div>
                              </div>
                            ) : courseMembers[c.id]?.length === 0 ? (
                              <p className="text-sm text-gray-500 italic">Keine Mitglieder in diesem Kurs.</p>
                            ) : (
                              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
                                {courseMembers[c.id]?.map(member => (
                                  <div key={member.loginId} className="flex items-center p-3 bg-white dark:bg-gray-800 rounded-xl border dark:border-gray-700 shadow-sm">
                                    <div className="h-8 w-8 rounded-lg bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center mr-3">
                                      <span className="text-xs font-black text-blue-600 uppercase">
                                        {(member.username || "Unbekannt" || "??").substring(0, 2)}
                                      </span>
                                    </div>
                                    <div className="overflow-hidden">
                                      <p className="text-xs font-bold dark:text-white truncate">{member.username || "Unbekannt"}</p>
                                      <p className="text-[10px] text-gray-400 truncate">{member.loginId} • {member.role}</p>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'system' && (
        <div className="space-y-8 animate-slideUp">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white dark:bg-gray-800 p-6 rounded-3xl shadow-sm border dark:border-gray-700">
              <Cpu className="text-blue-500 mb-4" size={24} />
              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">CPU & Last</p>
              <p className="text-xl font-black dark:text-white">{systemStatus?.availableProcessors || '...'} Kerne / {systemStatus?.systemLoad?.toFixed(2) || '0.00'} Load</p>
            </div>
            <div className="bg-white dark:bg-gray-800 p-6 rounded-3xl shadow-sm border dark:border-gray-700">
              <Activity className="text-purple-500 mb-4" size={24} />
              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">RAM Auslastung</p>
              <p className="text-xl font-black dark:text-white">{systemStatus?.memoryUsed || '0'} MB / {systemStatus?.memoryMax || '0'} MB</p>
            </div>
            <div className="bg-white dark:bg-gray-800 p-6 rounded-3xl shadow-sm border dark:border-gray-700">
              <HardDrive className="text-amber-500 mb-4" size={24} />
              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Speicherplatz</p>
              <p className="text-xl font-black dark:text-white">{systemStatus?.diskFree || '0'} GB frei / {systemStatus?.diskTotal || '0'} GB</p>
            </div>
            <div className="bg-white dark:bg-gray-800 p-6 rounded-3xl shadow-sm border dark:border-gray-700">
              <Clock className="text-green-500 mb-4" size={24} />
              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Uptime</p>
              <p className="text-xl font-black dark:text-white">{(systemStatus?.uptime ? systemStatus.uptime / 3600 : 0).toFixed(1)} Stunden</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border dark:border-gray-700">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-xl font-black dark:text-white flex items-center">
                  <Server className="mr-2 text-blue-500" /> SMT Solver (Z3)
                </h3>
                {systemStatus?.z3Available ? (
                  <span className="bg-green-100 text-green-700 text-xs font-black px-3 py-1 rounded-full flex items-center">
                    <CheckCircle size={14} className="mr-1" /> AKTIV
                  </span>
                ) : (
                  <span className="bg-red-100 text-red-700 text-xs font-black px-3 py-1 rounded-full flex items-center">
                    <XCircle size={14} className="mr-1" /> INAKTIV
                  </span>
                )}
              </div>
              <p className="text-gray-500 dark:text-gray-400 text-sm mb-6 leading-relaxed">
                Der Z3 Solver wird für die mathematische Äquivalenzprüfung von SQL-Queries benötigt.
              </p>
              <button 
                onClick={loadSystemStatus}
                className="flex items-center text-xs font-black text-blue-500 bg-blue-50 dark:bg-blue-900/30 px-4 py-2 rounded-xl"
              >
                <RefreshCw size={14} className="mr-2" /> Status aktualisieren
              </button>
            </div>

            <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border dark:border-gray-700">
              <h3 className="text-xl font-black dark:text-white mb-6 flex items-center">
                <Database className="mr-2 text-indigo-500" /> Datenbank-Tester
              </h3>
              <p className="text-gray-500 dark:text-gray-400 text-sm mb-6">
                Testen Sie die Verbindung zu allen konfigurierten Instructor-Datenbanken.
              </p>
              <button 
                onClick={testConnection}
                disabled={testingDb}
                className="bg-blue-600 text-white px-8 py-3 rounded-2xl font-black shadow-lg disabled:opacity-50 transition-all"
              >
                {testingDb ? <RefreshCw className="animate-spin mr-2" /> : 'Verbindungen prüfen'}
              </button>
              {dbStatus && (
                <div className={`mt-6 p-6 rounded-2xl text-xs font-mono whitespace-pre-wrap border ${
                  dbStatus.includes('ERFOLGREICH') ? 'bg-green-50 text-green-700 border-green-100' : 'bg-red-50 text-red-700 border-red-100'
                }`}>
                  {dbStatus}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
