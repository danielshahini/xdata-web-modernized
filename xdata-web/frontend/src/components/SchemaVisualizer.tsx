import React from 'react';
import { Table as TableIcon, KeyRound, Link2 } from 'lucide-react';

interface Column {
  columnName: string;
  dataType: string;
  primaryKey?: boolean;
  referencesTable?: string | null;
  referencesColumn?: string | null;
}

interface SchemaVisualizerProps {
  metadata: {
    schemaName: string;
    tables: {
      tableName: string;
      columns: Column[];
    }[];
  };
}

const SchemaVisualizer: React.FC<SchemaVisualizerProps> = ({ metadata }) => {
  if (!metadata || !metadata.tables) return null;

  const hasRelations = metadata.tables.some(t => t.columns.some(c => c.referencesTable));

  return (
    <div className="space-y-4 animate-fadeIn">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {metadata.tables.map(table => (
          <div key={table.tableName} className="bg-white dark:bg-ink-card rounded-2xl border border-slate-200 dark:border-ink-border shadow-sm overflow-hidden flex flex-col group hover:border-brand-200 dark:hover:border-blue-800 transition-all">
            <div className="bg-gray-50 dark:bg-ink-soft/50 px-5 py-3 border-b border-slate-200 dark:border-ink-border flex items-center justify-between">
              <div className="flex items-center gap-2">
                <TableIcon size={16} className="text-brand-500" />
                <span className="font-bold text-xs uppercase tracking-wider dark:text-white">{table.tableName}</span>
              </div>
              <span className="text-[9px] font-bold text-gray-400 uppercase tracking-widest">{table.columns.length} Spalten</span>
            </div>
            <div className="p-4 space-y-1.5">
              {table.columns.map(col => (
                <div key={col.columnName} className="flex justify-between items-center gap-2 group/col">
                  <div className="flex items-center gap-2 min-w-0">
                    {col.primaryKey
                      ? <KeyRound size={11} className="text-xp-500 shrink-0" />
                      : col.referencesTable
                        ? <Link2 size={11} className="text-brand-400 shrink-0" />
                        : <span className="w-[11px] shrink-0" />}
                    <span className={`text-xs font-bold truncate ${col.primaryKey ? 'text-xp-700 dark:text-xp-300' : 'text-gray-700 dark:text-gray-300'}`}>
                      {col.columnName}
                    </span>
                    {col.referencesTable && (
                      <span className="text-[9px] font-mono text-brand-500 truncate shrink-0" title={`Fremdschlüssel → ${col.referencesTable}.${col.referencesColumn}`}>
                        → {col.referencesTable}.{col.referencesColumn}
                      </span>
                    )}
                  </div>
                  <span className="text-[9px] font-bold text-gray-400 dark:text-gray-500 uppercase bg-gray-50 dark:bg-ink-soft px-2 py-0.5 rounded-full shrink-0">{col.dataType}</span>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Legend */}
      <div className="flex flex-wrap items-center gap-4 text-[10px] font-semibold text-gray-400">
        <span className="flex items-center gap-1.5"><KeyRound size={11} className="text-xp-500" /> Primärschlüssel</span>
        {hasRelations && <span className="flex items-center gap-1.5"><Link2 size={11} className="text-brand-400" /> Fremdschlüssel (Beziehung)</span>}
      </div>
    </div>
  );
};

export default SchemaVisualizer;
