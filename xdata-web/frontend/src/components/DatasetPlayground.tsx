import React, { useState, useEffect, useRef } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import api from '../api';
import Editor from '@monaco-editor/react';
import { toast } from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import { Database, Play, Download, Copy, RefreshCw, AlertCircle, Upload } from 'lucide-react';

interface Schema {
  id: number;
  schemaName: string;
}

const DatasetPlayground: React.FC = () => {
  const { isDark } = useAuth();
  const [selectedSchema, setSelectedSchema] = useState<number | null>(null);
  const [query, setQuery] = useState('');
  const [mutationTypes, setMutationTypes] = useState<string[]>(['SELECTION', 'EQUIVALENCE', 'AGG']);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [results, setResults] = useState<string[]>([]);
  const [message, setMessage] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { data: schemasData, retry: reloadSchemas } = useAsyncData<Schema[]>(
    () => api.get('/schemas').then(res => res.data).catch(err => {
      console.error('Failed to fetch schemas', err);
      toast.error('Could not load schemas');
      return [];
    }),
    []
  );
  const schemas = schemasData ?? [];

  useEffect(() => {
    if (schemasData && schemasData.length > 0 && selectedSchema === null) {
      setSelectedSchema(schemasData[0].id);
    }
  }, [schemasData, selectedSchema]);

  const handleEditorMount = (editor: any) => {
    const textarea = editor.getDomNode()?.querySelector('textarea');
    if (textarea) {
      textarea.setAttribute('autocomplete', 'off');
      textarea.setAttribute('autocorrect', 'off');
      textarea.setAttribute('autocapitalize', 'off');
      textarea.setAttribute('spellcheck', 'false');
      textarea.setAttribute('data-lpignore', 'true');
      textarea.setAttribute('data-form-type', 'other');
      textarea.setAttribute('name', 'no-autofill-' + Math.random());
      textarea.id = 'monaco-textarea-' + Math.random();
    }
  };

  const handleGenerate = async () => {
    if (!selectedSchema) {
      toast.error('Please select a schema first');
      return;
    }

    setLoading(true);
    setResults([]);
    setMessage('');

    try {
      const response = await api.post('/playground/generate-killing-data', {
        query,
        schemaId: selectedSchema,
        mutationTypes
      });

      const inserts = (response.data.inserts || []).filter((s: string) => !s.trim().startsWith('--'));
      if (response.data.success && inserts.length > 0) {
        setResults(inserts);
        setMessage(response.data.message);
        toast.success(`${inserts.length} Testdaten-Zeile(n) generiert`);
      } else {
        // success flag but no rows
        setResults([]);
        setMessage(response.data.message || 'Für diese Abfrage konnten keine Testdaten erzeugt werden.');
        toast('Keine Testdaten erzeugt', { icon: 'ℹ️' });
      }
    } catch (err: any) {
      console.error('Generation error', err);
      setResults([]);
      // The backend returns 422 with a clear, user-facing message for queries it
      // cannot handle; surface that instead of a generic error.
      const msg = err.response?.data?.message
        || 'Die Datengenerierung ist fehlgeschlagen. Bitte vereinfache die Abfrage oder versuche es erneut.';
      setMessage(msg);
      toast.error('Keine Testdaten erzeugt');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/playground/upload-schema', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      if (response.data.success) {
        toast.success('File uploaded successfully');
        reloadSchemas();
        if (response.data.schemaId) {
           setSelectedSchema(response.data.schemaId);
        }
        if (response.data.extractedSchema) {
           setMessage('Schema imported and saved. You can now generate data.');
        }
      } else {
        toast.error(response.data.message || 'Upload failed');
      }
    } catch (err) {
      console.error('Upload error', err);
      toast.error('Error uploading file');
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const copyToClipboard = () => {
    const text = results.join('\n');
    navigator.clipboard.writeText(text);
    toast.success('Copied to clipboard');
  };

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-8 flex flex-col md:flex-row justify-between items-start gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white flex items-center gap-3 tracking-tight">
            <Database className="w-10 h-10 text-brand-600" />
            Dataset <span className="text-brand-600">Playground</span>
          </h1>
          <p className="text-slate-500 dark:text-gray-400 mt-2 font-medium">
            Generiere gezielt Datensätze, um Mutanten deiner SQL-Abfrage zu erkennen.
          </p>
        </div>
        
        <div className="flex gap-3">
          <input 
            type="file" 
            ref={fileInputRef} 
            onChange={handleFileUpload} 
            className="hidden" 
            accept=".sql,.txt"
          />
          <button 
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            className="flex items-center gap-2 bg-white dark:bg-ink-card border border-slate-200 dark:border-ink-border hover:bg-slate-50 dark:hover:bg-gray-700 text-slate-700 dark:text-gray-200 px-6 py-3 rounded-2xl font-bold transition-all shadow-sm dark:shadow-none"
          >
            {uploading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Upload className="w-4 h-4" />}
            Schema laden (.sql)
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="space-y-6">
          <div className="bg-white dark:bg-ink-card rounded-2xl shadow-xl dark:shadow-none border border-slate-100 dark:border-ink-border p-8 transition-colors">
            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em] mb-3 ml-1">
              Datenbankschema auswählen
            </label>
            <div className="flex gap-4">
              <select
                className="flex-1 rounded-2xl border-slate-200 dark:border-ink-border dark:bg-ink-soft dark:text-white shadow-sm focus:ring-4 focus:ring-brand-500/10 focus:border-blue-500 font-bold px-4 py-3 appearance-none outline-none transition-all"
                value={selectedSchema || ''}
                onChange={(e) => setSelectedSchema(Number(e.target.value))}
              >
                <option value="" disabled>Schema wählen...</option>
                {schemas.map(s => (
                  <option key={s.id} value={s.id}>{s.schemaName}</option>
                ))}
              </select>
              <button 
                className="p-3 text-brand-600 bg-brand-50 dark:bg-brand-950/30 hover:bg-brand-100 dark:hover:bg-blue-900/50 rounded-2xl transition-all" 
                title="Refresh list"
                onClick={() => reloadSchemas()}
              >
                <RefreshCw className="w-5 h-5" />
              </button>
            </div>
          </div>

          <div className="bg-white dark:bg-ink-card rounded-2xl shadow-xl dark:shadow-none border border-slate-100 dark:border-ink-border overflow-hidden transition-colors">
            <div className="p-5 border-b border-slate-50 dark:border-ink-border bg-slate-50/50 dark:bg-ink-soft/50 flex justify-between items-center">
              <span className="text-xs font-bold text-slate-500 dark:text-gray-400 uppercase tracking-widest">Referenz-Abfrage (SQL)</span>
            </div>
            <div className="h-48 border-b dark:border-ink-border relative"
                 data-lpignore="true"
                 data-form-type="other"
                 data-ignore-autofill="true">
              <Editor
                height="100%"
                defaultLanguage="sql"
                theme={isDark ? 'vs-dark' : 'light'}
                value={query}
                onChange={(val) => setQuery(val || '')}
                onMount={handleEditorMount}
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  fontWeight: '700',
                  padding: { top: 12 }
                }}
              />
              {!query && (
                <div className="pointer-events-none absolute top-3 left-[3.4rem] font-mono text-[14px] text-slate-400 dark:text-gray-600 select-none">
                  z.&nbsp;B. SELECT name FROM students WHERE age &gt; 20;
                </div>
              )}
            </div>

            <div className="bg-white dark:bg-ink-card p-6 border-t border-slate-50 dark:border-ink-border">
                <h3 className="text-[10px] font-bold text-slate-400 dark:text-gray-500 uppercase tracking-widest mb-4 ml-1">Mutationstypen</h3>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {[
                    { id: 'SELECTION', label: 'Selection' },
                    { id: 'EQUIVALENCE', label: 'Joins (Equi)' },
                    { id: 'NONEQUIJOIN', label: 'Joins (Non-Equi)' },
                    { id: 'AGG', label: 'Aggregation' },
                    { id: 'DISTINCT', label: 'Distinct' },
                    { id: 'EXTRAGROUPBY', label: 'Group By' },
                    { id: 'HAVING', label: 'Having' }
                  ].map(type => (
                    <label key={type.id} className="flex items-center gap-2 text-xs font-bold text-slate-600 dark:text-gray-300 cursor-pointer hover:text-brand-600 dark:hover:text-brand-400 transition-colors bg-gray-50 dark:bg-ink-soft/50 p-3 rounded-xl border border-transparent hover:border-blue-100 dark:hover:border-blue-900/30">
                      <input
                        type="checkbox"
                        checked={mutationTypes.includes(type.id)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setMutationTypes([...mutationTypes, type.id]);
                          } else {
                            setMutationTypes(mutationTypes.filter(t => t !== type.id));
                          }
                        }}
                        className="w-4 h-4 rounded border-slate-300 dark:border-gray-600 text-brand-600 focus:ring-brand-500 dark:bg-ink-soft"
                      />
                      {type.label}
                    </label>
                  ))}
                </div>
            </div>
            <div className="p-6 bg-slate-50/50 dark:bg-ink-soft/50 border-t border-slate-50 dark:border-ink-border flex justify-end">
              <button
                onClick={handleGenerate}
                disabled={loading || !selectedSchema}
                className="flex items-center gap-3 bg-brand-600 hover:bg-brand-700 disabled:bg-slate-300 text-white px-8 py-4 rounded-[20px] font-bold transition-all shadow-xl shadow-blue-500/20 active:scale-95 disabled:opacity-50"
              >
                {loading ? <RefreshCw className="w-5 h-5 animate-spin" /> : <Play className="w-5 h-5" />}
                Dataset generieren
              </button>
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white dark:bg-ink-card rounded-2xl shadow-xl dark:shadow-none border border-slate-100 dark:border-ink-border flex flex-col h-full min-h-[500px] overflow-hidden transition-colors">
            <div className="p-5 border-b border-slate-50 dark:border-ink-border bg-slate-50/50 dark:bg-ink-soft/50 flex justify-between items-center">
              <span className="text-xs font-bold text-slate-500 dark:text-gray-400 uppercase tracking-widest">Generierter Datensatz (SQL Inserts)</span>
              {results.length > 0 && (
                <div className="flex gap-2">
                  <button
                    onClick={copyToClipboard}
                    className="p-3 text-slate-600 dark:text-gray-400 hover:bg-white dark:hover:bg-gray-800 hover:shadow-sm rounded-xl transition-all"
                    title="Kopieren"
                  >
                    <Copy className="w-5 h-5" />
                  </button>
                  <button
                    className="p-3 text-slate-600 dark:text-gray-400 hover:bg-white dark:hover:bg-gray-800 hover:shadow-sm rounded-xl transition-all"
                    title="Download .sql"
                    onClick={() => {
                      const element = document.createElement("a");
                      const file = new Blob([results.join('\n')], {type: 'text/plain'});
                      element.href = URL.createObjectURL(file);
                      element.download = "dataset.sql";
                      document.body.appendChild(element);
                      element.click();
                    }}
                  >
                    <Download className="w-5 h-5" />
                  </button>
                </div>
              )}
            </div>
            
            <div className="flex-1 p-6 font-mono text-sm overflow-auto bg-slate-900 dark:bg-black text-green-400 selection:bg-green-500/20 custom-scrollbar">
              {loading ? (
                <div className="flex flex-col items-center justify-center h-full text-slate-400 gap-4">
                  <RefreshCw className="w-12 h-12 animate-spin" />
                  <p>SMT Solver rechnet...</p>
                </div>
              ) : results.length > 0 ? (
                <pre className="whitespace-pre-wrap">
                  {results.join('\n')}
                </pre>
              ) : (
                <div className="flex flex-col items-center justify-center h-full text-slate-500 text-center gap-2">
                  <Database className="w-12 h-12 opacity-20" />
                  <p>Noch kein Datensatz generiert.<br/>Klicke auf "Dataset generieren".</p>
                </div>
              )}
            </div>

            {message && (
              <div className={`p-4 border-t ${results.length > 0 ? 'bg-easy/5 border-easy/20 text-easy' : 'bg-amber-50 dark:bg-amber-900/20 border-amber-100 dark:border-amber-900/40 text-amber-700 dark:text-amber-400'} flex items-start gap-2`}>
                <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
                <span className="text-sm font-medium">{message}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DatasetPlayground;
