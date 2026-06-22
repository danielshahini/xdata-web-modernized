import React, { useState } from 'react';
import { KeyRound, X } from 'lucide-react';
import api from '../../api';
import { toast } from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';

interface Props {
  /** When true the modal cannot be dismissed (first-login forced change). */
  forced?: boolean;
  onClose?: () => void;
}

const ChangePasswordModal: React.FC<Props> = ({ forced = false, onClose }) => {
  const { markPasswordChanged } = useAuth();
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    if (newPassword.length < 8) { toast.error('The new password must be at least 8 characters long.'); return; }
    if (newPassword !== confirm) { toast.error('The passwords do not match.'); return; }
    setLoading(true);
    try {
      await api.post('/auth/change-password', { oldPassword, newPassword });
      toast.success('Password changed successfully.');
      markPasswordChanged();
      onClose?.();
    } catch (err: any) {
      toast.error(err.response?.data || 'Password could not be changed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-card max-w-md p-6 sm:p-8">
        <div className="flex items-start justify-between mb-1">
          <h3 className="section-title flex items-center gap-2">
            <KeyRound size={20} className="text-brand-500" /> Change password
          </h3>
          {!forced && (
            <button onClick={onClose} className="icon-btn" aria-label="Close"><X size={18} /></button>
          )}
        </div>
        <p className="text-sm text-slate-500 dark:text-slate-400 mb-6">
          {forced
            ? 'For security reasons, please set your own password before continuing.'
            : 'Choose a new password (at least 8 characters).'}
        </p>

        <div className="space-y-4">
          <div>
            <label className="x-label">Current password</label>
            <input type="password" className="x-input" value={oldPassword}
                   onChange={e => setOldPassword(e.target.value)} autoFocus />
          </div>
          <div>
            <label className="x-label">New password</label>
            <input type="password" className="x-input" value={newPassword}
                   onChange={e => setNewPassword(e.target.value)} />
          </div>
          <div>
            <label className="x-label">Confirm new password</label>
            <input type="password" className="x-input" value={confirm}
                   onChange={e => setConfirm(e.target.value)}
                   onKeyDown={e => { if (e.key === 'Enter') submit(); }} />
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-2">
          {!forced && <button onClick={onClose} className="btn-secondary">Cancel</button>}
          <button onClick={submit} disabled={loading} className="btn-primary">
            {loading ? 'Saving…' : 'Change password'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChangePasswordModal;
