import {
  Project,
  TicketSummary,
  TicketDetail,
  TicketIdea,
  TicketCheckpoint,
  TicketComment,
  TicketAttachment,
  PredefinedPhoto,
  Metrics,
  User,
  ScopeType,
  PhaseType,
  PriorityType,
} from './types';

const getApiBase = () => {
  const path = window.location.pathname;
  if (path.startsWith('/ticketing')) {
    return '/ticketing/api';
  }
  return '/api';
};

const API_BASE = getApiBase();

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(errorData.error || errorData.message || `Request failed with ${res.status}`);
  }
  if (res.status === 204) {
    return {} as T;
  }
  return res.json() as Promise<T>;
}

export const api = {
  // Projects
  getProjects: (): Promise<Project[]> =>
    fetch(`${API_BASE}/projects`).then(res => handleResponse<Project[]>(res)),

  getProject: (code: string): Promise<Project> =>
    fetch(`${API_BASE}/projects/${code}`).then(res => handleResponse<Project>(res)),

  createProject: (data: { code: string; name: string; description?: string; photoUrl?: string }): Promise<Project> =>
    fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => handleResponse<Project>(res)),

  // Tickets
  getProjectTickets: (projectCode: string, scope: ScopeType, allCompleted = false): Promise<TicketSummary[]> =>
    fetch(`${API_BASE}/projects/${projectCode}/tickets?scope=${scope}&includeAllCompleted=${allCompleted}`).then(res => handleResponse<TicketSummary[]>(res)),

  getTicket: (id: string): Promise<TicketDetail> =>
    fetch(`${API_BASE}/tickets/${id}`).then(res => handleResponse<TicketDetail>(res)),

  createTicket: (data: {
    projectCode: string;
    title: string;
    description?: string;
    priority?: PriorityType;
    tags?: string[];
    ideas?: string[];
  }): Promise<TicketDetail> =>
    fetch(`${API_BASE}/tickets`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => handleResponse<TicketDetail>(res)),

  updateTicket: (id: string, data: {
    title?: string;
    description?: string;
    priority?: PriorityType;
    tags?: string[];
  }): Promise<TicketDetail> =>
    fetch(`${API_BASE}/tickets/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => handleResponse<TicketDetail>(res)),

  promoteTicket: (id: string): Promise<TicketDetail> =>
    fetch(`${API_BASE}/tickets/${id}/promote`, { method: 'POST' }).then(res => handleResponse<TicketDetail>(res)),

  cancelPlanTicket: (id: string): Promise<TicketDetail> =>
    fetch(`${API_BASE}/tickets/${id}/cancel-plan`, { method: 'POST' }).then(res => handleResponse<TicketDetail>(res)),

  transitionPhase: (id: string, phase: PhaseType, assignee?: string, completed?: boolean): Promise<TicketDetail> =>
    fetch(`${API_BASE}/tickets/${id}/transition`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phase, assignee, completed }),
    }).then(res => handleResponse<TicketDetail>(res)),

  reassignTicket: (id: string, assignee: string): Promise<TicketDetail> =>
    fetch(`${API_BASE}/tickets/${id}/reassign`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ assignee }),
    }).then(res => handleResponse<TicketDetail>(res)),

  // Ideas (Plan)
  addIdea: (ticketId: string, content: string, active = true): Promise<TicketIdea> =>
    fetch(`${API_BASE}/tickets/${ticketId}/ideas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content, active }),
    }).then(res => handleResponse<TicketIdea>(res)),

  updateIdea: (ticketId: string, ideaId: number, content: string, active: boolean): Promise<TicketIdea> =>
    fetch(`${API_BASE}/tickets/${ticketId}/ideas/${ideaId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content, active }),
    }).then(res => handleResponse<TicketIdea>(res)),

  deleteIdea: (ticketId: string, ideaId: number): Promise<void> =>
    fetch(`${API_BASE}/tickets/${ticketId}/ideas/${ideaId}`, { method: 'DELETE' }).then(res => handleResponse<void>(res)),

  // Checkpoints (Live)
  toggleCheckpoint: (ticketId: string, checkpointId: number, completed: boolean): Promise<TicketCheckpoint> =>
    fetch(`${API_BASE}/tickets/${ticketId}/checkpoints/${checkpointId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ completed }),
    }).then(res => handleResponse<TicketCheckpoint>(res)),

  // Comments
  addComment: (ticketId: string, content: string): Promise<TicketComment> =>
    fetch(`${API_BASE}/tickets/${ticketId}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content }),
    }).then(res => handleResponse<TicketComment>(res)),

  // Related Tickets
  linkRelatedTicket: (ticketId: string, relatedTicketId: string): Promise<void> =>
    fetch(`${API_BASE}/tickets/${ticketId}/related`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ relatedTicketId }),
    }).then(res => handleResponse<void>(res)),

  unlinkRelatedTicket: (ticketId: string, relatedTicketId: string): Promise<void> =>
    fetch(`${API_BASE}/tickets/${ticketId}/related/${relatedTicketId}`, { method: 'DELETE' }).then(res => handleResponse<void>(res)),

  // Attachments
  uploadAttachment: (ticketId: string, file: File): Promise<TicketAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    return fetch(`${API_BASE}/tickets/${ticketId}/attachments`, {
      method: 'POST',
      body: formData,
    }).then(res => handleResponse<TicketAttachment>(res));
  },

  deleteAttachment: (id: number): Promise<void> =>
    fetch(`${API_BASE}/attachments/${id}`, { method: 'DELETE' }).then(res => handleResponse<void>(res)),

  getAttachmentDownloadUrl: (id: number): string => `${API_BASE}/attachments/${id}/download`,

  getAttachmentConfig: (): Promise<{ maxAttachmentCount: number; maxSizeMb: number }> =>
    fetch(`${API_BASE}/attachments/config`).then(res => handleResponse<{ maxAttachmentCount: number; maxSizeMb: number }>(res)),

  // Photos
  getPhotos: (category = 'ALL'): Promise<PredefinedPhoto[]> =>
    fetch(`${API_BASE}/photos?category=${category}`).then(res => handleResponse<PredefinedPhoto[]>(res)),

  uploadPhoto: (file: File, name: string, category = 'BOTH'): Promise<PredefinedPhoto> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', name);
    formData.append('category', category);
    return fetch(`${API_BASE}/photos/upload`, {
      method: 'POST',
      body: formData,
    }).then(res => handleResponse<PredefinedPhoto>(res));
  },

  // Metrics & Tags
  getMetrics: (): Promise<Metrics> =>
    fetch(`${API_BASE}/metrics`).then(res => handleResponse<Metrics>(res)),

  getTags: (): Promise<string[]> =>
    fetch(`${API_BASE}/tags`).then(res => handleResponse<string[]>(res)),

  // Users & Profile
  getProfile: (): Promise<User> =>
    fetch(`${API_BASE}/profile`).then(res => handleResponse<User>(res)),

  updateTheme: (theme: string): Promise<User> =>
    fetch(`${API_BASE}/profile/theme`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ theme }),
    }).then(res => handleResponse<User>(res)),

  updateAvatar: (avatarUrl: string): Promise<User> =>
    fetch(`${API_BASE}/profile/avatar`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ avatarUrl }),
    }).then(res => handleResponse<User>(res)),

  getUsers: (): Promise<User[]> =>
    fetch(`${API_BASE}/users`).then(res => handleResponse<User[]>(res)),
};
