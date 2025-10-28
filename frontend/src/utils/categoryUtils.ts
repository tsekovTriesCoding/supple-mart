export const Category = {
  PROTEIN: 'PROTEIN',
  VITAMINS: 'VITAMINS',
  MINERALS: 'MINERALS',
  AMINO_ACIDS: 'AMINO_ACIDS',
  CREATINE: 'CREATINE',
  PRE_WORKOUT: 'PRE_WORKOUT',
  POST_WORKOUT: 'POST_WORKOUT',
  WEIGHT_LOSS: 'WEIGHT_LOSS',
  MASS_GAINER: 'MASS_GAINER',
  OMEGA_3: 'OMEGA_3',
  OTHER: 'OTHER'
} as const;

export type CategoryType = typeof Category[keyof typeof Category];

export const formatCategoryForDisplay = (category: string): string => {
  if (!category || category === 'all') return 'All';
  
  const dashCategories = ['pre_workout', 'post_workout', 'weight_loss'];
  
  const words = category
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase());
  
  if (dashCategories.includes(category.toLowerCase())) {
    return words.join('-');
  } else {
    return words.join(' ');
  }
};

export const formatCategoryForBackend = (category: string): string => {
  if (!category || category === 'all' || category === 'All') return '';
  
  return category
    .trim()
    .replace(/\s+/g, '_')
    .toUpperCase();
};

export const formatCategoryForUrl = (category: string): string => {
  if (!category || category === 'all' || category === 'All') return '';
  
  return category
    .trim()
    .toLowerCase()
    .replace(/_/g, '-')
    .replace(/\s+/g, '-');
};

export const formatCategoryFromUrl = (urlCategory: string): string => {
  if (!urlCategory || urlCategory === 'all') return 'All';
  
  return urlCategory
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

export const backendCategoryToUrl = (backendCategory: string): string => {
  if (!backendCategory) return '';
  
  return backendCategory
    .toLowerCase()
    .replace(/_/g, '-');
};

export const urlCategoryToBackend = (urlCategory: string): string => {
  if (!urlCategory || urlCategory === 'all') return '';
  
  return urlCategory
    .toUpperCase()
    .replace(/-/g, '_');
};

export const getAllCategories = (): CategoryType[] => {
  return Object.values(Category);
};

export const getAllCategoriesForDisplay = (): string[] => {
  return Object.values(Category).map(formatCategoryForDisplay);
};

export const getAllCategoriesForUrl = (): string[] => {
  return Object.values(Category).map(backendCategoryToUrl);
};