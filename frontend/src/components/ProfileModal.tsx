import React, { useState, useEffect } from 'react';
import { X, Upload, Check, Palette, User as UserIcon } from 'lucide-react';
import { api, resolveUrl } from '../api';
import { User, PredefinedPhoto } from '../types';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  currentUser: User | null;
  onUserUpdated: (user: User) => void;
  onThemeChanged: (theme: string) => void;
}

const THEMES = [
  { id: 'dark', name: 'Dark Slate', desc: 'Deep zinc & slate tones', bg: 'bg-[#090d16]', border: 'border-[#2e384d]' },
  { id: 'light', name: 'Clean Light', desc: 'Soft slate & white surface', bg: 'bg-[#f8fafc]', border: 'border-[#e2e8f0]' },
  { id: 'slate', name: 'Midnight Slate', desc: 'Ocean slate & sky accents', bg: 'bg-[#0b1120]', border: 'border-[#2d3e64]' },
  { id: 'nord', name: 'Arctic Nord', desc: 'Cool arctic grey & frost blue', bg: 'bg-[#242933]', border: 'border-[#434c5e]' },
  { id: 'indigo', name: 'Deep Indigo', desc: 'Velvet night & electric indigo', bg: 'bg-[#090a16]', border: 'border-[#2e3263]' },
];

