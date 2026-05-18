import React from 'react';
import { AlertTriangle } from 'lucide-react';
import { Button } from './ui';

type Props = { children: React.ReactNode };

type State = { hasError: boolean };

export class ErrorBoundary extends React.Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen grid place-items-center p-6">
          <div className="max-w-md pulse-surface p-8 text-center space-y-4">
            <AlertTriangle className="mx-auto h-12 w-12 text-brand-danger" />
            <h1 className="text-3xl font-bold">Something went wrong</h1>
            <p className="text-sm text-brand-muted">PulseFit hit an unexpected error. Refresh the page to try again.</p>
            <Button onClick={() => window.location.reload()}>Refresh</Button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
