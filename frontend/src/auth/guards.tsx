import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthProvider';

export function RequireAuth() {
  const auth = useAuth();
  if (!auth.ready) {
    return <div className="min-h-screen grid place-items-center text-sm text-brand-muted">Loading your space...</div>;
  }
  if (!auth.authenticated) {
    return <Navigate to="/unauthorized" replace />;
  }
  return <Outlet />;
}

export function RequireRole({ role }: { role: 'user' | 'admin' }) {
  const auth = useAuth();
  if (!auth.ready) {
    return <div className="min-h-screen grid place-items-center text-sm text-brand-muted">Loading your space...</div>;
  }
  if (!auth.authenticated) {
    return <Navigate to="/unauthorized" replace />;
  }
  if (!auth.hasRole(role) && !auth.hasRole('admin')) {
    return <Navigate to="/forbidden" replace />;
  }
  return <Outlet />;
}
