import { Edit, Plus, Search, Trash2, Upload, X } from 'lucide-react';
import { useCallback, useEffect, useReducer } from 'react';

import { Pagination } from '../../components/Pagination';
import { adminAPI } from '../../lib/api/admin';
import type { AdminProduct } from '../../types/admin';
import type { ApiError } from '../../types/error';
import { useProductCategories } from '../../hooks/useProducts';
import { 
  formatCategoryForDisplay, 
  formatCategoryForUrl, 
  urlCategoryToBackend 
} from '../../utils/categoryUtils';
import { adminProductsReducer, initialState } from '../../reducers/adminProductsReducer';

const AdminProducts = () => {
  const [state, dispatch] = useReducer(adminProductsReducer, initialState);

  const { data: categoriesData } = useProductCategories();
  const categories = ['all', ...(categoriesData || [])];

  const loadProducts = useCallback(async () => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      const response = await adminAPI.getAllProducts({
        page: state.currentPage,
        limit: 10,
        search: state.searchQuery || undefined,
        category: state.selectedCategory !== 'all' 
          ? urlCategoryToBackend(formatCategoryForUrl(state.selectedCategory))
          : undefined,
      });
      dispatch({
        type: 'SET_PRODUCTS',
        payload: { products: response.products, totalPages: response.totalPages },
      });
    } catch (err) {
      const apiError = err as ApiError;
      dispatch({ 
        type: 'SET_ERROR', 
        payload: apiError.response?.data?.message || 'Failed to load products' 
      });
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, [state.currentPage, state.searchQuery, state.selectedCategory]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      dispatch({ type: 'SET_ERROR', payload: 'Please select an image file' });
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      dispatch({ type: 'SET_ERROR', payload: 'Image size should be less than 5MB' });
      return;
    }

    try {
      dispatch({ type: 'SET_UPLOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      const imageUrl = await adminAPI.uploadProductImage(file);
      dispatch({ type: 'UPDATE_FORM_DATA', payload: { imageUrl } });
    } catch (err) {
      const apiError = err as ApiError;
      dispatch({ 
        type: 'SET_ERROR', 
        payload: apiError.response?.data?.message || 'Failed to upload image' 
      });
    } finally {
      dispatch({ type: 'SET_UPLOADING', payload: false });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      dispatch({ type: 'SET_ERROR', payload: null });
      if (state.editingProduct) {
        await adminAPI.updateProduct(state.editingProduct.id, state.formData);
      } else {
        await adminAPI.createProduct(state.formData);
      }
      
      dispatch({ type: 'CLOSE_MODAL' });
      loadProducts();
    } catch (err) {
      const apiError = err as ApiError;
      dispatch({ 
        type: 'SET_ERROR', 
        payload: apiError.response?.data?.message || 'Failed to save product' 
      });
    }
  };

  const handleEdit = (product: AdminProduct) => {
    dispatch({ type: 'START_EDIT', payload: product });
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this product?')) return;

    try {
      dispatch({ type: 'SET_ERROR', payload: null });
      await adminAPI.deleteProduct(id);
      loadProducts();
    } catch (err) {
      const apiError = err as ApiError;
      dispatch({ 
        type: 'SET_ERROR', 
        payload: apiError.response?.data?.message || 'Failed to delete product' 
      });
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-white">Products Management</h2>
        <button
          onClick={() => dispatch({ type: 'OPEN_MODAL' })}
          className="btn-primary flex items-center space-x-2"
        >
          <Plus size={20} />
          <span>Add Product</span>
        </button>
      </div>

      {state.error && (
        <div className="mb-4 p-4 bg-red-900/20 border border-red-900 rounded-lg text-red-400">
          {state.error}
        </div>
      )}

      <div className="card p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
              <input
                type="text"
                placeholder="Search products..."
                value={state.searchQuery}
                onChange={(e) => dispatch({ type: 'SET_SEARCH_QUERY', payload: e.target.value })}
                className="input w-full"
                style={{ paddingLeft: '3rem', paddingRight: '1rem' }}
              />
            </div>
          </div>
          <select
            value={state.selectedCategory}
            onChange={(e) => dispatch({ type: 'SET_SELECTED_CATEGORY', payload: e.target.value })}
            className="input"
          >
            {categories.map((cat) => (
              <option key={cat} value={cat}>
                {formatCategoryForDisplay(cat)}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-800">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                  Image
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                  Category
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                  Price
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                  Stock
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {state.isLoading ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-gray-400">
                    Loading...
                  </td>
                </tr>
              ) : state.products.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-gray-400">
                    No products found
                  </td>
                </tr>
              ) : (
                state.products.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-800/50">
                    <td className="px-6 py-4">
                      <img
                        src={product.imageUrl || 'https://via.placeholder.com/50'}
                        alt={product.name}
                        className="w-12 h-12 object-cover rounded"
                      />
                    </td>
                    <td className="px-6 py-4 text-white font-medium">{product.name}</td>
                    <td className="px-6 py-4 text-gray-400">
                      {formatCategoryForDisplay(product.category)}
                    </td>
                    <td className="px-6 py-4 text-white">
                      ${product.price.toFixed(2)}
                      {product.originalPrice && product.originalPrice > product.price && (
                        <span className="ml-2 text-sm text-gray-500 line-through">
                          ${product.originalPrice.toFixed(2)}
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          (product.stockQuantity || 0) > 10
                            ? 'bg-green-900/30 text-green-400'
                            : (product.stockQuantity || 0) > 0
                            ? 'bg-yellow-900/30 text-yellow-400'
                            : 'bg-red-900/30 text-red-400'
                        }`}
                      >
                        {product.stockQuantity || 0}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`px-3 py-1 text-xs rounded-full ${
                          product.active
                            ? 'bg-green-900/30 text-green-400'
                            : 'bg-gray-900/30 text-gray-400'
                        }`}
                      >
                        {product.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEdit(product)}
                          className="p-2 text-blue-400 hover:bg-blue-900/20 rounded"
                        >
                          <Edit size={18} />
                        </button>
                        <button
                          onClick={() => handleDelete(product.id)}
                          className="p-2 text-red-400 hover:bg-red-900/20 rounded"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {state.totalPages > 1 && (
          <div className="p-4 border-t border-gray-800">
            <Pagination
              currentPage={state.currentPage}
              totalPages={state.totalPages}
              onPageChange={(page) => dispatch({ type: 'SET_CURRENT_PAGE', payload: page })}
            />
          </div>
        )}
      </div>

      {state.showModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-gray-900 rounded-2xl p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-2xl font-bold text-white">
                {state.editingProduct ? 'Edit Product' : 'Add New Product'}
              </h3>
              <button onClick={() => dispatch({ type: 'CLOSE_MODAL' })} className="text-gray-400 hover:text-white cursor-pointer">
                <X size={24} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Product Image
                </label>
                <div className="flex items-center space-x-4">
                  {state.formData.imageUrl && (
                    <img
                      src={state.formData.imageUrl}
                      alt="Preview"
                      className="w-24 h-24 object-cover rounded"
                    />
                  )}
                  <label className="flex-1 cursor-pointer">
                    <div className="flex items-center justify-center space-x-2 px-4 py-2 border border-gray-700 rounded-lg hover:border-blue-500 transition-colors">
                      <Upload size={20} className="text-gray-400" />
                      <span className="text-gray-400">
                        {state.uploading ? 'Uploading...' : 'Upload Image'}
                      </span>
                    </div>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageUpload}
                      className="hidden"
                      disabled={state.uploading}
                    />
                  </label>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Product Name *
                </label>
                <input
                  type="text"
                  value={state.formData.name}
                  onChange={(e) => dispatch({ type: 'UPDATE_FORM_DATA', payload: { name: e.target.value } })}
                  className="input w-full"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Description *
                </label>
                <textarea
                  value={state.formData.description}
                  onChange={(e) => dispatch({ type: 'UPDATE_FORM_DATA', payload: { description: e.target.value } })}
                  className="input w-full h-24"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Category *
                </label>
                <select
                  value={state.formData.category}
                  onChange={(e) => dispatch({ type: 'UPDATE_FORM_DATA', payload: { category: e.target.value } })}
                  className="input w-full"
                  required
                >
                  <option value="">Select category</option>
                  {categories.filter((c) => c !== 'all').map((cat) => (
                    <option key={cat} value={urlCategoryToBackend(formatCategoryForUrl(cat))}>
                      {formatCategoryForDisplay(cat)}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">
                    Price *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={state.formData.price}
                    onChange={(e) => dispatch({ type: 'UPDATE_FORM_DATA', payload: { price: parseFloat(e.target.value) } })}
                    className="input w-full"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">
                    Original Price (Optional)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={state.formData.originalPrice}
                    onChange={(e) =>
                      dispatch({ type: 'UPDATE_FORM_DATA', payload: { originalPrice: parseFloat(e.target.value) } })
                    }
                    className="input w-full"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Stock Quantity *
                </label>
                <input
                  type="number"
                  value={state.formData.stockQuantity}
                  onChange={(e) =>
                    dispatch({ type: 'UPDATE_FORM_DATA', payload: { stockQuantity: parseInt(e.target.value) || 0 } })
                  }
                  className="input w-full"
                  required
                />
              </div>

              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  id="isActive"
                  checked={state.formData.isActive}
                  onChange={(e) => dispatch({ type: 'UPDATE_FORM_DATA', payload: { isActive: e.target.checked } })}
                  className="w-4 h-4 text-blue-600 bg-gray-700 border-gray-600 rounded focus:ring-blue-500 focus:ring-2"
                />
                <label htmlFor="isActive" className="text-sm font-medium text-gray-400">
                  Active (Product is visible to customers)
                </label>
              </div>

              <div className="flex justify-end space-x-4 pt-4">
                <button
                  type="button"
                  onClick={() => dispatch({ type: 'CLOSE_MODAL' })}
                  className="px-6 py-2 border border-gray-700 text-gray-400 rounded-lg hover:bg-gray-800"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {state.editingProduct ? 'Update Product' : 'Add Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminProducts;
