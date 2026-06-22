import React from 'react';
import { QueryInfo, MarkInfo } from '../types';
import { Check, X } from 'lucide-react';

interface MarkInfoDisplayProps {
  data?: MarkInfo;
  markInfoJson?: string;
}

const MarkInfoDisplay: React.FC<MarkInfoDisplayProps> = ({ data, markInfoJson }) => {
  let markData = data;
  
  if (!markData && markInfoJson) {
    try {
      markData = JSON.parse(markInfoJson);
    } catch (e) {
      console.error("Error parsing MarkInfo", e);
      return null;
    }
  }

  if (!markData || !markData.subqueryData || markData.subqueryData.length === 0) return null;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center bg-brand-50 dark:bg-brand-950/20 p-4 rounded-2xl border border-blue-100 dark:border-brand-800">
        <div className="flex flex-col">
          <span className="text-[10px] font-bold text-brand-600 dark:text-brand-400 uppercase tracking-widest">Overall score</span>
          <span className="text-2xl font-bold dark:text-white">
            {markData.marks.toFixed(1)} <span className="text-gray-400 text-sm font-bold">/ {markData.maxMarks.toFixed(1)}</span>
          </span>
        </div>
        <div className="text-right">
          <div className="text-3xl font-bold text-brand-600">{markData.percentage.toFixed(0)}%</div>
        </div>
      </div>

      {markData.subqueryData.map((qi, idx) => (
        <QueryLevelDisplay key={idx} qi={qi} />
      ))}
    </div>
  );
};

