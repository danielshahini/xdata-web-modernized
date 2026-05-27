import React, { useState, useEffect, useCallback } from 'react';
import api from '../api';
import { toast } from 'react-hot-toast';
import { 
  UserPlus, 
  Users, 
  Trash2, 
  Key, 
  UserCheck, 
  Upload as FileImport, 
  Search, 
  X,
  ShieldAlert as UserSecret,
  Edit2,
  CheckCircle,
  XCircle,
  ToggleLeft,
  ToggleRight
} from 'lucide-react';
import { User, Course } from '../types';
import ConfirmationModal from './common/ConfirmationModal';
import InfoTip from './common/InfoTip';

const UserManager: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [unassignedUsers, setUnassignedUsers] = useState<User[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [showAddExistingModal, setShowAddExistingModal] = useState(false);
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = currentUser.role?.trim().toUpperCase() === 'ADMIN';
  const isInstructor = currentUser.role?.trim().toUpperCase() === 'INSTRUCTOR';

  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  // Confirmation Modal state
  const [confirmConfig, setConfirmConfig] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    onConfirm: () => void;
    type: 'danger' | 'warning' | 'info';
  }>({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: () => {},
    type: 'danger'
  });

  const [newUser, setNewUser] = useState({ 
    username: '', 
    email: '', 
    role: 'STUDENT', 
    loginId: '', 
    password: '', 
    courseIds: [] as string[]
  });

  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editFormData, setEditFormData] = useState({
    username: '',
    email: '',
    role: '',
    courseIds: [] as string[]
  });

  const [showResetModal, setShowResetModal] = useState(false);
  const [resettingUser, setResettingUser] = useState<string | null>(null);
  const [newPassword, setNewPassword] = useState('');

  const loadUsers = useCallback(() => api.get('/admin/users')
    .then(res => setUsers(res.data || []))
    .catch(err => {
      console.error('Failed to load users', err);
      setUsers([]);
    }), []);

  const loadUnassignedUsers = useCallback(() => api.get('/admin/users/unassigned')
    .then(res => setUnassignedUsers(res.data || []))
    .catch(err => {
      console.error('Failed to load unassigned users', err);
      setUnassignedUsers([]);
    }), []);

  const loadCourses = useCallback(() => api.get('/admin/courses')
    .then(res => setCourses(res.data || []))
    .catch(err => {
      console.error('Failed to load courses', err);
      setCourses([]);
    }), []);

  useEffect(() => {
    loadUsers();
    loadCourses();
    if (isInstructor || isAdmin) {
      loadUnassignedUsers();
    }
  }, [loadUsers, loadCourses, loadUnassignedUsers, isInstructor, isAdmin]);

  const createUser = async () => {
    setLoading(true);
    try {
      await api.post('/admin/users', newUser);
      toast.success('Benutzer erfolgreich erstellt');
      setNewUser({ 
        username: '', 
        email: '', 
        role: 'STUDENT', 
        loginId: '', 
        password: '', 
        courseIds: []
      });
      loadUsers();
      loadUnassignedUsers();
    } catch (err: any) {
      toast.error(err.response?.data || 'Fehler beim Erstellen');
    } finally {
      setLoading(false);
    }
  };

  const assignCourse = async (loginId: string, courseId?: string) => {
    try {
      await api.post(`/admin/users/${loginId}/assign-course`, null, {
        params: { courseId: courseId }
      });
      toast.success('Kurs erfolgreich zugewiesen');
      loadUsers();
      loadUnassignedUsers();
      setShowAddExistingModal(false);
    } catch (err: any) {
      toast.error(err.response?.data || 'Fehler bei der Zuweisung');
    }
  };
  
  const handleEdit = (user: User) => {
    setEditingUser(user);
    setEditFormData({
      username: user.username || "Unbekannt",
      email: user.email || '',
      role: user.role,
      courseIds: user.courseIds || []
    });
    setShowEditModal(true);
  };

  const handleUpdateUser = async () => {
    if (!editingUser) return;
    setLoading(true);
    try {
      await api.put(`/admin/users/${editingUser.loginId}`, editFormData);
      toast.success('Benutzer erfolgreich aktualisiert');
      setShowEditModal(false);
      loadUsers();
      loadUnassignedUsers();
    } catch (err: any) {
      toast.error(err.response?.data || 'Fehler beim Aktualisieren');
    } finally {
      setLoading(false);
    }
  };

  const toggleStatus = (user: User) => {
    setConfirmConfig({
      isOpen: true,
      title: user.enabled !== false ? 'Benutzer deaktivieren' : 'Benutzer aktivieren',
      message: `Möchten Sie den Benutzer ${user.username || "Unbekannt"} wirklich ${user.enabled !== false ? 'deaktivieren' : 'aktivieren'}? ${user.enabled !== false ? 'Der Benutzer kann sich dann nicht mehr anmelden.' : ''}`,
      type: user.enabled !== false ? 'warning' : 'info',
      onConfirm: async () => {
        try {
          await api.patch(`/admin/users/${user.loginId}/toggle-status`);
          toast.success(`Benutzer ${user.enabled !== false ? 'deaktiviert' : 'aktiviert'}`);
          loadUsers();
        } catch (err: any) {
          toast.error(err.response?.data || 'Fehler beim Ändern des Status');
        }
      }
    });
  };

  const deleteUser = (id: string, name: string) => {
    setConfirmConfig({
      isOpen: true,
      title: 'Benutzer löschen',
      message: `Möchten Sie den Benutzer ${name} wirklich unwiderruflich löschen? Alle zugehörigen Daten gehen verloren.`,
      type: 'danger',
      onConfirm: async () => {
        try {
          await api.delete(`/admin/users/${id}`);
          toast.success('Benutzer gelöscht');
          loadUsers();
          loadUnassignedUsers();
        } catch (err: any) {
          toast.error(err.response?.data || 'Fehler beim Löschen');
        }
      }
    });
  };

  const handleResetPassword = async () => {
    if (!resettingUser || !newPassword) return;
    try {
      await api.post(`/admin/users/${resettingUser}/reset-password`, { password: newPassword });
      toast.success('Passwort erfolgreich zurückgesetzt');
      setShowResetModal(false);
      setNewPassword('');
    } catch (err: any) {
      toast.error(err.response?.data || 'Fehler beim Zurücksetzen');
    }
  };

  const handleImpersonate = (loginId: string, name: string) => {
    setConfirmConfig({
      isOpen: true,
      title: 'Benutzer-Impersonation',
      message: `Möchten Sie sich wirklich als ${name} anmelden? Ihre aktuelle Sitzung wird unterbrochen.`,
      type: 'info',
      onConfirm: async () => {
        try {
          const res = await api.post(`/admin/users/${loginId}/impersonate`);
          const { token, user } = res.data;
          localStorage.setItem('token', token);
          localStorage.setItem('user', JSON.stringify(user));
          toast.success(`Angemeldet als ${user.username || "Unbekannt"}.`);
          setTimeout(() => window.location.href = '/', 1000);
        } catch (err: any) {
          toast.error('Impersonation fehlgeschlagen: ' + (err.response?.data?.message || err.message));
        }
      }
    });
  };

  const handleCsvImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    if (isInstructor && currentUser.courseId) {
        formData.append('courseId', currentUser.courseId);
    }

    setLoading(true);
    try {
      const res = await api.post('/admin/users/import-csv', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      toast.success(`${res.data.imported} Studenten importiert (${res.data.skipped} übersprungen)`);
      loadUsers();
      loadUnassignedUsers();
    } catch (err: any) {
      toast.error('Fehler beim CSV Import: ' + (err.response?.data || err.message));
    } finally {
      setLoading(false);
      e.target.value = ''; // Reset input
    }
  };

  const filteredUsers = users.filter(u => 
    (u.username || "").toLowerCase().includes(searchTerm.toLowerCase()) || 
    (u.loginId || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
    (u.email || "").toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-10 animate-fadeIn text-gray-800 dark:text-gray-200">
      <ConfirmationModal 
        isOpen={confirmConfig.isOpen}
        title={confirmConfig.title}
        message={confirmConfig.message}
        type={confirmConfig.type}
        onConfirm={confirmConfig.onConfirm}
        onClose={() => setConfirmConfig({...confirmConfig, isOpen: false})}
      />

      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <h2 className="text-2xl font-black tracking-tight dark:text-white">
          Benutzer <span className="text-blue-600">Verwaltung</span>
        </h2>
        <div className="flex flex-wrap gap-3">
            <a 
                href={`${api.defaults.baseURL}/admin/users/template`} 
                className="bg-gray-50 dark:bg-gray-900/30 text-gray-600 dark:text-gray-400 border border-gray-200 dark:border-gray-800 py-2.5 px-4 rounded-xl font-bold hover:bg-gray-100 dark:hover:bg-gray-900/50 transition-all flex items-center shadow-sm"
                download
            >
                <FileImport size={18} className="mr-2" /> Vorlage laden
            </a>
            {isInstructor && (
              <button 
                onClick={() => { loadUnassignedUsers(); setShowAddExistingModal(true); }}
                className="bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-800 py-2.5 px-4 rounded-xl font-bold hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-all flex items-center shadow-sm"
              >
                <UserPlus size={18} className="mr-2" /> Bestehende hinzufügen
              </button>
            )}
            <label className="cursor-pointer bg-blue-600 text-white py-2.5 px-4 rounded-xl font-bold hover:bg-blue-700 transition-all flex items-center shadow-lg shadow-blue-500/20">
                <FileImport size={18} className="mr-2" /> CSV Importieren
                <input type="file" accept=".csv" className="hidden" onChange={handleCsvImport} disabled={loading} />
            </label>
            <InfoTip 
              title="CSV Import Format"
              content={
                <div className="space-y-2">
                  <p>Die CSV-Datei sollte folgende Spalten enthalten:</p>
                  <code className="block bg-gray-100 dark:bg-gray-900 p-2 rounded text-[10px]">
                    username,loginId,password,email,role,courseId
                  </code>
                  <p>Nutzen Sie den Button "Vorlage laden", um eine beispielhafte Datei herunterzuladen.</p>
                </div>
              }
            />
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-xl overflow-hidden relative transition-colors">
        <div className="absolute top-0 left-0 w-2 h-full bg-blue-600"></div>
        <h3 className="text-lg font-black mb-6 flex items-center dark:text-white">
          <UserPlus size={20} className="text-blue-500 mr-2" /> Neuen {isInstructor ? 'Studenten' : 'Benutzer'} anlegen
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div className="space-y-1">
            <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Vollständiger Name</label>
            <input 
              placeholder="z.B. Max Mustermann" 
              className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold text-gray-700 dark:text-gray-200 bg-gray-50 dark:bg-gray-900 focus:bg-white dark:focus:bg-gray-800 transition-all" 
              value={newUser.username} 
              onChange={e => setNewUser({...newUser, username: e.target.value})} 
            />
          </div>
          <div className="space-y-1">
            <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Login ID</label>
            <input 
              placeholder="z.B. mmuster" 
              className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold text-gray-700 dark:text-gray-200 bg-gray-50 dark:bg-gray-900 focus:bg-white dark:focus:bg-gray-800 transition-all" 
              value={newUser.loginId} 
              onChange={e => setNewUser({...newUser, loginId: e.target.value})} 
            />
          </div>
          <div className="space-y-1">
            <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">E-Mail Adresse</label>
            <input 
              placeholder="max@beispiel.de" 
              className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold text-gray-700 dark:text-gray-200 bg-gray-50 dark:bg-gray-900 focus:bg-white dark:focus:bg-gray-800 transition-all" 
              value={newUser.email} 
              onChange={e => setNewUser({...newUser, email: e.target.value})} 
            />
          </div>
          <div className="space-y-1">
            <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Passwort</label>
            <input 
              type="password" 
              placeholder="••••••••" 
              className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold text-gray-700 dark:text-gray-200 bg-gray-50 dark:bg-gray-900 focus:bg-white dark:focus:bg-gray-800 transition-all" 
              value={newUser.password} 
              onChange={e => setNewUser({...newUser, password: e.target.value})} 
            />
          </div>
          {isAdmin && (
            <>
              <div className="space-y-1">
                <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">
                  Rolle
                  <InfoTip 
                    title="Benutzerrollen"
                    content={
                      <div className="space-y-2">
                        <ul className="list-disc ml-4 space-y-1">
                          <li><strong>Student:</strong> Kann Aufgaben lösen und Playgrounds nutzen.</li>
                          <li><strong>Instructor:</strong> Kann Aufgaben, Schemata und Verbindungen verwalten.</li>
                          <li><strong>Admin:</strong> Hat Vollzugriff auf das gesamte System.</li>
                        </ul>
                      </div>
                    }
                  />
                </label>
                <select 
                  className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold text-gray-700 dark:text-gray-200 bg-gray-50 dark:bg-gray-900 focus:bg-white dark:focus:bg-gray-800 transition-all appearance-none" 
                  value={newUser.role} 
                  onChange={e => setNewUser({...newUser, role: e.target.value})}
                >
                  <option value="STUDENT">STUDENT</option>
                  <option value="INSTRUCTOR">INSTRUCTOR</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <div className="space-y-1 md:col-span-2">
                <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Kurs Zuweisung</label>
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2 p-3 rounded-2xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 max-h-[150px] overflow-y-auto">
                  {courses.map(c => (
                    <label key={c.instructorCourseId} className="flex items-center space-x-2 bg-white dark:bg-gray-800 p-2 rounded-xl border border-gray-100 dark:border-gray-700 cursor-pointer hover:border-blue-300 transition-all">
                      <input 
                        type="checkbox" 
                        className="rounded text-blue-600 focus:ring-blue-500"
                        checked={newUser.courseIds.includes(c.instructorCourseId)}
                        onChange={e => {
                          if (e.target.checked) {
                            setNewUser({...newUser, courseIds: [...newUser.courseIds, c.instructorCourseId]});
                          } else {
                            setNewUser({...newUser, courseIds: newUser.courseIds.filter(id => id !== c.instructorCourseId)});
                          }
                        }}
                      />
                      <span className="text-[11px] font-bold truncate dark:text-white" title={c.courseName}>{c.courseName}</span>
                    </label>
                  ))}
                  {courses.length === 0 && <p className="col-span-full text-center text-xs text-gray-400 italic py-2">Keine Kurse verfügbar</p>}
                </div>
              </div>
            </>
          )}
          {isInstructor && (
             <div className="space-y-1">
                <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Kurs Zuweisung (Meine Kurse)</label>
                <div className="flex flex-wrap gap-2 p-2 rounded-2xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900">
                    {courses.map(c => (
                        <label key={c.instructorCourseId} className="flex items-center space-x-2 bg-white dark:bg-gray-800 px-3 py-1.5 rounded-xl border border-gray-100 dark:border-gray-700 cursor-pointer">
                            <input 
                                type="checkbox" 
                                checked={newUser.courseIds.includes(c.instructorCourseId)}
                                onChange={e => {
                                    if (e.target.checked) {
                                        setNewUser({...newUser, courseIds: [...newUser.courseIds, c.instructorCourseId]});
                                    } else {
                                        setNewUser({...newUser, courseIds: newUser.courseIds.filter(id => id !== c.instructorCourseId)});
                                    }
                                }}
                            />
                            <span className="text-xs font-bold dark:text-white">{c.courseName}</span>
                        </label>
                    ))}
                </div>
             </div>
          )}
          <div className="flex items-end">
            <button 
              onClick={createUser} 
              disabled={loading}
              className="w-full bg-blue-600 text-white py-3 px-6 rounded-2xl font-black shadow-lg shadow-blue-100 dark:shadow-none hover:bg-blue-700 transition-all active:scale-95 flex items-center justify-center disabled:opacity-50"
            >
              {loading ? 'Verarbeite...' : <><UserCheck size={18} className="mr-2" /> {isInstructor ? 'Student Erstellen' : 'Benutzer Erstellen'}</>}
            </button>
          </div>
        </div>
      </div>
      
      <div className="bg-white dark:bg-gray-800 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden transition-colors">
        <div className="px-8 py-5 border-b border-gray-100 dark:border-gray-700 flex flex-col md:flex-row justify-between items-center gap-4 bg-gray-50/50 dark:bg-gray-900/50">
          <div className="flex items-center space-x-4 w-full md:w-auto">
            <h3 className="font-black text-gray-800 dark:text-white uppercase tracking-tight text-sm shrink-0">Zugeordnete Benutzer</h3>
            <span className="bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-400 px-3 py-1 rounded-full text-[10px] font-black shrink-0">{filteredUsers.length} Gesamt</span>
          </div>
          
          <div className="relative w-full md:w-64">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
            <input 
              type="text"
              placeholder="Suchen..."
              className="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm focus:ring-2 focus:ring-blue-500 outline-none transition-all dark:text-white"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white dark:bg-gray-800">
                <th className="px-8 py-4 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700">Status</th>
                <th className="px-8 py-4 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700">Name</th>
                <th className="px-8 py-4 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700">Login ID</th>
                <th className="px-8 py-4 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700">Kurs</th>
                <th className="px-8 py-4 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700">Rolle</th>
                <th className="px-8 py-4 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700 text-right">Aktionen</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-700">
              {filteredUsers.map(u => (
                <tr key={u.loginId} className="hover:bg-blue-50/30 dark:hover:bg-blue-900/10 transition-colors">
                  <td className="px-8 py-4">
                     {u.enabled !== false ? (
                        <span className="flex items-center text-green-500 font-bold text-[10px]">
                           <CheckCircle size={14} className="mr-1" /> AKTIV
                        </span>
                     ) : (
                        <span className="flex items-center text-red-500 font-bold text-[10px]">
                           <XCircle size={14} className="mr-1" /> INAKTIV
                        </span>
                     )}
                  </td>
                  <td className="px-8 py-4">
                    <div className="flex items-center">
                      <div className="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900/40 text-blue-600 dark:text-blue-400 flex items-center justify-center font-bold text-xs mr-3">
                        {(u.username || "?").charAt(0)}
                      </div>
                      <span className="font-bold text-gray-800 dark:text-gray-200">{u.username}</span>
                    </div>
                  </td>
                  <td className="px-8 py-4 font-mono text-sm text-gray-500 dark:text-gray-400">{u.loginId}</td>
                  <td className="px-8 py-4 text-xs font-bold text-gray-600 dark:text-gray-400">
                     <div className="flex flex-wrap gap-1">
                        {u.courseIds && u.courseIds.length > 0 ? u.courseIds.map(cid => (
                            <span key={cid} className="bg-gray-100 dark:bg-gray-700 px-2 py-0.5 rounded-md border border-gray-200 dark:border-gray-600 dark:text-gray-300">
                                {cid}
                            </span>
                        )) : <span className="text-gray-300 dark:text-gray-600 italic">Kein Kurs</span>}
                     </div>
                  </td>
                  <td className="px-8 py-4">
                    <span className={`px-3 py-1 rounded-full text-[10px] font-black tracking-tight ${
                      u.role === 'ADMIN' ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400' : 
                      u.role === 'INSTRUCTOR' ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400' : 
                      'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300'
                    }`}>
                      {u.role}
                    </span>
                  </td>
                  <td className="px-8 py-4 text-right">
                    <div className="flex items-center justify-end space-x-1">
                      <button 
                        onClick={() => toggleStatus(u)} 
                        className={`p-2 transition-colors ${u.enabled !== false ? 'text-gray-300 dark:text-gray-600 hover:text-orange-500' : 'text-orange-500 hover:text-orange-600'}`}
                        title={u.enabled !== false ? 'Benutzer deaktivieren' : 'Benutzer aktivieren'}
                      >
                        {u.enabled !== false ? <ToggleRight size={18} /> : <ToggleLeft size={18} />}
                      </button>
                      {isAdmin && u.loginId !== currentUser.loginId && (
                        <button 
                          onClick={() => handleImpersonate(u.loginId, u.username)} 
                          className="p-2 text-gray-300 dark:text-gray-600 hover:text-purple-500 dark:hover:text-purple-400 transition-colors"
                          title="Als dieser Benutzer anmelden"
                        >
                          <UserSecret size={18} />
                        </button>
                      )}
                      <button 
                        onClick={() => handleEdit(u)} 
                        className="p-2 text-gray-300 dark:text-gray-600 hover:text-green-500 dark:hover:text-green-400 transition-colors"
                        title="Benutzer bearbeiten"
                      >
                        <Edit2 size={18} />
                      </button>
                      <button 
                        onClick={() => { setResettingUser(u.loginId); setShowResetModal(true); }} 
                        className="p-2 text-gray-300 dark:text-gray-600 hover:text-blue-500 dark:hover:text-blue-400 transition-colors"
                        title="Passwort zurücksetzen"
                      >
                        <Key size={18} />
                      </button>
                      <button 
                        onClick={() => deleteUser(u.id, u.username)} 
                        className="p-2 text-gray-300 dark:text-gray-600 hover:text-red-500 dark:hover:text-red-400 transition-colors"
                        title="Benutzer löschen"
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showAddExistingModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fadeIn">
          <div className="bg-white dark:bg-gray-800 rounded-3xl p-8 max-w-2xl w-full shadow-2xl animate-slideUp max-h-[80vh] flex flex-col transition-colors border border-gray-100 dark:border-gray-700">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-black dark:text-white flex items-center">
                <Users className="mr-2 text-blue-500" size={24} /> Studenten ohne Kurszuweisung
              </h3>
              <button onClick={() => setShowAddExistingModal(false)} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200">
                <X size={24} />
              </button>
            </div>
            <div className="overflow-y-auto flex-1 pr-2 custom-scrollbar">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50 dark:bg-gray-900/50">
                    <th className="px-4 py-3 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700">Name</th>
                    <th className="px-4 py-3 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700">Login ID</th>
                    <th className="px-4 py-3 text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest border-b border-gray-100 dark:border-gray-700 text-right">Aktion</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50 dark:divide-gray-700">
                  {unassignedUsers.length === 0 ? (
                    <tr>
                      <td colSpan={3} className="px-4 py-12 text-center text-gray-400 font-bold italic">Keine kurslosen Studenten gefunden</td>
                    </tr>
                  ) : (
                    unassignedUsers.map(u => (
                      <tr key={u.loginId} className="hover:bg-blue-50/30 dark:hover:bg-blue-900/10 transition-colors">
                        <td className="px-4 py-3 font-bold text-gray-700 dark:text-gray-200">{u.username}</td>
                        <td className="px-4 py-3 font-mono text-sm text-gray-500 dark:text-gray-400">{u.loginId}</td>
                        <td className="px-4 py-3 text-right">
                          <div className="flex flex-wrap gap-2 justify-end">
                            {courses.filter(c => isAdmin || (isInstructor && currentUser.courseIds?.includes(c.instructorCourseId))).map(c => (
                                <button 
                                    key={c.instructorCourseId}
                                    onClick={() => assignCourse(u.loginId, c.instructorCourseId)}
                                    className="bg-blue-600/10 text-blue-600 dark:text-blue-400 py-1 px-3 rounded-lg text-[10px] font-black hover:bg-blue-600 hover:text-white transition-all border border-blue-200 dark:border-blue-800"
                                >
                                    + {c.courseName}
                                </button>
                            ))}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {showEditModal && editingUser && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fadeIn">
          <div className="bg-white dark:bg-gray-800 rounded-3xl p-8 max-w-2xl w-full shadow-2xl animate-slideUp border border-gray-100 dark:border-gray-700 transition-colors">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-black dark:text-white flex items-center">
                <Edit2 className="mr-2 text-blue-500" size={24} /> Benutzer bearbeiten: <span className="ml-2 text-blue-600">{editingUser.loginId}</span>
              </h3>
              <button onClick={() => setShowEditModal(false)} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200">
                <X size={24} />
              </button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
              <div className="space-y-1">
                <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Vollständiger Name</label>
                <input 
                  className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold bg-gray-50 dark:bg-gray-900 dark:text-white" 
                  value={editFormData.username} 
                  onChange={e => setEditFormData({...editFormData, username: e.target.value})} 
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">E-Mail Adresse</label>
                <input 
                  className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold bg-gray-50 dark:bg-gray-900 dark:text-white" 
                  value={editFormData.email} 
                  onChange={e => setEditFormData({...editFormData, email: e.target.value})} 
                />
              </div>
              {isAdmin && (
                <div className="space-y-1">
                  <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Rolle</label>
                  <select 
                    className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold bg-gray-50 dark:bg-gray-900 dark:text-white appearance-none" 
                    value={editFormData.role} 
                    onChange={e => setEditFormData({...editFormData, role: e.target.value})}
                  >
                    <option value="STUDENT">STUDENT</option>
                    <option value="INSTRUCTOR">INSTRUCTOR</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>
              )}
              
              <div className="space-y-1 md:col-span-2">
                <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Kurs Zuweisung</label>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 p-3 rounded-2xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 max-h-[200px] overflow-y-auto custom-scrollbar">
                  {courses.filter(c => isAdmin || (isInstructor && currentUser.courseIds?.includes(c.instructorCourseId))).map(c => (
                    <label key={c.instructorCourseId} className="flex items-center space-x-2 bg-white dark:bg-gray-800 p-2 rounded-xl border border-gray-100 dark:border-gray-700 cursor-pointer hover:border-blue-300 transition-all">
                      <input 
                        type="checkbox" 
                        className="rounded text-blue-600 focus:ring-blue-500"
                        checked={editFormData.courseIds.includes(c.instructorCourseId)}
                        onChange={e => {
                          if (e.target.checked) {
                            setEditFormData({...editFormData, courseIds: [...editFormData.courseIds, c.instructorCourseId]});
                          } else {
                            setEditFormData({...editFormData, courseIds: editFormData.courseIds.filter(id => id !== c.instructorCourseId)});
                          }
                        }}
                      />
                      <span className="text-[11px] font-bold truncate dark:text-white" title={c.courseName}>{c.courseName}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex gap-4">
              <button 
                onClick={() => setShowEditModal(false)}
                className="flex-1 px-6 py-3 rounded-2xl font-black text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all"
              >
                Abbrechen
              </button>
              <button 
                onClick={handleUpdateUser}
                disabled={loading}
                className="flex-1 px-6 py-3 rounded-2xl font-black bg-blue-600 text-white hover:bg-blue-700 shadow-lg shadow-blue-100 dark:shadow-none transition-all disabled:opacity-50"
              >
                {loading ? 'Speichern...' : 'Änderungen speichern'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showResetModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fadeIn">
          <div className="bg-white dark:bg-gray-800 rounded-3xl p-8 max-w-md w-full shadow-2xl animate-slideUp border border-gray-100 dark:border-gray-700">
            <div className="flex items-center space-x-3 mb-4">
               <div className="bg-orange-100 dark:bg-orange-900/30 p-2 rounded-lg">
                  <Key className="text-orange-600 dark:text-orange-400" size={20} />
               </div>
               <h3 className="text-xl font-black dark:text-white">Passwort zurücksetzen</h3>
            </div>
            <p className="text-gray-500 dark:text-gray-400 mb-6 font-medium leading-relaxed">
              Geben Sie ein neues Passwort für <span className="text-blue-600 font-bold">{resettingUser}</span> ein.
            </p>
            <input 
              type="password" 
              className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold mb-6 bg-gray-50 dark:bg-gray-900 dark:text-white" 
              placeholder="Neues Passwort"
              value={newPassword}
              onChange={e => setNewPassword(e.target.value)}
              autoFocus
            />
            <div className="flex gap-4">
              <button 
                onClick={() => setShowResetModal(false)}
                className="flex-1 px-6 py-3 rounded-2xl font-black text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all"
              >
                Abbrechen
              </button>
              <button 
                onClick={handleResetPassword}
                className="flex-1 px-6 py-3 rounded-2xl font-black bg-blue-600 text-white hover:bg-blue-700 shadow-lg shadow-blue-100 dark:shadow-none transition-all"
              >
                Speichern
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManager;
