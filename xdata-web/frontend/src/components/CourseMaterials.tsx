import React, { useState, useEffect, useCallback } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import api from '../api';
import { toast } from 'react-hot-toast';
import { Course } from '../types';
import { BookMarked, Plus, Trash2, Link as LinkIcon, FileText } from 'lucide-react';

interface Material { id: number; courseId: string; title: string; type: string; content?: string; createdAt?: string; }

const CourseMaterials: React.FC = () => {
  const { data: coursesData } = useAsyncData<Course[]>(() => api.get('/admin/courses').then(r => r.data || []), []);
  const courses = coursesData ?? [];
  const [materials, setMaterials] = useState<Material[]>([]);
  const [courseId, setCourseId] = useState('');
  const [form, setForm] = useState({ title: '', type: 'LINK', content: '' });

  useEffect(() => { if (courses.length && !courseId) setCourseId(courses[0].instructorCourseId); }, [courses, courseId]);

  const load = useCallback(() => {
    api.get('/materials').then(r => setMaterials(r.data || [])).catch(() => {});
  }, []);
  useEffect(() => { load(); }, [load]);

  const add = async () => {
    if (!form.title.trim()) { toast.error('Bitte einen Titel angeben.'); return; }
    if (!form.content.trim()) { toast.error(form.type === 'LINK' ? 'Bitte eine URL angeben.' : 'Bitte einen Inhalt angeben.'); return; }
    if (form.type === 'LINK' && !/^https?:\/\//i.test(form.content.trim())) {
      toast.error('Bitte eine gültige URL angeben (beginnend mit http:// oder https://).');
      return;
    }
    try {
      await api.post(`/materials?courseId=${encodeURIComponent(courseId)}`, form);
      toast.success('Material hinzugefügt');
      setForm({ title: '', type: 'LINK', content: '' });
      load();
    } catch (e: any) { toast.error('Material konnte nicht hinzugefügt werden.'); }
  };

  const remove = async (id: number) => {
    try { await api.delete(`/materials/${id}`); load(); toast.success('Entfernt'); }
    catch { toast.error('Fehler beim Entfernen'); }
  };

  const visible = materials.filter(m => m.courseId === courseId);

  return (
    <div className="space-y-6">
      <div>
        <p className="kicker">Inhalte</p>
        <h2 className="mt-1 section-title text-2xl flex items-center gap-2"><BookMarked size={22} className="text-brand-500" /> Kursinhalte</h2>
      </div>

      <section className="x-card p-6 sm:p-8">
        <h3 className="section-title flex items-center gap-2 mb-5"><Plus size={20} className="text-brand-500" /> Neues Material</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="x-label">Kurs</label>
            <select className="x-select" value={courseId} onChange={e => setCourseId(e.target.value)}>
              {courses.map(c => <option key={c.instructorCourseId} value={c.instructorCourseId}>{c.courseName}</option>)}
            </select>
          </div>
          <div>
            <label className="x-label">Typ</label>
            <select className="x-select" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
              <option value="LINK">Link</option>
              <option value="TEXT">Notiz/Text</option>
            </select>
          </div>
          <div className="md:col-span-2">
            <label className="x-label">Titel</label>
            <input className="x-input" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="z. B. SQL-Spickzettel" />
          </div>
          <div className="md:col-span-2">
            <label className="x-label">{form.type === 'LINK' ? 'URL' : 'Inhalt'}</label>
            {form.type === 'LINK'
              ? <input className="x-input" value={form.content} onChange={e => setForm({ ...form, content: e.target.value })} placeholder="https://…" />
              : <textarea className="x-input h-28 resize-y" value={form.content} onChange={e => setForm({ ...form, content: e.target.value })} />}
          </div>
        </div>
        <div className="mt-5 flex justify-end"><button onClick={add} className="btn-primary"><Plus size={16} /> Hinzufügen</button></div>
      </section>

      <section className="x-card p-6 sm:p-8">
        <h3 className="section-title mb-5">Vorhandene Materialien</h3>
        <div className="space-y-3">
          {visible.map(m => (
            <div key={m.id} className="flex items-start justify-between gap-4 p-4 rounded-xl border border-slate-200 dark:border-ink-border bg-slate-50/50 dark:bg-ink-soft/40">
              <div className="flex items-start gap-3 min-w-0">
                {m.type === 'LINK' ? <LinkIcon size={18} className="text-brand-500 shrink-0 mt-0.5" /> : <FileText size={18} className="text-brand-500 shrink-0 mt-0.5" />}
                <div className="min-w-0">
                  <p className="font-semibold text-slate-900 dark:text-white">{m.title}</p>
                  {m.type === 'LINK'
                    ? <a href={m.content} target="_blank" rel="noreferrer" className="text-xs text-brand-600 hover:underline break-all">{m.content}</a>
                    : <p className="text-sm text-slate-600 dark:text-slate-400 whitespace-pre-line">{m.content}</p>}
                </div>
              </div>
              <button onClick={() => remove(m.id)} className="icon-btn hover:text-hard shrink-0" aria-label="Entfernen"><Trash2 size={16} /></button>
            </div>
          ))}
          {visible.length === 0 && <div className="empty-state"><BookMarked className="text-slate-300 dark:text-slate-600 mb-3" size={36} /><p className="text-sm text-slate-500">Noch kein Material für diesen Kurs.</p></div>}
        </div>
      </section>
    </div>
  );
};

export default CourseMaterials;