const QueryLevelDisplay: React.FC<{ qi: QueryInfo }> = ({ qi }) => {
  const rows = [
    { label: 'Tables (FROM)', student: qi.studentRelations, instructor: qi.instructorRelations, marks: qi.studentRelationMarks },
    { label: 'Columns (SELECT)', student: qi.studentProjections, instructor: qi.instructorProjections, marks: qi.studentProjectionMarks },
    { label: 'Conditions (WHERE)', student: qi.studentPredicates, instructor: qi.instructorPredicates, marks: qi.studentPredicateMarks },
    { label: 'Joins (JOIN)', student: qi.studentJoins, instructor: qi.instructorJoins, marks: qi.studentJoinMarks },
    { label: 'Grouping (GROUP BY)', student: qi.studentGroupBy, instructor: qi.instructorGroupBy, marks: qi.studentGroupByMarks },
    { label: 'Sorting (ORDER BY)', student: qi.studentOrderBy, instructor: qi.instructorOrderBy, marks: qi.studentOrderByMarks },
  ];

  return (
    <div className="bg-white dark:bg-ink-card rounded-2xl border border-slate-200 dark:border-ink-border overflow-hidden shadow-sm">
      <div className="bg-gray-50 dark:bg-ink-soft/50 px-6 py-3 border-b border-slate-200 dark:border-ink-border">
        <h4 className="font-bold text-sm uppercase tracking-tight text-gray-500 dark:text-gray-400">
          {qi.level === 1 ? 'Main query' : `Subquery level ${qi.level}`}
        </h4>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="text-[10px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-50 dark:border-ink-border">
              <th className="px-6 py-3 w-1/4">Component</th>
              <th className="px-6 py-3 w-3/8">Your solution</th>
              <th className="px-6 py-3 w-3/8">Model solution</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50 dark:divide-gray-700/50">
            {rows.map((row) => (
              <tr key={row.label} className="hover:bg-gray-50/50 dark:hover:bg-gray-700/20 transition-colors">
                <td className="px-6 py-4">
                   <div className="font-bold text-sm text-gray-600 dark:text-gray-300">{row.label}</div>
                </td>
                <td className="px-6 py-4">
                  <ResultList items={row.student} marks={row.marks} />
                </td>
                <td className="px-6 py-4">
                  <div className="space-y-1">
                    {row.instructor && row.instructor.map((item, i) => (
                      <div key={i} className="text-xs font-medium text-gray-500 dark:text-gray-400 font-mono bg-gray-50 dark:bg-ink-soft/30 px-2 py-1 rounded-md inline-block mr-1">
                        {item}
                      </div>
                    ))}
                    {(!row.instructor || row.instructor.length === 0) && <span className="text-gray-300 dark:text-gray-600">-</span>}
                  </div>
                </td>
              </tr>
            ))}
            
            <tr className="hover:bg-gray-50/50 dark:hover:bg-gray-700/20 transition-colors">
              <td className="px-6 py-4 font-bold text-sm text-gray-600 dark:text-gray-300">Filter (HAVING)</td>
              <td className="px-6 py-4">
                 {qi.studentHaving ? (
                   <div className={`flex items-center space-x-2 text-xs font-bold ${qi.studentHavingMark > 0 ? 'text-green-600' : 'text-red-500'}`}>
                      {qi.studentHavingMark > 0 ? <Check size={14} /> : <X size={14} />}
                      <span className="font-mono">{qi.studentHaving}</span>
                      <span className="text-[10px] bg-gray-100 dark:bg-ink-soft px-1.5 py-0.5 rounded">
                        {qi.studentHavingMark > 0 ? '+' : ''}{qi.studentHavingMark.toFixed(1)}
                      </span>
                   </div>
                 ) : <span className="text-gray-300 dark:text-gray-600">-</span>}
              </td>
              <td className="px-6 py-4 text-xs font-medium text-gray-500 dark:text-gray-400 font-mono">
                {qi.instructorHaving || '-'}
              </td>
            </tr>

            <tr className="hover:bg-gray-50/50 dark:hover:bg-gray-700/20 transition-colors">
              <td className="px-6 py-4 font-bold text-sm text-gray-600 dark:text-gray-300">Duplicates (DISTINCT)</td>
              <td className="px-6 py-4">
                 <div className={`flex items-center space-x-2 text-xs font-bold ${qi.studentDistinctMark >= 0 ? 'text-green-600' : 'text-red-500'}`}>
                    {qi.studentDistinctMark >= 0 ? <Check size={14} /> : <X size={14} />}
                    <span>{qi.studentDistinct ? 'Yes' : 'No'}</span>
                    <span className="text-[10px] bg-gray-100 dark:bg-ink-soft px-1.5 py-0.5 rounded">
                      {qi.studentDistinctMark >= 0 ? '+' : ''}{qi.studentDistinctMark.toFixed(1)}
                    </span>
                 </div>
              </td>
              <td className="px-6 py-4 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase font-bold tracking-widest">
                {qi.instructorDistinct ? 'Yes' : 'No'}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};

const ResultList: React.FC<{ items: string[], marks: number[] }> = ({ items, marks }) => {
  if (!items || items.length === 0) return <span className="text-gray-300 dark:text-gray-600">-</span>;

  return (
    <div className="flex flex-wrap gap-2">
      {items.map((item, i) => {
        const mark = marks && marks[i] !== undefined ? marks[i] : 0;
        const success = mark > 0;
        return (
          <div 
            key={i} 
            className={`flex items-center space-x-1.5 px-2 py-1 rounded-lg border text-xs font-bold transition-all ${
              success 
                ? 'bg-green-50 border-green-100 text-green-700 dark:bg-green-900/20 dark:border-green-800 dark:text-green-400' 
                : 'bg-red-50 border-red-100 text-red-600 dark:bg-red-900/20 dark:border-red-800 dark:text-red-400'
            }`}
          >
            {success ? <Check size={12} /> : <X size={12} />}
            <span className="font-mono">{item}</span>
            <span className={`text-[10px] opacity-60`}>
              ({mark > 0 ? '+' : ''}{mark.toFixed(1)})
            </span>
          </div>
        );
      })}
    </div>
  );
};

export default MarkInfoDisplay;
