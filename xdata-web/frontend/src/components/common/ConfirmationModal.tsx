import React from 'react';
import { AlertTriangle, X } from 'lucide-react';

interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'danger' | 'warning' | 'info';
}

const ConfirmationModal: React.FC<ConfirmationModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Bestätigen',
  cancelText = 'Abbrechen',
  type = 'danger'
}) => {
  if (!isOpen) return null;

  const colors = {
    danger: 'bg-red-600 hover:bg-red-700 shadow-red-100',
    warning: 'bg-orange-500 hover:bg-orange-600 shadow-orange-100',
    info: 'bg-blue-600 hover:bg-blue-700 shadow-blue-100'
  };

  const icons = {
    danger: <AlertTriangle className="text-red-600" size={24} />,
    warning: <AlertTriangle className="text-orange-500" size={24} />,
    info: <AlertTriangle className="text-blue-600" size={24} />
  };

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[100] p-4 animate-fadeIn">
      <div className="bg-white dark:bg-gray-800 rounded-3xl p-8 max-w-md w-full shadow-2xl animate-slideUp border border-gray-100 dark:border-gray-700">
        <div className="flex justify-between items-start mb-6">
          <div className="flex items-center space-x-3">
            <div className={`p-3 rounded-2xl ${type === 'danger' ? 'bg-red-50 dark:bg-red-900/20' : type === 'warning' ? 'bg-orange-50 dark:bg-orange-900/20' : 'bg-blue-50 dark:bg-blue-900/20'}`}>
              {icons[type]}
            </div>
            <h3 className="text-xl font-black dark:text-white">{title}</h3>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors">
            <X size={24} />
          </button>
        </div>
        
        <p className="text-gray-600 dark:text-gray-400 mb-8 font-medium leading-relaxed">
          {message}
        </p>

        <div className="flex gap-4">
          <button 
            onClick={onClose}
            className="flex-1 px-6 py-3 rounded-2xl font-black text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all"
          >
            {cancelText}
          </button>
          <button 
            onClick={() => { onConfirm(); onClose(); }}
            className={`flex-1 px-6 py-3 rounded-2xl font-black text-white transition-all shadow-lg dark:shadow-none ${colors[type]}`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
