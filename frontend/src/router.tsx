import { Route, Routes } from 'react-router-dom';
import { RequireAuth, RequireRole } from './auth/guards';
import {
  AccountPage,
  AdminDashboardPage,
  BillingPage,
  DiscoveryPage,
  ForbiddenPage,
  LandingPage,
  NotFoundPage,
  StudioPage,
  UnauthorizedPage,
  UserDashboardPage
} from './features/pages';

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/discover" element={<DiscoveryPage />} />
      <Route path="/studios/:slug" element={<StudioPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />
      <Route element={<RequireRole role="user" />}>
        <Route path="/dashboard" element={<UserDashboardPage />} />
        <Route path="/billing" element={<BillingPage />} />
        <Route path="/account" element={<AccountPage />} />
      </Route>
      <Route element={<RequireRole role="admin" />}>
        <Route path="/admin" element={<AdminDashboardPage />} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
