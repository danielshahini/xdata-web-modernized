import React from 'react';
import { Table as TableIcon, Hash, Type } from 'lucide-react';

interface SchemaVisualizerProps {
  metadata: {
    schemaName: string;
    tables: {
      tableName: string;
      columns: {
        columnName: string;
        dataType: string;
      }[];
    }[];
  };
}

const SchemaVisualizer: React.FC<SchemaVisualizerProps> = ({ metadata }) => {
  if (!metadata || !metadata.tables) return null;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-fadeIn">
      {metadata.tables.map(table => (
        <div key={table.tableName} className="bg-white dark:bg-gray-800 rounded-3xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden flex flex-col group hover:border-blue-200 dark:hover:border-blue-800 transition-all">
          <div className="bg-gray-50 dark:bg-gray-900/50 px-5 py-3 border-b border-gray-100 dark:border-gray-700 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <TableIcon size={16} className="text-blue-500" />
              <span className="font-black text-xs uppercase tracking-wider dark:text-white">{table.tableName}</span>
            </div>
            <span className="text-[9px] font-black text-gray-400 uppercase tracking-widest">{table.columns.length} Spalten</span>
          </div>
          <div className="p-4 space-y-2">
            {table.columns.map(col => (
              <div key={col.columnName} className="flex justify-between items-center group/col">
                <div className="flex items-center gap-2">
                  <Hash size={10} className="text-gray-300 dark:text-gray-600 group-hover/col:text-blue-400 transition-colors" />
                  <span className="text-xs font-bold text-gray-700 dark:text-gray-300">{col.columnName}</span>
                </div>
                <div className="flex items-center gap-1.5">
                   <Type size={10} className="text-gray-300" />
                   <span className="text-[9px] font-black text-gray-400 dark:text-gray-500 uppercase bg-gray-50 dark:bg-gray-900 px-2 py-0.5 rounded-full">{col.dataType}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};

export default SchemaVisualizer;
