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
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  type = 'danger'
}) => {
  if (!isOpen) return null;

  const icons = {
    danger: <AlertTriangle className="text-red-600" size={24} />,
    warning: <AlertTriangle className="text-orange-500" size={24} />,
    info: <AlertTriangle className="text-brand-600" size={24} />
  };

  return (
    <div className="modal-overlay z-[100]">
      <div className="modal-card max-w-md p-8">
        <div className="flex justify-between items-start mb-6">
          <div className="flex items-center space-x-3">
            <div className={`p-3 rounded-2xl ${type === 'danger' ? 'bg-red-50 dark:bg-red-900/20' : type === 'warning' ? 'bg-orange-50 dark:bg-orange-900/20' : 'bg-brand-50 dark:bg-brand-950/20'}`}>
              {icons[type]}
            </div>
            <h3 className="text-xl font-bold dark:text-white">{title}</h3>
          </div>
          <button onClick={onClose} className="icon-btn hover:text-hard">
            <X size={20} />
          </button>
        </div>
        
        <p className="text-gray-600 dark:text-gray-400 mb-8 font-medium leading-relaxed">
          {message}
        </p>

        <div className="flex gap-4">
          <button onClick={onClose} className="btn-secondary flex-1">
            {cancelText}
          </button>
          <button
            onClick={() => { onConfirm(); onClose(); }}
            className={`flex-1 ${type === 'danger' ? 'btn-danger' : 'btn-primary'}`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
