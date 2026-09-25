import React, { useMemo } from 'react';
import {
  Layers,
  Sparkles,
  Plus,
  Clock,
  ArrowRight,
  Filter,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react';
import { TicketSummary, Project, ScopeType, FilterState } from '../types';
import { resolveUrl } from '../api';
import { TicketCard } from './TicketCard';

interface Props {
  project: Project;
  scope: ScopeType;
  tickets: TicketSummary[];
  filters: FilterState;
  onOpenTicket: (ticketId: string) => void;
  onOpenCreateTicket: () => void;
  onPromoteTicket: (ticketId: string) => void;
  onCancelPlanTicket: (ticketId: string) => void;
}

export const KanbanBoard: React.FC<Props> = ({
  project,
  scope,
  tickets,
  filters,
  onOpenTicket,
  onOpenCreateTicket,
  onPromoteTicket,
  onCancelPlanTicket,
}) => {
  const isFilterActive = Boolean(
    filters.search ||
    filters.assignee ||
    filters.phase ||
    filters.tag ||
    filters.dateRange
  );

  // Filter tickets according to FilterState
  const filteredTickets = useMemo(() => {
    return tickets.filter(t => {
      // Search
      if (filters.search) {
        const q = filters.search.toLowerCase();
        const matchesId = t.id.toLowerCase().includes(q);
        const matchesTitle = t.title.toLowerCase().includes(q);
        const matchesDesc = t.description?.toLowerCase().includes(q);
        if (!matchesId && !matchesTitle && !matchesDesc) return false;
      }

      // Assignee
      if (filters.assignee) {
        if (filters.assignee === 'unassigned') {
          if (t.assignee) return false;
        } else if (t.assignee !== filters.assignee) {
          return false;
        }
      }

      // Phase
      if (filters.phase && t.phase !== filters.phase) {
        return false;
      }

      // Tag
      if (filters.tag && !t.tags.includes(filters.tag)) {
        return false;
      }

      // Date Range (for completed tickets)
      if (filters.dateRange && t.completedAt) {
        const completedTime = new Date(t.completedAt).getTime();
        const now = Date.now();
        if (filters.dateRange === '1week') {
          const oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000;
          if (completedTime < oneWeekAgo) return false;
        } else if (filters.dateRange === '1month') {
          const oneMonthAgo = now - 30 * 24 * 60 * 60 * 1000;
          if (completedTime < oneMonthAgo) return false;
        }
      }

      return true;
    });
  }, [tickets, filters]);

  // When filter is applied, at most 5 or 6 tickets can only be shown per lane
  const MAX_PER_LANE_FILTERED = 6;

  // Split into 3 lanes for Live scope
  const plannedLane = useMemo(() => {
    const list = filteredTickets.filter(t => t.phase === 'PLANNED');
    if (isFilterActive) {
      return { items: list.slice(0, MAX_PER_LANE_FILTERED), total: list.length };
    }
    return { items: list, total: list.length };
  }, [filteredTickets, isFilterActive]);

  const executionLane = useMemo(() => {
    const list = filteredTickets.filter(t => t.phase === 'EXECUTION');
    if (isFilterActive) {
      return { items: list.slice(0, MAX_PER_LANE_FILTERED), total: list.length };
    }
    return { items: list, total: list.length };
  }, [filteredTickets, isFilterActive]);

  const closedLane = useMemo(() => {
    const list = filteredTickets.filter(t => t.phase === 'CLOSED');
    if (isFilterActive) {
      return { items: list.slice(0, MAX_PER_LANE_FILTERED), total: list.length };
    }
    return { items: list, total: list.length };
  }, [filteredTickets, isFilterActive]);

  return (
    <div className="flex-1 flex flex-col overflow-hidden">
      {/* Scope Header Banner */}
      <div className="flex items-center justify-between pb-3 mb-2 border-b border-theme-border">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-theme-surface border border-theme-border flex items-center justify-center overflow-hidden">
            <img
              src={resolveUrl(project.photoUrl, '/api/photos/default/project-1.svg')}
              alt=""
              className="w-full h-full object-cover"
            />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-xs font-bold text-theme-primary px-1.5 py-0.5 rounded bg-theme-primary/10">
                {project.code}
              </span>
              <h2 className="text-base font-bold text-theme-text">{project.name}</h2>
            </div>
            <p className="text-xs text-theme-muted">
              {scope === 'LIVE'
                ? 'Live Scope • 3 Phase Lanes (Planned, Execution, Closed)'
                : 'Plan Scope • Exploration & Ideas Backlog (No Lanes)'}
            </p>
          </div>
        </div>

        <button
          onClick={onOpenCreateTicket}
          className="px-3 py-1.5 bg-theme-primary hover:bg-theme-primaryHover text-white text-xs font-semibold rounded-lg shadow-sm flex items-center gap-1.5 transition-colors"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>New Ticket</span>
        </button>
      </div>

      {/* Main Board Area */}
      {scope === 'LIVE' ? (
        /* LIVE SCOPE: Exactly 3 lanes */
        <div className="flex-1 grid grid-cols-1 md:grid-cols-3 gap-4 overflow-hidden pt-1">
          {/* Lane 1: PLANNED */}
          <div className="flex flex-col bg-theme-surface/60 rounded-xl border border-theme-border overflow-hidden">
            {/* Lane Header */}
            <div className="px-4 py-3 border-b border-theme-border flex items-center justify-between bg-slate-900/40">
              <div className="flex items-center gap-2">
                <div className="w-2.5 h-2.5 rounded-full bg-blue-500 ring-2 ring-blue-500/20" />
                <h3 className="font-semibold text-xs text-theme-text uppercase tracking-wider">Planned</h3>
              </div>
              <div className="flex items-center gap-1.5 font-mono text-xs">
                <span className="px-2 py-0.5 rounded-full bg-theme-bg text-theme-text font-bold">
                  {plannedLane.total}
                </span>
                {isFilterActive && plannedLane.total > MAX_PER_LANE_FILTERED && (
                  <span className="text-[10px] text-theme-muted">(Cap: 6)</span>
                )}
              </div>
            </div>

            {/* Lane Cards Container */}
            <div className="flex-1 p-3 overflow-y-auto space-y-3">
              {plannedLane.items.map(ticket => (
                <TicketCard
                  key={ticket.id}
                  ticket={ticket}
                  onClick={() => onOpenTicket(ticket.id)}
                />
              ))}
              {plannedLane.items.length === 0 && (
                <div className="h-32 flex flex-col items-center justify-center text-xs text-theme-muted border border-dashed border-theme-border/70 rounded-xl">
                  <span>No tickets in Planned</span>
                </div>
              )}
            </div>
          </div>

          {/* Lane 2: EXECUTION */}
          <div className="flex flex-col bg-theme-surface/60 rounded-xl border border-theme-border overflow-hidden">
            {/* Lane Header */}
            <div className="px-4 py-3 border-b border-theme-border flex items-center justify-between bg-indigo-950/30">
              <div className="flex items-center gap-2">
                <div className="w-2.5 h-2.5 rounded-full bg-indigo-500 ring-2 ring-indigo-500/20" />
                <h3 className="font-semibold text-xs text-theme-text uppercase tracking-wider">Execution</h3>
              </div>
              <div className="flex items-center gap-1.5 font-mono text-xs">
                <span className="px-2 py-0.5 rounded-full bg-theme-bg text-theme-text font-bold">
                  {executionLane.total}
                </span>
                {isFilterActive && executionLane.total > MAX_PER_LANE_FILTERED && (
                  <span className="text-[10px] text-theme-muted">(Cap: 6)</span>
                )}
              </div>
            </div>

            {/* Lane Cards Container */}
            <div className="flex-1 p-3 overflow-y-auto space-y-3">
              {executionLane.items.map(ticket => (
                <TicketCard
                  key={ticket.id}
                  ticket={ticket}
                  onClick={() => onOpenTicket(ticket.id)}
                />
              ))}
              {executionLane.items.length === 0 && (
                <div className="h-32 flex flex-col items-center justify-center text-xs text-theme-muted border border-dashed border-theme-border/70 rounded-xl">
                  <span>No tickets in Execution</span>
                </div>
              )}
            </div>
          </div>

          {/* Lane 3: CLOSED (Completed & Cancelled) */}
          <div className="flex flex-col bg-theme-surface/60 rounded-xl border border-theme-border overflow-hidden">
            {/* Lane Header */}
            <div className="px-4 py-3 border-b border-theme-border flex items-center justify-between bg-emerald-950/20">
              <div className="flex items-center gap-2">
                <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 ring-2 ring-emerald-500/20" />
                <h3 className="font-semibold text-xs text-theme-text uppercase tracking-wider">Closed</h3>
              </div>
              <div className="flex items-center gap-1.5 font-mono text-xs">
                <span className="px-2 py-0.5 rounded-full bg-theme-bg text-theme-text font-bold">
                  {closedLane.total}
                </span>
                {isFilterActive && closedLane.total > MAX_PER_LANE_FILTERED && (
                  <span className="text-[10px] text-theme-muted">(Cap: 6)</span>
                )}
              </div>
            </div>

            {/* Lane Cards Container */}
            <div className="flex-1 p-3 overflow-y-auto space-y-3">
              {closedLane.items.map(ticket => (
                <TicketCard
                  key={ticket.id}
                  ticket={ticket}
                  onClick={() => onOpenTicket(ticket.id)}
                />
              ))}
              {closedLane.items.length === 0 && (
                <div className="h-32 flex flex-col items-center justify-center text-xs text-theme-muted border border-dashed border-theme-border/70 rounded-xl">
                  <span>No tickets in Closed</span>
                </div>
              )}
            </div>
          </div>
        </div>
      ) : (
        /* PLAN SCOPE: No lanes! Clean card grid/list */
        <div className="flex-1 overflow-y-auto pt-2">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredTickets.map(ticket => (
              <TicketCard
                key={ticket.id}
                ticket={ticket}
                onClick={() => onOpenTicket(ticket.id)}
                onQuickPromote={e => {
                  e.stopPropagation();
                  onPromoteTicket(ticket.id);
                }}
                onQuickCancel={e => {
                  e.stopPropagation();
                  onCancelPlanTicket(ticket.id);
                }}
              />
            ))}
          </div>

          {filteredTickets.length === 0 && (
            <div className="py-16 text-center space-y-3">
              <Sparkles className="w-8 h-8 text-amber-400 mx-auto" />
              <h3 className="text-sm font-semibold text-theme-text">No tickets in Plan Scope</h3>
              <p className="text-xs text-theme-muted max-w-sm mx-auto">
                All tickets start here for brainstorming ideas before promotion to Live.
              </p>
              <button
                onClick={onOpenCreateTicket}
                className="px-3.5 py-1.5 bg-theme-primary text-white text-xs font-semibold rounded-lg shadow-sm inline-flex items-center gap-1.5"
              >
                <Plus className="w-3.5 h-3.5" /> Create Plan Ticket
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
