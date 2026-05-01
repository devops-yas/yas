import { formatPrice } from '@/utils/formatPrice';

describe('formatPrice', () => {
  it('formats USD currency with cents', () => {
    expect(formatPrice(1234.5)).toBe('$1,234.50');
  });

  it('formats zero as $0.00', () => {
    expect(formatPrice(0)).toBe('$0.00');
  });
});
