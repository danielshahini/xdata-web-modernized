import React, { useState } from 'react';
import api from '../api';
import { useAsyncData } from '../hooks/useAsyncData';
import { Database, Plus, Plug, FlaskConical, Edit, Trash2, Save, X } from 'lucide-react';
import { toast } from 'react-hot-toast';
import ConfirmationModal from './common/ConfirmationModal';
import InfoTip from './common/InfoTip';
import { Course } from '../types';

interface DbConnection {
  id?: number;
  name: string;
  url: string;
  user: string;
  password?: string;
  courseId: string;
}

const DbConnectionManager: React.FC = () => {
  const [editingConnection, setEditingConnection] = useState<DbConnection | null>(null);
  const [deleteModal, setDeleteModal] = useState<{ isOpen: boolean; id: number | null }>({
    isOpen: false,
    id: null
  });

  const { data: coursesData } = useAsyncData<Course[]>(
    () => api.get('/admin/courses').then(res => res.data || []),
    []
  );
  const courses = coursesData ?? [];

  const { data: connectionsData, loading: connectionsLoading, retry: reloadConnections } =
    useAsyncData<DbConnection[]>(
      () => api.get('/instructor/connections').then(res => res.data || []),
      []
    );
  const connections = connectionsData ?? [];

  const handleTest = async (id?: number) => {
    if (!id) return;
    try {
      const res = await api.get(`/instructor/connections/${id}/test`);
      if (res.data === true || res.data === "Connection successful") {
        toast.success("Verbindung erfolgreich!");
      } else {
        toast.error("Verbindung fehlgeschlagen — bitte Zugangsdaten und Erreichbarkeit der Datenbank prüfen.");
      }
    } catch (e: any) {
      toast.error("Verbindungstest fehlgeschlagen — prüfe URL, Zugangsdaten und Erreichbarkeit der Datenbank.");
    }
  };

  const handleSave = async () => {
    if (!editingConnection) return;
    if (!editingConnection.name || !editingConnection.url || !editingConnection.courseId) {
      toast.error("Bitte alle Pflichtfelder ausfüllen");
      return;
    }

    try {
      if (editingConnection.id) {
        await api.put(`/instructor/connections/${editingConnection.id}`, editingConnection);
      } else {
        await api.post('/instructor/connections', editingConnection);
      }
      setEditingConnection(null);
      reloadConnections();
      toast.success("Verbindung erfolgreich gespeichert");
    } catch (e) {
      toast.error("Fehler beim Speichern der Verbindung");
    }
  };

  const handleDelete = async () => {
    if (!deleteModal.id) return;
    try {
      await api.delete(`/instructor/connections/${deleteModal.id}`);
      toast.success("Verbindung gelöscht");
      reloadConnections();
    } catch (e) {
      toast.error("Fehler beim Löschen");
    }
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      <ConfirmationModal 
        isOpen={deleteModal.isOpen}
        onClose={() => setDeleteModal({ isOpen: false, id: null })}
        onConfirm={handleDelete}
        title="Verbindung löschen"
        message="Möchten Sie diese Datenbankverbindung wirklich löschen? Alle Aufgaben, die diese Verbindung nutzen, werden nicht mehr funktionieren."
      />
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800 dark:text-white tracking-tight">Datenbank <span className="text-brand-600">Verbindungen</span></h2>
        {!editingConnection && (
          <button 
            onClick={() => setEditingConnection({ name: '', url: '', user: '', password: '', courseId: '' })}
            className="btn-primary"
          >
            <Plus className="mr-2" size={18} /> Neue Verbindung
          </button>
        )}
      </div>

      {editingConnection ? (
        <div className="bg-white dark:bg-ink-card rounded-2xl border border-slate-200 dark:border-ink-border shadow-xl overflow-hidden animate-slideUp transition-colors">
          <div className="px-8 py-6 bg-gray-50 dark:bg-ink-soft/50 border-b border-slate-200 dark:border-ink-border flex justify-between items-center transition-colors">
            <h3 className="text-xl font-bold text-gray-800 dark:text-white">Verbindung <span className="text-brand-600">{editingConnection.id ? 'bearbeiten' : 'erstellen'}</span></h3>
            <div className="flex space-x-3">
              <button onClick={() => setEditingConnection(null)} className="btn-secondary"><X size={16} /> Abbrechen</button>
              <button onClick={handleSave} className="btn-primary"><Save size={16} /> Speichern</button>
            </div>
          </div>
          <div className="p-8 grid md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="x-label">Name der Verbindung</label>
              <input 
                className="x-input"
                value={editingConnection.name}
                onChange={e => setEditingConnection({...editingConnection, name: e.target.value})}
                placeholder="z.B. Postgres Haupt-DB"
              />
            </div>
            <div className="space-y-2">
              <label className="x-label">Kurs</label>
              <select 
                className="x-select"
                value={editingConnection.courseId}
                onChange={e => setEditingConnection({...editingConnection, courseId: e.target.value})}
              >
                <option value="" className="dark:bg-ink-soft">Kurs wählen...</option>
                {courses.map(c => <option key={c.id} value={c.instructorCourseId} className="dark:bg-ink-soft">{c.courseName} ({c.instructorCourseId})</option>)}
              </select>
            </div>
            <div className="md:col-span-2 space-y-2">
              <label className="x-label">
                JDBC URL
                <InfoTip 
                  title="JDBC Verbindungs-URL" 
                  content={
                    <div className="space-y-2">
                      <p>Das Format hängt von der verwendeten Datenbank ab:</p>
                      <ul className="list-disc ml-4 space-y-1">
                        <li><strong>PostgreSQL:</strong> jdbc:postgresql://localhost:5432/dbname</li>
                        <li><strong>MySQL:</strong> jdbc:mysql://localhost:3306/dbname</li>
                        <li><strong>Oracle:</strong> jdbc:oracle:thin:@localhost:1521:xe</li>
                      </ul>
                      <p className="mt-2 font-bold text-brand-500">Hinweis: Die Datenbank muss für das System erreichbar sein.</p>
                    </div>
                  } 
                />
              </label>
              <input 
                className="x-input font-mono"
                value={editingConnection.url}
                onChange={e => setEditingConnection({...editingConnection, url: e.target.value})}
                placeholder="jdbc:postgresql://localhost:5432/db"
              />
            </div>
            <div className="space-y-2">
              <label className="x-label">Benutzername</label>
              <input 
                className="x-input"
                value={editingConnection.user}
                onChange={e => setEditingConnection({...editingConnection, user: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <label className="x-label">Passwort</label>
              <input 
                type="password"
                className="x-input"
                value={editingConnection.password || ''}
                onChange={e => setEditingConnection({...editingConnection, password: e.target.value})}
                placeholder="••••••••"
              />
            </div>
          </div>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {connections.map(conn => (
            <div key={conn.id} className="x-card p-6 hover:shadow-card-hover transition-all group">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-brand-50 dark:bg-brand-950/40 text-brand-600 dark:text-brand-400 rounded-2xl flex items-center justify-center">
                  <Plug size={24} />
                </div>
                <div className="flex space-x-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button onClick={() => conn.id && handleTest(conn.id)} className="icon-btn hover:text-easy" title="Verbindung testen"><FlaskConical size={18}/></button>
                  <button onClick={() => setEditingConnection(conn)} className="icon-btn hover:text-brand-600" title="Bearbeiten"><Edit size={18}/></button>
                  <button onClick={() => conn.id && setDeleteModal({ isOpen: true, id: conn.id })} className="icon-btn hover:text-hard" title="Löschen"><Trash2 size={18}/></button>
                </div>
              </div>
              <h3 className="text-lg font-bold text-gray-800 dark:text-white">{conn.name}</h3>
              <p className="text-xs font-bold text-brand-600 bg-brand-50 dark:bg-brand-950/30 px-2 py-1 rounded-lg inline-block mt-2 uppercase">{conn.courseId}</p>
              <p className="text-xs text-gray-400 dark:text-gray-500 mt-4 font-mono truncate">{conn.url}</p>
              <div className="mt-4 pt-4 border-t border-gray-50 dark:border-ink-border flex items-center text-xs font-bold text-gray-500 dark:text-gray-400">
                <Database size={14} className="mr-2" /> User: {conn.user}
              </div>
            </div>
          ))}
          {connections.length === 0 && !connectionsLoading && (
            <div className="col-span-full py-20 text-center text-gray-400 italic bg-gray-50 dark:bg-ink-card/50 rounded-2xl border border-dashed border-gray-200 dark:border-ink-border transition-colors">
              Noch keine Datenbankverbindungen konfiguriert.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default DbConnectionManager;