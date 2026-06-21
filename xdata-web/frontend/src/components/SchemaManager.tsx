import React, { useState } from 'react';
import api from '../api';
import { useAsyncData } from '../hooks/useAsyncData';
import { 
  Database, 
  Upload, 
  FileCode, 
  Trash2, 
  Eye, 
  Table, 
  X, 
  Layers, 
  GraduationCap, 
  MousePointer, 
  RefreshCw,
  FileCheck,
  CloudUpload
} from 'lucide-react';
import { toast } from 'react-hot-toast';
import ConfirmationModal from './common/ConfirmationModal';
import InfoTip from './common/InfoTip';
import { Course } from '../types';

interface SchemaInfo {
  id: number;
  schemaName: string;
  courseId: string;
  content: string;
}

interface SchemaMetadata {
  schemaId: number;
  schemaName: string;
  tables: {
    tableName: string;
    columns: {
      columnName: string;
      dataType: string;
    }[];
  }[];
}

const SchemaManager: React.FC = () => {
  const [selectedCourseId, setSelectedCourseId] = useState<string>('');
  const [newSchemaName, setNewSchemaName] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [viewingMetadata, setViewingMetadata] = useState<SchemaMetadata | null>(null);
  const [deleteModal, setDeleteModal] = useState<{ isOpen: boolean; id: number | null }>({
    isOpen: false,
    id: null
  });

  const loadMetadata = async (schemaId: number) => {
    try {
      const res = await api.get(`/schemas/${schemaId}/metadata`);
      setViewingMetadata(res.data);
    } catch (e) {
      toast.error("Fehler beim Laden der Metadaten");
    }
  };

  const { data: coursesData } = useAsyncData<Course[]>(
    () => api.get('/admin/courses').then(res => res.data || []),
    []
  );
  const courses = coursesData ?? [];

  const { data: schemasData, retry: reloadSchemas } = useAsyncData<SchemaInfo[]>(
    () => selectedCourseId
      ? api.get(`/schemas/course/${selectedCourseId}`).then(res => res.data)
      : Promise.resolve([]),
    [selectedCourseId]
  );
  const schemas = schemasData ?? [];

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const uploadSchema = async () => {
    if (!selectedCourseId || !newSchemaName || !selectedFile) {
      toast.error('Bitte füllen Sie alle Felder aus');
      return;
    }

    setLoading(true);
    const formData = new FormData();
    formData.append('courseId', selectedCourseId);
    formData.append('schemaName', newSchemaName);
    formData.append('file', selectedFile);

    try {
      await api.post('/schemas/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      toast.success('Schema erfolgreich hochgeladen');
      reloadSchemas();
      setNewSchemaName('');
      setSelectedFile(null);
    } catch (err) {
      toast.error('Fehler beim Hochladen des Schemas');
    } finally {
      setLoading(false);
    }
  };

  const deleteSchema = async () => {
    if (!deleteModal.id) return;
    try {
      await api.delete(`/schemas/${deleteModal.id}`);
      toast.success("Schema gelöscht");
      reloadSchemas();
    } catch (err) {
      toast.error('Fehler beim Löschen des Schemas');
    }
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      <ConfirmationModal 
        isOpen={deleteModal.isOpen}
        onClose={() => setDeleteModal({ isOpen: false, id: null })}
        onConfirm={deleteSchema}
        title="Schema löschen"
        message="Sind Sie sicher, dass Sie dieses Schema löschen möchten? Alle zugehörigen Aufgaben könnten beeinträchtigt werden."
      />
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800 dark:text-white tracking-tight">Schema <span className="text-brand-600">Verwaltung</span></h2>
      </div>
      
      <div className="bg-gray-50 dark:bg-ink-card/50 p-6 rounded-2xl border border-slate-200 dark:border-ink-border flex flex-col md:flex-row gap-4 items-center transition-colors">
        <div className="relative w-full md:w-80">
          <GraduationCap className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <select
            className="x-select pl-11"
            value={selectedCourseId}
            onChange={(e) => setSelectedCourseId(e.target.value)}
          >
            <option value="">-- Kurs auswählen --</option>
            {courses.map(c => (
              <option key={c.id} value={c.instructorCourseId}>{c.courseName}</option>
            ))}
          </select>
        </div>
      </div>

      {selectedCourseId ? (
        <div className="grid lg:grid-cols-3 gap-8">
          <div className="lg:col-span-1">
            <div className="x-card p-8 sticky top-24">
              <h3 className="text-xl font-bold text-gray-800 dark:text-white mb-6 flex items-center">
                <CloudUpload className="text-brand-500 mr-2" size={24} /> Neues Schema
              </h3>
              <div className="space-y-6">
                <div className="space-y-2">
                  <label className="x-label">Anzeigename</label>
                  <input 
                    type="text" 
                    placeholder="z.B. Universität DB" 
                    className="x-input"
                    value={newSchemaName}
                    onChange={(e) => setNewSchemaName(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <label className="x-label">
                    DDL Datei (.sql)
                    <InfoTip 
                      title="Was ist eine DDL-Datei?" 
                      content={
                        <div className="space-y-2">
                          <p>Eine <strong>Data Definition Language</strong> Datei enthält SQL-Befehle zum Erstellen von Tabellen.</p>
                          <pre className="bg-gray-100 dark:bg-ink-soft p-2 rounded text-[10px] font-mono">
                            {`CREATE TABLE Students (\n  id INT PRIMARY KEY,\n  name VARCHAR(50)\n);`}
                          </pre>
                          <p>Das System nutzt diese Datei, um die Tabellenstruktur für die Aufgaben zu verstehen.</p>
                        </div>
                      }
                    />
                  </label>
                  <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-gray-200 dark:border-ink-border border-dashed rounded-2xl cursor-pointer bg-gray-50 dark:bg-ink-soft hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
                    <div className="flex flex-col items-center justify-center pt-5 pb-6">
                      {selectedFile ? <FileCheck className="text-green-500 mb-2" size={32} /> : <FileCode className="text-gray-400 mb-2" size={32} />}
                      <p className="text-xs font-bold text-gray-500 px-4 text-center">{selectedFile ? selectedFile.name : 'SQL Datei auswählen'}</p>
                    </div>
                    <input type="file" className="hidden" onChange={handleFileUpload} accept=".sql,.txt" />
                  </label>
                </div>
                <button 
                  onClick={uploadSchema}
                  disabled={loading}
                  className="btn-primary w-full"
                >
                  {loading ? <RefreshCw className="animate-spin" size={18} /> : <Upload size={18} />}
                  Schema Hochladen
                </button>
              </div>
            </div>
          </div>

          <div className="lg:col-span-2 space-y-6">
            <h3 className="text-xl font-bold text-gray-800 dark:text-white flex items-center">
              <Layers className="text-brand-500 mr-2" size={24} /> Vorhandene Schemata
            </h3>
            <div className="grid gap-4">
              {schemas.map(s => (
                <div key={s.id} className="x-card p-6 flex flex-col md:flex-row justify-between items-center group hover:border-brand-200 dark:hover:border-blue-800 transition-all gap-4">
                  <div className="flex items-center space-x-4 w-full md:w-auto">
                    <div className="w-12 h-12 bg-brand-50 dark:bg-brand-950/40 text-brand-500 dark:text-brand-400 rounded-2xl flex items-center justify-center shadow-inner shrink-0">
                      <Database size={20} />
                    </div>
                    <div className="min-w-0">
                      <h4 className="font-bold text-gray-800 dark:text-white text-lg leading-none mb-1 truncate">{s.schemaName}</h4>
                      <div className="flex items-center space-x-2">
                        <span className="text-[10px] font-bold uppercase text-gray-400 dark:text-gray-500 tracking-tighter bg-gray-100 dark:bg-ink-soft px-2 py-0.5 rounded shrink-0">SQL DDL</span>
                        <span className="text-xs text-gray-400 dark:text-gray-600 font-medium italic truncate">Vorschau: {s.content.substring(0, 30)}...</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-wrap items-center justify-end space-x-4 w-full md:w-auto">
                    <div className="flex items-center space-x-1">
                      <button 
                        className="icon-btn hover:text-brand-600"
                        title="Metadaten ansehen"
                        onClick={() => loadMetadata(s.id)}
                      >
                        <Table size={18} />
                      </button>
                      <button 
                        className="icon-btn hover:text-brand-600"
                        title="DDL ansehen"
                        onClick={() => toast(s.content)}
                      >
                        <Eye size={18} />
                      </button>
                      <button 
                        onClick={() => setDeleteModal({ isOpen: true, id: s.id })}
                        className="icon-btn hover:text-hard"
                        title="Löschen"
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </div>
                </div>
              ))}

              {viewingMetadata && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-fadeIn">
                  <div className="bg-white dark:bg-ink-card rounded-[40px] shadow-2xl max-w-4xl w-full max-h-[85vh] overflow-hidden flex flex-col transition-colors border border-slate-200 dark:border-ink-border">
                    <div className="p-8 bg-gray-50 dark:bg-ink-soft border-b border-slate-200 dark:border-ink-border flex justify-between items-center transition-colors">
                      <h3 className="text-2xl font-bold text-gray-800 dark:text-white uppercase tracking-tight">Metadaten: <span className="text-brand-600">{viewingMetadata.schemaName}</span></h3>
                      <button onClick={() => setViewingMetadata(null)} className="icon-btn hover:text-hard" title="Schließen">
                        <X size={22} />
                      </button>
                    </div>
                    <div className="p-10 overflow-y-auto space-y-8 custom-scrollbar">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        {viewingMetadata.tables.map(table => (
                          <div key={table.tableName} className="x-card p-8">
                            <h4 className="font-bold text-brand-600 dark:text-brand-400 mb-6 flex items-center text-lg uppercase tracking-wider">
                              <Table className="mr-3 opacity-50" size={20} /> {table.tableName}
                            </h4>
                            <div className="space-y-4">
                              {table.columns.map(col => (
                                <div key={col.columnName} className="flex justify-between items-center py-2 border-b border-gray-50 dark:border-ink-border last:border-0 transition-colors">
                                  <span className="text-sm font-bold text-gray-700 dark:text-gray-200 tracking-tight">{col.columnName}</span>
                                  <span className="text-[10px] font-bold text-gray-400 dark:text-gray-500 uppercase bg-gray-50 dark:bg-ink-card px-3 py-1 rounded-full tracking-widest">{col.dataType}</span>
                                </div>
                              ))}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              )}
              {schemas.length === 0 && (
                <div className="py-20 text-center bg-gray-50 dark:bg-ink-card/50 rounded-2xl border border-dashed border-gray-200 dark:border-ink-border transition-colors">
                  <Database className="text-gray-200 dark:text-gray-700 mx-auto mb-4" size={64} />
                  <p className="text-gray-400 dark:text-gray-600 font-bold uppercase tracking-widest text-xs">Keine Schemata gefunden.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      ) : (
        <div className="py-20 text-center text-gray-400 dark:text-gray-600 bg-white dark:bg-ink-card rounded-2xl border border-slate-200 dark:border-ink-border shadow-sm transition-colors">
          <MousePointer className="mx-auto mb-4 opacity-20" size={64} />
          <p className="font-bold uppercase tracking-widest text-sm">Bitte wähle zuerst einen Kurs aus.</p>
        </div>
      )}
    </div>
  );
};

export default SchemaManager;
