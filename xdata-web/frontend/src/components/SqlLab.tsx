import React, { useState, useEffect, useRef } from 'react';
import api from '../api';
import Editor, { loader } from '@monaco-editor/react';
import { toast } from 'react-hot-toast';
import { Database, BarChart3, Settings, ChevronDown, ChevronUp, Beaker } from 'lucide-react';
import MarkInfoDisplay from './MarkInfoDisplay';
import InfoTip from './common/InfoTip';
import { PartialMarkParameters, MarkInfo } from '../types';
import { createSqlCompletionProvider } from '../utils/sqlCompletion';
import { useAsyncData } from '../hooks/useAsyncData';

interface Schema {
  id: number;
  schemaName: string;
}

const defaultParams: PartialMarkParameters = {
  relation: 1,
  predicate: 1,
  projection: 1,
  joins: 1,
  groupBy: 1,
  havingClause: 1,
  aggregates: 1,
  distinct: 1,
  orderBy: 1,
  setOperators: 1,
  whereSubQueries: 1,
  fromSubQueries: 1,
  outerQuery: 1,
  subQConnective: 1,
  maxPartialMarks: 90
};

const SqlLab: React.FC = () => {
  const [schemas, setSchemas] = useState<Schema[]>([]);
  const [selectedSchema, setSelectedSchema] = useState<number | null>(null);
  const [queryPattern, setQueryPattern] = useState('SELECT user_name FROM xdata_users WHERE internal_user_id = \'admin-uuid-admin1\';');
  const [queryStudent, setQueryStudent] = useState('SELECT user_name FROM xdata_users;');
  const [params, setParams] = useState<PartialMarkParameters>(defaultParams);
  
  const [loadingGrading, setLoadingGrading] = useState(false);
  const [markInfo, setMarkInfo] = useState<MarkInfo | null>(null);
  const [showParams, setShowParams] = useState(false);
  const { data: schemaMetadata } = useAsyncData<any>(
    () => selectedSchema
      ? api.get(`/schemas/${selectedSchema}/metadata`).then(res => res.data).catch(() => null)
      : Promise.resolve(null),
    [selectedSchema]
  );
  const completionProviderRef = useRef<any>(null);

  useEffect(() => {
    let isCancelled = false;
    if (schemaMetadata) {
      loader.init().then(monaco => {
        if (isCancelled) return;
        if (completionProviderRef.current) {
          completionProviderRef.current.dispose();
        }
        completionProviderRef.current = monaco.languages.registerCompletionItemProvider(
          'sql', createSqlCompletionProvider(monaco, schemaMetadata));
      });
    }
    return () => {
      isCancelled = true;
      if (completionProviderRef.current) {
        completionProviderRef.current.dispose();
        completionProviderRef.current = null;
      }
    };
  }, [schemaMetadata]);

  const handleEditorMount = (editor: any) => {
    const textarea = editor.getDomNode()?.querySelector('textarea');
    if (textarea) {
      textarea.setAttribute('autocomplete', 'off');
      textarea.setAttribute('autocorrect', 'off');
      textarea.setAttribute('autocapitalize', 'off');
      textarea.setAttribute('spellcheck', 'false');
      textarea.setAttribute('data-lpignore', 'true');
      textarea.setAttribute('data-form-type', 'other');
    }
  };

  useEffect(() => {
    api.get('/schemas')
      .then(res => {
        setSchemas(res.data);
        if (res.data.length > 0) setSelectedSchema(res.data[0].id);
      })
      .catch(() => toast.error('Failed to load schemas.'));
  }, []);

  const handleFullAnalysis = async () => {
    setLoadingGrading(true);
    setMarkInfo(null);

    try {
      const markRes = await api.post('/evaluation/playground/partial-marking', {
        patternQuery: queryPattern,
        studentQuery: queryStudent,
        schemaId: selectedSchema,
        params
      });
      setMarkInfo(markRes.data);
      toast.success('Structural evaluation complete.');
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data || 'Analysis failed.';
      toast.error(typeof msg === 'string' ? msg : 'Analysis failed.');
    } finally {
      setLoadingGrading(false);
    }
  };

  const updateParam = (key: keyof PartialMarkParameters, val: string) => {
    setParams(prev => ({ ...prev, [key]: parseInt(val) || 0 }));
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold dark:text-white">
            SQL <span className="text-brand-600">Diagnostic Lab</span>
            <InfoTip
              title="What is the SQL Diagnostic Lab?"
              content={
                <div className="space-y-2">
                  <p><strong>Simulate grading:</strong> Performs a structural partial evaluation of the student solution against the model solution (projections, predicates, joins, group-by, …) and shows how many points it would score.</p>
                </div>
              }
            />
          </h2>
          <p className="text-gray-500 dark:text-gray-400 font-medium">Structural grading simulation for SQL solutions.</p>
        </div>

        <div className="flex items-center gap-4 w-full md:w-auto">
          <div className="flex-1 md:w-64 relative">
             <Database className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
             <select 
               className="x-select pl-12"
               value={selectedSchema || ''}
               onChange={(e) => setSelectedSchema(parseInt(e.target.value))}
             >
               <option value="">No schema / default DB</option>
               {schemas.map(s => <option key={s.id} value={s.id}>{s.schemaName}</option>)}
             </select>
          </div>
        </div>
      </div>

      <div className="bg-white dark:bg-ink-card rounded-2xl border border-slate-200 dark:border-ink-border shadow-xl overflow-hidden transition-all">
        <button 
          onClick={() => setShowParams(!showParams)}
          className="w-full flex items-center justify-between p-6 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
        >
          <div className="flex items-center">
            <Settings className="text-brand-600 mr-3" size={20} />
            <span className="font-bold text-gray-700 dark:text-gray-200">Adjust grading weights</span>
          </div>
          {showParams ? <ChevronUp size={20} className="text-gray-400" /> : <ChevronDown size={20} className="text-gray-400" />}
        </button>
        
        {showParams && (
          <div className="p-8 border-t border-slate-200 dark:border-ink-border bg-gray-50/50 dark:bg-ink-soft/20 grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-6">
            {(Object.keys(defaultParams) as Array<keyof PartialMarkParameters>).map(key => (
              <div key={key} className="space-y-2">
                <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">{key}</label>
                <input 
                  type="number"
                  value={params[key]}
                  onChange={e => updateParam(key, e.target.value)}
                  className="x-input py-2"
                />
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white dark:bg-ink-card p-6 rounded-2xl border border-slate-200 dark:border-ink-border shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold dark:text-white flex items-center gap-2">
              <span className="w-8 h-8 rounded-lg bg-green-100 dark:bg-green-900/30 text-green-600 flex items-center justify-center text-sm italic">M</span>
              Model solution
            </h3>
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest bg-gray-50 dark:bg-gray-700 px-3 py-1 rounded-full">Instructor View</span>
          </div>
          <div className="h-64 rounded-2xl overflow-hidden border border-slate-200 dark:border-ink-border shadow-inner relative" 
               data-lpignore="true"
               data-form-type="other"
               data-ignore-autofill="true">
            <Editor
              height="100%"
              defaultLanguage="sql"
              theme={document.documentElement.classList.contains('dark') ? 'vs-dark' : 'light'}
              value={queryPattern}
              onChange={(val) => setQueryPattern(val || '')}
              onMount={handleEditorMount}
              loading={<div className="flex items-center justify-center h-full dark:bg-ink-soft dark:text-gray-400">Loading editor...</div>}
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                fontWeight: '700',
                padding: { top: 16 },
                automaticLayout: true,
                wordWrap: 'on',
                quickSuggestions: { other: true, comments: false, strings: false },
                parameterHints: { enabled: true },
                fixedOverflowWidgets: true,
                suggestOnTriggerCharacters: true
              }}
            />
          </div>
        </div>

        <div className="bg-white dark:bg-ink-card p-6 rounded-2xl border border-slate-200 dark:border-ink-border shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold dark:text-white flex items-center gap-2">
              <span className="w-8 h-8 rounded-lg bg-brand-100 dark:bg-brand-950/30 text-brand-600 flex items-center justify-center text-sm italic">S</span>
              Student query
            </h3>
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest bg-gray-50 dark:bg-gray-700 px-3 py-1 rounded-full">Test Candidate</span>
          </div>
          <div className="h-64 rounded-2xl overflow-hidden border border-slate-200 dark:border-ink-border shadow-inner relative" 
               data-lpignore="true"
               data-form-type="other"
               data-ignore-autofill="true">
            <Editor
              height="100%"
              defaultLanguage="sql"
              theme={document.documentElement.classList.contains('dark') ? 'vs-dark' : 'light'}
              value={queryStudent}
              onChange={(val) => setQueryStudent(val || '')}
              onMount={handleEditorMount}
              loading={<div className="flex items-center justify-center h-full dark:bg-ink-soft dark:text-gray-400">Loading editor...</div>}
              options={{ 
                minimap: { enabled: false }, 
                fontSize: 14, 
                fontWeight: '700', 
                padding: { top: 16 },
                automaticLayout: true,
                wordWrap: 'on',
                quickSuggestions: { other: true, comments: false, strings: false },
                parameterHints: { enabled: true },
                fixedOverflowWidgets: true,
                suggestOnTriggerCharacters: true
              }}
            />
          </div>
        </div>
      </div>

      <div className="flex flex-col gap-4">
          <button 
            onClick={handleFullAnalysis}
            disabled={loadingGrading}
            className="btn-primary w-full py-4 text-base"
          >
            {loadingGrading ? (
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white" />
            ) : (
              <Beaker className="group-hover:rotate-12 transition-transform" size={24} />
            )}
            <span className="text-xl">Run structural evaluation</span>
          </button>
      </div>

      {markInfo && (
        <div className="space-y-6 animate-slideIn">
          {markInfo && (
            <div className="bg-white dark:bg-ink-card p-8 rounded-[40px] border border-slate-200 dark:border-ink-border shadow-xl space-y-6 transition-colors">
              <div className="flex items-center justify-between border-b border-gray-50 dark:border-ink-border/50 pb-6">
                <h3 className="text-xl font-bold flex items-center dark:text-white uppercase tracking-wider">
                  <BarChart3 className="text-brand-600 mr-3" size={24} /> 
                  Structural <span className="text-brand-600 ml-2">Analysis & Feedback</span>
                </h3>
              </div>
              <MarkInfoDisplay data={markInfo} />
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SqlLab;
