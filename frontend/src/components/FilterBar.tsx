import React, { useState } from 'react';
import { Search, Filter, X, User as UserIcon, Tag, Calendar, Activity, CheckSquare } from 'lucide-react';
import { FilterState, PhaseType, User } from '../types';

interface Props {
  filters: FilterState;
  onFilterChange: (filters: FilterState) => void;
  users: User[];
  availableTags: string[];
  isPlanScope: boolean;
  totalFilteredCount: number;
}

export const FilterBar: React.FC<Props> = ({
  filters,
  onFilterChange,
  users,
  availableTags,
  isPlanScope,
  totalFilteredCount,
}) => {
  const [showDropdown, setShowDropdown] = useState(false);

  const hasActiveFilters = Boolean(
    filters.search ||
    filters.assignee ||
    filters.phase ||
    filters.tag ||
    filters.dateRange
  );

  const clearAllFilters = () => {
    onFilterChange({
      search: '',
      assignee: null,
      phase: null,
      tag: null,
      dateRange: null,
    });
  };

  return (
    <div className="space-y-2.5 pb-2">
      {/* Search and Dropdowns Bar */}
      <div className="flex flex-wrap items-center gap-2.5">
        {/* Search Input */}
        <div className="relative flex-1 min-w-[200px]">
          <Search className="w-4 h-4 absolute left-3 top-2.5 text-theme-muted" />
          <input
            type="text"
            value={filters.search}
            onChange={e => onFilterChange({ ...filters, search: e.target.value })}
            placeholder="Search tickets by ID, title, description..."
            className="w-full pl-9 pr-3 py-1.5 bg-theme-surface border border-theme-border rounded-lg text-xs text-theme-text placeholder-theme-muted focus:outline-none focus:ring-1 focus:ring-theme-primary"
          />
          {filters.search && (
            <button
              onClick={() => onFilterChange({ ...filters, search: '' })}
              className="absolute right-2.5 top-2 text-theme-muted hover:text-theme-text"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Filter Dropdowns */}
        <div className="flex items-center gap-2 flex-wrap">
          {/* User / Assignee Dropdown */}
          <div className="relative">
            <select
              value={filters.assignee || ''}
              onChange={e => onFilterChange({ ...filters, assignee: e.target.value || null })}
              className="px-2.5 py-1.5 bg-theme-surface border border-theme-border rounded-lg text-xs text-theme-text focus:outline-none focus:ring-1 focus:ring-theme-primary"
            >
              <option value="">All Assignees</option>
              <option value="unassigned">Unassigned</option>
              {users.map(u => (
                <option key={u.username} value={u.username}>
                  {u.name} (@{u.username})
                </option>
              ))}
            </select>
          </div>

          {/* Phase Dropdown (Live Scope Only) */}
          {!isPlanScope && (
            <div className="relative">
              <select
                value={filters.phase || ''}
                onChange={e => onFilterChange({ ...filters, phase: (e.target.value as PhaseType) || null })}
                className="px-2.5 py-1.5 bg-theme-surface border border-theme-border rounded-lg text-xs text-theme-text focus:outline-none focus:ring-1 focus:ring-theme-primary"
              >
                <option value="">All Phases</option>
                <option value="PLANNED">Planned Phase</option>
                <option value="EXECUTION">Execution Phase</option>
                <option value="CLOSED">Closed Phase</option>
              </select>
            </div>
          )}

          {/* Tag Dropdown */}
          <div className="relative">
            <select
              value={filters.tag || ''}
              onChange={e => onFilterChange({ ...filters, tag: e.target.value || null })}
              className="px-2.5 py-1.5 bg-theme-surface border border-theme-border rounded-lg text-xs text-theme-text focus:outline-none focus:ring-1 focus:ring-theme-primary"
            >
              <option value="">All Tags</option>
              {availableTags.map(t => (
                <option key={t} value={t}>
                  #{t}
                </option>
              ))}
            </select>
          </div>

          {/* Completion Date Range Dropdown */}
          <div className="relative">
            <select
              value={filters.dateRange || ''}
              onChange={e => onFilterChange({ ...filters, dateRange: (e.target.value as any) || null })}
              className="px-2.5 py-1.5 bg-theme-surface border border-theme-border rounded-lg text-xs text-theme-text focus:outline-none focus:ring-1 focus:ring-theme-primary"
            >
              <option value="">Completed: Up to 1 week (Default)</option>
              <option value="all">Completed: All time</option>
              <option value="1month">Completed: Past 30 days</option>
            </select>
          </div>

          {hasActiveFilters && (
            <button
              onClick={clearAllFilters}
              className="px-2.5 py-1.5 text-xs font-medium text-red-400 hover:text-red-300 hover:bg-red-950/30 rounded-lg border border-red-900/50 transition-colors flex items-center gap-1"
            >
              <X className="w-3.5 h-3.5" /> Clear Filters
            </button>
          )}
        </div>
      </div>

      {/* Applied Filter Chips with distinct color flavors */}
      {hasActiveFilters && (
        <div className="flex items-center gap-2 flex-wrap pt-1">
          <span className="text-[11px] font-semibold text-theme-muted uppercase tracking-wider">Active:</span>

          {filters.search && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-sky-950/60 border border-sky-600/50 text-sky-300">
              <Search className="w-3 h-3" />
              <span>"{filters.search}"</span>
              <button onClick={() => onFilterChange({ ...filters, search: '' })} className="hover:text-white">
                <X className="w-3 h-3" />
              </button>
            </span>
          )}

          {filters.assignee && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-blue-950/60 border border-blue-600/50 text-blue-300">
              <UserIcon className="w-3 h-3" />
              <span>
                User: {filters.assignee === 'unassigned' ? 'Unassigned' : filters.assignee}
              </span>
              <button onClick={() => onFilterChange({ ...filters, assignee: null })} className="hover:text-white">
                <X className="w-3 h-3" />
              </button>
            </span>
          )}

          {filters.phase && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-950/60 border border-amber-600/50 text-amber-300">
              <Activity className="w-3 h-3" />
              <span>Phase: {filters.phase}</span>
              <button onClick={() => onFilterChange({ ...filters, phase: null })} className="hover:text-white">
                <X className="w-3 h-3" />
              </button>
            </span>
          )}

          {filters.tag && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-950/60 border border-emerald-600/50 text-emerald-300">
              <Tag className="w-3 h-3" />
              <span>Tag: #{filters.tag}</span>
              <button onClick={() => onFilterChange({ ...filters, tag: null })} className="hover:text-white">
                <X className="w-3 h-3" />
              </button>
            </span>
          )}

          {filters.dateRange && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-purple-950/60 border border-purple-600/50 text-purple-300">
              <Calendar className="w-3 h-3" />
              <span>Date: {filters.dateRange}</span>
              <button onClick={() => onFilterChange({ ...filters, dateRange: null })} className="hover:text-white">
                <X className="w-3 h-3" />
              </button>
            </span>
          )}

          {/* Indicator for Cap: when filter is applied, at most 5 or 6 tickets are shown per lane */}
          <span className="text-[11px] text-theme-muted font-mono ml-auto">
            (Filtered view: max 6 tickets per lane)
          </span>
        </div>
      )}
    </div>
  );
};
