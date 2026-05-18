import { ArrowRight, Clock3, Search, ShieldCheck, Sparkles, Star, Wallet, CheckCircle2, CalendarDays, MapPin } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { apiFetch } from '../api/client';
import { Badge, Button, Card, Input, StatCard } from '../components/ui';
import { ActionCard, HeroBlob, PageFrame, SectionTitle } from '../components/layout';

type LandingStudio = { id: number; slug: string; name: string; neighborhood: string; discipline: string; imageUrl: string; rating: number; basePrice: number; featured: boolean };
type SessionSummary = { id: number; studioName: string; title: string; startsAt: string; discipline: string; neighborhood: string; price: number; capacity: number; bookedCount: number; }
type DashboardResponse = { nextClass?: SessionSummary; upcomingBookings: SessionSummary[]; recentActivity: string[]; creditsRemaining: number; recommendedStudios: LandingStudio[]; }
type AdminMetrics = { revenue: number; bookings: number; approvedStudios: number; pendingStudios: number; refunds: number; utilization: number; }
type AdminUser = { id: string; email: string; username: string; roles: string[]; enabled: boolean; }
type BillingSummary = { subscriptionActive: boolean; creditsRemaining: number; nextRenewalAt?: string; savedPaymentMethod?: string; customerPortalUrl?: string; receipts: { id: number; label: string; amount: number; status: string; createdAt: string }[] }
type Profile = { displayName: string; email: string; marketingOptIn: boolean }
type StudioDetail = { name: string; neighborhood: string; discipline: string; description: string; imageUrl: string; sessions: SessionSummary[]; instructorName: string; rating: number; priceFrom: number }

const featuredStudios: LandingStudio[] = [
  { id: 1, slug: 'solstice-yoga', name: 'Solstice Yoga', neighborhood: 'Notting Hill', discipline: 'Yoga', imageUrl: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80', rating: 4.9, basePrice: 24, featured: true },
  { id: 2, slug: 'pulse-hiit', name: 'Pulse HIIT Lab', neighborhood: 'Shoreditch', discipline: 'HIIT', imageUrl: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80', rating: 4.8, basePrice: 28, featured: true },
  { id: 3, slug: 'moss-pilates', name: 'Moss Pilates', neighborhood: 'King\'s Cross', discipline: 'Pilates', imageUrl: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80', rating: 4.7, basePrice: 26, featured: false }
];

function BookSessionButton({ sessionId, label = 'Book' }: { sessionId: number; label?: string }) {
  const auth = useAuth();
  const mutation = useMutation({
    mutationFn: () => apiFetch<{ bookingId?: number; checkoutUrl?: string }>('/bookings', auth.token, { method: 'POST', body: JSON.stringify({ sessionId }) }),
    onSuccess: (response) => {
      if (response.checkoutUrl) {
        window.location.assign(response.checkoutUrl);
      } else {
        window.location.assign('/dashboard');
      }
    }
  });

  return (
    <Button onClick={() => (auth.authenticated ? mutation.mutate() : auth.login())} disabled={mutation.isPending}>
      {mutation.isPending ? 'Preparing...' : label}
    </Button>
  );
}

function currency(value: number) {
  return new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 }).format(value);
}

function relativeTime(iso: string) {
  return new Intl.DateTimeFormat('en-GB', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso));
}

