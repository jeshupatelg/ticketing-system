import React, { useState, useEffect } from 'react';
import { X, Upload, Check } from 'lucide-react';
import { api, resolveUrl } from '../api';
import { PredefinedPhoto, Project } from '../types';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  onProjectCreated: (project: Project) => void;
}

export const CreateProjectModal: React.FC<Props> = ({ isOpen, onClose, onProjectCreated }) => {
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [selectedPhoto, setSelectedPhoto] = useState('');
  const [photos, setPhotos] = useState<PredefinedPhoto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      api.getPhotos('PROJECT').then(data => {
        setPhotos(data);
        if (data.length > 0 && !selectedPhoto) {
          setSelectedPhoto(data[0].url);
        }
      }).catch(console.error);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const isCodeValid = /^[A-Za-z0-9]{3}$/.test(code.trim());
  const isNameValid = name.trim().length > 0;
  const canSave = isCodeValid && isNameValid && !loading;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSave) return;

    setLoading(true);
    setError(null);
    try {
      const created = await api.createProject({
        code: code.trim().toUpperCase(),
        name: name.trim(),
        description: description.trim() || undefined,
        photoUrl: selectedPhoto || undefined,
      });
      onProjectCreated(created);
      onClose();
      setCode('');
      setName('');
      setDescription('');
    } catch (err: any) {
      setError(err.message || 'Failed to create project');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    const file = e.target.files[0];
    try {
      const uploaded = await api.uploadPhoto(file, `${code || 'PRJ'} Badge`, 'PROJECT');
      setPhotos(prev => [uploaded, ...prev]);
      setSelectedPhoto(uploaded.url);
    } catch (err: any) {
      setError('Failed to upload photo: ' + err.message);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="bg-theme-surface border border-theme-border rounded-xl shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between px-6 py-4 border-b border-theme-border bg-theme-surfaceHover/50">
          <h2 className="text-lg font-semibold text-theme-text">Create Project</h2>
          <button onClick={onClose} className="p-1 rounded-lg text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 text-sm text-red-400 bg-red-950/40 border border-red-800/60 rounded-lg">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
              Project Code (3 characters) *
            </label>
            <input
              type="text"
              maxLength={3}
              value={code}
              onChange={e => setCode(e.target.value.toUpperCase())}
              placeholder="e.g. PR1, DEV, OPS"
              className={`w-full px-3 py-2 bg-theme-bg border rounded-lg text-theme-text placeholder-theme-muted focus:outline-none focus:ring-2 uppercase tracking-widest font-mono text-center text-lg font-bold ${
                code && !isCodeValid ? 'border-red-500 focus:ring-red-500' : 'border-theme-border focus:ring-theme-primary'
              }`}
              required
            />
            <p className="text-xs text-theme-muted mt-1">Must be exactly 3 alphanumeric characters.</p>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
              Project Name *
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Infrastructure Core"
              className="w-full px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-theme-text placeholder-theme-muted focus:outline-none focus:ring-2 focus:ring-theme-primary"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
              Description
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Project goals, scope, and operational context..."
              className="w-full px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-theme-text placeholder-theme-muted focus:outline-none focus:ring-2 focus:ring-theme-primary text-sm resize-none"
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-theme-muted">
                Project Icon / Photo
              </label>
              <label className="text-xs text-theme-primary hover:underline cursor-pointer flex items-center gap-1">
                <Upload className="w-3.5 h-3.5" />
                Upload New
                <input type="file" accept="image/*" onChange={handleFileUpload} className="hidden" />
              </label>
            </div>
            <div className="grid grid-cols-4 gap-2 max-h-32 overflow-y-auto p-1 bg-theme-bg rounded-lg border border-theme-border">
              {photos.map(p => (
                <button
                  type="button"
                  key={p.id}
                  onClick={() => setSelectedPhoto(p.url)}
                  className={`relative p-2 rounded-lg flex flex-col items-center justify-center transition-all ${
                    selectedPhoto === p.url ? 'ring-2 ring-theme-primary bg-theme-surfaceHover' : 'hover:bg-theme-surfaceHover/50'
                  }`}
                >
                  <img src={resolveUrl(p.url)} alt={p.name} className="w-8 h-8 rounded object-cover" />
                  <span className="text-[10px] text-theme-muted truncate w-full text-center mt-1">{p.name}</span>
                  {selectedPhoto === p.url && (
                    <div className="absolute top-1 right-1 bg-theme-primary rounded-full p-0.5 text-white">
                      <Check className="w-2.5 h-2.5" />
                    </div>
                  )}
                </button>
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-theme-border">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium rounded-lg text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!canSave}
              className={`px-4 py-2 text-sm font-medium rounded-lg text-white transition-all ${
                canSave
                  ? 'bg-theme-primary hover:bg-theme-primaryHover shadow-lg shadow-theme-primary/20'
                  : 'bg-theme-border text-theme-muted cursor-not-allowed opacity-50'
              }`}
            >
              {loading ? 'Creating...' : 'Create Project'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
