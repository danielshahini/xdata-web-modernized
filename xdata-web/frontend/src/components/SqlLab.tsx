import React, { useState, useEffect } from 'react';
import api from '../api';
import Editor, { loader } from '@monaco-editor/react';
import { toast } from 'react-hot-toast';
import { Play, CheckCircle, XCircle, Info, Database, BarChart3, Settings, ChevronDown, ChevronUp, Beaker } from 'lucide-react';
import MarkInfoDisplay from './MarkInfoDisplay';
import InfoTip from './common/InfoTip';
import { PartialMarkParameters, MarkInfo } from '../types';

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
  const [queryPattern, setQueryPattern] = useState('SELECT username FROM xdata_users WHERE internal_user_id > 1;');
  const [queryStudent, setQueryStudent] = useState('SELECT username FROM xdata_users WHERE internal_user_id >= 2;');
  const [params, setParams] = useState<PartialMarkParameters>(defaultParams);
  
  const [loadingEquivalence, setLoadingEquivalence] = useState(false);
  const [loadingGrading, setLoadingGrading] = useState(false);
  
  const [isEquivalent, setIsEquivalent] = useState<boolean | null>(null);
  const [markInfo, setMarkInfo] = useState<MarkInfo | null>(null);
  const [showParams, setShowParams] = useState(false);
  const [schemaMetadata, setSchemaMetadata] = useState<any>(null);

  useEffect(() => {
    if (selectedSchema) {
      api.get(`/schemas/${selectedSchema}/metadata`)
        .then(res => setSchemaMetadata(res.data))
        .catch(() => setSchemaMetadata(null));
    } else {
      setSchemaMetadata(null);
    }
  }, [selectedSchema]);

  useEffect(() => {
    let provider: any = null;
    if (schemaMetadata) {
      loader.init().then(monaco => {
        provider = monaco.languages.registerCompletionItemProvider('sql', {
          triggerCharacters: ['.', ' '],
          provideCompletionItems: (model, position) => {
            const word = model.getWordUntilPosition(position);
            const range = {
              startLineNumber: position.lineNumber,
              endLineNumber: position.lineNumber,
              startColumn: word.startColumn,
              endColumn: word.endColumn,
            };

            const suggestions: any[] = [];
            schemaMetadata.tables.forEach((table: any) => {
              suggestions.push({
                label: table.tableName,
                kind: monaco.languages.CompletionItemKind.Class,
                insertText: table.tableName,
                detail: 'Tabelle',
                range
              });
              table.columns.forEach((col: any) => {
                suggestions.push({
                  label: col.columnName,
                  kind: monaco.languages.CompletionItemKind.Field,
                  insertText: col.columnName,
                  detail: `${table.tableName} (${col.dataType})`,
                  range
                });
              });
            });
            return { suggestions };
          }
        });
      });
    }
    return () => {
      if (provider) provider.dispose();
    };
  }, [schemaMetadata]);

  useEffect(() => {
    api.get('/schemas')
      .then(res => {
        setSchemas(res.data);
        if (res.data.length > 0) setSelectedSchema(res.data[0].id);
      })
      .catch(() => toast.error('Fehler beim Laden der Schemata.'));
  }, []);

  const handleCheckEquivalence = async () => {
    setLoadingEquivalence(true);
    setIsEquivalent(null);
    try {
      const res = await api.post('/evaluation/playground/smt-check', { 
        query1: queryPattern, 
        query2: queryStudent,
        schemaId: selectedSchema
      });
      setIsEquivalent(res.data.equivalent);
      
      if (res.data.equivalent) {
        toast.success('Abfragen sind logisch äquivalent!');
      } else {
        toast.error('Abfragen sind NICET äquivalent (auf Basis der generierten Testdaten).');
      }
    } catch (err) {
      toast.error('Fehler bei der Prüfung.');
    } finally {
      setLoadingEquivalence(false);
    }
  };

  const handleSimulateGrading = async () => {
    setLoadingGrading(true);
    setMarkInfo(null);
    try {
      const res = await api.post('/evaluation/playground/partial-marking', { 
        patternQuery: queryPattern, 
        studentQuery: queryStudent,
        schemaId: selectedSchema,
        params
      });
      setMarkInfo(res.data);
      toast.success('Bewertung simuliert.');
    } catch (err) {
      toast.error('Fehler bei der Simulation.');
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
          <h2 className="text-2xl font-black dark:text-white">
            SQL <span className="text-blue-600">Diagnose Labor</span>
            <InfoTip 
              title="Was ist das SQL Diagnose Labor?"
              content={
                <div className="space-y-2">
                  <p>Dieses Labor bietet zwei Arten der Analyse:</p>
                  <ul className="list-disc ml-4 space-y-1">
                    <li><strong>Logik prüfen:</strong> Vergleicht zwei Abfragen mathematisch auf Basis von automatisch generierten Testdaten.</li>
                    <li><strong>Bewertung simulieren:</strong> Führt eine strukturelle Teilbewertung durch, um zu sehen, wie viele Punkte eine studentische Lösung erzielen würde.</li>
                  </ul>
                </div>
              }
            />
          </h2>
          <p className="text-gray-500 dark:text-gray-400 font-medium">Das universelle Tool für Logik-Prüfung und Bewertungs-Simulation.</p>
        </div>

        <div className="flex items-center gap-4 w-full md:w-auto">
          <div className="flex-1 md:w-64 relative">
             <Database className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
             <select 
               className="w-full pl-12 pr-4 py-3 rounded-2xl border border-gray-200 dark:border-gray-700 dark:bg-gray-800 font-bold focus:ring-4 focus:ring-blue-500/10 outline-none appearance-none transition-all dark:text-white"
               value={selectedSchema || ''}
               onChange={(e) => setSelectedSchema(parseInt(e.target.value))}
             >
               <option value="">Kein Schema / Standard-DB</option>
               {schemas.map(s => <option key={s.id} value={s.id}>{s.schemaName}</option>)}
             </select>
          </div>
          <button 
            onClick={() => {
              handleCheckEquivalence();
              handleSimulateGrading();
            }}
            disabled={loadingEquivalence || loadingGrading}
            className="px-8 py-3 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white rounded-2xl font-black shadow-lg shadow-blue-500/25 flex items-center transition-all"
          >
            <Beaker className={`mr-2 ${loadingEquivalence || loadingGrading ? 'animate-spin' : ''}`} size={20} />
            Alles prüfen
          </button>
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-xl overflow-hidden transition-all">
        <button 
          onClick={() => setShowParams(!showParams)}
          className="w-full flex items-center justify-between p-6 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
        >
          <div className="flex items-center">
            <Settings className="text-blue-600 mr-3" size={20} />
            <span className="font-black text-gray-700 dark:text-gray-200">Bewertungs-Gewichte anpassen</span>
          </div>
          {showParams ? <ChevronUp size={20} className="text-gray-400" /> : <ChevronDown size={20} className="text-gray-400" />}
        </button>
        
        {showParams && (
          <div className="p-8 border-t border-gray-100 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-900/20 grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-6">
            {(Object.keys(defaultParams) as Array<keyof PartialMarkParameters>).map(key => (
              <div key={key} className="space-y-2">
                <label className="text-xs font-black text-gray-400 uppercase tracking-wider">{key}</label>
                <input 
                  type="number"
                  value={params[key]}
                  onChange={e => updateParam(key, e.target.value)}
                  className="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 dark:bg-gray-800 font-bold focus:ring-2 focus:ring-blue-500 outline-none dark:text-white"
                />
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white dark:bg-gray-800 p-6 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-black dark:text-white flex items-center gap-2">
              <span className="w-8 h-8 rounded-lg bg-green-100 dark:bg-green-900/30 text-green-600 flex items-center justify-center text-sm italic">M</span>
              Musterlösung
            </h3>
            <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest bg-gray-50 dark:bg-gray-700 px-3 py-1 rounded-full">Dozenten View</span>
          </div>
          <div className="h-64 rounded-2xl overflow-hidden border border-gray-100 dark:border-gray-700 shadow-inner relative" 
               data-lpignore="true"
               data-form-type="other"
               data-ignore-autofill="true">
            <Editor
              height="100%"
              defaultLanguage="sql"
              theme={document.documentElement.classList.contains('dark') ? 'vs-dark' : 'light'}
              value={queryPattern}
              onChange={(val) => setQueryPattern(val || '')}
              loading={<div className="flex items-center justify-center h-full dark:bg-gray-900 dark:text-gray-400">Lade Editor...</div>}
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

        <div className="bg-white dark:bg-gray-800 p-6 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-black dark:text-white flex items-center gap-2">
              <span className="w-8 h-8 rounded-lg bg-blue-100 dark:bg-blue-900/30 text-blue-600 flex items-center justify-center text-sm italic">S</span>
              Studentische Abfrage
            </h3>
            <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest bg-gray-50 dark:bg-gray-700 px-3 py-1 rounded-full">Test Kandidat</span>
          </div>
          <div className="h-64 rounded-2xl overflow-hidden border border-gray-100 dark:border-gray-700 shadow-inner relative" 
               data-lpignore="true"
               data-form-type="other"
               data-ignore-autofill="true">
            <Editor
              height="100%"
              defaultLanguage="sql"
              theme={document.documentElement.classList.contains('dark') ? 'vs-dark' : 'light'}
              value={queryStudent}
              onChange={(val) => setQueryStudent(val || '')}
              loading={<div className="flex items-center justify-center h-full dark:bg-gray-900 dark:text-gray-400">Lade Editor...</div>}
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

      <div className="flex flex-col md:flex-row gap-4">
          <button 
            onClick={handleCheckEquivalence}
            disabled={loadingEquivalence}
            className="flex-1 py-4 bg-gray-900 dark:bg-blue-600 text-white rounded-2xl font-black flex items-center justify-center gap-2 hover:bg-gray-800 dark:hover:bg-blue-500 transition-all shadow-xl shadow-gray-200 dark:shadow-blue-900/20 disabled:opacity-50"
          >
            {loadingEquivalence ? <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white" /> : <Play size={20} />}
            Logik prüfen
          </button>
          <button 
            onClick={handleSimulateGrading}
            disabled={loadingGrading}
            className="flex-1 py-4 bg-white dark:bg-gray-700 border-2 border-gray-900 dark:border-gray-600 text-gray-900 dark:text-white rounded-2xl font-black flex items-center justify-center gap-2 hover:bg-gray-50 dark:hover:bg-gray-600 transition-all shadow-xl disabled:opacity-50"
          >
            {loadingGrading ? <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600" /> : <BarChart3 size={20} />}
            Bewertung simulieren
          </button>
      </div>

      {isEquivalent !== null && (
        <div className="animate-slideIn">
          <div className={`p-8 rounded-[40px] border flex items-center shadow-xl ${
            isEquivalent 
              ? 'bg-green-50 border-green-100 text-green-800 dark:bg-green-900/20 dark:border-green-800 dark:text-green-400' 
              : 'bg-red-50 border-red-100 text-red-800 dark:bg-red-900/20 dark:border-red-800 dark:text-red-400'
          }`}>
            <div className={`p-4 rounded-3xl mr-6 ${isEquivalent ? 'bg-green-500/10' : 'bg-red-500/10'}`}>
              {isEquivalent ? <CheckCircle size={40} /> : <XCircle size={40} />}
            </div>
            <div>
              <p className="text-[10px] font-black uppercase tracking-[0.2em] opacity-60 mb-1">Diagnose Ergebnis</p>
              <h4 className="text-3xl font-black">{isEquivalent ? 'Äquivalent' : 'Nicht Äquivalent'}</h4>
              <div className="mt-2 flex items-center text-sm font-bold opacity-80 italic">
                <Info size={14} className="mr-2" />
                {isEquivalent 
                  ? 'Beide Queries liefern auf allen relevanten Testdatensätzen identische Ergebnismengen.' 
                  : 'Auf den generierten Testdaten liefern die Abfragen unterschiedliche Resultate.'}
              </div>
            </div>
          </div>
        </div>
      )}

      {markInfo && (
        <div className="bg-white dark:bg-gray-800 p-8 rounded-[40px] border border-gray-100 dark:border-gray-700 shadow-xl space-y-6 transition-colors animate-slideIn">
          <div className="flex items-center justify-between">
            <h3 className="text-xl font-black flex items-center dark:text-white">
              <BarChart3 className="text-blue-600 mr-2" /> Strukturelle <span className="text-blue-600 ml-1">Analyse</span>
            </h3>
            <div className="text-center bg-blue-50 dark:bg-blue-900/30 px-6 py-2 rounded-2xl border border-blue-100 dark:border-blue-800">
               <span className="text-2xl font-black text-blue-600">{(markInfo.percentage || 0).toFixed(0)}%</span>
               <span className="text-[10px] font-black text-gray-400 uppercase ml-1">Punkte Score</span>
            </div>
          </div>
          <MarkInfoDisplay data={markInfo} />
        </div>
      )}
    </div>
  );
};

export default SqlLab;