export const ProfileModal: React.FC<Props> = ({
  isOpen,
  onClose,
  currentUser,
  onUserUpdated,
  onThemeChanged,
}) => {
  const [photos, setPhotos] = useState<PredefinedPhoto[]>([]);
  const [selectedAvatar, setSelectedAvatar] = useState(currentUser?.avatarUrl || '');
  const [currentTheme, setCurrentTheme] = useState(currentUser?.themePreference || 'dark');
  const [users, setUsers] = useState<User[]>([]);
  const [activeTab, setActiveTab] = useState<'profile' | 'theme' | 'switch'>('profile');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      api.getPhotos('USER').then(setPhotos).catch(console.error);
      api.getUsers().then(setUsers).catch(console.error);
      if (currentUser) {
        setSelectedAvatar(currentUser.avatarUrl || '');
        setCurrentTheme(currentUser.themePreference || 'dark');
      }
    }
  }, [isOpen, currentUser]);

  if (!isOpen || !currentUser) return null;

  const handleSelectAvatar = async (url: string) => {
    setSelectedAvatar(url);
    try {
      const updated = await api.updateAvatar(url);
      onUserUpdated(updated);
      setMessage('Avatar updated successfully!');
      setTimeout(() => setMessage(null), 2500);
    } catch (err: any) {
      console.error(err);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    const file = e.target.files[0];
    try {
      const uploaded = await api.uploadPhoto(file, `${currentUser.username} Avatar`, 'USER');
      setPhotos(prev => [uploaded, ...prev]);
      handleSelectAvatar(uploaded.url);
    } catch (err: any) {
      alert('Upload failed: ' + err.message);
    }
  };

  const handleSelectTheme = async (themeId: string) => {
    setCurrentTheme(themeId);
    onThemeChanged(themeId);
    try {
      const updated = await api.updateTheme(themeId);
      onUserUpdated(updated);
      setMessage(`Theme switched to ${themeId}!`);
      setTimeout(() => setMessage(null), 2500);
    } catch (err: any) {
      console.error(err);
    }
  };

  const handleSwitchUser = (user: User) => {
    // In dev mode, we can set X-User-Name header or switch profile
    onUserUpdated(user);
    onThemeChanged(user.themePreference || 'dark');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="bg-theme-surface border border-theme-border rounded-xl shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between px-6 py-4 border-b border-theme-border bg-theme-surfaceHover/50">
          <div className="flex items-center gap-2">
            <UserIcon className="w-5 h-5 text-theme-primary" />
            <h2 className="text-lg font-semibold text-theme-text">User Profile & Settings</h2>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg text-theme-muted hover:text-theme-text hover:bg-theme-surfaceHover transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab navigation */}
        <div className="flex border-b border-theme-border bg-theme-bg/50 px-6">
          <button
            onClick={() => setActiveTab('profile')}
            className={`py-2.5 px-3 text-xs font-semibold uppercase tracking-wider border-b-2 transition-colors ${
              activeTab === 'profile' ? 'border-theme-primary text-theme-primary' : 'border-transparent text-theme-muted hover:text-theme-text'
            }`}
          >
            Avatar & Info
          </button>
          <button
            onClick={() => setActiveTab('theme')}
            className={`py-2.5 px-3 text-xs font-semibold uppercase tracking-wider border-b-2 transition-colors ${
              activeTab === 'theme' ? 'border-theme-primary text-theme-primary' : 'border-transparent text-theme-muted hover:text-theme-text'
            }`}
          >
            Theme
          </button>
          <button
            onClick={() => setActiveTab('switch')}
            className={`py-2.5 px-3 text-xs font-semibold uppercase tracking-wider border-b-2 transition-colors ${
              activeTab === 'switch' ? 'border-theme-primary text-theme-primary' : 'border-transparent text-theme-muted hover:text-theme-text'
            }`}
          >
            Simulate User
          </button>
        </div>

        <div className="p-6 space-y-4 max-h-[75vh] overflow-y-auto">
          {message && (
            <div className="p-2.5 text-xs text-emerald-400 bg-emerald-950/40 border border-emerald-800/60 rounded-lg flex items-center gap-2">
              <Check className="w-4 h-4" /> {message}
            </div>
          )}

          {activeTab === 'profile' && (
            <div className="space-y-4">
              <div className="flex items-center gap-4 p-3 bg-theme-bg rounded-lg border border-theme-border">
                <img
                  src={resolveUrl(selectedAvatar, '/api/photos/default/avatar-1.svg')}
                  alt={currentUser.name}
                  className="w-14 h-14 rounded-full object-cover border-2 border-theme-border"
                />
                <div>
                  <h3 className="font-semibold text-theme-text text-base">{currentUser.name}</h3>
                  <p className="text-xs text-theme-muted font-mono">@{currentUser.username}</p>
                  <p className="text-xs text-theme-muted mt-0.5">{currentUser.email || 'Synchronized via APIGW JWT'}</p>
                </div>
              </div>

              <div>
                <div className="flex items-center justify-between mb-2">
                  <label className="text-xs font-semibold uppercase tracking-wider text-theme-muted">
                    Choose Profile Photo
                  </label>
                  <label className="text-xs text-theme-primary hover:underline cursor-pointer flex items-center gap-1">
                    <Upload className="w-3.5 h-3.5" />
                    Upload Custom Photo
                    <input type="file" accept="image/*" onChange={handleFileUpload} className="hidden" />
                  </label>
                </div>
                <div className="grid grid-cols-4 gap-2.5 p-2 bg-theme-bg rounded-lg border border-theme-border max-h-48 overflow-y-auto">
                  {photos.map(p => (
                    <button
                      type="button"
                      key={p.id}
                      onClick={() => handleSelectAvatar(p.url)}
                      className={`relative p-2 rounded-lg flex flex-col items-center justify-center transition-all ${
                        selectedAvatar === p.url ? 'ring-2 ring-theme-primary bg-theme-surfaceHover' : 'hover:bg-theme-surfaceHover/50'
                      }`}
                    >
                      <img src={resolveUrl(p.url)} alt={p.name} className="w-10 h-10 rounded-full object-cover" />
                      <span className="text-[10px] text-theme-muted truncate w-full text-center mt-1">{p.name}</span>
                      {selectedAvatar === p.url && (
                        <div className="absolute top-1 right-1 bg-theme-primary rounded-full p-0.5 text-white">
                          <Check className="w-2.5 h-2.5" />
                        </div>
                      )}
                    </button>
                  ))}
                </div>
                <p className="text-[11px] text-theme-muted mt-1.5 italic">
                  Note: Uploaded photos become available as options to all users and projects!
                </p>
              </div>
            </div>
          )}

          {activeTab === 'theme' && (
            <div className="space-y-3">
              <div className="text-xs text-theme-muted mb-2">
                Theme choice is persisted in your user profile across sessions. Designed with balanced contrast for optimal readability.
              </div>
              <div className="space-y-2">
                {THEMES.map(t => (
                  <button
                    key={t.id}
                    onClick={() => handleSelectTheme(t.id)}
                    className={`w-full flex items-center justify-between p-3 rounded-lg border transition-all text-left ${
                      currentTheme === t.id
                        ? 'border-theme-primary ring-1 ring-theme-primary bg-theme-surfaceHover'
                        : 'border-theme-border hover:bg-theme-surfaceHover/60'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div className={`w-6 h-6 rounded-full ${t.bg} border ${t.border} shadow-sm`} />
                      <div>
                        <div className="text-sm font-medium text-theme-text">{t.name}</div>
                        <div className="text-xs text-theme-muted">{t.desc}</div>
                      </div>
                    </div>
                    {currentTheme === t.id && <Check className="w-4 h-4 text-theme-primary" />}
                  </button>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'switch' && (
            <div className="space-y-2">
              <div className="text-xs text-theme-muted mb-2">
                Switching active user for testing ticket assignments, reassignments, and comments:
              </div>
              <div className="space-y-1.5 max-h-56 overflow-y-auto">
                {users.map(u => (
                  <button
                    key={u.username}
                    onClick={() => handleSwitchUser(u)}
                    className={`w-full flex items-center justify-between p-2.5 rounded-lg border text-left transition-all ${
                      currentUser.username === u.username
                        ? 'border-theme-primary bg-theme-surfaceHover'
                        : 'border-theme-border hover:bg-theme-surfaceHover/50'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <img src={resolveUrl(u.avatarUrl, '/api/photos/default/avatar-1.svg')} alt={u.name} className="w-7 h-7 rounded-full object-cover" />
                      <div>
                        <span className="text-sm font-medium text-theme-text">{u.name}</span>
                        <span className="text-xs text-theme-muted ml-2">@{u.username}</span>
                      </div>
                    </div>
                    {currentUser.username === u.username && (
                      <span className="text-[10px] uppercase font-bold text-theme-primary px-2 py-0.5 bg-theme-primary/10 rounded">
                        Active
                      </span>
                    )}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="flex justify-end p-4 border-t border-theme-border bg-theme-surfaceHover/30">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium rounded-lg bg-theme-primary text-white hover:bg-theme-primaryHover transition-colors"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};
