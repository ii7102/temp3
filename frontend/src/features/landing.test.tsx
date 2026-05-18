import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { AuthContext } from '../auth/AuthProvider';
import { LandingPage } from './pages';

describe('LandingPage', () => {
  it('renders login actions that call the Keycloak entry points', async () => {
    const user = userEvent.setup();
    const login = vi.fn();
    const register = vi.fn();

    render(
      <AuthContext.Provider
        value={{
          ready: true,
          authenticated: false,
          token: null,
          userName: 'Guest',
          email: '',
          roles: [],
          login,
          register,
          logout: vi.fn(),
          hasRole: () => false
        }}
      >
        <MemoryRouter>
          <LandingPage />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Find your flow, one class at a time.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /login to book/i }));
    await user.click(screen.getByRole('button', { name: /create account/i }));
    expect(login).toHaveBeenCalled();
    expect(register).toHaveBeenCalled();
  });
});
