import { concatQueryString } from '@/utils/concatQueryString';

describe('concatQueryString', () => {
  it('returns base url when array is empty', () => {
    expect(concatQueryString([], '/products')).toBe('/products');
  });

  it('appends query params in order', () => {
    const result = concatQueryString(['page=1', 'size=20'], '/products');
    expect(result).toBe('/products?page=1&size=20');
  });

  it('appends a single param with question mark', () => {
    const result = concatQueryString(['q=shoes'], '/search');
    expect(result).toBe('/search?q=shoes');
  });
});
