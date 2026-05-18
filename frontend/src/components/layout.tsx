import { Menu, ShieldCheck, LogIn, UserPlus, LogOut, Zap } from 'lucide-react';
import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { Badge, Button, Card } from './ui';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `text-sm font-medium ${isActive ? 'text-brand-primary' : 'text-brand-text/80 hover:text-brand-primary'}`;

export function TopNav() {
  const auth = useAuth();

  return (
    <header className="sticky top-0 z-50 border-b border-slate-200/80 bg-white/85 backdrop-blur-xl">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
        <Link to="/" className="flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-xl bg-brand-primary text-white shadow-md">
            <Zap className="h-5 w-5" />
          </div>
          <div>
            <div className="text-lg font-bold tracking-tight">PulseFit</div>
            <div className="text-xs text-brand-muted">Classes near you</div>
          </div>
        </Link>

        <nav className="hidden items-center gap-6 md:flex">
          <NavLink to="/discover" className={linkClass}>Explore</NavLink>
          <NavLink to="/studios/solstice-yoga" className={linkClass}>Studios</NavLink>
          {!auth.authenticated ? <NavLink to="/pricing" className={linkClass}>Pricing</NavLink> : null}
          {auth.hasRole('admin') ? <NavLink to="/admin" className={linkClass}>Metrics</NavLink> : null}
          {auth.authenticated ? <NavLink to="/dashboard" className={linkClass}>My Bookings</NavLink> : null}
        </nav>

        <div className="flex items-center gap-2">
          {auth.authenticated ? <Badge tone="info">{auth.hasRole('admin') ? 'Admin' : 'User'}</Badge> : <Badge tone="neutral">Guest</Badge>}
          {auth.authenticated ? (
            <>
              <Button variant="ghost" size="sm" onClick={auth.logout}>
                <LogOut className="h-4 w-4" />
                Logout
              </Button>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="secondary" size="sm" onClick={auth.login}>
                <LogIn className="h-4 w-4" />
                Login
              </Button>
              <Button size="sm" onClick={auth.register}>
                <UserPlus className="h-4 w-4" />
                Sign up
              </Button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export function PageShell({ children }: { children: React.ReactNode }) {
  return <div className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(99,102,241,0.08),_transparent_36%),linear-gradient(180deg,_#f9fafb,_#f9fafb)]">{children}</div>;
}

export function PageFrame({ title, subtitle, children, actions }: { title: string; subtitle?: string; children: React.ReactNode; actions?: React.ReactNode }) {
  return (
    <PageShell>
      <TopNav />
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-brand-text sm:text-4xl">{title}</h1>
            {subtitle ? <p className="mt-2 max-w-3xl text-sm leading-6 text-brand-muted sm:text-base">{subtitle}</p> : null}
          </div>
          {actions ? <div>{actions}</div> : null}
        </div>
        {children}
      </main>
    </PageShell>
  );
}

export function HeroBlob() {
  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      <div className="absolute left-[-8rem] top-24 h-80 w-80 rounded-full bg-brand-primary/10 blur-3xl" />
      <div className="absolute right-[-6rem] top-40 h-72 w-72 rounded-full bg-brand-secondary/15 blur-3xl" />
      <div className="absolute bottom-0 left-1/3 h-64 w-64 rounded-full bg-brand-accent/10 blur-3xl" />
    </div>
  );
}

export function SectionTitle({ eyebrow, title, copy }: { eyebrow?: string; title: string; copy?: string }) {
  return (
    <div className="max-w-2xl space-y-2">
      {eyebrow ? <div className="text-xs font-semibold uppercase tracking-[0.24em] text-brand-primary">{eyebrow}</div> : null}
      <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">{title}</h2>
      {copy ? <p className="text-sm leading-6 text-brand-muted sm:text-base">{copy}</p> : null}
    </div>
  );
}

export function ActionCard({ title, copy, icon, href }: { title: string; copy: string; icon: React.ReactNode; href: string }) {
  return (
    <Card className="p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      <div className="flex items-start gap-4">
        <div className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-indigo-100 text-brand-primary">{icon}</div>
        <div className="space-y-1">
          <div className="font-semibold">{title}</div>
          <p className="text-sm text-brand-muted">{copy}</p>
          <Link to={href} className="inline-flex text-sm font-semibold text-brand-primary hover:text-indigo-500">Open</Link>
        </div>
      </div>
    </Card>
  );
}
