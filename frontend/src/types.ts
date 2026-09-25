export type ScopeType = 'PLAN' | 'LIVE';
export type PhaseType = 'PLAN' | 'PLANNED' | 'EXECUTION' | 'CLOSED';
export type PriorityType = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface User {
  id: string;
  username: string;
  name: string;
  email?: string;
  avatarUrl?: string;
  themePreference: string;
}

export interface Project {
  code: string;
  name: string;
  description?: string;
  photoUrl?: string;
  ticketCount: number;
  createdAt: string;
}

export interface TicketIdea {
  id: number;
  ticketId: string;
  content: string;
  active: boolean;
  orderIndex: number;
  createdAt: string;
}

export interface TicketCheckpoint {
  id: number;
  ticketId: string;
  title: string;
  completed: boolean;
  orderIndex: number;
  createdAt: string;
}

export interface TicketComment {
  id: number;
  ticketId: string;
  author: string;
  authorAvatarUrl?: string;
  content: string;
  createdAt: string;
}

export interface TicketAttachment {
  id: number;
  ticketId: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  filePath: string;
  uploadedBy: string;
  uploadedAt: string;
}

export interface TicketSummary {
  id: string;
  projectCode: string;
  title: string;
  description?: string;
  scope: ScopeType;
  phase: PhaseType;
  completed: boolean;
  assignee?: string;
  assigneeName?: string;
  assigneeAvatarUrl?: string;
  reporter?: string;
  reporterName?: string;
  priority: PriorityType;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  promotedAt?: string;
  ideaCount: number;
  activeIdeaCount: number;
  totalCheckpoints: number;
  completedCheckpoints: number;
  commentCount: number;
  attachmentCount: number;
}

export type TicketActivityType =
  | 'TICKET_CREATED'
  | 'TICKET_UPDATED'
  | 'TICKET_PROMOTED'
  | 'PHASE_TRANSITIONED'
  | 'TICKET_COMPLETED'
  | 'TICKET_CANCELLED'
  | 'TICKET_REASSIGNED'
  | 'IDEA_ADDED'
  | 'IDEA_UPDATED'
  | 'IDEA_DELETED'
  | 'CHECKPOINT_TOGGLED'
  | 'COMMENT_ADDED'
  | 'ATTACHMENT_UPLOADED'
  | 'ATTACHMENT_DELETED'
  | 'RELATED_TICKET_LINKED'
  | 'RELATED_TICKET_UNLINKED';

export interface TicketActivity {
  id: number;
  ticketId: string;
  activityType: TicketActivityType;
  username: string;
  userDisplayName?: string;
  userAvatarUrl?: string;
  description: string;
  details?: string;
  timestamp: string;
}

export interface TicketDetail extends TicketSummary {
  ideas: TicketIdea[];
  checkpoints: TicketCheckpoint[];
  comments: TicketComment[];
  relatedTickets: string[];
  attachments: TicketAttachment[];
  activities: TicketActivity[];
}

export interface Metrics {
  totalTickets: number;
  planScopeTickets: number;
  liveScopeTickets: number;
  plannedPhaseTickets: number;
  executionPhaseTickets: number;
  closedPhaseTickets: number;
  completedTickets: number;
  cancelledTickets: number;
  totalProjects: number;
  projectTicketCounts: Record<string, number>;
}

export interface PredefinedPhoto {
  id: number;
  name: string;
  url: string;
  category: string;
  custom: boolean;
}

export interface FilterState {
  search: string;
  assignee: string | null;
  phase: PhaseType | null;
  tag: string | null;
  dateRange: 'all' | '1week' | '1month' | null;
}
