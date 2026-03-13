import { reactive } from 'vue';

export interface ConfirmDialogState {
  show: boolean;
  title: string;
  message: string;
  confirmText: string;
  variant: string;        // 'danger' | 'warning' | 'success' | 'primary'
  loading: boolean;
  onConfirm: () => void;
}

/**
 * Generic confirm-dialog state manager.
 *
 * Works with both `<ConfirmDialog>` (warehouse views) and `<ConfirmModal>` (event planner views).
 * For ConfirmModal, map `variant` → `confirmClass` in the template if needed.
 */
export function useConfirmDialog() {
  const state = reactive<ConfirmDialogState>({
    show: false,
    title: '',
    message: '',
    confirmText: 'Confirm',
    variant: 'danger',
    loading: false,
    onConfirm: () => {},
  });

  function open(opts: {
    title: string;
    message: string;
    confirmText?: string;
    variant?: string;
    onConfirm: () => Promise<void> | void;
  }) {
    state.title = opts.title;
    state.message = opts.message;
    state.confirmText = opts.confirmText ?? 'Confirm';
    state.variant = opts.variant ?? 'danger';
    state.loading = false;
    state.onConfirm = async () => {
      state.loading = true;
      try {
        await opts.onConfirm();
      } finally {
        state.loading = false;
        state.show = false;
      }
    };
    state.show = true;
  }

  function close() {
    state.show = false;
    state.loading = false;
  }

  return { state, open, close };
}
