import { useState, useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { wishlistAPI } from '../lib/api/wishlist';

interface UseWishlistOptions {
  productId?: string;
  enabled?: boolean;
}

export const useWishlist = (options: UseWishlistOptions = {}) => {
  const { productId, enabled = true } = options;
  const queryClient = useQueryClient();
  const [isWishlisted, setIsWishlisted] = useState(false);

  const { data: isInWishlist } = useQuery({
    queryKey: ['wishlist-check', productId],
    queryFn: () => wishlistAPI.checkIfInWishlist(productId!),
    enabled: !!productId && !!localStorage.getItem('token') && enabled,
    staleTime: 30 * 1000,
  });

  const { data: wishlistData, isLoading: isLoadingWishlist } = useQuery({
    queryKey: ['wishlist'],
    queryFn: () => wishlistAPI.getUserWishlist(),
    enabled: !!localStorage.getItem('token') && enabled,
    staleTime: 2 * 60 * 1000,
  });

  const { data: wishlistCount } = useQuery({
    queryKey: ['wishlist-count'],
    queryFn: () => wishlistAPI.getWishlistCount(),
    enabled: !!localStorage.getItem('token') && enabled,
    staleTime: 30 * 1000,
  });

  const addToWishlistMutation = useMutation({
    mutationFn: (prodId: string) => wishlistAPI.addToWishlist(prodId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['wishlist'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-check'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-count'] });
      if (productId) {
        setIsWishlisted(true);
      }
    },
  });

  const removeFromWishlistMutation = useMutation({
    mutationFn: (prodId: string) => wishlistAPI.removeFromWishlist(prodId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['wishlist'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-check'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-count'] });
      if (productId) {
        setIsWishlisted(false);
      }
    },
  });

  useEffect(() => {
    if (productId && isInWishlist !== undefined) {
      setIsWishlisted(isInWishlist);
    }
  }, [isInWishlist, productId]);

  const toggleWishlist = async (prodId: string) => {
    if (!localStorage.getItem('token')) {
      console.log('Please log in to add items to your wishlist');
      return;
    }

    try {
      const isCurrentlyWishlisted = prodId === productId ? isWishlisted : false;
      
      if (isCurrentlyWishlisted) {
        await removeFromWishlistMutation.mutateAsync(prodId);
      } else {
        await addToWishlistMutation.mutateAsync(prodId);
      }
    } catch (error) {
      console.error('Failed to toggle wishlist:', error);
      throw error;
    }
  };

  const addToWishlist = async (prodId: string) => {
    if (!localStorage.getItem('token')) {
      console.log('Please log in to add items to your wishlist');
      return;
    }

    try {
      await addToWishlistMutation.mutateAsync(prodId);
    } catch (error) {
      console.error('Failed to add to wishlist:', error);
      throw error;
    }
  };

  const removeFromWishlist = async (prodId: string) => {
    if (!localStorage.getItem('token')) {
      console.log('Please log in to manage your wishlist');
      return;
    }

    try {
      await removeFromWishlistMutation.mutateAsync(prodId);
    } catch (error) {
      console.error('Failed to remove from wishlist:', error);
      throw error;
    }
  };

  const checkIsInWishlist = (prodId: string): boolean => {
    if (!wishlistData?.content) return false;
    return wishlistData.content.some(item => item.productId === prodId);
  };

  return {
    isWishlisted,
    wishlistItems: wishlistData?.content || [],
    wishlistCount: wishlistCount || 0,
    isLoadingWishlist,
    
    isAddingToWishlist: addToWishlistMutation.isPending,
    isRemovingFromWishlist: removeFromWishlistMutation.isPending,
    isTogglingWishlist: addToWishlistMutation.isPending || removeFromWishlistMutation.isPending,
    
    toggleWishlist,
    addToWishlist,
    removeFromWishlist,
    checkIsInWishlist,
  };
};
