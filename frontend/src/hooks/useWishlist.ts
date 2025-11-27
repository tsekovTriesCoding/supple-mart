import { useState, useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

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
    mutationFn: async (data: { prodId: string; productName?: string }) => {
      await wishlistAPI.addToWishlist(data.prodId);
      return data.productName;
    },
    onSuccess: (productName) => {
      queryClient.invalidateQueries({ queryKey: ['wishlist'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-check'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-count'] });
      if (productId) {
        setIsWishlisted(true);
      }
      
      const message = productName 
        ? `${productName} added to wishlist`
        : 'Added to wishlist';
      toast.success(message);
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to add to wishlist'
        : 'Failed to add to wishlist';
      toast.error(message);
    },
  });

  const removeFromWishlistMutation = useMutation({
    mutationFn: async (data: { prodId: string; productName?: string }) => {
      await wishlistAPI.removeFromWishlist(data.prodId);
      return data.productName;
    },
    onSuccess: (productName) => {
      queryClient.invalidateQueries({ queryKey: ['wishlist'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-check'] });
      queryClient.invalidateQueries({ queryKey: ['wishlist-count'] });
      if (productId) {
        setIsWishlisted(false);
      }
      
      const message = productName
        ? `${productName} removed from wishlist`
        : 'Removed from wishlist';
      toast.success(message);
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to remove from wishlist'
        : 'Failed to remove from wishlist';
      toast.error(message);
    },
  });

  useEffect(() => {
    if (productId && isInWishlist !== undefined) {
      setIsWishlisted(isInWishlist);
    }
  }, [isInWishlist, productId]);

  const checkIsInWishlist = (prodId: string): boolean => {
    if (!wishlistData?.content) return false;
    return wishlistData.content.some(item => item.productId === prodId);
  };

  const toggleWishlist = async (prodId: string, productNameFromCaller?: string) => {
    if (!localStorage.getItem('token')) {
      toast.error('Please log in to add items to your wishlist');
      return;
    }

    const isCurrentlyWishlisted = checkIsInWishlist(prodId);
    const item = wishlistData?.content.find(w => w.productId === prodId);
    const productName = productNameFromCaller ?? item?.productName;

    if (isCurrentlyWishlisted) {
      await removeFromWishlistMutation.mutateAsync({ prodId, productName });
    } else {
      await addToWishlistMutation.mutateAsync({ prodId, productName });
    }
  };

  const addToWishlist = async (prodId: string, productName?: string) => {
    if (!localStorage.getItem('token')) {
      toast.error('Please log in to add items to your wishlist');
      return;
    }

    await addToWishlistMutation.mutateAsync({ prodId, productName });
  };

  const removeFromWishlist = async (prodId: string) => {
    if (!localStorage.getItem('token')) {
      toast.error('Please log in to manage your wishlist');
      return;
    }

    const item = wishlistData?.content.find(w => w.productId === prodId);
    const productName = item?.productName;

    await removeFromWishlistMutation.mutateAsync({ prodId, productName });
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
