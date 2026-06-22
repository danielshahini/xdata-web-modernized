import React, { useState, useRef, useEffect } from 'react';
import { Info } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface InfoTipProps {
  content: React.ReactNode;
  title?: string;
}

const InfoTip: React.FC<InfoTipProps> = ({ content, title }) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="relative inline-block ml-2 align-middle" ref={containerRef}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="text-gray-400 hover:text-brand-500 transition-colors focus:outline-none"
      >
        <Info size={16} />
      </button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 10 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 10 }}
            className="absolute z-50 w-72 p-4 mt-2 -right-4 sm:right-auto sm:left-0 bg-white dark:bg-ink-card rounded-2xl shadow-2xl border border-slate-200 dark:border-ink-border pointer-events-auto"
          >
            {title && (
              <h4 className="text-sm font-bold text-gray-800 dark:text-white mb-2 uppercase tracking-tight">
                {title}
              </h4>
            )}
            <div className="text-xs text-gray-500 dark:text-gray-400 leading-relaxed">
              {content}
            </div>
            <div className="absolute top-0 right-4 sm:right-auto sm:left-4 -mt-2 w-4 h-4 bg-white dark:bg-ink-card border-l border-t border-slate-200 dark:border-ink-border transform rotate-45" />
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default InfoTip;