export function LandingPage() {
  const auth = useAuth();
  return (
    <PageFrame
      title="Find your flow, one class at a time."
      subtitle="Browse boutique studios nearby, book a spot in seconds, and keep your training streak moving without spreadsheets or DMs."
    >
      <HeroBlob />
      <div className="relative grid gap-8 lg:grid-cols-[1.15fr_0.85fr] lg:items-center">
        <div className="space-y-8">
          <div className="space-y-4">
            <div className="inline-flex items-center gap-2 rounded-full bg-indigo-100 px-4 py-2 text-xs font-semibold uppercase tracking-[0.2em] text-brand-primary">
              <Sparkles className="h-4 w-4" />
              Strava energy, ClassPass convenience
            </div>
            <p className="max-w-2xl text-base leading-7 text-brand-muted sm:text-lg">Yoga, HIIT, pilates, and climbing classes from the studios around your block. Pay per class or keep a 10-class pack in your pocket.</p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <Button onClick={auth.login} className="justify-center sm:w-auto">
              Login to book
              <ArrowRight className="h-4 w-4" />
            </Button>
            <Button variant="secondary" onClick={auth.register} className="justify-center sm:w-auto">
              Create account
            </Button>
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <StatCard label="Studios" value="40+" trend="Independently owned" />
            <StatCard label="Avg. checkout" value="45s" trend="Stripe Checkout" />
            <StatCard label="Refund policy" value="Studio based" trend="Clear before purchase" />
          </div>
        </div>

        <Card className="relative overflow-hidden p-4 shadow-lg sm:p-6">
          <img src={featuredStudios[0].imageUrl} alt="Yoga class" className="h-72 w-full rounded-xl object-cover" />
          <div className="absolute inset-x-6 bottom-6 rounded-xl bg-white/90 p-4 shadow-lg backdrop-blur">
            <div className="flex items-center justify-between gap-4">
              <div>
                <div className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-muted">Next class near you</div>
                <div className="mt-1 text-lg font-semibold">Sunrise Flow</div>
                <div className="text-sm text-brand-muted">Solstice Yoga · Notting Hill</div>
              </div>
              <Badge tone="success">{currency(24)}</Badge>
            </div>
          </div>
        </Card>
      </div>

      <section className="mt-16 space-y-6">
        <SectionTitle eyebrow="Featured studios" title="Independent spaces with strong personalities" copy="Discover the neighborhood studios people actually return to, not just the ones with the loudest ads." />
        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          {featuredStudios.map((studio) => (
            <Card key={studio.id} className="overflow-hidden transition hover:-translate-y-1 hover:shadow-lg">
              <img src={studio.imageUrl} alt={studio.name} className="h-56 w-full object-cover" />
              <div className="space-y-3 p-5">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <h3 className="text-xl font-semibold">{studio.name}</h3>
                    <p className="text-sm text-brand-muted">{studio.neighborhood} · {studio.discipline}</p>
                  </div>
                  <Badge tone="info">★ {studio.rating}</Badge>
                </div>
                <div className="flex items-center justify-between text-sm text-brand-muted">
                  <span>From {currency(studio.basePrice)}</span>
                  <Link to={`/studios/${studio.slug}`} className="font-semibold text-brand-primary">View studio</Link>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </section>

      <section className="mt-16 grid gap-5 lg:grid-cols-2">
        <Card className="p-6">
          <SectionTitle eyebrow="Top action" title="Book a session" copy="Search by neighborhood, time, discipline, and price. Join a class in a few taps." />
          <div className="mt-5 grid gap-4 sm:grid-cols-[1fr_auto]">
            <Input placeholder="Search yoga in Shoreditch" />
            <Link to="/discover" className="inline-flex h-12 items-center justify-center rounded-xl bg-brand-primary px-4 text-sm font-semibold text-white shadow-md transition hover:bg-indigo-500">
              Search
            </Link>
          </div>
        </Card>
        <Card className="p-6">
          <SectionTitle eyebrow="Pricing" title="10-class pack" copy="A monthly class pack that keeps credits ready for your next booking." />
          <div className="mt-5 flex items-center justify-between">
            <div>
              <div className="text-3xl font-bold">€180</div>
              <div className="text-sm text-brand-muted">10 credits, auto-renew, cancel anytime</div>
            </div>
            <Badge tone="success">Best for regulars</Badge>
          </div>
        </Card>
      </section>
    </PageFrame>
  );
}

export function DiscoveryPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('q') ?? '');
  const { data = [] } = useQuery<SessionSummary[]>({
    queryKey: ['discover-sessions', query],
    queryFn: () => apiFetch(`/public/sessions?q=${encodeURIComponent(query)}`)
  });

  return (
    <PageFrame title="Discover sessions" subtitle="Search live class sessions across the city.">
      <div className="grid gap-5 lg:grid-cols-[320px_1fr]">
        <Card className="p-5">
          <div className="space-y-4">
            <Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search by studio or discipline" />
            <Button className="w-full" onClick={() => setSearchParams(query ? { q: query } : {})}><Search className="h-4 w-4" /> Search</Button>
            <div className="space-y-2 text-sm text-brand-muted">
              <div className="flex items-center gap-2"><MapPin className="h-4 w-4 text-brand-primary" /> Neighbourhood filter ready</div>
              <div className="flex items-center gap-2"><Clock3 className="h-4 w-4 text-brand-primary" /> Morning, lunch, evening</div>
            </div>
          </div>
        </Card>
        <div className="grid gap-4">
          {data.map((session) => (
            <Card key={session.id} className="p-5">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <div className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-muted">{session.neighborhood}</div>
                  <h3 className="text-xl font-semibold">{session.title}</h3>
                  <p className="text-sm text-brand-muted">{session.studioName} · {session.discipline} · {relativeTime(session.startsAt)}</p>
                </div>
                <div className="flex items-center gap-3">
                  <Badge tone={session.capacity - session.bookedCount < 4 ? 'warning' : 'success'}>{session.capacity - session.bookedCount} spots left</Badge>
                  <BookSessionButton sessionId={session.id} label={currency(session.price)} />
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </PageFrame>
  );
}

export function StudioPage() {
  const { slug = 'solstice-yoga' } = useParams();
  const { data: detail } = useQuery<StudioDetail>({
    queryKey: ['studio', slug],
    queryFn: () => apiFetch(`/public/studios/${slug}`)
  });

  return (
    <PageFrame title={detail?.name ?? 'Studio'} subtitle={`${detail?.neighborhood ?? ''} · ${detail?.discipline ?? ''}`}>
      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <Card className="overflow-hidden">
          <img src={detail?.imageUrl ?? featuredStudios[0].imageUrl} alt={detail?.name ?? 'Studio'} className="h-96 w-full object-cover" />
          <div className="p-6">
            <p className="max-w-2xl text-sm leading-7 text-brand-muted">{detail?.description ?? 'Photography-forward space with a warm neighborhood feel and polished instruction.'}</p>
            <div className="mt-5 flex flex-wrap gap-3 text-sm">
              <Badge tone="info">Instructor: {detail?.instructorName ?? 'Mia Torres'}</Badge>
              <Badge tone="success">{detail?.rating ?? featuredStudios[0].rating} rating</Badge>
              <Badge tone="neutral">From {currency(detail?.priceFrom ?? featuredStudios[0].basePrice)}</Badge>
            </div>
          </div>
        </Card>
        <Card className="p-6">
          <SectionTitle title="Upcoming sessions" copy="Book a specific slot. Payment is handled in Stripe Checkout." />
          <div className="mt-5 space-y-4">
            {(detail?.sessions ?? []).map((session) => (
              <div key={session.id} className="flex items-center justify-between rounded-xl border border-slate-200 p-4">
                <div>
                  <div className="font-semibold">{session.title}</div>
                  <div className="text-sm text-brand-muted">{relativeTime(session.startsAt)}</div>
                </div>
                <BookSessionButton sessionId={session.id} />
              </div>
            ))}
          </div>
        </Card>
      </div>
    </PageFrame>
  );
}

export function UserDashboardPage() {
  const auth = useAuth();
  const { data } = useQuery<DashboardResponse>({
    queryKey: ['dashboard'],
    queryFn: () => apiFetch('/dashboard/user', auth.token)
  });

  return (
    <PageFrame title="Your dashboard" subtitle="Track your next class, upcoming bookings, and the studios you keep coming back to.">
      <div className="grid gap-5 lg:grid-cols-3">
        <StatCard label="Credits left" value={String(data?.creditsRemaining ?? 0)} trend="Class pack balance" />
        <StatCard label="Upcoming bookings" value={String(data?.upcomingBookings.length ?? 0)} trend="Your next session is ready" />
        <StatCard label="Recommended studios" value={String(data?.recommendedStudios.length ?? 0)} trend="Based on what you like" />
      </div>
      <div className="mt-6 grid gap-5 lg:grid-cols-[1.15fr_0.85fr]">
        <Card className="p-6">
          <SectionTitle title="Next class" copy="The next session on your calendar is shown here once bookings exist." />
          <div className="mt-5 rounded-xl bg-slate-50 p-5 text-sm text-brand-muted">
            {data?.nextClass ? `${data.nextClass.title} at ${data.nextClass.studioName}` : 'Ready to sweat? Browse studios nearby.'}
          </div>
        </Card>
        <Card className="p-6">
          <SectionTitle title="Primary actions" />
          <div className="mt-5 grid gap-3">
            <ActionCard title="Browse classes" copy="Find a session nearby." icon={<Search className="h-5 w-5" />} href="/discover" />
            <ActionCard title="Manage billing" copy="See receipts, subscriptions, and saved payment method." icon={<Wallet className="h-5 w-5" />} href="/billing" />
          </div>
        </Card>
      </div>
    </PageFrame>
  );
}

export function AdminDashboardPage() {
  const auth = useAuth();
  const [page, setPage] = useState(0);
  const metricsQuery = useQuery<AdminMetrics>({ queryKey: ['admin-metrics'], queryFn: () => apiFetch('/admin/metrics', auth.token) });
  const usersQuery = useQuery<{ content: AdminUser[]; total: number }>({ queryKey: ['admin-users', page], queryFn: () => apiFetch(`/admin/users?page=${page}&size=8`, auth.token) });
  const roleMutation = useMutation({
    mutationFn: ({ userId, role, promote }: { userId: string; role: string; promote: boolean }) => apiFetch(`/admin/users/${userId}/role?promote=${promote}`, auth.token, { method: 'PATCH', body: JSON.stringify({ role }) }),
    onSuccess: () => usersQuery.refetch()
  });

  return (
    <PageFrame title="Admin dashboard" subtitle="Approve studios, manage roles, and watch the platform health at a glance.">
      <div className="grid gap-5 lg:grid-cols-3">
        <StatCard label="Revenue" value={currency(metricsQuery.data?.revenue ?? 0)} trend="Platform commission included" />
        <StatCard label="Bookings" value={String(metricsQuery.data?.bookings ?? 0)} trend="All time" />
        <StatCard label="Pending studios" value={String(metricsQuery.data?.pendingStudios ?? 0)} trend="Approval queue" />
      </div>

      <div className="mt-6 grid gap-5 lg:grid-cols-[1.15fr_0.85fr]">
        <Card className="p-6">
          <SectionTitle title="User management" copy="Pulled from the Keycloak admin API via the backend." />
          <div className="mt-5 space-y-3">
            {(usersQuery.data?.content ?? []).map((user) => (
              <div key={user.id} className="flex items-center justify-between rounded-xl border border-slate-200 p-4 text-sm">
                <div>
                  <div className="font-semibold">{user.username}</div>
                  <div className="text-brand-muted">{user.email}</div>
                </div>
                <div className="flex items-center gap-2">
                  {user.roles.map((role) => <Badge key={role} tone={role === 'admin' ? 'warning' : 'neutral'}>{role}</Badge>)}
                  <Button size="sm" variant="secondary" onClick={() => roleMutation.mutate({ userId: user.id, role: 'admin', promote: true })}>Promote</Button>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-5 flex items-center gap-2">
            <Button variant="secondary" size="sm" onClick={() => setPage(Math.max(0, page - 1))}>Previous</Button>
            <Button variant="secondary" size="sm" onClick={() => setPage(page + 1)}>Next</Button>
          </div>
        </Card>
        <Card className="p-6">
          <SectionTitle title="System metrics" />
          <div className="mt-5 grid gap-3 text-sm text-brand-muted">
            <div className="flex items-center justify-between"><span>Approved studios</span><span>{metricsQuery.data?.approvedStudios ?? 0}</span></div>
            <div className="flex items-center justify-between"><span>Refunds issued</span><span>{metricsQuery.data?.refunds ?? 0}</span></div>
            <div className="flex items-center justify-between"><span>Utilization</span><span>{metricsQuery.data?.utilization ?? 0}%</span></div>
          </div>
        </Card>
      </div>
    </PageFrame>
  );
}

export function BillingPage() {
  const auth = useAuth();
  const { data } = useQuery<BillingSummary>({ queryKey: ['billing'], queryFn: () => apiFetch('/billing/summary', auth.token) });
  const classPackMutation = useMutation({
    mutationFn: () => apiFetch<{ checkoutUrl: string }>('/billing/class-pack/checkout', auth.token, { method: 'POST' }),
    onSuccess: (response) => window.location.assign(response.checkoutUrl)
  });
  const portalMutation = useMutation({
    mutationFn: () => apiFetch<{ url: string }>('/billing/customer-portal', auth.token, { method: 'POST' }),
    onSuccess: (response) => window.location.assign(response.url)
  });

  return (
    <PageFrame title="Billing settings" subtitle="Manage your saved payment method, class pack, and receipts.">
      <div className="grid gap-5 lg:grid-cols-2">
        <Card className="p-6">
          <SectionTitle title="Saved payment method" copy={data?.savedPaymentMethod ?? 'Connected through Stripe Customer Portal.'} />
          <div className="mt-5 flex flex-wrap gap-3">
            <Button onClick={() => classPackMutation.mutate()} disabled={classPackMutation.isPending}>{classPackMutation.isPending ? 'Redirecting...' : 'Buy class pack'}</Button>
            <Button variant="secondary" onClick={() => portalMutation.mutate()} disabled={portalMutation.isPending}>{portalMutation.isPending ? 'Opening...' : 'Customer portal'}</Button>
          </div>
        </Card>
        <Card className="p-6">
          <SectionTitle title="Subscription" copy={data?.subscriptionActive ? '10-class pack active.' : 'No active class pack.'} />
          <div className="mt-5 text-sm text-brand-muted">Credits remaining: {data?.creditsRemaining ?? 0}</div>
        </Card>
      </div>
      <Card className="mt-5 p-6">
        <SectionTitle title="Receipts" />
        <div className="mt-5 space-y-3">
          {(data?.receipts ?? []).map((receipt) => (
            <div key={receipt.id} className="flex items-center justify-between rounded-xl border border-slate-200 p-4 text-sm">
              <div>
                <div className="font-semibold">{receipt.label}</div>
                <div className="text-brand-muted">{relativeTime(receipt.createdAt)}</div>
              </div>
              <Badge tone={receipt.status === 'paid' ? 'success' : 'warning'}>{currency(receipt.amount)}</Badge>
            </div>
          ))}
        </div>
      </Card>
    </PageFrame>
  );
}

export function AccountPage() {
  const auth = useAuth();
  const { data } = useQuery<Profile>({ queryKey: ['profile'], queryFn: () => apiFetch('/account/profile', auth.token) });
  const [displayName, setDisplayName] = useState(data?.displayName ?? auth.userName);
  const [email, setEmail] = useState(data?.email ?? auth.email);
  const [marketingOptIn, setMarketingOptIn] = useState(data?.marketingOptIn ?? false);
  const saveMutation = useMutation({
    mutationFn: () => apiFetch<Profile>('/account/profile', auth.token, { method: 'PATCH', body: JSON.stringify({ displayName, email, marketingOptIn }) }),
    onSuccess: (response) => {
      setDisplayName(response.displayName);
      setEmail(response.email);
      setMarketingOptIn(response.marketingOptIn);
    }
  });

  useEffect(() => {
    if (data) {
      setDisplayName(data.displayName);
      setEmail(data.email);
      setMarketingOptIn(data.marketingOptIn);
    }
  }, [data]);

  return (
    <PageFrame title="Account settings" subtitle="Update your name, email, and GDPR opt-in preferences.">
      <Card className="max-w-2xl p-6">
        <div className="grid gap-4">
          <Input value={displayName} onChange={(event) => setDisplayName(event.target.value)} placeholder="Display name" />
          <Input value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Email" />
          <label className="flex items-center gap-3 text-sm text-brand-muted">
            <input type="checkbox" checked={marketingOptIn} onChange={(event) => setMarketingOptIn(event.target.checked)} />
            I want occasional product updates and offers by email.
          </label>
          <div className="flex gap-3">
            <Button onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>{saveMutation.isPending ? 'Saving...' : 'Save changes'}</Button>
            <Button variant="secondary">Manage password</Button>
          </div>
        </div>
      </Card>
    </PageFrame>
  );
}

export function UnauthorizedPage() {
  const auth = useAuth();
  return (
    <PageFrame title="Login required" subtitle="Book classes and manage your profile after signing in with Keycloak.">
      <div className="max-w-xl space-y-4">
        <Button onClick={auth.login}>Login</Button>
        <Button variant="secondary" onClick={auth.register}>Register</Button>
      </div>
    </PageFrame>
  );
}

export function ForbiddenPage() {
  return (
    <PageFrame title="Access denied" subtitle="Your account does not have permission for this area.">
      <Badge tone="danger">403</Badge>
    </PageFrame>
  );
}

export function NotFoundPage() {
  return (
    <PageFrame title="Page not found" subtitle="The page you were looking for does not exist.">
      <Badge tone="neutral">404</Badge>
    </PageFrame>
  );
}
