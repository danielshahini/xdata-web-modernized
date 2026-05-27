import React, { useState, useEffect } from 'react';
import api from '../api';

interface AuditLog {
  id: number;
  action: string;
  performedBy: string;
  targetUser: string;
  details: string;
  timestamp: string;
}

const AuditLogViewer: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [filters, setFilters] = useState({
    username: '',
    action: '',
    from: '',
    to: ''
  });

  useEffect(() => {
    const params = new URLSearchParams();
    if (filters.username) params.append('username', filters.username);
    if (filters.action) params.append('action', filters.action);
    if (filters.from) params.append('from', new Date(filters.from).toISOString());
    if (filters.to) params.append('to', new Date(filters.to).toISOString());

    api.get(`/admin/audit-logs?${params.toString()}`)
      .then(res => setLogs(res.data))
      .catch(() => {});
  }, [filters]);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h3 className="text-xl font-black text-gray-800 dark:text-white uppercase tracking-tight">Audit <span className="text-blue-600">Logs</span></h3>
        <button 
          onClick={() => setFilters({username: '', action: '', from: '', to: ''})}
          className="text-xs font-black text-blue-600 uppercase hover:underline"
        >
          Filter zurücksetzen
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 bg-gray-50 dark:bg-gray-800/50 p-6 rounded-3xl border border-gray-100 dark:border-gray-700 transition-colors">
        <div className="space-y-1">
          <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Benutzer</label>
          <input 
            placeholder="Name..." 
            className="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm font-bold focus:ring-2 focus:ring-blue-500 outline-none dark:text-white transition-colors"
            value={filters.username}
            onChange={e => setFilters({...filters, username: e.target.value})}
          />
        </div>
        <div className="space-y-1">
          <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Aktion</label>
          <input 
            placeholder="z.B. DELETE..." 
            className="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm font-bold focus:ring-2 focus:ring-blue-500 outline-none dark:text-white transition-colors"
            value={filters.action}
            onChange={e => setFilters({...filters, action: e.target.value})}
          />
        </div>
        <div className="space-y-1">
          <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Von</label>
          <input 
            type="date"
            className="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm font-bold focus:ring-2 focus:ring-blue-500 outline-none dark:text-white transition-colors"
            value={filters.from}
            onChange={e => setFilters({...filters, from: e.target.value})}
          />
        </div>
        <div className="space-y-1">
          <label className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest ml-1">Bis</label>
          <input 
            type="date"
            className="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm font-bold focus:ring-2 focus:ring-blue-500 outline-none dark:text-white transition-colors"
            value={filters.to}
            onChange={e => setFilters({...filters, to: e.target.value})}
          />
        </div>
      </div>
      <div className="bg-white dark:bg-gray-800 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden transition-colors">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-sm">
            <thead>
              <tr className="bg-gray-50/50 dark:bg-gray-900/50 transition-colors">
                <th className="px-6 py-4 font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest text-[10px] border-b border-gray-100 dark:border-gray-700">Zeitpunkt</th>
                <th className="px-6 py-4 font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest text-[10px] border-b border-gray-100 dark:border-gray-700">Aktion</th>
                <th className="px-6 py-4 font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest text-[10px] border-b border-gray-100 dark:border-gray-700">Ausgeführt von</th>
                <th className="px-6 py-4 font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest text-[10px] border-b border-gray-100 dark:border-gray-700">Ziel</th>
                <th className="px-6 py-4 font-black text-gray-400 dark:text-gray-500 uppercase tracking-widest text-[10px] border-b border-gray-100 dark:border-gray-700">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-700 transition-colors">
              {logs.map(log => (
                <tr key={log.id} className="hover:bg-blue-50/20 dark:hover:bg-blue-900/20 transition-colors">
                  <td className="px-6 py-4 text-gray-500 dark:text-gray-400 font-medium">
                    {new Date(log.timestamp).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 font-bold text-gray-800 dark:text-white">
                    <span className={`px-2 py-1 rounded-lg text-[10px] font-black ${
                        log.action.includes('DELETED') ? 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400' :
                        log.action.includes('CREATED') ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' :
                        log.action.includes('RESET') ? 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400' : 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400'
                    }`}>
                        {log.action}
                    </span>
                  </td>
                  <td className="px-6 py-4 font-bold text-blue-600 dark:text-blue-400">{log.performedBy}</td>
                  <td className="px-6 py-4 font-bold text-gray-700 dark:text-gray-200">{log.targetUser || '-'}</td>
                  <td className="px-6 py-4 text-gray-500 dark:text-gray-400">{log.details}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AuditLogViewer;
