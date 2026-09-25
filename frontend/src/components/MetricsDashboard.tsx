import React from 'react';
import {
  Layers,
  Sparkles,
  CheckCircle2,
  XCircle,
  Folder,
  ArrowRight,
  TrendingUp,
  Clock,
  Activity,
  Plus,
} from 'lucide-react';
import { Metrics, Project } from '../types';
import { resolveUrl } from '../api';

interface Props {
  metrics: Metrics | null;
  projects: Project[];
  onSelectProject: (project: Project) => void;
  onOpenCreateProject: () => void;
  onOpenCreateTicket: () => void;
}

export const MetricsDashboard: React.FC<Props> = ({
  metrics,
  projects,
  onSelectProject,
  onOpenCreateProject,
  onOpenCreateTicket,
}) => {
  if (!metrics) {
    return <div className="p-8 text-center text-xs text-theme-muted">Loading metrics...</div>;
  }

  const completionRate = metrics.closedPhaseTickets > 0
    ? Math.round((metrics.completedTickets / metrics.closedPhaseTickets) * 100)
    : 0;

  return (
    <div className="flex-1 overflow-y-auto space-y-6 pr-2">
      {/* Welcome Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 bg-gradient-to-r from-theme-surface to-theme-surfaceHover/80 border border-theme-border rounded-2xl gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-bold uppercase tracking-wider text-theme-primary bg-theme-primary/10 px-2.5 py-0.5 rounded-full">
              System Overview
            </span>
          </div>
          <h1 className="text-2xl font-bold text-theme-text tracking-tight">Ticketing Operations Dashboard</h1>
          <p className="text-xs text-theme-muted mt-1 max-w-xl">
            Select any project to view its real-time Kanban board with Live execution lanes or Plan scope exploration.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            onClick={onOpenCreateProject}
            className="px-3.5 py-2 text-xs font-semibold text-theme-text bg-theme-surface hover:bg-theme-surfaceHover border border-theme-border rounded-lg shadow-sm flex items-center gap-1.5 transition-colors"
          >
            <Folder className="w-4 h-4 text-blue-400" />
            <span>New Project</span>
          </button>
          <button
            onClick={onOpenCreateTicket}
            className="px-3.5 py-2 text-xs font-semibold text-white bg-theme-primary hover:bg-theme-primaryHover rounded-lg shadow-md flex items-center gap-1.5 transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>New Ticket</span>
          </button>
        </div>
      </div>

      {/* KPI Metric Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {/* Total Tickets */}
        <div className="p-4 bg-theme-surface border border-theme-border rounded-xl">
          <div className="flex items-center justify-between text-xs text-theme-muted mb-2">
            <span className="uppercase font-semibold tracking-wider">Total Tickets</span>
            <Activity className="w-4 h-4 text-theme-primary" />
          </div>
          <div className="text-2xl font-bold text-theme-text font-mono">{metrics.totalTickets}</div>
          <div className="text-[11px] text-theme-muted mt-1">Across all projects</div>
        </div>

        {/* Live Scope */}
        <div className="p-4 bg-theme-surface border border-theme-border rounded-xl">
          <div className="flex items-center justify-between text-xs text-theme-muted mb-2">
            <span className="uppercase font-semibold tracking-wider text-blue-400">Live Scope</span>
            <Layers className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold text-theme-text font-mono">{metrics.liveScopeTickets}</div>
          <div className="text-[11px] text-theme-muted mt-1">
            {metrics.executionPhaseTickets} currently in execution
          </div>
        </div>

        {/* Plan Scope */}
        <div className="p-4 bg-theme-surface border border-theme-border rounded-xl">
          <div className="flex items-center justify-between text-xs text-theme-muted mb-2">
            <span className="uppercase font-semibold tracking-wider text-amber-400">Plan Scope</span>
            <Sparkles className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-theme-text font-mono">{metrics.planScopeTickets}</div>
          <div className="text-[11px] text-theme-muted mt-1">Ideation & exploration</div>
        </div>

        {/* Closed & Completion */}
        <div className="p-4 bg-theme-surface border border-theme-border rounded-xl">
          <div className="flex items-center justify-between text-xs text-theme-muted mb-2">
            <span className="uppercase font-semibold tracking-wider text-emerald-400">Closed Tickets</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-theme-text font-mono">{metrics.closedPhaseTickets}</div>
          <div className="text-[11px] text-theme-muted mt-1">
            {metrics.completedTickets} completed • {metrics.cancelledTickets} cancelled
          </div>
        </div>
      </div>

      {/* Phase Breakdown Visual Bar */}
      <div className="p-5 bg-theme-surface border border-theme-border rounded-xl space-y-3">
        <div className="flex items-center justify-between text-xs">
          <h3 className="font-semibold text-theme-text uppercase tracking-wider">Phase Distribution</h3>
          <span className="text-theme-muted">
            {metrics.plannedPhaseTickets} Planned • {metrics.executionPhaseTickets} Execution • {metrics.closedPhaseTickets} Closed
          </span>
        </div>

        {metrics.totalTickets > 0 ? (
          <div className="h-3 w-full bg-theme-bg rounded-full overflow-hidden flex gap-0.5">
            <div
              style={{ width: `${(metrics.plannedPhaseTickets / metrics.totalTickets) * 100}%` }}
              className="bg-blue-500 h-full"
              title={`Planned: ${metrics.plannedPhaseTickets}`}
            />
            <div
              style={{ width: `${(metrics.executionPhaseTickets / metrics.totalTickets) * 100}%` }}
              className="bg-indigo-500 h-full"
              title={`Execution: ${metrics.executionPhaseTickets}`}
            />
            <div
              style={{ width: `${(metrics.completedTickets / metrics.totalTickets) * 100}%` }}
              className="bg-emerald-500 h-full"
              title={`Completed: ${metrics.completedTickets}`}
            />
            <div
              style={{ width: `${(metrics.cancelledTickets / metrics.totalTickets) * 100}%` }}
              className="bg-red-500 h-full"
              title={`Cancelled: ${metrics.cancelledTickets}`}
            />
          </div>
        ) : (
          <div className="h-3 w-full bg-theme-bg rounded-full" />
        )}

        <div className="flex items-center justify-between text-[11px] text-theme-muted pt-1">
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-blue-500" /> Planned</span>
            <span className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-indigo-500" /> Execution</span>
            <span className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-emerald-500" /> Completed</span>
            <span className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-red-500" /> Cancelled</span>
          </div>
          <span>Resolution rate: {completionRate}%</span>
        </div>
      </div>

      {/* Projects Grid: Click to Open Project Kanban Board */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold uppercase tracking-wider text-theme-text">Active Projects</h2>
          <span className="text-xs text-theme-muted">{projects.length} Projects Total</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map(p => (
            <div
              key={p.code}
              onClick={() => onSelectProject(p)}
              className="p-4 bg-theme-surface border border-theme-border rounded-xl hover:border-theme-primary cursor-pointer transition-all duration-150 hover:shadow-lg hover:-translate-y-0.5 group"
            >
              <div className="flex items-start justify-between gap-3 mb-2">
                <div className="flex items-center gap-3">
                  <img
                    src={resolveUrl(p.photoUrl, '/api/photos/default/project-1.svg')}
                    alt=""
                    className="w-10 h-10 rounded-lg object-cover border border-theme-border"
                  />
                  <div>
                    <span className="font-mono text-[10px] font-bold text-theme-primary px-1.5 py-0.5 rounded bg-theme-primary/10">
                      {p.code}
                    </span>
                    <h3 className="font-semibold text-sm text-theme-text mt-0.5 group-hover:text-theme-primary transition-colors">
                      {p.name}
                    </h3>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-theme-muted group-hover:text-theme-primary transition-colors" />
              </div>

              <p className="text-xs text-theme-muted line-clamp-2 mb-3 min-h-[2rem]">
                {p.description || 'No description provided.'}
              </p>

              <div className="flex items-center justify-between pt-2 border-t border-theme-border text-xs text-theme-muted">
                <span>{p.ticketCount} Tickets</span>
                <span className="text-theme-primary font-medium text-[11px] group-hover:underline">
                  Open Kanban Board →
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
