import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { 
  Plus,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  CalendarClock,
  Save, 
  Copy,
  Trash2,
  Edit,
  Check, 
  ClipboardList,
  Settings,
  Database,
  BarChart3,
  Calendar,
  ShieldCheck,
  Zap,
  Layout,
  FileText,
  CheckCircle2,
  AlertCircle,
  Upload
} from 'lucide-react';
import api from '../api';
import { toast } from 'react-hot-toast';
import AssignmentStats from './AssignmentStats';
import ConfirmationModal from './common/ConfirmationModal';
import InfoTip from './common/InfoTip';
import DeadlineExtensionModal from './DeadlineExtensionModal';
import { Course, Assignment, Question, PartialMarkParameters } from '../types';

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

const AssignmentManager: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourseId, setSelectedCourseId] = useState<string>('');
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [editingAssignment, setEditingAssignment] = useState<any | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [dbConnections, setDbConnections] = useState<any[]>([]);
  const [schemas, setSchemas] = useState<any[]>([]);
  const [showQuestionParams, setShowQuestionParams] = useState<number | null>(null);
  const [showStats, setShowStats] = useState<number | null>(null);
  const [extensionsFor, setExtensionsFor] = useState<Assignment | null>(null);
  const [wizardStep, setWizardStep] = useState<number>(1);
  const [showAdvAssignment, setShowAdvAssignment] = useState<boolean>(false);
  const [assignmentStats, setAssignmentStats] = useState<Record<number, any[]>>({});

  const [deleteModal, setDeleteModal] = useState<{ 
    isOpen: boolean; 
    type: 'assignment' | 'question' | null;
    id: number | null;
    idx?: number;
  }>({ isOpen: false, type: null, id: null });

  const totalPoints = useMemo(() => {
    return questions.reduce((sum, q) => sum + (q.marks || 0), 0);
  }, [questions]);

  const loadAssignments = useCallback((courseId: string) => {
    setSelectedCourseId(courseId);
    if (courseId) {
      api.get(`/assignments?courseId=${courseId}`).then(res => {
        setAssignments(res.data);
        // Load stats for each assignment
        res.data.forEach((a: any) => {
          api.get(`/assignments/${a.id}/stats`).then(statsRes => {
            setAssignmentStats(prev => ({...prev, [a.id]: statsRes.data}));
          });
        });
      });
      api.get(`/instructor/connections`).then(res => {
        const filtered = (res.data || []).filter((c: any) => c.courseId === courseId);
        setDbConnections(filtered);
      });
      api.get(`/schemas/course/${courseId}`)
        .then(res => setSchemas(res.data || []))
        .catch(() => setSchemas([]));
    }
  }, []);

  const loadCourses = useCallback(async () => {
    try {
      const res = await api.get('/admin/courses');
      setCourses(res.data || []);
      if (res.data && res.data.length > 0) {
        setSelectedCourseId(res.data[0].instructorCourseId);
        loadAssignments(res.data[0].instructorCourseId);
      }
    } catch (e) {
      console.error("Fehler beim Laden der Kurse");
    }
  }, [loadAssignments]);

  useEffect(() => {
    loadCourses();
  }, [loadCourses]);

  const startCreate = () => {
    if (!selectedCourseId) {
      toast.error('Bitte zuerst einen Kurs auswählen. Ist dir keiner zugeordnet, wende dich an einen Admin.');
      return;
    }
    setEditingAssignment({
      name: '', 
      courseId: selectedCourseId, 
      deadline: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
      publishedDate: null,
      lateSubmissionAllowed: false,
      penaltyPercentage: 10.0,
      maxAttempts: null,
      gradesReleased: true,
      connection: { id: dbConnections.length > 0 ? dbConnections[0].id : null }
    });
    setQuestions([]);
    setWizardStep(1);
  };

  const editAssignment = (a: Assignment) => {
    setEditingAssignment(a);
    api.get(`/assignments/${a.id}/questions`).then(res => setQuestions(res.data));
    setWizardStep(1);
  };

  const saveAssignment = async () => {
    // Client-side validation: surface a clear, friendly message instead of a
    // confusing backend 403/500 (e.g. an empty courseId → "Zugriff verweigert").
    if (!editingAssignment.id && !selectedCourseId) {
      toast.error('Bitte zuerst oben einen Kurs auswählen.');
      return null;
    }
    if (!editingAssignment.name?.trim()) {
      toast.error('Bitte einen Namen für die Aufgabe angeben.');
      return null;
    }
    if (!editingAssignment.connection?.id) {
      toast.error('Bitte eine Ziel-Datenbank wählen (unter „Erweiterte Einstellungen"). Lege ggf. zuerst eine Verbindung unter „Datenbanken" an.');
      return null;
    }
    try {
      let savedAssignment;
      if (editingAssignment.id) {
        const res = await api.put(`/assignments/${editingAssignment.id}`, editingAssignment);
        savedAssignment = res.data;
        toast.success("Aufgabe aktualisiert");
      } else {
        const res = await api.post(`/assignments?courseId=${encodeURIComponent(selectedCourseId)}`, editingAssignment);
        savedAssignment = res.data;
        setEditingAssignment(savedAssignment);
        toast.success("Aufgabe erstellt");
      }
      loadAssignments(selectedCourseId);
      return savedAssignment;
    } catch (e: any) {
      const msg = e.response?.status === 403
        ? 'Kein Zugriff auf diesen Kurs — bitte wähle einen deiner eigenen Kurse.'
        : (e.response?.data?.details || e.response?.data?.message
            || (typeof e.response?.data === 'string' ? e.response.data : 'Bitte Eingaben prüfen.'));
      toast.error(`Speichern fehlgeschlagen: ${msg}`);
      return null;
    }
  };

  const addQuestion = () => {
    const newQ: any = {
      name: `Frage ${questions.length + 1}`,
      instructorQuery: 'SELECT * FROM ',
      marks: 10.0,
      partialMarkParameters: { ...defaultParams }
    };
    setQuestions([...questions, newQ]);
  };

  const saveQuestion = async (q: any, idx: number) => {
    if (!q.name?.trim()) { toast.error('Bitte einen Titel für die Frage angeben.'); return; }
    if (!q.instructorQuery?.trim()) { toast.error(`Bitte eine Musterlösung (SQL) für „${q.name}" angeben.`); return; }
    if (!editingAssignment?.id) {
        const saved = await saveAssignment();
        if (!saved) return;
    }

    try {
      const payload = { ...q, assignmentId: editingAssignment.id };
      if (q.id) {
        await api.put(`/admin/questions/${q.id}`, payload);
      } else {
        const res = await api.post('/admin/questions', payload);
        const newQs = [...questions];
        newQs[idx] = res.data;
        setQuestions(newQs);
      }
      toast.success(`Frage "${q.name}" gespeichert`);
    } catch (e: any) {
      // The backend returns the validation message as a plain string body
      // (e.g. "Fehler in der Musterlösung: …"), not a JSON {message}.
      const msg = typeof e.response?.data === 'string'
        ? e.response.data
        : (e.response?.data?.message || 'Ungültige Abfrage');
      toast.error(`SQL Fehler in "${q.name}": ${msg}`);
    }
  };

  const saveAllQuestions = async () => {
    if (questions.length === 0) { toast.error('Füge zuerst mindestens eine Frage hinzu.'); return; }
    const invalid = questions.find(q => !q.name?.trim() || !(q as any).instructorQuery?.trim());
    if (invalid) { toast.error('Jede Frage braucht einen Titel und eine Musterlösung (SQL).'); return; }
    if (!editingAssignment?.id) {
        const saved = await saveAssignment();
        if (!saved) return;
    }

    toast.loading("Speichere alle Fragen...", { id: 'save-all' });
    let successCount = 0;
    const failed: string[] = [];
    for(let i=0; i < questions.length; i++) {
        const q = questions[i];
        try {
            const payload = { ...q, assignmentId: editingAssignment.id };
            if (q.id) {
                await api.put(`/admin/questions/${q.id}`, payload);
            } else {
                const res = await api.post('/admin/questions', payload);
                questions[i] = res.data;
            }
            successCount++;
        } catch(e) { failed.push(q.name || `Frage ${i + 1}`); }
    }
    setQuestions([...questions]);
    if (failed.length === 0) {
      toast.success(`${successCount} von ${questions.length} Fragen gespeichert`, { id: 'save-all' });
    } else {
      toast.error(`Gespeichert: ${successCount}/${questions.length}. Fehlerhaft: ${failed.join(', ')} — prüfe deren Musterlösung.`, { id: 'save-all' });
    }
  };

  const deleteQuestion = async () => {
    const { id, idx } = deleteModal;
    if (idx === undefined) return;
    try {
      if (id) {
        await api.delete(`/admin/questions/${id}`);
      }
      const newQs = [...questions];
      newQs.splice(idx, 1);
      setQuestions(newQs);
      toast.success("Frage entfernt");
    } catch (e) {
      toast.error("Fehler beim Löschen");
    }
  };

  const deleteAssignment = async () => {
    if (!deleteModal.id) return;
    try {
      await api.delete(`/assignments/${deleteModal.id}`);
      toast.success("Assignment gelöscht");
      loadAssignments(selectedCourseId);
    } catch (e) {
      toast.error("Löschen fehlgeschlagen");
    }
  };

  const duplicateAssignment = async (id: number) => {
    try {
      await api.post(`/assignments/${id}/duplicate`);
      toast.success("Assignment dupliziert");
      loadAssignments(selectedCourseId);
    } catch (e) {
      toast.error("Duplizieren fehlgeschlagen");
    }
  };

  const handleConfirmDelete = () => {
    if (deleteModal.type === 'question') {
      deleteQuestion();
    } else if (deleteModal.type === 'assignment') {
      deleteAssignment();
    }
  };

  const updateQuestionParams = (idx: number, key: keyof PartialMarkParameters, val: number) => {
    const newQs = [...questions];
    if (!newQs[idx].partialMarkParameters) {
        newQs[idx].partialMarkParameters = { ...defaultParams };
    }
    (newQs[idx].partialMarkParameters as any)[key] = val;
    setQuestions(newQs);
  };

  if (showStats) {
    return (
      <div className="space-y-6 animate-fadeIn">
        <button onClick={() => setShowStats(null)} className="btn-ghost mb-6">
          <ChevronLeft size={18} /> Zurück zur Übersicht
        </button>
        <AssignmentStats assignmentId={showStats} />
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fadeIn max-w-7xl mx-auto">
      <ConfirmationModal 
        isOpen={deleteModal.isOpen}
        onClose={() => setDeleteModal({ isOpen: false, type: null, id: null })}
        onConfirm={handleConfirmDelete}
        title={deleteModal.type === 'question' ? 'Frage löschen' : 'Assignment löschen'}
        message={deleteModal.type === 'question' ? 'Soll diese Frage wirklich entfernt werden?' : 'Soll dieses Assignment mit allen Fragen gelöscht werden?'}
      />

      {extensionsFor && (
        <DeadlineExtensionModal
          assignmentId={extensionsFor.id}
          assignmentName={extensionsFor.name}
          onClose={() => setExtensionsFor(null)}
        />
      )}

      {!editingAssignment ? (
        <div className="bg-white dark:bg-ink-card rounded-[2.5rem] p-10 shadow-2xl shadow-blue-500/5 border border-slate-200 dark:border-ink-border transition-all">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-6">
            <div className="flex items-center gap-5">
              <div className="p-4 bg-brand-500 text-white rounded-[1.5rem] shadow-lg shadow-blue-500/20">
                <ClipboardList size={32} />
              </div>
              <div>
                <h3 className="text-2xl font-bold dark:text-white tracking-tight">Aufgaben-Verwaltung</h3>
                <p className="text-xs text-gray-400 font-bold uppercase tracking-widest mt-1">Erstellen und verwalten Sie Kursinhalte</p>
              </div>
            </div>
            <div className="flex gap-4 w-full md:w-auto">
              <select
                className="x-select flex-1 md:w-72"
                value={selectedCourseId}
                onChange={e => loadAssignments(e.target.value)}
              >
                {courses.length === 0 && <option value="">Kein Kurs verfügbar</option>}
                {courses.map(c => <option key={c.instructorCourseId} value={c.instructorCourseId}>{c.courseName}</option>)}
              </select>
              <button onClick={startCreate} disabled={!selectedCourseId} className="btn-primary">
                <Plus size={18} /> Neu
              </button>
            </div>
          </div>

          {courses.length === 0 && (
            <div className="mb-8 flex items-start gap-3 p-5 rounded-2xl border border-amber-200 dark:border-amber-900/40 bg-amber-50/70 dark:bg-amber-950/20">
              <AlertCircle className="text-amber-500 shrink-0 mt-0.5" size={20} />
              <p className="text-sm font-medium text-amber-800 dark:text-amber-300">
                Dir ist noch kein Kurs zugeordnet. Bitte einen Admin, dich einem Kurs zuzuweisen — danach kannst du Aufgaben anlegen.
              </p>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {assignments.map(a => (
              <div key={a.id} className="p-8 rounded-[2rem] border-2 border-gray-50 dark:border-ink-border hover:border-blue-500/30 dark:hover:border-blue-500/30 transition-all group bg-white dark:bg-ink-card/50 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-24 h-24 bg-brand-500/5 rounded-full -mr-12 -mt-12 transition-all group-hover:scale-150"></div>
                
                <div className="flex justify-between items-start mb-6 relative z-10">
                  <span className="bg-brand-50 dark:bg-brand-950/30 text-brand-600 dark:text-brand-400 text-[10px] font-bold px-4 py-1.5 rounded-full uppercase tracking-widest border border-blue-100 dark:border-brand-800">
                    ID: {a.id}
                  </span>
                  <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-all translate-y-2 group-hover:translate-y-0">
                    <button onClick={() => editAssignment(a)} className="icon-btn hover:text-brand-600" title="Bearbeiten"><Edit size={18}/></button>
                    <button onClick={() => duplicateAssignment(a.id)} className="icon-btn hover:text-amber-500" title="Duplizieren"><Copy size={18}/></button>
                    <button onClick={() => setShowStats(a.id)} className="icon-btn hover:text-easy" title="Statistiken"><BarChart3 size={18}/></button>
                    <button onClick={() => setExtensionsFor(a)} className="icon-btn hover:text-brand-600" title="Fristverlängerungen"><CalendarClock size={18}/></button>
                    <button onClick={() => setDeleteModal({ isOpen: true, type: 'assignment', id: a.id })} className="icon-btn hover:text-hard" title="Löschen"><Trash2 size={18}/></button>
                  </div>
                </div>
                
                <h4 className="text-xl font-bold dark:text-white mb-4 line-clamp-1">{a.name}</h4>
                
                {assignmentStats[a.id] && assignmentStats[a.id].some(q => q.successRate < 0.4 && q.uniqueUsers > 2) && (
                  <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-800 rounded-xl flex items-center gap-3 animate-pulse">
                    <AlertCircle className="text-red-500" size={18} />
                    <span className="text-[10px] font-bold text-red-600 dark:text-red-400 uppercase tracking-tight">Kritische Erfolgsrate bei Fragen!</span>
                  </div>
                )}
                
                <div className="space-y-3 relative z-10">
                  <div className="flex items-center text-xs text-gray-400 font-bold uppercase tracking-wider">
                     <Database size={14} className="mr-3 text-brand-500" /> Schema: {schemas.find(s => s.id === a.defaultSchemaId)?.schemaName || 'Standard'}
                  </div>
                  <div className="flex items-center text-xs text-gray-400 font-bold uppercase tracking-wider">
                     <Calendar size={14} className="mr-3 text-brand-500" /> Deadline: {new Date(a.deadline).toLocaleDateString()}
                  </div>
                  <div className="pt-4 flex items-center gap-2">
                    {(!a.publishedDate || new Date(a.publishedDate) <= new Date()) ? (
                      <span className="text-[10px] font-bold uppercase tracking-widest px-3 py-1 bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 rounded-lg flex items-center border border-green-100 dark:border-green-800">
                        <ShieldCheck size={12} className="mr-2"/> Live
                      </span>
                    ) : (
                      <span className="text-[10px] font-bold uppercase tracking-widest px-3 py-1 bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400 rounded-lg flex items-center border border-amber-100 dark:border-amber-800">
                        <Settings size={12} className="mr-2"/> Geplant: {new Date(a.publishedDate).toLocaleDateString()}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
            
            {assignments.length === 0 && (
              <div className="col-span-full py-20 text-center bg-gray-50 dark:bg-ink-soft/30 rounded-[2.5rem] border-4 border-dashed border-slate-200 dark:border-ink-border">
                 <Zap className="mx-auto mb-4 text-gray-300" size={48} />
                 <p className="text-gray-400 font-bold uppercase tracking-widest">Keine Assignments gefunden</p>
                 <button onClick={startCreate} className="mt-4 text-brand-500 font-bold hover:underline">Erstes Assignment erstellen</button>
              </div>
            )}
          </div>
        </div>
      ) : (
        <div className="bg-white dark:bg-ink-card rounded-[2.5rem] p-10 shadow-2xl shadow-blue-500/5 border border-slate-200 dark:border-ink-border animate-slideUp">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-4 border-b dark:border-ink-border pb-8">
            <button onClick={() => setEditingAssignment(null)} className="btn-ghost">
              <ChevronLeft size={18} /> Zurück
            </button>
            <div className="flex items-center gap-2">
               {[1, 2].map(step => (
                 <React.Fragment key={step}>
                   <div 
                    onClick={() => setWizardStep(step)}
                    className={`flex items-center gap-2 px-6 py-2 rounded-full cursor-pointer transition-all ${wizardStep === step ? 'bg-brand-500 text-white shadow-lg' : 'bg-gray-100 dark:bg-gray-700 text-gray-400'}`}
                   >
                     <span className="text-sm font-bold">{step === 1 ? <Layout size={16}/> : <FileText size={16}/>}</span>
                     <span className="text-[10px] font-bold uppercase tracking-widest">{step === 1 ? 'Basis-Konfiguration' : 'Fragen-Katalog'}</span>
                   </div>
                   {step === 1 && <div className="w-8 h-0.5 bg-gray-100 dark:bg-gray-700"></div>}
                 </React.Fragment>
               ))}
            </div>
            <div className="bg-indigo-50 dark:bg-indigo-900/30 px-6 py-3 rounded-2xl border border-indigo-100 dark:border-indigo-800">
               <span className="text-[10px] font-bold text-indigo-400 uppercase tracking-widest block mb-1">Gesamtpunktzahl</span>
               <span className="text-xl font-bold text-indigo-600 dark:text-indigo-400">{totalPoints.toFixed(1)} Pkt.</span>
            </div>
          </div>

          {wizardStep === 1 ? (
            <div className="space-y-8 animate-fadeIn">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="space-y-4">
                   <label className="x-label">Name des Assignments</label>
                   <input
                      className="x-input"
                      value={editingAssignment.name}
                      onChange={e => setEditingAssignment({...editingAssignment, name: e.target.value})}
                      placeholder="z.B. Woche 1: SELECT Statements"
                   />
                </div>
                <div className="space-y-4">
                   <label className="x-label">Deadline</label>
                   <input
                      type="datetime-local"
                      className="x-input"
                      value={editingAssignment.deadline ? new Date(editingAssignment.deadline).toISOString().slice(0, 16) : ''}
                      onChange={e => setEditingAssignment({...editingAssignment, deadline: e.target.value})}
                   />
                </div>
              </div>

              <div>
                <button
                  type="button"
                  onClick={() => setShowAdvAssignment(v => !v)}
                  className="flex items-center gap-2 text-[11px] font-bold uppercase tracking-[0.14em] text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors"
                  aria-expanded={showAdvAssignment}
                >
                  <Settings size={14} /> Erweiterte Einstellungen
                  <ChevronDown size={14} className={`transition-transform ${showAdvAssignment ? 'rotate-180' : ''}`} />
                </button>

                {showAdvAssignment && (
                  <>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mt-5">
                    <div className="space-y-4">
                       <label className="x-label flex items-center">
                         Ziel-Datenbank
                         <InfoTip
                           title="Verbindung"
                           content="An welche Datenbank sollen die Abfragen der Studenten gesendet werden?"
                         />
                       </label>
                       <select
                          className="x-select"
                          value={editingAssignment.connection?.id || ''}
                          onChange={e => setEditingAssignment({
                            ...editingAssignment,
                            connection: { id: parseInt(e.target.value) }
                          })}
                       >
                          <option value="">Verbindung wählen...</option>
                          {dbConnections.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                       </select>
                    </div>
                    <div className="space-y-4">
                       <label className="x-label flex items-center">
                         Standard-Schema
                         <InfoTip
                           title="Schema"
                           content="Welches Schema sehen Studierende im Schema-Explorer und in der SQL-Autovervollständigung?"
                         />
                       </label>
                       <select
                          className="x-select"
                          value={editingAssignment.defaultSchemaId ?? ''}
                          onChange={e => setEditingAssignment({
                            ...editingAssignment,
                            defaultSchemaId: e.target.value ? parseInt(e.target.value) : null
                          })}
                       >
                          <option value="">Standard (keines)</option>
                          {schemas.map(s => <option key={s.id} value={s.id}>{s.schemaName}</option>)}
                       </select>
                    </div>
                    <div className="space-y-4">
                       <label className="x-label">Veröffentlichung</label>
                       <input
                          type="datetime-local"
                          className="x-input"
                          value={editingAssignment.publishedDate ? new Date(editingAssignment.publishedDate).toISOString().slice(0, 16) : ''}
                          onChange={e => setEditingAssignment({...editingAssignment, publishedDate: e.target.value})}
                       />
                    </div>
                    <div className="space-y-4">
                       <label className="x-label">Penalty (Verspätung)</label>
                       <div className="flex items-center h-[60px] gap-4 px-6 bg-gray-50 dark:bg-ink-soft rounded-2xl border-2 border-gray-50 dark:border-ink-border">
                          <input
                            type="checkbox"
                            className="w-6 h-6 rounded-lg border-gray-300 text-brand-600 focus:ring-brand-500"
                            checked={editingAssignment.lateSubmissionAllowed || false}
                            onChange={e => setEditingAssignment({...editingAssignment, lateSubmissionAllowed: e.target.checked})}
                          />
                          <span className="text-sm font-bold text-gray-500 uppercase">Aktiviert</span>
                          {editingAssignment.lateSubmissionAllowed && (
                            <div className="flex items-center gap-2 ml-auto">
                              <input
                                type="number"
                                className="x-input w-20 py-1.5 text-center text-xs"
                                value={editingAssignment.penaltyPercentage || 10}
                                onChange={e => setEditingAssignment({...editingAssignment, penaltyPercentage: parseFloat(e.target.value)})}
                              />
                              <span className="text-[10px] font-bold text-gray-400">%</span>
                            </div>
                          )}
                       </div>
                    </div>
                    <div className="space-y-4">
                       <label className="x-label flex items-center">
                         Max. Versuche
                         <InfoTip title="Versuche" content="Maximale Anzahl an Abgaben pro Frage. Leer = unbegrenzt." />
                       </label>
                       <input
                          type="number" min="1"
                          placeholder="unbegrenzt"
                          className="x-input"
                          value={editingAssignment.maxAttempts ?? ''}
                          onChange={e => setEditingAssignment({...editingAssignment, maxAttempts: e.target.value ? parseInt(e.target.value) : null})}
                       />
                    </div>
                    <div className="space-y-4">
                       <label className="x-label">Noten-Freigabe</label>
                       <label className="flex items-center h-[60px] gap-4 px-6 bg-gray-50 dark:bg-ink-soft rounded-2xl border-2 border-gray-50 dark:border-ink-border cursor-pointer">
                          <input
                            type="checkbox"
                            className="w-6 h-6 rounded-lg border-gray-300 text-brand-600 focus:ring-brand-500"
                            checked={editingAssignment.gradesReleased !== false}
                            onChange={e => setEditingAssignment({...editingAssignment, gradesReleased: e.target.checked})}
                          />
                          <span className="text-sm font-bold text-gray-500 uppercase">{editingAssignment.gradesReleased !== false ? 'Sichtbar' : 'Verborgen'}</span>
                       </label>
                    </div>
                  </div>

                  <div className="mt-6 space-y-2">
                    <label className="x-label flex items-center">
                      Testdaten (optional)
                      <InfoTip
                        title="Feste Testdaten"
                        content="INSERT-Anweisungen, die einmal pro Aufgabe gespeichert werden. Beim Bewerten werden Referenz- und Studenten-Abfrage auf einer Wegwerf-Datenbank mit genau diesen Daten ausgeführt und verglichen — schnell, deterministisch und ohne Live-Datenbank. Voraussetzung: ein Standard-Schema ist gesetzt. Generieren kannst du sie im Dataset-Playground."
                      />
                    </label>
                    <textarea
                      className="x-input font-mono h-40 resize-y"
                      placeholder={"INSERT INTO students VALUES (1, 'Alice', 23);\nINSERT INTO students VALUES (2, 'Bob', 19);"}
                      value={editingAssignment.seedSql || ''}
                      onChange={e => setEditingAssignment({ ...editingAssignment, seedSql: e.target.value })}
                    />
                    <div className="flex flex-wrap items-center gap-3">
                      <label className="btn-secondary cursor-pointer text-xs">
                        <Upload size={14} /> .sql laden
                        <input
                          type="file"
                          accept=".sql,.txt"
                          className="hidden"
                          onChange={e => {
                            const f = e.target.files?.[0];
                            if (!f) return;
                            const r = new FileReader();
                            r.onload = () => setEditingAssignment({ ...editingAssignment, seedSql: String(r.result || '') });
                            r.readAsText(f);
                            e.currentTarget.value = '';
                          }}
                        />
                      </label>
                      <span className="text-[11px] text-gray-400">Einmal pro Aufgabe — wird beim Bewerten auf einer Wegwerf-DB genutzt (Standard-Schema erforderlich).</span>
                    </div>
                  </div>
                  </>
                )}
              </div>

              <div className="pt-6 flex justify-end">
                 <button
                  onClick={async () => { const saved = await saveAssignment(); if (saved) setWizardStep(2); }}
                  className="btn-primary"
                 >
                    Nächster Schritt <ChevronRight size={18} />
                 </button>
              </div>
            </div>
          ) : (
            <div className="space-y-10 animate-fadeIn">
              <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
                <div>
                  <h3 className="text-2xl font-bold dark:text-white">Fragenkatalog</h3>
                  <p className="text-xs text-gray-400 font-bold uppercase tracking-widest mt-1">Definieren Sie die Aufgabenstellungen</p>
                </div>
                <div className="flex gap-4 w-full md:w-auto">
                  <button onClick={addQuestion} className="btn-secondary flex-1 md:flex-none">
                    <Plus size={18} /> Hinzufügen
                  </button>
                  <button onClick={saveAllQuestions} className="btn-primary flex-1 md:flex-none">
                    <Save size={18} /> Alle speichern
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-1 gap-8">
                {questions.map((q, idx) => (
                  <div key={idx} className="bg-gray-50/50 dark:bg-ink-soft/30 rounded-[2rem] border-2 border-gray-50 dark:border-ink-border p-8 group relative">
                    <div className="flex justify-between items-start mb-8 gap-6">
                      <div className="flex-1 grid grid-cols-1 md:grid-cols-5 gap-6">
                        <div className="md:col-span-3 space-y-2">
                          <label className="x-label">Fragentitel</label>
                          <input 
                            className="x-input"
                            value={q.name}
                            onChange={e => {
                              const newQs = [...questions];
                              newQs[idx].name = e.target.value;
                              setQuestions(newQs);
                            }}
                          />
                        </div>
                        <div className="space-y-2">
                          <label className="x-label">Punkte</label>
                          <input 
                            type="number"
                            className="x-input text-center"
                            value={q.marks}
                            onChange={e => {
                              const newQs = [...questions];
                              newQs[idx].marks = parseFloat(e.target.value);
                              setQuestions(newQs);
                            }}
                          />
                        </div>
                        <div className="space-y-2">
                          <label className="x-label">Schwierigkeit</label>
                          <select
                            className="x-select"
                            value={(q as any).difficulty || ''}
                            onChange={e => {
                              const newQs = [...questions];
                              (newQs[idx] as any).difficulty = e.target.value || null;
                              setQuestions(newQs);
                            }}
                          >
                            <option value="">Auto (nach Punkten)</option>
                            <option value="EASY">Easy</option>
                            <option value="MEDIUM">Medium</option>
                            <option value="HARD">Hard</option>
                          </select>
                        </div>
                      </div>
                      <div className="flex gap-2">
                        <button onClick={() => saveQuestion(q, idx)} className="icon-btn hover:text-easy" title="Frage speichern"><Check size={20} /></button>
                        <button onClick={() => setDeleteModal({ isOpen: true, type: 'question', id: q.id, idx })} className="icon-btn hover:text-hard" title="Frage löschen"><Trash2 size={20} /></button>
                      </div>
                    </div>

                    <div className="space-y-2 mb-6">
                      <label className="x-label">Aufgabenstellung (sehen die Studierenden)</label>
                      <textarea
                        className="x-input h-28 resize-y"
                        value={(q as any).description || ''}
                        placeholder="Beschreibe die Aufgabe in Worten, z. B.: Gib die Namen aller Studierenden aus, die älter als 22 sind, sortiert nach Name."
                        onChange={e => { const n = [...questions]; (n[idx] as any).description = e.target.value; setQuestions(n); }}
                      />
                    </div>

                    <div className="space-y-2 mb-6">
                      <label className="x-label">Themen-Tags (kommagetrennt)</label>
                      <input
                        className="x-input"
                        value={(q as any).tags || ''}
                        placeholder="z. B. JOIN, GROUP BY, Subquery"
                        onChange={e => { const n = [...questions]; (n[idx] as any).tags = e.target.value; setQuestions(n); }}
                      />
                    </div>

                    <div className="space-y-2 mb-6">
                      <label className="x-label">Hinweise (einer pro Zeile, progressiv)</label>
                      <textarea
                        className="x-input h-24 resize-y"
                        value={typeof (q as any).hints === 'string' ? (q as any).hints : (Array.isArray((q as any).hints) ? (q as any).hints.join('\n') : '')}
                        placeholder={'Denk an die WHERE-Klausel.\nVergleiche mit >.'}
                        onChange={e => { const n = [...questions]; (n[idx] as any).hints = e.target.value; setQuestions(n); }}
                      />
                    </div>

                    <div className="space-y-3 mb-8">
                       <div className="flex justify-between items-center px-1">
                          <label className="x-label !mb-0">Musterlösung (SQL)</label>
                          <span className="text-[9px] font-bold text-brand-500 uppercase tracking-tighter">Wird gegen Schema geprüft</span>
                       </div>
                       <textarea 
                         className="x-input font-mono min-h-[120px]"
                         value={q.instructorQuery}
                         spellCheck={false}
                         onChange={e => {
                           const newQs = [...questions];
                           newQs[idx].instructorQuery = e.target.value;
                           setQuestions(newQs);
                         }}
                       />
                    </div>

                    <div className="flex flex-wrap gap-4">
                      <button 
                        onClick={() => setShowQuestionParams(showQuestionParams === idx ? null : idx)}
                        className={`flex items-center gap-2 px-5 py-3 rounded-2xl font-bold uppercase tracking-widest text-[10px] transition-all ${showQuestionParams === idx ? 'bg-brand-500 text-white' : 'bg-white dark:bg-ink-card text-gray-400 border dark:border-ink-border hover:text-brand-500'}`}
                      >
                        <Settings size={14} /> Gewichte {showQuestionParams === idx ? 'ausblenden' : 'anpassen'}
                      </button>
                    </div>

                    {showQuestionParams === idx && (
                      <div className="mt-8 p-8 rounded-[1.5rem] bg-white dark:bg-ink-card border-2 border-blue-50 dark:border-blue-900/30 grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-6 animate-fadeIn">
                        {Object.keys(defaultParams).filter(k => k !== 'maxPartialMarks').map(key => (
                          <div key={key} className="space-y-2 group/param">
                            <label className="text-[9px] font-bold text-gray-400 dark:text-gray-500 uppercase truncate block group-hover/param:text-brand-500 transition-colors" title={key}>{key}</label>
                            <input 
                              type="number" 
                              step="0.1"
                              className="x-input py-2 text-xs"
                              value={(q.partialMarkParameters as any)?.[key] ?? (defaultParams as any)[key]}
                              onChange={(e) => updateQuestionParams(idx, key as any, parseFloat(e.target.value))}
                            />
                          </div>
                        ))}
                      </div>
                    )}

                  </div>
                ))}
              </div>

              <div className="pt-10 flex justify-between">
                <button onClick={() => setWizardStep(1)} className="btn-ghost">
                  <ChevronLeft size={18} /> Metadaten anpassen
                </button>
                <button
                  onClick={async () => { const saved = await saveAssignment(); if (saved) setEditingAssignment(null); }}
                  className="btn-primary"
                >
                  <CheckCircle2 size={18} /> Fertigstellen & Speichern
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AssignmentManager;
