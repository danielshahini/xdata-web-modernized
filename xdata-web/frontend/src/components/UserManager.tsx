import React, { useState } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
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
  RefreshCw,
  ToggleLeft,
  ToggleRight
} from 'lucide-react';
import { User, Course } from '../types';
import { useAuth } from '../context/AuthContext';
import ConfirmationModal from './common/ConfirmationModal';
import InfoTip from './common/InfoTip';

const UserManager: React.FC = () => {
  const { user: currentUser, isAdmin, isInstructor } = useAuth();
  const [showAddExistingModal, setShowAddExistingModal] = useState(false);

  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<'newest' | 'oldest' | 'name'>('newest');

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

  const { data: usersData, retry: reloadUsers } = useAsyncData<User[]>(
    // The backend returns each user's enrolled `courses` (objects), not a flat
    // `courseIds` array. Normalize so the course column and the edit modal's
    // checkboxes reflect the actual assignments.
    () => api.get('/admin/users').then(res => (res.data || []).map((u: any) => ({
      ...u,
      courseIds: (u.courseIds && u.courseIds.length)
        ? u.courseIds
        : (u.courses || []).map((c: any) => c.instructorCourseId),
    }))),
    []
  );
  const users = usersData ?? [];

  const { data: unassignedData, retry: reloadUnassigned } = useAsyncData<User[]>(
    () => (isInstructor || isAdmin)
      ? api.get('/admin/users/unassigned').then(res => res.data || [])
      : Promise.resolve([]),
    [isInstructor, isAdmin]
  );
  const unassignedUsers = unassignedData ?? [];

  const { data: coursesData } = useAsyncData<Course[]>(
    () => api.get('/admin/courses').then(res => res.data || []),
    []
  );
  const courses = coursesData ?? [];

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
      reloadUsers();
      reloadUnassigned();
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
      reloadUsers();
      reloadUnassigned();
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
      reloadUsers();
      reloadUnassigned();
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
          reloadUsers();
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
          reloadUsers();
          reloadUnassigned();
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

  const downloadTemplate = async () => {
    try {
      // Fetch through the authenticated api client (a plain <a download> cannot
      // send the JWT, which made the endpoint return 401).
      const res = await api.get('/admin/users/template', { responseType: 'blob' });
      const url = URL.createObjectURL(res.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'user_import_template.csv';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      toast.error('Vorlage konnte nicht geladen werden');
    }
  };

  const handleCsvImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    if (isInstructor && currentUser && currentUser.courseId) {
        formData.append('courseId', currentUser.courseId);
    }

    setLoading(true);
    try {
      const res = await api.post('/admin/users/import-csv', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      toast.success(`${res.data.imported} Studenten importiert (${res.data.skipped} übersprungen)`);
      reloadUsers();
      reloadUnassigned();
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
  ).sort((a, b) => {
    if (sortBy === 'name') return (a.username || '').localeCompare(b.username || '');
    const ta = a.createdAt ? new Date(a.createdAt).getTime() : 0;
    const tb = b.createdAt ? new Date(b.createdAt).getTime() : 0;
    return sortBy === 'newest' ? tb - ta : ta - tb;
  });

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
        <div>
          <p className="kicker">{isInstructor ? 'Kurs-Teilnehmer' : 'Benutzer'}</p>
          <h2 className="mt-1 section-title text-2xl">Benutzerverwaltung</h2>
        </div>
        <div className="flex flex-wrap items-center gap-2">
            <button onClick={downloadTemplate} className="btn-secondary">
                <FileImport size={16} /> Vorlage
            </button>
            {isInstructor && (
              <button
                onClick={() => { reloadUnassigned(); setShowAddExistingModal(true); }}
                className="btn-secondary"
              >
                <UserPlus size={16} /> Bestehende hinzufügen
              </button>
            )}
            <label className="btn-primary cursor-pointer">
                <FileImport size={16} /> CSV importieren
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

      <div className="x-card p-6 sm:p-8 relative overflow-hidden">
        <div className="absolute top-0 left-0 w-1 h-full bg-brand-600"></div>
        <h3 className="section-title flex items-center gap-2 mb-6">
          <UserPlus size={20} className="text-brand-500" /> Neuen {isInstructor ? 'Studenten' : 'Benutzer'} anlegen
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          <div className="space-y-1">
            <label className="x-label">Vollständiger Name</label>
            <input 
              placeholder="z.B. Max Mustermann" 
              className="x-input" 
              value={newUser.username} 
              onChange={e => setNewUser({...newUser, username: e.target.value})} 
            />
          </div>
          <div className="space-y-1">
            <label className="x-label">Login ID</label>
            <input 
              placeholder="z.B. mmuster" 
              className="x-input" 
              value={newUser.loginId} 
              onChange={e => setNewUser({...newUser, loginId: e.target.value})} 
            />
          </div>
          <div className="space-y-1">
            <label className="x-label">E-Mail Adresse</label>
            <input 
              placeholder="max@beispiel.de" 
              className="x-input" 
              value={newUser.email} 
              onChange={e => setNewUser({...newUser, email: e.target.value})} 
            />
          </div>
          <div className="space-y-1">
            <label className="x-label">Passwort</label>
            <input 
              type="password" 
              placeholder="••••••••" 
              className="x-input" 
              value={newUser.password} 
              onChange={e => setNewUser({...newUser, password: e.target.value})} 
            />
          </div>
          {isAdmin && (
            <>
              <div className="space-y-1">
                <label className="x-label">
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
                  className="x-select"
                  value={newUser.role}
                  onChange={e => setNewUser({...newUser, role: e.target.value})}
                  disabled={!isAdmin}
                >
                  <option value="STUDENT">STUDENT</option>
                  {isAdmin && <option value="TUTOR">TUTOR</option>}
                  {isAdmin && <option value="INSTRUCTOR">INSTRUCTOR</option>}
                  {isAdmin && <option value="ADMIN">ADMIN</option>}
                </select>
              </div>
              <div className="space-y-1 md:col-span-2">
                <label className="x-label">Kurs Zuweisung</label>
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2 p-3 rounded-2xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 max-h-[150px] overflow-y-auto">
                  {courses.map(c => (
                    <label key={c.instructorCourseId} className="flex items-center space-x-2 bg-white dark:bg-ink-card p-2 rounded-xl border border-slate-200 dark:border-ink-border cursor-pointer hover:border-brand-300 transition-all">
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
                <label className="x-label">Kurs Zuweisung (Meine Kurse)</label>
                <div className="flex flex-wrap gap-2 p-2 rounded-2xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900">
                    {courses.map(c => (
                        <label key={c.instructorCourseId} className="flex items-center space-x-2 bg-white dark:bg-ink-card px-3 py-1.5 rounded-xl border border-slate-200 dark:border-ink-border cursor-pointer">
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
              className="btn-primary w-full"
            >
              {loading ? <><RefreshCw size={16} className="animate-spin" /> Verarbeite…</> : <><UserCheck size={16} /> {isInstructor ? 'Student erstellen' : 'Benutzer erstellen'}</>}
            </button>
          </div>
        </div>
      </div>
      
      <div className="x-card overflow-hidden">
        <div className="px-5 sm:px-6 py-4 border-b border-slate-100 dark:border-ink-border flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="flex items-center gap-3 w-full md:w-auto">
            <h3 className="section-title text-base shrink-0">Zugeordnete Benutzer</h3>
            <span className="badge-neutral shrink-0">{filteredUsers.length} gesamt</span>
          </div>

          <div className="flex items-center gap-2 w-full md:w-auto">
            <div className="relative flex-1 md:w-72">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
              <input
                type="text"
                placeholder="Name, Login-ID oder E-Mail…"
                className="x-input pl-10 py-2"
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
            <select
              className="x-select py-2 w-auto shrink-0"
              value={sortBy}
              onChange={e => setSortBy(e.target.value as 'newest' | 'oldest' | 'name')}
              title="Sortierung"
            >
              <option value="newest">Neueste zuerst</option>
              <option value="oldest">Älteste zuerst</option>
              <option value="name">Name (A–Z)</option>
            </select>
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr>
                <th className="x-th">Status</th>
                <th className="x-th">Name</th>
                <th className="x-th">Login-ID</th>
                <th className="x-th">Kurs</th>
                <th className="x-th">Rolle</th>
                <th className="x-th">Erstellt</th>
                <th className="x-th text-right">Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map(u => (
                <tr key={u.loginId} className="x-row">
                  <td className="x-td">
                     {u.enabled !== false
                        ? <span className="badge-success"><CheckCircle size={12} /> Aktiv</span>
                        : <span className="badge bg-hard/10 text-hard ring-hard/20"><XCircle size={12} /> Inaktiv</span>}
                  </td>
                  <td className="x-td">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-lg bg-brand-500/10 text-brand-600 dark:text-brand-300 flex items-center justify-center font-bold text-xs uppercase shrink-0">
                        {(u.username || "?").charAt(0)}
                      </div>
                      <span className="font-semibold text-slate-900 dark:text-white">{u.username}</span>
                    </div>
                  </td>
                  <td className="x-td"><span className="font-mono text-slate-500 dark:text-slate-400">{u.loginId}</span></td>
                  <td className="x-td">
                     <div className="flex flex-wrap gap-1">
                        {u.courseIds && u.courseIds.length > 0 ? u.courseIds.map(cid => (
                            <span key={cid} className="badge-neutral font-mono normal-case">{cid}</span>
                        )) : <span className="text-slate-300 dark:text-slate-600 italic text-sm">Kein Kurs</span>}
                     </div>
                  </td>
                  <td className="x-td">
                    <span className={
                      u.role?.trim().toUpperCase() === 'ADMIN' ? 'badge bg-violet-500/10 text-violet-600 dark:text-violet-300 ring-violet-500/20' :
                      u.role?.trim().toUpperCase() === 'INSTRUCTOR' ? 'badge-brand' :
                      'badge-neutral'
                    }>
                      {u.role}
                    </span>
                  </td>
                  <td className="x-td">
                    <span className="text-sm text-slate-500 dark:text-slate-400 tabular-nums">
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString('de-DE') : '–'}
                    </span>
                  </td>
                  <td className="x-td text-right">
                    <div className="flex items-center justify-end space-x-1">
                      <button 
                        onClick={() => toggleStatus(u)} 
                        className={`p-2 transition-colors ${u.enabled !== false ? 'text-gray-300 dark:text-gray-600 hover:text-orange-500' : 'text-orange-500 hover:text-orange-600'}`}
                        title={u.enabled !== false ? 'Benutzer deaktivieren' : 'Benutzer aktivieren'}
                      >
                        {u.enabled !== false ? <ToggleRight size={18} /> : <ToggleLeft size={18} />}
                      </button>
                      {isAdmin && u.loginId !== currentUser?.loginId && (
                        <button 
                          onClick={() => handleImpersonate(u.loginId, u.username)} 
                          className="icon-btnhover:text-purple-500 dark:hover:text-purple-400 transition-colors"
                          title="Als dieser Benutzer anmelden"
                        >
                          <UserSecret size={18} />
                        </button>
                      )}
                      <button 
                        onClick={() => handleEdit(u)} 
                        className="icon-btnhover:text-green-500 dark:hover:text-green-400 transition-colors"
                        title="Benutzer bearbeiten"
                      >
                        <Edit2 size={18} />
                      </button>
                      <button 
                        onClick={() => { setResettingUser(u.loginId); setShowResetModal(true); }} 
                        className="icon-btnhover:text-blue-500 dark:hover:text-blue-400 transition-colors"
                        title="Passwort zurücksetzen"
                      >
                        <Key size={18} />
                      </button>
                      <button 
                        onClick={() => deleteUser(u.id, u.username)} 
                        className="icon-btnhover:text-red-500 dark:hover:text-red-400 transition-colors"
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
        <div className="modal-overlay">
          <div className="modal-card max-w-2xl p-6 sm:p-8 max-h-[85vh] flex flex-col">
            <div className="flex justify-between items-center mb-6">
              <h3 className="section-title flex items-center gap-2">
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
                    <th className="x-th">Name</th>
                    <th className="x-th">Login ID</th>
                    <th className="x-th">Aktion</th>
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
                            {courses.filter(c => isAdmin || (isInstructor && currentUser?.courseIds?.includes(c.instructorCourseId))).map(c => (
                                <button 
                                    key={c.instructorCourseId}
                                    onClick={() => assignCourse(u.loginId, c.instructorCourseId)}
                                    className="badge-brand hover:bg-brand-600 hover:text-white cursor-pointer transition-colors"
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
        <div className="modal-overlay">
          <div className="modal-card max-w-2xl p-6 sm:p-8">
            <div className="flex justify-between items-center mb-6">
              <h3 className="section-title flex items-center gap-2">
                <Edit2 className="mr-2 text-blue-500" size={24} /> Benutzer bearbeiten: <span className="ml-2 text-blue-600">{editingUser.loginId}</span>
              </h3>
              <button onClick={() => setShowEditModal(false)} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200">
                <X size={24} />
              </button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
              <div className="space-y-1">
                <label className="x-label">Vollständiger Name</label>
                <input 
                  className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold bg-gray-50 dark:bg-gray-900 dark:text-white" 
                  value={editFormData.username} 
                  onChange={e => setEditFormData({...editFormData, username: e.target.value})} 
                />
              </div>
              <div className="space-y-1">
                <label className="x-label">E-Mail Adresse</label>
                <input 
                  className="w-full px-5 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold bg-gray-50 dark:bg-gray-900 dark:text-white" 
                  value={editFormData.email} 
                  onChange={e => setEditFormData({...editFormData, email: e.target.value})} 
                />
              </div>
              {isAdmin && (
                <div className="space-y-1">
                  <label className="x-label">Rolle</label>
                  <select
                    className="x-select"
                    value={editFormData.role}
                    onChange={e => setEditFormData({...editFormData, role: e.target.value})}
                    disabled={!isAdmin}
                  >
                    <option value="STUDENT">STUDENT</option>
                    {isAdmin && <option value="TUTOR">TUTOR</option>}
                    {isAdmin && <option value="INSTRUCTOR">INSTRUCTOR</option>}
                    {isAdmin && <option value="ADMIN">ADMIN</option>}
                  </select>
                </div>
              )}
              
              <div className="space-y-1 md:col-span-2">
                <label className="x-label">Kurs Zuweisung</label>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 p-3 rounded-2xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 max-h-[200px] overflow-y-auto custom-scrollbar">
                  {courses.filter(c => isAdmin || (isInstructor && currentUser?.courseIds?.includes(c.instructorCourseId))).map(c => (
                    <label key={c.instructorCourseId} className="flex items-center space-x-2 bg-white dark:bg-ink-card p-2 rounded-xl border border-slate-200 dark:border-ink-border cursor-pointer hover:border-brand-300 transition-all">
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
                className="btn-secondary flex-1"
              >
                Abbrechen
              </button>
              <button 
                onClick={handleUpdateUser}
                disabled={loading}
                className="btn-primary flex-1"
              >
                {loading ? 'Speichern...' : 'Änderungen speichern'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showResetModal && (
        <div className="modal-overlay">
          <div className="modal-card max-w-md p-6 sm:p-8">
            <div className="flex items-center space-x-3 mb-4">
               <div className="bg-orange-100 dark:bg-orange-900/30 p-2 rounded-lg">
                  <Key className="text-orange-600 dark:text-orange-400" size={20} />
               </div>
               <h3 className="section-title">Passwort zurücksetzen</h3>
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
                className="btn-secondary flex-1"
              >
                Abbrechen
              </button>
              <button 
                onClick={handleResetPassword}
                className="btn-primary flex-1"
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
