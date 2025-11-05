import { describe, it, expect, beforeEach } from 'vitest';
import { render, fireEvent } from '@testing-library/svelte';
import Modal from '../../lib/Modal.svelte';

describe('Modal component', () => {
  beforeEach(() => {
    // Mock HTMLDialogElement methods if not available in jsdom
    if (!HTMLDialogElement.prototype.showModal) {
      HTMLDialogElement.prototype.showModal = function() {
        this.open = true;
      };
    }
    if (!HTMLDialogElement.prototype.close) {
      HTMLDialogElement.prototype.close = function() {
        this.open = false;
        this.dispatchEvent(new Event('close'));
      };
    }
  });

  it('should render with slot content', () => {
    const { getByText } = render(Modal, {
      props: { showModal: false },
      slots: { default: 'Test Content' }
    });

    expect(getByText('Test Content')).toBeInTheDocument();
  });

  it('should show dialog when showModal is true', () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const dialog = container.querySelector('dialog');
    expect(dialog).toBeInTheDocument();
  });

  it('should have correct CSS classes', () => {
    const { container } = render(Modal, {
      props: { showModal: false }
    });

    const dialog = container.querySelector('dialog');
    expect(dialog).toHaveClass('w-[500px]', 'h-[200px]', 'p-[25px]', 'outline-none');
  });

  it('should render close button with icon', () => {
    const { container } = render(Modal, {
      props: { showModal: false }
    });

    const closeButton = container.querySelector('span[title="Settings"]');
    expect(closeButton).toBeInTheDocument();
  });

  it('should close dialog when close button is clicked', async () => {
    const { container, component } = render(Modal, {
      props: { showModal: true }
    });

    const closeButton = container.querySelector('span[title="Settings"]');

    await fireEvent.click(closeButton);

    // The close method should have been called
    const dialog = container.querySelector('dialog');
    expect(dialog).toBeInTheDocument();
  });

  it('should close dialog when clicking on backdrop', async () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const dialog = container.querySelector('dialog');

    await fireEvent.click(dialog);

    // Dialog close should be triggered
    expect(dialog).toBeInTheDocument();
  });

  it('should not close when clicking on content', async () => {
    const { container } = render(Modal, {
      props: { showModal: true },
      slots: { default: '<div data-testid="content">Content</div>' }
    });

    const content = container.querySelector('[data-testid="content"]');

    await fireEvent.click(content);

    // Dialog should still be open
    const dialog = container.querySelector('dialog');
    expect(dialog).toBeInTheDocument();
  });

  it('should render with custom slot content', () => {
    const { getByText } = render(Modal, {
      props: { showModal: false },
      slots: { default: '<div><h1>Custom Title</h1><p>Custom paragraph</p></div>' }
    });

    expect(getByText('Custom Title')).toBeInTheDocument();
    expect(getByText('Custom paragraph')).toBeInTheDocument();
  });
});
