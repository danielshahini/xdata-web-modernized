import React, { useState, useEffect } from 'react';
import api from '../api';

interface LmsCredential {
  id?: number;
  lmsName: string;
  clientId: string;
  clientSecret: string;
  authUrl: string;
  tokenUrl: string;
}

const LmsManager: React.FC = () => {
  const [credentials, setCredentials] = useState<LmsCredential[]>([]);
  const [editing, setEditing] = useState<LmsCredential | null>(null);

  useEffect(() => {
    loadCredentials();
  }, []);

  const loadCredentials = async () => {
    try {
      const res = await api.get('/admin/lms');
      setCredentials(res.data);
    } catch (e) {}
  };

  const handleSave = async () => {
    if (!editing) return;
    try {
      if (editing.id) {
        await api.put(`/admin/lms/${editing.id}`, editing);
      } else {
        await api.post('/admin/lms', editing);
      }
      setEditing(null);
      loadCredentials();
      alert("LMS Konfiguration gespeichert");
    } catch (e) {
      alert("Fehler beim Speichern");
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("LMS Setup wirklich löschen?")) return;
    try {
      await api.delete(`/admin/lms/${id}`);
      loadCredentials();
    } catch (e) {
      alert("Fehler beim Löschen");
    }
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-black text-gray-800 dark:text-white tracking-tight">LTI / LMS <span className="text-blue-600">Setup</span></h2>
        {!editing && (
          <button 
            onClick={() => setEditing({ lmsName: '', clientId: '', clientSecret: '', authUrl: '', tokenUrl: '' })}
            className="px-6 py-3 bg-blue-600 text-white rounded-2xl font-black text-sm hover:bg-blue-700 transition-all shadow-lg shadow-blue-100 flex items-center"
          >
            <i className="fas fa-plus mr-2"></i> Neues LMS Setup
          </button>
        )}
      </div>

      {editing ? (
        <div className="bg-white dark:bg-gray-800 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-xl overflow-hidden animate-slideUp transition-colors">
          <div className="px-8 py-6 bg-gray-50 dark:bg-gray-900/50 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center transition-colors">
            <h3 className="text-xl font-black text-gray-800 dark:text-white">LMS <span className="text-blue-600">{editing.id ? 'bearbeiten' : 'konfigurieren'}</span></h3>
            <div className="flex space-x-3">
              <button onClick={() => setEditing(null)} className="px-6 py-2.5 rounded-xl font-bold text-sm text-gray-500 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700 transition-all">Abbrechen</button>
              <button onClick={handleSave} className="px-8 py-2.5 rounded-xl font-black text-sm text-white bg-blue-600 hover:bg-blue-700 shadow-lg shadow-blue-100 transition-all">Speichern</button>
            </div>
          </div>
          <div className="p-8 grid md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-xs font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">LMS Name</label>
              <input 
                className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-700 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none font-bold transition-colors" 
                value={editing.lmsName}
                onChange={e => setEditing({...editing, lmsName: e.target.value})}
                placeholder="z.B. Moodle, Canvas"
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Client ID</label>
              <input 
                className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-700 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none font-bold transition-colors" 
                value={editing.clientId}
                onChange={e => setEditing({...editing, clientId: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Client Secret</label>
              <input 
                type="password"
                className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-700 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none font-bold transition-colors" 
                value={editing.clientSecret}
                onChange={e => setEditing({...editing, clientSecret: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Auth URL</label>
              <input 
                className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-700 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none font-bold transition-colors" 
                value={editing.authUrl}
                onChange={e => setEditing({...editing, authUrl: e.target.value})}
              />
            </div>
            <div className="md:col-span-2 space-y-2">
              <label className="text-xs font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Token URL</label>
              <input 
                className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-700 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none font-bold transition-colors" 
                value={editing.tokenUrl}
                onChange={e => setEditing({...editing, tokenUrl: e.target.value})}
              />
            </div>
          </div>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {credentials.map(c => (
            <div key={c.id} className="bg-white dark:bg-gray-800 p-6 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-sm hover:shadow-md transition-all group">
              <div className="flex justify-between items-start">
                <div className="flex items-center space-x-4">
                  <div className="w-12 h-12 bg-blue-50 dark:bg-blue-900/40 text-blue-600 dark:text-blue-400 rounded-2xl flex items-center justify-center">
                    <i className="fas fa-university text-xl"></i>
                  </div>
                  <div>
                    <h3 className="text-lg font-black text-gray-800 dark:text-white">{c.lmsName}</h3>
                    <p className="text-xs font-bold text-gray-400 dark:text-gray-500 uppercase tracking-tighter">ID: {c.clientId.substring(0, 8)}...</p>
                  </div>
                </div>
                <div className="flex space-x-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button onClick={() => setEditing(c)} className="p-2 text-gray-400 dark:text-gray-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"><i className="fas fa-edit"></i></button>
                  <button onClick={() => c.id && handleDelete(c.id)} className="p-2 text-gray-400 dark:text-gray-500 hover:text-red-600 dark:hover:text-red-400 transition-colors"><i className="fas fa-trash-alt"></i></button>
                </div>
              </div>
              <div className="mt-6 space-y-3">
                <div className="flex items-center text-xs font-bold text-gray-500 dark:text-gray-400">
                  <div className="w-20">Auth URL:</div>
                  <div className="text-gray-400 dark:text-gray-500 font-mono truncate flex-1">{c.authUrl}</div>
                </div>
                <div className="flex items-center text-xs font-bold text-gray-500 dark:text-gray-400">
                  <div className="w-20">Token URL:</div>
                  <div className="text-gray-400 dark:text-gray-500 font-mono truncate flex-1">{c.tokenUrl}</div>
                </div>
              </div>
            </div>
          ))}
          {credentials.length === 0 && (
            <div className="col-span-full py-20 text-center text-gray-400 italic bg-gray-50 dark:bg-gray-800/50 rounded-3xl border border-dashed border-gray-200 dark:border-gray-700 transition-colors">
              Noch keine LMS Verbindungen eingerichtet.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default LmsManager;