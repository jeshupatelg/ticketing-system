import React, { useState, useEffect } from 'react';
import {
  X,
  CheckSquare,
  Square,
  MessageSquare,
  Paperclip,
  Share2,
  Trash2,
  Plus,
  Upload,
  Download,
  AlertCircle,
  Clock,
  User as UserIcon,
  Sparkles,
  ArrowRight,
  XCircle,
  CheckCircle2,
  Lock,
} from 'lucide-react';
import { api, resolveUrl } from '../api';
import {
  TicketDetail,
  TicketIdea,
  TicketCheckpoint,
  TicketComment,
  TicketAttachment,
  PhaseType,
  User,
} from '../types';

interface Props {
  ticketId: string | null;
  isOpen: boolean;
  onClose: () => void;
  onTicketUpdated: () => void;
  users: User[];
  onOpenRelatedTicket: (relatedId: string) => void;
}

export const TicketModal: React.FC<Props> = ({
  ticketId,
  isOpen,
  onClose,
  onTicketUpdated,
  users,
  onOpenRelatedTicket,
}) => {
  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form states
  const [newComment, setNewComment] = useState('');
  const [newIdeaText, setNewIdeaText] = useState('');
  const [relatedTicketInput, setRelatedTicketInput] = useState('');
  const [assigneePromptOpen, setAssigneePromptOpen] = useState(false);
  const [selectedAssigneeForExec, setSelectedAssigneeForExec] = useState('');
  const [uploadingAttachment, setUploadingAttachment] = useState(false);
  const [reassignDropdownOpen, setReassignDropdownOpen] = useState(false);

  useEffect(() => {
    if (isOpen && ticketId) {
      loadTicket();
    } else {
      setTicket(null);
      setError(null);
    }
  }, [isOpen, ticketId]);

  const loadTicket = async () => {
    if (!ticketId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await api.getTicket(ticketId);
      setTicket(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load ticket');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  // Color background banner for Phase (matching lane view, and RED for cancelled)
  const getPhaseBadgeStyle = () => {
    if (!ticket) return 'bg-slate-700 text-white';

    if (ticket.scope === 'PLAN') {
      if (ticket.phase === 'CLOSED') {
        return 'bg-red-700 text-white';
      }
      return 'bg-amber-600 text-white';
    }

    if (ticket.phase === 'PLANNED') {
      return 'bg-blue-600 text-white';
    }
    if (ticket.phase === 'EXECUTION') {
      return 'bg-indigo-600 text-white';
    }
    if (ticket.phase === 'CLOSED') {
      if (!ticket.completed) {
        // CRITICAL REQUIREMENT: Reserve red color for closed but incomplete i.e. cancelled tickets
        return 'bg-red-600 text-white';
      }
      return 'bg-emerald-600 text-white';
    }
    return 'bg-slate-700 text-white';
  };

  const getPhaseDisplayText = () => {
    if (!ticket) return '';
    if (ticket.scope === 'PLAN') {
      return ticket.phase === 'CLOSED' ? 'PLAN (CANCELLED)' : 'PLAN (IDEATION)';
    }
    if (ticket.phase === 'CLOSED') {
      return ticket.completed ? 'CLOSED (COMPLETED)' : 'CLOSED (CANCELLED / INCOMPLETE)';
    }
    return ticket.phase;
  };

  // Phase Transition Handlers
  const handleTransitionToExecution = async () => {
    if (!ticket) return;
    if (!ticket.assignee) {
      // First move from Planned to Execution when none assigned, prompts assignment necessarily!
      setSelectedAssigneeForExec(users[0]?.username || '');
      setAssigneePromptOpen(true);
      return;
    }

    try {
      await api.transitionPhase(ticket.id, 'EXECUTION');
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleConfirmExecutionWithAssignee = async () => {
    if (!ticket || !selectedAssigneeForExec) return;
    try {
      await api.transitionPhase(ticket.id, 'EXECUTION', selectedAssigneeForExec);
      setAssigneePromptOpen(false);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleTransitionPhase = async (targetPhase: PhaseType, completed = true) => {
    if (!ticket) return;
    try {
      await api.transitionPhase(ticket.id, targetPhase, undefined, completed);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleReassign = async (newAssignee: string) => {
    if (!ticket || !newAssignee) return;
    try {
      await api.reassignTicket(ticket.id, newAssignee);
      setReassignDropdownOpen(false);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handlePromote = async () => {
    if (!ticket) return;
    try {
      await api.promoteTicket(ticket.id);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleCancelPlan = async () => {
    if (!ticket) return;
    try {
      await api.cancelPlanTicket(ticket.id);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Idea handlers (Plan scope)
  const handleAddIdea = async () => {
    if (!ticket || !newIdeaText.trim()) return;
    try {
      await api.addIdea(ticket.id, newIdeaText.trim(), true);
      setNewIdeaText('');
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleToggleIdeaActive = async (idea: TicketIdea) => {
    if (!ticket) return;
    try {
      await api.updateIdea(ticket.id, idea.id, idea.content, !idea.active);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleDeleteIdea = async (ideaId: number) => {
    if (!ticket) return;
    try {
      await api.deleteIdea(ticket.id, ideaId);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Checkpoint handlers (Live scope)
  const handleToggleCheckpoint = async (cp: TicketCheckpoint) => {
    if (!ticket) return;
    if (ticket.phase !== 'EXECUTION') {
      return; // Checkpoints are immutable in Planned and Closed
    }
    try {
      await api.toggleCheckpoint(ticket.id, cp.id, !cp.completed);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Comment handler (Mutable in all phases!)
  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ticket || !newComment.trim()) return;
    try {
      await api.addComment(ticket.id, newComment.trim());
      setNewComment('');
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Related Tickets
  const handleAddRelatedTicket = async () => {
    if (!ticket || !relatedTicketInput.trim()) return;
    try {
      await api.linkRelatedTicket(ticket.id, relatedTicketInput.trim().toUpperCase());
      setRelatedTicketInput('');
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleRemoveRelatedTicket = async (relId: string) => {
    if (!ticket) return;
    try {
      await api.unlinkRelatedTicket(ticket.id, relId);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Attachments
  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!ticket || !e.target.files || e.target.files.length === 0) return;
    const file = e.target.files[0];
    setUploadingAttachment(true);
    try {
      await api.uploadAttachment(ticket.id, file);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setUploadingAttachment(false);
    }
  };

  const handleDeleteAttachment = async (attId: number) => {
    try {
      await api.deleteAttachment(attId);
      await loadTicket();
      onTicketUpdated();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const isClosed = ticket?.phase === 'CLOSED';
  const isPlanned = ticket?.phase === 'PLANNED';
  const isExecution = ticket?.phase === 'EXECUTION';
  const isPlanScope = ticket?.scope === 'PLAN';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md overflow-hidden">
      <div className="bg-theme-surface border border-theme-border rounded-2xl shadow-2xl w-full max-w-4xl max-h-[92vh] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header Bar */}
        <div className="px-6 py-4 border-b border-theme-border flex items-center justify-between bg-theme-surfaceHover/40">
          <div className="flex items-center gap-3">
            <span className="font-mono text-base font-bold text-theme-text tracking-wider">
              {ticket?.id || ticketId}
            </span>
            <span className="text-xs text-theme-muted font-semibold uppercase px-2 py-0.5 rounded bg-theme-bg border border-theme-border">
              [{ticket?.projectCode}]
            </span>

            {/* Phase Written Over Color Background from Lane View */}
            {ticket && (
              <div className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider shadow-sm flex items-center gap-1.5 ${getPhaseBadgeStyle()}`}>
                {ticket.phase === 'CLOSED' && (
                  ticket.completed ? <CheckCircle2 className="w-3.5 h-3.5" /> : <XCircle className="w-3.5 h-3.5" />
                )}
                <span>{getPhaseDisplayText()}</span>
              </div>
            )}
          </div>

          <div className="flex items-center gap-2">
            {isClosed && (
              <span className="text-xs text-theme-muted flex items-center gap-1 px-2 py-1 bg-theme-bg rounded border border-theme-border">
                <Lock className="w-3.5 h-3.5" /> Ticket is Closed & Immutable
              </span>
            )}
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {error && (
          <div className="mx-6 mt-4 p-3 text-xs text-red-400 bg-red-950/40 border border-red-800/60 rounded-lg flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
            <button onClick={() => setError(null)} className="text-red-400 hover:text-red-300">
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        )}

        {/* Modal Body */}
        {loading && !ticket ? (
          <div className="p-12 text-center text-theme-muted text-sm">Loading ticket details...</div>
        ) : ticket ? (
          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            {/* Title and Description */}
            <div>
              <h1 className="text-xl font-bold text-theme-text mb-2 leading-snug">{ticket.title}</h1>
              <div className="p-3 bg-theme-bg/60 rounded-xl border border-theme-border/60 text-sm text-theme-text/90 whitespace-pre-wrap">
                {ticket.description || <span className="text-theme-muted italic">No description provided.</span>}
              </div>
            </div>

            {/* Metadata Bar & Phase Transition Actions */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 bg-theme-bg rounded-xl border border-theme-border">
              {/* Left Meta: Priority, Reporter, Assignee */}
              <div className="space-y-2 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-theme-muted uppercase font-semibold">Priority:</span>
                  <span className="font-bold text-theme-text">{ticket.priority}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-theme-muted uppercase font-semibold">Reporter:</span>
                  <span className="text-theme-text">{ticket.reporterName || ticket.reporter}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-theme-muted uppercase font-semibold">Assignee:</span>
                  <div className="flex items-center gap-2">
                    {ticket.assignee ? (
                      <div className="flex items-center gap-1.5 font-medium text-theme-text">
                        <img
                          src={resolveUrl(ticket.assigneeAvatarUrl, '/api/photos/default/avatar-1.svg')}
                          alt=""
                          className="w-4 h-4 rounded-full object-cover"
                        />
                        <span>{ticket.assigneeName || ticket.assignee}</span>
                      </div>
                    ) : (
                      <span className="text-theme-muted italic">None (Unassigned)</span>
                    )}

                    {/* Reassign option in Execution phase */}
                    {isExecution && (
                      <div className="relative">
                        <button
                          onClick={() => setReassignDropdownOpen(!reassignDropdownOpen)}
                          className="text-[11px] text-theme-primary hover:underline font-semibold ml-1"
                        >
                          Reassign
                        </button>
                        {reassignDropdownOpen && (
                          <div className="absolute right-0 top-full mt-1 w-48 bg-theme-surface border border-theme-border rounded-lg shadow-xl p-1 z-30 max-h-48 overflow-y-auto">
                            <div className="px-2 py-1 text-[10px] uppercase font-bold text-theme-muted">
                              Select Assignee
                            </div>
                            {users.map(u => (
                              <button
                                key={u.username}
                                onClick={() => handleReassign(u.username)}
                                className="w-full text-left px-2 py-1.5 text-xs text-theme-text hover:bg-theme-surfaceHover rounded flex items-center gap-1.5"
                              >
                                <img src={resolveUrl(u.avatarUrl, '/api/photos/default/avatar-1.svg')} alt="" className="w-4 h-4 rounded-full" />
                                <span className="truncate">{u.name}</span>
                              </button>
                            ))}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Right: Phase Controls */}
              <div className="flex flex-col justify-center space-y-2 border-t md:border-t-0 md:border-l md:pl-4 border-theme-border">
                <span className="text-[10px] font-bold uppercase tracking-wider text-theme-muted">
                  Workflow Phase Actions
                </span>

                {isPlanScope && !isClosed && (
                  <div className="flex gap-2">
                    <button
                      onClick={handleCancelPlan}
                      className="px-3 py-1.5 text-xs font-medium text-red-400 hover:bg-red-950/40 rounded-lg border border-red-900/50 transition-colors"
                    >
                      Cancel Ticket
                    </button>
                    <button
                      onClick={handlePromote}
                      className="flex-1 px-3 py-1.5 text-xs font-semibold text-white bg-theme-primary hover:bg-theme-primaryHover rounded-lg shadow-md flex items-center justify-center gap-1.5 transition-colors"
                    >
                      Promote to Live <ArrowRight className="w-3.5 h-3.5" />
                    </button>
                  </div>
                )}

                {ticket.scope === 'LIVE' && !isClosed && (
                  <div className="flex flex-wrap gap-2">
                    {isPlanned && (
                      <button
                        onClick={handleTransitionToExecution}
                        className="flex-1 px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-lg shadow-md flex items-center justify-center gap-1.5 transition-colors"
                      >
                        Start Execution <ArrowRight className="w-3.5 h-3.5" />
                      </button>
                    )}

                    {isExecution && (
                      <button
                        onClick={() => handleTransitionPhase('PLANNED')}
                        className="px-3 py-1.5 text-xs font-medium text-theme-text bg-theme-surfaceHover hover:bg-theme-border rounded-lg border border-theme-border transition-colors"
                      >
                        Move back to Planned
                      </button>
                    )}

                    <button
                      onClick={() => handleTransitionPhase('CLOSED', true)}
                      className="px-3 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-md transition-colors"
                    >
                      Close (Completed)
                    </button>

                    <button
                      onClick={() => handleTransitionPhase('CLOSED', false)}
                      className="px-3 py-1.5 text-xs font-medium text-red-400 hover:bg-red-950/40 rounded-lg border border-red-900/50 transition-colors"
                    >
                      Cancel Ticket
                    </button>
                  </div>
                )}

                {isClosed && (
                  <div className="text-xs text-theme-muted">
                    Completed at: {ticket.completedAt ? new Date(ticket.completedAt).toLocaleString() : 'N/A'}
                  </div>
                )}
              </div>
            </div>

            {/* Mandatory Assignee Prompt Modal when moving Planned -> Execution if unassigned */}
            {assigneePromptOpen && (
              <div className="p-4 bg-indigo-950/40 border-2 border-indigo-500/60 rounded-xl space-y-3 animate-in fade-in">
                <div className="flex items-center gap-2 text-indigo-300 font-bold text-sm">
                  <UserIcon className="w-4 h-4" />
                  <span>Assignee Required for Execution Phase</span>
                </div>
                <p className="text-xs text-theme-muted">
                  First move from Planned to Execution requires assigning a team member. Assignees cannot be de-assigned afterwards.
                </p>
                <div className="flex gap-2">
                  <select
                    value={selectedAssigneeForExec}
                    onChange={e => setSelectedAssigneeForExec(e.target.value)}
                    className="flex-1 px-3 py-1.5 bg-theme-surface border border-theme-border rounded-lg text-xs text-theme-text focus:outline-none focus:ring-1 focus:ring-theme-primary"
                  >
                    {users.map(u => (
                      <option key={u.username} value={u.username}>
                        {u.name} (@{u.username})
                      </option>
                    ))}
                  </select>
                  <button
                    onClick={handleConfirmExecutionWithAssignee}
                    className="px-4 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-lg shadow-md"
                  >
                    Confirm & Start Execution
                  </button>
                  <button
                    onClick={() => setAssigneePromptOpen(false)}
                    className="px-3 py-1.5 text-xs font-medium text-theme-muted hover:text-theme-text rounded-lg border border-theme-border"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}

            {/* Plan Scope: Ideas List */}
            {isPlanScope && (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Sparkles className="w-4 h-4 text-amber-400" />
                    <h3 className="text-sm font-bold uppercase tracking-wider text-theme-text">
                      Plan Ideas (Active ideas become Checkpoints on Live Promotion)
                    </h3>
                  </div>
                  <span className="text-xs text-theme-muted font-mono">
                    {ticket.ideas.filter(i => i.active).length} of {ticket.ideas.length} active
                  </span>
                </div>

                <div className="space-y-2">
                  {ticket.ideas.map((idea, idx) => (
                    <div
                      key={idea.id}
                      className={`flex items-center justify-between p-3 rounded-lg border transition-all ${
                        idea.active
                          ? 'bg-theme-bg border-theme-border text-theme-text'
                          : 'bg-theme-bg/40 border-theme-border/40 text-theme-muted line-through'
                      }`}
                    >
                      <div className="flex items-center gap-3 flex-1">
                        <button
                          disabled={isClosed}
                          onClick={() => handleToggleIdeaActive(idea)}
                          className="text-theme-muted hover:text-theme-text disabled:opacity-50"
                        >
                          {idea.active ? (
                            <CheckSquare className="w-4 h-4 text-theme-primary" />
                          ) : (
                            <Square className="w-4 h-4 text-theme-muted" />
                          )}
                        </button>
                        <span className="text-sm font-medium">{idea.content}</span>
                      </div>
                      {!isClosed && (
                        <button
                          onClick={() => handleDeleteIdea(idea.id)}
                          className="text-theme-muted hover:text-red-400 p-1 rounded"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      )}
                    </div>
                  ))}
                  {ticket.ideas.length === 0 && (
                    <div className="p-4 text-center text-xs text-theme-muted border border-dashed border-theme-border rounded-lg">
                      No ideas recorded yet. Add ideas below.
                    </div>
                  )}
                </div>

                {!isClosed && (
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
                      className="flex-1 px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-xs text-theme-text placeholder-theme-muted focus:outline-none focus:ring-1 focus:ring-theme-primary"
                    />
                    <button
                      onClick={handleAddIdea}
                      className="px-4 py-2 bg-theme-surfaceHover text-theme-text border border-theme-border rounded-lg text-xs font-semibold hover:bg-theme-border flex items-center gap-1.5"
                    >
                      <Plus className="w-3.5 h-3.5" /> Add Idea
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* Live Scope: Checkpoints Checklist */}
            {ticket.scope === 'LIVE' && (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <CheckSquare className="w-4 h-4 text-theme-primary" />
                    <h3 className="text-sm font-bold uppercase tracking-wider text-theme-text">
                      Checkpoints ({ticket.completedCheckpoints}/{ticket.totalCheckpoints} Completed)
                    </h3>
                  </div>
                  {isPlanned && (
                    <span className="text-xs text-theme-muted italic">
                      Checkpoints immutable in Planned phase
                    </span>
                  )}
                </div>

                <div className="space-y-2">
                  {ticket.checkpoints.map(cp => (
                    <div
                      key={cp.id}
                      onClick={() => isExecution && handleToggleCheckpoint(cp)}
                      className={`flex items-center gap-3 p-3 rounded-lg border transition-all ${
                        isExecution ? 'cursor-pointer hover:bg-theme-surfaceHover' : 'cursor-default'
                      } ${
                        cp.completed
                          ? 'bg-emerald-950/20 border-emerald-800/40 text-emerald-200'
                          : 'bg-theme-bg border-theme-border text-theme-text'
                      }`}
                    >
                      <button
                        type="button"
                        disabled={!isExecution}
                        className="text-theme-muted flex-shrink-0 disabled:cursor-not-allowed"
                      >
                        {cp.completed ? (
                          <CheckSquare className="w-4 h-4 text-emerald-400" />
                        ) : (
                          <Square className="w-4 h-4 text-theme-muted" />
                        )}
                      </button>
                      <span className={`text-sm ${cp.completed ? 'line-through text-theme-muted' : 'font-medium'}`}>
                        {cp.title}
                      </span>
                    </div>
                  ))}
                  {ticket.checkpoints.length === 0 && (
                    <div className="p-4 text-center text-xs text-theme-muted border border-dashed border-theme-border rounded-lg">
                      No checkpoints active for this ticket.
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Related Tickets Section */}
            <div className="space-y-3 pt-2 border-t border-theme-border">
              <div className="flex items-center gap-2">
                <Share2 className="w-4 h-4 text-theme-primary" />
                <h3 className="text-sm font-bold uppercase tracking-wider text-theme-text">Related Tickets</h3>
              </div>
              <div className="flex flex-wrap gap-2 items-center">
                {ticket.relatedTickets.map(relId => (
                  <div
                    key={relId}
                    className="flex items-center gap-1.5 px-2.5 py-1 bg-theme-bg border border-theme-border rounded-lg text-xs font-mono font-medium hover:border-theme-primary transition-colors"
                  >
                    <button
                      onClick={() => onOpenRelatedTicket(relId)}
                      className="text-theme-primary hover:underline font-bold"
                    >
                      {relId}
                    </button>
                    {!isClosed && (
                      <button
                        onClick={() => handleRemoveRelatedTicket(relId)}
                        className="text-theme-muted hover:text-red-400 ml-1"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    )}
                  </div>
                ))}
                {!isClosed && (
                  <div className="flex items-center gap-1.5">
                    <input
                      type="text"
                      value={relatedTicketInput}
                      onChange={e => setRelatedTicketInput(e.target.value.toUpperCase())}
                      placeholder="e.g. ADH-00002"
                      className="px-2.5 py-1 bg-theme-bg border border-theme-border rounded-lg text-xs font-mono text-theme-text uppercase w-32 focus:outline-none focus:ring-1 focus:ring-theme-primary"
                    />
                    <button
                      onClick={handleAddRelatedTicket}
                      className="px-2.5 py-1 bg-theme-surfaceHover text-theme-text border border-theme-border rounded-lg text-xs font-medium hover:bg-theme-border"
                    >
                      Link
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Attachments Section */}
            <div className="space-y-3 pt-2 border-t border-theme-border">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Paperclip className="w-4 h-4 text-theme-primary" />
                  <h3 className="text-sm font-bold uppercase tracking-wider text-theme-text">
                    Attachments ({ticket.attachments.length})
                  </h3>
                </div>
                {!isClosed && (
                  <label className="px-2.5 py-1 text-xs font-medium text-theme-primary hover:bg-theme-primary/10 rounded-lg border border-theme-primary/40 cursor-pointer flex items-center gap-1.5">
                    <Upload className="w-3 h-3" />
                    <span>{uploadingAttachment ? 'Uploading...' : 'Upload File'}</span>
                    <input
                      type="file"
                      onChange={handleFileUpload}
                      disabled={uploadingAttachment}
                      className="hidden"
                    />
                  </label>
                )}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                {ticket.attachments.map(att => (
                  <div
                    key={att.id}
                    className="flex items-center justify-between p-2.5 bg-theme-bg border border-theme-border rounded-lg text-xs"
                  >
                    <div className="truncate flex-1 pr-2">
                      <div className="font-medium text-theme-text truncate">{att.fileName}</div>
                      <div className="text-[10px] text-theme-muted font-mono">
                        {(att.fileSize / 1024).toFixed(1)} KB • by @{att.uploadedBy}
                      </div>
                    </div>
                    <div className="flex items-center gap-1">
                      <a
                        href={api.getAttachmentDownloadUrl(att.id)}
                        target="_blank"
                        rel="noreferrer"
                        className="p-1 text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover rounded"
                        title="Download"
                      >
                        <Download className="w-3.5 h-3.5" />
                      </a>
                      {!isClosed && (
                        <button
                          onClick={() => handleDeleteAttachment(att.id)}
                          className="p-1 text-theme-muted hover:text-red-400 hover:bg-theme-surfaceHover rounded"
                          title="Delete"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
                {ticket.attachments.length === 0 && (
                  <div className="col-span-2 p-3 text-center text-xs text-theme-muted border border-dashed border-theme-border rounded-lg">
                    No attachments uploaded. Configurable limits apply.
                  </div>
                )}
              </div>
            </div>

            {/* Comments Section (Mutable for all phases!) */}
            <div className="space-y-3 pt-2 border-t border-theme-border">
              <div className="flex items-center gap-2">
                <MessageSquare className="w-4 h-4 text-theme-primary" />
                <h3 className="text-sm font-bold uppercase tracking-wider text-theme-text">
                  Comments ({ticket.comments.length})
                </h3>
              </div>

              <div className="space-y-3 max-h-60 overflow-y-auto">
                {ticket.comments.map(c => (
                  <div key={c.id} className="p-3 bg-theme-bg border border-theme-border rounded-xl space-y-1.5">
                    <div className="flex items-center justify-between text-xs">
                      <div className="flex items-center gap-2">
                        <img
                          src={resolveUrl(c.authorAvatarUrl, '/api/photos/default/avatar-1.svg')}
                          alt=""
                          className="w-5 h-5 rounded-full object-cover"
                        />
                        <span className="font-semibold text-theme-text">{c.author}</span>
                      </div>
                      <span className="text-[10px] text-theme-muted">
                        {new Date(c.createdAt).toLocaleString()}
                      </span>
                    </div>
                    <p className="text-xs text-theme-text/90 whitespace-pre-wrap pl-7">{c.content}</p>
                  </div>
                ))}
                {ticket.comments.length === 0 && (
                  <div className="p-3 text-center text-xs text-theme-muted border border-dashed border-theme-border rounded-lg">
                    No comments yet. Comments can be added in all phases.
                  </div>
                )}
              </div>

              {/* Add Comment Form */}
              <form onSubmit={handleAddComment} className="flex gap-2">
                <input
                  type="text"
                  value={newComment}
                  onChange={e => setNewComment(e.target.value)}
                  placeholder="Write a comment..."
                  className="flex-1 px-3 py-2 bg-theme-bg border border-theme-border rounded-lg text-xs text-theme-text placeholder-theme-muted focus:outline-none focus:ring-1 focus:ring-theme-primary"
                />
                <button
                  type="submit"
                  disabled={!newComment.trim()}
                  className="px-4 py-2 bg-theme-primary text-white rounded-lg text-xs font-semibold hover:bg-theme-primaryHover disabled:opacity-50 transition-colors shadow-sm"
                >
                  Comment
                </button>
              </form>
            </div>
          </div>
        ) : null}

        {/* Modal Footer */}
        <div className="px-6 py-3 border-t border-theme-border bg-theme-surfaceHover/30 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 text-xs font-semibold text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover rounded-lg transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
