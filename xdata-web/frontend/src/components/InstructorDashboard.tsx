import React, { useState, useEffect } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import SchemaManager from './SchemaManager';
import AssignmentManager from './AssignmentManager';
import Gradebook from './Gradebook';
import RegradeRequests from './RegradeRequests';
import CourseMaterials from './CourseMaterials';
import UserManager from './UserManager';
import AuditLogViewer from './AuditLogViewer';
import DbConnectionManager from './DbConnectionManager';
import AssignmentStats from './AssignmentStats';
import { useAuth } from '../context/AuthContext';
import { 
  ClipboardList, 
  Database, 
  Users, 
  Plug, 
  BarChart3,
  BookOpen,
  BookMarked,
  Gavel,
  GraduationCap,
  Briefcase,
  Bell,
  Trash2,
  Plus,
  ChevronDown
} from 'lucide-react';
import api from '../api';
import { toast } from 'react-hot-toast';
import { Course, Announcement } from '../types';

const InstructorDashboard: React.FC = () => {
  const { isAdmin } = useAuth();
  const [tab, setTab] = useState<'assignments' | 'gradebook' | 'regrades' | 'materials' | 'schemas' | 'users' | 'courses' | 'audit' | 'connections' | 'stats' | 'announcements'>('assignments');
  
  const [newAnnouncement, setNewAnnouncement] = useState({ title: '', content: '', courseId: '' });
  

  const { data: announcementsData, retry: reloadAnnouncements } = useAsyncData<Announcement[]>(
    () => api.get('/announcements').then(res => res.data || []),
    []
  );
  const announcements = announcementsData ?? [];

  const { data: coursesData, retry: reloadCourses } = useAsyncData<Course[]>(
    () => api.get('/admin/courses').then(res => res.data || []),
    []
  );
  const courses = coursesData ?? [];

  const [newCourse, setNewCourse] = useState({ courseName: '', instructorCourseId: '' });
  const handleCreateCourse = async () => {
    if (!newCourse.courseName.trim() || !newCourse.instructorCourseId.trim()) {
      toast.error('Please provide a course name and course ID.');
      return;
    }
    try {
      await api.post('/admin/courses', {
        courseName: newCourse.courseName.trim(),
        instructorCourseId: newCourse.instructorCourseId.trim(),
      });
      toast.success('Course created.');
      setNewCourse({ courseName: '', instructorCourseId: '' });
      reloadCourses();
    } catch (e: any) {
      toast.error(e?.response?.data || 'Course could not be created.');
    }
  };

  useEffect(() => {
    if (coursesData && coursesData.length > 0) {
      setNewAnnouncement(prev => prev.courseId ? prev : { ...prev, courseId: coursesData[0].instructorCourseId });
    }
  }, [coursesData]);

  const handleCreateAnnouncement = async () => {
    if (!newAnnouncement.title.trim() || !newAnnouncement.content.trim() || !newAnnouncement.courseId) {
      toast.error("Please fill in title, content, and course.");
      return;
    }
    try {
      await api.post(`/announcements?courseId=${newAnnouncement.courseId}`, {
        title: newAnnouncement.title,
        content: newAnnouncement.content
      });
      toast.success("Announcement created");
      setNewAnnouncement({ ...newAnnouncement, title: '', content: '' });
      reloadAnnouncements();
    } catch (e) {
      toast.error("Error while creating");
    }
  };

  const deleteAnnouncement = async (id: number) => {
    if (window.confirm("Delete announcement?")) {
      try {
        await api.delete(`/announcements/${id}`);
        reloadAnnouncements();
        toast.success("Deleted");
      } catch (e) {
        toast.error("Error while deleting");
      }
    }
  };


  // Everyday actions stay front-and-center; rarely used setup lives under "Advanced".
  const primaryItems = [
    { id: 'assignments', label: 'Assignments', icon: ClipboardList },
    { id: 'gradebook', label: 'Gradebook', icon: BookOpen },
    { id: 'regrades', label: 'Regrade requests', icon: Gavel },
    { id: 'users', label: 'Students', icon: Users },
    { id: 'announcements', label: 'Announcements', icon: Bell },
    { id: 'stats', label: 'Statistics', icon: BarChart3 },
  ];
  const advancedItems = [
    { id: 'materials', label: 'Course materials', icon: BookMarked },
    { id: 'schemas', label: 'SQL Schemas', icon: Database },
    { id: 'connections', label: 'Databases', icon: Plug },
    { id: 'courses', label: 'Courses', icon: GraduationCap },
    ...(isAdmin ? [{ id: 'audit', label: 'Audit Logs', icon: Briefcase }] : [])
  ];

  // Keep the advanced group open if the active tab lives inside it.
  const [showAdvanced, setShowAdvanced] = useState(
    ['materials', 'schemas', 'connections', 'courses', 'audit'].includes(tab)
  );

  const renderNavButton = (item: { id: string; label: string; icon: any }) => {
    const active = tab === item.id;
    return (
      <button
        key={item.id}
        onClick={() => setTab(item.id as any)}
        aria-current={active ? 'page' : undefined}
        className={`group w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 ${
          active
            ? 'bg-brand-600 text-white shadow-sm'
            : 'text-slate-600 dark:text-slate-300 hover:bg-white dark:hover:bg-ink-card hover:shadow-sm border border-transparent hover:border-slate-200 dark:hover:border-ink-border'
        }`}
      >
        <item.icon size={18} className={active ? 'text-white' : 'text-slate-400 group-hover:text-brand-500'} />
        {item.label}
      </button>
    );
  };

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-8 animate-fadeIn pb-20">
      <header className="animate-rise">
        <p className="kicker">{isAdmin ? 'Administration' : 'Instructor area'}</p>
        <h1 className="mt-1 text-3xl font-display font-extrabold tracking-tight text-slate-900 dark:text-white">Instructor Panel</h1>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Courses, assignments, and students in one place.</p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <nav className="lg:col-span-3 space-y-1.5" aria-label="Sections">
          {primaryItems.map(item => renderNavButton(item))}

          {advancedItems.length > 0 && (
            <>
              <button
                onClick={() => setShowAdvanced(v => !v)}
                className="w-full flex items-center justify-between gap-3 px-4 pt-4 pb-1.5 text-[11px] font-bold uppercase tracking-[0.14em] text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                aria-expanded={showAdvanced}
              >
                Advanced
                <ChevronDown size={14} className={`transition-transform ${showAdvanced ? 'rotate-180' : ''}`} />
              </button>
              {showAdvanced && advancedItems.map(item => renderNavButton(item))}
            </>
          )}
        </nav>

        <div className="lg:col-span-9 animate-slideUp">
          {tab === 'assignments' && <AssignmentManager />}
          {tab === 'gradebook' && <Gradebook />}
          {tab === 'regrades' && <RegradeRequests />}
          {tab === 'materials' && <CourseMaterials />}
          {tab === 'schemas' && <SchemaManager />}
          {tab === 'users' && <UserManager />}
          {tab === 'audit' && <AuditLogViewer />}
          {tab === 'connections' && <DbConnectionManager />}
          {tab === 'stats' && <AssignmentStats />}

          {tab === 'announcements' && (
            <div className="space-y-6">
              <section className="x-card p-6 sm:p-8">
                <h3 className="section-title flex items-center gap-2"><Plus size={20} className="text-brand-500" /> New Announcement</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                  <div>
                    <label className="x-label">Title</label>
                    <input
                      placeholder="e.g. Submission deadline extended"
                      className="x-input"
                      value={newAnnouncement.title}
                      onChange={e => setNewAnnouncement({...newAnnouncement, title: e.target.value})}
                    />
                  </div>
                  <div>
                    <label className="x-label">Course</label>
                    <select
                      className="x-select"
                      value={newAnnouncement.courseId}
                      onChange={e => setNewAnnouncement({...newAnnouncement, courseId: e.target.value})}
                    >
                      {courses.map(c => <option key={c.instructorCourseId} value={c.instructorCourseId}>{c.courseName}</option>)}
                    </select>
                  </div>
                </div>
                <div className="mt-4">
                  <label className="x-label">Content</label>
                  <textarea
                    placeholder="What should your students know?"
                    className="x-input h-32 resize-y"
                    value={newAnnouncement.content}
                    onChange={e => setNewAnnouncement({...newAnnouncement, content: e.target.value})}
                  />
                </div>
                <div className="mt-5 flex justify-end">
                  <button onClick={handleCreateAnnouncement} className="btn-primary"><Bell size={16} /> Publish</button>
                </div>
              </section>

              <section className="x-card p-6 sm:p-8">
                <h3 className="section-title mb-5">Published Announcements</h3>
                <div className="space-y-3">
                  {announcements.map(a => (
                    <div key={a.id} className="group flex justify-between items-start gap-4 p-5 rounded-xl border border-slate-200 dark:border-ink-border bg-slate-50/50 dark:bg-ink-soft/40">
                      <div className="min-w-0">
                        <p className="font-semibold text-slate-900 dark:text-white">{a.title}</p>
                        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1 whitespace-pre-line">{a.content}</p>
                        <div className="flex flex-wrap items-center gap-2 mt-3">
                          <span className="badge-brand">{a.course.courseName}</span>
                          <span className="text-xs text-slate-400">{new Date(a.createdAt).toLocaleString('en-US')}</span>
                        </div>
                      </div>
                      <button onClick={() => deleteAnnouncement(a.id)} className="icon-btn hover:text-hard shrink-0" title="Delete announcement" aria-label="Delete announcement">
                        <Trash2 size={18} />
                      </button>
                    </div>
                  ))}
                  {announcements.length === 0 && (
                    <div className="empty-state">
                      <Bell className="text-slate-300 dark:text-slate-600 mb-3" size={36} />
                      <p className="text-sm font-medium text-slate-500 dark:text-slate-400">No announcements yet. Create the first one above.</p>
                    </div>
                  )}
                </div>
              </section>
            </div>
          )}

          {tab === 'courses' && (
            <div className="space-y-6">
              <section className="x-card p-6">
                <p className="font-semibold text-slate-900 dark:text-white mb-1">Create new course</p>
                <p className="text-sm text-slate-600 dark:text-slate-400 mb-4">You will automatically be assigned to the course as an instructor.</p>
                <div className="grid grid-cols-1 md:grid-cols-[1fr_1fr_auto] gap-3 items-end">
                  <div className="space-y-1">
                    <label className="x-label">Course name</label>
                    <input className="x-input" placeholder="e.g. Databases II"
                      value={newCourse.courseName}
                      onChange={e => setNewCourse({ ...newCourse, courseName: e.target.value })} />
                  </div>
                  <div className="space-y-1">
                    <label className="x-label">Course ID</label>
                    <input className="x-input font-mono" placeholder="e.g. DB2-2026"
                      value={newCourse.instructorCourseId}
                      onChange={e => setNewCourse({ ...newCourse, instructorCourseId: e.target.value })}
                      onKeyDown={e => { if (e.key === 'Enter') handleCreateCourse(); }} />
                  </div>
                  <button className="btn-primary" onClick={handleCreateCourse}>
                    <Plus size={18} /> Create
                  </button>
                </div>
              </section>

              <section className="x-card overflow-hidden">
                <table className="w-full">
                  <thead>
                    <tr>
                      <th className="x-th">Course name</th>
                      <th className="x-th">Course ID</th>
                    </tr>
                  </thead>
                  <tbody>
                    {courses.map(c => (
                      <tr key={c.id} className="x-row">
                        <td className="x-td font-semibold text-slate-900 dark:text-white">{c.courseName}</td>
                        <td className="x-td"><span className="font-mono text-brand-600 dark:text-brand-400">{c.instructorCourseId}</span></td>
                      </tr>
                    ))}
                    {courses.length === 0 && (
                      <tr><td className="x-td text-slate-400 italic" colSpan={2}>You have not been assigned to any course yet.</td></tr>
                    )}
                  </tbody>
                </table>
              </section>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default InstructorDashboard;
