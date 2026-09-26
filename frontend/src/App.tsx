import React, { useState, useEffect, useCallback } from 'react';
import { api } from './api';
import {
  Project,
  TicketSummary,
  User,
  Metrics,
  ScopeType,
  FilterState,
} from './types';
import { Gutter } from './components/Gutter';
import { KanbanBoard } from './components/KanbanBoard';
import { MetricsDashboard } from './components/MetricsDashboard';
import { FilterBar } from './components/FilterBar';
import { TicketModal } from './components/TicketModal';
import { CreateProjectModal } from './components/CreateProjectModal';
import { CreateTicketModal } from './components/CreateTicketModal';
import { ProfileModal } from './components/ProfileModal';

export const App: React.FC = () => {
  // Navigation & Project state
  const [activeView, setActiveView] = useState<'dashboard' | 'project'>('dashboard');
  const [projects, setProjects] = useState<Project[]>([]);
  const [activeProject, setActiveProject] = useState<Project | null>(null);
  const [activeScope, setActiveScope] = useState<ScopeType>('LIVE');

  // Data state
  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [tags, setTags] = useState<string[]>([]);

  // Filter state
  const [filters, setFilters] = useState<FilterState>({
    search: '',
    assignee: null,
    phase: null,
    tag: null,
    dateRange: null,
  });

  // Modal states
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(null);
  const [isCreateProjectOpen, setIsCreateProjectOpen] = useState(false);
  const [isCreateTicketOpen, setIsCreateTicketOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  // Apply theme class to document body
  const applyTheme = (themeName: string) => {
    document.body.className = `theme-${themeName} bg-theme-bg text-theme-text font-sans antialiased overflow-hidden select-none`;
  };

  // Initial load
  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      const [profileData, projectsData, metricsData, usersData, tagsData] = await Promise.all([
        api.getProfile().catch(() => null),
        api.getProjects().catch(() => []),
        api.getMetrics().catch(() => null),
        api.getUsers().catch(() => []),
        api.getTags().catch(() => []),
      ]);

      if (profileData) {
        setCurrentUser(profileData);
        applyTheme(profileData.themePreference || 'dark');
      } else {
        applyTheme('dark');
      }

      setProjects(projectsData);
      setMetrics(metricsData);
      setUsers(usersData);
      setTags(tagsData);

      // Default to Adhocs project if available, or first project
      if (projectsData.length > 0) {
        const adhoc = projectsData.find(p => p.code === 'ADH') || projectsData[0];
        setActiveProject(adhoc);
      }
    } catch (err) {
      console.error('Failed to load initial application data', err);
    }
  };

  // Clear tickets immediately when project or scope switches to avoid stale render
  useEffect(() => {
    setTickets([]);
  }, [activeProject?.code, activeScope]);

  // Load tickets when project or scope changes
  const loadTickets = useCallback(async () => {
    if (!activeProject) return;
    const targetProject = activeProject.code;
    const targetScope = activeScope;
    try {
      const data = await api.getProjectTickets(
        targetProject,
        targetScope,
        filters.dateRange === 'all'
      );
      // Only commit data if the user is still on the same project and scope
      if (activeProject.code === targetProject && activeScope === targetScope) {
        setTickets(data);
      }
    } catch (err) {
      console.error('Failed to load tickets', err);
      // Clear tickets on error if still on the same target
      if (activeProject.code === targetProject && activeScope === targetScope) {
        setTickets([]);
      }
    }
  }, [activeProject, activeScope, filters.dateRange]);

  useEffect(() => {
    if (activeView === 'project' && activeProject) {
      loadTickets();
    }
  }, [activeView, activeProject, activeScope, loadTickets]);

  // Project selection
  const handleSelectProject = (project: Project | null) => {
    if (project) {
      setActiveProject(project);
      setActiveView('project');
    } else {
      setActiveView('dashboard');
    }
  };

  const handlePromoteTicket = async (ticketId: string) => {
    try {
      await api.promoteTicket(ticketId);
      loadTickets();
      api.getMetrics().then(setMetrics).catch(console.error);
    } catch (err: any) {
      alert(err.message);
    }
  };

  const handleCancelPlanTicket = async (ticketId: string) => {
    try {
      await api.cancelPlanTicket(ticketId);
      loadTickets();
      api.getMetrics().then(setMetrics).catch(console.error);
    } catch (err: any) {
      alert(err.message);
    }
  };

  const handleProjectCreated = (newProject: Project) => {
    setProjects(prev => [...prev, newProject]);
    setActiveProject(newProject);
    setActiveView('project');
    api.getMetrics().then(setMetrics).catch(console.error);
  };

  const handleTicketCreated = () => {
    loadTickets();
    api.getMetrics().then(setMetrics).catch(console.error);
    api.getTags().then(setTags).catch(console.error);
  };

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-theme-bg text-theme-text">
      {/* Persistent Left Gutter */}
      <Gutter
        projects={projects}
        activeProject={activeProject}
        onSelectProject={handleSelectProject}
        activeScope={activeScope}
        onSelectScope={setActiveScope}
        currentUser={currentUser}
        onOpenCreateProject={() => setIsCreateProjectOpen(true)}
        onOpenCreateTicket={() => setIsCreateTicketOpen(true)}
        onOpenProfile={() => setIsProfileOpen(true)}
        activeView={activeView}
        onNavigateDashboard={() => {
          setActiveView('dashboard');
          api.getMetrics().then(setMetrics).catch(console.error);
        }}
      />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden p-4 md:p-6 bg-theme-bg">
        {activeView === 'dashboard' ? (
          /* Landing Page Overview Metrics Dashboard */
          <MetricsDashboard
            metrics={metrics}
            projects={projects}
            onSelectProject={handleSelectProject}
            onOpenCreateProject={() => setIsCreateProjectOpen(true)}
            onOpenCreateTicket={() => setIsCreateTicketOpen(true)}
          />
        ) : activeProject ? (
          /* Project Kanban View with Filters */
          <div className="flex-1 flex flex-col h-full overflow-hidden">
            <FilterBar
              filters={filters}
              onFilterChange={setFilters}
              users={users}
              availableTags={tags}
              isPlanScope={activeScope === 'PLAN'}
              totalFilteredCount={tickets.length}
            />

            <KanbanBoard
              project={activeProject}
              scope={activeScope}
              tickets={tickets}
              filters={filters}
              onOpenTicket={setSelectedTicketId}
              onOpenCreateTicket={() => setIsCreateTicketOpen(true)}
              onPromoteTicket={handlePromoteTicket}
              onCancelPlanTicket={handleCancelPlanTicket}
            />
          </div>
        ) : (
          <div className="flex-1 flex items-center justify-center text-sm text-theme-muted">
            Please select a project to view tickets.
          </div>
        )}
      </main>

      {/* Ticket Detail Modal */}
      {selectedTicketId && (
        <TicketModal
          key={selectedTicketId}
          ticketId={selectedTicketId}
          isOpen={true}
          onClose={() => setSelectedTicketId(null)}
          onTicketUpdated={() => {
            loadTickets();
            api.getMetrics().then(setMetrics).catch(console.error);
          }}
          users={users}
          onOpenRelatedTicket={relId => setSelectedTicketId(relId)}
        />
      )}

      {/* Create Project Modal */}
      {isCreateProjectOpen && (
        <CreateProjectModal
          isOpen={true}
          onClose={() => setIsCreateProjectOpen(false)}
          onProjectCreated={handleProjectCreated}
        />
      )}

      {/* Create Ticket Modal */}
      {isCreateTicketOpen && (
        <CreateTicketModal
          isOpen={true}
          onClose={() => setIsCreateTicketOpen(false)}
          projects={projects}
          activeProjectCode={activeProject?.code || null}
          onTicketCreated={handleTicketCreated}
        />
      )}

      {/* Profile & Theme Settings Modal */}
      {isProfileOpen && (
        <ProfileModal
          isOpen={true}
          onClose={() => setIsProfileOpen(false)}
          currentUser={currentUser}
          onUserUpdated={u => setCurrentUser(u)}
          onThemeChanged={applyTheme}
        />
      )}
    </div>
  );
};
