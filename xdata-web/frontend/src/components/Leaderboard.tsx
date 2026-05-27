import React, { useState, useEffect } from 'react';
import api from '../api';
import { Trophy, Medal, Star, Filter, BookOpen, Zap } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';

interface LeaderboardEntry {
  username: string;
  loginId: string;
  totalMarks: number;
  xp: number;
}

interface Course {
  instructorCourseId: string;
  courseName: string;
}

const Leaderboard: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [entries, setEntries] = useState<LeaderboardEntry[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourse, setSelectedCourse] = useState<string | null>(searchParams.get('courseId'));
  const [loading, setLoading] = useState(true);
  
  const userJson = localStorage.getItem('user');
  const currentUser = userJson ? JSON.parse(userJson) : {};
  const isPrivileged = currentUser.role === 'ADMIN' || currentUser.role === 'INSTRUCTOR';

  useEffect(() => {
    if (isPrivileged) {
      api.get('/admin/courses')
        .then(res => {
          setCourses(res.data);
          if (res.data.length > 0 && !selectedCourse) {
            setSelectedCourse(res.data[0].instructorCourseId);
          }
        })
        .catch(err => console.error('Error loading courses', err));
    } else {
        setSelectedCourse(currentUser.courseId);
    }
  }, [isPrivileged, currentUser.courseId, selectedCourse]);

  useEffect(() => {
    if (!selectedCourse) return;
    
    setLoading(true);
    api.get('/student/leaderboard', { params: { courseId: selectedCourse } })
      .then(res => setEntries(res.data))
      .catch(err => {
          console.error('Leaderboard error', err);
          setEntries([]);
      })
      .finally(() => setLoading(false));
      
    setSearchParams({ courseId: selectedCourse });
  }, [selectedCourse, setSearchParams]);

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn p-4 pb-20">
      <div className="flex flex-col md:flex-row justify-between items-center gap-6">
        <div className="text-center md:text-left space-y-2">
          <div className="flex items-center justify-center md:justify-start gap-3">
             <Trophy className="text-yellow-500" size={32} />
             <h2 className="text-3xl font-black dark:text-white">Kurs <span className="text-blue-600">Leaderboard</span></h2>
          </div>
          <p className="text-gray-500 dark:text-gray-400 font-medium ml-1">Wer sind die SQL-Experten in deinem Kurs?</p>
        </div>

        {isPrivileged && courses.length > 0 && (
          <div className="relative w-full md:w-64">
            <BookOpen className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
            <select 
              className="pl-12 pr-10 py-3 bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl font-bold text-gray-700 dark:text-gray-200 outline-none focus:ring-2 focus:ring-blue-500 transition-all appearance-none w-full shadow-lg shadow-black/5"
              value={selectedCourse || ''}
              onChange={e => setSelectedCourse(e.target.value)}
            >
              {courses.map(c => <option key={c.instructorCourseId} value={c.instructorCourseId}>{c.courseName}</option>)}
            </select>
            <Filter className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-300" size={16} />
          </div>
        )}
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-2xl overflow-hidden transition-colors relative">
        <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-600 to-indigo-600"></div>
        
        {loading ? (
            <div className="p-20 text-center text-gray-400 font-black animate-pulse">Lade Rankings...</div>
        ) : (
            <div className="divide-y divide-gray-50 dark:divide-gray-700">
              {entries.length === 0 ? (
                <div className="p-20 text-center text-gray-400 italic font-medium">Noch keine Daten für diesen Kurs vorhanden.</div>
              ) : (
                entries.map((entry, index) => (
                  <div key={entry.loginId} className={`flex items-center p-6 transition-all hover:bg-gray-50 dark:hover:bg-gray-700/50 ${index === 0 ? 'bg-yellow-50/30 dark:bg-yellow-900/10' : ''}`}>
                    <div className="w-16 flex justify-center items-center">
                      {index === 0 ? <Medal className="text-yellow-500 drop-shadow-sm" size={32} /> : 
                       index === 1 ? <Medal className="text-gray-400" size={28} /> :
                       index === 2 ? <Medal className="text-orange-400" size={28} /> :
                       <span className="text-xl font-black text-gray-300 dark:text-gray-600">#{index + 1}</span>}
                    </div>
                    
                    <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/40 dark:to-blue-800/20 text-blue-600 dark:text-blue-400 flex items-center justify-center font-black text-xl mr-6 shadow-sm">
                      {(entry.username || "?").charAt(0).toUpperCase()}
                    </div>
    
                    <div className="flex-grow">
                      <div className="text-lg font-black text-gray-800 dark:text-gray-200 tracking-tight">{entry.username}</div>
                      <div className="flex items-center text-[10px] text-gray-400 dark:text-gray-500 font-black uppercase tracking-[0.2em] mt-1">
                        <Zap size={10} className="mr-1.5 text-yellow-500 fill-yellow-500" />
                        {entry.xp} XP • {index === 0 ? 'Meister-Entwickler' : index < 3 ? 'Top Performer' : 'SQL-Aspirant'}
                      </div>
                    </div>
    
                    <div className="text-right px-4">
                      <div className="text-3xl font-black text-blue-600 tabular-nums">{(entry.totalMarks || 0).toFixed(1)}</div>
                      <div className="text-[10px] font-black text-gray-400 uppercase tracking-widest leading-none mt-1">Punkte</div>
                    </div>
                  </div>
                )
              ))}
            </div>
        )}
      </div>
    </div>
  );
};

export default Leaderboard;
