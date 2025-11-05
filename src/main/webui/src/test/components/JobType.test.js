import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/svelte/svelte5';
import JobType from '../../lib/JobType.svelte';

describe('JobType component', () => {
  it('should render "APP" for APPLICATION type', () => {
    const { getByText, container } = render(JobType, { props: { type: 'APPLICATION' } });

    expect(getByText('APP')).toBeInTheDocument();

    const span = container.querySelector('span');
    expect(span).toHaveAttribute('title', 'Application Job');
  });

  it('should render "SESSION" for SESSION type', () => {
    const { getByText, container } = render(JobType, { props: { type: 'SESSION' } });

    expect(getByText('SESSION')).toBeInTheDocument();

    const span = container.querySelector('span');
    expect(span).toHaveAttribute('title', 'Session Job');
  });

  it('should have correct CSS classes for APPLICATION', () => {
    const { container } = render(JobType, { props: { type: 'APPLICATION' } });

    const span = container.querySelector('span');
    expect(span).toHaveClass('px-2', 'py-1', 'rounded', 'text-xs', 'font-semibold', 'bg-blue-100', 'text-blue-800', 'border', 'border-blue-300');
  });

  it('should render nothing for unknown type', () => {
    const { container } = render(JobType, { props: { type: 'UNKNOWN' } });

    const span = container.querySelector('span');
    expect(span).toBeNull();
  });

  it('should handle null type gracefully', () => {
    const { container } = render(JobType, { props: { type: null } });

    const span = container.querySelector('span');
    expect(span).toBeNull();
  });

  it('should update when type prop changes', async () => {
    const { getByText, component } = render(JobType, { props: { type: 'APPLICATION' } });

    expect(getByText('APP')).toBeInTheDocument();

    // Update the prop
    component.type = 'SESSION';
    await component.$set({ type: 'SESSION' });

    expect(getByText('SESSION')).toBeInTheDocument();
  });
});
