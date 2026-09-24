import React, { useState } from 'react';
import { X, Plus, Trash2 } from 'lucide-react';
import { api } from '../api';
import { Project, PriorityType, TicketDetail } from '../types';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  projects: Project[];
  activeProjectCode: string | null;
  onTicketCreated: (ticket: TicketDetail) => void;
}

export const CreateTicketModal: React.FC<Props> = ({
  isOpen,
  onClose,
  projects,
  activeProjectCode,
  onTicketCreated,
}) => {
  const [projectCode, setProjectCode] = useState(activeProjectCode || (projects[0]?.code ?? 'ADH'));
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<PriorityType>('MEDIUM');
  const [tagsInput, setTagsInput] = useState('');
  const [ideas, setIdeas] = useState<string[]>(['Initial assessment']);
  const [newIdeaText, setNewIdeaText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  React.useEffect(() => {
    if (activeProjectCode) {
      setProjectCode(activeProjectCode);
    } else if (projects.length > 0) {
      setProjectCode(projects[0].code);
    }
  }, [activeProjectCode, projects]);

  if (!isOpen) return null;

  const isTitleValid = title.trim().length > 0;
  const isProjectValid = !!projectCode;
  const canSave = isTitleValid && isProjectValid && !loading;

  const handleAddIdea = () => {
    if (newIdeaText.trim()) {
      setIdeas([...ideas, newIdeaText.trim()]);
      setNewIdeaText('');
    }
  };

  const handleRemoveIdea = (index: number) => {
    setIdeas(ideas.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSave) return;

    setLoading(true);
    setError(null);

    const parsedTags = tagsInput
      .split(',')
      .map(t => t.trim())
      .filter(Boolean);

    try {
      const created = await api.createTicket({
        projectCode,
        title: title.trim(),
        description: description.trim() || undefined,
        priority,
        tags: parsedTags,
        ideas: ideas.filter(Boolean),
      });

      onTicketCreated(created);
      onClose();
      setTitle('');
      setDescription('');
      setTagsInput('');
      setIdeas(['Initial assessment']);
    } catch (err: any) {
      setError(err.message || 'Failed to create ticket');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="bg-theme-surface border border-theme-border rounded-xl shadow-2xl w-full max-w-lg overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between px-6 py-4 border-b border-theme-border bg-theme-surfaceHover/50">
          <div>
            <h2 className="text-lg font-semibold text-theme-text">Create Ticket</h2>
            <p className="text-xs text-theme-muted">All tickets begin in Plan scope with ideas</p>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto">
          {error && (
            <div className="p-3 text-sm text-red-400 bg-red-950/40 border border-red-800/60 rounded-lg">
              {error}
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
                Project *
              </label>
              <select
                value={projectCode}
                onChange={e => setProjectCode(e.target.value)}
                className="w-full px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-theme-text focus:outline-none focus:ring-2 focus:ring-theme-primary text-sm font-medium"
                required
              >
                {projects.map(p => (
                  <option key={p.code} value={p.code}>
                    [{p.code}] {p.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
                Priority
              </label>
              <select
                value={priority}
                onChange={e => setPriority(e.target.value as PriorityType)}
                className="w-full px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-theme-text focus:outline-none focus:ring-2 focus:ring-theme-primary text-sm font-medium"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
              Title *
            </label>
            <input
              type="text"
              value={title}
              onChange={e => setTitle(e.target.value)}
              placeholder="e.g. Implement OAuth2 Refresh Token Rotation"
              className="w-full px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-theme-text placeholder-theme-muted focus:outline-none focus:ring-2 focus:ring-theme-primary text-sm"
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
              placeholder="Problem statement, background, acceptance criteria..."
              className="w-full px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-theme-text placeholder-theme-muted focus:outline-none focus:ring-2 focus:ring-theme-primary text-sm resize-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
              Tags (comma-separated)
            </label>
            <input
              type="text"
              value={tagsInput}
              onChange={e => setTagsInput(e.target.value)}
              placeholder="backend, security, v1.2"
              className="w-full px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-theme-text placeholder-theme-muted focus:outline-none focus:ring-2 focus:ring-theme-primary text-sm"
            />
          </div>

          {/* Initial Plan Ideas */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-theme-muted mb-1">
              Initial Ideas (Become Checkpoints on Live Promotion)
            </label>
            <div className="space-y-2 mb-2">
              {ideas.map((idea, idx) => (
                <div key={idx} className="flex items-center gap-2 bg-theme-bg p-2 rounded-lg border border-theme-border text-sm">
                  <span className="text-theme-muted text-xs font-mono w-4">{idx + 1}.</span>
                  <span className="flex-1 text-theme-text">{idea}</span>
                  <button
                    type="button"
                    onClick={() => handleRemoveIdea(idx)}
                    className="text-theme-muted hover:text-red-400 p-1 rounded"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <input
                type="text"
                value={newIdeaText}
                onChange={e => setNewIdeaText(e.target.value)}
                onKeyDown={e => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddIdea();
                  }
                }}
                placeholder="Add an exploratory idea..."
                className="flex-1 px-3 py-1.5 bg-theme-bg border border-theme-border rounded-lg text-theme-text text-xs placeholder-theme-muted focus:outline-none focus:ring-1 focus:ring-theme-primary"
              />
              <button
                type="button"
                onClick={handleAddIdea}
                className="px-3 py-1.5 bg-theme-surfaceHover text-theme-text border border-theme-border rounded-lg text-xs font-medium hover:bg-theme-border flex items-center gap-1"
              >
                <Plus className="w-3.5 h-3.5" /> Add
              </button>
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
              {loading ? 'Creating...' : 'Create Ticket'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
