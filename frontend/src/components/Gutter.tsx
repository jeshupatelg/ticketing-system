import React, { useState, useRef, useEffect } from 'react';
import {
  Folder,
  Layers,
  CheckCircle2,
  Settings,
  User as UserIcon,
  ChevronLeft,
  ChevronRight,
  Plus,
  BarChart3,
  ChevronDown,
  Sparkles,
  Zap,
} from 'lucide-react';
import { Project, ScopeType, User } from '../types';

interface Props {
  projects: Project[];
  activeProject: Project | null;
  onSelectProject: (project: Project | null) => void;
  activeScope: ScopeType;
  onSelectScope: (scope: ScopeType) => void;
  currentUser: User | null;
  onOpenCreateProject: () => void;
  onOpenCreateTicket: () => void;
  onOpenProfile: () => void;
  activeView: 'dashboard' | 'project';
  onNavigateDashboard: () => void;
}

export const Gutter: React.FC<Props> = ({
  projects,
  activeProject,
  onSelectProject,
  activeScope,
  onSelectScope,
  currentUser,
  onOpenCreateProject,
  onOpenCreateTicket,
  onOpenProfile,
  activeView,
  onNavigateDashboard,
}) => {
  const [collapsed, setCollapsed] = useState(false);
  const [showSettingsMenu, setShowSettingsMenu] = useState(false);
  const [showProjectDropdown, setShowProjectDropdown] = useState(false);
  const settingsRef = useRef<HTMLDivElement>(null);
  const projectDropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (settingsRef.current && !settingsRef.current.contains(e.target as Node)) {
        setShowSettingsMenu(false);
      }
      if (projectDropdownRef.current && !projectDropdownRef.current.contains(e.target as Node)) {
        setShowProjectDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <aside
      className={`h-screen bg-theme-surface border-r border-theme-border flex flex-col transition-all duration-300 relative z-30 select-none ${
        collapsed ? 'w-16' : 'w-64'
      }`}
    >
      {/* Top Header / App Brand */}
      <div className="p-4 border-b border-theme-border flex items-center justify-between">
        {!collapsed && (
          <div className="flex items-center gap-2.5 overflow-hidden cursor-pointer" onClick={onNavigateDashboard}>
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-theme-primary to-indigo-500 flex items-center justify-center text-white shadow-md shadow-theme-primary/30 flex-shrink-0">
              <Zap className="w-4 h-4" />
            </div>
            <div className="truncate">
              <h1 className="font-bold text-sm tracking-tight text-theme-text flex items-center gap-1.5">
                TicketEngine
              </h1>
              <span className="text-[10px] text-theme-muted uppercase tracking-wider font-semibold">Enterprise Hub</span>
            </div>
          </div>
        )}

        {collapsed && (
          <div className="w-8 h-8 rounded-lg bg-theme-primary/20 text-theme-primary flex items-center justify-center mx-auto cursor-pointer" onClick={onNavigateDashboard}>
            <Zap className="w-4 h-4" />
          </div>
        )}

        <button
          onClick={() => setCollapsed(!collapsed)}
          className={`p-1.5 rounded-lg text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors ${
            collapsed ? 'mx-auto mt-2' : ''
          }`}
          title={collapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
        >
          {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </button>
      </div>

      {/* Main Gutter Navigation Body */}
      <div className="flex-1 overflow-y-auto p-3 space-y-4">
        {/* Dashboard Link */}
        <button
          onClick={onNavigateDashboard}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold uppercase tracking-wider transition-all ${
            activeView === 'dashboard'
              ? 'bg-theme-primary text-white shadow-sm shadow-theme-primary/30'
              : 'text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover'
          } ${collapsed ? 'justify-center px-0' : ''}`}
          title="Metrics Dashboard"
        >
          <BarChart3 className="w-4 h-4 flex-shrink-0" />
          {!collapsed && <span>Overview Metrics</span>}
        </button>

        {/* Project Selector Dropdown */}
        <div className="relative" ref={projectDropdownRef}>
          {!collapsed && (
            <div className="text-[10px] font-bold text-theme-muted uppercase tracking-wider px-2 mb-1.5">
              Projects
            </div>
          )}

          <button
            onClick={() => setShowProjectDropdown(!showProjectDropdown)}
            className={`w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm border transition-all ${
              activeView === 'project'
                ? 'bg-theme-surfaceHover border-theme-border text-theme-text'
                : 'border-transparent text-theme-muted hover:bg-theme-surfaceHover hover:text-theme-text'
            } ${collapsed ? 'justify-center px-0' : 'justify-between'}`}
            title={activeProject ? `${activeProject.name} (${activeProject.code})` : 'Select Project'}
          >
            <div className="flex items-center gap-2 truncate">
              {activeProject?.photoUrl ? (
                <img src={activeProject.photoUrl} alt="" className="w-5 h-5 rounded object-cover flex-shrink-0" />
              ) : (
                <Folder className="w-4 h-4 flex-shrink-0 text-amber-400" />
              )}
              {!collapsed && (
                <span className="truncate font-medium text-xs">
                  {activeProject ? activeProject.name : 'Select Project...'}
                </span>
              )}
            </div>
            {!collapsed && <ChevronDown className="w-3.5 h-3.5 text-theme-muted flex-shrink-0" />}
          </button>

          {/* Project Dropdown Menu */}
          {showProjectDropdown && (
            <div
              className={`absolute top-full left-0 mt-1 bg-theme-surface border border-theme-border rounded-xl shadow-2xl py-1 z-50 max-h-64 overflow-y-auto ${
                collapsed ? 'w-48 left-16 -top-2' : 'w-full'
              }`}
            >
              <div className="px-3 py-1.5 text-[10px] font-bold uppercase tracking-wider text-theme-muted border-b border-theme-border">
                Switch Project
              </div>
              {projects.map(p => (
                <button
                  key={p.code}
                  onClick={() => {
                    onSelectProject(p);
                    setShowProjectDropdown(false);
                  }}
                  className={`w-full flex items-center gap-2.5 px-3 py-2 text-xs text-left transition-colors ${
                    activeProject?.code === p.code
                      ? 'bg-theme-primary/10 text-theme-primary font-bold'
                      : 'text-theme-text hover:bg-theme-surfaceHover'
                  }`}
                >
                  <img src={p.photoUrl || '/api/photos/default/project-1.svg'} alt="" className="w-4 h-4 rounded object-cover flex-shrink-0" />
                  <div className="truncate flex-1">
                    <span className="font-mono font-bold mr-1 text-[11px]">[{p.code}]</span>
                    <span>{p.name}</span>
                  </div>
                  <span className="text-[10px] text-theme-muted px-1.5 py-0.5 rounded-full bg-theme-bg">
                    {p.ticketCount}
                  </span>
                </button>
              ))}

              <div className="p-1 border-t border-theme-border mt-1">
                <button
                  onClick={() => {
                    setShowProjectDropdown(false);
                    onOpenCreateProject();
                  }}
                  className="w-full flex items-center gap-2 px-2.5 py-1.5 text-xs text-theme-primary hover:bg-theme-primary/10 rounded-lg transition-colors font-medium"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Create New Project
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Dynamic Project Scopes (in project view) */}
        {activeView === 'project' && activeProject && (
          <div className="space-y-1.5 pt-2 border-t border-theme-border">
            {!collapsed && (
              <div className="text-[10px] font-bold text-theme-muted uppercase tracking-wider px-2 mb-1">
                Board Scopes
              </div>
            )}

            {/* Live Scope Button (Default) */}
            <button
              onClick={() => onSelectScope('LIVE')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-semibold transition-all ${
                activeScope === 'LIVE'
                  ? 'bg-theme-surfaceHover border border-theme-primary/50 text-theme-text shadow-sm'
                  : 'text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover/50'
              } ${collapsed ? 'justify-center px-0' : ''}`}
              title="Live Scope (Kanban Lanes)"
            >
              <div className={`w-2 h-2 rounded-full ${activeScope === 'LIVE' ? 'bg-emerald-400 ring-2 ring-emerald-500/20' : 'bg-theme-muted'}`} />
              <Layers className="w-4 h-4 flex-shrink-0 text-blue-400" />
              {!collapsed && (
                <div className="flex-1 flex items-center justify-between text-left">
                  <span>Live Scope</span>
                  <span className="text-[10px] font-mono text-emerald-400 bg-emerald-500/10 px-1.5 py-0.5 rounded">
                    3 Lanes
                  </span>
                </div>
              )}
            </button>

            {/* Plan Scope Button */}
            <button
              onClick={() => onSelectScope('PLAN')}
              className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-semibold transition-all ${
                activeScope === 'PLAN'
                  ? 'bg-theme-surfaceHover border border-theme-primary/50 text-theme-text shadow-sm'
                  : 'text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover/50'
              } ${collapsed ? 'justify-center px-0' : ''}`}
              title="Plan Scope (Exploration Ideas)"
            >
              <div className={`w-2 h-2 rounded-full ${activeScope === 'PLAN' ? 'bg-amber-400 ring-2 ring-amber-500/20' : 'bg-theme-muted'}`} />
              <Sparkles className="w-4 h-4 flex-shrink-0 text-amber-400" />
              {!collapsed && (
                <div className="flex-1 flex items-center justify-between text-left">
                  <span>Plan Scope</span>
                  <span className="text-[10px] font-mono text-amber-400 bg-amber-500/10 px-1.5 py-0.5 rounded">
                    Ideas
                  </span>
                </div>
              )}
            </button>
          </div>
        )}
      </div>

      {/* Bottom Gutter: Settings & Profile (Persistent across all views) */}
      <div className="p-3 border-t border-theme-border space-y-2 bg-theme-surfaceHover/30">
        {/* Settings Option Menu */}
        <div className="relative" ref={settingsRef}>
          <button
            onClick={() => setShowSettingsMenu(!showSettingsMenu)}
            className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-medium text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors ${
              collapsed ? 'justify-center px-0' : ''
            }`}
            title="Settings"
          >
            <Settings className="w-4 h-4 flex-shrink-0" />
            {!collapsed && <span>Settings</span>}
          </button>

          {showSettingsMenu && (
            <div
              className={`absolute bottom-full left-0 mb-2 bg-theme-surface border border-theme-border rounded-xl shadow-2xl p-1 z-50 animate-in fade-in zoom-in-95 duration-100 ${
                collapsed ? 'w-48 left-16 bottom-0' : 'w-full'
              }`}
            >
              <div className="px-3 py-1.5 text-[10px] font-bold uppercase tracking-wider text-theme-muted border-b border-theme-border">
                Quick Actions
              </div>
              <button
                onClick={() => {
                  setShowSettingsMenu(false);
                  onOpenCreateProject();
                }}
                className="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-left text-theme-text hover:bg-theme-surfaceHover rounded-lg transition-colors font-medium"
              >
                <Folder className="w-3.5 h-3.5 text-blue-400" />
                <span>Create Project</span>
              </button>
              <button
                onClick={() => {
                  setShowSettingsMenu(false);
                  onOpenCreateTicket();
                }}
                className="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-left text-theme-text hover:bg-theme-surfaceHover rounded-lg transition-colors font-medium"
              >
                <Plus className="w-3.5 h-3.5 text-emerald-400" />
                <span>Create Ticket</span>
              </button>
            </div>
          )}
        </div>

        {/* Profile Button */}
        <button
          onClick={onOpenProfile}
          className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-medium text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors ${
            collapsed ? 'justify-center px-0' : ''
          }`}
          title="User Profile & Theme"
        >
          {currentUser?.avatarUrl ? (
            <img src={currentUser.avatarUrl} alt="" className="w-5 h-5 rounded-full object-cover flex-shrink-0 border border-theme-border" />
          ) : (
            <UserIcon className="w-4 h-4 flex-shrink-0" />
          )}
          {!collapsed && (
            <div className="flex-1 text-left truncate">
              <span className="truncate block font-semibold text-theme-text">{currentUser?.name || 'Profile'}</span>
              <span className="text-[10px] text-theme-muted truncate block">@{currentUser?.username || 'user'}</span>
            </div>
          )}
        </button>
      </div>
    </aside>
  );
};
