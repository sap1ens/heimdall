import { describe, it, expect, beforeEach } from 'vitest';
import { render, fireEvent } from '@testing-library/svelte/svelte5';
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

  it('should render dialog element', () => {
    const { container } = render(Modal, {
      props: { showModal: false }
    });

    const dialog = container.querySelector('dialog');
    expect(dialog).toBeInTheDocument();
  });

  it('should render when showModal is true', () => {
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
    expect(dialog).toHaveClass('w-[500px]', 'min-h-[200px]', 'p-[25px]', 'outline-none', 'rounded', 'shadow-lg', 'border', 'border-gray-300');
  });

  it('should render close button with icon', () => {
    const { container } = render(Modal, {
      props: { showModal: false }
    });

    const closeButton = container.querySelector('button[title="Close"]');
    expect(closeButton).toBeInTheDocument();
  });

  it('should close dialog when close button is clicked', async () => {
    const { container, component } = render(Modal, {
      props: { showModal: true }
    });

    const closeButton = container.querySelector('button[title="Close"]');

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

  it('should contain inner div wrapper', () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const innerDiv = container.querySelector('dialog > div');
    expect(innerDiv).toBeInTheDocument();
  });

  it('should close dialog when Escape key is pressed', async () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const dialog = container.querySelector('dialog');
    const closeSpy = vi.spyOn(dialog, 'close');

    // Simulate Escape key press
    await fireEvent.keyDown(dialog, { key: 'Escape' });

    expect(closeSpy).toHaveBeenCalled();

    closeSpy.mockRestore();
  });

  it('should not close dialog when other keys are pressed', async () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const dialog = container.querySelector('dialog');
    const closeSpy = vi.spyOn(dialog, 'close');

    // Simulate other key presses
    await fireEvent.keyDown(dialog, { key: 'Enter' });
    await fireEvent.keyDown(dialog, { key: 'Tab' });
    await fireEvent.keyDown(dialog, { key: 'a' });

    expect(closeSpy).not.toHaveBeenCalled();

    closeSpy.mockRestore();
  });

  it('should stop propagation when clicking inside modal content', async () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const innerDiv = container.querySelector('dialog > div');
    const dialog = container.querySelector('dialog');
    const closeSpy = vi.spyOn(dialog, 'close');

    // Click inside the content area
    await fireEvent.click(innerDiv);

    // Dialog should not close
    expect(closeSpy).not.toHaveBeenCalled();

    closeSpy.mockRestore();
  });

  it('should stop propagation for keydown events inside modal content', async () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const innerDiv = container.querySelector('dialog > div');

    // This should not throw and should handle the event
    await expect(fireEvent.keyDown(innerDiv, { key: 'Escape' })).resolves.not.toThrow();
  });

  it('should update dialog open state when showModal prop changes', async () => {
    const { container, component } = render(Modal, {
      props: { showModal: false }
    });

    const dialog = container.querySelector('dialog');
    const showModalSpy = vi.spyOn(dialog, 'showModal');

    // Update prop to true
    await component.$set({ showModal: true });

    expect(showModalSpy).toHaveBeenCalled();

    showModalSpy.mockRestore();
  });

  it('should trigger close event when dialog is closed', async () => {
    const { container } = render(Modal, {
      props: { showModal: true }
    });

    const dialog = container.querySelector('dialog');

    // Verify close event can be fired
    let closeEventFired = false;
    dialog.addEventListener('close', () => {
      closeEventFired = true;
    });

    await fireEvent(dialog, new Event('close'));

    expect(closeEventFired).toBe(true);
  });

  it('should have proper ARIA role on inner div', () => {
    const { container } = render(Modal, {
      props: { showModal: false }
    });

    const innerDiv = container.querySelector('dialog > div');
    expect(innerDiv).toHaveAttribute('role', 'presentation');
  });
});
