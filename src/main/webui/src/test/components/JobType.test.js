import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/svelte/svelte5';
import JobType from '../../lib/JobType.svelte';

describe('JobType component', () => {
  it('should render "A" for APPLICATION type', () => {
    const { getByText, container } = render(JobType, { props: { type: 'APPLICATION' } });

    expect(getByText('A')).toBeInTheDocument();

    const paragraph = container.querySelector('p');
    expect(paragraph).toHaveAttribute('title', 'Type: APPLICATION');
  });

  it('should render "S" for SESSION type', () => {
    const { getByText, container } = render(JobType, { props: { type: 'SESSION' } });

    expect(getByText('S')).toBeInTheDocument();

    const paragraph = container.querySelector('p');
    expect(paragraph).toHaveAttribute('title', 'Type: SESSION');
  });

  it('should have correct CSS classes', () => {
    const { container } = render(JobType, { props: { type: 'APPLICATION' } });

    const paragraph = container.querySelector('p');
    expect(paragraph).toHaveClass('ml-1', 'px-1', 'border', 'border-gray-500', 'rounded', 'bg-white');
  });

  it('should render nothing for unknown type', () => {
    const { container } = render(JobType, { props: { type: 'UNKNOWN' } });

    const paragraph = container.querySelector('p');
    expect(paragraph).toBeInTheDocument();
    expect(paragraph.textContent.trim()).toBe('');
  });

  it('should handle null type gracefully', () => {
    const { container } = render(JobType, { props: { type: null } });

    const paragraph = container.querySelector('p');
    expect(paragraph).toBeInTheDocument();
  });

  it('should update when type prop changes', async () => {
    const { getByText, component, rerender } = render(JobType, { props: { type: 'APPLICATION' } });

    expect(getByText('A')).toBeInTheDocument();

    // Update the prop
    await component.$set({ type: 'SESSION' });

    expect(getByText('S')).toBeInTheDocument();
  });
});
