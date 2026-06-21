import React from 'react';

// Renders a result set; rows present in `highlight` get a colored background
// (used for the expected-vs-actual diff: missing rows in green, extra rows in red).
const rowKey = (r: any[]) => JSON.stringify(r);

const DiffTable: React.FC<{ title: string; data: any; highlight?: any[][]; accent: 'easy' | 'hard' }> = ({ title, data, highlight, accent }) => {
  const hi = new Set((highlight || []).map(rowKey));
  const cols: string[] = data?.columns || [];
  const rows: any[][] = data?.rows || [];
  return (
    <div className="rounded-xl border border-slate-200 dark:border-ink-border overflow-hidden">
      <div className="px-4 py-2 bg-white/60 dark:bg-ink-card/60 border-b border-slate-200 dark:border-ink-border flex items-center justify-between">
        <span className="kicker">{title}</span>
        <span className="text-[10px] text-slate-400">{rows.length} Zeile(n){data?.truncated ? ' · gekürzt' : ''}</span>
      </div>
      <div className="overflow-x-auto max-h-72">
        <table className="w-full text-left border-collapse text-xs">
          <thead><tr>{cols.map((c, i) => <th key={i} className="x-th !py-2">{c}</th>)}</tr></thead>
          <tbody>
            {rows.map((r, ri) => (
              <tr key={ri} className={hi.has(rowKey(r)) ? (accent === 'easy' ? 'bg-easy/10' : 'bg-hard/10') : ''}>
                {r.map((cell, ci) => <td key={ci} className="x-td !py-2 font-mono">{cell === null ? <span className="text-slate-300 italic">NULL</span> : String(cell)}</td>)}
              </tr>
            ))}
            {rows.length === 0 && <tr><td className="x-td !py-2 text-slate-400 italic" colSpan={cols.length || 1}>keine Zeilen</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default DiffTable;
