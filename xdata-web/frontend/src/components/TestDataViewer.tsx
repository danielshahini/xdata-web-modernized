import React, { useState, useEffect } from 'react';
import api from '../api';

interface TestDataViewerProps {
  questionId?: number;
}

const TestDataViewer: React.FC<TestDataViewerProps> = ({ questionId }) => {
  const [testData, setTestData] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!questionId) return;
    setLoading(true);
    api.get(`/evaluation/test-data/${questionId}`)
      .then((res: any) => setTestData(res.data))
      .catch(() => setTestData([]))
      .finally(() => setLoading(false));
  }, [questionId]);

  if (!questionId) return <div className="p-4 text-gray-500 italic">Bitte wählen Sie eine Frage in der Aufgabenverwaltung aus, um Testdaten zu sehen.</div>;
  if (loading) return <div className="p-4 animate-pulse bg-gray-50 rounded-xl">Lade Testdaten...</div>;

  return (
    <div className="bg-gray-900 rounded-2xl p-6 overflow-hidden">
      <div className="flex justify-between items-center mb-4 border-b border-gray-800 pb-4">
        <h4 className="text-blue-400 font-black uppercase text-xs tracking-widest">Generierte SMT Testdaten</h4>
        <span className="text-gray-500 text-[10px] font-bold">SQL INSERT Statements</span>
      </div>
      <div className="max-h-60 overflow-y-auto custom-scrollbar">
        {testData.length > 0 ? (
          <pre className="text-green-500 font-mono text-xs whitespace-pre-wrap leading-relaxed">
            {testData.join('\n')}
          </pre>
        ) : (
          <p className="text-gray-600 text-xs italic">Keine Testdaten generiert oder Schema fehlt.</p>
        )}
      </div>
    </div>
  );
};

export default TestDataViewer;
