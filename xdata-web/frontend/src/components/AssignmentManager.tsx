import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { 
  Plus, 
  ChevronLeft,
  ChevronRight,
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
  AlertCircle
} from 'lucide-react';
import api from '../api';
import { toast } from 'react-hot-toast';
import AssignmentStats from './AssignmentStats';
import ConfirmationModal from './common/ConfirmationModal';
import InfoTip from './common/InfoTip';
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
  const [showQuestionParams, setShowQuestionParams] = useState<number | null>(null);
  const [showStats, setShowStats] = useState<number | null>(null);
  const [wizardStep, setWizardStep] = useState<number>(1);
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
    setEditingAssignment({ 
      name: '', 
      courseId: selectedCourseId, 
      deadline: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
      publishedDate: null,
      lateSubmissionAllowed: false,
      penaltyPercentage: 10.0,
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
    try {
      let savedAssignment;
      if (editingAssignment.id) {
        const res = await api.put(`/assignments/${editingAssignment.id}`, editingAssignment);
        savedAssignment = res.data;
        toast.success("Assignment Metadaten aktualisiert");
      } else {
        const res = await api.post(`/assignments?courseId=${selectedCourseId}`, editingAssignment);
        savedAssignment = res.data;
        setEditingAssignment(savedAssignment);
        toast.success("Assignment erstellt");
      }
      loadAssignments(selectedCourseId);
      return savedAssignment;
    } catch (e: any) {
      toast.error(`Fehler beim Speichern: ${e.response?.data?.details || e.response?.data?.message || 'Unbekannter Fehler'}`);
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
      toast.error(`SQL Fehler in "${q.name}": ${e.response?.data?.message || 'Ungültige Abfrage'}`);
    }
  };

  const saveAllQuestions = async () => {
    if (!editingAssignment?.id) {
        const saved = await saveAssignment();
        if (!saved) return;
    }
    
    toast.loading("Speichere alle Fragen...", { id: 'save-all' });
    let successCount = 0;
    for(let i=0; i < questions.length; i++) {
        try {
            const q = questions[i];
            const payload = { ...q, assignmentId: editingAssignment.id };
            if (q.id) {
                await api.put(`/admin/questions/${q.id}`, payload);
            } else {
                const res = await api.post('/admin/questions', payload);
                questions[i] = res.data;
            }
            successCount++;
        } catch(e) {}
    }
    setQuestions([...questions]);
    toast.success(`${successCount} von ${questions.length} Fragen erfolgreich gespeichert`, { id: 'save-all' });
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
        <button onClick={() => setShowStats(null)} className="flex items-center text-blue-500 font-black hover:underline mb-6 group">
          <ChevronLeft size={24} className="mr-2 group-hover:-translate-x-1 transition-transform" /> ZURÜCK ZUR ÜBERSICHT
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

      {!editingAssignment ? (
        <div className="bg-white dark:bg-gray-800 rounded-[2.5rem] p-10 shadow-2xl shadow-blue-500/5 border border-gray-100 dark:border-gray-700 transition-all">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-6">
            <div className="flex items-center gap-5">
              <div className="p-4 bg-blue-500 text-white rounded-[1.5rem] shadow-lg shadow-blue-500/20">
                <ClipboardList size={32} />
              </div>
              <div>
                <h3 className="text-2xl font-black dark:text-white tracking-tight">Aufgaben-Verwaltung</h3>
                <p className="text-xs text-gray-400 font-bold uppercase tracking-widest mt-1">Erstellen und verwalten Sie Kursinhalte</p>
              </div>
            </div>
            <div className="flex gap-4 w-full md:w-auto">
              <select 
                className="flex-1 md:w-72 px-6 py-4 rounded-2xl border-2 border-gray-50 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-black dark:text-white focus:border-blue-500 outline-none transition-all cursor-pointer"
                value={selectedCourseId}
                onChange={e => loadAssignments(e.target.value)}
              >
                {courses.map(c => <option key={c.instructorCourseId} value={c.instructorCourseId}>{c.courseName}</option>)}
              </select>
              <button 
                onClick={startCreate}
                className="bg-blue-600 text-white px-8 py-4 rounded-2xl font-black flex items-center shadow-2xl shadow-blue-500/20 hover:bg-blue-700 active:scale-95 transition-all"
              >
                <Plus size={24} className="mr-2" /> NEU
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {assignments.map(a => (
              <div key={a.id} className="p-8 rounded-[2rem] border-2 border-gray-50 dark:border-gray-700 hover:border-blue-500/30 dark:hover:border-blue-500/30 transition-all group bg-white dark:bg-gray-800/50 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-24 h-24 bg-blue-500/5 rounded-full -mr-12 -mt-12 transition-all group-hover:scale-150"></div>
                
                <div className="flex justify-between items-start mb-6 relative z-10">
                  <span className="bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-[10px] font-black px-4 py-1.5 rounded-full uppercase tracking-widest border border-blue-100 dark:border-blue-800">
                    ID: {a.id}
                  </span>
                  <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-all translate-y-2 group-hover:translate-y-0">
                    <button onClick={() => editAssignment(a)} className="p-2 text-gray-400 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/30 rounded-lg transition-all" title="Bearbeiten"><Edit size={18}/></button>
                    <button onClick={() => duplicateAssignment(a.id)} className="p-2 text-gray-400 hover:text-amber-500 hover:bg-amber-50 dark:hover:bg-amber-900/30 rounded-lg transition-all" title="Duplizieren"><Copy size={18}/></button>
                    <button onClick={() => setShowStats(a.id)} className="p-2 text-gray-400 hover:text-green-500 hover:bg-green-50 dark:hover:bg-green-900/30 rounded-lg transition-all" title="Statistiken"><BarChart3 size={18}/></button>
                    <button onClick={() => setDeleteModal({ isOpen: true, type: 'assignment', id: a.id })} className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg transition-all" title="Löschen"><Trash2 size={18}/></button>
                  </div>
                </div>
                
                <h4 className="text-xl font-black dark:text-white mb-4 line-clamp-1">{a.name}</h4>
                
                {assignmentStats[a.id] && assignmentStats[a.id].some(q => q.successRate < 0.4 && q.uniqueUsers > 2) && (
                  <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-800 rounded-xl flex items-center gap-3 animate-pulse">
                    <AlertCircle className="text-red-500" size={18} />
                    <span className="text-[10px] font-black text-red-600 dark:text-red-400 uppercase tracking-tight">Kritische Erfolgsrate bei Fragen!</span>
                  </div>
                )}
                
                <div className="space-y-3 relative z-10">
                  <div className="flex items-center text-xs text-gray-400 font-bold uppercase tracking-wider">
                     <Database size={14} className="mr-3 text-blue-500" /> Schema: {a.defaultSchemaId || 'Standard'}
                  </div>
                  <div className="flex items-center text-xs text-gray-400 font-bold uppercase tracking-wider">
                     <Calendar size={14} className="mr-3 text-blue-500" /> Deadline: {new Date(a.deadline).toLocaleDateString()}
                  </div>
                  <div className="pt-4 flex items-center gap-2">
                    {(!a.publishedDate || new Date(a.publishedDate) <= new Date()) ? (
                      <span className="text-[10px] font-black uppercase tracking-widest px-3 py-1 bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400 rounded-lg flex items-center border border-green-100 dark:border-green-800">
                        <ShieldCheck size={12} className="mr-2"/> Live
                      </span>
                    ) : (
                      <span className="text-[10px] font-black uppercase tracking-widest px-3 py-1 bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400 rounded-lg flex items-center border border-amber-100 dark:border-amber-800">
                        <Settings size={12} className="mr-2"/> Geplant: {new Date(a.publishedDate).toLocaleDateString()}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
            
            {assignments.length === 0 && (
              <div className="col-span-full py-20 text-center bg-gray-50 dark:bg-gray-900/30 rounded-[2.5rem] border-4 border-dashed border-gray-100 dark:border-gray-800">
                 <Zap className="mx-auto mb-4 text-gray-300" size={48} />
                 <p className="text-gray-400 font-black uppercase tracking-widest">Keine Assignments gefunden</p>
                 <button onClick={startCreate} className="mt-4 text-blue-500 font-bold hover:underline">Erstes Assignment erstellen</button>
              </div>
            )}
          </div>
        </div>
      ) : (
        <div className="bg-white dark:bg-gray-800 rounded-[2.5rem] p-10 shadow-2xl shadow-blue-500/5 border border-gray-100 dark:border-gray-700 animate-slideUp">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-4 border-b dark:border-gray-700 pb-8">
            <button onClick={() => setEditingAssignment(null)} className="flex items-center text-gray-400 font-black hover:text-gray-600 transition-colors uppercase tracking-widest text-xs">
              <ChevronLeft size={20} className="mr-2" /> Zurück
            </button>
            <div className="flex items-center gap-2">
               {[1, 2].map(step => (
                 <React.Fragment key={step}>
                   <div 
                    onClick={() => setWizardStep(step)}
                    className={`flex items-center gap-2 px-6 py-2 rounded-full cursor-pointer transition-all ${wizardStep === step ? 'bg-blue-500 text-white shadow-lg' : 'bg-gray-100 dark:bg-gray-700 text-gray-400'}`}
                   >
                     <span className="text-sm font-black">{step === 1 ? <Layout size={16}/> : <FileText size={16}/>}</span>
                     <span className="text-[10px] font-black uppercase tracking-widest">{step === 1 ? 'Basis-Konfiguration' : 'Fragen-Katalog'}</span>
                   </div>
                   {step === 1 && <div className="w-8 h-0.5 bg-gray-100 dark:bg-gray-700"></div>}
                 </React.Fragment>
               ))}
            </div>
            <div className="bg-indigo-50 dark:bg-indigo-900/30 px-6 py-3 rounded-2xl border border-indigo-100 dark:border-indigo-800">
               <span className="text-[10px] font-black text-indigo-400 uppercase tracking-widest block mb-1">Gesamtpunktzahl</span>
               <span className="text-xl font-black text-indigo-600 dark:text-indigo-400">{totalPoints.toFixed(1)} Pkt.</span>
            </div>
          </div>

          {wizardStep === 1 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 animate-fadeIn">
              <div className="space-y-4">
                 <label className="text-[10px] font-black uppercase text-gray-400 tracking-widest ml-1 flex items-center">
                    Name des Assignments
                 </label>
                 <input 
                    className="w-full px-6 py-4 rounded-2xl border-2 border-gray-50 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-black dark:text-white focus:border-blue-500 outline-none transition-all"
                    value={editingAssignment.name}
                    onChange={e => setEditingAssignment({...editingAssignment, name: e.target.value})}
                    placeholder="z.B. Woche 1: SELECT Statements"
                 />
              </div>
              <div className="space-y-4">
                 <label className="text-[10px] font-black uppercase text-gray-400 tracking-widest ml-1 flex items-center">
                   Ziel-Datenbank
                   <InfoTip 
                     title="Verbindung"
                     content="An welche Datenbank sollen die Abfragen der Studenten gesendet werden?"
                   />
                 </label>
                 <select 
                    className="w-full px-6 py-4 rounded-2xl border-2 border-gray-50 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-black dark:text-white focus:border-blue-500 outline-none transition-all"
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
                 <label className="text-[10px] font-black uppercase text-gray-400 tracking-widest ml-1">Deadline</label>
                 <input 
                    type="datetime-local"
                    className="w-full px-6 py-4 rounded-2xl border-2 border-gray-50 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-black dark:text-white focus:border-blue-500 outline-none transition-all"
                    value={editingAssignment.deadline ? new Date(editingAssignment.deadline).toISOString().slice(0, 16) : ''}
                    onChange={e => setEditingAssignment({...editingAssignment, deadline: e.target.value})}
                 />
              </div>
              <div className="space-y-4">
                 <label className="text-[10px] font-black uppercase text-gray-400 tracking-widest ml-1">Veröffentlichung</label>
                 <input 
                    type="datetime-local"
                    className="w-full px-6 py-4 rounded-2xl border-2 border-gray-50 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-black dark:text-white focus:border-blue-500 outline-none transition-all"
                    value={editingAssignment.publishedDate ? new Date(editingAssignment.publishedDate).toISOString().slice(0, 16) : ''}
                    onChange={e => setEditingAssignment({...editingAssignment, publishedDate: e.target.value})}
                 />
              </div>
              <div className="space-y-4">
                 <label className="text-[10px] font-black uppercase text-gray-400 tracking-widest ml-1">Penalty (Verspätung)</label>
                 <div className="flex items-center h-[60px] gap-4 px-6 bg-gray-50 dark:bg-gray-900 rounded-2xl border-2 border-gray-50 dark:border-gray-700">
                    <input 
                      type="checkbox"
                      className="w-6 h-6 rounded-lg border-gray-300 text-blue-600 focus:ring-blue-500"
                      checked={editingAssignment.lateSubmissionAllowed || false}
                      onChange={e => setEditingAssignment({...editingAssignment, lateSubmissionAllowed: e.target.checked})}
                    />
                    <span className="text-sm font-black text-gray-500 uppercase">Aktiviert</span>
                    {editingAssignment.lateSubmissionAllowed && (
                      <div className="flex items-center gap-2 ml-auto">
                        <input 
                          type="number"
                          className="w-20 px-3 py-1.5 rounded-xl border dark:border-gray-600 bg-white dark:bg-gray-800 text-xs font-black text-center"
                          value={editingAssignment.penaltyPercentage || 10}
                          onChange={e => setEditingAssignment({...editingAssignment, penaltyPercentage: parseFloat(e.target.value)})}
                        />
                        <span className="text-[10px] font-black text-gray-400">%</span>
                      </div>
                    )}
                 </div>
              </div>

              <div className="col-span-full pt-10 flex justify-end">
                 <button 
                  onClick={async () => { const saved = await saveAssignment(); if (saved) setWizardStep(2); }}
                  className="bg-blue-600 text-white px-12 py-5 rounded-2xl font-black shadow-2xl shadow-blue-500/20 flex items-center gap-3 hover:bg-blue-700 transition-all group"
                 >
                    Nächster Schritt <ChevronRight className="group-hover:translate-x-2 transition-transform" />
                 </button>
              </div>
            </div>
          ) : (
            <div className="space-y-10 animate-fadeIn">
              <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
                <div>
                  <h3 className="text-2xl font-black dark:text-white">Fragenkatalog</h3>
                  <p className="text-xs text-gray-400 font-bold uppercase tracking-widest mt-1">Definieren Sie die Aufgabenstellungen</p>
                </div>
                <div className="flex gap-4 w-full md:w-auto">
                  <button onClick={addQuestion} className="flex-1 md:flex-none px-8 py-4 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 rounded-2xl font-black hover:bg-gray-200 transition-all flex items-center justify-center">
                    <Plus size={20} className="mr-2" /> Hinzufügen
                  </button>
                  <button onClick={saveAllQuestions} className="flex-1 md:flex-none px-8 py-4 bg-blue-600 text-white rounded-2xl font-black shadow-xl shadow-blue-500/20 hover:bg-blue-700 transition-all flex items-center justify-center">
                    <Save size={20} className="mr-2" /> Alle speichern
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-1 gap-8">
                {questions.map((q, idx) => (
                  <div key={idx} className="bg-gray-50/50 dark:bg-gray-900/30 rounded-[2rem] border-2 border-gray-50 dark:border-gray-700 p-8 group relative">
                    <div className="flex justify-between items-start mb-8 gap-6">
                      <div className="flex-1 grid grid-cols-1 md:grid-cols-4 gap-6">
                        <div className="md:col-span-3 space-y-2">
                          <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Fragentitel</label>
                          <input 
                            className="w-full px-6 py-4 rounded-2xl border-2 border-white dark:border-gray-800 bg-white dark:bg-gray-800 font-black dark:text-white focus:border-blue-500 outline-none transition-all shadow-sm"
                            value={q.name}
                            onChange={e => {
                              const newQs = [...questions];
                              newQs[idx].name = e.target.value;
                              setQuestions(newQs);
                            }}
                          />
                        </div>
                        <div className="space-y-2">
                          <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Punkte</label>
                          <input 
                            type="number"
                            className="w-full px-6 py-4 rounded-2xl border-2 border-white dark:border-gray-800 bg-white dark:bg-gray-800 font-black dark:text-white focus:border-blue-500 outline-none transition-all shadow-sm text-center"
                            value={q.marks}
                            onChange={e => {
                              const newQs = [...questions];
                              newQs[idx].marks = parseFloat(e.target.value);
                              setQuestions(newQs);
                            }}
                          />
                        </div>
                      </div>
                      <div className="flex gap-2">
                        <button onClick={() => saveQuestion(q, idx)} className="p-4 bg-white dark:bg-gray-800 rounded-2xl text-green-500 shadow-sm border dark:border-gray-700 hover:bg-green-50 transition-all"><Check size={20} /></button>
                        <button onClick={() => setDeleteModal({ isOpen: true, type: 'question', id: q.id, idx })} className="p-4 bg-white dark:bg-gray-800 rounded-2xl text-red-500 shadow-sm border dark:border-gray-700 hover:bg-red-50 transition-all"><Trash2 size={20} /></button>
                      </div>
                    </div>

                    <div className="space-y-3 mb-8">
                       <div className="flex justify-between items-center px-1">
                          <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Musterlösung (SQL)</label>
                          <span className="text-[9px] font-black text-blue-500 uppercase tracking-tighter">Wird gegen Schema geprüft</span>
                       </div>
                       <textarea 
                         className="w-full p-6 rounded-[1.5rem] border-2 border-white dark:border-gray-800 bg-white dark:bg-gray-800 font-mono text-sm dark:text-blue-300 min-h-[120px] shadow-inner focus:border-blue-500 outline-none transition-all"
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
                        className={`flex items-center gap-2 px-5 py-3 rounded-2xl font-black uppercase tracking-widest text-[10px] transition-all ${showQuestionParams === idx ? 'bg-blue-500 text-white' : 'bg-white dark:bg-gray-800 text-gray-400 border dark:border-gray-700 hover:text-blue-500'}`}
                      >
                        <Settings size={14} /> Gewichte {showQuestionParams === idx ? 'ausblenden' : 'anpassen'}
                      </button>
                    </div>

                    {showQuestionParams === idx && (
                      <div className="mt-8 p-8 rounded-[1.5rem] bg-white dark:bg-gray-800 border-2 border-blue-50 dark:border-blue-900/30 grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-6 animate-fadeIn">
                        {Object.keys(defaultParams).filter(k => k !== 'maxPartialMarks').map(key => (
                          <div key={key} className="space-y-2 group/param">
                            <label className="text-[9px] font-black text-gray-400 dark:text-gray-500 uppercase truncate block group-hover/param:text-blue-500 transition-colors" title={key}>{key}</label>
                            <input 
                              type="number" 
                              step="0.1"
                              className="w-full px-3 py-2 rounded-xl border dark:border-gray-700 bg-gray-50 dark:bg-gray-900 font-black dark:text-white text-xs focus:border-blue-500 outline-none transition-all"
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
                <button 
                  onClick={() => setWizardStep(1)}
                  className="px-10 py-5 rounded-2xl font-black text-gray-400 uppercase tracking-widest hover:text-gray-600 transition-all flex items-center gap-3"
                >
                  <ChevronLeft size={20} /> Metadaten anpassen
                </button>
                <button 
                  onClick={async () => { const saved = await saveAssignment(); if (saved) setEditingAssignment(null); }}
                  className="bg-green-600 text-white px-12 py-5 rounded-2xl font-black shadow-2xl shadow-green-500/20 flex items-center gap-3 hover:bg-green-700 active:scale-95 transition-all"
                >
                  <CheckCircle2 size={24} /> Fertigstellen & Speichern
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
