import { cva, type VariantProps } from 'class-variance-authority';
import { forwardRef } from 'react';
import { twMerge } from 'tailwind-merge';

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 rounded-xl px-4 py-3 text-sm font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
  {
    variants: {
      variant: {
        primary: 'bg-brand-primary text-white shadow-md hover:bg-indigo-500',
        secondary: 'bg-white text-brand-text border border-slate-200 hover:border-brand-primary/40 hover:text-brand-primary',
        ghost: 'bg-transparent text-brand-text hover:bg-slate-100',
        danger: 'bg-brand-danger text-white hover:bg-red-500'
      },
      size: {
        md: 'h-12',
        sm: 'h-10 px-3 text-xs'
      }
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md'
    }
  }
);

export type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & VariantProps<typeof buttonVariants>;

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(({ className, variant, size, ...props }, ref) => (
  <button ref={ref} className={twMerge(buttonVariants({ variant, size }), className)} {...props} />
));
Button.displayName = 'Button';

export function Card({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={twMerge('rounded-xl border border-slate-200 bg-white shadow-sm', className)} {...props} />;
}

export function Badge({ className, tone = 'neutral', ...props }: React.HTMLAttributes<HTMLSpanElement> & { tone?: 'neutral' | 'success' | 'warning' | 'info' | 'danger' }) {
  const tones = {
    neutral: 'bg-slate-100 text-slate-700',
    success: 'bg-emerald-100 text-emerald-700',
    warning: 'bg-amber-100 text-amber-800',
    info: 'bg-indigo-100 text-indigo-700',
    danger: 'bg-red-100 text-red-700'
  };
  return <span className={twMerge('inline-flex items-center rounded-full px-3 py-1 text-xs font-medium', tones[tone], className)} {...props} />;
}

export function Input({ className, ...props }: React.InputHTMLAttributes<HTMLInputElement>) {
  return <input className={twMerge('h-12 w-full rounded-xl border border-slate-200 bg-white px-4 text-sm shadow-sm outline-none transition placeholder:text-slate-400 focus:border-brand-primary focus:ring-2 focus:ring-brand-primary/15', className)} {...props} />;
}

export function Avatar({ initials, className }: { initials: string; className?: string }) {
  return <div className={twMerge('grid h-10 w-10 place-items-center rounded-full bg-indigo-100 text-sm font-bold text-brand-primary', className)}>{initials}</div>;
}

export function StatCard({ label, value, trend, className }: { label: string; value: string; trend?: string; className?: string }) {
  return (
    <Card className={twMerge('p-5', className)}>
      <div className="text-sm text-brand-muted">{label}</div>
      <div className="mt-2 text-3xl font-bold tracking-tight">{value}</div>
      {trend ? <div className="mt-2 text-xs font-medium text-emerald-600">{trend}</div> : null}
    </Card>
  );
}
