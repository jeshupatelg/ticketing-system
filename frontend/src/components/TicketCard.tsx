import React from 'react';
import {
  CheckSquare,
  MessageSquare,
  Paperclip,
  Sparkles,
  ArrowRight,
  XCircle,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';
import { TicketSummary, PriorityType } from '../types';
import { resolveUrl } from '../api';

interface Props {
  ticket: TicketSummary;
  onClick: () => void;
  onQuickPromote?: (e: React.MouseEvent) => void;
  onQuickCancel?: (e: React.MouseEvent) => void;
}

export const TicketCard: React.FC<Props> = ({
  ticket,
  onClick,
  onQuickPromote,
  onQuickCancel,
}) => {
  // Determine card style based on phase and completion
  const getCardStyle = () => {
    if (ticket.scope === 'PLAN') {
      if (ticket.phase === 'CLOSED') {
        // Cancelled plan ticket
        return 'bg-red-950/30 border-red-900/60 hover:border-red-500/80 shadow-red-950/20';
      }
      return 'bg-amber-950/20 border-amber-800/40 hover:border-amber-500/70 shadow-amber-950/20';
    }

    // Live scope cards
    if (ticket.phase === 'PLANNED') {
      return 'bg-slate-900/60 border-slate-700/60 hover:border-sky-500/70 shadow-slate-900/30';
    }
    if (ticket.phase === 'EXECUTION') {
      return 'bg-indigo-950/40 border-indigo-800/60 hover:border-indigo-400/70 shadow-indigo-950/30';
    }
    if (ticket.phase === 'CLOSED') {
      if (!ticket.completed) {
        // CRITICAL: Reserve red color for closed but incomplete i.e. cancelled tickets
        return 'bg-red-950/40 border-red-700/70 hover:border-red-500 text-red-100 shadow-red-950/30 ring-1 ring-red-500/20';
      }
      return 'bg-emerald-950/30 border-emerald-800/60 hover:border-emerald-400/70 shadow-emerald-950/20';
    }

    return 'bg-theme-surface border-theme-border hover:border-theme-primary';
  };

  const getPriorityBadge = (p: PriorityType) => {
    switch (p) {
      case 'URGENT':
        return <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-red-500/20 text-red-400 border border-red-500/30">URGENT</span>;
      case 'HIGH':
        return <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30">HIGH</span>;
      case 'MEDIUM':
        return <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30">MED</span>;
      case 'LOW':
        return <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-zinc-500/20 text-zinc-400 border border-zinc-500/30">LOW</span>;
    }
  };

  return (
    <div
      onClick={onClick}
      className={`rounded-xl border p-4 cursor-pointer transition-all duration-150 hover:shadow-lg hover:-translate-y-0.5 select-none relative group ${getCardStyle()}`}
    >
      {/* Top row: ID + Priority + Status tag */}
      <div className="flex items-center justify-between gap-2 mb-2">
        <span className="font-mono font-bold text-xs text-theme-text/80 tracking-wider">
          {ticket.id}
        </span>
        <div className="flex items-center gap-1.5">
          {ticket.phase === 'CLOSED' && (
            <span
              className={`text-[10px] font-bold px-1.5 py-0.5 rounded flex items-center gap-1 ${
                ticket.completed
                  ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                  : 'bg-red-500/30 text-red-300 border border-red-500/50'
              }`}
            >
              {ticket.completed ? <CheckCircle2 className="w-2.5 h-2.5" /> : <XCircle className="w-2.5 h-2.5" />}
              {ticket.completed ? 'Completed' : 'Cancelled'}
            </span>
          )}
          {getPriorityBadge(ticket.priority)}
        </div>
      </div>

      {/* Ticket Title */}
      <h3 className="font-medium text-sm text-theme-text line-clamp-2 mb-2 leading-snug">
        {ticket.title}
      </h3>

      {/* Tags */}
      {ticket.tags && ticket.tags.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-3">
          {ticket.tags.slice(0, 3).map((tag, idx) => (
            <span
              key={idx}
              className="text-[10px] px-1.5 py-0.5 rounded bg-theme-bg/80 text-theme-muted border border-theme-border/60"
            >
              #{tag}
            </span>
          ))}
          {ticket.tags.length > 3 && (
            <span className="text-[10px] text-theme-muted">+{ticket.tags.length - 3}</span>
          )}
        </div>
      )}

      {/* Progress & Bottom Metadata */}
      <div className="flex items-center justify-between pt-2 border-t border-theme-border/40 text-xs text-theme-muted">
        {/* Left: Checkpoints or Ideas count */}
        {ticket.scope === 'LIVE' ? (
          <div className="flex items-center gap-1.5 font-mono text-[11px]" title="Checkpoints Progress">
            <CheckSquare className="w-3.5 h-3.5 text-theme-primary" />
            <span>
              {ticket.completedCheckpoints}/{ticket.totalCheckpoints}
            </span>
          </div>
        ) : (
          <div className="flex items-center gap-1.5 font-mono text-[11px]" title="Active Ideas">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            <span>
              {ticket.activeIdeaCount}/{ticket.ideaCount} Ideas
            </span>
          </div>
        )}

        {/* Right: Comments, Attachments, Assignee */}
        <div className="flex items-center gap-3">
          {ticket.attachmentCount > 0 && (
            <div className="flex items-center gap-1 text-[11px]" title="Attachments">
              <Paperclip className="w-3 h-3" />
              <span>{ticket.attachmentCount}</span>
            </div>
          )}

          {ticket.commentCount > 0 && (
            <div className="flex items-center gap-1 text-[11px]" title="Comments">
              <MessageSquare className="w-3 h-3" />
              <span>{ticket.commentCount}</span>
            </div>
          )}

          {/* Assignee Avatar */}
          {ticket.assignee ? (
            <div
              className="flex items-center gap-1"
              title={`Assigned to: ${ticket.assigneeName || ticket.assignee}`}
            >
              <img
                src={resolveUrl(ticket.assigneeAvatarUrl, '/api/photos/default/avatar-1.svg')}
                alt=""
                className="w-5 h-5 rounded-full object-cover border border-theme-border"
              />
            </div>
          ) : (
            ticket.scope === 'LIVE' && (
              <span className="text-[10px] text-theme-muted/70 italic">Unassigned</span>
            )
          )}
        </div>
      </div>

      {/* Quick Action Buttons for Plan scope cards */}
      {ticket.scope === 'PLAN' && ticket.phase !== 'CLOSED' && (
        <div className="mt-3 pt-2 border-t border-theme-border/40 flex items-center justify-between gap-2" onClick={e => e.stopPropagation()}>
          <button
            onClick={onQuickCancel}
            className="px-2 py-1 text-[11px] font-medium text-red-400 hover:text-red-300 hover:bg-red-950/30 rounded border border-red-900/40 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={onQuickPromote}
            className="px-2.5 py-1 text-[11px] font-medium text-white bg-theme-primary hover:bg-theme-primaryHover rounded shadow-sm flex items-center gap-1 transition-colors"
          >
            Promote to Live <ArrowRight className="w-3 h-3" />
          </button>
        </div>
      )}
    </div>
  );
};
