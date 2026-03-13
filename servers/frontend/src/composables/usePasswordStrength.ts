import { computed, type Ref, type ComputedRef, unref } from 'vue';

export interface PasswordStrengthLevel {
  level: number;
  label: string;
  color: string;
  textColor: string;
}

const LEVELS: PasswordStrengthLevel[] = [
  { level: 1, label: 'Weak', color: 'bg-red-400', textColor: 'text-red-500' },
  { level: 2, label: 'Fair', color: 'bg-amber-400', textColor: 'text-amber-500' },
  { level: 3, label: 'Good', color: 'bg-blue-400', textColor: 'text-blue-500' },
  { level: 4, label: 'Strong', color: 'bg-emerald-400', textColor: 'text-emerald-500' },
];

const EMPTY: PasswordStrengthLevel = { level: 0, label: '', color: '', textColor: '' };

export function usePasswordStrength(password: Ref<string> | ComputedRef<string>) {
  const strength = computed<PasswordStrengthLevel>(() => {
    const p = unref(password);
    if (!p) return EMPTY;

    let score = 0;
    if (p.length >= 6) score++;
    if (p.length >= 10) score++;
    if (/[A-Z]/.test(p) && /[a-z]/.test(p)) score++;
    if (/[0-9]/.test(p) && /[^a-zA-Z0-9]/.test(p)) score++;

    return LEVELS[Math.min(score, 4) - 1] || LEVELS[0];
  });

  const isStrongEnough = computed(() => strength.value.level >= 2);

  return { strength, isStrongEnough };
}
