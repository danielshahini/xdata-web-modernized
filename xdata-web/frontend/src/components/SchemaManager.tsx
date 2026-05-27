import React, { useState, useEffect } from 'react';
import api from '../api';
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
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourseId, setSelectedCourseId] = useState<string>('');
  const [schemas, setSchemas] = useState<SchemaInfo[]>([]);
  const [newSchemaName, setNewSchemaName] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [sampleFile, setSampleFile] = useState<File | null>(null);
  const [uploadingSample, setUploadingSample] = useState(false);
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

  useEffect(() => {
    // Ideally, get only courses where user is instructor
    api.get('/admin/courses')
      .then(res => setCourses(res.data || []))
      .catch(err => {
        console.error('Fehler beim Laden der Kurse:', err);
      });
  }, []);

  const loadSchemas = (courseId: string) => {
    setSelectedCourseId(courseId);
    if (courseId) {
      api.get(`/schemas/course/${courseId}`).then(res => setSchemas(res.data));
    } else {
      setSchemas([]);
    }
  };

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
      loadSchemas(selectedCourseId);
      setNewSchemaName('');
      setSelectedFile(null);
    } catch (err) {
      toast.error('Fehler beim Hochladen des Schemas');
    } finally {
      setLoading(false);
    }
  };

  const uploadSampleData = async (schemaName: string) => {
    if (!sampleFile) return;
    setUploadingSample(true);
    const formData = new FormData();
    formData.append('schemaName', schemaName);
    formData.append('file', sampleFile);
    try {
      await api.post('/schemas/sample-data/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      toast.success('Beispieldaten hochgeladen für ' + schemaName);
      setSampleFile(null);
    } catch (e) {
      toast.error('Fehler beim Hochladen der Beispieldaten');
    } finally {
      setUploadingSample(false);
    }
  };

  const deleteSchema = async () => {
    if (!deleteModal.id) return;
    try {
      await api.delete(`/schemas/${deleteModal.id}`);
      toast.success("Schema gelöscht");
      loadSchemas(selectedCourseId);
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
        <h2 className="text-2xl font-black text-gray-800 dark:text-white tracking-tight">Schema <span className="text-blue-600">Verwaltung</span></h2>
      </div>
      
      <div className="bg-gray-50 dark:bg-gray-800/50 p-6 rounded-3xl border border-gray-100 dark:border-gray-700 flex flex-col md:flex-row gap-4 items-center transition-colors">
        <div className="relative w-full md:w-80">
          <GraduationCap className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <select 
            className="w-full pl-11 pr-4 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none appearance-none bg-white dark:bg-gray-800 text-sm font-bold text-gray-700 dark:text-gray-200 shadow-sm"
            value={selectedCourseId}
            onChange={(e) => loadSchemas(e.target.value)}
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
            <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-lg sticky top-24 transition-colors">
              <h3 className="text-xl font-black text-gray-800 dark:text-white mb-6 flex items-center">
                <CloudUpload className="text-blue-500 mr-2" size={24} /> Neues Schema
              </h3>
              <div className="space-y-6">
                <div className="space-y-2">
                  <label className="text-xs font-black text-gray-400 uppercase tracking-widest ml-1">Anzeigename</label>
                  <input 
                    type="text" 
                    placeholder="z.B. Universität DB" 
                    className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 dark:border-gray-700 focus:ring-2 focus:ring-blue-500 outline-none font-bold text-gray-700 dark:text-white bg-gray-50 dark:bg-gray-900 focus:bg-white dark:focus:bg-gray-800 transition-all"
                    value={newSchemaName}
                    onChange={(e) => setNewSchemaName(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-xs font-black text-gray-400 uppercase tracking-widest ml-1">
                    DDL Datei (.sql)
                    <InfoTip 
                      title="Was ist eine DDL-Datei?" 
                      content={
                        <div className="space-y-2">
                          <p>Eine <strong>Data Definition Language</strong> Datei enthält SQL-Befehle zum Erstellen von Tabellen.</p>
                          <pre className="bg-gray-100 dark:bg-gray-900 p-2 rounded text-[10px] font-mono">
                            {`CREATE TABLE Students (\n  id INT PRIMARY KEY,\n  name VARCHAR(50)\n);`}
                          </pre>
                          <p>Das System nutzt diese Datei, um die Tabellenstruktur für die Aufgaben zu verstehen.</p>
                        </div>
                      }
                    />
                  </label>
                  <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-gray-200 dark:border-gray-700 border-dashed rounded-2xl cursor-pointer bg-gray-50 dark:bg-gray-900 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
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
                  className="w-full bg-blue-600 text-white py-4 rounded-2xl font-black shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-all active:scale-95 disabled:opacity-50 flex items-center justify-center"
                >
                  {loading ? <RefreshCw className="animate-spin mr-2" size={18} /> : <Upload className="mr-2" size={18} />}
                  Schema Hochladen
                </button>
              </div>
            </div>
          </div>

          <div className="lg:col-span-2 space-y-6">
            <h3 className="text-xl font-black text-gray-800 dark:text-white flex items-center">
              <Layers className="text-blue-500 mr-2" size={24} /> Vorhandene Schemata
            </h3>
            <div className="grid gap-4">
              {schemas.map(s => (
                <div key={s.id} className="bg-white dark:bg-gray-800 p-6 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-sm flex flex-col md:flex-row justify-between items-center group hover:border-blue-200 dark:hover:border-blue-800 transition-all duration-300 gap-4">
                  <div className="flex items-center space-x-4 w-full md:w-auto">
                    <div className="w-12 h-12 bg-blue-50 dark:bg-blue-900/40 text-blue-500 dark:text-blue-400 rounded-2xl flex items-center justify-center shadow-inner shrink-0">
                      <Database size={20} />
                    </div>
                    <div className="min-w-0">
                      <h4 className="font-black text-gray-800 dark:text-white text-lg leading-none mb-1 truncate">{s.schemaName}</h4>
                      <div className="flex items-center space-x-2">
                        <span className="text-[10px] font-black uppercase text-gray-400 dark:text-gray-500 tracking-tighter bg-gray-100 dark:bg-gray-900 px-2 py-0.5 rounded shrink-0">SQL DDL</span>
                        <span className="text-xs text-gray-400 dark:text-gray-600 font-medium italic truncate">Vorschau: {s.content.substring(0, 30)}...</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-wrap items-center justify-end space-x-4 w-full md:w-auto">
                    <div className="flex items-center space-x-2 bg-gray-50 dark:bg-gray-900/50 border border-gray-100 dark:border-gray-700 p-1.5 rounded-xl">
                      <input 
                        type="file" 
                        className="text-[10px] text-gray-500 file:mr-2 file:py-1 file:px-2 file:rounded-md file:border-0 file:text-[10px] file:font-black file:bg-blue-50 dark:file:bg-blue-900/40 file:text-blue-600 dark:file:text-blue-400"
                        onChange={e => setSampleFile(e.target.files?.[0] || null)}
                      />
                      <button 
                        onClick={() => uploadSampleData(s.schemaName)}
                        disabled={!sampleFile || uploadingSample}
                        className="px-2 py-1 bg-green-600 text-white rounded-md font-black text-[10px] uppercase disabled:opacity-50 hover:bg-green-700 transition-colors"
                      >
                        Sample Data
                      </button>
                      <InfoTip 
                        title="Beispieldaten hochladen"
                        content={
                          <div className="space-y-2">
                            <p>Laden Sie eine SQL-Datei mit <code>INSERT</code> Statements hoch, um Beispieldaten für dieses Schema bereitzustellen.</p>
                            <p>Diese Daten werden verwendet, um die Abfragen der Studenten gegen eine reale Datenbank zu testen.</p>
                          </div>
                        }
                      />
                    </div>
                    <div className="flex items-center space-x-1">
                      <button 
                        className="p-3 text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-xl transition-all"
                        title="Metadaten ansehen"
                        onClick={() => loadMetadata(s.id)}
                      >
                        <Table size={18} />
                      </button>
                      <button 
                        className="p-3 text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-xl transition-all"
                        title="DDL ansehen"
                        onClick={() => toast(s.content)}
                      >
                        <Eye size={18} />
                      </button>
                      <button 
                        onClick={() => setDeleteModal({ isOpen: true, id: s.id })}
                        className="p-3 text-gray-400 hover:text-red-500 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all"
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
                  <div className="bg-white dark:bg-gray-800 rounded-[40px] shadow-2xl max-w-4xl w-full max-h-[85vh] overflow-hidden flex flex-col transition-colors border border-gray-100 dark:border-gray-700">
                    <div className="p-8 bg-gray-50 dark:bg-gray-900 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center transition-colors">
                      <h3 className="text-2xl font-black text-gray-800 dark:text-white uppercase tracking-tight">Metadaten: <span className="text-blue-600">{viewingMetadata.schemaName}</span></h3>
                      <button onClick={() => setViewingMetadata(null)} className="w-12 h-12 rounded-2xl bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-gray-500 hover:text-red-500 transition-all flex items-center justify-center">
                        <X size={24} />
                      </button>
                    </div>
                    <div className="p-10 overflow-y-auto space-y-8 custom-scrollbar">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        {viewingMetadata.tables.map(table => (
                          <div key={table.tableName} className="bg-white dark:bg-gray-700/30 p-8 rounded-[32px] border border-gray-100 dark:border-gray-700 shadow-sm">
                            <h4 className="font-black text-blue-600 dark:text-blue-400 mb-6 flex items-center text-lg uppercase tracking-wider">
                              <Table className="mr-3 opacity-50" size={20} /> {table.tableName}
                            </h4>
                            <div className="space-y-4">
                              {table.columns.map(col => (
                                <div key={col.columnName} className="flex justify-between items-center py-2 border-b border-gray-50 dark:border-gray-700 last:border-0 transition-colors">
                                  <span className="text-sm font-bold text-gray-700 dark:text-gray-200 tracking-tight">{col.columnName}</span>
                                  <span className="text-[10px] font-black text-gray-400 dark:text-gray-500 uppercase bg-gray-50 dark:bg-gray-800 px-3 py-1 rounded-full tracking-widest">{col.dataType}</span>
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
                <div className="py-20 text-center bg-gray-50 dark:bg-gray-800/50 rounded-3xl border border-dashed border-gray-200 dark:border-gray-700 transition-colors">
                  <Database className="text-gray-200 dark:text-gray-700 mx-auto mb-4" size={64} />
                  <p className="text-gray-400 dark:text-gray-600 font-bold uppercase tracking-widest text-xs">Keine Schemata gefunden.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      ) : (
        <div className="py-20 text-center text-gray-400 dark:text-gray-600 bg-white dark:bg-gray-800 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-sm transition-colors">
          <MousePointer className="mx-auto mb-4 opacity-20" size={64} />
          <p className="font-black uppercase tracking-widest text-sm">Bitte wähle zuerst einen Kurs aus.</p>
        </div>
      )}
    </div>
  );
};

export default SchemaManager;
